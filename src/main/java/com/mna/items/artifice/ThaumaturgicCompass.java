 
package com.mna.items.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.rituals.effects.WorldUtils;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

public class ThaumaturgicCompass
extends CompassItem {
    public static final String trackingKey = "tracking_key";
    public static final String trackingType = "tracking_type";
    public static final String trackingId = "tracking_id";
    public static final String trackingMode = "tracking_mode";

    private static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag pCompoundTag) {
        return Level.RESOURCE_KEY_CODEC.parse((DynamicOps)NbtOps.INSTANCE, pCompoundTag.get("LodestoneDimension")).result();
    }

    public ThaumaturgicCompass() {
        super(new Item.Properties());
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
    }

    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId();
    }

    @Nullable
    public BlockPos getTrackedPosition(ItemStack stack, ResourceKey<Level> dimension) {
        if (!ThaumaturgicCompass.isLodestoneCompass((ItemStack)stack)) {
            return null;
        }
        Optional<ResourceKey<Level>> world = ThaumaturgicCompass.getLodestoneDimension(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (!world.isPresent() || !world.get().equals(dimension)) {
            return null;
        }
        return NbtUtils.readBlockPos(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), "LodestonePos").orElse(null);
    }

    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag != null) {
            if (tag.contains(trackingKey)) {
                tooltip.add((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.locating", (Object[])new Object[]{Component.translatable((String)tag.getString(trackingKey))}));
            }
            if (ThaumaturgicCompass.getTrackMode(stack)) {
                tooltip.add((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.mode_new"));
            } else {
                tooltip.add((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.mode_existing"));
            }
            if (ThaumaturgicCompass.getTrackType(stack) == TrackType.Structure) {
                tooltip.add((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.mode_hint_structure"));
            } else {
                tooltip.add((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.mode_hint_biome"));
            }
        }
    }

    @Nonnull
    @OnlyIn(value=Dist.CLIENT)
    public static String getLocalizedStructureName(@Nonnull String resource) {
        if (resource.equals("")) {
            return "";
        }
        int split = resource.indexOf(":");
        if (split == -1) {
            return resource;
        }
        String source = resource.substring(0, split);
        String name = resource.substring(split + 1);
        return I18n.get((String)String.format("structure.%s.%s", source, name), (Object[])new Object[0]);
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            if (player.isCrouching()) {
                switch (ThaumaturgicCompass.getTrackType(stack)) {
                    case Biome: {
                        ThaumaturgicCompass.search_biome((ServerPlayer)player, stack);
                        break;
                    }
                    case Structure: {
                        ThaumaturgicCompass.search_structure((ServerPlayer)player, stack, ThaumaturgicCompass.getTrackMode(stack));
                        break;
                    }
                }
            } else if (ThaumaturgicCompass.getTrackType(stack) == TrackType.Structure) {
                boolean newmode = !ThaumaturgicCompass.getTrackMode(stack);
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean(trackingMode, newmode);
                player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.mode_changed"));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, (boolean)world.isClientSide);
    }

    public static void setTrackedPosition(ItemStack stack, ResourceKey<Level> dimension, BlockPos position, ResourceLocation trackedID, TrackType trackType) {
        CompoundTag stackTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        stackTag.putBoolean(trackingMode, false);
        stackTag.putInt(trackingType, trackType.ordinal());
        stackTag.putString(trackingKey, trackedID.toString());
        stackTag.put("LodestonePos", (Tag)NbtUtils.writeBlockPos((BlockPos)position));
        Level.RESOURCE_KEY_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, dimension).resultOrPartial(arg_0 -> ((Logger)ManaAndArtifice.LOGGER).error(arg_0)).ifPresent(inbt -> stackTag.put("LodestoneDimension", inbt));
        stackTag.putBoolean("LodestoneTracked", true);
    }

    @Nullable
    public static ResourceLocation getTrackedID(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(trackingKey)) {
            return ResourceLocation.parse(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(trackingKey));
        }
        return null;
    }

    public static TrackType getTrackType(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(trackingType)) {
            return TrackType.values()[stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(trackingType) % TrackType.values().length];
        }
        return TrackType.Unset;
    }

    public static boolean getTrackMode(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(trackingMode)) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(trackingMode);
        }
        return false;
    }

    public static void search_structure(ServerPlayer player, ItemStack stack, boolean skipGeneratedChunks) {
        if (ThaumaturgicCompass.getTrackType(stack) != TrackType.Structure) {
            return;
        }
        ResourceLocation structureRegistryName = ThaumaturgicCompass.getTrackedID(stack);
        ServerLevel world = (ServerLevel)player.level();
        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.searching"));
        try {
            BlockPos blockpos = BlockPos.containing((Position)player.position());
            BlockPos locatedPos = WorldUtils.locateStructure(world, blockpos, structureRegistryName, skipGeneratedChunks);
            if (locatedPos == null) {
                player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.search_fail"));
            } else {
                player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.search_success"));
                ThaumaturgicCompass.setTrackedPosition(stack, (ResourceKey<Level>)world.dimension(), locatedPos, structureRegistryName, TrackType.Structure);
            }
        }
        catch (Throwable t) {
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.search_fail"));
        }
    }

    public static void search_biome(ServerPlayer player, ItemStack stack) {
        if (ThaumaturgicCompass.getTrackType(stack) != TrackType.Biome) {
            return;
        }
        ResourceLocation biomeLoc = ThaumaturgicCompass.getTrackedID(stack);
        ServerLevel world = (ServerLevel)player.level();
        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.searching"));
        BlockPos locatedPos = WorldUtils.locateBiome(world, player.blockPosition(), biomeLoc);
        if (locatedPos == null) {
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.search_fail"));
            return;
        }
        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.thaumaturgic_compass.search_success"));
        ThaumaturgicCompass.setTrackedPosition(stack, (ResourceKey<Level>)world.dimension(), locatedPos, biomeLoc, TrackType.Structure);
    }

    public static enum TrackType {
        Unset,
        Structure,
        Biome;

    }
}


