/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.bus.api.Event
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.Event;

public class ComponentActivate
extends SpellEffect {
    public ComponentActivate(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock() && source.isPlayerCaster()) {
            BlockHitResult result = new BlockHitResult(target.getPosition(), target.getBlockFace(this), target.getBlock(), false);
            BlockState state = context.getServerLevel().getBlockState(target.getBlock());
            PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(source.getPlayer(), source.getHand(), target.getBlock(), result);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return ComponentApplicationResult.FAIL;
            }
            if (state.useItemOn(source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND), (Level)context.getServerLevel(), source.getPlayer(), InteractionHand.MAIN_HAND, result).consumesAction()) {
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Buff.ARCANE;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ARCANE;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 1) {
            return;
        }
        float particle_spread = 0.2f;
        float v = 0.1f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 position = new Vec3(impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0));
            Vec3 velocity = new Vec3(position.x - impact_position.x, position.y - impact_position.y, position.z - impact_position.z).normalize().scale((double)v);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())), (Entity)caster), position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public boolean targetsEntities() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}


