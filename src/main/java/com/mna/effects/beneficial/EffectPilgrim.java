/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.neoforged.neoforge.common.NeoForgeMod
 */
package com.mna.effects.beneficial;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.resources.ResourceLocation;

public class EffectPilgrim
extends MobEffect
implements INoCreeperLingering {
    public EffectPilgrim() {
        super(MobEffectCategory.BENEFICIAL, 5713430);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.parse("0fddd148-bfc1-41ba-95af-5e9dd9a25f97"), 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(NeoForgeMod.SWIM_SPEED, ResourceLocation.parse("8b243cec-6b01-4018-a6c6-dbfbf319012e"), 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.parse("845ba178-29e0-4845-84ec-69a11b705380"), 1.0, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(NeoForgeMod.STEP_HEIGHT_ADDITION, ResourceLocation.parse("845ba178-29e0-4845-84ec-69a11b705380"), 1.0, AttributeModifier.Operation.ADD_VALUE);
    }
}


