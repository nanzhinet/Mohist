package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.util.CraftLegacy;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

@DelegateDeserialization(ItemStack.class)
public final class CraftItemStack extends ItemStack {

    public static net.minecraft.world.item.ItemStack asNMSCopy(ItemStack original) {
        if (original instanceof CraftItemStack) {
            CraftItemStack stack = (CraftItemStack) original;
            return stack.handle == null ? net.minecraft.world.item.ItemStack.EMPTY : stack.handle.copy();
        }
        if (original == null || original.getType() == Material.AIR) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        Item item = CraftItemType.bukkitToMinecraft(original.getType());

        if (item == null) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, original.getAmount());
        if (original.hasItemMeta()) {
            CraftItemStack.setItemMeta(stack, original.getItemMeta());
        }
        return stack;
    }

    public static net.minecraft.world.item.ItemStack copyNMSStack(net.minecraft.world.item.ItemStack original, int amount) {
        net.minecraft.world.item.ItemStack stack = original.copy();
        stack.setCount(amount);
        return stack;
    }

    /**
     * Copies the NMS stack to return as a strictly-Bukkit stack
     */
    public static ItemStack asBukkitCopy(net.minecraft.world.item.ItemStack original) {
        if (original.isEmpty()) {
            return new ItemStack(Material.AIR);
        }
        ItemStack stack = new ItemStack(CraftItemType.minecraftToBukkit(original.getItem()), original.getCount());
        if (CraftItemStack.hasItemMeta(original)) {
            stack.setItemMeta(CraftItemStack.getItemMeta(original));
        }
        return stack;
    }

    public static CraftItemStack asCraftMirror(net.minecraft.world.item.ItemStack original) {
        return new CraftItemStack((original == null || original.isEmpty()) ? null : original);
    }

    public static CraftItemStack asCraftCopy(ItemStack original) {
        if (original instanceof CraftItemStack) {
            CraftItemStack stack = (CraftItemStack) original;
            return new CraftItemStack(stack.handle == null ? null : stack.handle.copy());
        }
        return new CraftItemStack(original);
    }

    public static CraftItemStack asNewCraftStack(Item item) {
        return CraftItemStack.asNewCraftStack(item, 1);
    }

    public static CraftItemStack asNewCraftStack(Item item, int amount) {
        return new CraftItemStack(CraftItemType.minecraftToBukkit(item), amount, (short) 0, null);
    }

    net.minecraft.world.item.ItemStack handle;

    /**
     * Mirror
     */
    private CraftItemStack(net.minecraft.world.item.ItemStack item) {
        this.handle = item;
    }

    private CraftItemStack(ItemStack item) {
        this(item.getType(), item.getAmount(), item.getDurability(), item.hasItemMeta() ? item.getItemMeta() : null);
    }

    private CraftItemStack(Material type, int amount, short durability, ItemMeta itemMeta) {
        this.setType(type);
        this.setAmount(amount);
        this.setDurability(durability);
        this.setItemMeta(itemMeta);
    }

    @Override
    public MaterialData getData() {
        return this.handle != null ? CraftMagicNumbers.getMaterialData(this.handle.getItem()) : super.getData();
    }

    @Override
    public Material getType() {
        return this.handle != null ? CraftItemType.minecraftToBukkit(this.handle.getItem()) : Material.AIR;
    }

    @Override
    public void setType(Material type) {
        if (this.getType() == type) {
            return;
        } else if (type == Material.AIR) {
            this.handle = null;
        } else if (CraftItemType.bukkitToMinecraft(type) == null) { // :(
            this.handle = null;
        } else if (this.handle == null) {
            this.handle = new net.minecraft.world.item.ItemStack(CraftItemType.bukkitToMinecraft(type), 1);
        } else {
            this.handle.setItem(CraftItemType.bukkitToMinecraft(type));
            if (this.hasItemMeta()) {
                // This will create the appropriate item meta, which will contain all the data we intend to keep
                CraftItemStack.setItemMeta(this.handle, CraftItemStack.getItemMeta(this.handle));
            }
        }
        this.setData(null);
    }

    @Override
    public int getAmount() {
        return this.handle != null ? this.handle.getCount() : 0;
    }

    @Override
    public void setAmount(int amount) {
        if (this.handle == null) {
            return;
        }

        this.handle.setCount(amount);
        if (amount == 0) {
            this.handle = null;
        }
    }

    @Override
    public void setDurability(final short durability) {
        // Ignore damage if item is null
        if (this.handle != null) {
            this.handle.setDamageValue(durability);
        }
    }

    @Override
    public short getDurability() {
        if (this.handle != null) {
            return (short) this.handle.getDamageValue();
        } else {
            return -1;
        }
    }

    @Override
    public int getMaxStackSize() {
        return (this.handle == null) ? Material.AIR.getMaxStackSize() : this.handle.getMaxStackSize();
    }

    @Override
    public void addUnsafeEnchantment(Enchantment ench, int level) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");

        if (!CraftItemStack.makeTag(this.handle)) {
            return;
        }
        ItemEnchantments list = CraftItemStack.getEnchantmentList(this.handle);
        if (list == null) {
            list = ItemEnchantments.EMPTY;
        }
        ItemEnchantments.Mutable listCopy = new ItemEnchantments.Mutable(list);
        listCopy.set(CraftEnchantment.bukkitToMinecraftHolder(ench), level);
        this.handle.set(DataComponents.ENCHANTMENTS, listCopy.toImmutable());
    }

    static boolean makeTag(net.minecraft.world.item.ItemStack item) {
        if (item == null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean containsEnchantment(Enchantment ench) {
        return this.getEnchantmentLevel(ench) > 0;
    }

    @Override
    public int getEnchantmentLevel(Enchantment ench) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");
        if (this.handle == null) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(CraftEnchantment.bukkitToMinecraftHolder(ench), this.handle);
    }

    @Override
    public int removeEnchantment(Enchantment ench) {
        Preconditions.checkArgument(ench != null, "Enchantment cannot be null");

        ItemEnchantments list = CraftItemStack.getEnchantmentList(this.handle);
        if (list == null) {
            return 0;
        }
        int level = this.getEnchantmentLevel(ench);
        if (level <= 0) {
            return 0;
        }
        int size = list.size();

        if (size == 1) {
            this.handle.remove(DataComponents.ENCHANTMENTS);
            return level;
        }

        ItemEnchantments.Mutable listCopy = new ItemEnchantments.Mutable(list);
        listCopy.set(CraftEnchantment.bukkitToMinecraftHolder(ench), -1); // Negative to remove
        this.handle.set(DataComponents.ENCHANTMENTS, listCopy.toImmutable());

        return level;
    }

    @Override
    public void removeEnchantments() {
        this.handle.remove(DataComponents.ENCHANTMENTS);
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return CraftItemStack.getEnchantments(this.handle);
    }

    static Map<Enchantment, Integer> getEnchantments(net.minecraft.world.item.ItemStack item) {
        ItemEnchantments list = (item != null && item.isEnchanted()) ? item.get(DataComponents.ENCHANTMENTS) : null;

        if (list == null || list.size() == 0) {
            return ImmutableMap.of();
        }

        ImmutableMap.Builder<Enchantment, Integer> result = ImmutableMap.builder();

        list.entrySet().forEach((entry) -> {
            Holder<net.minecraft.world.item.enchantment.Enchantment> id = entry.getKey();
            int level = entry.getIntValue();

            Enchantment enchant = CraftEnchantment.minecraftHolderToBukkit(id);
            if (enchant != null) {
                result.put(enchant, level);
            }
        });

        return result.build();
    }

    static ItemEnchantments getEnchantmentList(net.minecraft.world.item.ItemStack item) {
        return (item != null && item.isEnchanted()) ? item.get(DataComponents.ENCHANTMENTS) : null;
    }

    @Override
    public CraftItemStack clone() {
        CraftItemStack itemStack = (CraftItemStack) super.clone();
        if (this.handle != null) {
            itemStack.handle = this.handle.copy();
        }
        return itemStack;
    }

    @Override
    public ItemMeta getItemMeta() {
        return CraftItemStack.getItemMeta(this.handle);
    }

    public static ItemMeta getItemMeta(net.minecraft.world.item.ItemStack item) {
        if (!CraftItemStack.hasItemMeta(item)) {
            return CraftItemFactory.instance().getItemMeta(CraftItemStack.getType(item));
        }
        switch (CraftItemStack.getType(item)) {
            case WRITTEN_BOOK:
                return new CraftMetaBookSigned(item.getComponentsPatch());
            case WRITABLE_BOOK:
                return new CraftMetaBook(item.getComponentsPatch());
            case CREEPER_HEAD:
            case CREEPER_WALL_HEAD:
            case DRAGON_HEAD:
            case DRAGON_WALL_HEAD:
            case PIGLIN_HEAD:
            case PIGLIN_WALL_HEAD:
            case PLAYER_HEAD:
            case PLAYER_WALL_HEAD:
            case SKELETON_SKULL:
            case SKELETON_WALL_SKULL:
            case WITHER_SKELETON_SKULL:
            case WITHER_SKELETON_WALL_SKULL:
            case ZOMBIE_HEAD:
            case ZOMBIE_WALL_HEAD:
                return new CraftMetaSkull(item.getComponentsPatch());
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_LEGGINGS:
            case NETHERITE_BOOTS:
            case TURTLE_HELMET:
                return new CraftMetaArmor(item.getComponentsPatch());
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case WOLF_ARMOR:
                return new CraftMetaColorableArmor(item.getComponentsPatch());
            case LEATHER_HORSE_ARMOR:
                return new CraftMetaLeatherArmor(item.getComponentsPatch());
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case TIPPED_ARROW:
                return new CraftMetaPotion(item.getComponentsPatch());
            case FILLED_MAP:
                return new CraftMetaMap(item.getComponentsPatch());
            case FIREWORK_ROCKET:
                return new CraftMetaFirework(item.getComponentsPatch());
            case FIREWORK_STAR:
                return new CraftMetaCharge(item.getComponentsPatch());
            case ENCHANTED_BOOK:
                return new CraftMetaEnchantedBook(item.getComponentsPatch());
            case BLACK_BANNER:
            case BLACK_WALL_BANNER:
            case BLUE_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_BANNER:
            case BROWN_WALL_BANNER:
            case CYAN_BANNER:
            case CYAN_WALL_BANNER:
            case GRAY_BANNER:
            case GRAY_WALL_BANNER:
            case GREEN_BANNER:
            case GREEN_WALL_BANNER:
            case LIGHT_BLUE_BANNER:
            case LIGHT_BLUE_WALL_BANNER:
            case LIGHT_GRAY_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case LIME_BANNER:
            case LIME_WALL_BANNER:
            case MAGENTA_BANNER:
            case MAGENTA_WALL_BANNER:
            case ORANGE_BANNER:
            case ORANGE_WALL_BANNER:
            case PINK_BANNER:
            case PINK_WALL_BANNER:
            case PURPLE_BANNER:
            case PURPLE_WALL_BANNER:
            case RED_BANNER:
            case RED_WALL_BANNER:
            case WHITE_BANNER:
            case WHITE_WALL_BANNER:
            case YELLOW_BANNER:
            case YELLOW_WALL_BANNER:
                return new CraftMetaBanner(item.getComponentsPatch());
            case ARMADILLO_SPAWN_EGG:
            case ALLAY_SPAWN_EGG:
            case AXOLOTL_SPAWN_EGG:
            case BAT_SPAWN_EGG:
            case BEE_SPAWN_EGG:
            case BLAZE_SPAWN_EGG:
            case BOGGED_SPAWN_EGG:
            case BREEZE_SPAWN_EGG:
            case CAT_SPAWN_EGG:
            case CAMEL_SPAWN_EGG:
            case CAVE_SPIDER_SPAWN_EGG:
            case CHICKEN_SPAWN_EGG:
            case COD_SPAWN_EGG:
            case COW_SPAWN_EGG:
            case CREEPER_SPAWN_EGG:
            case DOLPHIN_SPAWN_EGG:
            case DONKEY_SPAWN_EGG:
            case DROWNED_SPAWN_EGG:
            case ELDER_GUARDIAN_SPAWN_EGG:
            case ENDER_DRAGON_SPAWN_EGG:
            case ENDERMAN_SPAWN_EGG:
            case ENDERMITE_SPAWN_EGG:
            case EVOKER_SPAWN_EGG:
            case FOX_SPAWN_EGG:
            case FROG_SPAWN_EGG:
            case GHAST_SPAWN_EGG:
            case GLOW_SQUID_SPAWN_EGG:
            case GOAT_SPAWN_EGG:
            case GUARDIAN_SPAWN_EGG:
            case HOGLIN_SPAWN_EGG:
            case HORSE_SPAWN_EGG:
            case HUSK_SPAWN_EGG:
            case IRON_GOLEM_SPAWN_EGG:
            case LLAMA_SPAWN_EGG:
            case MAGMA_CUBE_SPAWN_EGG:
            case MOOSHROOM_SPAWN_EGG:
            case MULE_SPAWN_EGG:
            case OCELOT_SPAWN_EGG:
            case PANDA_SPAWN_EGG:
            case PARROT_SPAWN_EGG:
            case PHANTOM_SPAWN_EGG:
            case PIGLIN_BRUTE_SPAWN_EGG:
            case PIGLIN_SPAWN_EGG:
            case PIG_SPAWN_EGG:
            case PILLAGER_SPAWN_EGG:
            case POLAR_BEAR_SPAWN_EGG:
            case PUFFERFISH_SPAWN_EGG:
            case RABBIT_SPAWN_EGG:
            case RAVAGER_SPAWN_EGG:
            case SALMON_SPAWN_EGG:
            case SHEEP_SPAWN_EGG:
            case SHULKER_SPAWN_EGG:
            case SILVERFISH_SPAWN_EGG:
            case SKELETON_HORSE_SPAWN_EGG:
            case SKELETON_SPAWN_EGG:
            case SLIME_SPAWN_EGG:
            case SNIFFER_SPAWN_EGG:
            case SNOW_GOLEM_SPAWN_EGG:
            case SPIDER_SPAWN_EGG:
            case SQUID_SPAWN_EGG:
            case STRAY_SPAWN_EGG:
            case STRIDER_SPAWN_EGG:
            case TADPOLE_SPAWN_EGG:
            case TRADER_LLAMA_SPAWN_EGG:
            case TROPICAL_FISH_SPAWN_EGG:
            case TURTLE_SPAWN_EGG:
            case VEX_SPAWN_EGG:
            case VILLAGER_SPAWN_EGG:
            case VINDICATOR_SPAWN_EGG:
            case WANDERING_TRADER_SPAWN_EGG:
            case WARDEN_SPAWN_EGG:
            case WITCH_SPAWN_EGG:
            case WITHER_SKELETON_SPAWN_EGG:
            case WITHER_SPAWN_EGG:
            case WOLF_SPAWN_EGG:
            case ZOGLIN_SPAWN_EGG:
            case ZOMBIE_HORSE_SPAWN_EGG:
            case ZOMBIE_SPAWN_EGG:
            case ZOMBIE_VILLAGER_SPAWN_EGG:
            case ZOMBIFIED_PIGLIN_SPAWN_EGG:
                return new CraftMetaSpawnEgg(item.getComponentsPatch());
            case ARMOR_STAND:
                return new CraftMetaArmorStand(item.getComponentsPatch());
            case KNOWLEDGE_BOOK:
                return new CraftMetaKnowledgeBook(item.getComponentsPatch());
            case FURNACE:
            case CHEST:
            case TRAPPED_CHEST:
            case JUKEBOX:
            case DISPENSER:
            case DROPPER:
            case ACACIA_HANGING_SIGN:
            case ACACIA_SIGN:
            case ACACIA_WALL_HANGING_SIGN:
            case ACACIA_WALL_SIGN:
            case BAMBOO_HANGING_SIGN:
            case BAMBOO_SIGN:
            case BAMBOO_WALL_HANGING_SIGN:
            case BAMBOO_WALL_SIGN:
            case BIRCH_HANGING_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_HANGING_SIGN:
            case BIRCH_WALL_SIGN:
            case CHERRY_HANGING_SIGN:
            case CHERRY_SIGN:
            case CHERRY_WALL_HANGING_SIGN:
            case CHERRY_WALL_SIGN:
            case CRIMSON_HANGING_SIGN:
            case CRIMSON_SIGN:
            case CRIMSON_WALL_HANGING_SIGN:
            case CRIMSON_WALL_SIGN:
            case DARK_OAK_HANGING_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_HANGING_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_HANGING_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_HANGING_SIGN:
            case JUNGLE_WALL_SIGN:
            case MANGROVE_HANGING_SIGN:
            case MANGROVE_SIGN:
            case MANGROVE_WALL_HANGING_SIGN:
            case MANGROVE_WALL_SIGN:
            case OAK_HANGING_SIGN:
            case OAK_SIGN:
            case OAK_WALL_HANGING_SIGN:
            case OAK_WALL_SIGN:
            case SPRUCE_HANGING_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_WALL_HANGING_SIGN:
            case SPRUCE_WALL_SIGN:
            case WARPED_HANGING_SIGN:
            case WARPED_SIGN:
            case WARPED_WALL_HANGING_SIGN:
            case WARPED_WALL_SIGN:
            case SPAWNER:
            case BREWING_STAND:
            case ENCHANTING_TABLE:
            case COMMAND_BLOCK:
            case REPEATING_COMMAND_BLOCK:
            case CHAIN_COMMAND_BLOCK:
            case BEACON:
            case DAYLIGHT_DETECTOR:
            case HOPPER:
            case COMPARATOR:
            case SHIELD:
            case STRUCTURE_BLOCK:
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case ENDER_CHEST:
            case BARREL:
            case BELL:
            case BLAST_FURNACE:
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
            case JIGSAW:
            case LECTERN:
            case SMOKER:
            case BEEHIVE:
            case BEE_NEST:
            case SCULK_CATALYST:
            case SCULK_SHRIEKER:
            case SCULK_SENSOR:
            case CALIBRATED_SCULK_SENSOR:
            case CHISELED_BOOKSHELF:
            case DECORATED_POT:
            case SUSPICIOUS_SAND:
            case SUSPICIOUS_GRAVEL:
            case CRAFTER:
            case TRIAL_SPAWNER:
            case VAULT:
                return new CraftMetaBlockState(item.getComponentsPatch(), CraftItemType.minecraftToBukkit(item.getItem()));
            case TROPICAL_FISH_BUCKET:
                return new CraftMetaTropicalFishBucket(item.getComponentsPatch());
            case AXOLOTL_BUCKET:
                return new CraftMetaAxolotlBucket(item.getComponentsPatch());
            case CROSSBOW:
                return new CraftMetaCrossbow(item.getComponentsPatch());
            case SUSPICIOUS_STEW:
                return new CraftMetaSuspiciousStew(item.getComponentsPatch());
            case COD_BUCKET:
            case PUFFERFISH_BUCKET:
            case SALMON_BUCKET:
            case ITEM_FRAME:
            case GLOW_ITEM_FRAME:
            case PAINTING:
                return new CraftMetaEntityTag(item.getComponentsPatch());
            case COMPASS:
                return new CraftMetaCompass(item.getComponentsPatch());
            case BUNDLE:
                return new CraftMetaBundle(item.getComponentsPatch());
            case GOAT_HORN:
                return new CraftMetaMusicInstrument(item.getComponentsPatch());
            case OMINOUS_BOTTLE:
                return new CraftMetaOminousBottle(item.getComponentsPatch());
            default:
                return new CraftMetaItem(item.getComponentsPatch());
        }
    }

    static Material getType(net.minecraft.world.item.ItemStack item) {
        return item == null ? Material.AIR : CraftItemType.minecraftToBukkit(item.getItem());
    }

    @Override
    public boolean setItemMeta(ItemMeta itemMeta) {
        return CraftItemStack.setItemMeta(this.handle, itemMeta);
    }

    public static boolean setItemMeta(net.minecraft.world.item.ItemStack item, ItemMeta itemMeta) {
        if (item == null) {
            return false;
        }
        if (CraftItemFactory.instance().equals(itemMeta, null)) {
            item.restorePatch(DataComponentPatch.EMPTY);
            return true;
        }
        if (!CraftItemFactory.instance().isApplicable(itemMeta, CraftItemStack.getType(item))) {
            return false;
        }

        itemMeta = CraftItemFactory.instance().asMetaFor(itemMeta, CraftItemStack.getType(item));
        if (itemMeta == null) return true;

        if (!((CraftMetaItem) itemMeta).isEmpty()) {
            CraftMetaItem.Applicator tag = new CraftMetaItem.Applicator();

            ((CraftMetaItem) itemMeta).applyToItem(tag);
            item.restorePatch(tag.build());
        }
        // SpigotCraft#463 this is required now by the Vanilla client, so mimic ItemStack constructor in ensuring it
        if (item.getItem() != null && item.getMaxDamage() > 0) {
            item.setDamageValue(item.getDamageValue());
        }

        return true;
    }

    @Override
    public boolean isSimilar(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack == this) {
            return true;
        }
        if (!(stack instanceof CraftItemStack)) {
            return stack.getClass() == ItemStack.class && stack.isSimilar(this);
        }

        CraftItemStack that = (CraftItemStack) stack;
        if (this.handle == that.handle) {
            return true;
        }
        if (this.handle == null || that.handle == null) {
            return false;
        }
        Material comparisonType = CraftLegacy.fromLegacy(that.getType()); // This may be called from legacy item stacks, try to get the right material
        if (!(comparisonType == this.getType() && this.getDurability() == that.getDurability())) {
            return false;
        }
        return this.hasItemMeta() ? that.hasItemMeta() && this.handle.getComponents().equals(that.handle.getComponents()) : !that.hasItemMeta();
    }

    @Override
    public boolean hasItemMeta() {
        return CraftItemStack.hasItemMeta(this.handle) && !CraftItemFactory.instance().equals(this.getItemMeta(), null);
    }

    static boolean hasItemMeta(net.minecraft.world.item.ItemStack item) {
        return !(item == null || item.getComponentsPatch().isEmpty());
    }
}
