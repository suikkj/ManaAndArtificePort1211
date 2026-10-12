 
package com.mna.items.constructs;

import com.mna.api.entities.construct.IConstruct;
import com.mna.api.items.TieredItem;
import com.mna.tools.math.MathUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ConstructRepairKit
extends TieredItem {
    public ConstructRepairKit() {
        super(new Item.Properties().setNoRepair().durability(10));
    }

    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (pPlayer.getCooldowns().isOnCooldown((Item)this)) {
            return InteractionResult.PASS;
        }
        if (pInteractionTarget instanceof IConstruct) {
            IConstruct construct = (IConstruct)pInteractionTarget;
            construct.asEntity().heal(20.0f);
            if (!pPlayer.level().isClientSide()) {
                pStack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)pPlayer.level(), pPlayer, is -> {});
                pPlayer.getCooldowns().addCooldown((Item)this, 100);
                pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 1.0f, MathUtils.RandomBetween(0.9f, 1.1f));
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }
}

