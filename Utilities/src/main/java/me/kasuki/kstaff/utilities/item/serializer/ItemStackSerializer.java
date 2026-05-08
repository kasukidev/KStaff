package me.kasuki.kstaff.utilities.item.serializer;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.utilities.bukkit.item.Itemstack;
import me.kasuki.kstaff.utilities.item.codec.ItemStackCodec;
import me.kasuki.kstaff.utilities.item.nbt.ItemNbtBridge;
import me.kasuki.kstaff.utilities.item.reflect.ReflectionCache;
import me.kasuki.kstaff.utilities.item.reflect.ReflectiveAccess;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the item stack serializer component.
 */
@RequiredArgsConstructor
public final class ItemStackSerializer implements ItemStackCodec {

    private static final String CODEC_ID = "bukkit-itemstack-v1";
    private static final String RAW_COMPONENTS_KEY = "minecraft:raw_components";

    private final Logger logger;

    /**
     * Returns the result of serialize.
     *
     * @param item the item
     * @return the result of serialize
     */
    @Override
    public Itemstack.ItemStack serialize(ItemStack item) {
        if (item == null) {
            return null;
        }

        XMaterial xMaterial = XMaterial.matchXMaterial(item.getType());
        if (xMaterial == null) {
            log(Level.WARNING, "Failed to resolve XMaterial for " + item.getType());
            return null;
        }

        Itemstack.ItemStack.Builder builder =
                Itemstack.ItemStack.newBuilder()
                        .setType(xMaterial.name())
                        .setAmount(item.getAmount())
                        .setDurability(item.getDurability())
                        .setSerializerVersion(CODEC_ID)
                        .setMinecraftVersion(safeServerVersion());

        if (item.getType() != Material.AIR && item.getAmount() > 0) {
            String rawNbt = ItemNbtBridge.serializeWholeItem(item);
            if (!rawNbt.isEmpty()) {
                builder.setNbtData(rawNbt);
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            serializeCommonMeta(meta, builder);
            serializeSpecialMeta(item, meta, builder);
        }

        serializeRawFallbacks(item, builder);
        return builder.build();
    }

    /**
     * Returns the result of deserialize.
     *
     * @param proto the proto
     * @return the result of deserialize
     */
    @Override
    public ItemStack deserialize(Itemstack.ItemStack proto) {
        if (proto == null) {
            return null;
        }

        Optional<XMaterial> optional = XMaterial.matchXMaterial(proto.getType());
        if (!optional.isPresent()) {
            log(Level.WARNING, "Unknown proto material " + proto.getType());
            return null;
        }

        ItemStack item = optional.get().parseItem();
        if (item == null) {
            log(Level.WARNING, "Failed to parse item for proto material " + proto.getType());
            return null;
        }

        item.setAmount(Math.max(1, proto.getAmount()));
        item.setDurability((short) proto.getDurability());

        if (!proto.getNbtData().isEmpty()) {
            ItemNbtBridge.applyWholeItem(item, proto.getNbtData());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (proto.hasCommon()) {
                deserializeCommonMeta(meta, proto.getCommon());
            }

            deserializeSpecialMeta(item, meta, proto);

            try {
                item.setItemMeta(meta);
            } catch (Exception ex) {
                log(Level.WARNING, "Failed to apply ItemMeta to " + proto.getType(), ex);
            }
        }

        deserializeRawFallbacks(item, proto);
        return item;
    }

    /**
     * Returns the codec id.
     *
     * @return the codec id
     */
    @Override
    public String getCodecId() {
        return CODEC_ID;
    }

    /**
     * Performs the serialize common meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeCommonMeta(ItemMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.CommonMeta.Builder common = Itemstack.CommonMeta.newBuilder();

        if (meta.hasDisplayName()) {
            common.setDisplayName(meta.getDisplayName());
        }

        if (meta.hasLore() && meta.getLore() != null) {
            common.addAllLore(meta.getLore());
        }

        String localizedName = ReflectiveAccess.invokeString(meta, "getLocalizedName");
        if (!localizedName.isEmpty()) {
            common.setLocalizedName(localizedName);
        }

        String itemName = ReflectiveAccess.invokeString(meta, "getItemName");
        if (!itemName.isEmpty()) {
            common.setItemName(itemName);
        }

        Boolean unbreakable = readUnbreakable(meta);
        if (unbreakable != null) {
            common.setUnbreakable(unbreakable.booleanValue());
        }

        Integer customModelData = ReflectiveAccess.invokeInteger(meta, "getCustomModelData");
        if (customModelData != null) {
            common.setCustomModelData(customModelData.intValue());
        }

        if (meta instanceof Repairable) {
            Repairable repairable = (Repairable) meta;
            if (repairable.hasRepairCost()) {
                common.setRepairCost(repairable.getRepairCost());
            }
        }

        Integer damage = ReflectiveAccess.invokeInteger(meta, "getDamage");
        if (damage != null) {
            common.setDamage(damage.intValue());
        }

        Boolean glintOverride = ReflectiveAccess.invokeBoolean(meta, "getEnchantmentGlintOverride");
        if (glintOverride != null) {
            common.setHasGlintOverride(true);
            common.setGlintOverride(glintOverride.booleanValue());
        }

        Boolean hideTooltip = ReflectiveAccess.invokeBoolean(meta, "isHideTooltip");
        if (hideTooltip != null) {
            common.setHideTooltip(hideTooltip.booleanValue());
        }

        Boolean fireResistant = ReflectiveAccess.invokeBoolean(meta, "isFireResistant");
        if (fireResistant != null) {
            common.setFireResistant(fireResistant.booleanValue());
        }

        Object rarity = ReflectiveAccess.invoke(meta, "getRarity");
        if (rarity instanceof Enum) {
            common.setRarity(((Enum<?>) rarity).name());
        }

        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String enchantKey = resolveEnchantmentKey(entry.getKey());
            if (enchantKey.isEmpty()) {
                continue;
            }

            common.addEnchants(
                    Itemstack.Enchantment.newBuilder()
                            .setKey(enchantKey)
                            .setLevel(entry.getValue() == null ? 1 : entry.getValue().intValue())
                            .build());
        }

        for (ItemFlag flag : meta.getItemFlags()) {
            if (flag != null) {
                common.addItemFlags(flag.name());
            }
        }

        builder.setCommon(common.build());
    }

    /**
     * Performs the deserialize common meta operation.
     *
     * @param meta   the meta
     * @param common the common
     */
    private void deserializeCommonMeta(ItemMeta meta, Itemstack.CommonMeta common) {
        if (!common.getDisplayName().isEmpty()) {
            meta.setDisplayName(common.getDisplayName());
        }

        if (common.getLoreCount() > 0) {
            meta.setLore(new ArrayList<String>(common.getLoreList()));
        }

        if (!common.getLocalizedName().isEmpty()) {
            ReflectiveAccess.invokeVoid(
                    meta, "setLocalizedName", new Class<?>[]{String.class}, common.getLocalizedName());
        }

        if (!common.getItemName().isEmpty()) {
            ReflectiveAccess.invokeVoid(
                    meta, "setItemName", new Class<?>[]{String.class}, common.getItemName());
        }

        writeUnbreakable(meta, common.getUnbreakable());

        if (ReflectionCache.findMethod(meta.getClass(), "setCustomModelData", Integer.class) != null) {
            ReflectiveAccess.invokeVoid(
                    meta,
                    "setCustomModelData",
                    new Class<?>[]{Integer.class},
                    Integer.valueOf(common.getCustomModelData()));
        }

        if (meta instanceof Repairable) {
            ((Repairable) meta).setRepairCost(common.getRepairCost());
        }

        if (ReflectionCache.findMethod(meta.getClass(), "setDamage", Integer.TYPE) != null) {
            ReflectiveAccess.invokeVoid(
                    meta, "setDamage", new Class<?>[]{Integer.TYPE}, Integer.valueOf(common.getDamage()));
        }

        if (common.getHasGlintOverride()) {
            ReflectiveAccess.invokeVoid(
                    meta,
                    "setEnchantmentGlintOverride",
                    new Class<?>[]{Boolean.TYPE},
                    Boolean.valueOf(common.getGlintOverride()));
        }

        if (common.getHideTooltip()) {
            ReflectiveAccess.invokeVoid(
                    meta, "setHideTooltip", new Class<?>[]{Boolean.TYPE}, Boolean.TRUE);
        }

        if (common.getFireResistant()) {
            ReflectiveAccess.invokeVoid(
                    meta, "setFireResistant", new Class<?>[]{Boolean.TYPE}, Boolean.TRUE);
        }

        for (Itemstack.Enchantment enchantProto : common.getEnchantsList()) {
            Enchantment enchantment = resolveEnchantment(enchantProto.getKey());
            if (enchantment != null) {
                meta.addEnchant(enchantment, enchantProto.getLevel(), true);
            }
        }

        for (String flagName : common.getItemFlagsList()) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flagName));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Performs the serialize special meta operation.
     *
     * @param item    the item
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeSpecialMeta(
            ItemStack item, ItemMeta meta, Itemstack.ItemStack.Builder builder) {

        if (meta instanceof BookMeta) {
            serializeBookMeta((BookMeta) meta, builder);
            return;
        }

        if (meta instanceof EnchantmentStorageMeta) {
            serializeEnchantedBookMeta((EnchantmentStorageMeta) meta, builder);
            return;
        }

        if (meta instanceof LeatherArmorMeta) {
            serializeLeatherArmorMeta((LeatherArmorMeta) meta, builder);
            return;
        }

        if (meta instanceof SkullMeta) {
            serializeSkullMeta(item, (SkullMeta) meta, builder);
            return;
        }

        if (isPotionMeta(meta)) {
            serializePotionMeta(meta, builder);
            return;
        }

        if (meta instanceof MapMeta) {
            serializeMapMeta((MapMeta) meta, builder);
            return;
        }

        if (meta instanceof FireworkMeta) {
            serializeFireworkMeta((FireworkMeta) meta, builder);
            return;
        }

        if (meta instanceof BannerMeta) {
            serializeBannerMeta((BannerMeta) meta, builder);
        }
    }

