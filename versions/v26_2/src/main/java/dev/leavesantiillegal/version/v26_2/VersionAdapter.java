package dev.leavesantiillegal.version.v26_2;

import org.bukkit.plugin.java.JavaPlugin;

public final class VersionAdapter {
    private VersionAdapter() {
    }

    public static void initialize(JavaPlugin plugin) {
        plugin.getLogger().info("已加载 26.2.x 版本实现");
    }
}
