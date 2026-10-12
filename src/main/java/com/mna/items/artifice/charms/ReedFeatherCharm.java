 
package com.mna.items.artifice.charms;

import com.mna.items.artifice.charms.CharmBaseItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ReedFeatherCharm
extends CharmBaseItem {
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add((Component)Component.translatable((String)"item.mna.drown_charm.flavor").withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, world, tooltip, tooltipFlag);
    }
}