    /**
     * Performs the deserialize special meta operation.
     *
     * @param item  the item
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeSpecialMeta(ItemStack item, ItemMeta meta, Itemstack.ItemStack proto) {
        switch (proto.getSpecialMetaCase()) {
            case BOOK:
                deserializeBookMeta(meta, proto.getBook());
                return;
            case ENCHANTED_BOOK:
                deserializeEnchantedBookMeta(meta, proto.getEnchantedBook());
                return;
            case LEATHER_ARMOR:
                deserializeLeatherArmorMeta(meta, proto.getLeatherArmor());
                return;
            case SKULL:
                deserializeSkullMeta(meta, proto.getSkull());
                return;
            case POTION:
                deserializePotionMeta(meta, proto.getPotion());
                return;
            case MAP:
                deserializeMapMeta(meta, proto.getMap());
                return;
            case FIREWORK:
                deserializeFireworkMeta(meta, proto.getFirework());
                return;
            case BANNER:
                deserializeBannerMeta(meta, proto.getBanner());
                return;
            case SPECIALMETA_NOT_SET:
            default:
                return;
        }
    }

    /**
     * Performs the serialize book meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeBookMeta(BookMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.BookMeta.Builder book = Itemstack.BookMeta.newBuilder();

        if (meta.hasTitle()) {
            book.setTitle(meta.getTitle());
        }

        if (meta.hasAuthor()) {
            book.setAuthor(meta.getAuthor());
        }

        if (meta.hasPages()) {
            book.addAllPages(meta.getPages());
        }

        Object generation = ReflectiveAccess.invoke(meta, "getGeneration");
        if (generation instanceof Enum) {
            book.setGeneration(((Enum<?>) generation).name());
        }

        Boolean resolved = ReflectiveAccess.invokeBoolean(meta, "isResolved");
        if (resolved != null) {
            book.setResolved(resolved.booleanValue());
        }

        builder.setBook(book.build());
    }

    /**
     * Performs the deserialize book meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    @SuppressWarnings("unchecked")
    private void deserializeBookMeta(ItemMeta meta, Itemstack.BookMeta proto) {
        if (!(meta instanceof BookMeta)) {
            return;
        }

        BookMeta bookMeta = (BookMeta) meta;

        if (!proto.getTitle().isEmpty()) {
            bookMeta.setTitle(proto.getTitle());
        }

        if (!proto.getAuthor().isEmpty()) {
            bookMeta.setAuthor(proto.getAuthor());
        }

        if (proto.getPagesCount() > 0) {
            bookMeta.setPages(new ArrayList<String>(proto.getPagesList()));
        }

        if (!proto.getGeneration().isEmpty()) {
            Class<?> generationClass =
                    ReflectionCache.findClass("org.bukkit.inventory.meta.BookMeta$Generation");
            if (generationClass != null && generationClass.isEnum()) {
                Enum<?> generation = ReflectiveAccess.enumValue(generationClass, proto.getGeneration());
                if (generation != null) {
                    ReflectiveAccess.invokeVoid(
                            bookMeta, "setGeneration", new Class<?>[]{generationClass}, generation);
                }
            }
        }

        if (proto.getResolved()) {
            ReflectiveAccess.invokeVoid(
                    bookMeta, "setResolved", new Class<?>[]{Boolean.TYPE}, Boolean.TRUE);
        }
    }

    /**
     * Performs the serialize enchanted book meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeEnchantedBookMeta(
            EnchantmentStorageMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.EnchantedBookMeta.Builder book = Itemstack.EnchantedBookMeta.newBuilder();

        for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String key = resolveEnchantmentKey(entry.getKey());
            if (key.isEmpty()) {
                continue;
            }

            book.addStoredEnchants(
                    Itemstack.Enchantment.newBuilder()
                            .setKey(key)
                            .setLevel(entry.getValue() == null ? 1 : entry.getValue().intValue())
                            .build());
        }

        builder.setEnchantedBook(book.build());
    }

    /**
     * Performs the deserialize enchanted book meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeEnchantedBookMeta(ItemMeta meta, Itemstack.EnchantedBookMeta proto) {
        if (!(meta instanceof EnchantmentStorageMeta)) {
            return;
        }

        EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
        for (Itemstack.Enchantment enchantProto : proto.getStoredEnchantsList()) {
            Enchantment enchantment = resolveEnchantment(enchantProto.getKey());
            if (enchantment != null) {
                storageMeta.addStoredEnchant(enchantment, enchantProto.getLevel(), true);
            }
        }
    }

    /**
     * Performs the serialize leather armor meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeLeatherArmorMeta(
            LeatherArmorMeta meta, Itemstack.ItemStack.Builder builder) {
        Color color = meta.getColor();
        builder.setLeatherArmor(
                Itemstack.LeatherArmorMeta.newBuilder()
                        .setRed(color.getRed())
                        .setGreen(color.getGreen())
                        .setBlue(color.getBlue())
                        .build());
    }

    /**
     * Performs the deserialize leather armor meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeLeatherArmorMeta(ItemMeta meta, Itemstack.LeatherArmorMeta proto) {
        if (!(meta instanceof LeatherArmorMeta)) {
            return;
        }

        ((LeatherArmorMeta) meta)
                .setColor(
                        Color.fromRGB(
                                clampColor(proto.getRed()),
                                clampColor(proto.getGreen()),
                                clampColor(proto.getBlue())));
    }

    /**
     * Performs the serialize skull meta operation.
     *
     * @param item    the item
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeSkullMeta(
            ItemStack item, SkullMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.SkullMeta.Builder skull = Itemstack.SkullMeta.newBuilder();

        if (meta.hasOwner()) {
            skull.setOwner(meta.getOwner());
        }

        String uuid = ItemNbtBridge.readString(item, "SkullOwner", "Id");
        if (!uuid.isEmpty()) {
            skull.setOwningPlayerUuid(uuid);
        }

        String playerName = ItemNbtBridge.readString(item, "SkullOwner", "Name");
        if (!playerName.isEmpty()) {
            skull.setPlayerName(playerName);
        }

        String rawNbt = ItemNbtBridge.serializeCompound(item, "SkullOwner");
        if (!rawNbt.isEmpty()) {
            if (rawNbt.contains("Value")) {
                skull.setTexture(rawNbt);
            }
        }

        builder.setSkull(skull.build());
    }

    /**
     * Performs the deserialize skull meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeSkullMeta(ItemMeta meta, Itemstack.SkullMeta proto) {
        if (!(meta instanceof SkullMeta)) {
            return;
        }

        SkullMeta skullMeta = (SkullMeta) meta;
        if (!proto.getOwner().isEmpty()) {
            skullMeta.setOwner(proto.getOwner());
            return;
        }

        if (!proto.getPlayerName().isEmpty()) {
            skullMeta.setOwner(proto.getPlayerName());
        }
    }

    /**
     * Performs the serialize potion meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializePotionMeta(ItemMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.PotionMeta.Builder potion = Itemstack.PotionMeta.newBuilder();

        Object potionData = ReflectiveAccess.invoke(meta, "getBasePotionData");
        if (potionData != null) {
            Object potionType = ReflectiveAccess.invoke(potionData, "getType");
            Boolean upgraded = ReflectiveAccess.invokeBoolean(potionData, "isUpgraded");
            Boolean extended = ReflectiveAccess.invokeBoolean(potionData, "isExtended");

            if (potionType instanceof Enum) {
                potion.setBasePotionType(((Enum<?>) potionType).name());
            }

            if (upgraded != null) {
                potion.setUpgraded(upgraded.booleanValue());
            }

            if (extended != null) {
                potion.setExtended(extended.booleanValue());
            }
        }

        Color color = (Color) ReflectiveAccess.invoke(meta, "getColor");
        if (color != null) {
            potion.setColorRgb(color.asRGB());
        }

        @SuppressWarnings("unchecked")
        List<PotionEffect> effects =
                (List<PotionEffect>) ReflectiveAccess.invoke(meta, "getCustomEffects");

        if (effects != null) {
            for (PotionEffect effect : effects) {
                if (effect != null && effect.getType() != null) {
                    potion.addCustomEffects(serializePotionEffect(effect));
                }
            }
        }

        builder.setPotion(potion.build());
    }

    /**
     * Performs the deserialize potion meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    @SuppressWarnings("unchecked")
    private void deserializePotionMeta(ItemMeta meta, Itemstack.PotionMeta proto) {
        if (!isPotionMeta(meta)) {
            return;
        }

        if (!proto.getBasePotionType().isEmpty()) {
            Class<?> potionDataClass = ReflectionCache.findClass("org.bukkit.potion.PotionData");
            Class<?> potionTypeClass = ReflectionCache.findClass("org.bukkit.potion.PotionType");

            if (potionDataClass != null && potionTypeClass != null && potionTypeClass.isEnum()) {
                Enum<?> potionType = ReflectiveAccess.enumValue(potionTypeClass, proto.getBasePotionType());
                if (potionType != null) {
                    Object potionData =
                            ReflectiveAccess.construct(
                                    potionDataClass,
                                    new Class<?>[]{potionTypeClass, Boolean.TYPE, Boolean.TYPE},
                                    potionType,
                                    Boolean.valueOf(proto.getExtended()),
                                    Boolean.valueOf(proto.getUpgraded()));

                    if (potionData != null) {
                        ReflectiveAccess.invokeVoid(
                                meta, "setBasePotionData", new Class<?>[]{potionDataClass}, potionData);
                    }
                }
            }
        }

        if (ReflectionCache.findMethod(meta.getClass(), "setColor", Color.class) != null
                && proto.getColorRgb() != 0) {
            ReflectiveAccess.invokeVoid(
                    meta, "setColor", new Class<?>[]{Color.class}, Color.fromRGB(proto.getColorRgb()));
        }

        List<PotionEffect> currentEffects =
                (List<PotionEffect>) ReflectiveAccess.invoke(meta, "getCustomEffects");

        if (currentEffects != null) {
            for (PotionEffect existing : new ArrayList<PotionEffect>(currentEffects)) {
                ReflectiveAccess.invokeVoid(
                        meta,
                        "removeCustomEffect",
                        new Class<?>[]{PotionEffectType.class},
                        existing.getType());
            }
        }

        for (Itemstack.PotionEffect effectProto : proto.getCustomEffectsList()) {
            PotionEffect effect = deserializePotionEffect(effectProto);
            if (effect != null) {
                ReflectiveAccess.invokeVoid(
                        meta,
                        "addCustomEffect",
                        new Class<?>[]{PotionEffect.class, Boolean.TYPE},
                        effect,
                        Boolean.TRUE);
            }
        }
    }

    /**
     * Performs the serialize map meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeMapMeta(MapMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.MapMeta.Builder map = Itemstack.MapMeta.newBuilder().setScaling(meta.isScaling());

        Integer mapId = ReflectiveAccess.invokeInteger(meta, "getMapId");
        if (mapId != null) {
            map.setMapId(mapId.intValue());
        }

        String locationName = ReflectiveAccess.invokeString(meta, "getLocationName");
        if (!locationName.isEmpty()) {
            map.setLocationName(locationName);
        }

        Color color = (Color) ReflectiveAccess.invoke(meta, "getColor");
        if (color != null) {
            map.setColorRgb(color.asRGB());
        }

        builder.setMap(map.build());
    }

    /**
     * Performs the deserialize map meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeMapMeta(ItemMeta meta, Itemstack.MapMeta proto) {
        if (!(meta instanceof MapMeta)) {
            return;
        }

        MapMeta mapMeta = (MapMeta) meta;
        mapMeta.setScaling(proto.getScaling());

        if (proto.getMapId() != 0) {
            ReflectiveAccess.invokeVoid(
                    mapMeta, "setMapId", new Class<?>[]{Integer.TYPE}, Integer.valueOf(proto.getMapId()));
        }

        if (!proto.getLocationName().isEmpty()) {
            ReflectiveAccess.invokeVoid(
                    mapMeta, "setLocationName", new Class<?>[]{String.class}, proto.getLocationName());
        }

        if (proto.getColorRgb() != 0) {
            ReflectiveAccess.invokeVoid(
                    mapMeta, "setColor", new Class<?>[]{Color.class}, Color.fromRGB(proto.getColorRgb()));
        }
    }

    /**
     * Performs the serialize firework meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeFireworkMeta(FireworkMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.FireworkMeta.Builder firework =
                Itemstack.FireworkMeta.newBuilder().setPower(meta.getPower());

        for (FireworkEffect effect : meta.getEffects()) {
            if (effect != null) {
                firework.addEffects(serializeFireworkEffect(effect));
            }
        }

        builder.setFirework(firework.build());
    }

    /**
     * Performs the deserialize firework meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeFireworkMeta(ItemMeta meta, Itemstack.FireworkMeta proto) {
        if (!(meta instanceof FireworkMeta)) {
            return;
        }

        FireworkMeta fireworkMeta = (FireworkMeta) meta;
        fireworkMeta.setPower(proto.getPower());

        for (Itemstack.FireworkEffect effectProto : proto.getEffectsList()) {
            fireworkMeta.addEffect(deserializeFireworkEffect(effectProto));
        }
    }

    /**
     * Performs the serialize banner meta operation.
     *
     * @param meta    the meta
     * @param builder the builder
     */
    private void serializeBannerMeta(BannerMeta meta, Itemstack.ItemStack.Builder builder) {
        Itemstack.BannerMeta.Builder banner = Itemstack.BannerMeta.newBuilder();

        if (meta.getBaseColor() != null) {
            banner.setBaseColor(meta.getBaseColor().name());
        }

        for (Pattern pattern : meta.getPatterns()) {
            if (pattern == null) {
                continue;
            }

            banner.addPatterns(
                    Itemstack.BannerPattern.newBuilder()
                            .setColor(pattern.getColor().name())
                            .setPatternType(pattern.getPattern().name())
                            .build());
        }

        builder.setBanner(banner.build());
    }

