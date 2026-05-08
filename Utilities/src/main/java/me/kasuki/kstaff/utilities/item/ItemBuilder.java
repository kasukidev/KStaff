package me.kasuki.kstaff.utilities.item;

import com.cryptomorin.xseries.XMaterial;
import me.kasuki.kstaff.utilities.chat.CC;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Builder for item instances.
 */
public class ItemBuilder {

    /**
     * Stores item stack.
     */
    private final ItemStack itemStack;

    /**
     * Stores item meta.
     */
    private final ItemMeta itemMeta;

    /**
     * Creates a new ItemBuilder instance.
     */
    public ItemBuilder(XMaterial material, int amount) {
        this.itemStack = material.parseItem();
        this.itemStack.setAmount(amount);
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Creates a new ItemBuilder instance.
     */
    public ItemBuilder(XMaterial material) {
        this(material, 1);
    }

    /**
     * Sets type.
     */
    public ItemBuilder setType(Material material) {
        this.itemStack.setType(material);
        return this;
    }

    /**
     * Sets amount.
     */
    public ItemBuilder setAmount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    /**
     * Sets name.
     */
    public ItemBuilder setName(String name) {
        this.itemMeta.setDisplayName(CC.chat(name));
        return this;
    }

    /**
     * Sets lore.
     */
    public ItemBuilder setLore(List<String> lore) {
        this.itemMeta.setLore(CC.chat(lore));
        return this;
    }

    /**
     * Sets unbreakable.
     */
    public ItemBuilder setUnbreakable(boolean val) {
        this.itemMeta.spigot().setUnbreakable(val);
        return this;
    }

    /**
     * Executes add enchantment.
     */
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Executes build.
     */
    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }
}
