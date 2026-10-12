 
package com.mna.spells;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.MnaRegistry;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.entities.ISpellInteractibleEntity;
import com.mna.api.events.CalculatingManaCostEvent;
import com.mna.api.events.ComponentApplyingEvent;
import com.mna.api.events.SpellCooldownCalculatingEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.ISpellHelper;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.SpellCastingResultCode;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.base.SpellBlacklistResult;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedSpellEffect;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.entities.utility.SpellFX;
import com.mna.events.EventDispatcher;
import com.mna.factions.Factions;
import com.mna.interop.CuriosInterop;
import com.mna.inventory.InventoryRitualKit;
import com.mna.items.ItemInit;
import com.mna.items.artifice.charms.ContingencyCharm;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.items.sorcery.BookOfRote;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.NameProcessors;
import com.mna.spells.SpellAdjuster;
import com.mna.spells.components.ComponentTrueInvisibility;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.EntityUtil;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class SpellCaster
implements ISpellHelper {
    private static ArrayList<SpellAdjuster> _adjusters = new ArrayList();
    private static ArrayList<Item> _cooldownItems = new ArrayList();

    public static SpellCastingResult PlayerCast(ItemStack stack, Player caster, InteractionHand hand, Vec3 casterPosition, Vec3 casterLook, Level world, boolean consumeMana, boolean isCurioCast) {
        try {
            SpellCastingResult subCast;
            HashMap<IFaction, Float> ire;
            List missingRequired;
            IPlayerMagic magic = null;
            IPlayerProgression progression = null;
            if (caster.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SILENCE.get())) || caster.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()))) {
                return SpellCastingResultCode.SILENCED.createResult();
            }
            magic = (IPlayerMagic)caster.getData(PlayerMagicProvider.MAGIC.get());
            progression = (IPlayerProgression)caster.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (magic == null || progression == null) {
                return SpellCastingResultCode.CAPABILITY_MISSING.createResult();
            }
            if (!magic.isMagicUnlocked() && consumeMana) {
                if (!world.isClientSide) {
                    caster.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.no-magic"));
                }
                return SpellCastingResultCode.NOT_UNLOCKED_MAGIC.createResult();
            }
            SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            if (!recipe.isValid()) {
                return SpellCastingResultCode.INVALID_RECIPE.createResult();
            }
            if (consumeMana && !caster.isCreative() && recipe.getTier(world) > progression.getTier()) {
                if (!world.isClientSide) {
                    caster.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.tier-fail"));
                }
                return SpellCastingResultCode.INSUFFICIENT_TIER.createResult();
            }
            if (consumeMana && !caster.isCreative() && recipe.getComplexity() > (float)progression.getTierMaxComplexity()) {
                if (!world.isClientSide) {
                    caster.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.complexity-fail"));
                }
                return SpellCastingResultCode.TOO_COMPLEX.createResult();
            }
            HashMap<Item, Boolean> missing = SpellCaster.checkReagents(caster, hand, recipe);
            if (consumeMana && !caster.isCreative() && (missingRequired = missing.entrySet().stream().filter(e -> (Boolean)e.getValue() == false).collect(Collectors.toList())).size() > 0) {
                if (!world.isClientSide) {
                    if (missing.size() > 1) {
                        caster.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.reagents-missing.multi"));
                    } else {
                        caster.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.reagents-missing.single").append((Component)Component.translatable((String)((Item)((Map.Entry)missingRequired.get(0)).getKey()).getDescriptionId())));
                    }
                }
                return SpellCastingResultCode.MISSING_REAGENTS.createResult();
            }
            SpellCaster.applyAdjusters(stack, (LivingEntity)caster, hand, isCurioCast, recipe, SpellCastStage.CASTING);
            float manaCost = recipe.getManaCost();
            if (!caster.isCreative() && !magic.getCastingResource().hasEnough((LivingEntity)caster, manaCost) && consumeMana) {
                return SpellCastingResultCode.NOT_ENOUGH_MANA.createResult();
            }
            SpellContext context = new SpellContext(world, recipe);
            context.setMissingReagents(missing.keySet().stream().toList());
            if (!world.isClientSide && recipe.isMysterious()) {
                recipe.setMysterious(false);
                recipe.writeToNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            }
            if (consumeMana && (ire = recipe.getCastFactionIre()).size() > 0) {
                MNACap.get(caster, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                    IFaction alliedFaction = p.getAlliedFaction();
                    ire.forEach((faction, level) -> {
                        if (level.floatValue() > 0.0f && alliedFaction == null || !alliedFaction.isAlliedTo((IFaction)faction)) {
                            p.incrementFactionAggro((IFaction)faction, 0.0f, level.floatValue());
                        }
                    });
                });
            }
            if (!(subCast = SpellCaster.Affect(stack, recipe, world, new SpellSource((LivingEntity)caster, hand), null, context)).getCode().isConsideredSuccess()) {
                return subCast;
            }
            if (caster instanceof ServerPlayer) {
                CustomAdvancementTriggers.CAST_SPELL.trigger((ServerPlayer)caster, recipe);
            }
            if (consumeMana) {
                if (!recipe.canFactionCraft(progression)) {
                    recipe.usedByPlayer(caster);
                }
                if (((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).isChanneled()) {
                    magic.getCastingResource().consume((LivingEntity)caster, manaCost * 0.25f);
                } else {
                    magic.getCastingResource().consume((LivingEntity)caster, manaCost);
                    SpellCaster.AddAffinityAndMagicXP(recipe, caster);
                }
                if (caster instanceof ServerPlayer && magic.getCastingResource().getAmount() <= magic.getCastingResource().getMaxAmount() * 0.1f) {
                    ContingencyCharm.CheckAndConsumeCharmCharge((ServerPlayer)caster, ContingencyCharm.ContingencyEvent.LOW_CASTING_RESOURCE);
                }
                SpellCaster.consumeReagents(caster, hand, recipe, subCast);
            }
            return subCast;
        }
        catch (Throwable t) {
            try {
                caster.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.fatal_error"));
            }
            catch (Throwable subt) {
                ManaAndArtifice.LOGGER.error("Error notifying caster about spell cast failure:");
                ManaAndArtifice.LOGGER.error(subt);
            }
            ManaAndArtifice.LOGGER.error(t);
            return SpellCastingResultCode.EXCEPTION_THROWN.createResult();
        }
    }

    public static SpellCastingResult Affect(@Nullable ItemStack stack, ISpellDefinition recipe, Level world, SpellSource source) {
        return SpellCaster.Affect(stack, recipe, world, source, null, null);
    }

    public static SpellCastingResult Affect(@Nullable ItemStack stack, ISpellDefinition recipe, Level world, SpellSource source, SpellTarget targetHint) {
        return SpellCaster.Affect(stack, recipe, world, source, targetHint, null);
    }

    public static SpellCastingResult Affect(@Nullable ItemStack stack, ISpellDefinition recipe, Level world, SpellSource source, SpellTarget targetHint, SpellContext context) {
        try {
            net.minecraft.world.effect.MobEffectInstance efct = null;
            if (!recipe.isValid()) {
                return SpellCastingResultCode.INVALID_RECIPE.createResult();
            }
            Shape shape = recipe.getShape().getPart();
            if (shape == null || recipe.countComponents() == 0) {
                return SpellCastingResultCode.INVALID_RECIPE.createResult();
            }
            SpellBlacklistResult result = shape.canBeCastAt(world, source.getOrigin());
            if (result != SpellBlacklistResult.ALLOWED) {
                if (source.isPlayerCaster() && !world.isClientSide) {
                    source.getPlayer().sendSystemMessage((Component)Component.translatable((String)result.getMessageTranslationKey().toString()));
                }
                return SpellCastingResultCode.BLOCKED_BY_CONFIG.createResult();
            }
            for (int i = 0; i < recipe.countComponents(); ++i) {
                result = recipe.getComponent(i).getPart().canBeCastAt(world, source.getOrigin());
                if (result == SpellBlacklistResult.ALLOWED) continue;
                if (source.isPlayerCaster() && !world.isClientSide) {
                    source.getPlayer().sendSystemMessage((Component)Component.translatable((String)result.getMessageTranslationKey().toString()));
                }
                return SpellCastingResultCode.BLOCKED_BY_CONFIG.createResult();
            }
            if (!EventDispatcher.DispatchSpellCast(recipe, source, stack, context)) {
                return SpellCastingResultCode.CANCELED_BY_EVENT.createResult();
            }
            List<SpellTarget> targets = null;
            if (targetHint != null) {
                targets = shape.TargetNPCCast(source, world, recipe.getShape(), recipe, targetHint);
            }
            if (targetHint == null || targets == null || targets.size() == 0 || targets.get(0) == SpellTarget.NPC_CAST_ASSIST_NOT_IMPLEMENTED) {
                targets = shape.Target(source, world, recipe.getShape(), recipe);
            }
            if (targets == null || targets.size() == 0 || targets.get(0) == SpellTarget.NONE) {
                return SpellCastingResultCode.NO_TARGET.createResult();
            }
            if (!world.isClientSide && !((ServerLevel)world).getServer().isPvpAllowed() && recipe.isHarmful()) {
                MutableBoolean pvpdenied = new MutableBoolean(false);
                recipe.iterateComponents(c -> {
                    if (((SpellEffect)c.getPart()).getUseTag() == SpellPartTags.HARMFUL) {
                        pvpdenied.setTrue();
                    }
                });
                if (pvpdenied.booleanValue()) {
                    targets = targets.stream().filter(t -> !t.isEntity() || !(t.getEntity() instanceof Player) || t.getEntity() == source.getCaster()).collect(Collectors.toList());
                }
                if (targets.size() == 0) {
                    return SpellCastingResultCode.NO_TARGET.createResult();
                }
            }
            SoundEvent spellSound = SFX.Spell.Cast.ForAffinity(recipe.getHighestAffinity());
            if (stack != null) {
                spellSound = NameProcessors.checkAndOverrideSound(recipe, stack.getHoverName().getString(), spellSound);
            }
            if (spellSound != null && source.getCaster() != null) {
                float f;
                float pitch = (float)((double)0.9f + Math.random() * (double)0.1f);
                efct = source.getCaster().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()));
                if (efct != null) {
                    f = 0.1f * (float)efct.getAmplifier();
                    pitch += f;
                }
                if ((efct = source.getCaster().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()))) != null) {
                    f = 0.05f * (float)efct.getAmplifier();
                    pitch -= f;
                }
                world.playSound(null, (Entity)source.getCaster(), spellSound, SoundSource.PLAYERS, 0.5f, pitch);
            }
            if (!world.isClientSide) {
                com.mna.api.tools.MNACap.get(world.getChunkAt(BlockPos.containing(source.getOrigin())), com.mna.capabilities.CapabilitiesInit.CHUNK_MAGIC).ifPresent(cm -> cm.addResidualMagic(recipe.getManaCost()));
            }
            if (shape.spawnsTargetEntity()) {
                net.minecraft.world.entity.Entity efctEntity = source.getCaster();
                if (efctEntity instanceof Player) {
                    Player player = (Player)efctEntity;
                    EntityUtil.suppressInvisibilityEffects((LivingEntity)player);
                }
                HashMap<SpellEffect, ComponentApplicationResult> targetResult = new HashMap<SpellEffect, ComponentApplicationResult>();
                for (IModifiedSpellPart iModifiedSpellPart : recipe.getComponents()) {
                    targetResult.put((SpellEffect)iModifiedSpellPart.getPart(), ComponentApplicationResult.TARGET_ENTITY_SPAWNED);
                }
                if (shape.isChanneled()) {
                    return SpellCastingResultCode.CHANNEL.createResult(targetResult);
                }
                return SpellCastingResultCode.SUCCESS.createResult(targetResult);
            }
            if (targets.size() == 1) {
                boolean bl;
                SpellTarget target2 = targets.get(0);
                BlockState state = world.getBlockState(target2.getBlock());
                if (state.getBlock() instanceof ISpellInteractibleBlock && ((ISpellInteractibleBlock)state.getBlock()).onHitBySpell(world, target2.getBlock(), recipe)) {
                    return SpellCastingResultCode.SPELL_INTERACTIBLE_BLOCK_HIT.createResult();
                }
                if (target2.isEntity() && target2.getEntity() instanceof ISpellInteractibleEntity && (bl = ((ISpellInteractibleEntity)target2.getEntity()).onShapeTarget(recipe, source))) {
                    return SpellCastingResultCode.SPELL_INTERACTIBLE_ENTITY_HIT.createResult();
                }
            }
            HashMap<SpellEffect, ComponentApplicationResult> mergedComponentResults = new HashMap<SpellEffect, ComponentApplicationResult>();
            if (!world.isClientSide) {
                SpellContext iteratorContext = context == null ? new SpellContext(world, recipe) : context;
                targets.forEach(target -> {
                    LivingEntity patt19163$temp;
                    if (target.getEntity() != source.getCaster() && (patt19163$temp = source.getCaster()) instanceof Player) {
                        Player player = (Player)patt19163$temp;
                        ComponentTrueInvisibility.DisableForSeconds((LivingEntity)player, 5);
                    }
                    HashMap<SpellEffect, ComponentApplicationResult> componentResults = SpellCaster.ApplyComponents(recipe, source, target, iteratorContext);
                    SpellCaster.mergeComponentResults(mergedComponentResults, componentResults);
                    List<SpellEffect> appliedEffects = componentResults.entrySet().stream().map(e -> e.getValue() == ComponentApplicationResult.SUCCESS ? (SpellEffect)e.getKey() : null).filter(e -> e != null).collect(Collectors.toList());
                    if (appliedEffects.size() > 0) {
                        SpellCaster.spawnClientFX(world, target.getPosition(), Vec3.ZERO, source, appliedEffects);
                    }
                });
            }
            return mergedComponentResults.values().stream().anyMatch(c -> c == ComponentApplicationResult.SUCCESS || c == ComponentApplicationResult.DELAYED) ? SpellCastingResultCode.SUCCESS.createResult(mergedComponentResults) : SpellCastingResultCode.NO_TARGET.createResult(mergedComponentResults);
        }
        catch (Throwable t2) {
            try {
                if (source.getCaster() != null && source.getCaster() instanceof Player) {
                    ((Player)source.getCaster()).sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.fatal_error"));
                }
            }
            catch (Throwable subt) {
                ManaAndArtifice.LOGGER.error("Error notifying caster about spell cast failure:");
                ManaAndArtifice.LOGGER.error(subt);
            }
            ManaAndArtifice.LOGGER.error(t2);
            return SpellCastingResultCode.EXCEPTION_THROWN.createResult();
        }
    }

    public static void mergeComponentResults(HashMap<SpellEffect, ComponentApplicationResult> mainList, HashMap<SpellEffect, ComponentApplicationResult> toMerge) {
        toMerge.forEach((c, r) -> {
            if (mainList.containsKey(c)) {
                if (r.is_success) {
                    mainList.put((SpellEffect)c, (ComponentApplicationResult)(r));
                }
            } else {
                mainList.put((SpellEffect)c, (ComponentApplicationResult)(r));
            }
        });
    }

    public static HashMap<SpellEffect, ComponentApplicationResult> ApplyComponents(ISpellDefinition recipe, SpellSource source, SpellTarget target, SpellContext context) {
        List<SpellEffect> appliedEffects;
        HashMap<SpellEffect, ComponentApplicationResult> results = new HashMap<SpellEffect, ComponentApplicationResult>();
        if (!context.getServerLevel().getServer().isPvpAllowed() && recipe.isHarmful()) {
            MutableBoolean pvpdenied = new MutableBoolean(false);
            recipe.iterateComponents(c -> {
                if (((SpellEffect)c.getPart()).getUseTag() == SpellPartTags.HARMFUL) {
                    pvpdenied.setTrue();
                }
            });
            if (pvpdenied.booleanValue() && target.isEntity() && target.getEntity() instanceof Player && target.getEntity() != source.getCaster()) {
                recipe.iterateComponents(c -> results.put((SpellEffect)c.getPart(), ComponentApplicationResult.FAIL));
                return results;
            }
        }
        recipe.iterateComponents(c -> {
            int delayInSeconds = (int)(c.getValue(Attribute.DELAY) * 20.0f);
            if (NeoForge.EVENT_BUS.post(new ComponentApplyingEvent(source, context, target, (SpellEffect)c.getPart()))) {
                return;
            }
            if (target.isBlock() && ((SpellEffect)c.getPart()).targetsBlocks() && !context.hasBlockBeenAffected((SpellEffect)c.getPart(), target.getBlock())) {
                if (delayInSeconds == 0) {
                    ComponentApplicationResult cRes = null;
                    try {
                        cRes = ((SpellEffect)c.getPart()).ApplyEffect(source, target, (IModifiedSpellPart<SpellEffect>)c, context);
                    }
                    catch (Throwable t) {
                        ManaAndArtifice.LOGGER.error(t);
                        cRes = ComponentApplicationResult.FAIL;
                    }
                    results.put((SpellEffect)c.getPart(), cRes);
                    context.addAffectedBlock((SpellEffect)c.getPart(), target.getBlock());
                } else {
                    DelayedEventQueue.pushEvent((Level)context.getServerLevel(), new TimedDelayedSpellEffect("" + context.getServerLevel().getGameTime(), delayInSeconds, source, target, (IModifiedSpellPart<SpellEffect>)c, context));
                    results.put((SpellEffect)c.getPart(), ComponentApplicationResult.DELAYED);
                }
            } else if (target.isEntity() && ((SpellEffect)c.getPart()).targetsEntities() && !context.hasEntityBeenAffected((SpellEffect)c.getPart(), target.getEntity())) {
                if (delayInSeconds == 0) {
                    ComponentApplicationResult cRes = null;
                    try {
                        cRes = ((SpellEffect)c.getPart()).ApplyEffect(source, target, (IModifiedSpellPart<SpellEffect>)c, context);
                    }
                    catch (Throwable t) {
                        ManaAndArtifice.LOGGER.error(t);
                        cRes = ComponentApplicationResult.FAIL;
                    }
                    results.put((SpellEffect)c.getPart(), cRes);
                    context.addAffectedEntity((SpellEffect)c.getPart(), target.getEntity());
                } else {
                    DelayedEventQueue.pushEvent((Level)context.getServerLevel(), new TimedDelayedSpellEffect("" + context.getServerLevel().getGameTime(), delayInSeconds, source, target, (IModifiedSpellPart<SpellEffect>)c, context));
                    results.put((SpellEffect)c.getPart(), ComponentApplicationResult.DELAYED);
                }
            }
        });
        ServerLevel world = context.getServerLevel();
        if (!world.isClientSide && !recipe.getShape().getPart().isChanneled() && (appliedEffects = results.entrySet().stream().map(e -> e.getValue() == ComponentApplicationResult.SUCCESS ? (SpellEffect)e.getKey() : null).filter(e -> e != null).collect(Collectors.toList())).size() > 0) {
            SpellCaster.spawnClientFX((Level)context.getServerLevel(), target.getPosition(), Vec3.ZERO, source, appliedEffects);
        }
        return results;
    }

    public static void AddAffinityAndMagicXP(SpellRecipe recipe, Player caster) {
        SpellCaster.AddAffinityAndMagicXP(recipe, caster, -1);
    }

    public static void AddAffinityAndMagicXP(SpellRecipe recipe, Player caster, int channelDuration) {
        if (caster == null || recipe == null || !recipe.isValid()) {
            return;
        }
        MNACap.get(caster, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
            MNACap.get(caster, PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> {
                int xp = (int)recipe.getComplexity();
                float affinity = 0.1f;
                if (channelDuration > -1) {
                    int maxChannelTime = ((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).maxChannelTime(recipe.getShape());
                    xp = (int)((float)xp * ((float)channelDuration / (float)Math.max(1, maxChannelTime)));
                    affinity *= (float)channelDuration / (float)Math.max(1, maxChannelTime);
                }
                magic.addMagicXP(xp, caster, (IPlayerProgression)progression);
                if (((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).grantComponentRoteXP() && !CuriosInterop.GetSingleItem((LivingEntity)caster, (Item)ItemInit.BELT_AFFINITY_LOCK.get()).isPresent()) {
                    for (Map.Entry<Affinity, Float> e : recipe.getAffinity().entrySet()) {
                        magic.shiftAffinity(caster, e.getKey(), affinity * e.getValue().floatValue());
                    }
                }
            });
            if (!caster.level().isClientSide()) {
                MNACap.get(caster, PlayerRoteSpellsProvider.ROTE).ifPresent(rote -> {
                    if (rote.addRoteXP(caster, (ISpellComponent)((ModifiedSpellPart)recipe.getShape()).getPart(), channelDuration == -1 ? 1.0f : (float)channelDuration)) {
                        SpellCaster.sendRoteMessage(caster, ((ModifiedSpellPart)recipe.getShape()).getPart());
                    }
                    if (((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).grantComponentRoteXP()) {
                        recipe.iterateComponents(c -> SpellCaster.addComponentXP(caster, magic, rote, (SpellEffect)c.getPart()));
                    }
                    for (int i = 0; i < recipe.countModifiers(); ++i) {
                        if (!rote.addRoteXP(caster, recipe.getModifier(i))) continue;
                        SpellCaster.sendRoteMessage(caster, recipe.getModifier(i));
                    }
                });
            }
        });
    }

    public static void addComponentRoteProgress(Player caster, SpellEffect c) {
        SpellCaster.addComponentXP(caster, (IPlayerMagic)caster.getData(PlayerMagicProvider.MAGIC.get()), (IPlayerRoteSpells)caster.getData(PlayerRoteSpellsProvider.ROTE.get()), c);
    }

    public static void addComponentXP(Player caster, IPlayerMagic magic, IPlayerRoteSpells rote, SpellEffect c) {
        float oppositeDepth;
        float detriment;
        float mainDepth = Math.min(magic.getAffinityDepth(c.getAffinity()), 90.0f);
        float bonus = mainDepth / 100.0f;
        float xpAdd = 1.0f + bonus - (detriment = (oppositeDepth = Math.min(magic.getAffinityDepth(c.getAffinity().getOpposite()), 90.0f)) / 100.0f);
        if (rote.addRoteXP(caster, c, xpAdd)) {
            SpellCaster.sendRoteMessage(caster, c);
        }
    }

    public static void sendRoteMessage(Player caster, ISpellComponent component) {
        MutableComponent roteMessage = Component.translatable((String)"event.mna.spell_part_rote", (Object[])new Object[]{Component.translatable((String)component.getRegistryName().toString())}).withStyle(ChatFormatting.GOLD);
        caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SFX.Event.Player.SPELL_CREATED, SoundSource.PLAYERS, 1.0f, 1.0f);
        caster.sendSystemMessage((Component)roteMessage);
    }

    public static void spawnClientFX(Level world, Vec3 position, Vec3 normal, SpellSource source, SpellEffect ... effects) {
        SpellCaster.spawnClientFX(world, position, normal, source, Arrays.asList(effects));
    }

    public static void spawnClientFX(Level world, Vec3 position, Vec3 normal, SpellSource source, List<SpellEffect> effects) {
        if (!world.isLoaded(BlockPos.containing((Position)position))) {
            return;
        }
        SpellRecipe recipe = new SpellRecipe();
        effects.forEach(e -> recipe.addComponent((SpellEffect)e));
        SpellFX fx = new SpellFX((EntityType)EntityInit.SPELL_FX.get(), world);
        fx.setCaster(source.getCaster());
        fx.setRecipe(recipe);
        fx.setPos(position.x, position.y, position.z);
        fx.setDeltaMovement(normal);
        world.addFreshEntity((Entity)fx);
    }

    public static float getComplexity(ItemStack spellStack) {
        return SpellCaster.getComplexity(spellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    public static float getComplexity(CompoundTag spellNBT) {
        SpellRecipe recipe = SpellRecipe.fromNBT(spellNBT);
        if (recipe == null || !recipe.isValid()) {
            return 0.0f;
        }
        return recipe.getComplexity();
    }

    public static void registerAdjuster(Predicate<SpellAdjustingContext> executeCheck, Consumer<SpellAdjustingContext> adjuster) {
        _adjusters.add(new SpellAdjuster(executeCheck, adjuster));
    }

    public static void registerAdjuster(Predicate<SpellAdjustingContext> executeCheck, BiConsumer<ISpellDefinition, LivingEntity> adjuster) {
        _adjusters.add(new SpellAdjuster(executeCheck, adjuster));
    }

    public static void applyAdjusters(ItemStack stack, LivingEntity caster, InteractionHand hand, boolean curioCast, ISpellDefinition recipe, SpellCastStage stage) {
        for (SpellAdjuster adjuster : _adjusters) {
            if (!adjuster.check(stage, stack, recipe, caster, hand, curioCast)) continue;
            adjuster.modify(stack, recipe, caster, hand, curioCast);
        }
        CalculatingManaCostEvent event = new CalculatingManaCostEvent(caster, recipe, recipe.getManaCost());
        NeoForge.EVENT_BUS.post(event);
        if (event.getManaCost() != recipe.getManaCost()) {
            recipe.setManaCost(event.getManaCost());
        }
    }

    public static boolean consumeChanneledMana(Player player, ItemStack stack) {
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic == null) {
            return false;
        }
        SpellRecipe spell = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (spell == null || !spell.isValid()) {
            return false;
        }
        if (player.isCreative()) {
            return true;
        }
        SpellCaster.applyAdjusters(stack, (LivingEntity)player, player.getUsedItemHand(), false, spell, SpellCastStage.CALCULATING_MANA_COST);
        float manaCost = spell.getManaCost();
        if (!magic.getCastingResource().hasEnough((LivingEntity)player, manaCost)) {
            return false;
        }
        magic.getCastingResource().consume((LivingEntity)player, manaCost);
        return true;
    }

    public static final void setCooldown(ItemStack stack, Player player, int durationInTicks) {
        IPlayerProgression progression;
        Item item = stack.getItem();
        if (!_cooldownItems.contains(item)) {
            _cooldownItems.add(item);
        }
        if ((progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get())) != null && progression.getAlliedFaction() == Factions.DEMONS) {
            int flameLevel = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
            durationInTicks = player.isOnFire() && flameLevel > 0 ? (int)((float)durationInTicks * 0.333f) : (int)((double)durationInTicks * 0.75);
        }
        int quickCharge = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        durationInTicks = (int)((float)durationInTicks * (1.0f - 0.1f * (float)quickCharge));
        SpellCooldownCalculatingEvent event = new SpellCooldownCalculatingEvent(player, SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()), durationInTicks);
        NeoForge.EVENT_BUS.post(event);
        if (event.getCooldown() == 0) {
            return;
        }
        for (Item cdItem : _cooldownItems) {
            player.getCooldowns().addCooldown(cdItem, event.getCooldown());
        }
    }

    private static List<Pair<IItemHandler, Direction>> getReagentSearchInventories(Player caster) {
        ArrayList<Pair<IItemHandler, Direction>> output = new ArrayList<Pair<IItemHandler, Direction>>();
        CuriosInterop.GetSingleItem((LivingEntity)caster, (Item)ItemInit.PRACTITIONERS_POUCH.get(), (Item)ItemInit.MITHIONS_MAGNIFICENT_MBAG.get()).ifPresent(result -> {
            ItemStack invStack = result.stack();
            if (invStack.isEmpty() || !(invStack.getItem() instanceof PractitionersPouch)) {
                return;
            }
            PractitionersPouch item = (PractitionersPouch)invStack.getItem();
            Pair<IItemHandler, Direction> remoteInv = item.resolveRemoteInventory(invStack, caster.level());
            if (remoteInv.getFirst() != null) {
                output.add(remoteInv);
            }
            if (item.getPatchLevel(invStack, PractitionersPouchPatches.RIFT) > 0) {
                MNACap.get(caster, PlayerMagicProvider.MAGIC).ifPresent(m -> output.add(new Pair(new InvWrapper((Container)m.getRiftInventory()), Direction.UP)));
            }
            InventoryRitualKit kit = new InventoryRitualKit(invStack);
            output.add(new Pair(kit, Direction.UP));
        });
        for (int i = 0; i < caster.getInventory().getContainerSize(); ++i) {
            ItemStack invStack = caster.getInventory().getItem(i);
            if (invStack.isEmpty() || !(invStack.getItem() instanceof PractitionersPouch)) continue;
            PractitionersPouch item = (PractitionersPouch)invStack.getItem();
            Pair<IItemHandler, Direction> remoteInv = item.resolveRemoteInventory(invStack, caster.level());
            if (remoteInv.getFirst() != null) {
                output.add(remoteInv);
            }
            if (item.getPatchLevel(invStack, PractitionersPouchPatches.RIFT) > 0) {
                MNACap.get(caster, PlayerMagicProvider.MAGIC).ifPresent(m -> output.add(new Pair(new InvWrapper((Container)m.getRiftInventory()), Direction.UP)));
            }
            InventoryRitualKit kit = new InventoryRitualKit(invStack);
            output.add((Pair<IItemHandler, Direction>)new Pair(kit, Direction.UP));
        }
        output.add(new Pair(new InvWrapper((Container)caster.getInventory()), Direction.UP));
        return output;
    }

    private static final HashMap<Item, Boolean> checkReagents(Player caster, InteractionHand hand, SpellRecipe recipe) {
        HashMap<Item, Boolean> missing = new HashMap<Item, Boolean>();
        if (caster.isCreative()) {
            return missing;
        }
        List<Pair<IItemHandler, Direction>> inventories = SpellCaster.getReagentSearchInventories(caster);
        for (SpellReagent reagent : recipe.getReagents(caster, hand, null)) {
            if (InventoryUtilities.consumeAcrossInventories(reagent.getReagentStack(), reagent.getIgnoreDurability(), reagent.getCompareNBT(), true, inventories)) continue;
            missing.put(reagent.getReagentStack().getItem(), reagent.getOptional());
        }
        return missing;
    }

    public static final boolean consumeReagents(Player caster, InteractionHand hand, List<SpellReagent> reagents) {
        if (caster.isCreative() || caster.level().isClientSide()) {
            return true;
        }
        List<Pair<IItemHandler, Direction>> inventories = SpellCaster.getReagentSearchInventories(caster);
        boolean output = true;
        for (SpellReagent reagent : reagents) {
            output &= InventoryUtilities.consumeAcrossInventories(reagent.getReagentStack(), reagent.getIgnoreDurability(), reagent.getCompareNBT(), false, inventories);
        }
        return output;
    }

    public static final boolean consumeReagents(Player caster, InteractionHand hand, ISpellDefinition recipe, SpellCastingResult componentResults) {
        if (caster.isCreative() || caster.level().isClientSide()) {
            return true;
        }
        List<Pair<IItemHandler, Direction>> inventories = SpellCaster.getReagentSearchInventories(caster);
        boolean output = true;
        for (SpellReagent reagent : recipe.getReagents(caster, hand, componentResults)) {
            if (!reagent.getConsume()) continue;
            output &= InventoryUtilities.consumeAcrossInventories(reagent.getReagentStack(), reagent.getIgnoreDurability(), reagent.getCompareNBT(), false, inventories);
        }
        return output;
    }

    @Override
    public SpellCastingResult affect(ItemStack stack, ISpellDefinition spell, Level world, SpellSource source) {
        return SpellCaster.Affect(stack, spell, world, source, null, null);
    }

    @Override
    public SpellCastingResult affect(ItemStack stack, ISpellDefinition spell, Level world, SpellSource source, SpellTarget targetHint) {
        return SpellCaster.Affect(stack, spell, world, source, targetHint, null);
    }

    @Override
    public SpellCastingResult affect(ItemStack stack, ISpellDefinition spell, Level world, SpellSource source, SpellTarget targetHint, SpellContext context) {
        return SpellCaster.Affect(stack, spell, world, source, targetHint, context);
    }

    @Override
    public ISpellDefinition createSpell(Shape shape, SpellEffect component, Modifier ... modifiers) {
        SpellRecipe recipe = new SpellRecipe();
        recipe.setShape(shape);
        recipe.addComponent(component);
        for (int i = 0; i < Math.min(3, modifiers.length); ++i) {
            recipe.setModifier(modifiers[i], i);
        }
        return recipe;
    }

    @Override
    public ISpellDefinition parseSpellDefinition(ItemStack stack) {
        return this.parseSpellDefinition(stack, null);
    }

    @Override
    public ISpellDefinition parseSpellDefinition(ItemStack stack, @Nullable Player player) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (stack.getItem() == ItemInit.ROTE_BOOK.get()) {
            tag = player == null ? ((BookOfRote)ItemInit.ROTE_BOOK.get()).getSelectedStack(stack, player).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() : ((BookOfRote)ItemInit.ROTE_BOOK.get()).getSpellCompound(stack, player);
        }
        return SpellRecipe.fromNBT(tag);
    }

    @Override
    public SpellCastingResult playerCast(ItemStack stack, Player caster, InteractionHand hand, boolean consumeMana, boolean isCurioCast) {
        return SpellCaster.PlayerCast(stack, caster, hand, caster.position(), caster.getLookAngle(), caster.level(), consumeMana, isCurioCast);
    }

    @Override
    public void writeSpellDefinition(ISpellDefinition spell, ItemStack stack) {
        spell.writeToNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    @Override
    public void registerSpellCastingItem(Item item) {
        _cooldownItems.add(item);
    }

    @Override
    public void registerSpellAdjuster(Predicate<SpellAdjustingContext> predicate, BiConsumer<ISpellDefinition, LivingEntity> adjuster) {
        SpellCaster.registerAdjuster(predicate, adjuster);
    }

    @Override
    public void registerSpellAdjuster(Predicate<SpellAdjustingContext> predicate, Consumer<SpellAdjustingContext> adjuster) {
        SpellCaster.registerAdjuster(predicate, adjuster);
    }

    @Override
    public MnaRegistry<Shape> getShapeRegistry() {
        return Registries.Shape;
    }

    @Override
    public MnaRegistry<SpellEffect> getComponentRegistry() {
        return Registries.SpellEffect;
    }

    @Override
    public MnaRegistry<Modifier> getModifierRegistry() {
        return Registries.Modifier;
    }

    @Override
    public boolean containsSpell(ItemStack stack) {
        return SpellRecipe.stackContainsSpell(stack);
    }

    @Override
    public boolean reflectSpell(Level level, @Nullable LivingEntity reflector, ISpellDefinition spell, SpellSource source, Vec3 position, Vec3 forward, boolean forwardOnly) {
        if (!spell.isValid()) {
            return true;
        }
        if (!source.hasCasterReference()) {
            return true;
        }
        Vec3 direction = position.subtract(source.getOrigin()).normalize();
        float angleDeg = (float)(Math.acos(forward.dot(direction)) * 180.0 / Math.PI);
        if (!forwardOnly || angleDeg > 120.0f && angleDeg < 170.0f) {
            if (!level.isClientSide()) {
                Vec3 reflect = MathUtils.reflect(forward, direction);
                Shape shape = spell.getShape().getPart();
                CompoundTag spellData = new CompoundTag();
                spell.writeToNBT(spellData);
                if (shape != Shapes.BEAM) {
                    if (shape == Shapes.PROJECTILE) {
                        SpellProjectile newProjectile = new SpellProjectile(reflector, level);
                        newProjectile.setSpellRecipe(spellData);
                        newProjectile.shoot((Entity)reflector, reflect, 1.0f, 0.1f);
                        level.addFreshEntity((Entity)newProjectile);
                    } else if (shape == Shapes.BOLT) {
                        ServerMessageDispatcher.sendParticleSpawn(position.x(), position.y(), position.z(), position.x() + reflect.x * 8.0, position.y() + reflect.y * 8.0, position.z() + reflect.z * 8.0, 0, 64.0f, (ResourceKey<Level>)level.dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void spawnSpellVFX(Level world, Vec3 position, Vec3 normal, SpellSource source, SpellEffect part) {
        SpellCaster.spawnClientFX(world, position, normal, source, part);
    }

    @Override
    public void setCooldown(ItemStack stack, Player player, ISpellDefinition spell) {
        SpellCaster.setCooldown(stack, player, spell.getCooldown((LivingEntity)player));
    }

    @Override
    public ISpellDefinition parseSpellDefinition(CompoundTag tag) {
        return SpellRecipe.fromNBT(tag);
    }

    static {
        _cooldownItems.add((Item)ItemInit.SPELL.get());
        _cooldownItems.add((Item)ItemInit.SPELL_BOOK.get());
        _cooldownItems.add((Item)ItemInit.GRIMOIRE.get());
        _cooldownItems.add((Item)ItemInit.GRIMOIRE_COUNCIL.get());
        _cooldownItems.add((Item)ItemInit.GRIMOIRE_DEMON.get());
        _cooldownItems.add((Item)ItemInit.GRIMOIRE_FEY.get());
        _cooldownItems.add((Item)ItemInit.GRIMOIRE_UNDEAD.get());
        _cooldownItems.add((Item)ItemInit.ROTE_BOOK.get());
        _cooldownItems.add((Item)ItemInit.PUNKIN_STAFF.get());
        _cooldownItems.add((Item)ItemInit.STAFF_AMETHYST.get());
        _cooldownItems.add((Item)ItemInit.STAFF_AUM.get());
        _cooldownItems.add((Item)ItemInit.STAFF_CERUBLOSSOM.get());
        _cooldownItems.add((Item)ItemInit.STAFF_CHIMERITE.get());
        _cooldownItems.add((Item)ItemInit.STAFF_DESERTNOVA.get());
        _cooldownItems.add((Item)ItemInit.STAFF_EMERALD.get());
        _cooldownItems.add((Item)ItemInit.STAFF_GLASS.get());
        _cooldownItems.add((Item)ItemInit.STAFF_GOLD.get());
        _cooldownItems.add((Item)ItemInit.STAFF_IRON.get());
        _cooldownItems.add((Item)ItemInit.STAFF_LAPIS.get());
        _cooldownItems.add((Item)ItemInit.STAFF_NETHERQUARTZ.get());
        _cooldownItems.add((Item)ItemInit.STAFF_PRISMARINECRYSTAL.get());
        _cooldownItems.add((Item)ItemInit.STAFF_PRISMARINESHARD.get());
        _cooldownItems.add((Item)ItemInit.STAFF_REDSTONE.get());
        _cooldownItems.add((Item)ItemInit.STAFF_SKULL.get());
        _cooldownItems.add((Item)ItemInit.STAFF_TARMA.get());
        _cooldownItems.add((Item)ItemInit.STAFF_VINTEUM.get());
        _cooldownItems.add((Item)ItemInit.STAFF_WAKEBLOOM.get());
        _cooldownItems.add((Item)ItemInit.STAFF_FOX.get());
        _cooldownItems.add((Item)ItemInit.BONE_SCYTHE.get());
        _cooldownItems.add((Item)ItemInit.WAND_AMETHYST.get());
        _cooldownItems.add((Item)ItemInit.WAND_AUM.get());
        _cooldownItems.add((Item)ItemInit.WAND_CERUBLOSSOM.get());
        _cooldownItems.add((Item)ItemInit.WAND_CHIMERITE.get());
        _cooldownItems.add((Item)ItemInit.WAND_DESERTNOVA.get());
        _cooldownItems.add((Item)ItemInit.WAND_EMERALD.get());
        _cooldownItems.add((Item)ItemInit.WAND_GLASS.get());
        _cooldownItems.add((Item)ItemInit.WAND_GOLD.get());
        _cooldownItems.add((Item)ItemInit.WAND_IRON.get());
        _cooldownItems.add((Item)ItemInit.WAND_LAPIS.get());
        _cooldownItems.add((Item)ItemInit.WAND_NETHERQUARTZ.get());
        _cooldownItems.add((Item)ItemInit.WAND_PRISMARINECRYSTAL.get());
        _cooldownItems.add((Item)ItemInit.WAND_PRISMARINESHARD.get());
        _cooldownItems.add((Item)ItemInit.WAND_REDSTONE.get());
        _cooldownItems.add((Item)ItemInit.WAND_SKULL.get());
        _cooldownItems.add((Item)ItemInit.WAND_TARMA.get());
        _cooldownItems.add((Item)ItemInit.WAND_VINTEUM.get());
        _cooldownItems.add((Item)ItemInit.WAND_WAKEBLOOM.get());
        _cooldownItems.add((Item)ItemInit.HELLFIRE_STAFF.get());
        _cooldownItems.add((Item)ItemInit.ASTRO_BLADE.get());
    }
}