    /**
     * Performs the deserialize banner meta operation.
     *
     * @param meta  the meta
     * @param proto the proto
     */
    private void deserializeBannerMeta(ItemMeta meta, Itemstack.BannerMeta proto) {
        if (!(meta instanceof BannerMeta)) {
            return;
        }

        BannerMeta bannerMeta = (BannerMeta) meta;

        if (!proto.getBaseColor().isEmpty()) {
            try {
                bannerMeta.setBaseColor(DyeColor.valueOf(proto.getBaseColor()));
            } catch (Exception ignored) {
            }
        }

        List<Pattern> patterns = new ArrayList<Pattern>();
        for (Itemstack.BannerPattern patternProto : proto.getPatternsList()) {
            try {
                DyeColor color = DyeColor.valueOf(patternProto.getColor());
                org.bukkit.block.banner.PatternType patternType =
                        org.bukkit.block.banner.PatternType.valueOf(patternProto.getPatternType());
                patterns.add(new Pattern(color, patternType));
            } catch (Exception ignored) {
            }
        }

        bannerMeta.setPatterns(patterns);
    }

    /**
     * Performs the serialize raw fallbacks operation.
     *
     * @param item    the item
     * @param builder the builder
     */
    private void serializeRawFallbacks(ItemStack item, Itemstack.ItemStack.Builder builder) {
        String blockEntityTag = ItemNbtBridge.serializeBlockEntityTag(item);
        if (!blockEntityTag.isEmpty() && !builder.hasBlockState()) {
            builder.setBlockState(
                    Itemstack.BlockStateMeta.newBuilder().setBlockEntityNbt(blockEntityTag).build());
        }

        String blockStateTag = ItemNbtBridge.serializeBlockStateTag(item);
        if (!blockStateTag.isEmpty() && !builder.hasBlockData()) {
            builder.setBlockData(
                    Itemstack.BlockDataMeta.newBuilder().setBlockData(blockStateTag).build());
        }

        String rawComponents = ItemNbtBridge.serializeRawComponents(item);
        if (!rawComponents.isEmpty()) {
            builder.addComponents(
                    Itemstack.DataComponent.newBuilder()
                            .setKey(RAW_COMPONENTS_KEY)
                            .setJsonValue(rawComponents)
                            .build());
        }
    }

