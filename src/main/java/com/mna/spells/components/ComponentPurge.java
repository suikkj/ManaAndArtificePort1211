 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.config.GeneralConfig;
import com.mna.factions.Factions;
import java.util.Collection;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class ComponentPurge
extends SpellEffect {
    public ComponentPurge(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DAMAGE, 0.0f, 0.0f, 5.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 1.0f, 1.0f, 5.0f, 1.0f, 15.0f), new AttributeValuePair(Attribute.MAGNITUDE, 0.0f, 0.0f, 5.0f, 1.0f, 15.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!target.isLivingEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        int removeUpto = (int)modificationData.getValue(Attribute.LESSER_MAGNITUDE) + (int)modificationData.getValue(Attribute.MAGNITUDE);
        Collection activeEffects = target.getLivingEntity().getActiveEffects().stream().filter(e -> !e.isAmbient() && e.getDuration() >= 220 && e.getEffect().value().isBeneficial() && e.getAmplifier() <= removeUpto - 1).collect(Collectors.toList());
        MutableInt removedEffects = new MutableInt(0);
        activeEffects.forEach(ae -> {
            if (target.getLivingEntity().removeEffect(((net.minecraft.world.effect.MobEffectInstance)ae).getEffect().value())) {
                removedEffects.increment();
            }
        });
        if (removedEffects.getValue() > 0) {
            float damage = (float)removedEffects.getValue().intValue() * modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier();
            target.getLivingEntity().hurt(target.getLivingEntity().damageSources().magic(), damage);
            if (target.getLivingEntity() instanceof Player) {
                float pct = 0.05f + (0.025f * (float)removedEffects.getValue().intValue() - 1.0f);
                MNACap.get((Player)target.getLivingEntity(), PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().consume(target.getLivingEntity(), m.getCastingResource().getMaxAmount() * pct));
            }
        }
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public float initialComplexity() {
        return 40.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

