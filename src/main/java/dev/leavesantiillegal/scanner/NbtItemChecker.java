package dev.leavesantiillegal.scanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.NumberTag;
import net.querz.nbt.tag.Tag;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

public final class NbtItemChecker {
    private final Set<String> bannedMaterials = new HashSet<>();
    private final Map<String, Integer> maxStackSizes = new HashMap<>();
    private final Map<String, Integer> maxDurabilities = new HashMap<>();
    private final Map<String, Integer> enchantLimits = new HashMap<>();
    private final Map<String, Double> attributeLimits = new HashMap<>();
    private final boolean checkOverpoweredEnchants;
    private final boolean checkIllegalDurability;
    private final boolean checkIllegalStackSize;
    private final boolean checkIllegalAttributes;
    private final boolean checkUnbreakable;
    private final boolean scanNestedContainers;
    private final int maxNestedDepth;
    private final int unknownEnchantMaxLevel;

    public NbtItemChecker(FileConfiguration config) {
        this(config, loadRegisteredEnchantLimits(config));
    }

    NbtItemChecker(FileConfiguration config, Map<String, Integer> registeredEnchantLimits) {
        for (String material : config.getStringList("banned-materials")) {
            bannedMaterials.add(normalizeId(material));
        }
        for (Material material : Material.values()) {
            String id = normalizeId(material.name());
            maxStackSizes.put(id, material.getMaxStackSize());
            maxDurabilities.put(id, (int) material.getMaxDurability());
        }

        enchantLimits.putAll(registeredEnchantLimits);
        ConfigurationSection customLimits = config.getConfigurationSection("custom-enchant-limits");
        if (customLimits != null) {
            for (String configuredName : customLimits.getKeys(false)) {
                int limit = customLimits.getInt(configuredName);
                String normalized = configuredName.toLowerCase(Locale.ROOT);
                enchantLimits.put(normalized, limit);
                enchantLimits.put(normalizeId(normalized), limit);
            }
        }

        attributeLimits.put("attack_damage", config.getDouble("attribute-limits.attack-damage", 50.0));
        attributeLimits.put("attack_speed", config.getDouble("attribute-limits.attack-speed", 10.0));
        attributeLimits.put("max_health", config.getDouble("attribute-limits.max-health", 200.0));
        attributeLimits.put("movement_speed", config.getDouble("attribute-limits.movement-speed", 1.0));
        attributeLimits.put("armor", config.getDouble("attribute-limits.armor", 30.0));
        attributeLimits.put("armor_toughness", config.getDouble("attribute-limits.armor-toughness", 20.0));
        attributeLimits.put(
                "knockback_resistance",
                config.getDouble("attribute-limits.knockback-resistance", 1.0)
        );

        checkOverpoweredEnchants = config.getBoolean("check-overpowered-enchants", true);
        checkIllegalDurability = config.getBoolean("check-illegal-durability", true);
        checkIllegalStackSize = config.getBoolean("check-illegal-stack-size", true);
        checkIllegalAttributes = config.getBoolean("check-illegal-attributes", true);
        checkUnbreakable = config.getBoolean("check-unbreakable", true);
        scanNestedContainers = config.getBoolean("scanners.nested-containers.enabled", true);
        maxNestedDepth = Math.max(1, config.getInt("scanners.nested-containers.max-depth", 3));
        unknownEnchantMaxLevel = Math.max(1, config.getInt(
                "scanners.offline-player-data.unknown-enchant-max-level",
                10
        ));
    }

    public int sanitizePlayerData(CompoundTag playerData, Consumer<NbtViolation> violationConsumer) {
        int removed = 0;
        removed += sanitizeNamedList(playerData, "Inventory", "离线背包", 0, violationConsumer);
        removed += sanitizeNamedList(playerData, "EnderItems", "离线末影箱", 0, violationConsumer);
        return removed;
    }

    private int sanitizeNamedList(
            CompoundTag parent,
            String key,
            String path,
            int depth,
            Consumer<NbtViolation> violationConsumer
    ) {
        ListTag<?> rawList = parent.getListTag(key);
        if (rawList == null || rawList.getTypeClass() != CompoundTag.class) {
            return 0;
        }
        return sanitizeDirectItemList(rawList.asCompoundTagList(), path, depth, violationConsumer);
    }

    private int sanitizeDirectItemList(
            ListTag<CompoundTag> items,
            String path,
            int depth,
            Consumer<NbtViolation> violationConsumer
    ) {
        int removed = 0;
        for (int index = items.size() - 1; index >= 0; index--) {
            CompoundTag item = items.get(index);
            String itemPath = path + " 槽位 " + getSlot(item, index);
            String reason = checkItem(item);
            if (reason != null) {
                items.remove(index);
                removed++;
                violationConsumer.accept(toViolation(item, itemPath, reason));
            } else {
                removed += sanitizeNestedItem(item, itemPath, depth + 1, violationConsumer);
            }
        }
        return removed;
    }

