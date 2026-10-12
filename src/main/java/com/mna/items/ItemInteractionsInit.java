/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.cauldron.CauldronInteraction
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.LayeredCauldronBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.minecraftforge.fluids.FluidStack
 *  net.minecraftforge.fluids.FluidUtil
 *  net.minecraftforge.fluids.capability.IFluidHandler$FluidAction
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package com.mna.items;

import com.mna.items.ItemInit;
import com.mna.items.artifice.FluidJug;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class ItemInteractionsInit {
    public static CauldronInteraction LEATHER_TO_VELLUM = (state, world, pos, player, hand, stack) -> {
        if (!world.isClientSide) {
            ItemStack vellum = new ItemStack((ItemLike)ItemInit.VELLUM.get(), 2);
            stack.shrink(1);
            if (!player.addItem(vellum)) {
                player.drop(vellum, true);
            }
            LayeredCauldronBlock.lowerFillLevel((BlockState)state, (Level)world, (BlockPos)pos);
        }
        return InteractionResult.sidedSuccess((boolean)world.isClientSide);
    };

    static InteractionResult emptyFluidJug(BlockState pBlockState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, ItemStack stack, FluidStack toExtract, BlockState targetState, SoundEvent pFillSound) {
        if (!pLevel.isClientSide()) {
            MutableBoolean success = new MutableBoolean(false);
            FluidUtil.getFluidHandler((ItemStack)stack).ifPresent(h -> {
                if (h.drain(toExtract, IFluidHandler.FluidAction.SIMULATE).getAmount() == toExtract.getAmount()) {
                    h.drain(toExtract, IFluidHandler.FluidAction.EXECUTE);
                    pPlayer.awardStat(Stats.USE_CAULDRON);
                    pPlayer.awardStat(Stats.ITEM_USED.get(((Item)ItemInit.FLUID_JUG.get())));
                    pLevel.setBlockAndUpdate(pPos, targetState);
                    pLevel.playSound((Player)null, pPos, pFillSound, SoundSource.BLOCKS, 1.0f, 1.0f);
                    pLevel.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, pPos);
                    success.setTrue();
                }
            });
            return success.getValue() != false ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    static InteractionResult fillFluidJug(BlockState pBlockState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, ItemStack stack, FluidStack toInsert, Predicate<BlockState> pStatePredicate, SoundEvent pFillSound) {
        if (!pStatePredicate.test(pBlockState)) {
            return InteractionResult.PASS;
        }
        if (!pLevel.isClientSide()) {
            MutableBoolean success = new MutableBoolean(false);
            FluidUtil.getFluidHandler((ItemStack)stack).ifPresent(h -> {
                if (h.fill(toInsert, IFluidHandler.FluidAction.SIMULATE) == toInsert.getAmount()) {
                    h.fill(toInsert, IFluidHandler.FluidAction.EXECUTE);
                    pPlayer.awardStat(Stats.USE_CAULDRON);
                    pPlayer.awardStat(Stats.ITEM_USED.get(((Item)ItemInit.FLUID_JUG.get())));
                    pLevel.setBlockAndUpdate(pPos, Blocks.CAULDRON.defaultBlockState());
                    pLevel.playSound((Player)null, pPos, pFillSound, SoundSource.BLOCKS, 1.0f, 1.0f);
                    pLevel.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, pPos);
                    success.setTrue();
                }
            });
            return success.getValue() != false ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    @SubscribeEvent
    public static void setupInteractions(FMLCommonSetupEvent event) {
        CauldronInteraction.WATER.map().put(Items.LEATHER, LEATHER_TO_VELLUM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.MAGE_BOOTS.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.MAGE_HOOD.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.MAGE_LEGGINGS.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.MAGE_ROBES.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.RECIPE_COPY_BOOK.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.BANGLE.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.PRACTITIONERS_POUCH.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.RUNE_MARKING.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.RUNE_MARKING_PAIR.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.SPELL_BOOK.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.ROTE_BOOK.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.WATER.map().put((Item)ItemInit.GRIMOIRE.get(), CauldronInteraction.DYED_ITEM);
        CauldronInteraction.EMPTY.map().put((Item)ItemInit.FLUID_JUG.get(), (blockState, level, pos, player, hand, stack) -> {
            FluidStack contained = ((FluidJug)ItemInit.FLUID_JUG.get()).getFluidTagData(stack);
            if (contained.isEmpty() || contained.getAmount() < 1000 || contained.getFluid() != Fluids.WATER && contained.getFluid() != Fluids.LAVA) {
                return InteractionResult.FAIL;
            }
            FluidStack toExtract = contained.copy();
            toExtract.setAmount(1000);
            BlockState targetState = contained.getFluid() == Fluids.WATER ? (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue((Property)LayeredCauldronBlock.LEVEL, (Comparable)Integer.valueOf(3)) : Blocks.LAVA_CAULDRON.defaultBlockState();
            return ItemInteractionsInit.emptyFluidJug(blockState, level, pos, player, hand, stack, toExtract, targetState, SoundEvents.BUCKET_EMPTY);
        });
        CauldronInteraction.EMPTY.map().put((Item)ItemInit.FLUID_JUG_INFINITE_WATER.get(), (blockState, level, pos, player, hand, stack) -> ItemInteractionsInit.emptyFluidJug(blockState, level, pos, player, hand, stack, new FluidStack((Fluid)Fluids.WATER, 1000), (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue((Property)LayeredCauldronBlock.LEVEL, (Comparable)Integer.valueOf(3)), SoundEvents.BUCKET_EMPTY));
        CauldronInteraction.EMPTY.map().put((Item)ItemInit.FLUID_JUG_INFINITE_LAVA.get(), (blockState, level, pos, player, hand, stack) -> ItemInteractionsInit.emptyFluidJug(blockState, level, pos, player, hand, stack, new FluidStack((Fluid)Fluids.LAVA, 1000), Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY));
        CauldronInteraction.WATER.map().put((Item)ItemInit.FLUID_JUG.get(), (blockState, level, pos, player, hand, stack) -> ItemInteractionsInit.fillFluidJug(blockState, level, pos, player, hand, stack, new FluidStack((Fluid)Fluids.WATER, 1000), predicateBlockState -> (Integer)predicateBlockState.getValue((Property)LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        CauldronInteraction.WATER.map().put((Item)ItemInit.FLUID_JUG_INFINITE_WATER.get(), (blockState, level, pos, player, hand, stack) -> ItemInteractionsInit.fillFluidJug(blockState, level, pos, player, hand, stack, new FluidStack((Fluid)Fluids.WATER, 1000), predicateBlockState -> (Integer)predicateBlockState.getValue((Property)LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        CauldronInteraction.LAVA.map().put((Item)ItemInit.FLUID_JUG.get(), (blockState, level, pos, player, hand, stack) -> ItemInteractionsInit.fillFluidJug(blockState, level, pos, player, hand, stack, new FluidStack((Fluid)Fluids.LAVA, 1000), predicateBlockState -> true, SoundEvents.BUCKET_FILL));
        CauldronInteraction.LAVA.map().put((Item)ItemInit.FLUID_JUG_INFINITE_LAVA.get(), (blockState, level, pos, player, hand, stack) -> ItemInteractionsInit.fillFluidJug(blockState, level, pos, player, hand, stack, new FluidStack((Fluid)Fluids.LAVA, 1000), predicateBlockState -> true, SoundEvents.BUCKET_FILL));
    }
}


