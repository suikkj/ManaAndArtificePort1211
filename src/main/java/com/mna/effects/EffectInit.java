/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.effects;

import com.mna.effects.beneficial.EffectBindWounds;
import com.mna.effects.beneficial.EffectBriarthornBarrier;
import com.mna.effects.beneficial.EffectBulwark;
import com.mna.effects.beneficial.EffectBurningRage;
import com.mna.effects.beneficial.EffectCamouflage;
import com.mna.effects.beneficial.EffectChronoAnchor;
import com.mna.effects.beneficial.EffectCircleOfPower;
import com.mna.effects.beneficial.EffectEldrinFlight;
import com.mna.effects.beneficial.EffectEldrinSight;
import com.mna.effects.beneficial.EffectFireShield;
import com.mna.effects.beneficial.EffectInstantMana;
import com.mna.effects.beneficial.EffectLevitation;
import com.mna.effects.beneficial.EffectManaBoost;
import com.mna.effects.beneficial.EffectManaRegen;
import com.mna.effects.beneficial.EffectManaShield;
import com.mna.effects.beneficial.EffectMistForm;
import com.mna.effects.beneficial.EffectPilgrim;
import com.mna.effects.beneficial.EffectPossession;
import com.mna.effects.beneficial.EffectRepair;
import com.mna.effects.beneficial.EffectSenseLife;
import com.mna.effects.beneficial.EffectSoar;
import com.mna.effects.beneficial.EffectSpiderClimbing;
import com.mna.effects.beneficial.EffectTelekinesis;
import com.mna.effects.beneficial.EffectTrueInvisibility;
import com.mna.effects.beneficial.EffectUndeadFrostWalker;
import com.mna.effects.beneficial.EffectWaterWalking;
import com.mna.effects.beneficial.EffectWellspringSight;
import com.mna.effects.beneficial.EffectWindWall;
import com.mna.effects.beneficial.SimpleBeneficialEffect;
import com.mna.effects.harmful.EffectAsphyxiate;
import com.mna.effects.harmful.EffectChill;
import com.mna.effects.harmful.EffectChronoExhaustion;
import com.mna.effects.harmful.EffectConfuse;
import com.mna.effects.harmful.EffectCurseOfAgony;
import com.mna.effects.harmful.EffectDeath;
import com.mna.effects.harmful.EffectDisjunction;
import com.mna.effects.harmful.EffectEarthquake;
import com.mna.effects.harmful.EffectEntangle;
import com.mna.effects.harmful.EffectHeatwave;
import com.mna.effects.harmful.EffectIcarianFlight;
import com.mna.effects.harmful.EffectInstantChill;
import com.mna.effects.harmful.EffectLacerate;
import com.mna.effects.harmful.EffectLivingBomb;
import com.mna.effects.harmful.EffectManaBurn;
import com.mna.effects.harmful.EffectMindControl;
import com.mna.effects.harmful.EffectMindVision;
import com.mna.effects.harmful.EffectShacklesOfPain;
import com.mna.effects.harmful.EffectSoulTrap;
import com.mna.effects.harmful.EffectSoulVulnerability;
import com.mna.effects.harmful.EffectSunder;
import com.mna.effects.harmful.EffectWrithingBrambles;
import com.mna.effects.harmful.SimpleHarmfulEffect;
import com.mna.effects.neutral.EffectAmplifyMagic;
import com.mna.effects.neutral.EffectChoosingWellspring;
import com.mna.effects.neutral.EffectColdDark;
import com.mna.effects.neutral.EffectDampenMagic;
import com.mna.effects.neutral.EffectGrow;
import com.mna.effects.neutral.EffectLift;
import com.mna.effects.neutral.EffectManaStunt;
import com.mna.effects.neutral.EffectShrink;
import com.mna.effects.neutral.EffectSoaked;
import com.mna.effects.neutral.SimpleNeutralEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EffectInit {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "mna");
    public static final DeferredHolder<MobEffect, ? extends MobEffect> REPAIR = EFFECTS.register("repair", () -> new EffectRepair());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ELDRIN_SIGHT = EFFECTS.register("eldrin_sight", () -> new EffectEldrinSight());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WELLSPRING_SIGHT = EFFECTS.register("wellspring_sight", () -> new EffectWellspringSight());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WATERY_GRAVE = EFFECTS.register("watery_grave", () -> new SimpleHarmfulEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> GRAVITY_WELL = EFFECTS.register("gravity_well", () -> new SimpleHarmfulEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHRONO_ANCHOR = EFFECTS.register("chrono_anchor", () -> new EffectChronoAnchor());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHRONO_EXHAUSTION = EFFECTS.register("chrono_exhaustion", () -> new EffectChronoExhaustion());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ENTANGLE = EFFECTS.register("entangle", () -> new EffectEntangle());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SILENCE = EFFECTS.register("silence", () -> new SimpleHarmfulEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_BOOST = EFFECTS.register("mana_boost", () -> new EffectManaBoost());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_REGEN = EFFECTS.register("mana_regen", () -> new EffectManaRegen());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DISPEL_EXHAUSTION = EFFECTS.register("dispel_exhaustion", () -> new SimpleNeutralEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ASPHYXIATE = EFFECTS.register("asphyxiate", () -> new EffectAsphyxiate());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> INSTANT_MANA = EFFECTS.register("instant_mana", () -> new EffectInstantMana(0x99CCFF));
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_BURN = EFFECTS.register("mana_burn", () -> new EffectManaBurn());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_STUNT = EFFECTS.register("mana_stunt", () -> new EffectManaStunt());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LIFE_TAP = EFFECTS.register("life_tap", () -> new SimpleHarmfulEffect());
    public static final DeferredHolder<MobEffect, ? extends EffectManaShield> MANA_SHIELD = EFFECTS.register("mana_shield", () -> new EffectManaShield());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BRIARTHORN_BARRIER = EFFECTS.register("briarthorn_barrier", () -> new EffectBriarthornBarrier());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> FIRE_SHIELD = EFFECTS.register("flame_cloak", () -> new EffectFireShield());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> GREATER_INVISIBILITY = EFFECTS.register("greater_invisibility", () -> new EffectTrueInvisibility());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CONFUSION = EFFECTS.register("confusion", () -> new EffectConfuse());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MIND_CONTROL = EFFECTS.register("mind_control", () -> new EffectMindControl());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DIVINATION = EFFECTS.register("divination", () -> new SimpleBeneficialEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MIST_FORM = EFFECTS.register("mist_form", () -> new EffectMistForm());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MIND_VISION = EFFECTS.register("mind_vision", () -> new EffectMindVision());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LIVING_BOMB = EFFECTS.register("living_bomb", () -> new EffectLivingBomb());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> TIMED_DEATH = EFFECTS.register("death", () -> new EffectDeath());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> POSSESSION = EFFECTS.register("possession", () -> new EffectPossession());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHOOSING_WELLSPRING = EFFECTS.register("choosing_wellspring", () -> new EffectChoosingWellspring());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ELDRIN_FLIGHT = EFFECTS.register("eldrin_flight", () -> new EffectEldrinFlight());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CIRCLE_OF_POWER = EFFECTS.register("circle_of_power", () -> new EffectCircleOfPower());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> COLD_DARK = EFFECTS.register("cold_dark", () -> new EffectColdDark());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOUL_VULNERABILITY = EFFECTS.register("soul_vulnerability", () -> new EffectSoulVulnerability());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WIND_WALL = EFFECTS.register("wind_wall", () -> new EffectWindWall());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LEVITATION = EFFECTS.register("levitation", () -> new EffectLevitation());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> FORTIFICATION = EFFECTS.register("fossilize", () -> new SimpleBeneficialEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SNOWBLIND = EFFECTS.register("snowblind", () -> new SimpleHarmfulEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BIND_WOUNDS = EFFECTS.register("bind_wounds", () -> new EffectBindWounds());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> INSIGHT = EFFECTS.register("insight", () -> new SimpleNeutralEffect(8632899));
    public static final DeferredHolder<MobEffect, ? extends MobEffect> EARTHQUAKE = EFFECTS.register("earthquake", () -> new EffectEarthquake());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> AMPLIFY_MAGIC = EFFECTS.register("amplify_magic", () -> new EffectAmplifyMagic());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DAMPEN_MAGIC = EFFECTS.register("dampen_magic", () -> new EffectDampenMagic());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOAKED = EFFECTS.register("soaked", () -> new EffectSoaked());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SUNDER = EFFECTS.register("sunder", () -> new EffectSunder());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> FRAILTY = EFFECTS.register("frailty", () -> new SimpleHarmfulEffect());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DISJUNCTION = EFFECTS.register("disjunction", () -> new EffectDisjunction());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> TELEKINESIS = EFFECTS.register("telekinesis", () -> new EffectTelekinesis());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WATER_WALKING = EFFECTS.register("water_walking", () -> new EffectWaterWalking());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SPIDER_CLIMBING = EFFECTS.register("spider_climbing", () -> new EffectSpiderClimbing());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOAR = EFFECTS.register("soar", () -> new EffectSoar());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LACERATE = EFFECTS.register("lacerate", () -> new EffectLacerate());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WRITHING_BRAMBLES = EFFECTS.register("writhing_brambles", () -> new EffectWrithingBrambles());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> AURA_OF_FROST = EFFECTS.register("aura_of_frost", () -> new EffectUndeadFrostWalker());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BURNING_RAGE = EFFECTS.register("burning_rage", () -> new EffectBurningRage());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> REDUCE = EFFECTS.register("reduced", () -> new EffectShrink());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ENLARGE = EFFECTS.register("enlarged", () -> new EffectGrow());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CAMOUFLAGE = EFFECTS.register("camouflage", () -> new EffectCamouflage());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BULWARK = EFFECTS.register("bulwark", () -> new EffectBulwark());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CURSE_OF_AGONY = EFFECTS.register("curse_of_agony", () -> new EffectCurseOfAgony());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SENSE_LIFE = EFFECTS.register("sense_life", () -> new EffectSenseLife());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOUL_TRAP = EFFECTS.register("soul_trap", () -> new EffectSoulTrap());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SHACKLES_OF_PAIN = EFFECTS.register("shackles_of_pain", () -> new EffectShacklesOfPain());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> INSTANT_CHILL = EFFECTS.register("instant_chill", () -> new EffectInstantChill());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> HEATWAVE = EFFECTS.register("heatwave", () -> new EffectHeatwave());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHILL = EFFECTS.register("chill", () -> new EffectChill());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LIFT = EFFECTS.register("lift", () -> new EffectLift());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> PILGRIM = EFFECTS.register("pilgrim", () -> new EffectPilgrim());
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ICARIAN_FLIGHT = EFFECTS.register("icarian_flight", () -> new EffectIcarianFlight());
}


