 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.blocks.BlockInit;
import com.mna.effects.EffectInit;
import com.mna.spells.components.PotionEffectComponent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ComponentGrow
extends PotionEffectComponent {
    private static final UUID _ara = UUID.fromString("c10026c1-3b38-401a-8e4a-590cba8037b4");

    public ComponentGrow(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.ENLARGE, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 5.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 3.0f, 1.0f, 9.0f, 1.0f, 5.0f));
        this.addPermanencyReagent(new ItemStack((ItemLike)BlockInit.DESERT_NOVA.get()), new IFaction[0]);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        UUID profileID;
        boolean isAranai;
        Player player;
        if (source.isPlayerCaster() && source.getCaster() == target.getEntity() && (player = source.getPlayer()).getGameProfile() != null && player.getGameProfile().getId() != null && (isAranai = (profileID = player.getGameProfile().getId()).equals(_ara))) {
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()))) {
                player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()));
                return ComponentApplicationResult.SUCCESS;
            }
            context.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SFX.Entity.Aranai.GRUMBLE, SoundSource.PLAYERS, 1.0f, 1.0f);
            modificationData.setValue(Attribute.DURATION, -1.0f);
        }
        return super.ApplyEffect(source, target, modificationData, context);
    }

    @Override
    protected List<SpellReagent> getPermanencyReagents(Player caster, InteractionHand hand) {
        if (caster == null) {
            return null;
        }
        UUID profileID = caster.getGameProfile().getId();
        boolean isAranai = profileID.equals(_ara);
        if (isAranai) {
            return null;
        }
        return super.getPermanencyReagents(caster, hand);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public float initialComplexity() {
        return 40.0f;
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

