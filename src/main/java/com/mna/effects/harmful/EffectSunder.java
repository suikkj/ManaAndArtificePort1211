/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package com.mna.effects.harmful;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class EffectSunder
extends MobEffect
implements INoCreeperLingering {
    public EffectSunder() {
        super(MobEffectCategory.HARMFUL, 0);
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.parse("e1a36b85-b471-4a65-b01b-44dc13584c6f"), -1.0, AttributeModifier.Operation.ADD_VALUE);
    }

    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return -pAmplifier - 1;
    }
}

