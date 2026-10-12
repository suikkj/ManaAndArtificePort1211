 
package com.mna.items.base;

import com.mna.KeybindInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.base.IBagItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public interface IItemWithGui<T extends Item>
extends IBagItem {
    public MenuProvider getProvider(ItemStack var1);

    default public boolean addItemWithGuiTooltip(ItemStack stack) {
        return true;
    }

    default public boolean requiresModifierKey() {
        return true;
    }

    default public void fillByteBuffer(FriendlyByteBuf buf) {
    }

    default public boolean openGuiIfModifierPressed(ItemStack stack, Player player, Level world) {
        MutableBoolean guiOpened = new MutableBoolean(false);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (!this.requiresModifierKey() || m.isModifierPressed()) {
                if (!world.isClientSide) {
                    player.openMenu((MenuProvider)this.getProvider(stack), t -> this.fillByteBuffer((FriendlyByteBuf)t));
                }
                guiOpened.setTrue();
            }
        });
        return guiOpened.booleanValue();
    }

    default public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (!this.addItemWithGuiTooltip(stack)) {
            return;
        }
        if (this.requiresModifierKey()) {
            String txt = I18n.get((String)((KeyMapping)KeybindInit.InventoryItemOpen.get()).getKey().getName(), (Object[])new Object[0]);
            tooltip.add((Component)Component.translatable((String)"item.mna.item-with-gui.open-with", (Object[])new Object[]{txt}).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.item-with-gui.open-rclick").withStyle(ChatFormatting.AQUA));
        }
    }
}


