package me.kasuki.kstaff.utilities.item.codec;

import me.kasuki.kstaff.utilities.bukkit.item.Itemstack;
import org.bukkit.inventory.ItemStack;

/**
 * Defines the contract for item stack codec.
 */
public interface ItemStackCodec {

    /**
     * Returns the result of serialize.
     *
     * @param item the item
     * @return the result of serialize
     */
    Itemstack.ItemStack serialize(ItemStack item);

    /**
     * Returns the result of deserialize.
     *
     * @param proto the proto
     * @return the result of deserialize
     */
    ItemStack deserialize(Itemstack.ItemStack proto);

    /**
     * Returns the codec id.
     *
     * @return the codec id
     */
    String getCodecId();
}
