/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package com.mna.effects.beneficial;

import com.mna.effects.beneficial.SimpleBeneficialEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class EffectBulwark
extends SimpleBeneficialEffect {
    public EffectBulwark() {
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.parse("72a01976-f534-43f6-b664-28d8fd4ba8bb"), 4.0, AttributeModifier.Operation.ADD_VALUE);
    }
}

