 
package com.mna.mixins;

import com.mna.enchantments.framework.EnchantmentInit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PiglinAi.class})
public class PiglinGoldMixin {
    @Inject(at={@At(value="RETURN")}, method={"isWearingGold"}, cancellable=true)
    private static void curios$isWearingGold(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        int level = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.GILDED, (LivingEntity)livingEntity);
        if (level > 0) {
            cir.setReturnValue(true);
        }
    }
}

