 
package com.mna.effects.beneficial;

import com.mna.effects.interfaces.INoCreeperLingering;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class EffectTelekinesis
extends MobEffect
implements INoCreeperLingering {
    public EffectTelekinesis() {
        super(MobEffectCategory.BENEFICIAL, 0);
        this.addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, ResourceLocation.parse("0c280955-9322-497b-8a97-4ea15fd021b2"), 0.0, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, ResourceLocation.parse("24b22d16-01b8-4e1f-b905-881ff598eb17"), 0.0, AttributeModifier.Operation.ADD_VALUE);
    }

    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return pAmplifier;
    }
}


