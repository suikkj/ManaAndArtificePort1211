 
package com.mna.items.constructs.parts.arms;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ConstructPartGrabberArmLeft
extends ItemConstructPart {
    private Multimap<Attribute, AttributeModifier> defaultModifiers;
    private int reachIncrease;

    public ConstructPartGrabberArmLeft(ConstructMaterial material, int reachIncrease) {
        super(material, ConstructSlot.LEFT_ARM, 1);
        this.reachIncrease = reachIncrease;
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.MELEE_ATTACK, ConstructCapability.PLANT, ConstructCapability.CARRY, ConstructCapability.CRAFT};
    }

    @Override
    public float getAttackDamage() {
        return 1.0f;
    }

    @Override
    public int getAttackSpeedModifier() {
        return 5;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (this.defaultModifiers == null) {
            ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
            builder.put((Attributes.BLOCK_INTERACTION_RANGE), new AttributeModifier(ManaAndArtificeMod.REACH_DISTANCE_ID, (double)this.reachIncrease, AttributeModifier.Operation.ADD_VALUE));
            this.defaultModifiers = builder.build();
        }
        return this.defaultModifiers;
    }
}


