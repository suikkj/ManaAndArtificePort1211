 
package com.mna.blocks.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.blocks.tileentities.FluidJugTile;
import com.mna.items.ItemInit;
import com.mna.items.artifice.FluidJug;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.world.item.alchemy.PotionContents;

public class FluidJugBlock
extends HorizontalDirectionalBlock
implements EntityBlock,
IDontCreateBlockItem,
SimpleWaterloggedBlock {
    public static final ResourceLocation JUG = ResourceLocation.parse("jug");
    private static final VoxelShape SHAPE = Block.box((double)5.0, (double)0.0, (double)5.0, (double)11.0, (double)16.0, (double)11.0);
    private final boolean is_infinite;
    private final ResourceLocation infinite_fluid_type;

    public FluidJugBlock(boolean is_infinite, ResourceLocation fluidType) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).noOcclusion().strength(2.0f));
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)));
        this.is_infinite = is_infinite;
        this.infinite_fluid_type = fluidType;
    }

    public FluidJugBlock() {
        this(false, null);
    }

    public final boolean is_infinite() {
        return this.is_infinite;
    }

    @Nullable
    public final ResourceLocation getFluidType() {
        return this.infinite_fluid_type;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidJugTile(pos, state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
        builder.add(new Property[]{BlockStateProperties.WATERLOGGED});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState base = (BlockState)this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
        BlockPos blockpos = ctx.getClickedPos();
        BlockState blockstate = ctx.getLevel().getBlockState(blockpos);
        if (blockstate.is((Block)this)) {
            return (BlockState)blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
        }
        FluidState fluidstate = ctx.getLevel().getFluidState(blockpos);
        return (BlockState)base.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED) != false ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return super.canPlaceLiquid(worldIn, pos, state, fluidIn);
    }

    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (((Boolean)stateIn.getValue((Property)BlockStateProperties.WATERLOGGED)).booleanValue()) {
            worldIn.scheduleTick(currentPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)worldIn));
        }
        return stateIn;
    }

    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        switch (type) {
            case LAND: {
                return false;
            }
            case WATER: {
                return worldIn.getFluidState(pos).is(FluidTags.WATER);
            }
            case AIR: {
                return false;
            }
        }
        return false;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos position, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.FAIL;
        }
        java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> handler = FluidUtil.getFluidHandler((Level)world, (BlockPos)position, (Direction)Direction.DOWN);
        if (handler.isPresent()) {
            if (heldItem.getItem() == Items.GLASS_BOTTLE) {
                BlockEntity be;
                FluidStack blockFluidStack = ((IFluidHandler)handler.get()).getFluidInTank(0);
                int amt = blockFluidStack.getAmount();
                if (amt >= 100 && (be = world.getBlockEntity(position)) != null && be instanceof FluidJugTile) {
                    FluidJugTile fluidTile = (FluidJugTile)be;
                    fluidTile.drain(100, IFluidHandler.FluidAction.EXECUTE);
                    player.setItemInHand(hand, ItemUtils.createFilledResult((ItemStack)heldItem, (Player)player, (ItemStack)com.mna.api.tools.MNAItemData.setPotionContents(new ItemStack((ItemLike)Items.POTION), new PotionContents(Potions.WATER))));
                    player.swing(hand);
                    return InteractionResult.sidedSuccess((boolean)world.isClientSide);
                }
            } else if (FluidUtil.interactWithFluidHandler((Player)player, (InteractionHand)hand, (IFluidHandler)((IFluidHandler)handler.get()))) {
                return InteractionResult.sidedSuccess((boolean)world.isClientSide);
            }
        }
        return InteractionResult.FAIL;
    }

    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity living, ItemStack stack) {
        BlockEntity be;
        FluidStack flStack;
        if (!this.is_infinite && stack.has(DataComponents.CUSTOM_DATA) && !(flStack = ((FluidJug)ItemInit.FLUID_JUG.get()).getFluidTagData(stack)).isEmpty() && (be = world.getBlockEntity(pos)) != null && be instanceof FluidJugTile) {
            FluidJugTile fluidTile = (FluidJugTile)be;
            fluidTile.fill(flStack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        FluidJugTile jug;
        FluidStack jugFluid;
        BlockEntity blockentity;
        if (!this.is_infinite && (blockentity = (BlockEntity)builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)) instanceof FluidJugTile && !(jugFluid = (jug = (FluidJugTile)blockentity).getFluidInTank(0)).isEmpty()) {
            ItemStack jugStack = new ItemStack((ItemLike)ItemInit.FLUID_JUG.get());
            FluidUtil.getFluidHandler((ItemStack)jugStack).ifPresent(f -> f.fill(jugFluid, IFluidHandler.FluidAction.EXECUTE));
            return Arrays.asList(jugStack);
        }
        return super.getDrops(state, builder);
    }
}


