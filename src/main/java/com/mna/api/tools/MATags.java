 
package com.mna.api.tools;

import com.mna.api.tools.RLoc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class MATags {
    public static boolean isItemEqual(ItemStack stack, ResourceLocation rLoc) {
        if (BuiltInRegistries.ITEM.containsKey(rLoc)) {
            return BuiltInRegistries.ITEM.get(rLoc) == stack.getItem();
        }
        return MATags.isItemIn(stack.getItem(), rLoc);
    }

    public static List<Item> getItemTagContents(ResourceLocation tagID) {
        try {
            Optional<HolderSet.Named<Item>> tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagID));
            if (tag.isPresent()) {
                return tag.get().stream().map(Holder::value).collect(Collectors.toList());
            }
        }
        catch (Exception exception) {
             
        }
        return new ArrayList<Item>();
    }

    public static List<Holder<Biome>> getBiomeTagContents(ServerLevel world, ResourceLocation tagID) {
        try {
            Registry biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
            TagKey tag = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)tagID);
            Optional biomeTag = biomeRegistry.getTag(tag);
            if (biomeTag != null && biomeTag.isPresent()) {
                return ((net.minecraft.core.HolderSet.Named<net.minecraft.world.level.biome.Biome>)biomeTag.get()).stream().collect(Collectors.toList());
            }
        }
        catch (Exception exception) {
             
        }
        return new ArrayList<Holder<Biome>>();
    }

    public static List<Block> getBlockTagContents(ResourceLocation tagID) {
        try {
            Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, tagID));
            if (tag.isPresent()) {
                return tag.get().stream().map(Holder::value).collect(Collectors.toList());
            }
        }
        catch (Exception exception) {
             
        }
        return new ArrayList<Block>();
    }

    public static List<Holder<Structure>> getStructureContents(ServerLevel world, ResourceLocation tagID) {
        try {
            Registry registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
            TagKey tagKey = TagKey.create((ResourceKey)registry.key(), (ResourceLocation)tagID);
            Optional tag = registry.getTag(tagKey);
            if (tag != null && tag.isPresent()) {
                return ((HolderSet.Named)tag.get()).stream().collect(Collectors.toList());
            }
        }
        catch (Exception exception) {
             
        }
        return new ArrayList<Holder<Structure>>();
    }

    @Nullable
    public static Structure getStructure(ServerLevel world, ResourceLocation tagID) {
        try {
            Registry registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
            if (registry.containsKey(tagID)) {
                return (Structure)registry.get(tagID);
            }
        }
        catch (Exception exception) {
             
        }
        return null;
    }

    public static List<Item> smartLookupItem(ResourceLocation rLoc) {
        if (rLoc == null) {
            return new ArrayList<Item>();
        }
        if (BuiltInRegistries.ITEM.containsKey(rLoc)) {
            return Arrays.asList((Item)BuiltInRegistries.ITEM.get(rLoc));
        }
        return MATags.getItemTagContents(rLoc);
    }

    public static List<Block> smartLookupBlock(ResourceLocation rLoc) {
        if (rLoc == null) {
            return new ArrayList<Block>();
        }
        if (BuiltInRegistries.BLOCK.containsKey(rLoc)) {
            return Arrays.asList((Block)BuiltInRegistries.BLOCK.get(rLoc));
        }
        return MATags.getBlockTagContents(rLoc);
    }

    public static ItemStack lookupItem(ResourceLocation rLoc) {
        if (BuiltInRegistries.ITEM.containsKey(rLoc)) {
            return new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(rLoc));
        }
        return ItemStack.EMPTY;
    }

    public static boolean isBlockIn(Block block, ResourceLocation tag) {
        try {
            return MATags.getBlockTagContents(tag).contains(block);
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean isItemIn(Item item, ResourceLocation tag) {
        try {
            return MATags.getItemTagContents(tag).contains(item);
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean isEntityIn(EntityType<?> item, ResourceLocation tag) {
        try {
            return MATags.getEntitiesOnTag(tag).contains(item);
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean isStructureIn(ServerLevel world, Holder<Structure> structure, ResourceLocation tag) {
        try {
            TagKey key = TagKey.create((ResourceKey)Registries.STRUCTURE, (ResourceLocation)tag);
            world.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrCreateTag(key).contains(structure);
        }
        catch (Exception exception) {
             
        }
        return false;
    }

    public static boolean doesEntityTagExist(ResourceLocation tagID) {
        try {
            Optional<HolderSet.Named<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, tagID));
            if (tag.isPresent()) {
                return true;
            }
        }
        catch (Exception exception) {
             
        }
        return false;
    }

    public static List<EntityType<?>> getEntitiesOnTag(ResourceLocation tagID) {
        try {
            Optional<HolderSet.Named<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, tagID));
            if (tag.isPresent()) {
                return tag.get().stream().map(Holder::value).collect(Collectors.toList());
            }
        }
        catch (Exception exception) {
             
        }
        return new ArrayList();
    }

    public static ItemStack getRandomItemFrom(ResourceLocation ... tagIDs) {
        ResourceLocation tagID = tagIDs[(int)(Math.random() * (double)tagIDs.length)];
        List<Item> items = MATags.getItemTagContents(tagID);
        if (items.size() == 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack((ItemLike)items.get((int)(Math.random() * (double)items.size())));
    }

    public static class EntityTypes {
        public static ResourceLocation WOODLAND_CREATURES = RLoc.create("woodland_creatures");
        public static ResourceLocation SUMMON_BLACKLIST = RLoc.create("summon_blacklist");

        public static class Constructs {
            public static ResourceLocation[] HUNT_DANGER_LEVELS = new ResourceLocation[]{RLoc.create("constructs/hunt/danger/1_passive"), RLoc.create("constructs/hunt/danger/2_easy"), RLoc.create("constructs/hunt/danger/3_moderate"), RLoc.create("constructs/hunt/danger/4_hard"), RLoc.create("constructs/hunt/danger/5_deadly")};
        }
    }

    public static class DamageTypes {
        public static TagKey<DamageType> CONSTRUCT_IMMUNE = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_immune"));
        public static TagKey<DamageType> CONSTRUCT_RESIST_ARCANE = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_arcane_resist"));
        public static TagKey<DamageType> CONSTRUCT_RESIST_ENDER = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_ender_resist"));
        public static TagKey<DamageType> CONSTRUCT_RESIST_FIRE = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_fire_resist"));
        public static TagKey<DamageType> CONSTRUCT_RESIST_WATER = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_water_resist"));
        public static TagKey<DamageType> CONSTRUCT_RESIST_EARTH = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_earth_resist"));
        public static TagKey<DamageType> CONSTRUCT_RESIST_WIND = TagKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)RLoc.create("construct_wind_resist"));
    }

    public static class Biomes {
        public static TagKey<Biome> BROKER_BIOMES = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("broker_biomes"));

        public static class Wellspring {
            public static TagKey<Biome> ARCANE = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("wellspring_arcane"));
            public static TagKey<Biome> ENDER = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("wellspring_ender"));
            public static TagKey<Biome> FIRE = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("wellspring_fire"));
            public static TagKey<Biome> WATER = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("wellspring_water"));
            public static TagKey<Biome> EARTH = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("wellspring_earth"));
            public static TagKey<Biome> WIND = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)RLoc.create("wellspring_wind"));
        }
    }

    public static class Structures {
        public static ResourceLocation BOSS_ARENAS = RLoc.create("boss_arenas");
        public static TagKey<Structure> BROKER_STRUCTURES = TagKey.create((ResourceKey)Registries.STRUCTURE, (ResourceLocation)RLoc.create("broker_structures"));
    }

    public static class Blocks {
        public static ResourceLocation LOGS = RLoc.create("logs");
        public static ResourceLocation LEAVES = RLoc.create("leaves");
        public static ResourceLocation VELLUM_DOUBLERS = RLoc.create("vellum_doublers");
        public static ResourceLocation SHEARABLES = RLoc.create("shearables");
        public static ResourceLocation ANIMUS_TANGLES = RLoc.create("animus_tangles");
        public static ResourceLocation ANIMUS_DAMAGEBOOST = RLoc.create("animus_damageboost");
        public static ResourceLocation ANIMUS_FLYING = RLoc.create("animus_flying");
        public static ResourceLocation ANIMUS_GLOW = RLoc.create("animus_glow");
        public static ResourceLocation ANIMUS_KNOCKBACK = RLoc.create("animus_knockback");
        public static ResourceLocation ANIMUS_POISONCLOUD = RLoc.create("animus_poisoncloud");
        public static ResourceLocation ANIMUS_SLOWS = RLoc.create("animus_slows");
        public static ResourceLocation ANIMUS_NOFALLDAMAGE = RLoc.create("animus_nofalldamage");
        public static ResourceLocation ANIMUS_NOFRICTION = RLoc.create("animus_nofriction");
        public static ResourceLocation REFRACTION_LENSES = RLoc.create("refraction_lenses");
        public static ResourceLocation BOOKSHELVES = RLoc.create("bookshelves");
        public static ResourceLocation SPECTRAL_BLOCKS = RLoc.create("spectral_blocks");
        public static ResourceLocation CONSTRUCT_HARVESTABLES_NO_AGE = RLoc.create("construct_harvestables_no_age");
        public static ResourceLocation CONSTRUCT_HARVESTABLES = RLoc.create("construct_harvestables");
        public static ResourceLocation CONSTRUCT_HARVESTABLE_EXCLUDE = RLoc.create("construct_harvestable_exclude");
        public static ResourceLocation CONSTRUCT_PLANTABLE_GRASS = RLoc.create("construct_plantable_grass");
        public static ResourceLocation CONSTRUCT_PLANTABLE_FARMLAND = RLoc.create("construct_plantable_farmland");
        public static ResourceLocation CONSTRUCT_PLANTABLE_SAND = RLoc.create("construct_plantable_sand");
        public static ResourceLocation CONSTRUCT_PLANTABLE_SOUL_SAND = RLoc.create("construct_plantable_soul_sand");
        public static ResourceLocation GUST_DESTRUCTIBLE_BLOCKS = RLoc.create("gust_destructible_blocks");
        public static ResourceLocation CONSTRUCT_WALK_THROUGH = RLoc.create("construct_walk_through");
    }

    public static class Items {
        public static ResourceLocation FLUID_ARM_PLAYER_TANKS = RLoc.create("fluid_arm_player_tanks");
        public static ResourceLocation LESSER_MOTES = RLoc.create("lesser_motes");
        public static ResourceLocation GREATER_MOTES = RLoc.create("greater_motes");
        public static ResourceLocation RUNES = RLoc.create("runes");
        public static ResourceLocation ALL_CONCRETE = RLoc.create("all_concrete");
        public static ResourceLocation ANY_CORAL = RLoc.create("any_coral");
        public static ResourceLocation ARCANE_FURNACE_DOUBLING_BLACKLIST = RLoc.create("arcane_furnace_doubling_blacklist");
        public static ResourceLocation CHIMERITE_CRYSTALS = RLoc.create("chimerite_crystals");
        public static ResourceLocation HEALING_POULTICE_BASES = RLoc.create("healing_poultice_bases");
        public static ResourceLocation HERBALIST_POUCH_ITEMS = RLoc.create("herbalist_pouch_items");
        public static ResourceLocation IMPROVISED_MANAWEAVE_CAPS = RLoc.create("improvised_manaweave_wand_caps");
        public static ResourceLocation IMPROVISED_MANAWEAVE_SHAFTS = RLoc.create("improvised_manaweave_wand_shafts");
        public static ResourceLocation INSCRIPTIONIST_POUCH_ITEMS = RLoc.create("inscriptionist_pouch_items");
        public static ResourceLocation LOCATING_COMPASSES = RLoc.create("locating_compasses");
        public static ResourceLocation FLOWERS = RLoc.create("ma_flowers");
        public static ResourceLocation MONSTER_HEADS = RLoc.create("monster_heads");
        public static ResourceLocation BRIGHT_PEDESTAL_ITEMS = RLoc.create("pedestal_bright_light_items");
        public static ResourceLocation DIM_PEDESTAL_ITEMS = RLoc.create("pedestal_dim_light_items");
        public static ResourceLocation RUNE_PATTERNS = RLoc.create("rune_patterns");
        public static ResourceLocation STAVES = RLoc.create("staves");
        public static ResourceLocation WANDS = RLoc.create("wands");
        public static ResourceLocation ALTERATION_ITEMS = RLoc.create("alteration_items");
        public static ResourceLocation GENERATED_SPELL_ITEMS = RLoc.create("generated_spell_items");
        public static ResourceLocation WEAVER_POUCH_ITEMS = RLoc.create("weaver_pouch_items");
        public static ResourceLocation STONE_RUNES = RLoc.create("stone_runes");
        public static ResourceLocation CANNON_HALFBOOST = RLoc.create("cannon_fiftypct_boost");
        public static ResourceLocation CANNON_FULLBOOST = RLoc.create("cannon_hundredpct_boost");
        public static ResourceLocation ANIMUS_BOWS = RLoc.create("animus_bows");
        public static ResourceLocation SCROLL_SHELF_POTIONS = RLoc.create("scroll_shelf_potions");
        public static ResourceLocation LECTERN_ALLOWED_ITEMS = RLoc.create("lectern_allowed_items");
        public static ResourceLocation BATTLEMAGE_AMULET_SWORDS = RLoc.create("battlemage_amulet_valid_swords");
        public static ResourceLocation REPAIR_BLACKLIST = RLoc.create("repair_blacklist");
        public static ResourceLocation DISENCHANT_BLACKLIST = RLoc.create("disenchant_blacklist");

        public static class Ritual {
            public static ResourceLocation SUMMER_FLOWERS = RLoc.create("ritual/summer_flowers");
            public static ResourceLocation WINTER_FLOWERS = RLoc.create("ritual/winter_flowers");
        }

        public static class Stonecutter {
            public static ResourceLocation ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_arcane_sandstones");
            public static ResourceLocation ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_arcane_stones");
            public static ResourceLocation CHIMERITE_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_chimerite_arcane_sandstones");
            public static ResourceLocation CHIMERITE_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_chimerite_arcane_stones");
            public static ResourceLocation REDSTONE_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_redstone_arcane_sandstones");
            public static ResourceLocation REDSTONE_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_redstone_arcane_stones");
            public static ResourceLocation VINTEUM_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_vinteum_arcane_sandstones");
            public static ResourceLocation VINTEUM_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_vinteum_arcane_stones");
            public static ResourceLocation EMERALD_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_emerald_arcane_sandstones");
            public static ResourceLocation EMERALD_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_emerald_arcane_stones");
            public static ResourceLocation AMETHYST_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_amethyst_arcane_sandstones");
            public static ResourceLocation AMETHYST_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_amethyst_arcane_stones");
            public static ResourceLocation QUICKSILVER_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_quicksilver_arcane_sandstones");
            public static ResourceLocation QUICKSILVER_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_quicksilver_arcane_stones");
            public static ResourceLocation GOLD_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_gold_arcane_sandstones");
            public static ResourceLocation GOLD_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_gold_arcane_stones");
            public static ResourceLocation DIAMOND_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_diamond_arcane_sandstones");
            public static ResourceLocation DIAMOND_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_diamond_arcane_stones");
            public static ResourceLocation COPPER_ARCANE_SANDSTONE_RESET = RLoc.create("stonecutter_resettable_copper_arcane_sandstones");
            public static ResourceLocation COPPER_ARCANE_STONE_RESET = RLoc.create("stonecutter_resettable_copper_arcane_stones");
        }

        public static class Constructs {
            public static final ResourceLocation HATS = RLoc.create("constructs/hats");
            public static ResourceLocation FARMLAND_PLANTABLE = RLoc.create("constructs/farmland_plantable");
            public static ResourceLocation GRASS_PLANTABLE = RLoc.create("constructs/grass_plantable");
            public static ResourceLocation SAND_PLANTABLE = RLoc.create("constructs/sand_plantable");
            public static ResourceLocation SOUL_SAND_PLANTABLE = RLoc.create("constructs/soul_sand_plantable");
            public static ResourceLocation UNDERGROUND_LIGHTS = RLoc.create("constructs/underground_lights");
        }

        public static class Ingots {
            public static ResourceLocation VINTEUM = RLoc.create("ingots/vinteum");
            public static ResourceLocation PURIFIED_VINTEUM = RLoc.create("ingots/purified_vinteum");
        }

        public static class Gems {
            public static ResourceLocation CHIMERITE = RLoc.create("gems/chimerite");
        }

        public static class Dusts {
            public static ResourceLocation ARCANE_ASH = RLoc.create("dusts/arcane_ash");
            public static ResourceLocation ARCANE_COMPOUND = RLoc.create("dusts/arcane_compound");
            public static ResourceLocation BONE_ASH = RLoc.create("dusts/bone_ash");
            public static ResourceLocation PURIFIED_VINTEUM = RLoc.create("dusts/purified_vinteum");
            public static ResourceLocation VINTEUM = RLoc.create("dusts/vinteum");
        }
    }
}


