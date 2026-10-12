/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.tools.BlockUtilities;
import com.mna.blocks.TableShape;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TableBlock
extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<TableShape> SHAPE = EnumProperty.create((String)"shape", TableShape.class);
    private static final VoxelShape TABLE_LEG_NE = Block.box((double)10.0, (double)0.0, (double)2.0, (double)14.0, (double)13.0, (double)6.0);
    private static final VoxelShape TABLE_LEG_NW = Block.box((double)2.0, (double)0.0, (double)2.0, (double)6.0, (double)13.0, (double)6.0);
    private static final VoxelShape TABLE_LEG_SE = Block.box((double)10.0, (double)0.0, (double)10.0, (double)14.0, (double)13.0, (double)14.0);
    private static final VoxelShape TABLE_LEG_SW = Block.box((double)2.0, (double)0.0, (double)10.0, (double)6.0, (double)13.0, (double)14.0);
    private static final VoxelShape LEG_BRACE_N = Block.box((double)6.0, (double)6.0, (double)3.0, (double)10.0, (double)8.0, (double)4.0);
    private static final VoxelShape LEG_BRACE_E = Block.box((double)12.0, (double)6.0, (double)6.0, (double)13.0, (double)8.0, (double)10.0);
    private static final VoxelShape LEG_BRACE_W = Block.box((double)3.0, (double)6.0, (double)6.0, (double)4.0, (double)8.0, (double)10.0);
    private static final VoxelShape LEG_BRACE_S = Block.box((double)6.0, (double)6.0, (double)12.0, (double)10.0, (double)8.0, (double)13.0);
    public static final VoxelShape STANDALONE = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)1.0, (double)15.0, (double)16.0, (double)15.0), (VoxelShape[])new VoxelShape[]{TABLE_LEG_NE, TABLE_LEG_NW, TABLE_LEG_SE, TABLE_LEG_SW, LEG_BRACE_N, LEG_BRACE_E, LEG_BRACE_W, LEG_BRACE_S});
    private static final VoxelShape END_N = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)0.0, (double)15.0, (double)16.0, (double)15.0), (VoxelShape[])new VoxelShape[]{Block.box((double)12.0, (double)6.0, (double)0.0, (double)13.0, (double)8.0, (double)10.0), Block.box((double)3.0, (double)6.0, (double)0.0, (double)4.0, (double)8.0, (double)10.0), TABLE_LEG_SE, TABLE_LEG_SW, LEG_BRACE_S});
    public static final VoxelShape STRAIGHT_N = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)0.0, (double)15.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)12.0, (double)6.0, (double)0.0, (double)13.0, (double)8.0, (double)16.0), Block.box((double)3.0, (double)6.0, (double)0.0, (double)4.0, (double)8.0, (double)16.0)});
    public static final VoxelShape CORNER_N = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)15.0), (VoxelShape[])new VoxelShape[]{Block.box((double)3.0, (double)6.0, (double)0.0, (double)4.0, (double)8.0, (double)10.0), Block.box((double)6.0, (double)6.0, (double)12.0, (double)16.0, (double)8.0, (double)13.0), TABLE_LEG_SW});
    public static final VoxelShape CORNER_INTERIOR_N = Shapes.or((VoxelShape)Block.box((double)0.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape)TABLE_LEG_NE);
    public static final VoxelShape CORNER_INTERSECTION_N = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)15.0), (VoxelShape[])new VoxelShape[]{TABLE_LEG_SW, Block.box((double)6.0, (double)6.0, (double)12.0, (double)16.0, (double)8.0, (double)13.0), Block.box((double)3.0, (double)6.0, (double)0.0, (double)4.0, (double)8.0, (double)10.0)});
    public static final VoxelShape T_N = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)3.0, (double)6.0, (double)0.0, (double)4.0, (double)8.0, (double)16.0), TABLE_LEG_SE, TABLE_LEG_NE});
    public static final VoxelShape T_INTERSECTION_N = Shapes.or((VoxelShape)Block.box((double)0.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[]{TABLE_LEG_NW, TABLE_LEG_NE});
    public static final VoxelShape EDGE_N = Shapes.or((VoxelShape)Block.box((double)1.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape)Block.box((double)3.0, (double)6.0, (double)0.0, (double)4.0, (double)8.0, (double)16.0));
    public static final VoxelShape EDGE_INTERIOR_RIGHT_N = Shapes.or((VoxelShape)EDGE_N, (VoxelShape)TABLE_LEG_NE);
    public static final VoxelShape EDGE_INTERIOR_LEFT_N = Shapes.or((VoxelShape)EDGE_N, (VoxelShape)TABLE_LEG_SE);
    public static final VoxelShape CENTER = Block.box((double)0.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);
    public static final VoxelShape CROSS = Shapes.or((VoxelShape)STANDALONE, (VoxelShape)Block.box((double)0.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0));
    public static final VoxelShape CROSS_SINGLE_INTERCARDINAL_N = Shapes.or((VoxelShape)Block.box((double)0.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[]{TABLE_LEG_NW, TABLE_LEG_SW, TABLE_LEG_SE});
    public static final VoxelShape CROSS_DIAGONAL_INTERCARDINAL_N = Shapes.or((VoxelShape)Block.box((double)0.0, (double)13.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape[])new VoxelShape[]{TABLE_LEG_NE, TABLE_LEG_SW});
    private static final HashMap<TableShape, List<VoxelShape>> VoxelLookup = new HashMap();

    public TableBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)Direction.NORTH)).setValue(SHAPE, (Comparable)(TableShape.STANDALONE)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(new Property[]{FACING, SHAPE});
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level blockGetter = pContext.getLevel();
        BlockPos myPos = pContext.getClickedPos();
        return this.calculateStateFromSurroundings(this.defaultBlockState(), (BlockGetter)blockGetter, myPos);
    }

    private BlockState calculateStateFromSurroundings(BlockState input, BlockGetter blockGetter, BlockPos myPos) {
        Direction calculatedFacing = Direction.NORTH;
        TableShape calculatedShape = TableShape.STANDALONE;
        boolean connectEast = this.canConnectTo(blockGetter, myPos, Direction.EAST);
        boolean connectWest = this.canConnectTo(blockGetter, myPos, Direction.WEST);
        boolean connectNorth = this.canConnectTo(blockGetter, myPos, Direction.NORTH);
        boolean connectSouth = this.canConnectTo(blockGetter, myPos, Direction.SOUTH);
        boolean ic_NW = this.checkIntercardinal(blockGetter, myPos, Direction.NORTH, Direction.WEST);
        boolean ic_NE = this.checkIntercardinal(blockGetter, myPos, Direction.NORTH, Direction.EAST);
        boolean ic_SE = this.checkIntercardinal(blockGetter, myPos, Direction.SOUTH, Direction.EAST);
        boolean ic_SW = this.checkIntercardinal(blockGetter, myPos, Direction.SOUTH, Direction.WEST);
        int num_connections = (connectEast ? 1 : 0) + (connectWest ? 1 : 0) + (connectNorth ? 1 : 0) + (connectSouth ? 1 : 0);
        int num_intercardinals = (ic_NW ? 1 : 0) + (ic_NE ? 1 : 0) + (ic_SW ? 1 : 0) + (ic_SE ? 1 : 0);
        block0 : switch (num_connections) {
            case 1: {
                calculatedShape = TableShape.END;
                if (connectEast) {
                    calculatedFacing = Direction.EAST;
                    break;
                }
                if (connectWest) {
                    calculatedFacing = Direction.WEST;
                    break;
                }
                if (connectNorth) {
                    calculatedFacing = Direction.NORTH;
                    break;
                }
                if (!connectSouth) break;
                calculatedFacing = Direction.SOUTH;
                break;
            }
            case 2: {
                if (connectEast && connectEast == connectWest) {
                    calculatedShape = TableShape.STRAIGHT;
                    calculatedFacing = Direction.EAST;
                    break;
                }
                if (connectNorth && connectNorth == connectSouth) {
                    calculatedShape = TableShape.STRAIGHT;
                    calculatedFacing = Direction.NORTH;
                    break;
                }
                if (connectNorth && connectEast) {
                    calculatedFacing = Direction.NORTH;
                    calculatedShape = ic_NE ? TableShape.CORNER_INTERSECTION : TableShape.CORNER;
                    break;
                }
                if (connectSouth && connectEast) {
                    calculatedFacing = Direction.EAST;
                    calculatedShape = ic_SE ? TableShape.CORNER_INTERSECTION : TableShape.CORNER;
                    break;
                }
                if (connectSouth && connectWest) {
                    calculatedFacing = Direction.SOUTH;
                    calculatedShape = ic_SW ? TableShape.CORNER_INTERSECTION : TableShape.CORNER;
                    break;
                }
                calculatedFacing = Direction.WEST;
                calculatedShape = ic_NW ? TableShape.CORNER_INTERSECTION : TableShape.CORNER;
                break;
            }
            case 3: {
                if (connectNorth && connectEast && connectSouth) {
                    calculatedShape = ic_NE && ic_SE ? TableShape.EDGE : (ic_NE ? TableShape.EDGE_INTERIOR_LEFT : (ic_SE ? TableShape.EDGE_INTERIOR_RIGHT : TableShape.T));
                    calculatedFacing = Direction.NORTH;
                    break;
                }
                if (connectWest && connectSouth && connectEast) {
                    calculatedShape = ic_SE && ic_SW ? TableShape.EDGE : (ic_SE ? TableShape.EDGE_INTERIOR_LEFT : (ic_SW ? TableShape.EDGE_INTERIOR_RIGHT : TableShape.T));
                    calculatedFacing = Direction.EAST;
                    break;
                }
                if (connectNorth && connectWest && connectSouth) {
                    calculatedShape = ic_NW && ic_SW ? TableShape.EDGE : (ic_SW ? TableShape.EDGE_INTERIOR_LEFT : (ic_NW ? TableShape.EDGE_INTERIOR_RIGHT : TableShape.T));
                    calculatedFacing = Direction.SOUTH;
                    break;
                }
                calculatedShape = ic_NW && ic_NE ? TableShape.EDGE : (ic_NW ? TableShape.EDGE_INTERIOR_LEFT : (ic_NE ? TableShape.EDGE_INTERIOR_RIGHT : TableShape.T));
                calculatedFacing = Direction.WEST;
                break;
            }
            case 4: {
                switch (num_intercardinals) {
                    case 4: {
                        calculatedShape = TableShape.CENTER;
                        break block0;
                    }
                    case 3: {
                        calculatedShape = TableShape.CORNER_INTERIOR;
                        if (!ic_NE) {
                            calculatedFacing = Direction.NORTH;
                            break block0;
                        }
                        if (!ic_SE) {
                            calculatedFacing = Direction.EAST;
                            break block0;
                        }
                        if (!ic_SW) {
                            calculatedFacing = Direction.SOUTH;
                            break block0;
                        }
                        if (ic_NW) break block0;
                        calculatedFacing = Direction.WEST;
                        break block0;
                    }
                    case 2: {
                        calculatedShape = TableShape.T_INTERSECTION;
                        if (ic_NE && ic_NW) {
                            calculatedFacing = Direction.SOUTH;
                        } else if (ic_SE && ic_SW) {
                            calculatedFacing = Direction.NORTH;
                        } else if (ic_NE && ic_SE) {
                            calculatedFacing = Direction.WEST;
                        } else if (ic_NW && ic_SW) {
                            calculatedFacing = Direction.EAST;
                        }
                        if (ic_NE && ic_SW) {
                            calculatedShape = TableShape.CROSS_DIAGONAL_INTERCARDINAL;
                            calculatedFacing = Direction.EAST;
                            break block0;
                        }
                        if (!ic_NW || !ic_SE) break block0;
                        calculatedShape = TableShape.CROSS_DIAGONAL_INTERCARDINAL;
                        break block0;
                    }
                    case 1: {
                        calculatedShape = TableShape.CROSS_SINGLE_INTERCARDINAL;
                        if (ic_NE) {
                            calculatedFacing = Direction.NORTH;
                            break block0;
                        }
                        if (ic_SE) {
                            calculatedFacing = Direction.EAST;
                            break block0;
                        }
                        if (ic_SW) {
                            calculatedFacing = Direction.SOUTH;
                            break block0;
                        }
                        if (!ic_NW) break block0;
                        calculatedFacing = Direction.WEST;
                        break block0;
                    }
                }
                calculatedShape = TableShape.CROSS;
            }
        }
        return (BlockState)((BlockState)input.setValue((Property)FACING, (Comparable)calculatedFacing)).setValue(SHAPE, (Comparable)(calculatedShape));
    }

    private boolean canConnectTo(BlockGetter blockGetter, BlockPos myPos, Direction dir) {
        BlockState neighborState = blockGetter.getBlockState(myPos.offset(dir.getStepX(), dir.getStepY(), dir.getStepZ()));
        return this.canConnectTo(neighborState, dir);
    }

    private boolean checkIntercardinal(BlockGetter blockGetter, BlockPos myPos, Direction one, Direction two) {
        BlockPos intercardinalCheck = myPos.offset(one.getNormal()).offset(two.getNormal());
        BlockState intercardinalState = blockGetter.getBlockState(intercardinalCheck);
        return intercardinalState.getBlock() instanceof TableBlock;
    }

    private boolean canConnectTo(BlockState neighborState, Direction dir) {
        return neighborState.getBlock() instanceof TableBlock;
    }

    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        return this.calculateStateFromSurroundings(pState, (BlockGetter)pLevel, pPos);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        TableShape tableShape = (TableShape)(pState.getValue(SHAPE));
        Direction dir = (Direction)pState.getValue((Property)FACING);
        if (dir == Direction.DOWN || dir == Direction.UP) {
            return Shapes.block();
        }
        List<VoxelShape> valuesForShape = VoxelLookup.getOrDefault(tableShape, Arrays.asList(Shapes.block()));
        return valuesForShape.get(dir.get2DDataValue() % valuesForShape.size());
    }

    private static List<VoxelShape> createHorizontalPermutations(VoxelShape northShape) {
        return Arrays.asList(BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, northShape), BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, northShape), northShape, BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, northShape));
    }

    static {
        VoxelLookup.put(TableShape.STANDALONE, Arrays.asList(STANDALONE));
        VoxelLookup.put(TableShape.END, TableBlock.createHorizontalPermutations(END_N));
        VoxelLookup.put(TableShape.STRAIGHT, TableBlock.createHorizontalPermutations(STRAIGHT_N));
        VoxelLookup.put(TableShape.CORNER, TableBlock.createHorizontalPermutations(CORNER_N));
        VoxelLookup.put(TableShape.CORNER_INTERIOR, TableBlock.createHorizontalPermutations(CORNER_INTERIOR_N));
        VoxelLookup.put(TableShape.CORNER_INTERSECTION, TableBlock.createHorizontalPermutations(CORNER_INTERSECTION_N));
        VoxelLookup.put(TableShape.T, TableBlock.createHorizontalPermutations(T_N));
        VoxelLookup.put(TableShape.T_INTERSECTION, TableBlock.createHorizontalPermutations(T_INTERSECTION_N));
        VoxelLookup.put(TableShape.EDGE, TableBlock.createHorizontalPermutations(EDGE_N));
        VoxelLookup.put(TableShape.EDGE_INTERIOR_LEFT, TableBlock.createHorizontalPermutations(EDGE_INTERIOR_LEFT_N));
        VoxelLookup.put(TableShape.EDGE_INTERIOR_RIGHT, TableBlock.createHorizontalPermutations(EDGE_INTERIOR_RIGHT_N));
        VoxelLookup.put(TableShape.CENTER, Arrays.asList(CENTER));
        VoxelLookup.put(TableShape.CROSS, Arrays.asList(CROSS));
        VoxelLookup.put(TableShape.CROSS_SINGLE_INTERCARDINAL, TableBlock.createHorizontalPermutations(CROSS_SINGLE_INTERCARDINAL_N));
        VoxelLookup.put(TableShape.CROSS_DIAGONAL_INTERCARDINAL, TableBlock.createHorizontalPermutations(CROSS_DIAGONAL_INTERCARDINAL_N));
    }
}

