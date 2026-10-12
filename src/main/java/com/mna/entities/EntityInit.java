/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.SpawnPlacements$Type
 *  net.minecraft.world.entity.animal.Cow
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
 *  net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent
 *  net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent$Operation
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegisterEvent
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.entities;

import com.mna.api.entities.FactionRaidRegistry;
import com.mna.api.entities.IFactionEnemy;
import com.mna.entities.LivingUtilityEntity;
import com.mna.entities.WandClone;
import com.mna.entities.boss.CouncilWarden;
import com.mna.entities.boss.DemonLord;
import com.mna.entities.boss.FaerieQueen;
import com.mna.entities.boss.Odin;
import com.mna.entities.boss.PigDragon;
import com.mna.entities.boss.PumpkinKing;
import com.mna.entities.boss.WitherLich;
import com.mna.entities.boss.attacks.IllusionCreeper;
import com.mna.entities.boss.attacks.OrangeChickenProjectile;
import com.mna.entities.boss.attacks.PumpkinKingEntangle;
import com.mna.entities.boss.attacks.PumpkinKingIncinerate;
import com.mna.entities.boss.attacks.ThrownAllfatherAxe;
import com.mna.entities.boss.attacks.ThrownWeapon;
import com.mna.entities.boss.effects.Bifrost;
import com.mna.entities.boss.effects.DemonReturnPortal;
import com.mna.entities.constructs.BubbleBoat;
import com.mna.entities.constructs.ConstructAssemblyStand;
import com.mna.entities.constructs.MagicBroom;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.faction.Barkling;
import com.mna.entities.faction.Broker;
import com.mna.entities.faction.Chatterer;
import com.mna.entities.faction.Deathcap;
import com.mna.entities.faction.DemonImp;
import com.mna.entities.faction.HulkingZombie;
import com.mna.entities.faction.LanternWraith;
import com.mna.entities.faction.LivingWard;
import com.mna.entities.faction.Pixie;
import com.mna.entities.faction.SkeletonAssassin;
import com.mna.entities.faction.Spellbreaker;
import com.mna.entities.faction.WitchHunter;
import com.mna.entities.faction.util.FactionRaid;
import com.mna.entities.faction.util.FactionWar;
import com.mna.entities.faction.util.WitchHunterArrow;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.entities.manaweaving.ThrownManaweaveBottle;
import com.mna.entities.passive.Magmoo;
import com.mna.entities.projectile.SentryProjectile;
import com.mna.entities.projectile.SkeletonAssassinBolo;
import com.mna.entities.projectile.SkeletonAssassinShuriken;
import com.mna.entities.projectile.WitchhunterTrickshot;
import com.mna.entities.rituals.AncientCouncil;
import com.mna.entities.rituals.DemonStone;
import com.mna.entities.rituals.FeyLight;
import com.mna.entities.rituals.FlatLands;
import com.mna.entities.rituals.FlatLandsProjectile;
import com.mna.entities.rituals.Portal;
import com.mna.entities.rituals.Ritual;
import com.mna.entities.rituals.TimeChangeBall;
import com.mna.entities.sorcery.AffinityIcon;
import com.mna.entities.sorcery.EntityDecoy;
import com.mna.entities.sorcery.Rift;
import com.mna.entities.sorcery.targeting.BoundBowProjectile;
import com.mna.entities.sorcery.targeting.Smite;
import com.mna.entities.sorcery.targeting.SpellBeam;
import com.mna.entities.sorcery.targeting.SpellCone;
import com.mna.entities.sorcery.targeting.SpellEmanation;
import com.mna.entities.sorcery.targeting.SpellEmber;
import com.mna.entities.sorcery.targeting.SpellFissure;
import com.mna.entities.sorcery.targeting.SpellFocus;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.entities.sorcery.targeting.SpellSigil;
import com.mna.entities.sorcery.targeting.SpellWall;
import com.mna.entities.sorcery.targeting.SpellWave;
import com.mna.entities.sorcery.targeting.SpellWrath;
import com.mna.entities.summon.AnimusBlock;
import com.mna.entities.summon.BloodGolem;
import com.mna.entities.summon.BloodTendril;
import com.mna.entities.summon.GreaterAnimus;
import com.mna.entities.summon.InsectSwarm;
import com.mna.entities.summon.Simulacrum;
import com.mna.entities.summon.SummonedSpectralHorse;
import com.mna.entities.utility.DisplayDamage;
import com.mna.entities.utility.DisplayReagents;
import com.mna.entities.utility.EldrinFlight;
import com.mna.entities.utility.FillHole;
import com.mna.entities.utility.ImpulseProjectile;
import com.mna.entities.utility.PresentItem;
import com.mna.entities.utility.RisingBlock;
import com.mna.entities.utility.SpellFX;
import com.mna.entities.utility.StationaryBlock;
import com.mna.entities.utility.ThrownRunescribingPattern;
import com.mna.entities.utility.WanderingWizard;
import com.mna.factions.Factions;
import com.mna.items.spawn_eggs.ModSpawnEggItem;
import java.util.HashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "mna");
    public static final DeferredHolder<EntityType<?>, EntityType<Ritual>> RITUAL_ENTITY = ENTITY_TYPES.register("ritual_entity", () -> EntityType.Builder.<Ritual>of(Ritual::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).build("mna:ritual_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<Portal>> PORTAL_ENTITY = ENTITY_TYPES.register("portal_entity", () -> EntityType.Builder.<Portal>of(Portal::new, (MobCategory)MobCategory.MISC).sized(1.0f, 2.0f).build("mna:portal_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<DisplayReagents>> REAGENT_ENTITY = ENTITY_TYPES.register("reagent_entity", () -> EntityType.Builder.<DisplayReagents>of(DisplayReagents::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:reagent_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<TimeChangeBall>> STARBALL_ENTITY = ENTITY_TYPES.register("starball_entity", () -> EntityType.Builder.<TimeChangeBall>of(TimeChangeBall::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:starball_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<Manaweave>> MANAWEAVE_ENTITY = ENTITY_TYPES.register("manaweave_entity", () -> EntityType.Builder.<Manaweave>of(Manaweave::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:manaweave_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellProjectile>> SPELL_PROJECTILE = ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<OrangeChickenProjectile>> ORANGE_CHICKEN = ENTITY_TYPES.register("orange_chicken", () -> EntityType.Builder.<OrangeChickenProjectile>of(OrangeChickenProjectile::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:orange_chicken"));
    public static final DeferredHolder<EntityType<?>, EntityType<IllusionCreeper>> ILLUSION_CREEPER = ENTITY_TYPES.register("illusion_creeper", () -> EntityType.Builder.<IllusionCreeper>of(IllusionCreeper::new, (MobCategory)MobCategory.MONSTER).sized(0.6f, 1.7f).clientTrackingRange(8).build("mna:illusion_creeper"));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownAllfatherAxe>> ALLFATHER_AXE = ENTITY_TYPES.register("allfather_axe_thrown", () -> EntityType.Builder.<ThrownAllfatherAxe>of(ThrownAllfatherAxe::new, (MobCategory)MobCategory.MISC).sized(1.75f, 0.25f).build("mna:allfather_axe_thrown"));
    public static final DeferredHolder<EntityType<?>, EntityType<Smite>> SMITE_PROJECTILE = ENTITY_TYPES.register("smite_projectile", () -> EntityType.Builder.<Smite>of(Smite::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:smite_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SentryProjectile>> SENTRY_PROJECTILE = ENTITY_TYPES.register("sentry_projectile", () -> EntityType.Builder.<SentryProjectile>of(SentryProjectile::new, (MobCategory)MobCategory.MISC).sized(0.05f, 0.05f).build("mna:sentry_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellFX>> SPELL_FX = ENTITY_TYPES.register("spell_fx", () -> EntityType.Builder.<SpellFX>of(SpellFX::new, (MobCategory)MobCategory.MISC).sized(0.0f, 0.0f).build("mna:spell_fx"));
    public static final DeferredHolder<EntityType<?>, EntityType<DisplayDamage>> DAMAGE_NUMBERS = ENTITY_TYPES.register("damage_numbers", () -> EntityType.Builder.<DisplayDamage>of(DisplayDamage::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:damage_numbers"));
    public static final DeferredHolder<EntityType<?>, EntityType<Rift>> RIFT = ENTITY_TYPES.register("rift", () -> EntityType.Builder.<Rift>of(Rift::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:rift"));
    public static final DeferredHolder<EntityType<?>, EntityType<MagicBroom>> MAGIC_BROOM = ENTITY_TYPES.register("magic_broom", () -> EntityType.Builder.<MagicBroom>of(MagicBroom::new, (MobCategory)MobCategory.MISC).sized(0.7f, 0.7f).build("mna:magic_broom"));
    public static final DeferredHolder<EntityType<?>, EntityType<BubbleBoat>> BUBBLE_BOAT = ENTITY_TYPES.register("bubble_boat", () -> EntityType.Builder.<BubbleBoat>of(BubbleBoat::new, (MobCategory)MobCategory.MISC).sized(1.375f, 0.5625f).build("mna:bubble_boat"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlatLandsProjectile>> FLAT_LANDS_PROJECTILE = ENTITY_TYPES.register("flat_lands_projectile", () -> EntityType.Builder.<FlatLandsProjectile>of(FlatLandsProjectile::new, (MobCategory)MobCategory.MISC).sized(0.2f, 0.2f).build("mna:flat_lands_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlatLands>> FLAT_LANDS = ENTITY_TYPES.register("flat_lands", () -> EntityType.Builder.<FlatLands>of(FlatLands::new, (MobCategory)MobCategory.MISC).sized(0.2f, 0.2f).build("mna:flat_lands"));
    public static final DeferredHolder<EntityType<?>, EntityType<Construct>> ANIMATED_CONSTRUCT = ENTITY_TYPES.register("animated_construct", () -> EntityType.Builder.<Construct>of(Construct::new, (MobCategory)MobCategory.MISC).sized(0.6f, 1.75f).build("mna:animated_construct"));
    public static final DeferredHolder<EntityType<?>, EntityType<ConstructAssemblyStand>> CONSTRUCT_ASSEMBLY_STAND = ENTITY_TYPES.register("construct_assembly_stand", () -> EntityType.Builder.<ConstructAssemblyStand>of(ConstructAssemblyStand::new, (MobCategory)MobCategory.MISC).sized(0.5f, 1.975f).clientTrackingRange(10).build("mna:construct_assembly_stand"));
    public static final DeferredHolder<EntityType<?>, EntityType<DemonStone>> DEMON_STONE = ENTITY_TYPES.register("demon_stone", () -> EntityType.Builder.<DemonStone>of(DemonStone::new, (MobCategory)MobCategory.MISC).sized(5.0f, 5.0f).build("mna:demon_stone"));
    public static final DeferredHolder<EntityType<?>, EntityType<DemonLord>> DEMON_LORD = ENTITY_TYPES.register("demon_lord", () -> EntityType.Builder.<DemonLord>of(DemonLord::new, (MobCategory)MobCategory.MONSTER).sized(3.0f, 5.0f).build("mna:demon_lord"));
    public static final DeferredHolder<EntityType<?>, EntityType<FeyLight>> FEY_LIGHT = ENTITY_TYPES.register("faerie_light", () -> EntityType.Builder.<FeyLight>of(FeyLight::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).build("mna:faerie_light"));
    public static final DeferredHolder<EntityType<?>, EntityType<FaerieQueen>> FAERIE_QUEEN = ENTITY_TYPES.register("faerie_queen", () -> EntityType.Builder.<FaerieQueen>of(FaerieQueen::new, (MobCategory)MobCategory.MISC).sized(1.0f, 2.0f).build("mna:faerie_queen"));
    public static final DeferredHolder<EntityType<?>, EntityType<AncientCouncil>> ANCIENT_COUNCIL = ENTITY_TYPES.register("ancient_council", () -> EntityType.Builder.<AncientCouncil>of(AncientCouncil::new, (MobCategory)MobCategory.MISC).sized(1.0f, 2.0f).build("mna:ancient_council"));
    public static final DeferredHolder<EntityType<?>, EntityType<CouncilWarden>> COUNCIL_WARDEN = ENTITY_TYPES.register("council_warden", () -> EntityType.Builder.<CouncilWarden>of(CouncilWarden::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:council_warden"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellWall>> SPELL_WALL = ENTITY_TYPES.register("spell_entity_wall", () -> EntityType.Builder.<SpellWall>of(SpellWall::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:spell_entity_wall"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellWave>> SPELL_WAVE = ENTITY_TYPES.register("spell_entity_wave", () -> EntityType.Builder.<SpellWave>of(SpellWave::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:spell_entity_wave"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellFissure>> SPELL_FISSURE = ENTITY_TYPES.register("spell_entity_fissure", () -> EntityType.Builder.<SpellFissure>of(SpellFissure::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:spell_entity_fissure"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellEmber>> SPELL_EMBER = ENTITY_TYPES.register("spell_entity_ember", () -> EntityType.Builder.<SpellEmber>of(SpellEmber::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_entity_ember"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellFocus>> SPELL_FOCUS = ENTITY_TYPES.register("spell_entity_focus", () -> EntityType.Builder.<SpellFocus>of(SpellFocus::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_entity_focus"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellEmanation>> SPELL_EMANATION = ENTITY_TYPES.register("spell_entity_emanation", () -> EntityType.Builder.<SpellEmanation>of(SpellEmanation::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_entity_emanation"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellCone>> SPELL_CONE = ENTITY_TYPES.register("spell_entity_cone", () -> EntityType.Builder.<SpellCone>of(SpellCone::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_entity_cone"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellWrath>> SPELL_WRATH = ENTITY_TYPES.register("spell_entity_rain", () -> EntityType.Builder.<SpellWrath>of(SpellWrath::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_entity_rain"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellBeam>> SPELL_BEAM = ENTITY_TYPES.register("spell_entity_beam", () -> EntityType.Builder.<SpellBeam>of(SpellBeam::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:spell_entity_beam"));
    public static final DeferredHolder<EntityType<?>, EntityType<WandClone>> WAND_CLONE = ENTITY_TYPES.register("wand_clone", () -> EntityType.Builder.<WandClone>of(WandClone::new, (MobCategory)MobCategory.MISC).sized(0.2f, 0.2f).build("mna:wand_clone"));
    public static final DeferredHolder<EntityType<?>, EntityType<AffinityIcon>> AFFINITY_ICON = ENTITY_TYPES.register("affinity_icon_entity", () -> EntityType.Builder.<AffinityIcon>of(AffinityIcon::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:affinity_icon_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<Deathcap>> MUSHROOM_SOLDIER = ENTITY_TYPES.register("mushroom_soldier", () -> EntityType.Builder.<Deathcap>of(Deathcap::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 1.25f).build("mna:mushroom_soldier"));
    public static final DeferredHolder<EntityType<?>, EntityType<Pixie>> PIXIE = ENTITY_TYPES.register("pixie", () -> EntityType.Builder.<Pixie>of(Pixie::new, (MobCategory)MobCategory.MONSTER).sized(0.4f, 0.5f).build("mna:pixie"));
    public static final DeferredHolder<EntityType<?>, EntityType<EldrinFlight>> ELDRIN_FLIGHT = ENTITY_TYPES.register("eldrin_flight", () -> EntityType.Builder.<EldrinFlight>of(EldrinFlight::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:eldrin_flight"));
    public static final DeferredHolder<EntityType<?>, EntityType<DemonImp>> DEMON_IMP = ENTITY_TYPES.register("demon_imp", () -> EntityType.Builder.<DemonImp>of(DemonImp::new, (MobCategory)MobCategory.MONSTER).sized(0.75f, 1.5f).build("mna:demon_imp"));
    public static final DeferredHolder<EntityType<?>, EntityType<Barkling>> BARKLING = ENTITY_TYPES.register("barkling", () -> EntityType.Builder.<Barkling>of(Barkling::new, (MobCategory)MobCategory.MONSTER).sized(0.75f, 2.0f).build("mna:barkling"));
    public static final DeferredHolder<EntityType<?>, EntityType<LanternWraith>> LANTERN_WRAITH = ENTITY_TYPES.register("lantern_wraith", () -> EntityType.Builder.<LanternWraith>of(LanternWraith::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 1.5f).build("mna:lantern_wraith"));
    public static final DeferredHolder<EntityType<?>, EntityType<Spellbreaker>> SPELLBREAKER = ENTITY_TYPES.register("spell_breaker", () -> EntityType.Builder.<Spellbreaker>of(Spellbreaker::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:spell_breaker"));
    public static final DeferredHolder<EntityType<?>, EntityType<LivingWard>> LIVING_WARD = ENTITY_TYPES.register("living_ward", () -> EntityType.Builder.<LivingWard>of(LivingWard::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 1.0f).build("mna:living_ward"));
    public static final DeferredHolder<EntityType<?>, EntityType<WitchHunter>> WITCH_HUNTER = ENTITY_TYPES.register("witch_hunter", () -> EntityType.Builder.<WitchHunter>of(WitchHunter::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:witch_hunter"));
    public static final DeferredHolder<EntityType<?>, EntityType<HulkingZombie>> HULKING_ZOMBIE = ENTITY_TYPES.register("hulking_zombie", () -> EntityType.Builder.<HulkingZombie>of(HulkingZombie::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:hulking_zombie"));
    public static final DeferredHolder<EntityType<?>, EntityType<SkeletonAssassin>> SKELETON_ASSASSIN = ENTITY_TYPES.register("skeleton_assassin", () -> EntityType.Builder.<SkeletonAssassin>of(SkeletonAssassin::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:skeleton_assassin"));
    public static final DeferredHolder<EntityType<?>, EntityType<Chatterer>> CHATTERER = ENTITY_TYPES.register("chatterer", () -> EntityType.Builder.<Chatterer>of(Chatterer::new, (MobCategory)MobCategory.MONSTER).sized(0.5f, 0.5f).build("mna:chatterer"));
    public static final DeferredHolder<EntityType<?>, EntityType<SpellSigil>> SPELL_RUNE = ENTITY_TYPES.register("spell_rune_entity", () -> EntityType.Builder.<SpellSigil>of(SpellSigil::new, (MobCategory)MobCategory.MISC).sized(1.0f, 0.1f).build("mna:spell_rune_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<WitchhunterTrickshot>> WITCH_HUNTER_TRICKSHOT = ENTITY_TYPES.register("witch_hunter_trickshot", () -> EntityType.Builder.<WitchhunterTrickshot>of(WitchhunterTrickshot::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:witch_hunter_trickshot"));
    public static final DeferredHolder<EntityType<?>, EntityType<WitchHunterArrow>> WITCH_HUNTER_ARROW = ENTITY_TYPES.register("witch_hunter_arrow", () -> EntityType.Builder.<WitchHunterArrow>of(WitchHunterArrow::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("mna:witch_hunter_arrow"));
    public static final DeferredHolder<EntityType<?>, EntityType<FactionRaid>> FACTION_RAID_ENTITY = ENTITY_TYPES.register("faction_raid", () -> EntityType.Builder.<FactionRaid>of(FactionRaid::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:faction_raid"));
    public static final DeferredHolder<EntityType<?>, EntityType<FactionWar>> FACTION_WAR = ENTITY_TYPES.register("faction_war", () -> EntityType.Builder.<FactionWar>of(FactionWar::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 1.0f).build("mna:faction_war"));
    public static final DeferredHolder<EntityType<?>, EntityType<PresentItem>> PRESENTATION_ENTITY = ENTITY_TYPES.register("presentation_entity", () -> EntityType.Builder.<PresentItem>of(PresentItem::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:presentation_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityDecoy>> DECOY_ENTITY = ENTITY_TYPES.register("decoy_entity", () -> EntityType.Builder.<EntityDecoy>of(EntityDecoy::new, (MobCategory)MobCategory.MISC).sized(0.6f, 1.8f).build("mna:decoy_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<AnimusBlock>> ANIMUS_BLOCK = ENTITY_TYPES.register("animus_block", () -> EntityType.Builder.<AnimusBlock>of(AnimusBlock::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:animus_block"));
    public static final DeferredHolder<EntityType<?>, EntityType<GreaterAnimus>> GREATER_ANIMUS = ENTITY_TYPES.register("greater_animus", () -> EntityType.Builder.<GreaterAnimus>of(GreaterAnimus::new, (MobCategory)MobCategory.MISC).sized(1.0f, 2.0f).build("mna:greater_animus"));
    public static final DeferredHolder<EntityType<?>, EntityType<WanderingWizard>> WANDERING_WIZARD = ENTITY_TYPES.register("wandering_wizard", () -> EntityType.Builder.<WanderingWizard>of(WanderingWizard::new, (MobCategory)MobCategory.MISC).sized(1.0f, 2.0f).build("mna:wandering_wizard"));
    public static final DeferredHolder<EntityType<?>, EntityType<Broker>> BROKER = ENTITY_TYPES.register("broker", () -> EntityType.Builder.<Broker>of(Broker::new, (MobCategory)MobCategory.MISC).sized(1.0f, 2.0f).build("mna:broker"));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownManaweaveBottle>> MANAWEAVE_BOTTLE_THROWN = ENTITY_TYPES.register("thrown_manaweave_potion", () -> EntityType.Builder.<ThrownManaweaveBottle>of(ThrownManaweaveBottle::new, (MobCategory)MobCategory.MISC).sized(0.2f, 0.2f).build("mna:thrown_manaweave_potion"));
    public static final DeferredHolder<EntityType<?>, EntityType<RisingBlock>> RISING_BLOCK_ENTITY = ENTITY_TYPES.register("rising_block_entity", () -> EntityType.Builder.<RisingBlock>of(RisingBlock::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:rising_block_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<StationaryBlock>> STATIONARY_BLOCK_ENTITY = ENTITY_TYPES.register("stationary_block_entity", () -> EntityType.Builder.<StationaryBlock>of(StationaryBlock::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).build("mna:stationary_block_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<SummonedSpectralHorse>> SPECTRAL_HORSE = ENTITY_TYPES.register("spectral_horse", () -> EntityType.Builder.<SummonedSpectralHorse>of(SummonedSpectralHorse::new, (MobCategory)MobCategory.CREATURE).sized(1.3964844f, 1.6f).build("mna:spectral_horse"));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownRunescribingPattern>> THROWN_RUNESCRIBE_PATTERN = ENTITY_TYPES.register("thrown_runescribing_pattern", () -> EntityType.Builder.<ThrownRunescribingPattern>of(ThrownRunescribingPattern::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:thrown_runescribing_pattern"));
    public static final DeferredHolder<EntityType<?>, EntityType<ImpulseProjectile>> IMPULSE_PROJECTILE = ENTITY_TYPES.register("impulse_projectile", () -> EntityType.Builder.<ImpulseProjectile>of(ImpulseProjectile::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:impulse_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<BoundBowProjectile>> BOUND_BOW_PROJECTILE = ENTITY_TYPES.register("bound_bow_projectile", () -> EntityType.Builder.<BoundBowProjectile>of(BoundBowProjectile::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("mna:bound_bow_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SkeletonAssassinShuriken>> SKELETON_ASSASSIN_SHURIKEN = ENTITY_TYPES.register("skeleton_assassin_shuriken", () -> EntityType.Builder.<SkeletonAssassinShuriken>of(SkeletonAssassinShuriken::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("mna:skeleton_assassin_shuriken"));
    public static final DeferredHolder<EntityType<?>, EntityType<SkeletonAssassinBolo>> SKELETON_ASSASSIN_BOLO = ENTITY_TYPES.register("skeleton_assassin_bolo", () -> EntityType.Builder.<SkeletonAssassinBolo>of(SkeletonAssassinBolo::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("mna:skeleton_assassin_bolo"));
    public static final DeferredHolder<EntityType<?>, EntityType<WitherLich>> LICH = ENTITY_TYPES.register("wither_lich", () -> EntityType.Builder.<WitherLich>of(WitherLich::new, (MobCategory)MobCategory.MONSTER).sized(2.0f, 3.0f).build("mna:wither_lich"));
    public static final DeferredHolder<EntityType<?>, EntityType<PigDragon>> PIG_DRAGON = ENTITY_TYPES.register("tfpd", () -> EntityType.Builder.<PigDragon>of(PigDragon::new, (MobCategory)MobCategory.MONSTER).sized(2.0f, 3.0f).build("mna:tfpd"));
    public static final DeferredHolder<EntityType<?>, EntityType<PumpkinKing>> PUMPKIN_KING = ENTITY_TYPES.register("pumpkin_king", () -> EntityType.Builder.<PumpkinKing>of(PumpkinKing::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 6.0f).build("mna:pumpkin_king"));
    public static final DeferredHolder<EntityType<?>, EntityType<Odin>> ODIN = ENTITY_TYPES.register("odin", () -> EntityType.Builder.<Odin>of(Odin::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:odin"));
    public static final DeferredHolder<EntityType<?>, EntityType<Bifrost>> BIFROST = ENTITY_TYPES.register("bifrost", () -> EntityType.Builder.<Bifrost>of(Bifrost::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:bifrost"));
    public static final DeferredHolder<EntityType<?>, EntityType<PumpkinKingIncinerate>> PUMPKIN_KING_INCINERATE = ENTITY_TYPES.register("pumpkin_king_incinerate", () -> EntityType.Builder.<PumpkinKingIncinerate>of(PumpkinKingIncinerate::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:pumpkin_king_incinerate"));
    public static final DeferredHolder<EntityType<?>, EntityType<PumpkinKingEntangle>> PUMPKIN_KING_ENTANGLE = ENTITY_TYPES.register("pumpkin_king_entangle", () -> EntityType.Builder.<PumpkinKingEntangle>of(PumpkinKingEntangle::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:pumpkin_king_incinerate"));
    public static final DeferredHolder<EntityType<?>, EntityType<FillHole>> FILL_HOLE = ENTITY_TYPES.register("fill_hole", () -> EntityType.Builder.<FillHole>of(FillHole::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).build("mna:fill_hole"));
    public static final DeferredHolder<EntityType<?>, EntityType<InsectSwarm>> INSECT_SWARM = ENTITY_TYPES.register("insect_swarm", () -> EntityType.Builder.<InsectSwarm>of(InsectSwarm::new, (MobCategory)MobCategory.MONSTER).sized(1.25f, 1.25f).build("mna:insect_swarm"));
    public static final DeferredHolder<EntityType<?>, EntityType<DemonReturnPortal>> DEMON_RETURN_PORTAL = ENTITY_TYPES.register("demon_return_portal", () -> EntityType.Builder.<DemonReturnPortal>of(DemonReturnPortal::new, (MobCategory)MobCategory.MISC).sized(3.0f, 0.25f).build("mna:demon_return_portal"));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownWeapon>> THROWN_WEAPON = ENTITY_TYPES.register("thrown_weapon", () -> EntityType.Builder.<ThrownWeapon>of(ThrownWeapon::new, (MobCategory)MobCategory.MISC).sized(1.75f, 0.25f).build("mna:thrown_weapon"));
    public static final DeferredHolder<EntityType<?>, EntityType<Magmoo>> MAGMOO = ENTITY_TYPES.register("magmoo", () -> EntityType.Builder.<Magmoo>of(Magmoo::new, (MobCategory)MobCategory.MISC).sized(0.9f, 1.4f).clientTrackingRange(10).build("mna:magmoo"));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodGolem>> BLOOD_GOLEM = ENTITY_TYPES.register("blood_golem", () -> EntityType.Builder.<BloodGolem>of(BloodGolem::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:blood_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodTendril>> BLOOD_TENDRIL = ENTITY_TYPES.register("blood_tendril", () -> EntityType.Builder.<BloodTendril>of(BloodTendril::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.0f).build("mna:blood_tendril"));
    public static final DeferredHolder<EntityType<?>, EntityType<Simulacrum>> SIMULACRUM = ENTITY_TYPES.register("simulacrum", () -> EntityType.Builder.<Simulacrum>of(Simulacrum::new, (MobCategory)MobCategory.MISC).sized(0.6f, 1.8f).build("mna:simulacrum"));

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPostRegisterEntities(RegisterEvent event) {
        ModSpawnEggItem.initUnaddedEggs();
    }

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put((EntityType)COUNCIL_WARDEN.get(), CouncilWarden.getGlobalAttributes().build());
        event.put((EntityType)DEMON_LORD.get(), DemonLord.getGlobalAttributes().build());
        event.put((EntityType)FAERIE_QUEEN.get(), FaerieQueen.getGlobalAttributes().build());
        event.put((EntityType)ANCIENT_COUNCIL.get(), LivingUtilityEntity.getGlobalAttributes().build());
        event.put((EntityType)MAGIC_BROOM.get(), MagicBroom.getGlobalAttributes().build());
        event.put((EntityType)ANIMATED_CONSTRUCT.get(), Construct.getGlobalAttributes().build());
        event.put((EntityType)MUSHROOM_SOLDIER.get(), Deathcap.getGlobalAttributes().build());
        event.put((EntityType)PIXIE.get(), Pixie.getGlobalAttributes().build());
        event.put((EntityType)BARKLING.get(), Barkling.getGlobalAttributes().build());
        event.put((EntityType)DEMON_IMP.get(), DemonImp.getGlobalAttributes().build());
        event.put((EntityType)HULKING_ZOMBIE.get(), HulkingZombie.getGlobalAttributes().build());
        event.put((EntityType)SKELETON_ASSASSIN.get(), SkeletonAssassin.getGlobalAttributes().build());
        event.put((EntityType)LANTERN_WRAITH.get(), LanternWraith.getGlobalAttributes().build());
        event.put((EntityType)SPELLBREAKER.get(), Spellbreaker.getGlobalAttributes().build());
        event.put((EntityType)WITCH_HUNTER.get(), WitchHunter.getGlobalAttributes().build());
        event.put((EntityType)DECOY_ENTITY.get(), EntityDecoy.getGlobalAttributes().build());
        event.put((EntityType)ANIMUS_BLOCK.get(), AnimusBlock.getGlobalAttributes().build());
        event.put((EntityType)WANDERING_WIZARD.get(), WanderingWizard.getGlobalAttributes().build());
        event.put((EntityType)BROKER.get(), Broker.getGlobalAttributes().build());
        event.put((EntityType)SPECTRAL_HORSE.get(), SummonedSpectralHorse.getGlobalAttributes().build());
        event.put((EntityType)LICH.get(), WitherLich.getGlobalAttributes().build());
        event.put((EntityType)PIG_DRAGON.get(), PigDragon.getGlobalAttributes().build());
        event.put((EntityType)PUMPKIN_KING.get(), PumpkinKing.getGlobalAttributes().build());
        event.put((EntityType)ODIN.get(), Odin.getGlobalAttributes().build());
        event.put((EntityType)CONSTRUCT_ASSEMBLY_STAND.get(), LivingEntity.createLivingAttributes().build());
        event.put((EntityType)GREATER_ANIMUS.get(), GreaterAnimus.createAttributes().build());
        event.put((EntityType)INSECT_SWARM.get(), InsectSwarm.createAttributes().build());
        event.put((EntityType)LIVING_WARD.get(), LivingWard.getGlobalAttributes().build());
        event.put((EntityType)ILLUSION_CREEPER.get(), Creeper.createAttributes().build());
        event.put((EntityType)FACTION_WAR.get(), FactionWar.getGlobalAttributes().build());
        event.put((EntityType)CHATTERER.get(), Chatterer.getGlobalAttributes().build());
        event.put((EntityType)MAGMOO.get(), Cow.createAttributes().build());
        event.put((EntityType)BLOOD_GOLEM.get(), BloodGolem.getGlobalAttributes().build());
        event.put((EntityType)BLOOD_TENDRIL.get(), BloodTendril.getGlobalAttributes().build());
        event.put((EntityType)SIMULACRUM.get(), Simulacrum.getGlobalAttributes().build());
    }

    @SubscribeEvent
    public static void handleSpawnPlacementRegistration(RegisterSpawnPlacementsEvent event) {
        event.register((EntityType)DEMON_IMP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.WORLD_SURFACE, DemonImp::canSpawnPredicate, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)LANTERN_WRAITH.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LanternWraith::canSpawnPredicate, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)SPELLBREAKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IFactionEnemy::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)WITCH_HUNTER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IFactionEnemy::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)LIVING_WARD.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LivingWard::canSpawnPredicate, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)PIXIE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pixie::canSpawnPredicate, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)MUSHROOM_SOLDIER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IFactionEnemy::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)BARKLING.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IFactionEnemy::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)HULKING_ZOMBIE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IFactionEnemy::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)SKELETON_ASSASSIN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IFactionEnemy::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)CHATTERER.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Chatterer::canSpawnPredicate, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register((EntityType)FACTION_WAR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FactionWar::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    @SubscribeEvent
    public static void loadCompleteEventHandler(FMLLoadCompleteEvent event) {
        FactionRaidRegistry.registerSoldier(Factions.COUNCIL, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)SPELLBREAKER.get()), new HashMap<Integer, Integer>(){
            {
                this.put(30, 0);
                this.put(65, 1);
                this.put(100, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.COUNCIL, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)WITCH_HUNTER.get()), new HashMap<Integer, Integer>(){
            {
                this.put(15, 0);
                this.put(45, 1);
                this.put(75, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.COUNCIL, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)LIVING_WARD.get()), new HashMap<Integer, Integer>(){
            {
                this.put(25, 0);
                this.put(35, 1);
                this.put(45, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.FEY, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)BARKLING.get()), new HashMap<Integer, Integer>(){
            {
                this.put(10, 0);
                this.put(25, 1);
                this.put(40, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.FEY, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)MUSHROOM_SOLDIER.get()), new HashMap<Integer, Integer>(){
            {
                this.put(10, 0);
                this.put(45, 1);
                this.put(80, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.FEY, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)PIXIE.get()), new HashMap<Integer, Integer>(){
            {
                this.put(10, 0);
                this.put(35, 1);
                this.put(60, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.DEMONS, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)DEMON_IMP.get()), new HashMap<Integer, Integer>(){
            {
                this.put(20, 0);
                this.put(50, 1);
                this.put(80, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.DEMONS, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)LANTERN_WRAITH.get()), new HashMap<Integer, Integer>(){
            {
                this.put(10, 0);
                this.put(35, 1);
                this.put(60, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.UNDEAD, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)HULKING_ZOMBIE.get()), new HashMap<Integer, Integer>(){
            {
                this.put(30, 0);
                this.put(65, 1);
                this.put(100, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.UNDEAD, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)SKELETON_ASSASSIN.get()), new HashMap<Integer, Integer>(){
            {
                this.put(15, 0);
                this.put(30, 1);
                this.put(45, 2);
            }
        });
        FactionRaidRegistry.registerSoldier(Factions.UNDEAD, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)CHATTERER.get()), new HashMap<Integer, Integer>(){
            {
                this.put(15, 0);
                this.put(30, 1);
                this.put(45, 2);
            }
        });
    }
}


