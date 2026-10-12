 
package com.mna.entities.utility;

import com.mna.entities.EntityInit;
import com.mna.tools.BlockUtils;
import com.mna.tools.InventoryUtilities;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class FillHole
extends Entity {
    private static final String NBT_BLOCK = "place_block";
    private static final String NBT_POSITIONS = "positions";
    private static final String NBT_INDEX = "place_index";
    private static final String NBT_RADIUS = "radius";
    private static final String NBT_HEIGHT = "height";
    private static final String NBT_FINISHED_SCAN = "finished_scan";
    private static final String NBT_PLAYER_ID = "player_uuid";
    private Block placeBlock;
    private ArrayList<BlockPos> positions = new ArrayList();
    private int placeIndex = 0;
    private byte radius = 0;
    private byte height = 0;
    private boolean isValid = false;
    private boolean finishedScan = false;
    private UUID playerID = null;
    private Player player = null;

    public FillHole(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
        this.blocksBuilding = false;
    }

    public FillHole(Level world, Block toPlace, BlockPos center, Player caster, int height, int radius) {
        this((EntityType)EntityInit.FILL_HOLE.get(), world);
        this.setData(toPlace, center, caster, height, radius);
    }

    public void tick() {
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.isValid) {
            this.discard();
            return;
        }
        if (!this.finishedScan) {
            if (this.placeIndex < this.height) {
                if (!this.scanLayer(this.blockPosition().offset(0, this.placeIndex++, 0))) {
                    this.positions = (ArrayList)this.positions.stream().map(bp -> bp.asLong()).distinct().map(l -> BlockPos.of((long)l)).collect(Collectors.toList());
                    this.finishedScan = true;
                    this.placeIndex = 0;
                }
            } else {
                this.positions = (ArrayList)this.positions.stream().map(bp -> bp.asLong()).distinct().map(l -> BlockPos.of((long)l)).collect(Collectors.toList());
                this.placeIndex = 0;
                this.finishedScan = true;
            }
        } else if (!this.tryPlaceBlock()) {
            this.discard();
        }
    }

    private boolean tryPlaceBlock() {
        Player player = this.resolvePlayer();
        if (player == null) {
            return false;
        }
        if (this.placeIndex >= this.positions.size()) {
            return false;
        }
        BlockPos placePos = this.positions.get(this.placeIndex++);
        ItemStack stack = new ItemStack((ItemLike)this.placeBlock);
        BlockHitResult bhr = new BlockHitResult(Vec3.atCenterOf((Vec3i)placePos), this.getDirection(), placePos, true);
        BlockPlaceContext ctx = new BlockPlaceContext(player, InteractionHand.OFF_HAND, stack, bhr);
        BlockState prevState = this.level().getBlockState(placePos);
        if (this.level().isUnobstructed((Entity)this) && prevState.canBeReplaced(ctx) && BlockUtils.placeBlock((ServerLevel)this.level(), placePos, Direction.DOWN, this.placeBlock.defaultBlockState(), player) && !player.isCreative() && !InventoryUtilities.removeItemFromInventory(stack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()))) {
            this.level().setBlock(placePos, prevState, 3);
            return false;
        }
        return true;
    }

    @Nullable
    private Player resolvePlayer() {
        if (this.playerID == null) {
            return null;
        }
        if (this.player == null) {
            this.player = this.level().getPlayerByUUID(this.playerID);
        }
        return this.player;
    }

    public void setData(Block toPlace, BlockPos center, Player caster, int height, int radius) {
        this.placeBlock = toPlace;
        this.setPos((float)center.getX() + 0.5f, (float)center.getY() + 0.5f, (float)center.getZ() + 0.5f);
        this.height = (byte)height;
        this.radius = (byte)radius;
        if (caster.getGameProfile() != null) {
            this.playerID = caster.getGameProfile().getId();
            this.player = caster;
            this.isValid = true;
        } else {
            this.isValid = false;
        }
    }

    private boolean scanLayer(BlockPos center) {
        ArrayList<BlockPos> foundPositions = new ArrayList<BlockPos>();
        BlockHitResult posZ = this.level().clip(new ClipContext(Vec3.atCenterOf((Vec3i)center), Vec3.atCenterOf((Vec3i)center.offset(0, 0, (int)this.radius)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
        BlockHitResult negZ = this.level().clip(new ClipContext(Vec3.atCenterOf((Vec3i)center), Vec3.atCenterOf((Vec3i)center.offset(0, 0, (int)(-this.radius))), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
        BlockHitResult posX = this.level().clip(new ClipContext(Vec3.atCenterOf((Vec3i)center), Vec3.atCenterOf((Vec3i)center.offset((int)this.radius, 0, 0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
        BlockHitResult negX = this.level().clip(new ClipContext(Vec3.atCenterOf((Vec3i)center), Vec3.atCenterOf((Vec3i)center.offset((int)(-this.radius), 0, 0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
        if (!(posZ.getType() != HitResult.Type.MISS && negZ.getType() != HitResult.Type.MISS || posX.getType() != HitResult.Type.MISS && negX.getType() != HitResult.Type.MISS)) {
            return false;
        }
        int rPZ = posZ.getType() == HitResult.Type.MISS ? this.radius : Math.abs(center.getZ() - posZ.getBlockPos().getZ());
        int rNZ = negZ.getType() == HitResult.Type.MISS ? -this.radius : -Math.abs(center.getZ() - negZ.getBlockPos().getZ());
        int rPX = posX.getType() == HitResult.Type.MISS ? this.radius : Math.abs(center.getX() - posX.getBlockPos().getX());
        int rNX = negX.getType() == HitResult.Type.MISS ? -this.radius : -Math.abs(center.getX() - negX.getBlockPos().getX());
        int zLen = rPZ - rNZ;
        int xLen = rPX - rNX;
        boolean zAxis = zLen >= xLen;
        for (int i = zAxis ? rNZ : rNX; i < (zAxis ? rPZ : rPX); ++i) {
            BlockPos offsetPos = center.offset(zAxis ? 0 : i, 0, zAxis ? i : 0);
            BlockPos rayEndPos = offsetPos.offset((int)(zAxis ? this.radius : (byte)0), 0, (int)(zAxis ? (byte)0 : this.radius));
            BlockPos rayEndNeg = offsetPos.offset((int)(zAxis ? -this.radius : (byte)0), 0, zAxis ? 0 : (int)(-this.radius));
            BlockHitResult posPass = this.level().clip(new ClipContext(Vec3.atCenterOf((Vec3i)offsetPos), Vec3.atCenterOf((Vec3i)rayEndPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
            BlockHitResult negPass = this.level().clip(new ClipContext(Vec3.atCenterOf((Vec3i)offsetPos), Vec3.atCenterOf((Vec3i)rayEndNeg), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
            if (posPass.getType() == HitResult.Type.MISS && negPass.getType() == HitResult.Type.MISS) continue;
            int posPassOffset = posPass.getType() == HitResult.Type.MISS ? 0 : (int)Math.sqrt(offsetPos.distSqr((Vec3i)posPass.getBlockPos()));
            int negPassOffset = negPass.getType() == HitResult.Type.MISS ? 0 : (int)(-Math.sqrt(offsetPos.distSqr((Vec3i)negPass.getBlockPos())));
            for (int o = negPassOffset + 1; o <= posPassOffset; ++o) {
                BlockPos passStepOffset = offsetPos.offset(zAxis ? o : 0, 0, zAxis ? 0 : o);
                foundPositions.add(passStepOffset);
            }
        }
        this.positions.addAll(foundPositions);
        return true;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains(NBT_BLOCK) && nbt.contains(NBT_POSITIONS) && nbt.contains(NBT_INDEX) && nbt.contains(NBT_RADIUS) && nbt.contains(NBT_HEIGHT) && nbt.contains(NBT_FINISHED_SCAN) && nbt.contains(NBT_PLAYER_ID)) {
            this.placeBlock = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbt.getString(NBT_BLOCK)));
            if (this.placeBlock != null) {
                ListTag list = nbt.getList(NBT_POSITIONS, 4);
                list.forEach(l -> this.positions.add(BlockPos.of((long)((LongTag)l).getAsLong())));
                this.placeIndex = nbt.getInt(NBT_INDEX);
                this.radius = nbt.getByte(NBT_RADIUS);
                this.height = nbt.getByte(NBT_HEIGHT);
                this.finishedScan = nbt.getBoolean(NBT_FINISHED_SCAN);
                this.playerID = UUID.fromString(nbt.getString(NBT_PLAYER_ID));
                this.isValid = true;
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag nbt) {
        if (this.placeBlock == null && !this.isValid) {
            return;
        }
        nbt.putString(NBT_BLOCK, BuiltInRegistries.BLOCK.getKey(this.placeBlock).toString());
        nbt.putInt(NBT_INDEX, this.placeIndex);
        nbt.putByte(NBT_HEIGHT, this.height);
        nbt.putByte(NBT_RADIUS, this.radius);
        nbt.putString(NBT_PLAYER_ID, this.playerID.toString());
        nbt.putBoolean(NBT_FINISHED_SCAN, this.finishedScan);
        ListTag positions = new ListTag();
        this.positions.forEach(bp -> positions.add(LongTag.valueOf((long)bp.asLong())));
        nbt.put(NBT_POSITIONS, (Tag)positions);
    }
}


