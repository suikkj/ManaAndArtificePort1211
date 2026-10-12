/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package com.mna.effects.beneficial;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class EffectBurningRage
extends MobEffect
implements INoCreeperLingering {
    public EffectBurningRage() {
        super(MobEffectCategory.BENEFICIAL, 0);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.parse("c085fd06-e784-11ed-a05b-0242ac120003"), 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.parse("dedc59ee-e784-11ed-a05b-0242ac120003"), 6.0, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.parse("ed360102-e784-11ed-a05b-0242ac120003"), 0.5, AttributeModifier.Operation.ADD_VALUE);
    }
}

