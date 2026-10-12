 
package com.mna.items.base;

import com.mna.KeybindInit;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public interface IRadialMenuItem {
    default public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        String txt = I18n.get((String)((KeyMapping)KeybindInit.RadialMenuOpen.get()).getKey().getDisplayName().getString(), (Object[])new Object[0]);
        tooltip.add((Component)Component.translatable((String)"item.mna.item-with-gui.radial-open", (Object[])new Object[]{txt}).withStyle(ChatFormatting.AQUA));
    }
}

