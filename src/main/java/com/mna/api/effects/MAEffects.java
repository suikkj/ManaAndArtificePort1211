package com.mna.api.effects;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Compat 1.21.1: antes esta classe usava @ObjectHolder (removido no 1.21.1),
 * o que deixava TODAS as constantes nulas -> NPE garantido em jogo.
 * Agora aponta para os DeferredHolder reais de EffectInit.
 */
public class MAEffects {
    public static final DeferredHolder<MobEffect, ? extends MobEffect> REPAIR = com.mna.effects.EffectInit.REPAIR;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ELDRIN_SIGHT = com.mna.effects.EffectInit.ELDRIN_SIGHT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WELLSPRING_SIGHT = com.mna.effects.EffectInit.WELLSPRING_SIGHT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WATERY_GRAVE = com.mna.effects.EffectInit.WATERY_GRAVE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> GRAVITY_WELL = com.mna.effects.EffectInit.GRAVITY_WELL;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHRONO_ANCHOR = com.mna.effects.EffectInit.CHRONO_ANCHOR;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHRONO_EXHAUSTION = com.mna.effects.EffectInit.CHRONO_EXHAUSTION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ENTANGLE = com.mna.effects.EffectInit.ENTANGLE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SILENCE = com.mna.effects.EffectInit.SILENCE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_BOOST = com.mna.effects.EffectInit.MANA_BOOST;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_REGEN = com.mna.effects.EffectInit.MANA_REGEN;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DISPEL_EXHAUSTION = com.mna.effects.EffectInit.DISPEL_EXHAUSTION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ASPHYXIATE = com.mna.effects.EffectInit.ASPHYXIATE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> INSTANT_MANA = com.mna.effects.EffectInit.INSTANT_MANA;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_BURN = com.mna.effects.EffectInit.MANA_BURN;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_STUNT = com.mna.effects.EffectInit.MANA_STUNT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LIFE_TAP = com.mna.effects.EffectInit.LIFE_TAP;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MANA_SHIELD = com.mna.effects.EffectInit.MANA_SHIELD;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BRIARTHORN_BARRIER = com.mna.effects.EffectInit.BRIARTHORN_BARRIER;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> FIRE_SHIELD = com.mna.effects.EffectInit.FIRE_SHIELD;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> TRUE_INVISIBILITY = com.mna.effects.EffectInit.GREATER_INVISIBILITY;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CONFUSION = com.mna.effects.EffectInit.CONFUSION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MIND_CONTROL = com.mna.effects.EffectInit.MIND_CONTROL;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DIVINATION = com.mna.effects.EffectInit.DIVINATION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MIST_FORM = com.mna.effects.EffectInit.MIST_FORM;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> MIND_VISION = com.mna.effects.EffectInit.MIND_VISION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LIVING_BOMB = com.mna.effects.EffectInit.LIVING_BOMB;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> TIMED_DEATH = com.mna.effects.EffectInit.TIMED_DEATH;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> POSSESSION = com.mna.effects.EffectInit.POSSESSION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHOOSING_WELLSPRING = com.mna.effects.EffectInit.CHOOSING_WELLSPRING;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ELDRIN_FLIGHT = com.mna.effects.EffectInit.ELDRIN_FLIGHT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CIRCLE_OF_POWER = com.mna.effects.EffectInit.CIRCLE_OF_POWER;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOUL_VULNERABILITY = com.mna.effects.EffectInit.SOUL_VULNERABILITY;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WIND_WALL = com.mna.effects.EffectInit.WIND_WALL;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LEVITATION = com.mna.effects.EffectInit.LEVITATION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> FOSSILIZE = com.mna.effects.EffectInit.FORTIFICATION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BIND_WOUNDS = com.mna.effects.EffectInit.BIND_WOUNDS;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> INSIGHT = com.mna.effects.EffectInit.INSIGHT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> EARTHQUAKE = com.mna.effects.EffectInit.EARTHQUAKE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> AMPLIFY_MAGIC = com.mna.effects.EffectInit.AMPLIFY_MAGIC;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DAMPEN_MAGIC = com.mna.effects.EffectInit.DAMPEN_MAGIC;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOAKED = com.mna.effects.EffectInit.SOAKED;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SUNDER = com.mna.effects.EffectInit.SUNDER;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> FRAILTY = com.mna.effects.EffectInit.FRAILTY;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> DISJUNCTION = com.mna.effects.EffectInit.DISJUNCTION;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> TELEKINESIS = com.mna.effects.EffectInit.TELEKINESIS;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WATER_WALKING = com.mna.effects.EffectInit.WATER_WALKING;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SPIDER_CLIMBING = com.mna.effects.EffectInit.SPIDER_CLIMBING;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SOAR = com.mna.effects.EffectInit.SOAR;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LACERATE = com.mna.effects.EffectInit.LACERATE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> WRITHING_BRAMBLES = com.mna.effects.EffectInit.WRITHING_BRAMBLES;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> AURA_OF_FROST = com.mna.effects.EffectInit.AURA_OF_FROST;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> BURNING_RAGE = com.mna.effects.EffectInit.BURNING_RAGE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> REDUCE = com.mna.effects.EffectInit.REDUCE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ENLARGE = com.mna.effects.EffectInit.ENLARGE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> HEATWAVE = com.mna.effects.EffectInit.HEATWAVE;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> CHILL = com.mna.effects.EffectInit.CHILL;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> SNOWBLIND = com.mna.effects.EffectInit.SNOWBLIND;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> LIFT = com.mna.effects.EffectInit.LIFT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> PILGRIM = com.mna.effects.EffectInit.PILGRIM;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> ICARIAN_FLIGHT = com.mna.effects.EffectInit.ICARIAN_FLIGHT;
    public static final DeferredHolder<MobEffect, ? extends MobEffect> COLD_DARK = com.mna.effects.EffectInit.COLD_DARK;
}
