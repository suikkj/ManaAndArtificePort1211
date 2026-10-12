package com.mna.tools;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class MATools {
    public static boolean isUndead(LivingEntity e) {
        return e instanceof Zombie || e instanceof Husk || e instanceof Drowned
            || e instanceof AbstractSkeleton || e instanceof Skeleton || e instanceof Stray
            || e instanceof WitherSkeleton || e instanceof WitherBoss || e instanceof Phantom
            || e instanceof Zoglin || e instanceof ZombifiedPiglin
            || e instanceof SkeletonHorse || e instanceof ZombieHorse;
    }

    /** [PORT 1.21.1] com.mna.tools.MATools.canChangeDimensions(Entity) (sem args) virou canChangeDimensions(Level, Level). */
    public static boolean canChangeDimensions(net.minecraft.world.entity.Entity entity) {
        return entity.canChangeDimensions(entity.level(), entity.level());
    }
}
