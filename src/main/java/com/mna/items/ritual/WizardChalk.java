 
package com.mna.items.ritual;

import com.mna.api.items.TieredItem;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class WizardChalk
extends TieredItem {
    public WizardChalk() {
        super(new Item.Properties().durability(150));
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public boolean isEnchantable(ItemStack p_77616_1_) {
        return false;
    }

    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            if (context.getLevel().getBlockState(context.getClickedPos()).isFaceSturdy((BlockGetter)context.getLevel(), context.getClickedPos(), Direction.UP) && context.getLevel().isEmptyBlock(context.getClickedPos().above())) {
                context.getLevel().setBlockAndUpdate(context.getClickedPos().above(), (BlockState)((BlockState)((BlockState)((Block)BlockInit.CHALK_RUNE.get()).defaultBlockState().setValue(ChalkRuneBlock.RUNEINDEX, Integer.valueOf((int)Math.floor(Math.random() * (double)(ChalkRuneBlock.RUNEINDEX.getPossibleValues().size() - 1))))).setValue(ChalkRuneBlock.METAL, Boolean.valueOf(false))).setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(false)));
                context.getItemInHand().hurtAndBreak(1, (LivingEntity)context.getPlayer(), ((LivingEntity)context.getPlayer()).getEquipmentSlotForItem(context.getItemInHand()));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        int usesRemaining = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add((Component)Component.translatable((String)"item.mna.wizard_chalk_uses", (Object[])new Object[]{usesRemaining}));
    }
}