    /**
     * Performs the deserialize raw fallbacks operation.
     *
     * @param item  the item
     * @param proto the proto
     */
    private void deserializeRawFallbacks(ItemStack item, Itemstack.ItemStack proto) {
        if (proto.hasBlockState() && !proto.getBlockState().getBlockEntityNbt().isEmpty()) {
            ItemNbtBridge.applyBlockEntityTag(item, proto.getBlockState().getBlockEntityNbt());
        }

        if (proto.hasBlockData() && !proto.getBlockData().getBlockData().isEmpty()) {
            ItemNbtBridge.applyBlockStateTag(item, proto.getBlockData().getBlockData());
        }

        for (Itemstack.DataComponent component : proto.getComponentsList()) {
            if (RAW_COMPONENTS_KEY.equals(component.getKey()) && !component.getJsonValue().isEmpty()) {
                ItemNbtBridge.applyRawComponents(item, component.getJsonValue());
            }
        }
    }

    /**
     * Returns the result of serialize potion effect.
     *
     * @param effect the effect
     * @return the result of serialize potion effect
     */
    private Itemstack.PotionEffect serializePotionEffect(PotionEffect effect) {
        Itemstack.PotionEffect.Builder builder =
                Itemstack.PotionEffect.newBuilder()
                        .setType(effect.getType().getName())
                        .setDuration(effect.getDuration())
                        .setAmplifier(effect.getAmplifier())
                        .setAmbient(effect.isAmbient());

        Boolean particles = ReflectiveAccess.invokeBoolean(effect, "hasParticles");
        if (particles != null) {
            builder.setParticles(particles.booleanValue());
        }

        Boolean icon = ReflectiveAccess.invokeBoolean(effect, "hasIcon");
        if (icon != null) {
            builder.setIcon(icon.booleanValue());
        }

        return builder.build();
    }

