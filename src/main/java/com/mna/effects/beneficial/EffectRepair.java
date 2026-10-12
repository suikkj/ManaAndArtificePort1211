 
package com.mna.effects.beneficial;

import com.mna.api.tools.MATags;
import com.mna.effects.interfaces.INoCreeperLingering;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EffectRepair
extends MobEffect
implements INoCreeperLingering {
    public EffectRepair() {
        super(MobEffectCategory.BENEFICIAL, 15436816);
    }

    public boolean isInstantenous() {
        return false;
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof Player) {
            Player player = (Player)entityLivingBaseIn;
            if (entityLivingBaseIn.level().getGameTime() % 5L == 0L) {
                for (ItemStack stack : this.getRepairables((Container)player.getInventory())) {
                    stack.setDamageValue(stack.getDamageValue() - 1);

        return true;
    }
            }
        }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private List<ItemStack> getRepairables(Container inventory) {
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !stack.isRepairable() || !stack.isDamaged() || MATags.isItemIn(stack.getItem(), MATags.Items.REPAIR_BLACKLIST)) continue;
            stacks.add(stack);
        }
        return stacks;
    }
}

