 
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.resources.ResourceLocation;

public class EffectShrink
extends SimpleNeutralEffect implements com.mna.api.effects.IMNAEffectHook {
    public EffectShrink() {
        super(4199772);
        this.addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.parse("7a3200de-96c6-41c6-80a4-c9a5493b1bed"), -0.05f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        AttributeMap pAttributeMap = pLivingEntity.getAttributes();
            MobEffectInstance mobEffectInstance = pLivingEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()));
            PacketDistributor.sendToPlayersTrackingEntity(pLivingEntity, new ClientboundUpdateMobEffectPacket(pLivingEntity.getId(), mobEffectInstance, false));
            float healthPct = pLivingEntity.getHealth() / pLivingEntity.getMaxHealth();
            boolean modifierAdded = false;
            for (Map.Entry entry : java.util.Collections.<Map.Entry>emptyList()) {
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
            PacketDistributor.sendToPlayersTrackingEntity(pLivingEntity, new ClientboundRemoveMobEffectPacket(pLivingEntity.getId(), EffectInit.REDUCE));        float healthPct = pLivingEntity.getHealth() / pLivingEntity.getMaxHealth();
            pLivingEntity.setHealth(pLivingEntity.getMaxHealth() * healthPct);
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }
}