    /**
     * Returns the result of deserialize potion effect.
     *
     * @param proto the proto
     * @return the result of deserialize potion effect
     */
    private PotionEffect deserializePotionEffect(Itemstack.PotionEffect proto) {
        PotionEffectType type = resolvePotionEffectType(proto.getType());
        if (type == null) {
            return null;
        }

        try {
            Class<?>[] signature =
                    new Class<?>[]{
                            PotionEffectType.class,
                            Integer.TYPE,
                            Integer.TYPE,
                            Boolean.TYPE,
                            Boolean.TYPE,
                            Boolean.TYPE
                    };

            Object effect =
                    ReflectiveAccess.construct(
                            PotionEffect.class,
                            signature,
                            type,
                            Integer.valueOf(proto.getDuration()),
                            Integer.valueOf(proto.getAmplifier()),
                            Boolean.valueOf(proto.getAmbient()),
                            Boolean.valueOf(proto.getParticles()),
                            Boolean.valueOf(proto.getIcon()));

            if (effect instanceof PotionEffect) {
                return (PotionEffect) effect;
            }
        } catch (Throwable ignored) {
        }

        try {
            return new PotionEffect(type, proto.getDuration(), proto.getAmplifier(), proto.getAmbient());
        } catch (Throwable ex) {
            log(Level.WARNING, "Failed to create PotionEffect for " + proto.getType(), ex);
            return null;
        }
    }

