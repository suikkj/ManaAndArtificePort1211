/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.items.runes;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.items.TieredItem;
import com.mna.entities.utility.ThrownRunescribingPattern;
import com.mna.items.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RunePattern
extends TieredItem {
    private static final String NBT_KEY_HMUTEX = "hmutex";
    private static final String NBT_KEY_VMUTEX = "vmutex";
    private static final String NBT_KEY_USES = "uses";
    private static final int max_uses = 4;

    public RunePattern() {
        super(new Item.Properties());
    }

    public RunePattern(Item.Properties properties) {
        super(properties);
    }

    public long getHMutex(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return 0L;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getLong(NBT_KEY_HMUTEX);
    }

    public void setHMutex(ItemStack stack, long hMutex) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong(NBT_KEY_HMUTEX, hMutex);
    }

    public long getVMutex(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return 0L;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getLong(NBT_KEY_VMUTEX);
    }

    public void setVMutex(ItemStack stack, long vMutex) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong(NBT_KEY_VMUTEX, vMutex);
    }

    public int getBarWidth(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_KEY_USES)) {
            return 13 - Math.round(13.0f * ((float)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(NBT_KEY_USES) / 4.0f));
        }
        return 0;
    }

    public int getBarColor(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_KEY_USES)) {
            float f = Math.max(0.0f, (4.0f - (float)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(NBT_KEY_USES)) / 4.0f);
            return Mth.hsvToRgb((float)(f / 3.0f), (float)1.0f, (float)1.0f);
        }
        return Mth.hsvToRgb((float)0.0f, (float)1.0f, (float)1.0f);
    }

    public boolean isBarVisible(ItemStack stack) {
        return this.getBarWidth(stack) > 0;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (itemstack.getItem() != ItemInit.RUNE_PATTERN.get() || this.getHMutex(itemstack) == 0L && this.getVMutex(itemstack) == 0L) {
            return InteractionResultHolder.fail(itemstack);
        }
        worldIn.playSound((Player)null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (worldIn.random.nextFloat() * 0.4f + 0.8f));
        if (!worldIn.isClientSide) {
            ThrownRunescribingPattern dangit = new ThrownRunescribingPattern(worldIn, (LivingEntity)playerIn);
            dangit.setItem(itemstack);
            dangit.shootFromRotation((Entity)playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0f, 1.5f, 1.0f);
            worldIn.addFreshEntity((Entity)dangit);
            CustomAdvancementTriggers.THROWN_RUNESCRIBE_PATTERN.trigger((ServerPlayer)playerIn);
        }
        playerIn.awardStat(Stats.ITEM_USED.get(this));
        if (!playerIn.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(itemstack, (boolean)worldIn.isClientSide());
    }

    public static boolean incrementDamage(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int uses = nbt.getInt(NBT_KEY_USES) + 1;
        nbt.putInt(NBT_KEY_USES, uses);
        return uses == 4;
    }
}

