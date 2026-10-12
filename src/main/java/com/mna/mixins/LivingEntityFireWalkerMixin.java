 
package com.mna.mixins;

import com.mna.effects.EffectInit;
import com.mna.items.ItemInit;
import com.mna.items.armor.DemonArmorItem;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityFireWalkerMixin
implements ILivingEntityExtension {
    @Inject(at={@At(value="RETURN")}, method={"canStandOnFluid"}, cancellable=true)
    public void mna$canStandOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (state.getTags().anyMatch(t -> t == FluidTags.LAVA) && ((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped(self)) {
            cir.setReturnValue(!self.isCrouching());
        }
        if (state.getTags().anyMatch(t -> t == FluidTags.WATER) && self.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.WATER_WALKING.get())) && self.getFluidTypeHeight((NeoForgeMod.WATER_TYPE).value()) < self.getFluidJumpThreshold() * 1.5) {
            cir.setReturnValue(!self.isCrouching());
        }
    }

    @Inject(at={@At(value="RETURN")}, method={"isAffectedByFluids"}, cancellable=true)
    public void mna$isAffectedByFluids(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.isInLava() && ((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped(self)) {
            cir.setReturnValue(!self.isInLava());
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"jumpInLiquid"}, cancellable=true)
    public void mna$jumpInLiquid(TagKey<Fluid> fluid, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.isInLava() && ((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped(self)) {
            ((Player)self).jumpFromGround();
            ci.cancel();
        }
    }

    public void jumpInFluid(FluidType fluid) {
        LivingEntity self = this.self();
        if (self.isInLava() && ((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped(self) && self instanceof Player) {
            ((Player)self).jumpFromGround();
        } else {
            self.setDeltaMovement(self.getDeltaMovement().add(0.0, (double)0.04f * self.getAttributeValue(NeoForgeMod.SWIM_SPEED), 0.0));
        }
    }
}


