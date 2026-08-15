package dev.leavesantiillegal.scanner;

import dev.leavesantiillegal.LeavesAntiIllegalPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

public final class LoadedContainerScanner implements Listener {
    private final LeavesAntiIllegalPlugin plugin;
    private final Set<ChunkRef> loadedChunks = new HashSet<>();
    private final boolean enabled;
    private final boolean scanBlockContainers;
    private final boolean scanEntityContainers;
    private final int chunksPerRun;
    private final long intervalTicks;
    private final Set<String> includedWorlds;
    private int cursor;
    private long scannedChunks;
    private long scannedContainers;
    private long removedItems;
    private boolean active;
    private BukkitTask repeatingTask;

    public LoadedContainerScanner(LeavesAntiIllegalPlugin plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getBoolean("scanners.loaded-containers.enabled", true);
        scanBlockContainers = plugin.getConfig().getBoolean(
                "scanners.loaded-containers.scan-block-containers",
                true
        );
        scanEntityContainers = plugin.getConfig().getBoolean(
                "scanners.loaded-containers.scan-entity-containers",
                true
        );
        chunksPerRun = Math.max(1, plugin.getConfig().getInt(
                "scanners.loaded-containers.chunks-per-run",
                16
        ));
        intervalTicks = Math.max(20L, plugin.getConfig().getLong(
                "scanners.loaded-containers.interval-ticks",
                200L
        ));
        includedWorlds = Set.copyOf(plugin.getConfig().getStringList(
                "scanners.loaded-containers.included-worlds"
        ));
    }

    public void start() {
        if (!enabled || active) {
            return;
        }
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Loaded container scanner must start on the Bukkit main thread");
        }
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (World world : Bukkit.getWorlds()) {
            if (!shouldScan(world)) {
                continue;
            }
            for (Chunk chunk : world.getLoadedChunks()) {
                loadedChunks.add(new ChunkRef(world, chunk.getX(), chunk.getZ()));
            }
        }
        repeatingTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::scanBatch,
                Math.min(intervalTicks, 100L),
                intervalTicks
        );
    }

    public void stop() {
        active = false;
        if (repeatingTask != null) {
            repeatingTask.cancel();
            repeatingTask = null;
        }
        HandlerList.unregisterAll(this);
        loadedChunks.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (active && shouldScan(event.getWorld())) {
            Chunk chunk = event.getChunk();
            loadedChunks.add(new ChunkRef(event.getWorld(), chunk.getX(), chunk.getZ()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        loadedChunks.remove(new ChunkRef(event.getWorld(), chunk.getX(), chunk.getZ()));
    }

    private void scanBatch() {
        if (!active || loadedChunks.isEmpty()) {
            return;
        }
        List<ChunkRef> snapshot = new ArrayList<>(loadedChunks);
        int batchSize = Math.min(chunksPerRun, snapshot.size());
        int startIndex = Math.floorMod(cursor, snapshot.size());
        cursor = (startIndex + batchSize) % snapshot.size();
        for (int offset = 0; offset < batchSize; offset++) {
            scanChunk(snapshot.get((startIndex + offset) % snapshot.size()));
        }
    }

    private void scanChunk(ChunkRef ref) {
        if (!active || !ref.world().isChunkLoaded(ref.x(), ref.z())) {
            loadedChunks.remove(ref);
            return;
        }

        Chunk chunk = ref.world().getChunkAt(ref.x(), ref.z());
        Set<Inventory> seenInventories = Collections.newSetFromMap(new IdentityHashMap<>());
        if (scanBlockContainers) {
            for (BlockState state : chunk.getTileEntities()) {
                if (!(state instanceof InventoryHolder holder)) {
                    continue;
                }
                Inventory inventory = holder.getInventory();
                if (seenInventories.add(inventory)) {
                    scanInventory(inventory, formatLocation(state.getLocation()) + " (" + state.getType() + ")");
                }
            }
        }

        if (scanEntityContainers && chunk.isEntitiesLoaded()) {
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Player || !(entity instanceof InventoryHolder holder)) {
                    continue;
                }
                Inventory inventory = holder.getInventory();
                if (seenInventories.add(inventory)) {
                    scanInventory(
                            inventory,
                            formatLocation(entity.getLocation()) + " (" + entity.getType() + ", "
                                    + entity.getUniqueId() + ")"
                    );
                }
            }
        }
        scannedChunks++;
    }

    private void scanInventory(Inventory inventory, String source) {
        try {
            scannedContainers++;
            removedItems += plugin.getItemChecker().scanContainer(inventory, source);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "扫描已加载容器失败: " + source, exception);
        }
    }

    private boolean shouldScan(World world) {
        return includedWorlds.isEmpty() || includedWorlds.contains(world.getName());
    }

    private String formatLocation(Location location) {
        return location.getWorld().getName() + " "
                + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTrackedChunkCount() {
        return loadedChunks.size();
    }

    public long getScannedChunks() {
        return scannedChunks;
    }

    public long getScannedContainers() {
        return scannedContainers;
    }

    public long getRemovedItems() {
        return removedItems;
    }

    private record ChunkRef(World world, int x, int z) {
    }
}
