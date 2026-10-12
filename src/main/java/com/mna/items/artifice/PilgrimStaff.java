 
package com.mna.items.artifice;

import com.mna.api.items.TieredItem;
import com.mna.effects.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class PilgrimStaff
extends TieredItem {
    public PilgrimStaff() {
        super(new Item.Properties().stacksTo(1));
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        MobEffectInstance activeEffect;
        if (entityIn instanceof LivingEntity && ((LivingEntity)entityIn).getOffhandItem().getItem() == this && !worldIn.isClientSide && ((activeEffect = ((LivingEntity)entityIn).getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.PILGRIM.get()))) == null || activeEffect.getDuration() <= 20)) {
            ((LivingEntity)entityIn).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.PILGRIM.get()), 200, 0, false, false));
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}

