/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.items.IItemHandler
 */
package com.mna.api.items;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public interface IPhylacteryItem {
    public boolean isFull(ItemStack var1);

    public float getFillPct(ItemStack var1);

    public float getContainedSouls(ItemStack var1);

    public boolean setContainedSouls(ItemStack var1, float var2);

    @Nullable
    public EntityType<? extends Mob> getContainedEntity(ItemStack var1);

    public boolean fill(ItemStack var1, EntityType<? extends Mob> var2, float var3, Level var4);

    public boolean fill(IItemHandler var1, EntityType<? extends Mob> var2, float var3, Level var4, boolean var5);

    public boolean fill(Container var1, EntityType<? extends Mob> var2, float var3, Level var4, boolean var5);

    public int getMaximumFill();

    default public void appendTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (!(stack.getItem() instanceof IPhylacteryItem)) {
            return;
        }
        EntityType<? extends Mob> type = this.getContainedEntity(stack);
        if (type != null) {
            String entityType = I18n.get((String)type.getDescriptionId(), (Object[])new Object[0]);
            float amount = this.getContainedSouls(stack);
            if (!this.isFull(stack)) {
                tooltip.add((Component)Component.translatable((String)"item.mna.crystal_phylactery.tooltip", (Object[])new Object[]{entityType, (int)amount, this.getMaximumFill()}).withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add((Component)Component.translatable((String)"item.mna.crystal_phylactery.tooltip", (Object[])new Object[]{entityType, (int)amount, this.getMaximumFill()}).withStyle(ChatFormatting.DARK_PURPLE));
            }
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.crystal_phylactery.empty").withStyle(ChatFormatting.AQUA));
        }
    }
}

