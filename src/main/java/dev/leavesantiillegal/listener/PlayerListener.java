package dev.leavesantiillegal.listener;

import dev.leavesantiillegal.ItemChecker;
import dev.leavesantiillegal.LeavesAntiIllegalPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerListener implements Listener {
    private final LeavesAntiIllegalPlugin plugin;

    public PlayerListener(LeavesAntiIllegalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        plugin.markPlayerLoggingIn(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLoginComplete(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            plugin.markPlayerLoggedOut(event.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.markPlayerOnline(player.getUniqueId());
        plugin.getItemChecker().scanPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.markPlayerLoggedOut(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemChecker checker = plugin.getItemChecker();
        if (checker.shouldSkip(player)) {
            return;
        }
        ItemStack item = event.getItem().getItemStack();
        ItemChecker.CheckResult result = checker.checkItem(item);
        if (result.isIllegal()) {
            event.setCancelled(true);
            event.getItem().remove();
            sendRemovedMessage(player, item, result);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemChecker checker = plugin.getItemChecker();
        if (checker.shouldSkip(player)) {
            return;
        }
        ItemStack item = event.getItemDrop().getItemStack();
        ItemChecker.CheckResult result = checker.checkItem(item);
        if (result.isIllegal()) {
            event.getItemDrop().remove();
            sendRemovedMessage(player, item, result);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemChecker checker = plugin.getItemChecker();
        if (checker.shouldSkip(player)) {
            return;
        }
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null) {
            return;
        }
        ItemChecker.CheckResult result = checker.checkItem(item);
        if (result.isIllegal()) {
            player.getInventory().clear(event.getNewSlot());
            sendRemovedMessage(player, item, result);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemChecker checker = plugin.getItemChecker();
        if (checker.shouldSkip(player) || event.getItem() == null) {
            return;
        }
        ItemStack item = event.getItem();
        ItemChecker.CheckResult result = checker.checkItem(item);
        if (result.isIllegal()) {
            event.setCancelled(true);
            player.getInventory().removeItemAnySlot(item);
            sendRemovedMessage(player, item, result);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemChecker checker = plugin.getItemChecker();
        if (checker.shouldSkip(player)) {
            return;
        }
        ItemStack mainHand = event.getMainHandItem();
        ItemChecker.CheckResult mainResult = checker.checkItem(mainHand);
        if (mainResult.isIllegal()) {
            event.setCancelled(true);
            player.getInventory().removeItemAnySlot(mainHand);
            sendRemovedMessage(player, mainHand, mainResult);
            return;
        }
        ItemStack offHand = event.getOffHandItem();
        ItemChecker.CheckResult offResult = checker.checkItem(offHand);
        if (offResult.isIllegal()) {
            event.setCancelled(true);
            player.getInventory().setItemInOffHand(null);
            sendRemovedMessage(player, offHand, offResult);
        }
    }

    private void sendRemovedMessage(Player player, ItemStack item, ItemChecker.CheckResult result) {
        String message = plugin.getConfig().getString(
                        "messages.item-removed",
                        "&c[反作弊] &e你持有的违禁物品 &f{item} &e已被移除！ &7({reason})"
                )
                .replace("{item}", item.getType().name())
                .replace("{location}", "实时事件")
                .replace("{reason}", result.reason());
        player.sendMessage(LeavesAntiIllegalPlugin.colorize(message));
    }
}
