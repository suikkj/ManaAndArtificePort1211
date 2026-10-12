 
package com.mna.spells.components.boss;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.effects.EffectInit;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ComponentHeatwave
extends PotionEffectComponent {
    public ComponentHeatwave(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.HEATWAVE, new AttributeValuePair(Attribute.MAGNITUDE, 0.0f, 0.0f, 5.0f, 1.0f, 20.0f), new AttributeValuePair(Attribute.DURATION, 10.0f, 5.0f, 60.0f, 5.0f, 20.0f), new AttributeValuePair(Attribute.RADIUS, 3.0f, 1.0f, 15.0f, 1.0f, 5.0f), new AttributeValuePair(Attribute.DAMAGE, 5.0f, 1.0f, 20.0f, 1.0f, 5.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isLivingEntity() && ((LivingEntity)target.getEntity()).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHILL.get()))) {
            ((LivingEntity)target.getEntity()).removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHILL.get()));
            context.getServerLevel().getEntities((Entity)source.getCaster(), target.getEntity().getBoundingBox().inflate((double)modificationData.getValue(Attribute.RADIUS)), e -> e instanceof LivingEntity && !this.isTargetFriendlyToCaster(source, (Entity)e)).stream().map(e -> (LivingEntity)e).forEach(e -> e.hurt(e.damageSources().inFire(), modificationData.getValue(Attribute.DAMAGE)));
            return ComponentApplicationResult.SUCCESS;
        }
        return super.ApplyEffect(source, target, modificationData, context);
    }

    @Override
    public int requiredXPForRote() {
        return 999;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.FIRE;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public boolean canBeOnRandomStaff() {
        return false;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public boolean isCraftable(SpellCraftingContext context) {
        return false;
    }

    @Override
    public boolean isSilverSpell() {
        return true;
    }
}

