 
package com.mna.effects.neutral;

import com.mna.effects.EffectInit;
import com.mna.effects.neutral.SimpleNeutralEffect;
import java.util.Map;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.resources.ResourceLocation;

public class EffectGrow
extends SimpleNeutralEffect implements com.mna.api.effects.IMNAEffectHook {
    public EffectGrow() {
        super(15096587);
        this.addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.parse("264a6b95-63ed-47f2-b7cd-47f13b2d1e53"), 0.05f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.parse("ac600a2b-ec4c-49cc-8a85-3331f5799227"), 0.0069375, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.parse("a9a77b3e-ec50-4813-be2a-204ace0f2fec"), 0.111f, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK, ResourceLocation.parse("189f5b92-0bdf-4b63-bc08-a50e85f24c57"), 0.111f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, ResourceLocation.parse("b37cdf69-27ea-488a-b012-ef21359fcc90"), 0.222f, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, ResourceLocation.parse("29721638-2e59-479f-b9f6-d79665240142"), 0.222f, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.STEP_HEIGHT, ResourceLocation.parse("60288c04-9c19-4fd7-802f-b70cb55ed220"), 0.111f, AttributeModifier.Operation.ADD_VALUE);
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        AttributeMap pAttributeMap = pLivingEntity.getAttributes();
            MobEffectInstance mobEffectInstance = pLivingEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()));
            PacketDistributor.TRACKING_ENTITY.with(() -> pLivingEntity).send((Packet)new ClientboundUpdateMobEffectPacket(pLivingEntity.getId(), mobEffectInstance, false));
            float healthPct = pLivingEntity.getHealth() / pLivingEntity.getMaxHealth();
            boolean modifierAdded = false;
            for (Map.Entry entry : this.getAttributeModifiers().entrySet()) {
                AttributeInstance attributeinstance = pAttributeMap.getInstance((net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>)entry.getKey());
                if (attributeinstance != null) continue;
                modifierAdded = true;
            }        if (modifierAdded) {
                pLivingEntity.setHealth(pLivingEntity.getMaxHealth() * healthPct);
            }
    
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pAttributeMap, pAmplifier);
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            PacketDistributor.TRACKING_ENTITY.with(() -> pLivingEntity).send((Packet)new ClientboundRemoveMobEffectPacket(pLivingEntity.getId(), EffectInit.ENLARGE));
            float healthPct = pLivingEntity.getHealth() / pLivingEntity.getMaxHealth();        pLivingEntity.setHealth(pLivingEntity.getMaxHealth() * healthPct);
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }
}


