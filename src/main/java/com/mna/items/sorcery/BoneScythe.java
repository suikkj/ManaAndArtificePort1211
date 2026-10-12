 
package com.mna.items.sorcery;

import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.items.sorcery.MagicStaff;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;

public class BoneScythe
extends MagicStaff {
    public BoneScythe(float attackDamage) {
        super(attackDamage, -3.0f);
    }

    @Override
    protected void buildAllowedEnchantments(ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> defaultList) {
        defaultList.remove(EnchantmentInit.BLUDGEONING.getKey());
        defaultList.add(Enchantments.SHARPNESS);
        defaultList.add(Enchantments.SWEEPING_EDGE);
    }

    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        if (pState.is(Blocks.COBWEB)) {
            return 15.0f;
        }
        return pState.is(BlockTags.SWORD_EFFICIENT) ? 1.5f : 1.0f;
    }

    public boolean isCorrectToolForDrops(BlockState pBlock) {
        return pBlock.is(Blocks.COBWEB);
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }
}


