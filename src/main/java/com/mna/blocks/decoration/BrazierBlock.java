/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.decoration;

import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.BrazierTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mna.api.tools.MNADyeableItem;

public class BrazierBlock
extends WaterloggableBlock
implements EntityBlock,
ISpellInteractibleBlock<BrazierBlock> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create((String)"active");
    public static final BooleanProperty AUTO_ACTIVATE = BooleanProperty.create((String)"auto_activate");
    public static final DirectionProperty FACING = DirectionProperty.create((String)"facing", (Direction[])Direction.values());
    protected static final VoxelShape SHAPE = Block.box((double)0.0, (double)1.0, (double)0.0, (double)16.0, (double)8.0, (double)16.0);

    public BrazierBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f, 10.0f).noOcclusion().randomTicks().sound(SoundType.LANTERN).isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(ACTIVE, Boolean.valueOf(false))).setValue(AUTO_ACTIVATE, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{ACTIVE, AUTO_ACTIVATE, FACING});
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (((Boolean)state.getValue((Property)AUTO_ACTIVATE)).booleanValue()) {
            BlockState newstate = state;
            if (worldIn.isNight() && !((Boolean)state.getValue((Property)ACTIVE)).booleanValue()) {
                newstate = (BlockState)state.setValue(ACTIVE, Boolean.valueOf(true));
            } else if (worldIn.isDay() && ((Boolean)state.getValue((Property)ACTIVE)).booleanValue()) {
                newstate = (BlockState)state.setValue(ACTIVE, Boolean.valueOf(false));
            }
            worldIn.setBlock(pos, newstate, 1);
            worldIn.sendBlockUpdated(pos, state, newstate, 2);
            worldIn.scheduleTick(pos, (Block)this, 20);
        }
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        if (!worldIn.isClientSide) {
            BlockState newstate = state;
            if (player.isCrouching()) {
                newstate = (BlockState)state.setValue(AUTO_ACTIVATE, Boolean.valueOf((Boolean)state.getValue(AUTO_ACTIVATE) == false));
                if (((Boolean)newstate.getValue((Property)AUTO_ACTIVATE)).booleanValue()) {
                    player.sendSystemMessage((Component)Component.translatable((String)"block.mna.brazier.auto_on"));
                } else {
                    player.sendSystemMessage((Component)Component.translatable((String)"block.mna.brazier.auto_off"));
                }
                worldIn.scheduleTick(pos, (Block)this, 20);
            } else {
                newstate = (BlockState)state.setValue(ACTIVE, Boolean.valueOf((Boolean)state.getValue(ACTIVE) == false));
            }
            worldIn.setBlock(pos, newstate, 1);
            worldIn.sendBlockUpdated(pos, state, newstate, 2);
        }
        return InteractionResult.SUCCESS;
    }

    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (((Boolean)stateIn.getValue((Property)ACTIVE)).booleanValue()) {
            int color = -1;
            BlockEntity be = worldIn.getBlockEntity(pos);
            if (be != null && be instanceof BrazierTile) {
                color = ((BrazierTile)be).getColor();
            }
            for (int i = 0; i < 20; ++i) {
                MAParticleType pfx = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setMaxAge((int)(20.0 + 20.0 * Math.random())).setScale(0.03f);
                if (color == -1) {
                    pfx.setColor(30, 172, 255);
                } else {
                    pfx.setColor(FastColor.ARGB32.red((int)color), FastColor.ARGB32.green((int)color), FastColor.ARGB32.blue((int)color), FastColor.ARGB32.alpha((int)color));
                }
                worldIn.addParticle((ParticleOptions)pfx, (double)((float)pos.getX() + 0.5f + (-0.175f + worldIn.getRandom().nextFloat() * 0.35f)), (double)pos.getY() + 0.2, (double)((float)pos.getZ() + 0.5f + (-0.175f + worldIn.getRandom().nextFloat() * 0.35f)), 0.0, (double)0.02f, 0.0);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        BlockState superState = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        Direction myFacing = (Direction)stateIn.getValue((Property)FACING);
        if (!this.isDirectionValidAnchor((LevelReader)worldIn, currentPos, myFacing)) {
            Direction newFacing = null;
            for (Direction dir : Direction.values()) {
                if (!this.isDirectionValidAnchor((LevelReader)worldIn, currentPos, dir)) continue;
                newFacing = dir;
                break;
            }
            if (newFacing == null) {
                newFacing = Direction.DOWN;
            }
            superState = (BlockState)superState.setValue(FACING, newFacing);
        }
        return superState;
    }

    private boolean isDirectionValidAnchor(LevelReader world, BlockPos pos, Direction facing) {
        BlockPos facingPos = pos.relative(facing);
        BlockState facingBlockState = world.getBlockState(facingPos);
        return facingBlockState.isFaceSturdy((BlockGetter)world, facingPos, facing.getOpposite());
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        if (((Boolean)state.getValue((Property)ACTIVE)).booleanValue()) {
            return 14;
        }
        return 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BrazierTile(pPos, pState);
    }

    @Override
    public boolean onHitBySpell(Level world, BlockPos pos, ISpellDefinition spell) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be != null && be instanceof BrazierTile && spell.containsPart(Components.LIGHT.getRegistryName())) {
            // [PORT 1.21.1] MNADyeableItem.setColor agora exige ItemStack (sem stack neste contexto)
            return true;
        }
        return false;
    }
}

