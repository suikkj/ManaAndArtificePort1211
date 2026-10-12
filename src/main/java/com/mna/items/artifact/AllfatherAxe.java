 
package com.mna.items.artifact;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.IShowHud;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.boss.attacks.ThrownAllfatherAxe;
import com.mna.items.ItemInit;
import com.mna.items.artifact.AllfatherAxeControl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class AllfatherAxe
extends AxeItem
implements IShowHud {
    private static final float THROW_MANA_COST = 50.0f;

    public AllfatherAxe() {
        super((Tier)Tiers.NETHERITE, 10.0f, 1.0f, new Item.Properties().rarity(Rarity.EPIC));
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        MutableBoolean didThrow = new MutableBoolean(false);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (m.getCastingResource().hasEnough((LivingEntity)player, 50.0f)) {
                m.getCastingResource().consume((LivingEntity)player, 50.0f);
                didThrow.setTrue();
                if (!world.isClientSide) {
                    ItemStack playerStack = player.getItemInHand(hand);
                    ThrownAllfatherAxe thrown = new ThrownAllfatherAxe((LivingEntity)player, world, playerStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                    Vec3 look = player.getLookAngle();
                    thrown.shoot(look.x(), look.y, look.z, 2.0f, 0.0f);
                    world.addFreshEntity((Entity)thrown);
                    ItemStack control = new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE_CONTROL.get());
                    AllfatherAxeControl.setAxe(control, thrown);
                    player.getCooldowns().addCooldown((Item)ItemInit.ALLFATHER_AXE_CONTROL.get(), 10);
                    player.setItemInHand(hand, control);
                }
            }
        });
        if (didThrow.getValue().booleanValue()) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}

