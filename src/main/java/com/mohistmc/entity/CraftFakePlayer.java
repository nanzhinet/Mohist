package com.mohistmc.entity;

import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;

public class CraftFakePlayer extends CraftPlayer {

    public CraftFakePlayer(CraftServer server, FakePlayer entity) {
        super(server, entity);
    }

    public String toString() {
        return "CraftFakePlayer{" + "name=" + getName() + '}';
    }
}