package me.kasuki.kstaff.utilities.item.serializer;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.utilities.item.Itemstack;
import me.kasuki.kstaff.utilities.item.codec.ItemStackCodec;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents the item stack array serializer component.
 */
@RequiredArgsConstructor
public final class ItemStackArraySerializer {

    private final ItemStackCodec itemStackCodec;

    /**
     * Returns the result of serialize.
     *
     * @param items the items
     * @return the result of serialize
     */
    public Itemstack.ItemStackArray serialize(ItemStack[] items) {
        Itemstack.ItemStackArray.Builder builder = Itemstack.ItemStackArray.newBuilder();

        if (items == null || items.length == 0) {
            return builder.build();
        }

        for (ItemStack item : items) {
            builder.addItems(serializeNullable(item));
        }

        return builder.build();
    }

    /**
     * Returns the result of serialize to bytes.
     *
     * @param items the items
     * @return the result of serialize to bytes
     */
    public byte[] serializeToBytes(ItemStack[] items) {
        return serialize(items).toByteArray();
    }

    /**
     * Returns the result of deserialize.
     *
     * @param proto the proto
     * @return the result of deserialize
     */
    public ItemStack[] deserialize(Itemstack.ItemStackArray proto) {
        if (proto == null || proto.getItemsCount() == 0) {
            return new ItemStack[0];
        }

        ItemStack[] items = new ItemStack[proto.getItemsCount()];

        for (int i = 0; i < proto.getItemsCount(); i++) {
            items[i] = deserializeNullable(proto.getItems(i));
        }

        return items;
    }

    /**
     * Returns the result of deserialize.
     *
     * @param data the data
     * @return the result of deserialize
     * @throws InvalidProtocolBufferException if the operation fails
     */
    public ItemStack[] deserialize(byte[] data) throws InvalidProtocolBufferException {
        if (data == null || data.length == 0) {
            return new ItemStack[0];
        }

        return deserialize(Itemstack.ItemStackArray.parseFrom(data));
    }

    /**
     * Returns the result of serialize nullable.
     *
     * @param item the item
     * @return the result of serialize nullable
     */
    private Itemstack.ItemStack serializeNullable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return Itemstack.ItemStack.newBuilder().setType("AIR").setAmount(1).build();
        }

        Itemstack.ItemStack serialized = itemStackCodec.serialize(item);
        if (serialized == null) {
            return Itemstack.ItemStack.newBuilder().setType("AIR").setAmount(1).build();
        }

        return serialized;
    }

    /**
     * Returns the result of deserialize nullable.
     *
     * @param proto the proto
     * @return the result of deserialize nullable
     */
    private ItemStack deserializeNullable(Itemstack.ItemStack proto) {
        if (proto == null) {
            return null;
        }

        if ("AIR".equalsIgnoreCase(proto.getType())) {
            return null;
        }

        return itemStackCodec.deserialize(proto);
    }

    /**
     * Returns the result of deserialize to list.
     *
     * @param proto the proto
     * @return the result of deserialize to list
     */
    public List<ItemStack> deserializeToList(Itemstack.ItemStackArray proto) {
        ItemStack[] array = deserialize(proto);
        List<ItemStack> list = new ArrayList<ItemStack>(array.length);
        for (ItemStack item : array) {
            list.add(item);
        }
        return list;
    }
}
