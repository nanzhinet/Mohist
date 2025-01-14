package com.mohistmc.mohist.bukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractSkeleton;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.jetbrains.annotations.NotNull;

public class MohistModsSkeleton extends CraftAbstractSkeleton {
    public MohistModsSkeleton(CraftServer server, net.minecraft.world.entity.monster.AbstractSkeleton entity) {
        super(server, entity);
    }

    public @NotNull Skeleton.SkeletonType getSkeletonType() {
        return SkeletonType.NORMAL;
    }

    @Override
    public String toString() {
        return "MohistModsSkeleton{" + getType() + '}';
    }
}
