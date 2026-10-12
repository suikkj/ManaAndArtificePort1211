/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.NeoForge
 *  net.minecraftforge.event.entity.EntityTeleportEvent
 *  net.minecraftforge.eventbus.api.Event
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.items.ItemInit;
import com.mna.items.artifice.SpellModifierRing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.bus.api.Event;

public class ComponentTransplace
extends SpellEffect {
    public ComponentTransplace(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RANGE, 8.0f, 8.0f, 32.0f, 8.0f, 2.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.getCaster() == null) {
            return ComponentApplicationResult.FAIL;
        }
        if (target.isLivingEntity()) {
            double range = modificationData.getValue(Attribute.RANGE);
            if (((SpellModifierRing)ItemInit.BLINK_PRECISION_RING.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, true)) {
                range *= 2.0;
            }
            if (source.getCaster().level().dimension().equals(target.getLivingEntity().level().dimension()) && (double)source.getCaster().distanceTo((Entity)target.getLivingEntity()) <= range) {
                EntityTeleportEvent tpEventTarget;
                Vec3 casterPos = source.getOrigin();
                Vec3 targetPos = target.getLivingEntity().position();
                EntityTeleportEvent tpEventCaster = new EntityTeleportEvent((Entity)source.getCaster(), targetPos.x, targetPos.y, targetPos.z);
                if (!NeoForge.EVENT_BUS.post((Event)tpEventCaster)) {
                    source.getCaster().teleportTo(targetPos.x, targetPos.y, targetPos.z);
                }
                if (!NeoForge.EVENT_BUS.post((Event)(tpEventTarget = new EntityTeleportEvent((Entity)target.getLivingEntity(), casterPos.x, casterPos.y, casterPos.z)))) {
                    target.getLivingEntity().teleportTo(casterPos.x, casterPos.y, casterPos.z);
                }
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.ENDER;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age < 5) {
            Vec3 rotationOffset = new Vec3(1.5, 0.0, 0.0);
            BlockPos bp = BlockPos.containing((Position)impact_position).offset(0, -1, 0);
            for (int angle = 0; angle < 360; angle += 30) {
                Vec3 point = rotationOffset.yRot((float)((double)angle * Math.PI / 180.0));
                world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)caster), (double)((float)bp.getX() + 0.5f) + point.x, (double)bp.getY(), (double)((float)bp.getZ() + 0.5f) + point.z, (double)((float)bp.getX() + 0.5f), (double)(bp.getY() + 2), (double)((float)bp.getZ() + 0.5f));
            }
        }
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.NEUTRAL;
    }
}

