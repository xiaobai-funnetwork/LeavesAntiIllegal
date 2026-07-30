package dev.leavesantiillegal.scanner;

import dev.leavesantiillegal.LeavesAntiIllegalPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Stream;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public final class OfflinePlayerDataScanner {
    private static final String LAST_SCAN_DATE_KEY = "last-successful-scan-date";

    private final LeavesAntiIllegalPlugin plugin;
    private final NbtItemChecker itemChecker;
    private final ConcurrentLinkedQueue<PlayerDataFile> pendingFiles = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean sweepRunning = new AtomicBoolean();
    private final AtomicLong scannedFiles = new AtomicLong();
    private final AtomicLong changedFiles = new AtomicLong();
    private final AtomicLong removedItems = new AtomicLong();
    private final boolean enabled;
    private final boolean backupBeforeWrite;
    private final boolean dryRun;
    private final boolean logEachRemoval;
    private final int maxOnlinePlayers;
    private final int filesPerBatch;
    private final int minimumDaysBetweenRuns;
    private final long batchDelayTicks;
    private final long checkIntervalSeconds;
    private final LocalTime windowStart;
    private final LocalTime windowEnd;
    private final ZoneId zoneId;
    private final String backupSuffix;
    private final Path stateFile;
    private volatile boolean active;
    private volatile LocalDate lastSuccessfulScanDate;
    private volatile Set<UUID> excludedOperators = Set.of();
    private volatile String currentWindowKey;
    private BukkitTask windowCheckTask;
    private BukkitTask batchTask;

    public OfflinePlayerDataScanner(LeavesAntiIllegalPlugin plugin) {
        this.plugin = plugin;
        itemChecker = new NbtItemChecker(plugin.getConfig());
        enabled = plugin.getConfig().getBoolean("scanners.offline-player-data.enabled", true);
        backupBeforeWrite = plugin.getConfig().getBoolean(
                "scanners.offline-player-data.backup-before-write",
                true
        );
        dryRun = plugin.getConfig().getBoolean("scanners.offline-player-data.dry-run", false);
        logEachRemoval = plugin.getConfig().getBoolean(
                "scanners.offline-player-data.log-each-removal",
                false
        );
        maxOnlinePlayers = Math.max(0, plugin.getConfig().getInt(
                "scanners.offline-player-data.max-online-players",
                5
        ));
        filesPerBatch = Math.max(1, plugin.getConfig().getInt(
                "scanners.offline-player-data.files-per-batch",
                10
        ));
        minimumDaysBetweenRuns = Math.max(1, plugin.getConfig().getInt(
                "scanners.offline-player-data.minimum-days-between-runs",
                1
        ));
        long batchDelayTicks = Math.max(1L, plugin.getConfig().getLong(
                "scanners.offline-player-data.batch-delay-ticks",
                20L
        ));
        this.batchDelayTicks = batchDelayTicks;
        checkIntervalSeconds = Math.max(30L, plugin.getConfig().getLong(
                "scanners.offline-player-data.window-check-interval-seconds",
                60L
        ));
        windowStart = parseTime(
                plugin.getConfig().getString("scanners.offline-player-data.window-start", "04:00"),
                LocalTime.of(4, 0)
        );
        windowEnd = parseTime(
                plugin.getConfig().getString("scanners.offline-player-data.window-end", "06:00"),
                LocalTime.of(6, 0)
        );
        zoneId = parseZone(plugin.getConfig().getString(
                "scanners.offline-player-data.time-zone",
                "Asia/Shanghai"
        ));
        backupSuffix = sanitizeSuffix(plugin.getConfig().getString(
                "scanners.offline-player-data.backup-suffix",
                ".fai.bak"
        ));
        stateFile = plugin.getDataFolder().toPath().resolve("scanner-state.properties");
        lastSuccessfulScanDate = loadLastSuccessfulDate();
    }

    public void start() {
        if (!enabled || active) {
            return;
        }
        active = true;
        windowCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::checkWindow,
                200L,
                checkIntervalSeconds * 20L
        );
    }

    public void stop() {
        active = false;
        sweepRunning.set(false);
        pendingFiles.clear();
        if (windowCheckTask != null) {
            windowCheckTask.cancel();
            windowCheckTask = null;
        }
        if (batchTask != null) {
            batchTask.cancel();
            batchTask = null;
        }
    }

    private void checkWindow() {
        if (!active || sweepRunning.get()) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate windowDate = getWindowDate(now);
        if (windowDate == null || !isDue(windowDate)) {
            return;
        }
        if (plugin.getOnlinePlayerCount() > maxOnlinePlayers) {
            return;
        }
        if (!sweepRunning.compareAndSet(false, true)) {
            return;
        }
        currentWindowKey = windowDate.toString();
        Bukkit.getScheduler().runTask(plugin, this::prepareSweepOnMainThread);
    }

    private void prepareSweepOnMainThread() {
        if (!active) {
            sweepRunning.set(false);
            return;
        }
        Set<UUID> operators = new HashSet<>();
        for (OfflinePlayer operator : Bukkit.getOperators()) {
            operators.add(operator.getUniqueId());
        }
        excludedOperators = Set.copyOf(operators);

        List<PlayerDataRoot> roots = new ArrayList<>();
        Set<Path> seenPaths = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            Path playerData = world.getWorldFolder().toPath().resolve("playerdata").toAbsolutePath().normalize();
            if (seenPaths.add(playerData)) {
                roots.add(new PlayerDataRoot(world.getName(), playerData));
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> enumerateFiles(roots));
    }

    private void enumerateFiles(List<PlayerDataRoot> roots) {
        if (!active) {
            finishSweep(false);
            return;
        }
        try {
            for (PlayerDataRoot root : roots) {
                if (!Files.isDirectory(root.path())) {
                    continue;
                }
                try (Stream<Path> files = Files.list(root.path())) {
                    files.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".dat"))
                            .sorted()
                            .forEach(path -> addPlayerDataFile(root.worldName(), path));
                }
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "枚举离线玩家数据文件失败", exception);
            finishSweep(false);
            return;
        }

        if (!active) {
            finishSweep(false);
            return;
        }
        plugin.getLogger().info("低峰离线数据扫描开始，共发现 " + pendingFiles.size() + " 个玩家数据文件");
        if (pendingFiles.isEmpty()) {
            finishSweep(true);
            return;
        }
        batchTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::processBatch,
                1L,
                batchDelayTicks
        );
    }

    private void addPlayerDataFile(String worldName, Path path) {
        String fileName = path.getFileName().toString();
        String uuidText = fileName.substring(0, fileName.length() - 4);
        try {
            pendingFiles.add(new PlayerDataFile(worldName, UUID.fromString(uuidText), path));
        } catch (IllegalArgumentException ignored) {
            // 非 UUID 的 .dat 文件不属于标准玩家数据，不处理。
        }
    }

    private void processBatch() {
        LocalDate activeWindowDate = getWindowDate(ZonedDateTime.now(zoneId));
        if (!active || activeWindowDate == null || !activeWindowDate.toString().equals(currentWindowKey)) {
            cancelBatchTask();
            finishSweep(false);
            return;
        }
        if (plugin.getOnlinePlayerCount() > maxOnlinePlayers) {
            return;
        }
        for (int index = 0; index < filesPerBatch; index++) {
            PlayerDataFile playerDataFile = pendingFiles.poll();
            if (playerDataFile == null) {
                cancelBatchTask();
                finishSweep(true);
                return;
            }
            processFile(playerDataFile);
        }
    }

    private void cancelBatchTask() {
        BukkitTask task = batchTask;
        batchTask = null;
        if (task != null) {
            task.cancel();
        }
    }

    private void processFile(PlayerDataFile playerDataFile) {
        UUID playerId = playerDataFile.playerId();
        if (excludedOperators.contains(playerId) || plugin.getItemChecker().isWhitelisted(playerId)) {
            return;
        }

        ReentrantLock lock = plugin.getPlayerDataLock(playerId);
        lock.lock();
        try {
            if (plugin.isPlayerDataProtected(playerId)) {
                return;
            }
            scanAndUpdateFile(playerDataFile);
        } finally {
            lock.unlock();
        }
    }

    private void scanAndUpdateFile(PlayerDataFile playerDataFile) {
        Path file = playerDataFile.path();
        Path temporaryFile = file.resolveSibling(file.getFileName() + ".fai.tmp");
        try {
            NamedTag namedTag = NBTUtil.read(file.toFile());
            if (!(namedTag.getTag() instanceof CompoundTag playerData)) {
                plugin.getLogger().warning("跳过非 Compound NBT 玩家文件: " + file);
                return;
            }

            List<NbtItemChecker.NbtViolation> violations = new ArrayList<>();
            int removed = itemChecker.sanitizePlayerData(playerData, violations::add);
            scannedFiles.incrementAndGet();
            if (removed == 0) {
                return;
            }

            if (!active) {
                return;
            }

            if (logEachRemoval) {
                for (NbtItemChecker.NbtViolation violation : violations) {
                    plugin.getLogger().warning("[离线违禁物] 玩家 " + playerDataFile.playerId()
                            + "，世界 " + playerDataFile.worldName()
                            + "，位置 " + violation.path()
                            + "，物品 " + violation.itemId() + " x" + violation.amount()
                            + "，原因: " + violation.reason());
                }
            }

            if (!dryRun) {
                if (backupBeforeWrite) {
                    Files.copy(
                            file,
                            file.resolveSibling(file.getFileName() + backupSuffix),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
                NBTUtil.write(namedTag, temporaryFile.toFile());
                replaceAtomically(temporaryFile, file);
                changedFiles.incrementAndGet();
            }
            removedItems.addAndGet(removed);
            plugin.getLogger().warning("[离线数据扫描] 玩家 " + playerDataFile.playerId()
                    + (dryRun ? " 检出 " : " 已移除 ") + removed + " 个违禁物品");
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "扫描离线玩家数据失败: " + file, exception);
        } finally {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "无法删除离线扫描临时文件: " + temporaryFile, exception);
            }
        }
    }

    private void replaceAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void finishSweep(boolean successful) {
        pendingFiles.clear();
        if (successful && currentWindowKey != null) {
            lastSuccessfulScanDate = LocalDate.parse(currentWindowKey);
            saveLastSuccessfulDate(lastSuccessfulScanDate);
            plugin.getLogger().info("低峰离线数据扫描完成：累计检查 " + scannedFiles.get()
                    + " 个文件，修改 " + changedFiles.get()
                    + " 个文件，移除 " + removedItems.get() + " 个违禁物品");
        }
        currentWindowKey = null;
        sweepRunning.set(false);
    }

    private LocalDate getWindowDate(ZonedDateTime now) {
        LocalTime current = now.toLocalTime();
        if (windowStart.equals(windowEnd)) {
            return now.toLocalDate();
        }
        if (windowStart.isBefore(windowEnd)) {
            return !current.isBefore(windowStart) && current.isBefore(windowEnd)
                    ? now.toLocalDate()
                    : null;
        }
        if (!current.isBefore(windowStart)) {
            return now.toLocalDate();
        }
        if (current.isBefore(windowEnd)) {
            return now.toLocalDate().minusDays(1);
        }
        return null;
    }

    private boolean isDue(LocalDate windowDate) {
        return lastSuccessfulScanDate == null
                || ChronoUnit.DAYS.between(lastSuccessfulScanDate, windowDate) >= minimumDaysBetweenRuns;
    }

    private LocalDate loadLastSuccessfulDate() {
        if (!Files.isRegularFile(stateFile)) {
            return null;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(stateFile)) {
            properties.load(input);
            String value = properties.getProperty(LAST_SCAN_DATE_KEY);
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "读取离线扫描状态失败，将允许重新扫描", exception);
            return null;
        }
    }

    private void saveLastSuccessfulDate(LocalDate date) {
        Properties properties = new Properties();
        properties.setProperty(LAST_SCAN_DATE_KEY, date.toString());
        try {
            Files.createDirectories(stateFile.getParent());
            try (OutputStream output = Files.newOutputStream(stateFile)) {
                properties.store(output, "LeavesAntiIllegal offline scanner state");
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "保存离线扫描状态失败", exception);
        }
    }

    private LocalTime parseTime(String value, LocalTime fallback) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeException exception) {
            plugin.getLogger().warning("无效的低峰时间 " + value + "，改用 " + fallback);
            return fallback;
        }
    }

    private ZoneId parseZone(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("system")) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(value);
        } catch (DateTimeException exception) {
            plugin.getLogger().warning("无效的时区 " + value + "，改用系统时区 " + ZoneId.systemDefault());
            return ZoneId.systemDefault();
        }
    }

    private String sanitizeSuffix(String suffix) {
        if (suffix == null || suffix.isBlank() || suffix.contains("/") || suffix.contains("\\")) {
            return ".fai.bak";
        }
        return suffix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSweepRunning() {
        return sweepRunning.get();
    }

    public int getPendingFileCount() {
        return pendingFiles.size();
    }

    public long getScannedFiles() {
        return scannedFiles.get();
    }

    public long getChangedFiles() {
        return changedFiles.get();
    }

    public long getRemovedItems() {
        return removedItems.get();
    }

    private record PlayerDataRoot(String worldName, Path path) {
    }

    private record PlayerDataFile(String worldName, UUID playerId, Path path) {
    }
}
