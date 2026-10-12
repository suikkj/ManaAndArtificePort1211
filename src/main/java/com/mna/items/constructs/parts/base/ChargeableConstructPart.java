 
package com.mna.items.constructs.parts.base;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.items.IShowHud;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public abstract class ChargeableConstructPart
extends ItemConstructPart
implements IShowHud {
    public ChargeableConstructPart(ConstructMaterial material, ConstructSlot slot, int modelTypeMutex) {
        super(material, slot, modelTypeMutex);
    }

    public abstract float getChargeCost();

    public int getChargeDuration(ItemStack stack) {
        return 25;
    }

    public abstract void onChargeReleased(ItemStack var1, Level var2, LivingEntity var3);

    private final float getTickChargeCost(ItemStack stack) {
        return this.getChargeCost() / (float)this.getChargeDuration(stack);
    }

    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (m.getCastingResource().hasEnough((LivingEntity)player, this.getChargeCost())) {
                player.startUsingItem(hand);
            }
        });
        return InteractionResultHolder.consume(itemstack);
    }

    public void onUseTick(Level world, LivingEntity living, ItemStack stack, int ticksRemaining) {
        if (ticksRemaining < 2 || !(living instanceof Player)) {
            if (living instanceof Player && !world.isClientSide) {
                this.onChargeReleased(stack, world, living);
                ((Player)living).getCooldowns().addCooldown((Item)this, 20);
            }
            living.stopUsingItem();
            return;
        }
        java.util.Optional<com.mna.api.capabilities.IPlayerMagic> cap = MNACap.get((Player)living, PlayerMagicProvider.MAGIC);
        if (!cap.isPresent()) {
            living.stopUsingItem();
            return;
        }
        IPlayerMagic magic = (IPlayerMagic)cap.get();
        if (!magic.getCastingResource().hasEnough(living, this.getTickChargeCost(stack))) {
            living.stopUsingItem();
            return;
        }
        magic.getCastingResource().consume(living, this.getTickChargeCost(stack));
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int ticksRemaining) {
        if (ticksRemaining == 0) {
             
        }
    }
}


