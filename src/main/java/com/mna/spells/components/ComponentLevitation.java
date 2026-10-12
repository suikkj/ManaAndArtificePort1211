/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.blocks.BlockInit;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.items.relic.CrystallizedWind;
import com.mna.spells.components.PotionEffectComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ComponentLevitation
extends PotionEffectComponent {
    List<SpellReagent> permanenceReagents;

    public ComponentLevitation(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.LEVITATION, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 10.0f));
        this.addReagent(new ItemStack((ItemLike)Items.FEATHER), false, false, true, Factions.FEY);
        this.addPermanencyReagent(new ItemStack((ItemLike)BlockInit.TARMA_ROOT.get()), Factions.FEY);
        this.makePermanentFor(Factions.FEY);
    }

    @Override
    protected boolean checkPermanencyReagents(SpellSource source, SpellTarget target, SpellContext context) {
        if (source.isPlayerCaster() && source.getCaster() == target.getEntity() && CrystallizedWind.FindOn(source.getCaster())) {
            return true;
        }
        return super.checkPermanencyReagents(source, target, context);
    }

    @Override
    protected List<SpellReagent> getPermanencyReagents(Player caster, InteractionHand hand) {
        if (CrystallizedWind.FindOn((LivingEntity)caster)) {
            return new ArrayList<SpellReagent>();
        }
        return super.getPermanencyReagents(caster, hand);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.FRIENDLY;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }
}

