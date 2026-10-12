/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
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
import com.mna.config.GeneralConfig;
import com.mna.factions.Factions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ComponentBackdraft
extends SpellEffect {
    public ComponentBackdraft(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RANGE, 3.0f, 1.0f, 6.0f, 0.5f, 3.5f), new AttributeValuePair(Attribute.DAMAGE, 0.5f, 0.5f, 2.0f, 0.25f, 2.0f));
        this.addReagent(new ItemStack((ItemLike)Items.FIRE_CHARGE), new IFaction[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!target.isLivingEntity() || !target.getLivingEntity().isOnFire()) {
            return ComponentApplicationResult.FAIL;
        }
        int fireTicks = target.getLivingEntity().getRemainingFireTicks();
        int damageSteps = fireTicks / 20;
        target.getLivingEntity().hurt(target.getLivingEntity().damageSources().onFire(), (float)damageSteps * modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier());
        target.getLivingEntity().clearFire();
        int targetFireTicks = fireTicks / 2;
        context.getServerLevel().getEntitiesOfClass(LivingEntity.class, target.getLivingEntity().getBoundingBox().inflate((double)modificationData.getValue(Attribute.RANGE))).stream().forEach(e -> {
            if (e == source.getCaster() || e == target.getLivingEntity()) {
                return;
            }
            if (e.getRemainingFireTicks() < targetFireTicks) {
                e.setRemainingFireTicks(targetFireTicks);
            }
        });
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.FIRE;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
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
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

