package com.mna.api.sound;

import com.mna.api.affinity.Affinity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Compat 1.21.1: no 1.20.1 esta classe usava @ObjectHolder (removido no 1.21.1),
 * deixando TODOS os campos nulos -> NPE em jogo ao tocar qualquer som.
 * Agora os campos sao criados diretamente; onRegisterSounds continua registrando
 * pelo RegisterEvent usando estas mesmas instancias.
 */
public final class SFX {

    public static final class Block {

        public static final SoundEvent FUME_BELLOWS =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:block_fume_bellows"));
    }

    public static final class Entity {

        public static final class Aranai {

            public static final SoundEvent GRUMBLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_aranai"));
        }

        public static final class BloodGolem {

            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_bloodgolem_idle"));
            public static final SoundEvent HIT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_bloodgolem_hit"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_bloodgolem_death"));
            public static final SoundEvent SUMMON =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_bloodgolem_summon"));
        }

        public static final class BloodTendril {

            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_bloodtendril_idle"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_bloodtendril_death"));
        }

        public static final class Broker {

            public static final SoundEvent DEAL =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_broker_deal"));
        }

        public static final class Chatterer {

            public static final SoundEvent CLACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_chatterer_clack"));
        }

        public static final class Construct {

            public static final SoundEvent HORN =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_construct_horn"));
            public static final SoundEvent TAUNT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_construct_taunt"));
            public static final SoundEvent SPRAY_GENERIC =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_construct_spray_generic"));
            public static final SoundEvent SPRAY_FIRE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_construct_spray_fire"));
        }

        public static final class DemonLord {

            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_attack"));
            public static final SoundEvent HIT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_hit"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_idle"));
            public static final SoundEvent DRAW_SWORDS =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_drawswords"));
            public static final SoundEvent SUMMON_STAFF =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_summonstaff"));
            public static final SoundEvent SCREAM =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_scream"));
            public static final SoundEvent LEAP =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_leap"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_demonlord_death"));
        }

        public static final class Generic {

            public static final SoundEvent WOOSH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_generic_woosh"));
        }

        public static final class HulkingZombie {

            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_hulking_zombie_attack"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_hulking_zombie_death"));
            public static final SoundEvent HURT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_hulking_zombie_hurt"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_hulking_zombie_idle"));
            public static final SoundEvent LEAP =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_hulking_zombie_leap"));
            public static final SoundEvent ROAR =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_hulking_zombie_roar"));
        }

        public static final class Imp {

            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_imp_attack"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_imp_death"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_imp_idle"));
            public static final SoundEvent LEAP =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_imp_leap"));
        }

        public static final class LanternWraith {

            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lantern_wraith_death"));
            public static final SoundEvent HURT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lantern_wraith_hurt"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lantern_wraith_idle"));
        }

        public static final class Odin {

            public static final SoundEvent BIFROST =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_odin_bifrost"));
            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_odin_attack"));
            public static final SoundEvent DRAW =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_odin_draw"));
            public static final SoundEvent ENRAGE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_odin_enrage"));
            public static final SoundEvent HIT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_odin_hurt"));
        }

        public static final class Oink {

            public static final SoundEvent OINK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_oink"));
        }

        public static final class Pixie {

            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pixie_death"));
            public static final SoundEvent HURT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pixie_hurt"));
            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pixie_attack"));
        }

        public static final class PumpkinKing {

            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_attack"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_death"));
            public static final SoundEvent HURT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_hit"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_idle"));
            public static final SoundEvent ENTANGLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_entangle"));
            public static final SoundEvent INCINERATE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_incinerate"));
            public static final SoundEvent SPAWN =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_pumpkin_king_spawn"));
        }

        public static final class SkeletonAssassin {

            public static final SoundEvent SHURIKEN_THROW =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_skeleton_assassin_shuriken_throw"));
            public static final SoundEvent SHURIKEN_IMPACT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_skeleton_assassin_shuriken_impact"));
            public static final SoundEvent BOLO_THROW =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_skeleton_assassin_bolo_throw"));
            public static final SoundEvent BOLO_IMPACT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_skeleton_assassin_bolo_impact"));
            public static final SoundEvent SMOKE_BOMB =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_skeleton_assassin_smoke_bomb"));
        }

        public static final class SpectralHorse {

            public static final SoundEvent SUMMON =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_spectral_horse_summon"));
        }

        public static final class Spellbreaker {

            public static final SoundEvent RALLY =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_spellbreaker_rally"));
            public static final SoundEvent SHIELD_BASH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_spellbreaker_shield_bash"));
        }

        public static final class WanderingWizard {

            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_wandering_wizard_death"));
            public static final SoundEvent HIT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_wandering_wizard_hit"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_wandering_wizard_idle"));
            public static final SoundEvent NO =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_wandering_wizard_no"));
            public static final SoundEvent YES =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_wandering_wizard_yes"));
        }

        public static final class Warden {

            public static final SoundEvent ESCAPE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_warden_escape"));
        }

        public static final class WitherLich {

            public static final SoundEvent ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_attack"));
            public static final SoundEvent HIT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_hit"));
            public static final SoundEvent IDLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_idle"));
            public static final SoundEvent BREATH_ATTACK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_breathattack"));
            public static final SoundEvent CAST =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_cast"));
            public static final SoundEvent DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_death"));
            public static final SoundEvent SUMMON =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_summon"));
            public static final SoundEvent PW_DEATH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:entity_lich_pwdeath"));
        }

    }

    public static final class Event {

        public static final class AltarOfArcana {

            public static final SoundEvent START =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_altar_of_arcana_start"));
            public static final SoundEvent COMPLETE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_altar_of_arcana_complete"));
        }

        public static final class Artifact {

            public static final SoundEvent DEMON_ARMOR_SPRINT_START =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_demon_armor_sprint_start"));
            public static final SoundEvent METEOR_JUMP =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_meteor_jump"));
        }

        public static final class Block {

            public static final SoundEvent ARCANE_SENTRY_CHARGE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_arcane_sentry_charge"));
            public static final SoundEvent MANAWEAVE_ALTAR_CRAFT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_manaweave_craft"));
            public static final SoundEvent MANAWEAVE_CACHE_OPEN =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_manaweave_cache_open"));
            public static final SoundEvent GANTRY_SUMMON =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_gantry_summon"));
            public static final SoundEvent SAW_CUT =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_saw_cut"));
        }

        public static final class Eldrin {

            public static final SoundEvent DRAW_IN_ITEM =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_eldrin_altar_item_inhale"));
            public static final SoundEvent CRAFT_COMPLETE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_eldrin_altar_complete"));
        }

        public static final class Faction {

            public static final SoundEvent FACTION_RAID_DEMONS =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faction_raid_demons"));
            public static final SoundEvent FACTION_RAID_FEY =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faction_raid_fey"));
            public static final SoundEvent FACTION_RAID_COUNCIL =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faction_raid_council"));
            public static final SoundEvent FACTION_HORN_COUNCIL =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_summon_ally_council"));
            public static final SoundEvent FACTION_HORN_DEMONS =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_summon_ally_demons"));
            public static final SoundEvent FACTION_HORN_FEY =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_summon_ally_fey"));
            public static final SoundEvent FACTION_HORN_UNDEAD =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_summon_ally_undead"));
        }

        public static final class Player {

            public static final SoundEvent MAGIC_UNLOCKED =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_magic_unlocked"));
            public static final SoundEvent MAGIC_LEVEL_UP =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_magic_level_up"));
            public static final SoundEvent SPELL_CREATED =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_spell_created"));
            public static final SoundEvent MANAWEAVE_PATTERN_MATCH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_manaweave_pattern_match"));
            public static final SoundEvent WTF_BOOM =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_wtfboom"));
            public static final SoundEvent WING_FLAP =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_flap"));
        }

        public static final class Ritual {

            public static final SoundEvent DEMON_SUMMON_END =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_demon_summon_end"));
            public static final SoundEvent DEMON_SUMMON_CHANNEL =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_demon_summon_channel"));
            public static final SoundEvent FAERIE_BLOW_KISS =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faerie_blow_kiss"));
            public static final SoundEvent FAERIE_SUMMON =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faerie_summon"));
            public static final SoundEvent FAERIE_GIGGLE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faerie_giggle"));
            public static final SoundEvent FAERIE_LEAVE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faerie_leave"));
            public static final SoundEvent FAERIE_IMBUE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_faerie_imbue"));
            public static final SoundEvent ANCIENT_SUMMON =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ancient_summon"));
            public static final SoundEvent ANCIENT_IMBUE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ancient_imbue"));
            public static final SoundEvent ANCIENT_IMBUE_1 =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ancient_imbue_1"));
            public static final SoundEvent ANCIENT_IMBUE_2 =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ancient_imbue_2"));
            public static final SoundEvent ANCIENT_IMBUE_3 =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ancient_imbue_3"));
            public static final SoundEvent COLD_DARK =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_cold_dark"));
            public static final SoundEvent IRON_BELL =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_iron_bell"));
        }

    }

    public static final class Gui {

        public static final SoundEvent PAGE_FLIP =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_page_flip"));
        public static final SoundEvent CHISEL =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_chisel"));
        public static final SoundEvent PENCIL_WRITE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_pencil_write"));
        public static final SoundEvent CHARCOAL_SCRIBBLE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_charcoal_scribble"));
        public static final SoundEvent BOOK_OPEN =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_book_open"));
        public static final SoundEvent BOOK_CLOSE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_book_close"));
        public static final SoundEvent PACK_OPEN =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_pack_open"));
        public static final SoundEvent PACK_CLOSE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:gui_pack_close"));
    }

    public static final class Loops {

        public static final SoundEvent AIR =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_air"));
        public static final SoundEvent ARCANE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_arcane"));
        public static final SoundEvent EARTH =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_earth"));
        public static final SoundEvent EARTH_RUMBLE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_earth_rumble"));
        public static final SoundEvent ENDER =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_ender"));
        public static final SoundEvent FIRE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_fire"));
        public static final SoundEvent LIGHTNING =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_lightning"));
        public static final SoundEvent WATER =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_water"));
        public static final SoundEvent ICE =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_ice"));
        public static final SoundEvent BLOOD =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_blood"));
        public static final SoundEvent MANAWEAVING =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_manaweaving"));
        public static final SoundEvent DEMON_SUMMON =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_demon_summon"));
        public static final SoundEvent ELDRIN_ALTAR =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_eldrin_altar"));
        public static final SoundEvent SHADOW_COPY =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_shadow_copy"));
        public static final SoundEvent ALTAR_OF_ARCANA =
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:loop_altar_of_arcana"));
    }

    public static final class Ritual {

        public static final class Effects {

            public static final SoundEvent RITUAL_STARTED =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ritual_started"));
            public static final SoundEvent RITUAL_POINT_APPEAR =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:event_ritual_point_appear"));
        }

    }

    public static final class Spell {

        public static final class Buff {

            public static final SoundEvent ARCANE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_arcane"));
            public static final SoundEvent EARTH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_earth"));
            public static final SoundEvent ENDER =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_ender"));
            public static final SoundEvent FIRE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_fire"));
            public static final SoundEvent ICE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_ice"));
            public static final SoundEvent WATER =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_water"));
            public static final SoundEvent WIND =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_wind"));
            public static final SoundEvent BLOOD =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:buff_blood"));
        }

        public static final class Cast {

            public static final SoundEvent ARCANE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_arcane"));
            public static final SoundEvent BRIAN =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_brian"));
            public static final SoundEvent BRIAN_2 =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_brian_2"));
            public static final SoundEvent EARTH =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_earth"));
            public static final SoundEvent ENDER =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_ender"));
            public static final SoundEvent FIRE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_fire"));
            public static final SoundEvent ICE =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_ice"));
            public static final SoundEvent LIGHTNING =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_lightning"));
            public static final SoundEvent WATER =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_water"));
            public static final SoundEvent WIND =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_wind"));
            public static final SoundEvent KABLAM =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_kablam"));
            public static final SoundEvent BLOOD =
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:cast_blood"));

            public static SoundEvent ForAffinity(Affinity aff) {
            switch (aff) {
            case ARCANE:
            return ARCANE;
            case EARTH:
            return EARTH;
            case ENDER:
            return ENDER;
            case FIRE:
            return FIRE;
            case WATER:
            return WATER;
            case WIND:
            return WIND;
            case LIGHTNING:
            return LIGHTNING;
            case ICE:
            return ICE;
            case BLOOD:
            return BLOOD;
            case UNKNOWN:
            default:
            return ARCANE;
            }
        }
        }

        public static final class Impact {

            public static final class AoE {

                public static final SoundEvent ARCANE =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_arcane_aoe"));
                public static final SoundEvent EARTH =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_earth_aoe"));
                public static final SoundEvent FIRE =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_fire_aoe"));
                public static final SoundEvent WIND =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_wind_aoe"));
                public static final SoundEvent LIGHTNING =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_lightning_aoe"));
                public static final SoundEvent BLOOD =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_blood_aoe"));
            }

            public static final class Single {

                public static final SoundEvent ARCANE =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_arcane"));
                public static final SoundEvent EARTH =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_earth"));
                public static final SoundEvent ENDER =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_ender"));
                public static final SoundEvent FIRE =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_fire"));
                public static final SoundEvent LIGHTNING =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_lightning"));
                public static final SoundEvent ICE =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_ice"));
                public static final SoundEvent WATER =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_water"));
                public static final SoundEvent WIND =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_wind"));
                public static final SoundEvent BLOOD =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_blood"));
                public static final SoundEvent CURSE_OF_AGONY =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_curse_of_agony"));
                public static final SoundEvent SOUL_TRAP =
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse("mna:impact_soul_trap"));
            }

        }

    }
}