    /**
     * Returns the result of serialize firework effect.
     *
     * @param effect the effect
     * @return the result of serialize firework effect
     */
    private Itemstack.FireworkEffect serializeFireworkEffect(FireworkEffect effect) {
        Itemstack.FireworkEffect.Builder builder =
                Itemstack.FireworkEffect.newBuilder()
                        .setType(effect.getType().name())
                        .setFlicker(effect.hasFlicker())
                        .setTrail(effect.hasTrail());

        for (Color color : effect.getColors()) {
            builder.addColors(color.asRGB());
        }

        for (Color color : effect.getFadeColors()) {
            builder.addFadeColors(color.asRGB());
        }

        return builder.build();
    }

    /**
     * Returns the result of deserialize firework effect.
     *
     * @param proto the proto
     * @return the result of deserialize firework effect
     */
    private FireworkEffect deserializeFireworkEffect(Itemstack.FireworkEffect proto) {
        FireworkEffect.Builder builder =
                FireworkEffect.builder()
                        .with(resolveFireworkType(proto.getType()))
                        .flicker(proto.getFlicker())
                        .trail(proto.getTrail());

        List<Color> colors = new ArrayList<Color>();
        for (int rgb : proto.getColorsList()) {
            colors.add(Color.fromRGB(rgb));
        }
        if (!colors.isEmpty()) {
            builder.withColor(colors);
        }

        List<Color> fadeColors = new ArrayList<Color>();
        for (int rgb : proto.getFadeColorsList()) {
            fadeColors.add(Color.fromRGB(rgb));
        }
        if (!fadeColors.isEmpty()) {
            builder.withFade(fadeColors);
        }

        return builder.build();
    }

