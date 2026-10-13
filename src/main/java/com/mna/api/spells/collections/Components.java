package com.mna.api.spells.collections;

import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;

/**
 * Compat 1.21.1: era @ObjectHolder (removido) -> todos os campos nulos (NPE em jogo).
 * Agora resolve do registry do mod.
 */
public class Components {
    public static final SpellEffect FIRE_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/fire_damage"));
    public static final SpellEffect FROST_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/frost_damage"));
    public static final SpellEffect LIGHTNING_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/lightning_damage"));
    public static final SpellEffect BREAK =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/break"));
    public static final SpellEffect FLING =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/fling"));
    public static final SpellEffect LEAP =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/leap"));
    public static final SpellEffect SHIELD =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/shield"));
    public static final SpellEffect SLOWFALL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/slowfall"));
    public static final SpellEffect HEAL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/heal"));
    public static final SpellEffect RIFT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/rift"));
    public static final SpellEffect TRANSPLACE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/transplace"));
    public static final SpellEffect BLINK =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/blink"));
    public static final SpellEffect GUST =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/gust"));
    public static final SpellEffect INVISIBILITY =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/invisibility"));
    public static final SpellEffect LIGHT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/light"));
    public static final SpellEffect HASTE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/haste"));
    public static final SpellEffect SLOW =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/slow"));
    public static final SpellEffect STRENGTH =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/strength"));
    public static final SpellEffect WEAKNESS =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/weakness"));
    public static final SpellEffect NIGHT_VISION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/night_vision"));
    public static final SpellEffect BLIND =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/blind"));
    public static final SpellEffect SWIFT_SWIM =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/swift_swim"));
    public static final SpellEffect WATERY_GRAVE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/watery_grave"));
    public static final SpellEffect ENTANGLE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/entangle"));
    public static final SpellEffect GRAVITY_WELL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/gravity_well"));
    public static final SpellEffect CHRONO_ANCHOR =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/chrono_anchor"));
    public static final SpellEffect SILENCE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/silence"));
    public static final SpellEffect TRANSITORY_TILE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/transitory_tile"));
    public static final SpellEffect ACTIVATE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/activate"));
    public static final SpellEffect POISON =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/poison"));
    public static final SpellEffect DROWN =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/drown"));
    public static final SpellEffect CAUTERIZE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/cauterize"));
    public static final SpellEffect MAGIC_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/magic_damage"));
    public static final SpellEffect EXPLOSION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/explosion"));
    public static final SpellEffect LIFE_TAP =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/life_tap"));
    public static final SpellEffect FIRE_SHIELD =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/fire_shield"));
    public static final SpellEffect MANA_SHIELD =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/mana_shield"));
    public static final SpellEffect BRIARTHORN_BARRIER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/briarthorn_barrier"));
    public static final SpellEffect ABSORPTION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/absorption"));
    public static final SpellEffect MANA_TRANSFER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/mana_transfer"));
    public static final SpellEffect DECOY =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/decoy"));
    public static final SpellEffect BLOOM =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/bloom"));
    public static final SpellEffect CRYSTALLIZE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/crystallize"));
    public static final SpellEffect COLLAPSE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/collapse"));
    public static final SpellEffect EARTHSKY =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/earthsky"));
    public static final SpellEffect TRUE_INVISIBILITY =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/true_invisibility"));
    public static final SpellEffect ANIMUS =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/animus"));
    public static final SpellEffect CONFUSE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/confuse"));
    public static final SpellEffect MIND_CONTROL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/mind_control"));
    public static final SpellEffect SPECTRAL_CRAFTING_TABLE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spectral_crafting_table"));
    public static final SpellEffect DIVINATION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/divination"));
    public static final SpellEffect MIST_FORM =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/mist_form"));
    public static final SpellEffect RECALL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/recall"));
    public static final SpellEffect ELDRIN_FLIGHT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/eldrin_flight"));
    public static final SpellEffect MIND_VISION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/mind_vision"));
    public static final SpellEffect SUMMON_SKELETAL_HORSE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/summon_skeletal_horse"));
    public static final SpellEffect POSESSION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/posession"));
    public static final SpellEffect GREATER_POSSESSION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/greater_possession"));
    public static final SpellEffect LIVING_BOMB =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/living_bomb"));
    public static final SpellEffect WITHER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/wither"));
    public static final SpellEffect TRANSITORY_TUNNEL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/transitory_tunnel"));
    public static final SpellEffect FELLED_OAK =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/felled_oak"));
    public static final SpellEffect WIND_WALL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/wind_wall"));
    public static final SpellEffect LEVITATION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/levitation"));
    // REMOVIDO no 1.21.1 (sem registro correspondente): FOSSILIZE
    public static final SpellEffect FOSSILIZE = com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/fortification"));
    public static final SpellEffect EXCHANGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/exchange"));
    public static final SpellEffect SHEAR =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/shear"));
    public static final SpellEffect BIND_WOUNDS =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/bind_wounds"));
    public static final SpellEffect FILL_HOLE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/fill_hole"));
    public static final SpellEffect SPECTRAL_ANVIL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spectral_anvil"));
    public static final SpellEffect SPECTRAL_STONECUTTER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spectral_stonecutter"));
    public static final SpellEffect LMNOP =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/lmnop"));
    public static final SpellEffect TRANSMUTE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/transmute"));
    public static final SpellEffect IMPALE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/impale"));
    public static final SpellEffect EARTHQUAKE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/earthquake"));
    public static final SpellEffect REDSTONE_PULSE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/redstone_pulse"));
    public static final SpellEffect PULL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/pull"));
    // REMOVIDO no 1.21.1 (sem registro correspondente): CANNON
    // CANNON: o original tambem nao registrava components/cannon (stub morto, sem usos) - mantido null.
    public static final SpellEffect CANNON = null;
    public static final SpellEffect WIND_SHEAR =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/wind_shear"));
    public static final SpellEffect BACKDRAFT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/backdraft"));
    public static final SpellEffect PURGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/purge"));
    public static final SpellEffect AMPLIFY_MAGIC =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/amplify_magic"));
    public static final SpellEffect DAMPEN_MAGIC =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/dampen_magic"));
    public static final SpellEffect SUNDER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/sunder"));
    public static final SpellEffect FRAILTY =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/frailty"));
    public static final SpellEffect SPLASH =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/splash"));
    public static final SpellEffect SACRIFICE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/sacrifice"));
    public static final SpellEffect SHATTER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/shatter"));
    public static final SpellEffect INSIGHT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/insight"));
    public static final SpellEffect TELEKINESIS =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/telekinesis"));
    public static final SpellEffect DISJUNCTION =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/disjunction"));
    public static final SpellEffect CRUCIBLE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/crucible"));
    public static final SpellEffect ICE_SPIKE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/ice_spike"));
    public static final SpellEffect SPECTRAL_LADDER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spectral_ladder"));
    public static final SpellEffect SPECTRAL_WEB =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spectral_web"));
    public static final SpellEffect WATER_WALKING =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/water_walking"));
    public static final SpellEffect SPIDER_CLIMBING =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spider_climbing"));
    public static final SpellEffect INSECT_SWARM =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/insect_swarm"));
    public static final SpellEffect FLIGHT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/eldrin_flight"));
    public static final SpellEffect LACERATE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/lacerate"));
    public static final SpellEffect PHANTOM_SOUND =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/phantom_sound"));
    public static final SpellEffect ILLUSORY_CREEPER =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/illusory_creeper"));
    public static final SpellEffect PLACE_BLCOK =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/place_block"));
    public static final SpellEffect GREATER_ANIMUS =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/greater_animus"));
    public static final SpellEffect GROW =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/grow"));
    public static final SpellEffect SHRINK =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/shrink"));
    public static final SpellEffect LIFE_DRAIN =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/life_drain"));
    public static final SpellEffect CAMOUFLAGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/camouflage"));
    public static final SpellEffect CHILL =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/chill"));
    public static final SpellEffect HEATWAVE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/heatwave"));
    public static final SpellEffect ICARIAN_FLIGHT =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/icarian_flight"));
    public static final SpellEffect CURSE_OF_AGONY =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/curse_of_agony"));
    public static final SpellEffect BLOOD_SACRIFICE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/blood_sacrifice"));
    public static final SpellEffect SHACKLES_OF_PAIN =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/shackles_of_pain"));
    public static final SpellEffect SENSE_LIFE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/sense_life"));
    public static final SpellEffect SOUL_TRAP =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/soul_trap"));
    public static final SpellEffect HEMMORHAGE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/hemmorhage"));
    public static final SpellEffect BLOOD_TENDRILS =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/blood_tendrils"));
    public static final SpellEffect BLOOD_GOLEM =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/blood_golem"));
    public static final SpellEffect SIMULACRUM =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/simulacrum"));
    public static final SpellEffect SPECTRAL_FENCE =
            com.mna.Registries.SpellEffect.getValue(com.mna.api.tools.RLoc.create("components/spectral_fence"));
}
