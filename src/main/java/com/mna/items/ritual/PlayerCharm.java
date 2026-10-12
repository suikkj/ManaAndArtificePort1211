 
package com.mna.items.ritual;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.TieredItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class PlayerCharm
extends TieredItem {
    private static final String KEY_PLAYER_TARGET_NAME = "player_target_name";
    private static final String KEY_PLAYER_TARGET_UUID = "player_target_uuid";
    private static final String KEY_PLAYER_TARGET_SALT = "player_target_salt";

    public PlayerCharm() {
        super(new Item.Properties().stacksTo(1));
    }

    public boolean SetPlayerTarget(Player entity, ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        java.util.Optional<com.mna.api.capabilities.IPlayerMagic> magic = MNACap.get(entity, PlayerMagicProvider.MAGIC);
        if (magic.isPresent()) {
            nbt.putString(KEY_PLAYER_TARGET_UUID, entity.getUUID().toString());
            nbt.putInt(KEY_PLAYER_TARGET_SALT, ((IPlayerMagic)magic.get()).getTeleportSalt());
            nbt.putString(KEY_PLAYER_TARGET_NAME, entity.getGameProfile().getName());
            return true;
        }
        return false;
    }

    @Nullable
    public UUID getPlayerUUID(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt == null || !nbt.contains(KEY_PLAYER_TARGET_UUID) || !nbt.contains(KEY_PLAYER_TARGET_SALT)) {
            return null;
        }
        return UUID.fromString(nbt.getString(KEY_PLAYER_TARGET_UUID));
    }

    @Nullable
    public Player GetPlayerTarget(ItemStack stack, Level world) {
        java.util.Optional magic;
        UUID uuid = this.getPlayerUUID(stack);
        if (uuid == null) {
            return null;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Player entity = world.getPlayerByUUID(uuid);
        if (entity != null && (magic = MNACap.get(entity, PlayerMagicProvider.MAGIC)).isPresent() && ((IPlayerMagic)magic.get()).getTeleportSalt() == nbt.getInt(KEY_PLAYER_TARGET_SALT)) {
            return entity;
        }
        return null;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (nbt == null) {
                nbt = new CompoundTag();
            }
            if (!nbt.contains(KEY_PLAYER_TARGET_UUID) && !nbt.contains(KEY_PLAYER_TARGET_SALT)) {
                this.SetPlayerTarget(playerIn, stack);
                playerIn.sendSystemMessage((Component)Component.translatable((String)"item.mna.player_charm.attuned_success"));
            } else {
                playerIn.sendSystemMessage((Component)Component.translatable((String)"item.mna.player_charm.attuned_failure"));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt == null) {
            tooltip.add((Component)Component.translatable((String)"item.mna.player_charm.not_attuned"));
            return;
        }
        String playerName = nbt.getString(KEY_PLAYER_TARGET_NAME);
        if (playerName == null) {
            return;
        }
        tooltip.add((Component)Component.translatable((String)"item.mna.player_charm.attuned", (Object[])new Object[]{playerName}));
    }
}


