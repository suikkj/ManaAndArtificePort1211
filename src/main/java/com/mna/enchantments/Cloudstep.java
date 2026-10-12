 
package com.mna.enchantments;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNACap;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mna.api.enchantments.MAEnchantmentBridge;

public class Cloudstep
extends MAEnchantmentBase {
    public Cloudstep(MAEnchantmentBridge.Rarity rarityIn) {
    }

    public boolean canEnchant(ItemStack pStack) {
        return com.mna.api.enchantments.MAEnchantmentBridge.isBoots(pStack.getItem());
    }

    protected boolean checkCompatibility(Enchantment ench) {
        return true;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 3;
    }

    public static void apply(Player source, int enchantmentLevel) {
        if (source.isInWater()) {
            return;
        }
        MNACap.get(source, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (m.getAirJumps() < enchantmentLevel + 1 && mnaNoJumpDelay(source) == 0) {
                m.incrementAirJumps(source);
                source.jumpFromGround();
            }
        });
    }

    private static int mnaNoJumpDelay(net.minecraft.world.entity.LivingEntity entity) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("noJumpDelay");
            f.setAccessible(true);
            return (Integer)f.get(entity);
        } catch (ReflectiveOperationException e) {
            return 1;
        }
    }
}
