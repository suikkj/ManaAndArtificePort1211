/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.entity.EntityTypeTest
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.ritual;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.rituals.IRitualReagent;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Ritual;
import com.mna.items.ItemInit;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.tools.DidYouKnowHelper;
import com.mna.tools.InventoryUtilities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChalkRuneBlock
extends WaterloggableBlock
implements ITranslucentBlock,
IDontCreateBlockItem,
EntityBlock {
    protected static final VoxelShape SHAPE = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)0.05, (double)16.0);
    public static final BooleanProperty ACTIVATED = BooleanProperty.create((String)"activated");
    public static final BooleanProperty METAL = BooleanProperty.create((String)"metal");
    public static final IntegerProperty RUNEINDEX = IntegerProperty.create((String)"rune", (int)0, (int)15);
    private int heldItemActivateCount = 0;
    private long lastActivateTime = 0L;

    public ChalkRuneBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.1f).noCollission().noOcclusion().sound(SoundType.WOOL).isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue((Property)ACTIVATED, (Comparable)Boolean.valueOf(false))).setValue((Property)METAL, (Comparable)Boolean.valueOf(false))).setValue((Property)RUNEINDEX, (Comparable)Integer.valueOf((int)Math.random() * 15)));
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.OPEN;
    }

    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        BlockItem blockItem;
        Item item;
        if (((Boolean)pState.getValue((Property)METAL)).booleanValue() && (item = pUseContext.getItemInHand().getItem()) instanceof BlockItem && MATags.isBlockIn((blockItem = (BlockItem)item).getBlock(), MATags.Blocks.SPECTRAL_BLOCKS)) {
            return true;
        }
        return super.canBeReplaced(pState, pUseContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{ACTIVATED, RUNEINDEX, METAL});
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChalkRuneTile(pos, state);
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        List rituals;
        ItemStack activeStack = player.getItemInHand(handIn);
        if (ItemFilterGroup.GROUND_RUNE_NON_PLACEABLE.anyMatchesFilter(activeStack)) {
            return InteractionResult.PASS;
        }
        if (((Boolean)state.getValue((Property)ACTIVATED)).booleanValue() && (rituals = worldIn.getEntities((EntityTypeTest)EntityInit.RITUAL_ENTITY.get(), new AABB(pos).inflate(16.0), e -> true)).size() > 0 && rituals.stream().anyMatch(r -> r.getState() != Ritual.RitualState.GUIDING_REAGENT_PLACEMENT)) {
            return InteractionResult.PASS;
        }
        if (worldIn.isClientSide) {
            if (!activeStack.isEmpty() && ((Boolean)state.getValue((Property)ACTIVATED)).booleanValue()) {
                if (worldIn.getGameTime() - this.lastActivateTime < 200L) {
                    ++this.heldItemActivateCount;
                    if (this.heldItemActivateCount >= 4) {
                        DidYouKnowHelper.CheckAndShowDidYouKnow(player, "helptip.mna.ritual_empty_hand");
                    }
                } else {
                    this.heldItemActivateCount = 0;
                }
                this.lastActivateTime = worldIn.getGameTime();
            }
            return InteractionResult.SUCCESS;
        }
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof ChalkRuneTile) {
            ChalkRuneTile tecr = (ChalkRuneTile)tileEntity;
            if (!tecr.isEmpty()) {
                ItemStack stack = tecr.removeItemNoUpdate(0);
                if (!player.addItem(stack)) {
                    player.drop(stack, true);
                }
                worldIn.sendBlockUpdated(pos, state, state, 2);
                worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            } else {
                IRitualReagent reagent;
                if (activeStack.isEmpty() && (reagent = this.getReagentFromNearbyRitual(worldIn, pos)) != null) {
                    if (!player.isCreative()) {
                        activeStack = InventoryUtilities.removeSingleItemFromInventory(reagent.getResourceLocation(), (Container)player.getInventory());
                    } else {
                        List<Item> possibilities = MATags.smartLookupItem(reagent.getResourceLocation());
                        if (possibilities != null && possibilities.size() > 0) {
                            activeStack = new ItemStack((ItemLike)possibilities.get((int)(Math.random() * (double)possibilities.size())));
                        }
                    }
                }
                if (!activeStack.isEmpty() && tecr.isEmpty()) {
                    ItemStack single = activeStack.copy();
                    single.setCount(1);
                    tecr.setItem(0, single);
                    worldIn.sendBlockUpdated(pos, state, state, 2);
                    worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
                    if (!player.isCreative()) {
                        activeStack.shrink(1);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private IRitualReagent getReagentFromNearbyRitual(Level world, BlockPos pos) {
        AABB bb = new AABB(pos).inflate(5.0);
        List rituals = world.getEntitiesOfClass(Ritual.class, bb);
        if (rituals == null || rituals.size() != 1) {
            return null;
        }
        return ((Ritual)(rituals.get(0))).getReagentForPosition(pos);
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.notifyNearbyRitualsOfRuneDestruction(state, worldIn, pos);
            this.dropInventory(worldIn, pos);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof ChalkRuneTile && !((ChalkRuneTile)tileEntity).isGhostItem()) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
        }
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        if (((Boolean)state.getValue((Property)ACTIVATED)).booleanValue()) {
            return 6;
        }
        return 0;
    }

    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide && fromPos.equals(pos.below()) && !worldIn.getBlockState(fromPos).isSolidRender((BlockGetter)worldIn, fromPos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof ChalkRuneTile && !((ChalkRuneTile)te).getDisplayedItem().isEmpty()) {
            return 15;
        }
        return 0;
    }

    public void notifyNearbyRitualsOfRuneDestruction(BlockState blockState, Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        List rituals = world.getEntities((EntityTypeTest)EntityInit.RITUAL_ENTITY.get(), new AABB(pos).inflate(16.0), e -> true);
        for (Ritual ritual : rituals) {
            ritual.checkForExplosion(false);
        }
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        if (state.hasProperty((Property)METAL) && ((Boolean)state.getValue((Property)METAL)).booleanValue()) {
            return super.canPlaceLiquid(worldIn, pos, state, fluidIn);
        }
        return true;
    }

    @Override
    public boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        if (state.hasProperty((Property)METAL) && !((Boolean)state.getValue((Property)METAL)).booleanValue()) {
            worldIn.setBlock(pos, fluidStateIn.createLegacyBlock(), 3);
            return false;
        }
        return super.placeLiquid(worldIn, pos, state, fluidStateIn);
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (((Boolean)state.getValue((Property)ACTIVATED)).booleanValue()) {
            return new ItemStack((ItemLike)ItemInit.RUNE_RITUAL_METAL.get());
        }
        return new ItemStack((ItemLike)ItemInit.WIZARD_CHALK.get());
    }
}

