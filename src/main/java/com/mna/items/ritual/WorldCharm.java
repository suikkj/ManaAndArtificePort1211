 
package com.mna.items.ritual;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.TieredItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class WorldCharm
extends TieredItem {
    private static final String KEY_WORLD_TARGET_REGISTRY_NAME = "world_key_type";
    private static final String KEY_WORLD_TARGET_LOCATION = "world_key_value";

    public WorldCharm() {
        super(new Item.Properties().stacksTo(1));
    }

    public boolean SetWorldTarget(Player entity, ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        java.util.Optional<com.mna.api.capabilities.IPlayerMagic> magic = MNACap.get(entity, PlayerMagicProvider.MAGIC);
        if (magic.isPresent()) {
            nbt.putString(KEY_WORLD_TARGET_REGISTRY_NAME, entity.level().dimension().registry().toString());
            nbt.putString(KEY_WORLD_TARGET_LOCATION, entity.level().dimension().location().toString());
            return true;
        }
        return false;
    }

    @Nullable
    public ResourceKey<Level> GetWorldTarget(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt == null || !nbt.contains(KEY_WORLD_TARGET_REGISTRY_NAME) || !nbt.contains(KEY_WORLD_TARGET_LOCATION)) {
            return null;
        }
        ResourceLocation location = ResourceLocation.parse(nbt.getString(KEY_WORLD_TARGET_LOCATION));
        return ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)location);
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (nbt == null) {
                nbt = new CompoundTag();
            }
            if (!nbt.contains(KEY_WORLD_TARGET_REGISTRY_NAME) && !nbt.contains(KEY_WORLD_TARGET_LOCATION)) {
                this.SetWorldTarget(playerIn, stack);
                playerIn.sendSystemMessage((Component)Component.translatable((String)"item.mna.world_charm.attuned_success"));
            } else {
                playerIn.sendSystemMessage((Component)Component.translatable((String)"item.mna.world_charm.attuned_failure"));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt == null || !nbt.contains(KEY_WORLD_TARGET_LOCATION)) {
            tooltip.add((Component)Component.translatable((String)"item.mna.world_charm.not_attuned"));
            return;
        }
        ResourceLocation worldLocation = ResourceLocation.parse(nbt.getString(KEY_WORLD_TARGET_LOCATION));
        tooltip.add((Component)Component.translatable((String)"item.mna.world_charm.attuned", (Object[])new Object[]{worldLocation.toString()}));
    }
}


