 
package com.mna.items.artifice;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.TieredItem;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.TransitoryTunnelBlock;
import com.mna.blocks.tileentities.TransitoryTunnelTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.tools.BlockUtils;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.mna.api.tools.MNACap;

public class ItemTransitoryTunnel
extends TieredItem {
    public ItemTransitoryTunnel() {
        super(new Item.Properties().stacksTo(1));
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        IPlayerMagic magic = MNACap.get((IPlayerMagic)context.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
        if (!magic.isMagicUnlocked()) {
            return InteractionResult.FAIL;
        }
        BlockPos[] positions = BlockUtils.getBlocksInFrontOfCharacter((LivingEntity)context.getPlayer(), 8, context.getClickedPos());
        ArrayList<BlockPos> checkedPositions = new ArrayList<BlockPos>();
        ArrayList<BlockPos> processedPositions = new ArrayList<BlockPos>();
        for (int i = 1; i < positions.length; ++i) {
            BlockPos pos = positions[i];
            int preLoopCount = processedPositions.size();
            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    for (int z = -1; z <= 1; ++z) {
                        BlockPos operator = pos.offset(x, y, z);
                        if (checkedPositions.contains(operator)) continue;
                        checkedPositions.add(operator);
                        BlockState existing = world.getBlockState(operator);
                        if (existing.getBlock() == BlockInit.TRANSITORY_TUNNEL.get() || world.isEmptyBlock(operator) || existing.getDestroySpeed((BlockGetter)world, operator) < 0.0f || !existing.canOcclude() || world.getBlockEntity(operator) != null) continue;
                        processedPositions.add(operator);
                        world.setBlock(operator, ((TransitoryTunnelBlock)BlockInit.TRANSITORY_TUNNEL.get()).defaultBlockState(), 1);
                        BlockEntity te = world.getBlockEntity(operator);
                        if (te == null || !(te instanceof TransitoryTunnelTile)) continue;
                        ((TransitoryTunnelTile)te).setDurationAndPreviousState(200, existing);
                    }
                }
            }
            if (preLoopCount == processedPositions.size()) break;
        }
        for (BlockPos operator : processedPositions) {
            int mask = TransitoryTunnelBlock.getStateBasedOnSurroundings(world, operator);
            BlockState existing = ((TransitoryTunnelBlock)BlockInit.TRANSITORY_TUNNEL.get()).defaultBlockState();
            BlockState newstate = (BlockState)((TransitoryTunnelBlock)BlockInit.TRANSITORY_TUNNEL.get()).defaultBlockState().setValue((Property)TransitoryTunnelBlock.FACE_VISIBILITY_MASK, (Comparable)Integer.valueOf(mask));
            world.setBlock(operator, newstate, 1);
            world.sendBlockUpdated(operator, existing, newstate, 3);
        }
        if (processedPositions.size() > 0) {
            context.getPlayer().getCooldowns().addCooldown(context.getItemInHand().getItem(), 20);
        }
        return InteractionResult.PASS;
    }
}

