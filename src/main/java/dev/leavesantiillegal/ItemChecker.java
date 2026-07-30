package dev.leavesantiillegal;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemChecker {
    private final LeavesAntiIllegalPlugin plugin;
    private final Set<Material> bannedMaterials = new HashSet<>();
    private final Map<Enchantment, Integer> customEnchantLimits = new HashMap<>();
    private final Set<UUID> whitelistedPlayers = new HashSet<>();
    private final boolean checkOverpoweredEnchants;
    private final int enchantMaxMultiplier;
    private final boolean checkIllegalDurability;
    private final boolean checkIllegalStackSize;
    private final boolean checkIllegalAttributes;
    private final boolean checkUnbreakable;
    private final boolean notifyAdmins;
    private final boolean logToConsole;
    private final boolean scanPlayerInventory;
    private final boolean scanEnderChest;
    private final boolean scanCursor;
    private final boolean scanNestedContainers;
    private final int maxNestedDepth;
    private final double maxAttackDamage;
    private final double maxAttackSpeed;
    private final double maxHealth;
    private final double maxMovementSpeed;
    private final double maxArmor;
    private final double maxArmorToughness;
    private final double maxKnockbackResistance;

    public ItemChecker(LeavesAntiIllegalPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        for (String name : config.getStringList("banned-materials")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                plugin.getLogger().warning("无效的违禁材料名称: " + name);
            } else {
                bannedMaterials.add(material);
            }
        }

        checkOverpoweredEnchants = config.getBoolean("check-overpowered-enchants", true);
        enchantMaxMultiplier = Math.max(1, config.getInt("enchant-max-multiplier", 1));
        checkIllegalDurability = config.getBoolean("check-illegal-durability", true);
        checkIllegalStackSize = config.getBoolean("check-illegal-stack-size", true);
        checkIllegalAttributes = config.getBoolean("check-illegal-attributes", true);
        checkUnbreakable = config.getBoolean("check-unbreakable", true);
        notifyAdmins = config.getBoolean("notify-admins", true);
        logToConsole = config.getBoolean("log-to-console", true);

        ConfigurationSection customLimits = config.getConfigurationSection("custom-enchant-limits");
        if (customLimits != null) {
            for (String key : customLimits.getKeys(false)) {
                Enchantment enchantment = Bukkit.getRegistry(Enchantment.class).match(key);
                if (enchantment != null) {
                    customEnchantLimits.put(enchantment, customLimits.getInt(key));
                } else {
                    plugin.getLogger().warning("无效的自定义附魔名称: " + key);
                }
            }
        }

        maxAttackDamage = config.getDouble("attribute-limits.attack-damage", 50.0);
        maxAttackSpeed = config.getDouble("attribute-limits.attack-speed", 10.0);
        maxHealth = config.getDouble("attribute-limits.max-health", 200.0);
        maxMovementSpeed = config.getDouble("attribute-limits.movement-speed", 1.0);
        maxArmor = config.getDouble("attribute-limits.armor", 30.0);
        maxArmorToughness = config.getDouble("attribute-limits.armor-toughness", 20.0);
        maxKnockbackResistance = config.getDouble("attribute-limits.knockback-resistance", 1.0);

        for (String uuidText : config.getStringList("whitelisted-players")) {
            try {
                whitelistedPlayers.add(UUID.fromString(uuidText));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("无效的白名单 UUID: " + uuidText);
            }
        }

        scanPlayerInventory = config.getBoolean("scanners.online-players.scan-inventory", true);
        scanEnderChest = config.getBoolean("scanners.online-players.scan-ender-chest", true);
        scanCursor = config.getBoolean("scanners.online-players.scan-cursor", true);
        scanNestedContainers = config.getBoolean("scanners.nested-containers.enabled", true);
        maxNestedDepth = Math.max(1, config.getInt("scanners.nested-containers.max-depth", 3));
    }

    public boolean shouldSkip(Player player) {
        return player.hasPermission("antiillegal.bypass")
                || player.isOp()
                || whitelistedPlayers.contains(player.getUniqueId());
    }

    public boolean isWhitelisted(UUID playerId) {
        return whitelistedPlayers.contains(playerId);
    }

    public int scanPlayer(Player player) {
        if (shouldSkip(player)) {
            return 0;
        }

        ViolationHandler handler = (item, result, path) -> handlePlayerViolation(player, item, result, path);
        int removed = 0;
        if (scanPlayerInventory) {
            removed += scanInventory(player.getInventory(), handler, "玩家背包", 0);
        }
        if (scanEnderChest) {
            removed += scanInventory(player.getEnderChest(), handler, "末影箱", 0);
        }
        if (scanCursor) {
            ItemStack cursor = player.getItemOnCursor();
            if (cursor != null && !cursor.getType().isAir()) {
                CheckResult result = checkItem(cursor);
                if (result.isIllegal()) {
                    player.setItemOnCursor(null);
                    removed++;
                    handler.handle(cursor, result, "光标物品");
                } else {
                    removed += scanNestedItem(cursor, handler, "光标物品", 1);
                    player.setItemOnCursor(cursor);
                }
            }
        }
        return removed;
    }

    public int scanContainer(Inventory inventory, String source) {
        return scanInventory(
                inventory,
                (item, result, path) -> handleContainerViolation(item, result, path),
                source,
                0
        );
    }

    private int scanInventory(Inventory inventory, ViolationHandler handler, String path, int depth) {
        int removed = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            String itemPath = path + " 槽位 " + slot;
            CheckResult result = checkItem(item);
            if (result.isIllegal()) {
                inventory.clear(slot);
                removed++;
                handler.handle(item, result, itemPath);
                continue;
            }

            int nestedRemoved = scanNestedItem(item, handler, itemPath, depth + 1);
            if (nestedRemoved > 0) {
                inventory.setItem(slot, item);
                removed += nestedRemoved;
            }
        }
        return removed;
    }

    private int scanNestedItem(ItemStack outerItem, ViolationHandler handler, String path, int depth) {
        if (!scanNestedContainers || depth > maxNestedDepth || !outerItem.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = outerItem.getItemMeta();
        int removed = 0;
        if (meta instanceof BlockStateMeta blockStateMeta && blockStateMeta.hasBlockState()) {
            BlockState state = blockStateMeta.getBlockState();
            if (state instanceof InventoryHolder holder) {
                removed += scanInventory(holder.getInventory(), handler, path + " -> 容器物品", depth);
                if (removed > 0) {
                    blockStateMeta.setBlockState(state);
                    outerItem.setItemMeta(blockStateMeta);
                }
            }
        } else if (meta instanceof BundleMeta bundleMeta && bundleMeta.hasItems()) {
            List<ItemStack> items = new ArrayList<>(bundleMeta.getItems());
            for (int index = items.size() - 1; index >= 0; index--) {
                ItemStack item = items.get(index);
                String itemPath = path + " -> 收纳袋槽位 " + index;
                CheckResult result = checkItem(item);
                if (result.isIllegal()) {
                    items.remove(index);
                    removed++;
                    handler.handle(item, result, itemPath);
                } else {
                    removed += scanNestedItem(item, handler, itemPath, depth + 1);
                }
            }
            if (removed > 0) {
                bundleMeta.setItems(items);
                outerItem.setItemMeta(bundleMeta);
            }
        }
        return removed;
    }

    public CheckResult checkItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return CheckResult.LEGAL;
        }
        if (bannedMaterials.contains(item.getType())) {
            return new CheckResult(true, "违禁物品类型: " + item.getType().name());
        }

        if (checkOverpoweredEnchants) {
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                CheckResult result = checkEnchant(entry.getKey(), entry.getValue());
                if (result.isIllegal()) {
                    return result;
                }
            }
            if (item.hasItemMeta()) {
                for (Map.Entry<Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                    CheckResult result = checkEnchant(entry.getKey(), entry.getValue());
                    if (result.isIllegal()) {
                        return result;
                    }
                }
            }
        }

        if (checkIllegalDurability && item.hasItemMeta() && item.getItemMeta() instanceof Damageable damageable) {
            int damage = damageable.getDamage();
            short maxDurability = item.getType().getMaxDurability();
            if (maxDurability > 0 && damage < 0) {
                return new CheckResult(true, "非法耐久度: 负数伤害值 " + damage);
            }
            if (maxDurability > 0 && damage > maxDurability + 10) {
                return new CheckResult(true, "非法耐久度: " + damage + " (最大 " + maxDurability + ")");
            }
        }

        if (checkIllegalStackSize) {
            int maxStack = item.getMaxStackSize();
            if (maxStack > 0 && item.getAmount() > maxStack) {
                return new CheckResult(true, "非法堆叠: " + item.getAmount() + " (最大 " + maxStack + ")");
            }
            if (item.getAmount() < 0) {
                return new CheckResult(true, "非法堆叠: 负数数量 " + item.getAmount());
            }
        }

        if (checkIllegalAttributes && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
            if (modifiers != null) {
                for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                    double amount = entry.getValue().getAmount();
                    Double limit = getAttributeLimit(entry.getKey());
                    if (limit != null && Math.abs(amount) > limit) {
                        return new CheckResult(true, "非法属性: " + attributeKey(entry.getKey())
                                + " = " + amount + " (上限 " + limit + ")");
                    }
                }
            }
        }

        if (checkUnbreakable && item.hasItemMeta() && item.getItemMeta().isUnbreakable()) {
            return new CheckResult(true, "不可破坏物品");
        }
        return CheckResult.LEGAL;
    }

    private CheckResult checkEnchant(Enchantment enchantment, int level) {
        int maxLevel = customEnchantLimits.getOrDefault(
                enchantment,
                enchantment.getMaxLevel() * enchantMaxMultiplier
        );
        if (level > maxLevel) {
            return new CheckResult(true, "超级附魔: " + enchantment.getKey().getKey()
                    + " 等级 " + level + " (上限 " + maxLevel + ")");
        }
        if (level < 0) {
            return new CheckResult(true, "非法附魔等级: " + enchantment.getKey().getKey() + " 等级 " + level);
        }
        return CheckResult.LEGAL;
    }

    private Double getAttributeLimit(Attribute attribute) {
        return switch (attributeKey(attribute)) {
            case "attack_damage", "generic.attack_damage" -> maxAttackDamage;
            case "attack_speed", "generic.attack_speed" -> maxAttackSpeed;
            case "max_health", "generic.max_health" -> maxHealth;
            case "movement_speed", "generic.movement_speed" -> maxMovementSpeed;
            case "armor", "generic.armor" -> maxArmor;
            case "armor_toughness", "generic.armor_toughness" -> maxArmorToughness;
            case "knockback_resistance", "generic.knockback_resistance" -> maxKnockbackResistance;
            default -> null;
        };
    }

    private String attributeKey(Attribute attribute) {
        return attribute.getKey().getKey();
    }

    private void handlePlayerViolation(Player player, ItemStack item, CheckResult result, String path) {
        String itemName = item.getType().name();
        String playerMessage = plugin.getConfig().getString(
                        "messages.item-removed",
                        "&c[反作弊] &e你持有的违禁物品 &f{item} &e已被移除！"
                )
                .replace("{item}", itemName)
                .replace("{location}", path)
                .replace("{reason}", result.reason());
        player.sendMessage(LeavesAntiIllegalPlugin.colorize(playerMessage));

        notifyAdmins(plugin.getConfig().getString(
                        "messages.admin-notify",
                        "&c[反作弊] &e玩家 &f{player} &e持有违禁物品 &f{item} &e(x{amount})，已自动移除"
                )
                .replace("{player}", player.getName())
                .replace("{item}", itemName)
                .replace("{amount}", String.valueOf(item.getAmount()))
                .replace("{location}", path)
                .replace("{reason}", result.reason()), player);

        logViolation("玩家 " + player.getName() + " 的 " + path, item, result);
    }

    private void handleContainerViolation(ItemStack item, CheckResult result, String path) {
        notifyAdmins(plugin.getConfig().getString(
                        "messages.container-admin-notify",
                        "&c[反作弊] &e容器 &f{location} &e中的违禁物品 &f{item} &e(x{amount}) 已移除"
                )
                .replace("{item}", item.getType().name())
                .replace("{amount}", String.valueOf(item.getAmount()))
                .replace("{location}", path)
                .replace("{reason}", result.reason()), null);
        logViolation("容器 " + path, item, result);
    }

    private void notifyAdmins(String message, Player excluded) {
        if (!notifyAdmins) {
            return;
        }
        String colored = LeavesAntiIllegalPlugin.colorize(message);
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (!admin.hasPermission("antiillegal.notify") || admin.equals(excluded)) {
                continue;
            }
            admin.sendMessage(colored);
        }
    }

    private void logViolation(String source, ItemStack item, CheckResult result) {
        if (logToConsole) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "[违禁物品] {0} 持有 {1} x{2} - 原因: {3}",
                    new Object[]{source, item.getType().name(), item.getAmount(), result.reason()}
            );
        }
    }

    @FunctionalInterface
    private interface ViolationHandler {
        void handle(ItemStack item, CheckResult result, String path);
    }

    public record CheckResult(boolean illegal, String reason) {
        public static final CheckResult LEGAL = new CheckResult(false, null);

        public boolean isIllegal() {
            return illegal;
        }
    }
}
