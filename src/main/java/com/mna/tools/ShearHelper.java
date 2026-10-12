/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BeehiveBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.entity.EntityTypeTest
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.IShearable
 *  net.minecraftforge.common.capabilities.ForgeCapabilities
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.common.util.FakePlayerFactory
 */
package com.mna.tools;

import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.tools.MATags;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.PumpkinKing;
import com.mna.events.seasonal.SeasonalHelper;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class ShearHelper {
    public static List<ItemStack> shearBlock(ServerLevel world, BlockPos position, Direction face, Player player) {
        return ShearHelper.shearBlock(world, position, face, player, new ItemStack((ItemLike)Items.SHEARS));
    }

    public static List<ItemStack> shearBlock(ServerLevel world, BlockPos position, Direction face, Player player, ItemStack shearStack) {
        List<ItemStack> output = new ArrayList<ItemStack>();
        if (world.isClientSide()) {
            return output;
        }
        if (ShearHelper.shearSpecial(world, position, face)) {
            return output;
        }
        BlockState targetState = world.getBlockState(position);
        if (targetState.getBlock() instanceof IShearable && (output = ShearHelper.shearIShearable((IShearable)targetState.getBlock(), player, position, shearStack, (Level)world)).size() > 0) {
            return output;
        }
        BlockEntity te = world.getBlockEntity(position);
        if (te != null && te instanceof TileEntityWithInventory) {
            return output;
        }
        FakePlayer entity = FakePlayerFactory.get((ServerLevel)world, (GameProfile)player.getGameProfile());
        entity.getInventory().clearContent();
        entity.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.SHEARS));
        entity.setPos((double)position.getX(), (double)position.getY(), (double)position.getZ());
        output = new ArrayList();
        BlockHitResult brtr = new BlockHitResult(new Vec3((double)position.getX() + 0.5, (double)position.getY() + 0.5, (double)position.getZ() + 0.5), face, position, true);
        net.minecraft.world.ItemInteractionResult interRes = world.getBlockState(position).useItemOn((ItemStack)shearStack, (Level)world, (Player)entity, InteractionHand.MAIN_HAND, brtr);
        if (interRes == net.minecraft.world.ItemInteractionResult.FAIL || interRes == net.minecraft.world.ItemInteractionResult.PASS || interRes == net.minecraft.world.ItemInteractionResult.CONSUME) {
            List shearDrops = Block.getDrops((BlockState)targetState, (ServerLevel)world, (BlockPos)position, (BlockEntity)te, (Entity)entity, (ItemStack)shearStack);
            List nonShearDrops = Block.getDrops((BlockState)targetState, (ServerLevel)world, (BlockPos)position, (BlockEntity)te, (Entity)entity, (ItemStack)ItemStack.EMPTY);
            if (shearDrops.size() != nonShearDrops.size()) {
                world.removeBlock(position, false);
                output.addAll(shearDrops);
            } else {
                entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                for (ItemStack item : entity.getInventory().items) {
                    if (item.isEmpty()) continue;
                    output.add(item);
                }
                for (ItemStack item : entity.getInventory().offhand) {
                    if (item.isEmpty()) continue;
                    output.add(item);
                }
            }
        }
        return output;
    }

    public static List<ItemStack> shearEntity(ServerLevel world, Player player, Entity entity, InteractionHand hand) {
        ArrayList<ItemStack> output = new ArrayList<ItemStack>();
        if (entity.level().isClientSide()) {
            return output;
        }
        if (entity instanceof IShearable) {
            return ShearHelper.shearIShearable((IShearable)entity, player, entity.blockPosition(), player.getItemInHand(hand), (Level)world);
        }
        return output;
    }

    public static boolean canBlockBeSheared(Level world, Player player, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof IShearable && ((IShearable)state.getBlock()).isShearable(player, new ItemStack((ItemLike)Items.SHEARS), world, pos)) {
            return true;
        }
        if (state.getBlock() instanceof BeehiveBlock) {
            return (Integer)state.getValue((Property)BeehiveBlock.HONEY_LEVEL) >= 5;
        }
        return MATags.isBlockIn(state.getBlock(), MATags.Blocks.SHEARABLES);
    }

    private static List<ItemStack> shearIShearable(IShearable target, Player player, BlockPos pos, ItemStack heldItem, Level world) {
        List<ItemStack> output = new ArrayList<ItemStack>();
        if (target.isShearable(player, heldItem, world, pos)) {
            output = target.onSheared(player, heldItem, world, pos);
        }
        return output;
    }

    private static boolean shearSpecial(ServerLevel world, BlockPos pos, Direction face) {
        if (!SeasonalHelper.isHalloween()) {
            return false;
        }
        if (world.getEntities((EntityTypeTest)EntityInit.PUMPKIN_KING.get(), new AABB(pos).inflate(16.0), e -> true).size() != 0) {
            return false;
        }
        for (int i = 1; i < 5; ++i) {
            if (world.isEmptyBlock(pos.above(i))) continue;
            return false;
        }
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.PUMPKIN) {
            world.removeBlock(pos, false);
            PumpkinKing eds = new PumpkinKing((Level)world);
            eds.setPos((float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f);
            eds.setupSpawn();
            switch (face) {
                case EAST: {
                    eds.setYRot(270.0f);
                    break;
                }
                case NORTH: {
                    eds.setYRot(-180.0f);
                    break;
                }
                case SOUTH: {
                    eds.setYRot(0.0f);
                    break;
                }
                default: {
                    eds.setYRot(90.0f);
                }
            }
            eds.yRotO = eds.getYRot();
            eds.yBodyRot = eds.getYRot();
            eds.yBodyRotO = eds.getYRot();
            world.addFreshEntity((Entity)eds);
            return true;
        }
        return false;
    }
}

