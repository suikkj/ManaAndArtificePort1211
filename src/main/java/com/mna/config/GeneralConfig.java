/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.config.ModConfigEvent
 *  org.apache.commons.lang3.tuple.Pair
 */
package com.mna.config;

import com.mna.ManaAndArtifice;
import com.mna.api.config.GeneralConfigValues;
import com.mna.config.GeneralConfigProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class GeneralConfig {
    public static final GeneralConfigProvider GENERAL;
    public static final ModConfigSpec GENERAL_SPEC;

    @SubscribeEvent
    public static void onModConfigEvent(ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == GENERAL_SPEC) {
            GeneralConfig.bakeConfig();
        }
    }

    private static void bakeConfig() {
        ManaAndArtifice.LOGGER.info("M&A >> Baking General Config");
        GeneralConfigValues.ModifyVillagerTrades = (Boolean)GeneralConfigProvider.MODIFY_VILLAGER_TRADES.get();
        GeneralConfigValues.DisableVillagerRegistration = (Boolean)GeneralConfigProvider.DISABLE_VILLAGER_REGISTRATION.get();
        GeneralConfigValues.ClassicRaids = (Boolean)GeneralConfigProvider.CLASSIC_RAIDS.get();
        GeneralConfigValues.ForcedRaidStrength = (Integer)GeneralConfigProvider.FORCED_RAID_STRENGTH.get();
        GeneralConfigValues.RaidChanceBase = (Double)GeneralConfigProvider.RAID_CHANCE_BASE.get();
        GeneralConfigValues.RaidChanceTier = (Double)GeneralConfigProvider.RAID_CHANCE_TIER.get();
        GeneralConfigValues.RaidSpawnAttempts = (Integer)GeneralConfigProvider.RAID_SPAWN_ATTEMPTS.get();
        GeneralConfigValues.FactionMobKillIre = (Boolean)GeneralConfigProvider.FACTION_MOB_KILL_IRE.get();
        GeneralConfigValues.WellspringDistance = (Integer)GeneralConfigProvider.WELLSPRING_DISTANCE.get();
        GeneralConfigValues.GenericWellsprings = (Boolean)GeneralConfigProvider.GENERIC_WELLSPRINGS.get();
        GeneralConfigValues.GradualTimeChange = (Boolean)GeneralConfigProvider.GRADUAL_TIME_CHANGE.get();
        GeneralConfigValues.FossilizeDamage = (Double)GeneralConfigProvider.FOSSILIZE_DAMAGE.get();
        GeneralConfigValues.PhylacteryKillCount = (Integer)GeneralConfigProvider.PHYLACTERY_KILLS.get();
        GeneralConfigValues.SummonBosses = (Boolean)GeneralConfigProvider.SUMMON_BOSSES.get();
        GeneralConfigValues.SummonInteractions = (Boolean)GeneralConfigProvider.SUMMON_INTERACTIONS.get();
        GeneralConfigValues.BossesAllowedInPhylacteries = (Boolean)GeneralConfigProvider.PHYLACTERY_BOSSES.get();
        GeneralConfigValues.GlobalDamageScale = (Double)GeneralConfigProvider.DAMAGE_SCALE.get();
        GeneralConfigValues.SigilLimit = (Integer)GeneralConfigProvider.SIGIL_LIMIT.get();
        GeneralConfigValues.AutoRoteLowTier = (Integer)GeneralConfigProvider.ROTE_LOW_TIER_AUTO.get();
        GeneralConfigValues.Tier5ComplexityLimit = (Integer)GeneralConfigProvider.TIER_5_COMPLEXITY_LIMIT.get();
        GeneralConfigValues.ConfuseAffectsPlayers = (Boolean)GeneralConfigProvider.CONFUSE_AFFECTS_PLAYERS.get();
        GeneralConfigValues.PossessionAffectsPlayers = (Boolean)GeneralConfigProvider.POSSESSION_AFFECTS_PLAYERS.get();
        GeneralConfigValues.SplashCreatesSources = (Boolean)GeneralConfigProvider.SPLASH_CREATES_SOURCES.get();
        GeneralConfigValues.BreakMagnitudeMapping = new ArrayList((Collection)GeneralConfigProvider.BREAK_MAGNITUDE_MAPPING.get());
        GeneralConfigValues.InsightDamageColors = new HashSet((Collection)GeneralConfigProvider.INSIGHT_DAMAGE_COLORS.get());
        GeneralConfigValues.DisjunctionBlacklist = new HashSet((Collection)GeneralConfigProvider.DISJUNCTION_BLACKLIST.get());
        GeneralConfigValues.MeteorJumpEnabled = (Boolean)GeneralConfigProvider.METEOR_JUMP.get();
        GeneralConfigValues.SpellweaverReflectCharges = (Integer)GeneralConfigProvider.SPELLWEAVER_REFLECTCHARGES.get();
        GeneralConfigValues.SpellweaverReflectTime = (Integer)GeneralConfigProvider.SPELLWEAVER_RECHARGETIME.get();
        GeneralConfigValues.DruidicReflectChance = (Double)GeneralConfigProvider.DRUIDIC_REFLECTCHANCE.get();
        GeneralConfigValues.DruidicTeleportChance = (Double)GeneralConfigProvider.DRUIDIC_TELEPORTCHANCE.get();
        GeneralConfigValues.TotalManaRegenTicks = (Integer)GeneralConfigProvider.MANA_TICKS_FOR_REGEN.get();
        GeneralConfigValues.SoulsForPlayerKill = (Integer)GeneralConfigProvider.SOULS_PLAYER.get();
        GeneralConfigValues.SoulsForVillagerKill = (Integer)GeneralConfigProvider.SOULS_VILLAGER.get();
        GeneralConfigValues.SoulsForFactionMobKill = (Integer)GeneralConfigProvider.SOULS_FACTION.get();
        GeneralConfigValues.SoulsForMobKill = (Integer)GeneralConfigProvider.SOULS_MOB.get();
        GeneralConfigValues.SoulsForAnimalKill = (Integer)GeneralConfigProvider.SOULS_ANIMAL.get();
        GeneralConfigValues.SoulsForUndeadKill = (Integer)GeneralConfigProvider.SOULS_UNDEAD.get();
        GeneralConfigValues.BrimstonePerHeart = (Integer)GeneralConfigProvider.BRIMSTONE_PER_HEART.get();
        GeneralConfigValues.TierHealthBoosts = (List)GeneralConfigProvider.TIER_HEALTH_BOOSTS.get();
        GeneralConfigValues.TierUpPercentTasksComplete = (Double)GeneralConfigProvider.TIER_PCT.get();
        GeneralConfigValues.MeleeDistance = (Double)GeneralConfigProvider.MELEE_DIST.get();
        GeneralConfigValues.LiteMode = (Boolean)GeneralConfigProvider.LITE_MODE.get();
        GeneralConfigValues.FixJumpBoost = (Boolean)GeneralConfigProvider.FIX_JUMP_BOOST.get();
        GeneralConfigValues.ProjectionCanEnchantAnything = (Boolean)GeneralConfigProvider.PROJECTION_ENCHANT_ANYTHING.get();
        GeneralConfigValues.AverageManaweaveCost = (Integer)GeneralConfigProvider.AVERAGE_MANAWEAVE_COST.get();
        GeneralConfigValues.ExperiencePerRunescribeUndo = (Integer)GeneralConfigProvider.EXPERIENCE_PER_RUNESCRIBE_UNDO.get();
        GeneralConfigValues.RitualPermissionLevel = (Integer)GeneralConfigProvider.RITUAL_PERMISSION_LEVEL.get();
        GeneralConfigValues.RTPDistance = (Integer)GeneralConfigProvider.RTP_DISTANCE.get();
        GeneralConfigValues.WardingCandleBlacklist = new HashSet((Collection)GeneralConfigProvider.WARD_BLACKLIST.get());
        GeneralConfigValues.WardingCandleWhitelist = new HashSet((Collection)GeneralConfigProvider.WARD_WHITELIST.get());
        GeneralConfigValues.DemonBonusDamageMod = (Integer)GeneralConfigProvider.DEMON_BONUS_DAMAGE_MOD.get();
        GeneralConfigValues.DemonBonusRangeMod = (Integer)GeneralConfigProvider.DEMON_BONUS_RANGE_MOD.get();
        GeneralConfigValues.DemonBonusRadiusMod = (Integer)GeneralConfigProvider.DEMON_BONUS_RADIUS_MOD.get();
        GeneralConfigValues.UndeadCoffinSoulsDelay = (Integer)GeneralConfigProvider.UNDEAD_COFFIN_SOULS_DELAY.get();
        GeneralConfigValues.UndeadCoffinSoulsRestored = (Double)GeneralConfigProvider.UNDEAD_COFFIN_SOULS_RESTORED.get();
        GeneralConfigValues.LightPylonBrightnessThreshold = (Integer)GeneralConfigProvider.LIGHT_PYLON_BRIGHTNESS_THRESHOLD.get();
        GeneralConfigValues.ImmortalConstructs = (Boolean)GeneralConfigProvider.IMMORTAL_CONSTRUCTS.get();
        GeneralConfigValues.LanternWraithSpawnBoundsSize = (Integer)GeneralConfigProvider.LANTERN_WRAITH_SPAWN_BOUNDS_SIZE.get();
        GeneralConfigValues.LanternWraithGroupingLimit = (Integer)GeneralConfigProvider.LANTERN_WRAITH_GROUPING_LIMIT.get();
        GeneralConfigValues.DisableBossArenaProtection = (Boolean)GeneralConfigProvider.DISABLE_BOSS_ARENA_PROTECTION.get();
        GeneralConfigValues.ChunkMagicRequiredForSpawns = (Boolean)GeneralConfigProvider.REQUIRE_RECENT_MAGIC_FOR_SPAWNS.get();
    }

    public static float getDamageMultiplier() {
        return (float)Math.max(GeneralConfigValues.GlobalDamageScale, (double)0.1f);
    }

    static {
        Pair specPair = new ModConfigSpec.Builder().configure(GeneralConfigProvider::new);
        GENERAL = (GeneralConfigProvider)specPair.getLeft();
        GENERAL_SPEC = (ModConfigSpec)specPair.getRight();
    }
}


