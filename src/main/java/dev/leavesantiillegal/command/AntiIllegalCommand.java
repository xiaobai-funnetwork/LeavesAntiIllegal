package dev.leavesantiillegal.command;

import dev.leavesantiillegal.ItemChecker;
import dev.leavesantiillegal.LeavesAntiIllegalPlugin;
import dev.leavesantiillegal.scanner.LoadedContainerScanner;
import dev.leavesantiillegal.scanner.OfflinePlayerDataScanner;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AntiIllegalCommand implements CommandExecutor, TabCompleter {
    private final LeavesAntiIllegalPlugin plugin;

    public AntiIllegalCommand(LeavesAntiIllegalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("antiillegal.admin")) {
            sender.sendMessage(LeavesAntiIllegalPlugin.colorize(plugin.getConfig().getString(
                    "messages.no-permission",
                    "&c[反作弊] &e你没有权限执行此命令"
            )));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> plugin.reloadPluginConfig(sender);
            case "scan" -> runScan(sender, args);
            case "check" -> runCheck(sender);
            case "status" -> sendStatus(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void runScan(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                        "&c[反作弊] &e玩家不在线: " + args[1]
                ));
                return;
            }
            int removed = plugin.getItemChecker().scanPlayer(target);
            sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                    "&a[反作弊] &e扫描玩家 &f" + target.getName()
                            + " &e完成，移除 &f" + removed + " &e个违禁物品"
            ));
            return;
        }
        plugin.scanAllPlayers(sender);
    }

    private void runCheck(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                    "&c[反作弊] &e此命令只能由玩家执行"
            ));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(LeavesAntiIllegalPlugin.colorize(
                    "&c[反作弊] &e你手中没有物品"
            ));
            return;
        }
        ItemChecker.CheckResult result = plugin.getItemChecker().checkItem(item);
        player.sendMessage(LeavesAntiIllegalPlugin.colorize(
                result.isIllegal()
                        ? "&c[反作弊] &e该物品为违禁物品！原因: &f" + result.reason()
                        : "&a[反作弊] &e该物品合法"
        ));
    }

    private void sendStatus(CommandSender sender) {
        LoadedContainerScanner containerScanner = plugin.getLoadedContainerScanner();
        OfflinePlayerDataScanner offlineScanner = plugin.getOfflinePlayerDataScanner();
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize("&6=== LeavesAntiIllegal 状态 ==="));
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                "&e在线玩家: &f" + Bukkit.getOnlinePlayers().size()
        ));
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                "&e玩家定时扫描: &f" + onOff(plugin.getConfig().getBoolean(
                        "scanners.online-players.enabled",
                        true
                ))
        ));
        if (containerScanner != null) {
            sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                    "&e容器扫描: &f" + onOff(containerScanner.isEnabled())
                            + " &7(跟踪区块 " + containerScanner.getTrackedChunkCount()
                            + "，已扫容器 " + containerScanner.getScannedContainers()
                            + "，移除 " + containerScanner.getRemovedItems() + ")"
            ));
        }
        if (offlineScanner != null) {
            sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                    "&e离线数据扫描: &f" + onOff(offlineScanner.isEnabled())
                            + " &7(运行中 " + (offlineScanner.isSweepRunning() ? "是" : "否")
                            + "，待处理 " + offlineScanner.getPendingFileCount()
                            + "，已检查 " + offlineScanner.getScannedFiles()
                            + "，移除 " + offlineScanner.getRemovedItems() + ")"
            ));
        }
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize(
                "&e32K / 耐久 / 堆叠 / 属性: &f"
                        + onOff(plugin.getConfig().getBoolean("check-overpowered-enchants", true)) + " / "
                        + onOff(plugin.getConfig().getBoolean("check-illegal-durability", true)) + " / "
                        + onOff(plugin.getConfig().getBoolean("check-illegal-stack-size", true)) + " / "
                        + onOff(plugin.getConfig().getBoolean("check-illegal-attributes", true))
        ));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize("&6=== LeavesAntiIllegal 帮助 ==="));
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize("&e/antiillegal reload &7- 重载配置与扫描器"));
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize("&e/antiillegal scan [玩家] &7- 扫描全部或指定在线玩家"));
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize("&e/antiillegal check &7- 检查主手物品"));
        sender.sendMessage(LeavesAntiIllegalPlugin.colorize("&e/antiillegal status &7- 查看扫描器统计"));
    }

    private String onOff(boolean enabled) {
        return enabled ? "开启" : "关闭";
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission("antiillegal.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return List.of("reload", "scan", "check", "status").stream()
                    .filter(option -> option.startsWith(prefix))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("scan")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
