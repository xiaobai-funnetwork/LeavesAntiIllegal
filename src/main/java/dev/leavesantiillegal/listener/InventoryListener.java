package dev.leavesantiillegal.listener;

import dev.leavesantiillegal.ItemChecker;
import dev.leavesantiillegal.LeavesAntiIllegalPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public final class InventoryListener implements Listener {
    private final LeavesAntiIllegalPlugin plugin;

    public InventoryListener(LeavesAntiIllegalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemChecker checker = plugin.getItemChecker();
        if (checker.shouldSkip(player)) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemChecker.CheckResult currentResult = checker.checkItem(currentItem);
        if (currentResult.isIllegal()) {
            event.setCurrentItem(null);
            event.setCancelled(true);
            sendRemovedMessage(player, currentItem, currentResult);
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemChecker.CheckResult cursorResult = checker.checkItem(cursor);
        if (cursorResult.isIllegal()) {
            event.getView().setCursor(null);
            event.setCancelled(true);
            sendRemovedMessage(player, cursor, cursorResult);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreativeInventory(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemChecker checker = plugin.getItemChecker();
        ItemStack item = event.getCursor();
        if (checker.shouldSkip(player)) {
            return;
        }
        ItemChecker.CheckResult result = checker.checkItem(item);
        if (result.isIllegal()) {
            event.setCancelled(true);
            event.setCursor(null);
            sendRemovedMessage(player, item, result);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && !plugin.getItemChecker().shouldSkip(player)) {
            plugin.getItemChecker().scanPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && !plugin.getItemChecker().shouldSkip(player)) {
            plugin.getItemChecker().scanPlayer(player);
        }
    }

    private void sendRemovedMessage(Player player, ItemStack item, ItemChecker.CheckResult result) {
        String message = plugin.getConfig().getString(
                        "messages.item-removed",
                        "&c[反作弊] &e你持有的违禁物品 &f{item} &e已被移除！ &7({reason})"
                )
                .replace("{item}", item.getType().name())
                .replace("{location}", "库存事件")
                .replace("{reason}", result.reason());
        player.sendMessage(LeavesAntiIllegalPlugin.colorize(message));
    }
}
