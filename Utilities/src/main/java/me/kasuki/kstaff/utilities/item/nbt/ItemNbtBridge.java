package me.kasuki.kstaff.utilities.item.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.inventory.ItemStack;

public final class ItemNbtBridge {

    private ItemNbtBridge() {
    }

    public static String serializeWholeItem(ItemStack item) {
        if (item == null) {
            return "";
        }

        try {
            return new NBTItem(item).toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static void applyWholeItem(ItemStack item, String nbtData) {
        if (item == null || nbtData == null || nbtData.isEmpty()) {
            return;
        }

        try {
            NBT.modify(
                    item,
                    nbt -> {
                        ReadWriteNBT parsed = NBT.parseNBT(nbtData);
                        nbt.mergeCompound(parsed);
                    });
        } catch (Throwable ignored) {
        }
    }

    public static String serializeCompound(ItemStack item, String key) {
        if (item == null || key == null || key.isEmpty()) {
            return "";
        }

        try {
            NBTItem nbtItem = new NBTItem(item);
            if (!nbtItem.hasTag(key)) {
                return "";
            }

            ReadWriteNBT compound = nbtItem.getCompound(key);
            return compound == null ? "" : compound.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static void applyCompound(ItemStack item, String key, String snbt) {
        if (item == null || key == null || key.isEmpty() || snbt == null || snbt.isEmpty()) {
            return;
        }

        try {
            NBT.modify(
                    item,
                    nbt -> {
                        nbt.getOrCreateCompound(key).mergeCompound(NBT.parseNBT(snbt));
                    });
        } catch (Throwable ignored) {
        }
    }

    public static String serializeRawComponents(ItemStack item) {
        return serializeCompound(item, "components");
    }

    public static void applyRawComponents(ItemStack item, String snbt) {
        applyCompound(item, "components", snbt);
    }

    public static String serializeBlockEntityTag(ItemStack item) {
        return serializeCompound(item, "BlockEntityTag");
    }

    public static void applyBlockEntityTag(ItemStack item, String snbt) {
        applyCompound(item, "BlockEntityTag", snbt);
    }

    public static String serializeBlockStateTag(ItemStack item) {
        return serializeCompound(item, "BlockStateTag");
    }

    public static void applyBlockStateTag(ItemStack item, String snbt) {
        applyCompound(item, "BlockStateTag", snbt);
    }

    public static String readString(ItemStack item, String... path) {
        if (item == null || path == null || path.length == 0) {
            return "";
        }

        try {
            NBTItem nbtItem = new NBTItem(item);
            ReadWriteNBT current = nbtItem;
            for (int i = 0; i < path.length - 1; i++) {
                current = current.getCompound(path[i]);
                if (current == null) {
                    return "";
                }
            }

            String value = current.getString(path[path.length - 1]);
            return value == null ? "" : value;
        } catch (Throwable ignored) {
            return "";
        }
    }
}
