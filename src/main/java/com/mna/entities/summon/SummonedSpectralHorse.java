 
package com.mna.entities.summon;

import com.mna.tools.SummonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;

public class SummonedSpectralHorse
extends AbstractHorse {
    public SummonedSpectralHorse(EntityType<? extends AbstractHorse> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.ATTACK_KNOCKBACK).add(Attributes.JUMP_STRENGTH, 1.25);
    }

    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)0.35f);
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(1.25);
        this.getAttribute(Attributes.GRAVITY).setBaseValue((double)0.05f);
    }

    public float getStepHeight() {
        return 1.75f;
    }

    public void openCustomInventoryScreen(Player pPlayer) {
    }

    public boolean canBreed() {
        return false;
    }

    protected boolean canParent() {
        return false;
    }

    public boolean shouldDropExperience() {
        return false;
    }

    public boolean mnaCanBreatheUnderwater() {
        return true;
    }

    public boolean canStandOnFluid(FluidState p_204042_) {
        return p_204042_.is((Fluid)Fluids.WATER) && this.getForward().y > (double)-0.2f && !this.isUnderWater();
    }

    protected float getWaterSlowDown() {
        return 1.0f;
    }

    public boolean canHoldItem(ItemStack stack) {
        return false;
    }

    public boolean canMate(Animal otherAnimal) {
        return false;
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player == SummonUtils.getSummoner((LivingEntity)this)) {
            this.doPlayerRide(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public boolean causeFallDamage(float p_149499_, float p_149500_, DamageSource p_149501_) {
        return false;
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
    }

    protected void playJumpSound() {
    }
}


