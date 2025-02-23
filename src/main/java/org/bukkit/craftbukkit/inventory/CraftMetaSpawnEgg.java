package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.util.CraftLegacy;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.MaterialData;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaSpawnEgg extends CraftMetaItem implements SpawnEggMeta {

    private static final Set<Material> SPAWN_EGG_MATERIALS = Sets.newHashSet(
            Material.ARMADILLO_SPAWN_EGG,
            Material.ALLAY_SPAWN_EGG,
            Material.AXOLOTL_SPAWN_EGG,
            Material.BAT_SPAWN_EGG,
            Material.BEE_SPAWN_EGG,
            Material.BLAZE_SPAWN_EGG,
            Material.BOGGED_SPAWN_EGG,
            Material.BREEZE_SPAWN_EGG,
            Material.CAT_SPAWN_EGG,
            Material.CAMEL_SPAWN_EGG,
            Material.CAVE_SPIDER_SPAWN_EGG,
            Material.CHICKEN_SPAWN_EGG,
            Material.COD_SPAWN_EGG,
            Material.COW_SPAWN_EGG,
            Material.CREEPER_SPAWN_EGG,
            Material.DOLPHIN_SPAWN_EGG,
            Material.DONKEY_SPAWN_EGG,
            Material.DROWNED_SPAWN_EGG,
            Material.ELDER_GUARDIAN_SPAWN_EGG,
            Material.ENDER_DRAGON_SPAWN_EGG,
            Material.ENDERMAN_SPAWN_EGG,
            Material.ENDERMITE_SPAWN_EGG,
            Material.EVOKER_SPAWN_EGG,
            Material.FOX_SPAWN_EGG,
            Material.FROG_SPAWN_EGG,
            Material.GHAST_SPAWN_EGG,
            Material.GLOW_SQUID_SPAWN_EGG,
            Material.GOAT_SPAWN_EGG,
            Material.GUARDIAN_SPAWN_EGG,
            Material.HOGLIN_SPAWN_EGG,
            Material.HORSE_SPAWN_EGG,
            Material.HUSK_SPAWN_EGG,
            Material.IRON_GOLEM_SPAWN_EGG,
            Material.LLAMA_SPAWN_EGG,
            Material.MAGMA_CUBE_SPAWN_EGG,
            Material.MOOSHROOM_SPAWN_EGG,
            Material.MULE_SPAWN_EGG,
            Material.OCELOT_SPAWN_EGG,
            Material.PANDA_SPAWN_EGG,
            Material.PARROT_SPAWN_EGG,
            Material.PHANTOM_SPAWN_EGG,
            Material.PIGLIN_BRUTE_SPAWN_EGG,
            Material.PIGLIN_SPAWN_EGG,
            Material.PIG_SPAWN_EGG,
            Material.PILLAGER_SPAWN_EGG,
            Material.POLAR_BEAR_SPAWN_EGG,
            Material.PUFFERFISH_SPAWN_EGG,
            Material.RABBIT_SPAWN_EGG,
            Material.RAVAGER_SPAWN_EGG,
            Material.SALMON_SPAWN_EGG,
            Material.SHEEP_SPAWN_EGG,
            Material.SHULKER_SPAWN_EGG,
            Material.SILVERFISH_SPAWN_EGG,
            Material.SKELETON_HORSE_SPAWN_EGG,
            Material.SKELETON_SPAWN_EGG,
            Material.SLIME_SPAWN_EGG,
            Material.SNIFFER_SPAWN_EGG,
            Material.SNOW_GOLEM_SPAWN_EGG,
            Material.SPIDER_SPAWN_EGG,
            Material.SQUID_SPAWN_EGG,
            Material.STRAY_SPAWN_EGG,
            Material.STRIDER_SPAWN_EGG,
            Material.TADPOLE_SPAWN_EGG,
            Material.TRADER_LLAMA_SPAWN_EGG,
            Material.TROPICAL_FISH_SPAWN_EGG,
            Material.TURTLE_SPAWN_EGG,
            Material.VEX_SPAWN_EGG,
            Material.VILLAGER_SPAWN_EGG,
            Material.VINDICATOR_SPAWN_EGG,
            Material.WARDEN_SPAWN_EGG,
            Material.WANDERING_TRADER_SPAWN_EGG,
            Material.WITCH_SPAWN_EGG,
            Material.WITHER_SPAWN_EGG,
            Material.WITHER_SKELETON_SPAWN_EGG,
            Material.WOLF_SPAWN_EGG,
            Material.ZOGLIN_SPAWN_EGG,
            Material.ZOMBIE_HORSE_SPAWN_EGG,
            Material.ZOMBIE_SPAWN_EGG,
            Material.ZOMBIE_VILLAGER_SPAWN_EGG,
            Material.ZOMBIFIED_PIGLIN_SPAWN_EGG
    );

    static final ItemMetaKeyType<CustomData> ENTITY_TAG = new ItemMetaKeyType<>(DataComponents.ENTITY_DATA, "entity-tag");
    @ItemMetaKey.Specific(ItemMetaKey.Specific.To.NBT)
    static final ItemMetaKey ENTITY_ID = new ItemMetaKey("id");

    private CompoundTag entityTag;

    CraftMetaSpawnEgg(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaSpawnEgg egg)) {
            return;
        }

        this.entityTag = egg.entityTag;
    }

    CraftMetaSpawnEgg(DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, CraftMetaSpawnEgg.ENTITY_TAG).ifPresent((nbt) -> {
            this.entityTag = nbt.copyTag();
        });
    }

    CraftMetaSpawnEgg(Map<String, Object> map) {
        super(map);
    }

    @Override
    void deserializeInternal(CompoundTag tag, Object context) {
        super.deserializeInternal(tag, context);

        if (tag.contains(CraftMetaSpawnEgg.ENTITY_TAG.NBT)) {
            this.entityTag = tag.getCompound(CraftMetaSpawnEgg.ENTITY_TAG.NBT);

            // Tag still has some other data, lets try our luck with a conversion
            if (!this.entityTag.isEmpty()) {
                // SPIGOT-4128: This is hopeless until we start versioning stacks. RIP data.
                // entityTag = (NBTTagCompound) MinecraftServer.getServer().dataConverterManager.update(DataConverterTypes.ENTITY, new Dynamic(DynamicOpsNBT.a, entityTag), -1, CraftMagicNumbers.DATA_VERSION).getValue();
            }
        }
    }

    @Override
    void serializeInternal(Map<String, Tag> internalTags) {
        if (this.entityTag != null && !this.entityTag.isEmpty()) {
            internalTags.put(CraftMetaSpawnEgg.ENTITY_TAG.NBT, this.entityTag);
        }
    }

    @Override
    void applyToItem(CraftMetaItem.Applicator tag) {
        super.applyToItem(tag);

        if (this.entityTag != null) {
            tag.put(CraftMetaSpawnEgg.ENTITY_TAG, CustomData.of(this.entityTag));
        }
    }

    @Override
    boolean applicableTo(Material type) {
        return CraftMetaSpawnEgg.SPAWN_EGG_MATERIALS.contains(type);
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && this.isSpawnEggEmpty();
    }

    boolean isSpawnEggEmpty() {
        return !(entityTag != null);
    }

    @Override
    public EntityType getSpawnedType() {
        throw new UnsupportedOperationException("Must check item type to get spawned type");
    }

    @Override
    public void setSpawnedType(EntityType type) {
        throw new UnsupportedOperationException("Must change item type to set spawned type");
    }

    @Override
    public EntitySnapshot getSpawnedEntity() {
        return CraftEntitySnapshot.create(this.entityTag);
    }

    @Override
    public void setSpawnedEntity(EntitySnapshot snapshot) {
        Preconditions.checkArgument(snapshot.getEntityType().isSpawnable(), "Entity is not spawnable");
        this.entityTag = ((CraftEntitySnapshot) snapshot).getData();
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaSpawnEgg) {
            CraftMetaSpawnEgg that = (CraftMetaSpawnEgg) meta;

            return entityTag != null ? that.entityTag != null && this.entityTag.equals(that.entityTag) : entityTag == null;
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaSpawnEgg || this.isSpawnEggEmpty());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (this.entityTag != null) {
            hash = 73 * hash + this.entityTag.hashCode();
        }

        return original != hash ? CraftMetaSpawnEgg.class.hashCode() ^ hash : hash;
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        return builder;
    }

    @Override
    public CraftMetaSpawnEgg clone() {
        CraftMetaSpawnEgg clone = (CraftMetaSpawnEgg) super.clone();

        if (this.entityTag != null) {
            clone.entityTag = this.entityTag.copy();
        }

        return clone;
    }
}