    /**
     * Returns the result of resolve firework type.
     *
     * @param name the name
     * @return the result of resolve firework type
     */
    private FireworkEffect.Type resolveFireworkType(String name) {
        try {
            return FireworkEffect.Type.valueOf(name);
        } catch (Exception ignored) {
            return FireworkEffect.Type.BALL;
        }
    }

    /**
     * Returns the result of resolve enchantment key.
     *
     * @param enchantment the enchantment
     * @return the result of resolve enchantment key
     */
    private String resolveEnchantmentKey(Enchantment enchantment) {
        Object key = ReflectiveAccess.invoke(enchantment, "getKey");
        if (key != null) {
            String namespace = ReflectiveAccess.invokeString(key, "getNamespace");
            String value = ReflectiveAccess.invokeString(key, "getKey");
            if (!namespace.isEmpty() && !value.isEmpty()) {
                return namespace + ":" + value;
            }
        }

        try {
            return enchantment.getName();
        } catch (Throwable ignored) {
            return "";
        }
    }

    /**
     * Returns the result of resolve enchantment.
     *
     * @param key the key
     * @return the result of resolve enchantment
     */
    private Enchantment resolveEnchantment(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        Class<?> namespacedKeyClass = ReflectionCache.findClass("org.bukkit.NamespacedKey");
        if (namespacedKeyClass != null) {
            Object namespacedKey = parseNamespacedKey(key);
            Object result =
                    ReflectiveAccess.invokeStatic(
                            Enchantment.class, "getByKey", new Class<?>[]{namespacedKeyClass}, namespacedKey);
            if (result instanceof Enchantment) {
                return (Enchantment) result;
            }
        }

        try {
            return Enchantment.getByName(key.toUpperCase());
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the result of resolve potion effect type.
     *
     * @param key the key
     * @return the result of resolve potion effect type
     */
    private PotionEffectType resolvePotionEffectType(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        Class<?> namespacedKeyClass = ReflectionCache.findClass("org.bukkit.NamespacedKey");
        if (namespacedKeyClass != null) {
            Object namespacedKey = parseNamespacedKey(key.toLowerCase());
            Object result =
                    ReflectiveAccess.invokeStatic(
                            PotionEffectType.class,
                            "getByKey",
                            new Class<?>[]{namespacedKeyClass},
                            namespacedKey);
            if (result instanceof PotionEffectType) {
                return (PotionEffectType) result;
            }
        }

        try {
            PotionEffectType type = PotionEffectType.getByName(key.toUpperCase());
            if (type != null) {
                return type;
            }
        } catch (Throwable ignored) {
        }

        try {
            return PotionEffectType.getByName(key);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the result of read unbreakable.
     *
     * @param meta the meta
     * @return whether read unbreakable
     */
    private Boolean readUnbreakable(ItemMeta meta) {
        Boolean direct = ReflectiveAccess.invokeBoolean(meta, "isUnbreakable");
        if (direct != null) {
            return direct;
        }

        Object spigot = ReflectiveAccess.invoke(meta, "spigot");
        if (spigot != null) {
            return ReflectiveAccess.invokeBoolean(spigot, "isUnbreakable");
        }

        return null;
    }

    /**
     * Performs the write unbreakable operation.
     *
     * @param meta  the meta
     * @param value the value
     */
    private void writeUnbreakable(ItemMeta meta, boolean value) {
        if (ReflectionCache.findMethod(meta.getClass(), "setUnbreakable", Boolean.TYPE) != null) {
            ReflectiveAccess.invokeVoid(
                    meta, "setUnbreakable", new Class<?>[]{Boolean.TYPE}, Boolean.valueOf(value));
            return;
        }

        Object spigot = ReflectiveAccess.invoke(meta, "spigot");
        if (spigot != null) {
            ReflectiveAccess.invokeVoid(
                    spigot, "setUnbreakable", new Class<?>[]{Boolean.TYPE}, Boolean.valueOf(value));
        }
    }

    /**
     * Returns whether potion meta.
     *
     * @param meta the meta
     * @return whether potion meta
     */
    private boolean isPotionMeta(ItemMeta meta) {
        Class<?> potionMetaClass = ReflectionCache.findClass("org.bukkit.inventory.meta.PotionMeta");
        return potionMetaClass != null && potionMetaClass.isInstance(meta);
    }

    /**
     * Returns the result of parse namespaced key.
     *
     * @param input the input
     * @return the result of parse namespaced key
     */
    private Object parseNamespacedKey(String input) {
        Class<?> namespacedKeyClass = ReflectionCache.findClass("org.bukkit.NamespacedKey");
        if (namespacedKeyClass == null || input == null || input.isEmpty()) {
            return null;
        }

        String[] split = input.split(":", 2);
        String namespace = split.length == 2 ? split[0] : "minecraft";
        String key = split.length == 2 ? split[1] : split[0];

        Object direct =
                ReflectiveAccess.construct(
                        namespacedKeyClass, new Class<?>[]{String.class, String.class}, namespace, key);
        if (direct != null) {
            return direct;
        }

        return ReflectiveAccess.invokeStatic(
                namespacedKeyClass, "fromString", new Class<?>[]{String.class}, input);
    }

    /**
     * Returns the result of clamp color.
     *
     * @param value the value
     * @return the result of clamp color
     */
    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Returns the result of safe server version.
     *
     * @return the result of safe server version
     */
    private String safeServerVersion() {
        try {
            return Bukkit.getVersion();
        } catch (Throwable ignored) {
            return "";
        }
    }

    /**
     * Performs the log operation.
     *
     * @param level   the level
     * @param message the message
     */
    private void log(Level level, String message) {
        if (logger != null) {
            logger.log(level, message);
        }
    }

    /**
     * Performs the log operation.
     *
     * @param level     the level
     * @param message   the message
     * @param throwable the throwable
     */
    private void log(Level level, String message, Throwable throwable) {
        if (logger != null) {
            logger.log(level, message, throwable);
        }
    }
}
