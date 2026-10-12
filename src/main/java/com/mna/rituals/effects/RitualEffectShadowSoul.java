/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.items.IPhylacteryItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.sound.SFX;
import com.mna.entities.utility.PresentItem;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.CrystalPhylactery;
import com.mna.items.sorcery.EntityCrystal;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RitualEffectShadowSoul
extends RitualEffect {
    public RitualEffectShadowSoul(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        NonNullList<RitualBlockPos> positions = context.getAllPositions();
        for (RitualBlockPos pos : positions) {
            if (pos == null) continue;
            BlockPos cur = pos.getBlockPos();
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SOUL.get())).setGravity(0.0f).setPhysics(true), (double)cur.getX() + Math.random(), (double)cur.getY() + 0.1, (double)cur.getZ() + Math.random(), 0.0, 0.05, 0.0);
        }
        return true;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.SHADOW_COPY;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        ItemStack phylacteryStack = ItemStack.EMPTY;
        ItemStack crystalStack = ItemStack.EMPTY;
        List<ItemStack> collected = context.getCollectedReagents();
        for (int i = 0; i < collected.size(); ++i) {
            ItemStack stack = collected.get(i);
            if (stack.getItem() instanceof IPhylacteryItem) {
                phylacteryStack = stack;
                continue;
            }
            if (stack.getItem() != ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get() || EntityCrystal.getEntityType(stack) == null) continue;
            crystalStack = stack;
        }
        if (phylacteryStack.isEmpty() || crystalStack.isEmpty()) {
            this.ejectItems(context);
            return false;
        }
        try {
            EntityType<?> crystalType = EntityCrystal.getEntityType(crystalStack);
            EntityType<? extends Mob> existing = ((IPhylacteryItem)phylacteryStack.getItem()).getContainedEntity(phylacteryStack);
            if (existing != null && existing != crystalType) {
                this.ejectItems(context);
                return false;
            }
            if (existing == null) {
                CrystalPhylactery.setEntityType(phylacteryStack, crystalType, context.getLevel());
            }
            ((IPhylacteryItem)phylacteryStack.getItem()).fill(phylacteryStack, crystalType, (float)((IPhylacteryItem)phylacteryStack.getItem()).getMaximumFill() * 0.25f, context.getLevel());
            Vec3 center = Vec3.atCenterOf((Vec3i)context.getCenter());
            PresentItem epi = new PresentItem(context.getLevel(), center.x, center.y, center.z, phylacteryStack);
            context.getLevel().addFreshEntity((Entity)epi);
            Entity e = EntityCrystal.restoreEntity(context.getLevel(), crystalStack);
            e.setPos(Vec3.atCenterOf((Vec3i)context.getCenter()));
            context.getLevel().addFreshEntity(e);
        }
        catch (Throwable t) {
            this.ejectItems(context);
        }
        return false;
    }

    private void ejectItems(IRitualContext context) {
        Vec3 center = Vec3.atCenterOf((Vec3i)context.getCenter());
        context.getCollectedReagents().forEach(is -> {
            ItemEntity ie = new ItemEntity(context.getLevel(), center.x, center.y, center.z, is, (double)-0.2f + Math.random() * (double)0.4f, (double)0.2f, (double)-0.2f + Math.random() * (double)0.4f);
            context.getLevel().addFreshEntity((Entity)ie);
        });
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}