    private int sanitizeContainerEntries(
            ListTag<CompoundTag> entries,
            String path,
            int depth,
            Consumer<NbtViolation> violationConsumer
    ) {
        int removed = 0;
        for (int index = entries.size() - 1; index >= 0; index--) {
            CompoundTag entry = entries.get(index);
            CompoundTag item = entry.getCompoundTag("item");
            if (item == null) {
                item = entry;
            }
            String itemPath = path + " 槽位 " + getSlot(entry, index);
            String reason = checkItem(item);
            if (reason != null) {
                entries.remove(index);
                removed++;
                violationConsumer.accept(toViolation(item, itemPath, reason));
            } else {
                removed += sanitizeNestedItem(item, itemPath, depth + 1, violationConsumer);
            }
        }
        return removed;
    }

    private int sanitizeNestedItem(
            CompoundTag item,
            String path,
            int depth,
            Consumer<NbtViolation> violationConsumer
    ) {
        if (!scanNestedContainers || depth > maxNestedDepth) {
            return 0;
        }
        int removed = 0;
        CompoundTag components = item.getCompoundTag("components");
        if (components != null) {
            ListTag<?> container = components.getListTag("minecraft:container");
            if (isCompoundList(container)) {
                removed += sanitizeContainerEntries(
                        container.asCompoundTagList(),
                        path + " -> 容器物品",
                        depth,
                        violationConsumer
                );
            }
            ListTag<?> bundle = components.getListTag("minecraft:bundle_contents");
            if (isCompoundList(bundle)) {
                removed += sanitizeDirectItemList(
                        bundle.asCompoundTagList(),
                        path + " -> 收纳袋",
                        depth,
                        violationConsumer
                );
            }
        }

        CompoundTag legacyTag = item.getCompoundTag("tag");
        if (legacyTag != null) {
            CompoundTag blockEntityTag = legacyTag.getCompoundTag("BlockEntityTag");
            if (blockEntityTag != null) {
                removed += sanitizeNamedList(
                        blockEntityTag,
                        "Items",
                        path + " -> 旧版容器物品",
                        depth,
                        violationConsumer
                );
            }
            removed += sanitizeNamedList(
                    legacyTag,
                    "Items",
                    path + " -> 旧版收纳袋",
                    depth,
                    violationConsumer
            );
        }
        return removed;
    }

    private String checkItem(CompoundTag item) {
        String itemId = normalizeId(item.getString("id"));
        if (itemId.isEmpty()) {
            return null;
        }
        if (bannedMaterials.contains(itemId)) {
            return "违禁物品类型: " + itemId.toUpperCase(Locale.ROOT);
        }

        if (checkIllegalStackSize) {
            int count = getNumber(item, "count", getNumber(item, "Count", 1));
            int maxStack = maxStackSizes.getOrDefault(itemId, 64);
            if (count < 0) {
                return "非法堆叠: 负数数量 " + count;
            }
            if (maxStack > 0 && count > maxStack) {
                return "非法堆叠: " + count + " (最大 " + maxStack + ")";
            }
        }

        CompoundTag components = item.getCompoundTag("components");
        CompoundTag legacyTag = item.getCompoundTag("tag");
        if (checkOverpoweredEnchants) {
            String reason = checkComponentEnchantments(components);
            if (reason == null) {
                reason = checkLegacyEnchantments(legacyTag, "Enchantments");
            }
            if (reason == null) {
                reason = checkLegacyEnchantments(legacyTag, "StoredEnchantments");
            }
            if (reason != null) {
                return reason;
            }
        }

        if (checkIllegalDurability) {
            int damage = getNumber(components, "minecraft:damage", getNumber(legacyTag, "Damage", 0));
            int maxDurability = maxDurabilities.getOrDefault(itemId, 0);
            if (maxDurability > 0 && damage < 0) {
                return "非法耐久度: 负数伤害值 " + damage;
            }
            if (maxDurability > 0 && damage > maxDurability + 10) {
                return "非法耐久度: " + damage + " (最大 " + maxDurability + ")";
            }
        }

        if (checkIllegalAttributes) {
            String reason = checkComponentAttributes(components);
            if (reason == null) {
                reason = checkLegacyAttributes(legacyTag);
            }
            if (reason != null) {
                return reason;
            }
        }

        if (checkUnbreakable) {
            if (components != null && components.containsKey("minecraft:unbreakable")) {
                return "不可破坏物品";
            }
            if (legacyTag != null && legacyTag.getBoolean("Unbreakable")) {
                return "不可破坏物品";
            }
        }
        return null;
    }

    private String checkComponentEnchantments(CompoundTag components) {
        if (components == null) {
            return null;
        }
        String reason = checkEnchantmentComponent(components.getCompoundTag("minecraft:enchantments"));
        if (reason != null) {
            return reason;
        }
        return checkEnchantmentComponent(components.getCompoundTag("minecraft:stored_enchantments"));
    }

