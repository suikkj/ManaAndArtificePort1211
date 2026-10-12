 
package com.mna.items.sorcery;

import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.TieredItem;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SightUnguent
extends TieredItem
implements IFactionSpecific {
    final int power;

    public SightUnguent(Item.Properties properties, int power) {
        super(new Item.Properties().durability(10).setNoRepair());
        this.power = power;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        this.ApplyUnguent(worldIn, playerIn, handIn);
        return InteractionResultHolder.pass(playerIn.getItemInHand(handIn));
    }

    public InteractionResult useOn(UseOnContext context) {
        this.ApplyUnguent(context.getLevel(), context.getPlayer(), context.getHand());
        return InteractionResult.SUCCESS;
    }

    private void ApplyUnguent(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide) {
            return;
        }
        if (this.power > 0) {
            this.usedByPlayer(player);
            player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_SIGHT.get()), 3600, 0));
        } else {
            player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.WELLSPRING_SIGHT.get()), 12000, 0));
        }
        player.getItemInHand(hand).hurtAndBreak(1, player, hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.translatable((String)"item.mna.sight_unguent.flavor_text").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
    }

    @Override
    public IFaction getFaction() {
        return this.power == 0 ? null : Factions.COUNCIL;
    }
}

