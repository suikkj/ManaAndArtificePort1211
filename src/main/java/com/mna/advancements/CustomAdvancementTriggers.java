/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.CriteriaTriggers
 *  net.minecraft.advancements.CriterionTrigger
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 */
package com.mna.advancements;

import com.mna.advancements.triggers.AffinityTinkerTrigger;
import com.mna.advancements.triggers.ArcaneFurnaceSmeltTrigger;
import com.mna.advancements.triggers.BeOurGuestTrigger;
import com.mna.advancements.triggers.CaptureWellspringTrigger;
import com.mna.advancements.triggers.CastCantripTrigger;
import com.mna.advancements.triggers.CastSpellTrigger;
import com.mna.advancements.triggers.CompleteMultiblockTrigger;
import com.mna.advancements.triggers.CraftSpellTrigger;
import com.mna.advancements.triggers.DefeatBossTrigger;
import com.mna.advancements.triggers.DisenchantTrigger;
import com.mna.advancements.triggers.DrawManaweavingPatternTrigger;
import com.mna.advancements.triggers.EldrinAltarCraftTrigger;
import com.mna.advancements.triggers.FactionJoinTrigger;
import com.mna.advancements.triggers.LightEldrinFumeTrigger;
import com.mna.advancements.triggers.MagicLevelTrigger;
import com.mna.advancements.triggers.ManaweaveAltarCraftTrigger;
import com.mna.advancements.triggers.NotSoEasyTrigger;
import com.mna.advancements.triggers.OpenManaweaveCacheTrigger;
import com.mna.advancements.triggers.PerformRitualTrigger;
import com.mna.advancements.triggers.RunescribingTableCraftTrigger;
import com.mna.advancements.triggers.RunicAnvilCraftTrigger;
import com.mna.advancements.triggers.SetBonusTrigger;
import com.mna.advancements.triggers.ShamanTrigger;
import com.mna.advancements.triggers.StudyDeskRoteTrigger;
import com.mna.advancements.triggers.SummonConstructTrigger;
import com.mna.advancements.triggers.ThrownRunescribingPatternTrigger;
import com.mna.advancements.triggers.TierTrigger;
import com.mna.advancements.triggers.TranscribeSpellTrigger;
import com.mna.advancements.triggers.UseCharmTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class CustomAdvancementTriggers {
    public static final ThrownRunescribingPatternTrigger THROWN_RUNESCRIBE_PATTERN = new ThrownRunescribingPatternTrigger();
    public static final UseCharmTrigger USE_CHARM = new UseCharmTrigger();
    public static final SummonConstructTrigger SUMMON_CONSTRUCT = new SummonConstructTrigger();
    public static final ShamanTrigger SHAMAN = new ShamanTrigger();
    public static final NotSoEasyTrigger NOT_SO_EASY = new NotSoEasyTrigger();
    public static final BeOurGuestTrigger BE_OUR_GUEST = new BeOurGuestTrigger();
    public static final MagicLevelTrigger MAGIC_LEVEL = new MagicLevelTrigger();
    public static final DrawManaweavingPatternTrigger DRAW_MANAWEAVE = new DrawManaweavingPatternTrigger();
    public static final PerformRitualTrigger PERFORM_RITUAL = new PerformRitualTrigger();
    public static final ManaweaveAltarCraftTrigger MANAWEAVE_CRAFT = new ManaweaveAltarCraftTrigger();
    public static final RunescribingTableCraftTrigger RUNESCRIBE = new RunescribingTableCraftTrigger();
    public static final RunicAnvilCraftTrigger RUNIC_ANVIL_CRAFT = new RunicAnvilCraftTrigger();
    public static final ArcaneFurnaceSmeltTrigger ARCANE_FURNACE_SMELT = new ArcaneFurnaceSmeltTrigger();
    public static final EldrinAltarCraftTrigger ELDRIN_ALTAR_CRAFT = new EldrinAltarCraftTrigger();
    public static final CastCantripTrigger CANTRIP_CAST = new CastCantripTrigger();
    public static final CraftSpellTrigger CRAFT_SPELL = new CraftSpellTrigger();
    public static final CastSpellTrigger CAST_SPELL = new CastSpellTrigger();
    public static final TierTrigger TIER_CHANGE = new TierTrigger();
    public static final FactionJoinTrigger FACTION_JOIN = new FactionJoinTrigger();
    public static final SetBonusTrigger SET_BONUS = new SetBonusTrigger();
    public static final AffinityTinkerTrigger AFFINITY_TINKER = new AffinityTinkerTrigger();
    public static final LightEldrinFumeTrigger LIGHT_FUME = new LightEldrinFumeTrigger();
    public static final OpenManaweaveCacheTrigger OPEN_CACHE = new OpenManaweaveCacheTrigger();
    public static final CompleteMultiblockTrigger COMPLETE_MULTIBLOCK = new CompleteMultiblockTrigger();
    public static final DisenchantTrigger DISENCHANT = new DisenchantTrigger();
    public static final StudyDeskRoteTrigger STUDY_DESK_ROTE = new StudyDeskRoteTrigger();
    public static final DefeatBossTrigger DEFEAT_BOSS = new DefeatBossTrigger();
    public static final CaptureWellspringTrigger CAPTURE_WELLSPRING = new CaptureWellspringTrigger();
    public static final TranscribeSpellTrigger TRANSCRIBE_SPELL = new TranscribeSpellTrigger();

    @SubscribeEvent
    public static void registerTriggers(net.neoforged.neoforge.registries.RegisterEvent event) {
        // [PORT 1.21.1] Forge 1.20.1 registrava triggers no common setup com namespace do mod
        // (CriteriaTriggers.register 1-arg); o NeoForge congela antes do setup e o overload 2-arg
        // usa minecraft: -> registrar explicitamente como mna:<nome> via RegisterEvent.
        event.register(net.minecraft.core.registries.BuiltInRegistries.TRIGGER_TYPES.key(), helper -> {
            helper.register(com.mna.api.tools.RLoc.create("affinity_tinker"), AFFINITY_TINKER);
            helper.register(com.mna.api.tools.RLoc.create("arcane_furnace_smelt"), ARCANE_FURNACE_SMELT);
            helper.register(com.mna.api.tools.RLoc.create("animus_mana_tea"), BE_OUR_GUEST);
            helper.register(com.mna.api.tools.RLoc.create("cast_cantrip"), CANTRIP_CAST);
            helper.register(com.mna.api.tools.RLoc.create("capture_wellspring"), CAPTURE_WELLSPRING);
            helper.register(com.mna.api.tools.RLoc.create("cast_spell"), CAST_SPELL);
            helper.register(com.mna.api.tools.RLoc.create("multiblock_complete"), COMPLETE_MULTIBLOCK);
            helper.register(com.mna.api.tools.RLoc.create("craft_spell"), CRAFT_SPELL);
            helper.register(com.mna.api.tools.RLoc.create("defeat_boss"), DEFEAT_BOSS);
            helper.register(com.mna.api.tools.RLoc.create("disenchant"), DISENCHANT);
            helper.register(com.mna.api.tools.RLoc.create("draw_manaweave_pattern"), DRAW_MANAWEAVE);
            helper.register(com.mna.api.tools.RLoc.create("eldrin_altar_craft"), ELDRIN_ALTAR_CRAFT);
            helper.register(com.mna.api.tools.RLoc.create("faction_join"), FACTION_JOIN);
            helper.register(com.mna.api.tools.RLoc.create("light_fume"), LIGHT_FUME);
            helper.register(com.mna.api.tools.RLoc.create("magic_level"), MAGIC_LEVEL);
            helper.register(com.mna.api.tools.RLoc.create("manaweave_altar_craft"), MANAWEAVE_CRAFT);
            helper.register(com.mna.api.tools.RLoc.create("not_so_easy"), NOT_SO_EASY);
            helper.register(com.mna.api.tools.RLoc.create("open_manaweave_cache"), OPEN_CACHE);
            helper.register(com.mna.api.tools.RLoc.create("perform_ritual"), PERFORM_RITUAL);
            helper.register(com.mna.api.tools.RLoc.create("runescribe"), RUNESCRIBE);
            helper.register(com.mna.api.tools.RLoc.create("runeforge"), RUNIC_ANVIL_CRAFT);
            helper.register(com.mna.api.tools.RLoc.create("set_bonus"), SET_BONUS);
            helper.register(com.mna.api.tools.RLoc.create("shaman"), SHAMAN);
            helper.register(com.mna.api.tools.RLoc.create("study_desk_rote"), STUDY_DESK_ROTE);
            helper.register(com.mna.api.tools.RLoc.create("summon_construct"), SUMMON_CONSTRUCT);
            helper.register(com.mna.api.tools.RLoc.create("thrown_runescribe_pattern"), THROWN_RUNESCRIBE_PATTERN);
            helper.register(com.mna.api.tools.RLoc.create("tier"), TIER_CHANGE);
            helper.register(com.mna.api.tools.RLoc.create("transcribe_spell"), TRANSCRIBE_SPELL);
            helper.register(com.mna.api.tools.RLoc.create("use_charm"), USE_CHARM);
        });
    }
}