    private String checkEnchantmentComponent(CompoundTag enchantmentComponent) {
        if (enchantmentComponent == null) {
            return null;
        }
        CompoundTag levels = enchantmentComponent.getCompoundTag("levels");
        if (levels == null) {
            levels = enchantmentComponent;
        }
        for (Map.Entry<String, Tag<?>> entry : levels.entrySet()) {
            if (entry.getValue() instanceof NumberTag<?> numberTag) {
                String reason = checkEnchantLevel(entry.getKey(), numberTag.asInt());
                if (reason != null) {
                    return reason;
                }
            }
        }
        return null;
    }

    private String checkLegacyEnchantments(CompoundTag legacyTag, String key) {
        if (legacyTag == null || !isCompoundList(legacyTag.getListTag(key))) {
            return null;
        }
        for (CompoundTag enchantment : legacyTag.getListTag(key).asCompoundTagList()) {
            String reason = checkEnchantLevel(
                    enchantment.getString("id"),
                    getNumber(enchantment, "lvl", 0)
            );
            if (reason != null) {
                return reason;
            }
        }
        return null;
    }

    private String checkEnchantLevel(String enchantmentId, int level) {
        String normalized = enchantmentId.toLowerCase(Locale.ROOT);
        String shortName = normalizeId(normalized);
        int maxLevel = enchantLimits.getOrDefault(
                normalized,
                enchantLimits.getOrDefault(shortName, unknownEnchantMaxLevel)
        );
        if (level < 0) {
            return "非法附魔等级: " + enchantmentId + " 等级 " + level;
        }
        if (level > maxLevel) {
            return "超级附魔: " + enchantmentId + " 等级 " + level + " (上限 " + maxLevel + ")";
        }
        return null;
    }

    private String checkComponentAttributes(CompoundTag components) {
        if (components == null) {
            return null;
        }
        Tag<?> raw = components.get("minecraft:attribute_modifiers");
        if (raw instanceof CompoundTag compound) {
            raw = compound.get("modifiers");
        }
        if (!(raw instanceof ListTag<?> list) || !isCompoundList(list)) {
            return null;
        }
        return checkAttributeList(list.asCompoundTagList());
    }

    private String checkLegacyAttributes(CompoundTag legacyTag) {
        if (legacyTag == null || !isCompoundList(legacyTag.getListTag("AttributeModifiers"))) {
            return null;
        }
        return checkAttributeList(legacyTag.getListTag("AttributeModifiers").asCompoundTagList());
    }

    private String checkAttributeList(ListTag<CompoundTag> modifiers) {
        for (CompoundTag modifier : modifiers) {
            String attributeId = modifier.getString("type");
            if (attributeId.isEmpty()) {
                attributeId = modifier.getString("AttributeName");
            }
            NumberTag<?> amountTag = modifier.getNumberTag("amount");
            if (amountTag == null) {
                amountTag = modifier.getNumberTag("Amount");
            }
            if (amountTag == null) {
                continue;
            }
            Double limit = findAttributeLimit(attributeId);
            double amount = amountTag.asDouble();
            if (limit != null && Math.abs(amount) > limit) {
                return "非法属性: " + attributeId + " = " + amount + " (上限 " + limit + ")";
            }
        }
        return null;
    }

    private Double findAttributeLimit(String attributeId) {
        String normalized = normalizeId(attributeId).replace('.', '_');
        for (Map.Entry<String, Double> entry : attributeLimits.entrySet()) {
            if (normalized.equals(entry.getKey()) || normalized.endsWith("_" + entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private NbtViolation toViolation(CompoundTag item, String path, String reason) {
        return new NbtViolation(
                item.getString("id"),
                getNumber(item, "count", getNumber(item, "Count", 1)),
                path,
                reason
        );
    }

    private int getSlot(CompoundTag tag, int fallback) {
        return getNumber(tag, "slot", getNumber(tag, "Slot", fallback));
    }

    private int getNumber(CompoundTag tag, String key, int fallback) {
        if (tag == null) {
            return fallback;
        }
        NumberTag<?> numberTag = tag.getNumberTag(key);
        return numberTag == null ? fallback : numberTag.asInt();
    }

    private boolean isCompoundList(ListTag<?> list) {
        return list != null && list.getTypeClass() == CompoundTag.class;
    }

    private static Map<String, Integer> loadRegisteredEnchantLimits(FileConfiguration config) {
        int multiplier = Math.max(1, config.getInt("enchant-max-multiplier", 1));
        Map<String, Integer> limits = new HashMap<>();
        Registry<Enchantment> registry = Bukkit.getRegistry(Enchantment.class);
        for (Enchantment enchantment : registry) {
            int limit = enchantment.getMaxLevel() * multiplier;
            limits.put(enchantment.getKey().toString().toLowerCase(Locale.ROOT), limit);
            limits.put(enchantment.getKey().getKey().toLowerCase(Locale.ROOT), limit);
        }
        return limits;
    }

    private static String normalizeId(String id) {
        if (id == null) {
            return "";
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.indexOf(':');
        return separator >= 0 ? normalized.substring(separator + 1) : normalized;
    }

    public record NbtViolation(String itemId, int amount, String path, String reason) {
    }
}
