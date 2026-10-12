 
package com.mna.spells.components;

import com.mna.api.faction.IFaction;
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
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.spells.SpellCaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public abstract class PotionEffectComponent
extends SpellEffect {
    protected final MobEffect effect;
    protected final RegistryObject<? extends MobEffect> registry_effect;
    protected final boolean modifiesMagnitude;
    protected final boolean modifiesDuration;
    private ArrayList<SpellReagent> permanenceReagents;
    private List<IFaction> permanentFor;

    public PotionEffectComponent(ResourceLocation guiIcon, MobEffect effect, AttributeValuePair ... attributeValuePairs) {
        super(guiIcon, attributeValuePairs);
        this.effect = effect;
        this.registry_effect = null;
        List<AttributeValuePair> pairs = Arrays.asList(attributeValuePairs);
        this.modifiesDuration = pairs.stream().anyMatch(p -> p.getAttribute() == Attribute.DURATION);
        this.modifiesMagnitude = pairs.stream().anyMatch(p -> p.getAttribute() == Attribute.MAGNITUDE || p.getAttribute() == Attribute.LESSER_MAGNITUDE);
    }

    @Override
    public void addReagentTooltip(Player player, @Nullable InteractionHand hand, List<Component> tooltip, SpellReagent reagent) {
        List<SpellReagent> permanencyReagents = this.getPermanencyReagents(player, hand);
        if (permanencyReagents != null && permanencyReagents.contains(reagent)) {
            MutableComponent c = Component.literal((String)String.format("%d x ", reagent.getReagentStack().getCount())).append(reagent.getReagentStack().getHoverName()).append((Component)Component.literal((String)" (")).append((Component)Component.translatable((String)"item.mna.spell.tooltip.permanent"));
            if (reagent.getOptional()) {
                c.append((Component)Component.literal((String)", "));
                c.append((Component)Component.translatable((String)"item.mna.spell.tooltip.optional"));
            }
            c.append((Component)Component.literal((String)")"));
            tooltip.add((Component)c);
        } else {
            super.addReagentTooltip(player, hand, tooltip, reagent);
        }
    }

    public PotionEffectComponent(ResourceLocation guiIcon, RegistryObject<? extends MobEffect> effect, AttributeValuePair ... attributeValuePairs) {
        super(guiIcon, attributeValuePairs);
        this.effect = null;
        this.registry_effect = effect;
        List<AttributeValuePair> pairs = Arrays.asList(attributeValuePairs);
        this.modifiesDuration = pairs.stream().anyMatch(p -> p.getAttribute() == Attribute.DURATION);
        this.modifiesMagnitude = pairs.stream().anyMatch(p -> p.getAttribute() == Attribute.MAGNITUDE || p.getAttribute() == Attribute.LESSER_MAGNITUDE);
    }

    protected boolean modifiesDuration() {
        return this.modifiesDuration;
    }

    protected boolean modifiesMagnitude() {
        return this.modifiesMagnitude;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isLivingEntity()) {
            int amp;
            int duration;
            boolean permanent = this.checkPermanencyReagents(source, target, context);
            int n = permanent ? -1 : (duration = this.modifiesDuration() ? (int)(modificationData.getValue(Attribute.DURATION) * 20.0f) : 1);
            if (duration < 0) {
                duration = -1;
            }
            int n2 = amp = this.modifiesMagnitude() ? this.getPotionAmplitude(source, target, modificationData, context) : 0;
            if (this.applicationPredicate(target.getLivingEntity())) {
                if (source.getCaster() == target.getEntity() && target.getLivingEntity().hasEffect(this.getEffect()) && target.getLivingEntity().getEffect(this.getEffect()).isInfiniteDuration()) {
                    target.getLivingEntity().removeEffect(this.getEffect());
                } else {
                    if (permanent) {
                        List<SpellReagent> permanentReagents = this.getPermanencyReagents(source.getPlayer(), source.getHand());
                        if (!SpellCaster.consumeReagents(source.getPlayer(), source.getHand(), permanentReagents)) {
                            return ComponentApplicationResult.FAIL;
                        }
                    }
                    target.getLivingEntity().addEffect(new MobEffectInstance(this.getEffect(), duration, amp, false, false));
                }
                if (this.getEffect().getCategory() == MobEffectCategory.HARMFUL && source.hasCasterReference()) {
                    target.getLivingEntity().setLastHurtByMob(source.getCaster());
                }
                return ComponentApplicationResult.SUCCESS;
            }
            if (source.isPlayerCaster()) {
                source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/potion_effect_component.cannot_apply", (Object[])new Object[]{Component.translatable((String)target.getLivingEntity().getType().getDescriptionId()).getString()}));
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    protected boolean checkPermanencyReagents(SpellSource source, SpellTarget target, SpellContext context) {
        if (source.isPlayerCaster() && source.getCaster() == target.getEntity()) {
            List<SpellReagent> permanentReagents;
            if (this.getPermanentFor() != null) {
                Player player = source.getPlayer();
                MutableBoolean factionMatch = new MutableBoolean(false);
                MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                    if (p.getAlliedFaction() != null) {
                        ArrayList<IFaction> permanentForCopy = new ArrayList<IFaction>(this.getPermanentFor());
                        permanentForCopy.retainAll(p.getAlliedFaction().getAlliedFactions());
                        factionMatch.setValue(permanentForCopy.size() > 0);
                    }
                });
                if (factionMatch.booleanValue()) {
                    return true;
                }
            }
            if ((permanentReagents = this.getPermanencyReagents(source.getPlayer(), source.getHand())) != null && permanentReagents.size() > 0) {
                for (SpellReagent reagent : permanentReagents) {
                    if (!context.isReagentMissing(reagent.getReagentStack().getItem())) continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    protected int getPotionAmplitude(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (modificationData.getContainedAttributes().contains(Attribute.LESSER_MAGNITUDE)) {
            return (int)modificationData.getValue(Attribute.LESSER_MAGNITUDE) - 1;
        }
        return (int)modificationData.getValue(Attribute.MAGNITUDE) - 1;
    }

    private MobEffect getEffect() {
        return this.effect != null ? this.effect : (MobEffect)this.registry_effect.get();
    }

    protected boolean applicationPredicate(LivingEntity target) {
        return true;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        switch (this.getEffect().getCategory()) {
            case BENEFICIAL: {
                return SpellPartTags.FRIENDLY;
            }
            case HARMFUL: {
                return SpellPartTags.HARMFUL;
            }
        }
        return SpellPartTags.NEUTRAL;
    }

    @Override
    public final List<SpellReagent> getRequiredReagents(Player caster, @Nullable InteractionHand hand) {
        List<SpellReagent> permanencyReagents;
        ArrayList<SpellReagent> reagents = new ArrayList<SpellReagent>();
        List<SpellReagent> baselineReagents = super.getRequiredReagents(caster, hand);
        if (baselineReagents != null) {
            reagents.addAll(baselineReagents);
        }
        if ((permanencyReagents = this.getPermanencyReagents(caster, hand)) != null) {
            reagents.addAll(permanencyReagents);
        }
        return reagents;
    }

    protected List<SpellReagent> getPermanencyReagents(Player caster, @Nullable InteractionHand hand) {
        if (this.permanenceReagents == null) {
            return new ArrayList<SpellReagent>();
        }
        return this.permanenceReagents.stream().filter(r -> !r.isIgnoredBy(caster)).toList();
    }

    protected final void addPermanencyReagent(ItemStack reagentStack, boolean matchNBT, boolean matchDurability, boolean consume, IFaction ... ignoredBy) {
        if (this.permanenceReagents == null) {
            this.permanenceReagents = new ArrayList();
        }
        this.permanenceReagents.add(new SpellReagent((SpellEffect)this, reagentStack, matchNBT, matchDurability, consume, true, ignoredBy));
    }

    protected final void addPermanencyReagent(ItemStack reagentStack, IFaction ... ignoredBy) {
        this.addPermanencyReagent(reagentStack, false, false, true, ignoredBy);
    }

    protected final void makePermanentFor(IFaction ... factions) {
        this.permanentFor = Arrays.asList(factions);
    }

    @Nullable
    private List<IFaction> getPermanentFor() {
        return this.permanentFor;
    }
}

