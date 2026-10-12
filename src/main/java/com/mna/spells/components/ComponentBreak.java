/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
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
import com.mna.items.artifice.FortuneRing;
import com.mna.items.artifice.SpellModifierRing;
import com.mna.tools.BlockUtils;
import com.mna.tools.EnchantmentUtils;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentBreak
extends SpellEffect {
    public ComponentBreak(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 15.0f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 2.5f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        ComponentApplicationResult result = ComponentApplicationResult.FAIL;
        if (target.isBlock()) {
            boolean silkTouch = source.isPlayerCaster() ? EnchantmentUtils.getSilkTouch(source.getPlayer()) || modificationData.getValue(Attribute.PRECISION) == 1.0f : false;
            int fortune_level = 0;
            if (!silkTouch) {
                fortune_level = source.isPlayerCaster() ? EnchantmentUtils.getFortuneLevel(source.getPlayer()) : 0;
            }
            int magnitude = (int)modificationData.getValue(Attribute.MAGNITUDE);
            Tier harvestTier = BlockUtils.tierFromHarvestLevel(magnitude - 1);
            Pair captureRedirect = source.isPlayerCaster() ? InventoryUtilities.getCaptureAndRedirect(source.getPlayer()) : new Pair(false, false);
            ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
            if (((Boolean)captureRedirect.getFirst()).booleanValue()) {
                drops.addAll(BlockUtils.destroyBlockCaptureDrops(source.getCaster(), (Level)context.getServerLevel(), target.getBlock(), silkTouch, fortune_level, harvestTier));
                if (source.isPlayerCaster()) {
                    InventoryUtilities.redirectCaptureOrDrop(source.getPlayer(), (Level)context.getServerLevel(), drops, (Boolean)captureRedirect.getSecond());
                }
                result = ComponentApplicationResult.SUCCESS;
            } else if (BlockUtils.destroyBlock(source.getCaster(), (Level)context.getServerLevel(), target.getBlock(), true, silkTouch, fortune_level, harvestTier)) {
                result = ComponentApplicationResult.SUCCESS;
            }
            if (result == ComponentApplicationResult.SUCCESS) {
                if (silkTouch) {
                    ((SpellModifierRing)ItemInit.SILK_TOUCH_RING.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, true);
                } else {
                    ((FortuneRing)ItemInit.FORTUNE_RING_GREATER.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, true);
                    ((FortuneRing)ItemInit.FORTUNE_RING.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, true);
                    ((FortuneRing)ItemInit.FORTUNE_RING_MINOR.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, true);
                }
            }
        }
        return result;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.EARTH;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 1) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 0.1f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3((double)(-v / 2.0f) + Math.random() * (double)v, Math.random() * (double)v, (double)(-v / 2.0f) + Math.random() * (double)v);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 1.0f;
    }

    @Override
    public boolean targetsEntities() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}

