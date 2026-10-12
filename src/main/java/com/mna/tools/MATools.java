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

    /** [PORT 1.21.1] doEnchantDamageEffects foi removido; efeitos de encantamento passam pelo pipeline de dano. */
    public static void doEnchantDamageEffects(net.minecraft.world.entity.LivingEntity attacker, net.minecraft.world.entity.Entity target) {
    }

    /** [PORT 1.21.1] Tier.getLevel() removido (Tiers virou enum). Mapeia tier -> nivel vanilla. */
    public static int getTierLevel(net.minecraft.world.item.Tier tier) {
        if (tier == net.minecraft.world.item.Tiers.WOOD) return 1;
        if (tier == net.minecraft.world.item.Tiers.GOLD) return 2;
        if (tier == net.minecraft.world.item.Tiers.STONE) return 2;
        if (tier == net.minecraft.world.item.Tiers.IRON) return 3;
        if (tier == net.minecraft.world.item.Tiers.DIAMOND) return 4;
        if (tier == net.minecraft.world.item.Tiers.NETHERITE) return 5;
        return 0;
    }
}
