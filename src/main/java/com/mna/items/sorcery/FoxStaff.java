/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.pathfinder.PathComputationType
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.items.sorcery;

import com.mna.api.tools.MATags;
import com.mna.items.sorcery.MagicStaff;
import com.mna.tools.SummonUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class FoxStaff
extends MagicStaff {
    public FoxStaff(float attackDamage) {
        super(attackDamage);
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        EntityType<?> summon;
        List<EntityType<?>> woodlandCreatures;
        LivingEntity living;
        if (!player.level().isClientSide() && entity instanceof LivingEntity && !SummonUtils.isTargetFriendly((Entity)(living = (LivingEntity)entity), (LivingEntity)player) && (woodlandCreatures = MATags.getEntitiesOnTag(MATags.EntityTypes.WOODLAND_CREATURES)).size() > 0 && (summon = woodlandCreatures.get((int)(Math.random() * (double)woodlandCreatures.size()))) != null) {
            Entity summonInst;
            BlockPos summonPos = entity.blockPosition().offset(new Vec3i((int)(-2.0 + Math.random() * 4.0), 0, (int)(-2.0 + Math.random() * 4.0)));
            if (player.level().getBlockState(summonPos).isPathfindable(PathComputationType.LAND) && (summonInst = summon.create(player.level())) instanceof Mob) {
                Mob summonMob = (Mob)summonInst;
                summonInst.setPos(Vec3.atBottomCenterOf((Vec3i)summonPos));
                SummonUtils.setSummon(summonMob, (LivingEntity)player, 60);
                summonMob.setTarget(living);
                player.level().addFreshEntity((Entity)summonMob);
            }
        }
        return false;
    }
}

