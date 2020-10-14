package org.bukkit.craftbukkit.v1_16_R2.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.PoweredMinecart;

@SuppressWarnings("deprecation")
public class CraftMinecartFurnace extends CraftMinecart implements PoweredMinecart {
    public CraftMinecartFurnace(CraftServer server, FurnaceMinecartEntity entity) {
        super(server, entity);
    }

    @Override
    public FurnaceMinecartEntity getHandle() {
        return (FurnaceMinecartEntity) entity;
    }

    @Override
    public int getFuel() {
        return getHandle().fuel;
    }

    @Override
    public void setFuel(int fuel) {
        Preconditions.checkArgument(fuel >= 0, "ticks cannot be negative");
        getHandle().fuel = fuel;
    }

    @Override
    public String toString() {
        return "CraftMinecartFurnace";
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_FURNACE;
    }


}
