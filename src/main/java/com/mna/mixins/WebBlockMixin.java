 
package com.mna.mixins;

import com.mna.effects.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WebBlock.class})
public class WebBlockMixin {
    @Inject(at={@At(value="HEAD")}, method={"entityInside"}, cancellable=true)
    private void mna$entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, CallbackInfo cir) {
        LivingEntity living;
        if (pEntity instanceof LivingEntity && ((living = (LivingEntity)pEntity).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SPIDER_CLIMBING.get())) || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get())))) {
            cir.cancel();
        }
    }
}

