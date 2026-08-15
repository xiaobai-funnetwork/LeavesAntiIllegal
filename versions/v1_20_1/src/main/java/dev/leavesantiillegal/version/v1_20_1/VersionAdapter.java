package dev.leavesantiillegal.version.v1_20_1;

import org.bukkit.plugin.java.JavaPlugin;

public final class VersionAdapter {
    private VersionAdapter() {
    }

    public static void initialize(JavaPlugin plugin) {
        plugin.getLogger().info("已加载 1.20.x 版本实现");
    }
}
