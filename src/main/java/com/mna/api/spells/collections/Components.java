package com.mna.api.spells.collections;

import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;

/**
 * Compat 1.21.1: era @ObjectHolder (removido) -> todos os campos nulos (NPE em jogo).
 * Agora resolve do registry do mod.
 */
public class Components {
    public static final SpellEffect FIRE_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/fire_damage"));
    public static final SpellEffect FROST_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/frost_damage"));
    public static final SpellEffect LIGHTNING_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/lightning_damage"));
    public static final SpellEffect BREAK =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/break"));
    public static final SpellEffect FLING =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/fling"));
    public static final SpellEffect LEAP =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/leap"));
    public static final SpellEffect SHIELD =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/shield"));
    public static final SpellEffect SLOWFALL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/slowfall"));
    public static final SpellEffect HEAL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/heal"));
    public static final SpellEffect RIFT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/rift"));
    public static final SpellEffect TRANSPLACE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/transplace"));
    public static final SpellEffect BLINK =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/blink"));
    public static final SpellEffect GUST =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/gust"));
    public static final SpellEffect INVISIBILITY =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/invisibility"));
    public static final SpellEffect LIGHT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/light"));
    public static final SpellEffect HASTE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/haste"));
    public static final SpellEffect SLOW =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/slow"));
    public static final SpellEffect STRENGTH =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/strength"));
    public static final SpellEffect WEAKNESS =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/weakness"));
    public static final SpellEffect NIGHT_VISION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/night_vision"));
    public static final SpellEffect BLIND =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/blind"));
    public static final SpellEffect SWIFT_SWIM =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/swift_swim"));
    public static final SpellEffect WATERY_GRAVE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/watery_grave"));
    public static final SpellEffect ENTANGLE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/entangle"));
    public static final SpellEffect GRAVITY_WELL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/gravity_well"));
    public static final SpellEffect CHRONO_ANCHOR =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/chrono_anchor"));
    public static final SpellEffect SILENCE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/silence"));
    public static final SpellEffect TRANSITORY_TILE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/transitory_tile"));
    public static final SpellEffect ACTIVATE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/activate"));
    public static final SpellEffect POISON =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/poison"));
    public static final SpellEffect DROWN =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/drown"));
    public static final SpellEffect CAUTERIZE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/cauterize"));
    public static final SpellEffect MAGIC_DAMAGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/magic_damage"));
    public static final SpellEffect EXPLOSION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/explosion"));
    public static final SpellEffect LIFE_TAP =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/life_tap"));
    public static final SpellEffect FIRE_SHIELD =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/fire_shield"));
    public static final SpellEffect MANA_SHIELD =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/mana_shield"));
    public static final SpellEffect BRIARTHORN_BARRIER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/briarthorn_barrier"));
    public static final SpellEffect ABSORPTION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/absorption"));
    public static final SpellEffect MANA_TRANSFER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/mana_transfer"));
    public static final SpellEffect DECOY =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/decoy"));
    public static final SpellEffect BLOOM =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/bloom"));
    public static final SpellEffect CRYSTALLIZE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/crystallize"));
    public static final SpellEffect COLLAPSE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/collapse"));
    public static final SpellEffect EARTHSKY =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/earthsky"));
    public static final SpellEffect TRUE_INVISIBILITY =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/true_invisibility"));
    public static final SpellEffect ANIMUS =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/animus"));
    public static final SpellEffect CONFUSE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/confuse"));
    public static final SpellEffect MIND_CONTROL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/mind_control"));
    public static final SpellEffect SPECTRAL_CRAFTING_TABLE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spectral_crafting_table"));
    public static final SpellEffect DIVINATION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/divination"));
    public static final SpellEffect MIST_FORM =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/mist_form"));
    public static final SpellEffect RECALL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/recall"));
    public static final SpellEffect ELDRIN_FLIGHT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/eldrin_flight"));
    public static final SpellEffect MIND_VISION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/mind_vision"));
    public static final SpellEffect SUMMON_SKELETAL_HORSE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/summon_skeletal_horse"));
    public static final SpellEffect POSESSION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/posession"));
    public static final SpellEffect GREATER_POSSESSION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/greater_possession"));
    public static final SpellEffect LIVING_BOMB =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/living_bomb"));
    public static final SpellEffect WITHER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/wither"));
    public static final SpellEffect TRANSITORY_TUNNEL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/transitory_tunnel"));
    public static final SpellEffect FELLED_OAK =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/felled_oak"));
    public static final SpellEffect WIND_WALL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/wind_wall"));
    public static final SpellEffect LEVITATION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/levitation"));
    // REMOVIDO no 1.21.1 (sem registro correspondente): FOSSILIZE
    public static final SpellEffect FOSSILIZE = null;
    public static final SpellEffect EXCHANGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/exchange"));
    public static final SpellEffect SHEAR =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/shear"));
    public static final SpellEffect BIND_WOUNDS =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/bind_wounds"));
    public static final SpellEffect FILL_HOLE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/fill_hole"));
    public static final SpellEffect SPECTRAL_ANVIL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spectral_anvil"));
    public static final SpellEffect SPECTRAL_STONECUTTER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spectral_stonecutter"));
    public static final SpellEffect LMNOP =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/lmnop"));
    public static final SpellEffect TRANSMUTE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/transmute"));
    public static final SpellEffect IMPALE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/impale"));
    public static final SpellEffect EARTHQUAKE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/earthquake"));
    public static final SpellEffect REDSTONE_PULSE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/redstone_pulse"));
    public static final SpellEffect PULL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/pull"));
    // REMOVIDO no 1.21.1 (sem registro correspondente): CANNON
    public static final SpellEffect CANNON = null;
    public static final SpellEffect WIND_SHEAR =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/wind_shear"));
    public static final SpellEffect BACKDRAFT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/backdraft"));
    public static final SpellEffect PURGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/purge"));
    public static final SpellEffect AMPLIFY_MAGIC =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/amplify_magic"));
    public static final SpellEffect DAMPEN_MAGIC =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/dampen_magic"));
    public static final SpellEffect SUNDER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/sunder"));
    public static final SpellEffect FRAILTY =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/frailty"));
    public static final SpellEffect SPLASH =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/splash"));
    public static final SpellEffect SACRIFICE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/sacrifice"));
    public static final SpellEffect SHATTER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/shatter"));
    public static final SpellEffect INSIGHT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/insight"));
    public static final SpellEffect TELEKINESIS =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/telekinesis"));
    public static final SpellEffect DISJUNCTION =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/disjunction"));
    public static final SpellEffect CRUCIBLE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/crucible"));
    public static final SpellEffect ICE_SPIKE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/ice_spike"));
    public static final SpellEffect SPECTRAL_LADDER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spectral_ladder"));
    public static final SpellEffect SPECTRAL_WEB =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spectral_web"));
    public static final SpellEffect WATER_WALKING =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/water_walking"));
    public static final SpellEffect SPIDER_CLIMBING =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spider_climbing"));
    public static final SpellEffect INSECT_SWARM =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/insect_swarm"));
    public static final SpellEffect FLIGHT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/eldrin_flight"));
    public static final SpellEffect LACERATE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/lacerate"));
    public static final SpellEffect PHANTOM_SOUND =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/phantom_sound"));
    public static final SpellEffect ILLUSORY_CREEPER =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/illusory_creeper"));
    public static final SpellEffect PLACE_BLCOK =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/place_block"));
    public static final SpellEffect GREATER_ANIMUS =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/greater_animus"));
    public static final SpellEffect GROW =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/grow"));
    public static final SpellEffect SHRINK =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/shrink"));
    public static final SpellEffect LIFE_DRAIN =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/life_drain"));
    public static final SpellEffect CAMOUFLAGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/camouflage"));
    public static final SpellEffect CHILL =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/chill"));
    public static final SpellEffect HEATWAVE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/heatwave"));
    public static final SpellEffect ICARIAN_FLIGHT =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/icarian_flight"));
    public static final SpellEffect CURSE_OF_AGONY =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/curse_of_agony"));
    public static final SpellEffect BLOOD_SACRIFICE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/blood_sacrifice"));
    public static final SpellEffect SHACKLES_OF_PAIN =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/shackles_of_pain"));
    public static final SpellEffect SENSE_LIFE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/sense_life"));
    public static final SpellEffect SOUL_TRAP =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/soul_trap"));
    public static final SpellEffect HEMMORHAGE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/hemmorhage"));
    public static final SpellEffect BLOOD_TENDRILS =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/blood_tendrils"));
    public static final SpellEffect BLOOD_GOLEM =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/blood_golem"));
    public static final SpellEffect SIMULACRUM =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/simulacrum"));
    public static final SpellEffect SPECTRAL_FENCE =
            com.mna.Registries.SpellEffect.getValue(ResourceLocation.parse("components/spectral_fence"));
}
