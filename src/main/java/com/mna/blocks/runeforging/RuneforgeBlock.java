/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.CriteriaTriggers
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.runeforging;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.RuneForgeTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.items.ItemInit;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RuneforgeBlock
extends WaterloggableBlockWithOffset
implements EntityBlock {
    public static final int MATERIAL_STONE = 0;
    public static final int MATERIAL_BRIMSTONE = 1;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create((String)"active");
    public static final BooleanProperty ORE_DOUBLING = BooleanProperty.create((String)"doubling");
    public static final BooleanProperty REPAIR = BooleanProperty.create((String)"repair");
    public static final BooleanProperty SPEED = BooleanProperty.create((String)"speed");
    public static final IntegerProperty MATERIAL = IntegerProperty.create((String)"material", (int)0, (int)1);
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)2.0, (double)0.0, (double)2.0, (double)14.0, (double)1.0, (double)14.0), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)1.0, (double)4.0, (double)12.0, (double)2.0, (double)12.0), Block.box((double)5.0, (double)2.0, (double)5.0, (double)11.0, (double)13.5, (double)11.0), Block.box((double)1.0, (double)13.5, (double)1.0, (double)15.0, (double)16.5, (double)15.0)});

    public RuneforgeBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER).requiresCorrectToolForDrops(), false, new BlockPos[0]);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(ACTIVE, Boolean.valueOf(false))).setValue(REPAIR, Boolean.valueOf(false))).setValue(MATERIAL, Integer.valueOf(0)));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RuneForgeTile(pos, state);
    }

    public VoxelShape getShape(BlockState p_48816_, BlockGetter p_48817_, BlockPos p_48818_, CollisionContext p_48819_) {
        return SHAPE;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.RUNEFORGE.get() ? (lvl, pos, state1, be) -> RuneForgeTile.Tick(lvl, pos, state1, (RuneForgeTile)be) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, ACTIVE, REPAIR, SPEED, MATERIAL, ORE_DOUBLING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        ItemStack activeStack = player.getItemInHand(handIn);
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof RuneForgeTile) {
            RuneForgeTile te = (RuneForgeTile)tileEntity;
            if (activeStack.getItem() == ItemInit.BRIMSTONE_CHARM.get()) {
                if ((Integer)state.getValue((Property)MATERIAL) == 0) {
                    if (!worldIn.isClientSide) {
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, activeStack);
                        }
                        worldIn.setBlock(pos, (BlockState)state.setValue(MATERIAL, Integer.valueOf(1)), 3);
                        worldIn.playSound(null, pos, SFX.Spell.Buff.FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        worldIn.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f);
                        if (!player.isCreative()) {
                            activeStack.shrink(1);
                        }
                    } else {
                        for (int i = 0; i < 150; ++i) {
                            worldIn.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), (double)pos.getX() + Math.random(), (double)pos.getY() + Math.random() * 2.0, (double)pos.getZ() + Math.random(), 0.0, Math.random() * 0.01 + 0.01, 0.0);
                        }
                    }
                }
            } else if (!te.isEmpty()) {
                if (!worldIn.isClientSide) {
                    ItemStack stack = te.removeItemNoUpdate(0);
                    if (player instanceof ServerPlayer) {
                        CustomAdvancementTriggers.ARCANE_FURNACE_SMELT.trigger((ServerPlayer)player, stack);
                    }
                    if (!player.addItem(stack)) {
                        player.drop(stack, true);
                    }
                    worldIn.sendBlockUpdated(pos, state, state, 2);
                }
            } else if (!worldIn.isClientSide && !activeStack.isEmpty() && te.isEmpty()) {
                ItemStack insert = activeStack.copy();
                int quantity = Math.min(16, insert.getCount());
                insert.setCount(quantity);
                te.setItem(0, insert);
                worldIn.sendBlockUpdated(pos, state, state, 2);
                if (!player.isCreative()) {
                    activeStack.shrink(quantity);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        if (((Boolean)state.getValue((Property)ACTIVE)).booleanValue()) {
            return 15;
        }
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.dropInventory(worldIn, pos, state);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos, BlockState state) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof RuneForgeTile) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
        }
        if (state.getBlock() == this && (Integer)state.getValue((Property)MATERIAL) == 1) {
            Containers.dropContents((Level)world, (BlockPos)pos, (NonNullList)NonNullList.of(ItemStack.EMPTY, (Object[])new ItemStack[]{new ItemStack((ItemLike)ItemInit.BRIMSTONE_CHARM.get())}));
        }
    }
}

