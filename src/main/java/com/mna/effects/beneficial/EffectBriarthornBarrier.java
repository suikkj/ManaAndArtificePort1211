/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package com.mna.effects.beneficial;

import com.mna.capabilities.entity.MAPFX;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class EffectBriarthornBarrier
extends EffectWithCustomClientParticles
implements INoCreeperLingering {
    public EffectBriarthornBarrier() {
        super(MobEffectCategory.BENEFICIAL, 0, MAPFX.Flag.BRIARTHORN_BARRIER);
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.parse("24602b52-344f-4f69-b316-85dd7a65b8bf"), 4.0, AttributeModifier.Operation.ADD_VALUE);
    }
}

