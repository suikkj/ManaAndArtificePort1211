/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.items.artifice;

import com.mna.api.items.ChargeableItem;
import com.mna.items.artifice.curio.IPreEnchantedItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WitherguardAmulet
extends ChargeableItem
implements IPreEnchantedItem<WitherguardAmulet> {
    public WitherguardAmulet(Item.Properties properties, float maxMana) {
        super(properties, maxMana);
    }

    @Override
    protected boolean tickEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        if (player.getEffect(MobEffects.WITHER) != null) {
            player.removeEffectNoUpdate(MobEffects.WITHER);
            return true;
        }
        return false;
    }
}

