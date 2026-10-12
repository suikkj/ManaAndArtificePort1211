 
package com.mna.items.base;

import com.mna.api.items.ManaBatteryItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.api.tools.MNACap;

public interface IManaRepairable {
    public static final float DEFAULT_MANACOST = 0.25f;
    public static final float DEFAULT_REPAIR = 0.01f;

    default public float manaPerRepairTick() {
        return 0.25f;
    }

    default public float repairPerTick() {
        return 0.01f;
    }

    default public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof Player) {
            IManaRepairable.tickRepair(stack, (Player)entityIn, this.manaPerRepairTick(), this.repairPerTick(), itemSlot);
        }
    }

    public static void tickRepair(ItemStack stack, Player player, float manaPerRepairTick, float repairPerTick, int itemSlot) {
        if (stack.isDamaged()) {
            MutableObject consumedMana = new MutableObject(false);
            Optional<ItemStack> manaCrystal = player.getInventory().items.stream().filter(i -> i.getItem() instanceof ManaBatteryItem && ((ManaBatteryItem)i.getItem()).getMana((ItemStack)i) > 0.0f).findFirst();
            if (manaCrystal.isPresent() && ((ManaBatteryItem)manaCrystal.get().getItem()).consumeMana(manaCrystal.get(), manaPerRepairTick, player)) {
                consumedMana.setValue(true);
            }
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                int repairAmount;
                if (!((Boolean)consumedMana.getValue()).booleanValue() && m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, manaPerRepairTick)) {
                    m.getCastingResource().consume((LivingEntity)player, manaPerRepairTick);
                    consumedMana.setValue(true);
                }
                if (((Boolean)consumedMana.getValue()).booleanValue() && (repairAmount = m.bankArmorRepair(itemSlot, repairPerTick)) > 0) {
                    stack.hurtAndBreak(-repairAmount, (LivingEntity)player, p -> {});
                }
            });
        }
    }
}

