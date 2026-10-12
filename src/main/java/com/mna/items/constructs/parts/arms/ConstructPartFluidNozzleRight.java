 
package com.mna.items.constructs.parts.arms;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.FluidParameterRegistry;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.events.construct.ConstructSprayEffectEvent;
import com.mna.api.tools.MATags;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class ConstructPartFluidNozzleRight
extends ItemConstructPart {
    public ConstructPartFluidNozzleRight(ConstructMaterial material) {
        super(material, ConstructSlot.RIGHT_ARM, 64);
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE};
    }

    @Override
    public float getAttackDamage() {
        return 0.0f;
    }

    @Override
    public int getAttackSpeedModifier() {
        return 10;
    }

    @Override
    public float getRangedAttackDamage() {
        return 0.0f;
    }

    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return UseAnim.BOW;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FluidStack backpackFluid = this.getBackpackFluid((LivingEntity)player);
        if (!backpackFluid.isEmpty()) {
            player.startUsingItem(hand);
            world.playSound(player, player.getX(), player.getY(), player.getZ(), FluidParameterRegistry.INSTANCE.forFluid(backpackFluid.getFluid()).spraySound(), SoundSource.NEUTRAL, 0.25f, (float)(0.9 + Math.random() * 0.1));
            return InteractionResultHolder.consume(stack);
        }
        if (!player.level().isClientSide()) {
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.constructs.construct_fluid_nozzle.no_backpack"));
            player.getCooldowns().addCooldown((Item)this, 60);
        }
        return InteractionResultHolder.pass(stack);
    }

    public void onUseTick(Level world, LivingEntity living, ItemStack stack, int ticksRemaining) {
        FluidStack backpackFluid;
        if (ticksRemaining < 2 && living instanceof Player) {
            ((Player)living).getCooldowns().addCooldown((Item)this, 60);
            ((Player)living).stopUsingItem();
        }
        if (!(backpackFluid = this.getBackpackFluid(living)).isEmpty() && this.consumeBackpackFluid(living)) {
            FluidParameterRegistry.FluidParameter flParam = FluidParameterRegistry.INSTANCE.forFluid(backpackFluid.getFluid());
            if (world.isClientSide) {
                for (int i = 0; i < flParam.getParticleQuantity(); ++i) {
                    Vec3 fwd = living.getForward();
                    Vec3 particlePos = living.position().add(0.0, (double)(living.getEyeHeight() / 5.0f * 4.0f), 0.0).add(fwd);
                    Vec3 vel = flParam.adjustVelocity(fwd);
                    world.addParticle(flParam.particle(), particlePos.x, particlePos.y, particlePos.z, vel.x, vel.y, vel.z);
                }
            } else {
                AABB bb = living.getBoundingBox().inflate(0.0, 1.0, 0.0);
                Vec3 look = living.getForward();
                bb = bb.expandTowards(look.scale(8.0));
                world.getEntities((Entity)living, bb, e -> {
                    if (!(e instanceof LivingEntity) || e == living) {
                        return false;
                    }
                    return living.hasLineOfSight(e);
                }).stream().map(e -> (LivingEntity)e).forEach(e -> {
                    ConstructSprayEffectEvent effectEvt = new ConstructSprayEffectEvent(living, (LivingEntity)e, backpackFluid.getFluid(), false, false);
                    NeoForge.EVENT_BUS.post(effectEvt);
                });
            }
        } else {
            ((Player)living).getCooldowns().addCooldown((Item)this, 60);
            ((Player)living).stopUsingItem();
        }
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int ticksRemaining) {
        if (living instanceof Player) {
            ((Player)living).getCooldowns().addCooldown((Item)this, 60);
        }
    }

    private FluidStack getBackpackFluid(LivingEntity living) {
        Optional equipped = CuriosApi.getCuriosHelper().findFirstCurio(living, is -> MATags.isItemIn(is.getItem(), MATags.Items.FLUID_ARM_PLAYER_TANKS));
        if (equipped.isEmpty()) {
            return FluidStack.EMPTY;
        }
        Optional fluid = FluidUtil.getFluidContained((ItemStack)((SlotResult)equipped.get()).stack());
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return (FluidStack)fluid.get();
    }

    private boolean consumeBackpackFluid(LivingEntity living) {
        Optional equipped = CuriosApi.getCuriosHelper().findFirstCurio(living, is -> MATags.isItemIn(is.getItem(), MATags.Items.FLUID_ARM_PLAYER_TANKS));
        if (equipped.isEmpty()) {
            return false;
        }
        java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> fluid = FluidUtil.getFluidHandler((ItemStack)((SlotResult)equipped.get()).stack());
        if (!fluid.isPresent()) {
            return false;
        }
        if (((IFluidHandlerItem)fluid.get()).drain(10, IFluidHandler.FluidAction.SIMULATE).getAmount() == 10) {
            ((IFluidHandlerItem)fluid.get()).drain(10, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
}


