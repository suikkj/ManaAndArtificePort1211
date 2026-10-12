 
package com.mna.api.blocks;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class PylonBlock
extends WaterloggableBlockWithOffset
implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected HashMap<Affinity, Float> powerRequirements = new HashMap();
    protected float radius = 32.0f;
    protected String runeword;
    private static final VoxelShape MAIN_WE = Block.box((double)4.0, (double)0.0, (double)0.0, (double)12.0, (double)16.0, (double)16.0);
    private static final VoxelShape MAIN_NS = Block.box((double)0.0, (double)0.0, (double)4.0, (double)16.0, (double)16.0, (double)12.0);
    private static final VoxelShape OFFSET_1_WE = Block.box((double)5.0, (double)0.0, (double)0.0, (double)11.0, (double)16.0, (double)16.0);
    private static final VoxelShape OFFSET_1_NS = Block.box((double)0.0, (double)0.0, (double)5.0, (double)16.0, (double)16.0, (double)11.0);
    private static final VoxelShape OFFSET_2_WE = Block.box((double)5.0, (double)0.0, (double)2.0, (double)11.0, (double)16.0, (double)14.0);
    private static final VoxelShape OFFSET_2_NS = Block.box((double)2.0, (double)0.0, (double)5.0, (double)14.0, (double)16.0, (double)11.0);

    public PylonBlock(BlockBehaviour.Properties properties, String runeword, BlockPos ... offsets) {
        super(properties, false, offsets);
        this.runeword = runeword;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity block = pLevel.getBlockEntity(pPos);
        if (block != null && block instanceof PylonTileBase) {
            PylonTileBase pylonTile = (PylonTileBase)block;
            ItemStack stack = pPlayer.getItemInHand(pHand);
            if (!stack.isEmpty()) {
                Item debugOrb = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "debug_wand"));
                Item item = stack.getItem();
                if (item instanceof DyeItem) {
                    DyeItem dye = (DyeItem)item;
                    if (!pLevel.isClientSide()) {
                        int clr = dye.getDyeColor().getTextColor();
                        pylonTile.setColor(new int[]{FastColor.ARGB32.red((int)clr), FastColor.ARGB32.green((int)clr), FastColor.ARGB32.blue((int)clr)});
                    }
                } else if (stack.getItem() == debugOrb && !pLevel.isClientSide()) {
                    pylonTile.setMapMakerMode();
                    pPlayer.sendSystemMessage((Component)Component.literal((String)"Pylon map maker mode enabled.  Eldrin power requirements removed, and ownership information deleted."));
                }
            }
            if (!pLevel.isClientSide && !ManaAndArtificeMod.checkMagicUnlock((ServerLevel)pLevel, pPlayer)) {
                pPlayer.openMenu(pylonTile, (BlockPos)pPos);
            }
        }
        return InteractionResult.sidedSuccess((boolean)pLevel.isClientSide);
    }

    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity living, ItemStack stack) {
        if (living instanceof Player) {
            Player player = (Player)living;
            PylonTileBase tile = (PylonTileBase)world.getBlockEntity(pos);
            tile.setOwner(player);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)super.getStateForPlacement(context).setValue((Property)FACING, (Comparable)context.getHorizontalDirection().getOpposite());
    }

    public PylonBlock addPowerRequirement(Affinity aff, float amount) {
        this.powerRequirements.put(aff, Float.valueOf(Math.max(amount, 0.0f)));
        return this;
    }

    public PylonBlock setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public float getRadius() {
        return this.radius;
    }

    public HashMap<Affinity, Float> getPowerConsumption() {
        return this.powerRequirements;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = (Direction)pState.getValue((Property)FACING);
        boolean sideways = facing == Direction.EAST || facing == Direction.WEST;
        return sideways ? MAIN_WE : MAIN_NS;
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState state, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        boolean sideways = facing == Direction.EAST || facing == Direction.WEST;
        switch (delta.getY()) {
            case 1: {
                return sideways ? OFFSET_1_WE : OFFSET_1_NS;
            }
            case 2: {
                return sideways ? OFFSET_2_WE : OFFSET_2_NS;
            }
        }
        return Shapes.block();
    }
}


