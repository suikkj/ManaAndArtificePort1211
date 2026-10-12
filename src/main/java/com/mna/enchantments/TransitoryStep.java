 
package com.mna.enchantments;

import com.mna.blocks.BlockInit;
import com.mna.blocks.sorcery.TransitoryTileBlock;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.enchantments.base.MAEnchantmentBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mna.api.enchantments.MAEnchantmentBridge;

public class TransitoryStep
extends MAEnchantmentBase {
    private static final float BLOCK_MANA_COST = 10.0f;

    public TransitoryStep(MAEnchantmentBridge.Rarity rarityIn) {
    }

    public boolean canEnchant(ItemStack pStack) {
        return com.mna.api.enchantments.MAEnchantmentBridge.isBoots(pStack.getItem());
    }

    protected boolean checkCompatibility(Enchantment ench) {
        return true;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public boolean apply(Player source, int enchantmentLevel) {
        MutableBoolean success = new MutableBoolean(false);
        BlockPos target = source.blockPosition().below().relative(source.getMotionDirection());
        if (source.level().isEmptyBlock(target) && source.onGround() && !source.isShiftKeyDown()) {
            MNACap.get(source, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                if (m.getCastingResource().hasEnoughAbsolute((LivingEntity)source, 10.0f)) {
                    m.getCastingResource().consume((LivingEntity)source, 10.0f);
                    source.level().setBlockAndUpdate(target, (BlockState)((TransitoryTileBlock)BlockInit.TRANSITORY_TILE.get()).defaultBlockState().setValue(TransitoryTileBlock.DURATION, Integer.valueOf(1)));
                    source.level().scheduleTick(target, (Block)BlockInit.TRANSITORY_TILE.get(), 20);
                    success.setTrue();
                }
            });
        }
        return success.booleanValue();
    }
}

