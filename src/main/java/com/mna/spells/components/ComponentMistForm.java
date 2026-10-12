 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.items.armor.BoneArmorItem;
import com.mna.spells.components.PotionEffectComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class ComponentMistForm
extends PotionEffectComponent {
    public ComponentMistForm(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.MIST_FORM, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 5.0f));
        this.addReagent(new ItemStack((ItemLike)Items.PHANTOM_MEMBRANE), Factions.UNDEAD);
    }

    @Override
    protected boolean checkPermanencyReagents(SpellSource source, SpellTarget target, SpellContext context) {
        if (source.isPlayerCaster() && target.getEntity() == source.getCaster() && this.hasUndeadSetBonus(source.getPlayer())) {
            return true;
        }
        return super.checkPermanencyReagents(source, target, context);
    }

    @Override
    protected List<SpellReagent> getPermanencyReagents(Player caster, InteractionHand hand) {
        if (this.hasUndeadSetBonus(caster)) {
            return new ArrayList<SpellReagent>();
        }
        return super.getPermanencyReagents(caster, hand);
    }

    private boolean hasUndeadSetBonus(Player caster) {
        if (caster == null) {
            return false;
        }
        MutableBoolean setBonusPermanent = new MutableBoolean(false);
        MNACap.get(caster, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getAlliedFaction() == Factions.UNDEAD && ((BoneArmorItem)ItemInit.BONE_ARMOR__CHEST.get()).isSetEquipped((LivingEntity)caster)) {
                setBonusPermanent.setTrue();
            }
        });
        return setBonusPermanent.booleanValue();
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
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
    public float initialComplexity() {
        return 75.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.WATER, Affinity.WIND, Affinity.ICE);
    }
}

