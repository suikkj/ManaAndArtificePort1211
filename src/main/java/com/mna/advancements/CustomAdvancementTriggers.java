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
    public static void commonLoad(FMLCommonSetupEvent event) {
        CriteriaTriggers.register("affinity_tinker", AFFINITY_TINKER);
        CriteriaTriggers.register("arcane_furnace_smelt", ARCANE_FURNACE_SMELT);
        CriteriaTriggers.register("animus_mana_tea", BE_OUR_GUEST);
        CriteriaTriggers.register("cast_cantrip", CANTRIP_CAST);
        CriteriaTriggers.register("capture_wellspring", CAPTURE_WELLSPRING);
        CriteriaTriggers.register("cast_spell", CAST_SPELL);
        CriteriaTriggers.register("multiblock_complete", COMPLETE_MULTIBLOCK);
        CriteriaTriggers.register("craft_spell", CRAFT_SPELL);
        CriteriaTriggers.register("defeat_boss", DEFEAT_BOSS);
        CriteriaTriggers.register("disenchant", DISENCHANT);
        CriteriaTriggers.register("draw_manaweave_pattern", DRAW_MANAWEAVE);
        CriteriaTriggers.register("eldrin_altar_craft", ELDRIN_ALTAR_CRAFT);
        CriteriaTriggers.register("faction_join", FACTION_JOIN);
        CriteriaTriggers.register("light_fume", LIGHT_FUME);
        CriteriaTriggers.register("magic_level", MAGIC_LEVEL);
        CriteriaTriggers.register("manaweave_altar_craft", MANAWEAVE_CRAFT);
        CriteriaTriggers.register("not_so_easy", NOT_SO_EASY);
        CriteriaTriggers.register("open_manaweave_cache", OPEN_CACHE);
        CriteriaTriggers.register("perform_ritual", PERFORM_RITUAL);
        CriteriaTriggers.register("runescribe", RUNESCRIBE);
        CriteriaTriggers.register("runeforge", RUNIC_ANVIL_CRAFT);
        CriteriaTriggers.register("set_bonus", SET_BONUS);
        CriteriaTriggers.register("shaman", SHAMAN);
        CriteriaTriggers.register("study_desk_rote", STUDY_DESK_ROTE);
        CriteriaTriggers.register("summon_construct", SUMMON_CONSTRUCT);
        CriteriaTriggers.register("thrown_runescribe_pattern", THROWN_RUNESCRIBE_PATTERN);
        CriteriaTriggers.register("tier", TIER_CHANGE);
        CriteriaTriggers.register("transcribe_spell", TRANSCRIBE_SPELL);
        CriteriaTriggers.register("use_charm", USE_CHARM);
    }
}


