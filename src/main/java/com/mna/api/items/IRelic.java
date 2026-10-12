/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package com.mna.api.items;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public interface IRelic {
    @Nullable
    default public Component getHoverAddition(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        return Component.translatable((String)"item.mna.relic").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GOLD);
    }
}

