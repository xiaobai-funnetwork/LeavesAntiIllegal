package dev.leavesantiillegal;

import java.lang.reflect.Method;
import java.util.Locale;
import org.bukkit.plugin.java.JavaPlugin;

public final class VersionRuntime {
    private static volatile boolean modernItemData;

    private VersionRuntime() {
    }

    public static boolean initialize(JavaPlugin plugin) {
        String bukkitVersion = plugin.getServer().getBukkitVersion();
        String implementation = implementationFor(bukkitVersion);
        if (implementation == null) {
            plugin.getLogger().severe("不支持的服务端版本: " + bukkitVersion + "，支持范围为 1.20 - 26.2");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }

        try {
            Class<?> adapterClass = Class.forName(implementation);
            Method initialize = adapterClass.getMethod("initialize", JavaPlugin.class);
            initialize.invoke(null, plugin);
            modernItemData = !implementation.endsWith("v1_20_1.VersionAdapter");
            return true;
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "无法加载服务端版本实现: " + implementation, exception);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    public static boolean supportsModernItemData() {
        return modernItemData;
    }

    private static String implementationFor(String bukkitVersion) {
        String version = bukkitVersion == null ? "" : bukkitVersion.toLowerCase(Locale.ROOT);
        if (version.startsWith("1.20.")) {
            return "dev.leavesantiillegal.version.v1_20_1.VersionAdapter";
        }
        if (version.startsWith("1.21.")) {
            return "dev.leavesantiillegal.version.v1_21.VersionAdapter";
        }
        if (version.startsWith("26.")) {
            return "dev.leavesantiillegal.version.v26_2.VersionAdapter";
        }
        return null;
    }
}
