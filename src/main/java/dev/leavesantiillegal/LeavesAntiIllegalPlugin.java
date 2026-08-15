package dev.leavesantiillegal;

import dev.leavesantiillegal.command.AntiIllegalCommand;
import dev.leavesantiillegal.listener.InventoryListener;
import dev.leavesantiillegal.listener.PlayerListener;
import dev.leavesantiillegal.scanner.LoadedContainerScanner;
import dev.leavesantiillegal.scanner.OfflinePlayerDataScanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class LeavesAntiIllegalPlugin extends JavaPlugin {
    private static LeavesAntiIllegalPlugin instance;

    private final ConcurrentHashMap<UUID, Long> protectedPlayerDataUntil = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> playerDataLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap.KeySetView<UUID, Boolean> onlinePlayerIds = ConcurrentHashMap.newKeySet();
    private volatile ItemChecker itemChecker;
    private volatile long loginProtectionMillis;
    private volatile long logoutProtectionMillis;
    private BukkitTask onlineScanTask;
    private LoadedContainerScanner loadedContainerScanner;
    private OfflinePlayerDataScanner offlinePlayerDataScanner;

    @Override
    public void onEnable() {
        int pluginId = 33374;
        Metrics metrics = new Metrics(this, pluginId);
        instance = this;
        if (!VersionRuntime.initialize(this)) {
            return;
        }
        saveDefaultConfig();
        reloadRuleSnapshot();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        PluginCommand command = getCommand("antiillegal");
        if (command != null) {
            AntiIllegalCommand executor = new AntiIllegalCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            markPlayerOnline(player.getUniqueId());
        }
        startScanners();
        getLogger().info("LeavesAntiIllegal 已启用 - 使用 Bukkit API");
        metrics.addCustomChart(
                new Metrics.SimplePie("chart_id", () -> "My value")
        );
    }

    @Override
    public void onDisable() {
        stopScanners();
        getLogger().info("LeavesAntiIllegal 已禁用");
    }

    private void reloadRuleSnapshot() {
        itemChecker = new ItemChecker(this);
        loginProtectionMillis = Math.max(30L, getConfig().getLong(
                "scanners.offline-player-data.login-protection-seconds",
                300L
        )) * 1_000L;
        logoutProtectionMillis = Math.max(5L, getConfig().getLong(
                "scanners.offline-player-data.safe-after-logout-seconds",
                30L
        )) * 1_000L;
    }

    private void startScanners() {
        ensureMainThread();
        startOnlinePlayerScanner();
        loadedContainerScanner = new LoadedContainerScanner(this);
        loadedContainerScanner.start();
        offlinePlayerDataScanner = new OfflinePlayerDataScanner(this);
        offlinePlayerDataScanner.start();
    }

    private void stopScanners() {
        if (onlineScanTask != null) {
            onlineScanTask.cancel();
            onlineScanTask = null;
        }
        if (loadedContainerScanner != null) {
            loadedContainerScanner.stop();
            loadedContainerScanner = null;
        }
        if (offlinePlayerDataScanner != null) {
            offlinePlayerDataScanner.stop();
            offlinePlayerDataScanner = null;
        }
    }

    private void startOnlinePlayerScanner() {
        if (!getConfig().getBoolean("scanners.online-players.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(20L, getConfig().getLong(
                "scanners.online-players.interval-ticks",
                100L
        ));
        onlineScanTask = Bukkit.getScheduler().runTaskTimer(
                this,
                () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        itemChecker.scanPlayer(player);
                    }
                },
                intervalTicks,
                intervalTicks
        );
    }

    public void scanAllPlayers(CommandSender requester) {
        ensureMainThread();
        int totalRemoved = 0;
        int playersProcessed = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            totalRemoved += itemChecker.scanPlayer(player);
            playersProcessed++;
        }
        if (requester != null && (!(requester instanceof Player player) || player.isOnline())) {
            requester.sendMessage(colorize(getConfig().getString(
                            "messages.scan-complete",
                            "&a[反作弊] &e全服扫描完成，共检查 &f{players} &e名玩家，移除 &f{count} &e个违禁物品"
                    )
                    .replace("{players}", String.valueOf(playersProcessed))
                    .replace("{count}", String.valueOf(totalRemoved))));
        }
    }

    public void reloadPluginConfig(CommandSender sender) {
        Runnable reload = () -> {
            stopScanners();
            reloadConfig();
            reloadRuleSnapshot();
            startScanners();
            sender.sendMessage(colorize(getConfig().getString(
                    "messages.reload-success",
                    "&a[反作弊] &e配置与全部扫描器已重新加载"
            )));
        };
        if (Bukkit.isPrimaryThread()) {
            reload.run();
        } else {
            Bukkit.getScheduler().runTask(this, reload);
        }
    }

    public void markPlayerLoggingIn(UUID playerId) {
        ReentrantLock lock = getPlayerDataLock(playerId);
        lock.lock();
        try {
            protectedPlayerDataUntil.put(playerId, System.currentTimeMillis() + loginProtectionMillis);
        } finally {
            lock.unlock();
        }
    }

    public void markPlayerOnline(UUID playerId) {
        ReentrantLock lock = getPlayerDataLock(playerId);
        lock.lock();
        try {
            onlinePlayerIds.add(playerId);
            protectedPlayerDataUntil.put(playerId, Long.MAX_VALUE);
        } finally {
            lock.unlock();
        }
    }

    public void markPlayerLoggedOut(UUID playerId) {
        ReentrantLock lock = getPlayerDataLock(playerId);
        lock.lock();
        try {
            onlinePlayerIds.remove(playerId);
            protectedPlayerDataUntil.put(playerId, System.currentTimeMillis() + logoutProtectionMillis);
        } finally {
            lock.unlock();
        }
    }

    public boolean isPlayerDataProtected(UUID playerId) {
        if (onlinePlayerIds.contains(playerId)) {
            return true;
        }
        Long protectedUntil = protectedPlayerDataUntil.get(playerId);
        if (protectedUntil == null) {
            return false;
        }
        if (protectedUntil == Long.MAX_VALUE || protectedUntil > System.currentTimeMillis()) {
            return true;
        }
        protectedPlayerDataUntil.remove(playerId, protectedUntil);
        return false;
    }

    public ReentrantLock getPlayerDataLock(UUID playerId) {
        return playerDataLocks.computeIfAbsent(playerId, ignored -> new ReentrantLock());
    }

    public int getOnlinePlayerCount() {
        return onlinePlayerIds.size();
    }

    public ItemChecker getItemChecker() {
        return itemChecker;
    }

    public LoadedContainerScanner getLoadedContainerScanner() {
        return loadedContainerScanner;
    }

    public OfflinePlayerDataScanner getOfflinePlayerDataScanner() {
        return offlinePlayerDataScanner;
    }

    public static LeavesAntiIllegalPlugin getInstance() {
        return instance;
    }

    public static String colorize(String message) {
        return message == null ? "" : message.replace('&', '§');
    }

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Bukkit inventory/world operations must run on the Bukkit main thread");
        }
    }
}
