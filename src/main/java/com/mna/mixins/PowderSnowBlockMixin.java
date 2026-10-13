 
package com.mna.mixins;

import com.mna.enchantments.framework.EnchantmentInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PowderSnowBlock.class})
public class PowderSnowBlockMixin {
    @Inject(at={@At(value="HEAD")}, method={"canEntityWalkOnPowderSnow"}, cancellable=true)
    private static void mna$canEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            int level = com.mna.api.enchantments.MNAEnchantmentHelper.getEnchantmentLevel(EnchantmentInit.CLOUDSTEP.getKey(), (LivingEntity)living);
            if (level > 0) {
                cir.setReturnValue(true);
            }
        }
    }
}

