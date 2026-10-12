 
package com.mna.items.ritual;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.tools.BiomeUtils;
import com.mna.tools.StructureUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ThaumaturgicLink
extends Item {
    private static final String NBT_RECIPEID = "linked_position";
    private static final String NBT_LINK_TYPE = "link_type";
    public static final String LINK_TYPE_BIOME = "biome";
    public static final String LINK_TYPE_STRUCTURE = "structure";
    private static ArrayList<ResourceLocation> all_structure_locations;
    private static ArrayList<ResourceLocation> all_biome_locations;

    public ThaumaturgicLink() {
        super(new Item.Properties());
    }

    private ArrayList<ResourceLocation> getAllStructureLocations(ServerLevel world) {
        if (all_structure_locations == null) {
            all_structure_locations = new ArrayList();
            StructureUtils.getAllStructureIDs(world).forEach(s -> all_structure_locations.add((ResourceLocation)s));
        }
        return all_structure_locations;
    }

    private ArrayList<ResourceLocation> getAllBiomeLocations(ServerLevel world) {
        if (all_biome_locations == null) {
            all_biome_locations = new ArrayList();
            BiomeUtils.getAllBiomeIDs(world).forEach(s -> all_biome_locations.add((ResourceLocation)s));
        }
        return all_biome_locations;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ResourceLocation recipe = this.getLocationKey(stack);
        if (recipe == null) {
            return;
        }
        tooltip.add((Component)Component.translatable((String)recipe.toString()));
    }

    @Nullable
    public ResourceLocation getLocationKey(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || stack.getItem() != this) {
            return null;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains(NBT_RECIPEID)) {
            return null;
        }
        ResourceLocation rLoc = ResourceLocation.parse(nbt.get(NBT_RECIPEID).getAsString());
        return rLoc;
    }

    public boolean isStructure(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || stack.getItem() != this) {
            return false;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains(NBT_LINK_TYPE)) {
            return false;
        }
        return nbt.getString(NBT_LINK_TYPE).equals(LINK_TYPE_STRUCTURE);
    }

    public boolean isBiome(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || stack.getItem() != this) {
            return false;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains(NBT_LINK_TYPE)) {
            return false;
        }
        return nbt.getString(NBT_LINK_TYPE).equals(LINK_TYPE_BIOME);
    }

    public static void setBiomeKey(ServerLevel level, ItemStack stack, Holder<Biome> biome) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ResourceLocation key = BiomeUtils.getBiomeID(level, biome);
        if (key != null) {
            nbt.putString(NBT_RECIPEID, key.toString());
            nbt.putString(NBT_LINK_TYPE, LINK_TYPE_BIOME);
        } else {
            ManaAndArtifice.LOGGER.error("Failed to look up biome key, not setting location.");
        }
    }

    public static void setStructureKey(ServerLevel level, ItemStack stack, Holder<Structure> structure) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ResourceLocation key = StructureUtils.getStructureID(level, structure);
        if (key != null) {
            nbt.putString(NBT_RECIPEID, key.toString());
            nbt.putString(NBT_LINK_TYPE, LINK_TYPE_STRUCTURE);
        } else {
            ManaAndArtifice.LOGGER.error("Failed to look up structure key, not setting location.");
        }
    }

    public ItemStack getRandomLink(ServerLevel world) {
        ItemStack output = new ItemStack((ItemLike)this);
        if (world.random.nextBoolean()) {
            output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_RECIPEID, this.getAllBiomeLocations(world).get((int)(Math.random() * (double)this.getAllBiomeLocations(world).size())).toString());
            output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_LINK_TYPE, LINK_TYPE_BIOME);
        } else {
            output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_RECIPEID, this.getAllStructureLocations(world).get((int)(Math.random() * (double)this.getAllStructureLocations(world).size())).toString());
            output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_LINK_TYPE, LINK_TYPE_STRUCTURE);
        }
        return output;
    }
}

