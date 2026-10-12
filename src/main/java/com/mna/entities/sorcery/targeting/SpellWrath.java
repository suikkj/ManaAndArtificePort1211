/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.sorcery.targeting;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.base.ChanneledSpellEntity;
import com.mna.entities.sorcery.targeting.Smite;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpellWrath
extends ChanneledSpellEntity {
    public SpellWrath(EntityType<? extends SpellWrath> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public SpellWrath(LivingEntity caster, ISpellDefinition spell, Level world) {
        super((EntityType<? extends ChanneledSpellEntity>)((EntityType)EntityInit.SPELL_WRATH.get()), caster, spell, world);
        this.setPos(caster.getX(), caster.getY() + (double)caster.getEyeHeight(), caster.getZ());
        this.setRot(this.getCaster().getYRot(), this.getCaster().getXRot());
        float x = -Mth.sin((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
        float y = -Mth.sin((float)(this.getXRot() * ((float)Math.PI / 180)));
        float z = Mth.cos((float)(this.getYRot() * ((float)Math.PI / 180))) * Mth.cos((float)(this.getXRot() * ((float)Math.PI / 180)));
        Vec3 motion = new Vec3((double)x, (double)y, (double)z).normalize().scale((double)this.getShapeAttribute(Attribute.SPEED));
        this.setDeltaMovement(motion);
    }

    @Override
    public void tick() {
        if (this.getCaster() != null && this.getDeltaMovement().x == 0.0 && this.getDeltaMovement().y == 0.0 && this.getDeltaMovement().z == 0.0) {
            this.setPos(this.getCaster().getX(), this.getCaster().isShiftKeyDown() ? this.getCaster().getY() - 1.0 : this.getCaster().getY(), this.getCaster().getZ());
        } else {
            this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
        }
        super.tick();
    }

    @Override
    protected void applyEffect(ItemStack stack, SpellRecipe recipe, LivingEntity caster, ServerLevel world) {
        if (caster == null) {
            return;
        }
        float radius_h = this.getShapeAttributeByAge(Attribute.WIDTH);
        Vec3 position = this.position().add((double)(-radius_h) + Math.random() * (double)(2.0f * radius_h), 0.0, (double)(-radius_h) + Math.random() * (double)(2.0f * radius_h));
        int count = 0;
        BlockPos targetPos = BlockPos.containing((Position)position);
        while (count < 15) {
            if (!world.isEmptyBlock(targetPos)) {
                return;
            }
            ++count;
            targetPos = targetPos.above();
        }
        position = position.add(0.0, 15.0, 0.0);
        CompoundTag recipeData = new CompoundTag();
        recipe.writeToNBT(recipeData);
        Smite projectile = new Smite((Level)world, position, recipeData, caster);
        world.addFreshEntity((Entity)projectile);
    }

    @Override
    protected void spawnAirParticles(SpellRecipe recipe) {
    }

    @Override
    protected void spawnEarthParticles(SpellRecipe recipe) {
    }

    @Override
    protected void spawnFireParticles(SpellRecipe recipe, boolean hellfire, boolean lightning) {
    }

    @Override
    protected void spawnWaterParticles(SpellRecipe recipe, boolean ice) {
    }

    @Override
    protected void spawnEnderParticles(SpellRecipe recipe, boolean blood) {
    }

    @Override
    protected void spawnArcaneParticles(SpellRecipe recipe) {
    }

    @Override
    protected int getApplicationRate() {
        return 2;
    }
}

