package com.mohistmc.mohist.bukkit.entity;

import net.minecraft.world.entity.monster.Monster;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMonster;

/**
 * Mohist
 *
 * @author Malcolm - m1lc0lm
 * @Created at 20.02.2022 - 21:02 GMT+1
 * © Copyright 2021 / 2022 - M1lcolm
 */
public class MohistModsMonster extends CraftMonster {

    public MohistModsMonster(CraftServer server, Monster entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "MohistModsMonster{" + getType() + '}';
    }
}
