package org.bukkit.craftbukkit.enchantments;

import com.google.common.base.Preconditions;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class CraftEnchantment extends Enchantment implements Handleable<net.minecraft.world.item.enchantment.Enchantment> {

    public static Enchantment minecraftToBukkit(net.minecraft.world.item.enchantment.Enchantment minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.ENCHANTMENT, Registry.ENCHANTMENT);
    }

    public static Enchantment minecraftHolderToBukkit(Holder<net.minecraft.world.item.enchantment.Enchantment> minecraft) {
        return CraftEnchantment.minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.item.enchantment.Enchantment bukkitToMinecraft(Enchantment bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static Holder<net.minecraft.world.item.enchantment.Enchantment> bukkitToMinecraftHolder(Enchantment bukkit) {
        return CraftRegistry.bukkitToMinecraftHolder(bukkit, Registries.ENCHANTMENT);
    }

    public static String bukkitToString(Enchantment bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static Enchantment stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        // We currently do not have any version-dependent remapping, so we can use current version
        // First convert from when only the names where saved
        string = FieldRename.convertEnchantmentName(ApiVersion.CURRENT, string);
        string = string.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(string);

        // Now also convert from when keys where saved
        return CraftRegistry.get(Registry.ENCHANTMENT, key, ApiVersion.CURRENT);
    }

    private final NamespacedKey key;
    private final Holder<net.minecraft.world.item.enchantment.Enchantment> handle;
    private final int id;

    public CraftEnchantment(NamespacedKey key, net.minecraft.world.item.enchantment.Enchantment handle) {
        this.key = key;
        this.handle = CraftRegistry.getMinecraftRegistry(Registries.ENCHANTMENT).wrapAsHolder(handle);
        this.id = CraftRegistry.getMinecraftRegistry(Registries.ENCHANTMENT).getId(handle);
    }

    @Override
    public net.minecraft.world.item.enchantment.Enchantment getHandle() {
        return this.handle.value();
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public int getMaxLevel() {
        return this.getHandle().getMaxLevel();
    }

    @Override
    public int getStartLevel() {
        return this.getHandle().getMinLevel();
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        throw new UnsupportedOperationException("Method no longer applicable. Use Tags instead.");
    }

    @Override
    public boolean isTreasure() {
        return !handle.is(EnchantmentTags.IN_ENCHANTING_TABLE);
    }

    @Override
    public boolean isCursed() {
        return handle.is(EnchantmentTags.CURSE);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return this.getHandle().canEnchant(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public String getName() {
        // PAIL: migration paths
        return switch (this.id) {
            case 0 -> "PROTECTION_ENVIRONMENTAL";
            case 1 -> "PROTECTION_FIRE";
            case 2 -> "PROTECTION_FALL";
            case 3 -> "PROTECTION_EXPLOSIONS";
            case 4 -> "PROTECTION_PROJECTILE";
            case 5 -> "OXYGEN";
            case 6 -> "WATER_WORKER";
            case 7 -> "THORNS";
            case 8 -> "DEPTH_STRIDER";
            case 9 -> "FROST_WALKER";
            case 10 -> "BINDING_CURSE";
            case 11 -> "SOUL_SPEED";
            case 12 -> "SWIFT_SNEAK";
            case 13 -> "DAMAGE_ALL";
            case 14 -> "DAMAGE_UNDEAD";
            case 15 -> "DAMAGE_ARTHROPODS";
            case 16 -> "KNOCKBACK";
            case 17 -> "FIRE_ASPECT";
            case 18 -> "LOOT_BONUS_MOBS";
            case 19 -> "SWEEPING_EDGE";
            case 20 -> "DIG_SPEED";
            case 21 -> "SILK_TOUCH";
            case 22 -> "DURABILITY";
            case 23 -> "LOOT_BONUS_BLOCKS";
            case 24 -> "ARROW_DAMAGE";
            case 25 -> "ARROW_KNOCKBACK";
            case 26 -> "ARROW_FIRE";
            case 27 -> "ARROW_INFINITE";
            case 28 -> "LUCK";
            case 29 -> "LURE";
            case 30 -> "LOYALTY";
            case 31 -> "IMPALING";
            case 32 -> "RIPTIDE";
            case 33 -> "CHANNELING";
            case 34 -> "MULTISHOT";
            case 35 -> "QUICK_CHARGE";
            case 36 -> "PIERCING";
            case 37 -> "DENSITY";
            case 38 -> "BREACH";
            case 39 -> "WIND_BURST";
            case 40 -> "MENDING";
            case 41 -> "VANISHING_CURSE";
            default -> this.getKey().toString();
        };
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        if (other instanceof EnchantmentWrapper) {
            other = ((EnchantmentWrapper) other).getEnchantment();
        }
        if (!(other instanceof CraftEnchantment)) {
            return false;
        }
        CraftEnchantment ench = (CraftEnchantment) other;
        return !net.minecraft.world.item.enchantment.Enchantment.areCompatible(handle, ench.handle);
    }

    @Override
    public String getTranslationKey() {
        return Util.makeDescriptionId("enchantment", handle.unwrapKey().get().location());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof CraftEnchantment)) {
            return false;
        }

        return this.getKey().equals(((Enchantment) other).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public String toString() {
        return "CraftEnchantment[" + this.getKey() + "]";
    }
}
