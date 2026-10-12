/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.AgeableMob
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.navigation.GroundPathNavigation
 *  net.minecraft.world.entity.ai.navigation.PathNavigation
 *  net.minecraft.world.entity.animal.Cow
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.ItemUtils
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.LiquidBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.level.pathfinder.PathFinder
 *  net.minecraft.world.level.pathfinder.WalkNodeEvaluator
 *  net.minecraft.world.phys.shapes.CollisionContext
 */
package com.mna.entities.passive;

import com.mna.entities.EntityInit;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Magmoo
extends Cow {
    public Magmoo(EntityType<? extends Cow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.is(Items.BUCKET) && !this.isBaby()) {
            pPlayer.playSound(SoundEvents.BUCKET_FILL_LAVA, 1.0f, 1.0f);
            ItemStack itemstack1 = ItemUtils.createFilledResult((ItemStack)itemstack, (Player)pPlayer, (ItemStack)Items.LAVA_BUCKET.getDefaultInstance());
            pPlayer.setItemInHand(pHand, itemstack1);
            return InteractionResult.sidedSuccess((boolean)this.level().isClientSide());
        }
        return super.mobInteract(pPlayer, pHand);
    }

    public void tick() {
        super.tick();
        this.floatMagmoo();
        this.checkInsideBlocks();
    }

    public boolean checkSpawnObstruction(LevelReader pLevel) {
        return pLevel.isUnobstructed((Entity)this);
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        this.checkInsideBlocks();
        if (this.isInLava()) {
            this.resetFallDistance();
        } else {
            super.checkFallDamage(pY, pOnGround, pState, pPos);
        }
    }

    private void floatMagmoo() {
        if (this.isInLava()) {
            CollisionContext collisioncontext = CollisionContext.of((Entity)this);
            if (collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) && !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.setOnGround(true);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            }
        }
    }

    public boolean fireImmune() {
        return true;
    }

    public boolean canStandOnFluid(FluidState pFluidState) {
        return pFluidState.is(FluidTags.LAVA);
    }

    @Nullable
    public Cow getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return (Cow)((EntityType)EntityInit.MAGMOO.get()).create((Level)pLevel);
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() != null && pSource.getEntity().distanceTo((Entity)this) < 16.0f) {
            pSource.getEntity().lavaHurt();
        }
        return super.hurt(pSource, pAmount);
    }

    public boolean isSensitiveToWater() {
        return true;
    }

    public boolean isOnFire() {
        return false;
    }

    protected PathNavigation createNavigation(Level pLevel) {
        return new MagmooPathNavigation(this, pLevel);
    }

    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        if (pLevel.getBlockState(pPos).getFluidState().is(FluidTags.LAVA)) {
            return 10.0f;
        }
        return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0f;
    }

    static class MagmooPathNavigation
    extends GroundPathNavigation {
        MagmooPathNavigation(Magmoo pStrider, Level pLevel) {
            super((Mob)pStrider, pLevel);
        }

        protected PathFinder createPathFinder(int pMaxVisitedNodes) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
        }

        protected boolean hasValidPathType(PathType pPathType) {
            return pPathType != PathType.LAVA && pPathType != PathType.DAMAGE_FIRE && pPathType != PathType.DANGER_FIRE ? super.hasValidPathType(pPathType) : true;
        }

        public boolean isStableDestination(BlockPos pPos) {
            return this.level.getBlockState(pPos).is(Blocks.LAVA) || super.isStableDestination(pPos);
        }
    }
}

