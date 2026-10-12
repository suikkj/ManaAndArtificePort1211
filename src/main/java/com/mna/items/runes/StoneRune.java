 
package com.mna.items.runes;

import com.mna.ManaAndArtifice;
import com.mna.api.items.TieredItem;
import com.mna.api.spells.ICanContainSpell;
import com.mna.items.sorcery.ItemSpell;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class StoneRune
extends TieredItem
implements ICanContainSpell {
    public StoneRune() {
        super(new Item.Properties());
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, world, lines, flags);
        ICanContainSpell.super.getSpell(stack).addItemTooltip(stack, world.level(), lines, ManaAndArtifice.instance.proxy.getClientPlayer());
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> castRes = ItemSpell.castSpellOnUse(held, world, player, hand, this::shouldConsumeMana);
        if (castRes.getResult() == InteractionResult.SUCCESS && !world.isClientSide && player.getUseItem() != held) {
            held.shrink(1);
            if (held.isEmpty() && !player.isCreative()) {
                player.onEquippedItemBroken(held, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
        return castRes;
    }

    public int getUseDuration(ItemStack p_41454_) {
        return 99999;
    }

    public void onUseTick(Level pLevel, LivingEntity player, ItemStack stack, int count) {
        if (player instanceof Player) {
            ItemSpell.castSpellOnChannelTick(stack, (Player)player, count, this::shouldConsumeChanneledMana);
        }
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (entityLiving instanceof Player) {
            ((Player)entityLiving).releaseUsingItem();
        }
        stack.shrink(1);
        return stack;
    }

    public boolean isFoil(ItemStack p_41453_) {
        return SpellRecipe.stackContainsSpell(p_41453_);
    }

    protected boolean shouldConsumeMana(ItemStack stack) {
        return false;
    }

    protected boolean shouldConsumeChanneledMana(Player player, ItemStack stack) {
        return false;
    }
}

