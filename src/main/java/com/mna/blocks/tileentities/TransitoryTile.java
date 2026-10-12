/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.sorcery.TransitoryTileBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TransitoryTile
extends BlockEntity {
    private float topFaceAlpha = 0.0f;
    private boolean wasSteppedOn = false;
    private byte neighborMatrix = 0;
    private int age = 0;
    private int color = -1;

    public TransitoryTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TransitoryTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.TRANSITORY_TILE.get(), pos, state);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, TransitoryTile tile) {
        ++tile.age;
        if (tile.getLevel().isClientSide()) {
            tile.topFaceAlpha = tile.getLevel().getEntitiesOfClass(Entity.class, new AABB(tile.worldPosition.above())).size() > 0 ? 1.0f : (tile.topFaceAlpha -= 0.05f);
            if (!tile.wasSteppedOn && tile.topFaceAlpha > 0.0f) {
                tile.particleBurst();
            }
            tile.wasSteppedOn = tile.topFaceAlpha > 0.0f;
            for (Direction d : Direction.values()) {
                if (tile.hasNeighborOnSide(d)) continue;
                tile.SpawnParticlesForFace(pos, d);
            }
        } else if (tile.age > (Integer)tile.getBlockState().getValue((Property)TransitoryTileBlock.DURATION) * 20) {
            tile.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private void SpawnParticlesForFace(BlockPos pos, Direction face) {
        if (ManaAndArtifice.instance.proxy.isGamePaused()) {
            return;
        }
        Vec3 offset = null;
        RandomSource rand = this.level.random;
        switch (face) {
            case DOWN: {
                if (rand.nextBoolean()) {
                    if (!this.hasNeighborOnSide(Direction.EAST) && !this.hasNeighborOnSide(Direction.WEST)) {
                        offset = new Vec3(rand.nextBoolean() ? 1.0 : 0.0, 0.0, rand.nextDouble());
                        break;
                    }
                    if (!this.hasNeighborOnSide(Direction.EAST)) {
                        offset = new Vec3(1.0, 0.0, rand.nextDouble());
                        break;
                    }
                    if (this.hasNeighborOnSide(Direction.WEST)) break;
                    offset = new Vec3(0.0, 0.0, rand.nextDouble());
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH) && !this.hasNeighborOnSide(Direction.SOUTH)) {
                    offset = new Vec3(rand.nextDouble(), 0.0, rand.nextBoolean() ? 1.0 : 0.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH)) {
                    offset = new Vec3(rand.nextDouble(), 0.0, 0.0);
                    break;
                }
                if (this.hasNeighborOnSide(Direction.SOUTH)) break;
                offset = new Vec3(rand.nextDouble(), 0.0, 1.0);
                break;
            }
            case EAST: {
                if (rand.nextBoolean()) {
                    if (!this.hasNeighborOnSide(Direction.UP) && !this.hasNeighborOnSide(Direction.DOWN)) {
                        offset = new Vec3(1.0, rand.nextBoolean() ? 1.0 : 0.0, rand.nextDouble());
                        break;
                    }
                    if (!this.hasNeighborOnSide(Direction.UP)) {
                        offset = new Vec3(1.0, 1.0, rand.nextDouble());
                        break;
                    }
                    if (this.hasNeighborOnSide(Direction.DOWN)) break;
                    offset = new Vec3(1.0, 0.0, rand.nextDouble());
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH) && !this.hasNeighborOnSide(Direction.SOUTH)) {
                    offset = new Vec3(1.0, rand.nextDouble(), rand.nextBoolean() ? 1.0 : 0.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH)) {
                    offset = new Vec3(1.0, rand.nextDouble(), 0.0);
                    break;
                }
                if (this.hasNeighborOnSide(Direction.SOUTH)) break;
                offset = new Vec3(1.0, rand.nextDouble(), 1.0);
                break;
            }
            case WEST: {
                if (rand.nextBoolean()) {
                    if (!this.hasNeighborOnSide(Direction.UP) && !this.hasNeighborOnSide(Direction.DOWN)) {
                        offset = new Vec3(0.0, rand.nextBoolean() ? 1.0 : 0.0, rand.nextDouble());
                        break;
                    }
                    if (!this.hasNeighborOnSide(Direction.UP)) {
                        offset = new Vec3(0.0, 1.0, rand.nextDouble());
                        break;
                    }
                    if (this.hasNeighborOnSide(Direction.DOWN)) break;
                    offset = new Vec3(0.0, 0.0, rand.nextDouble());
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH) && !this.hasNeighborOnSide(Direction.SOUTH)) {
                    offset = new Vec3(0.0, rand.nextDouble(), rand.nextBoolean() ? 1.0 : 0.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH)) {
                    offset = new Vec3(0.0, rand.nextDouble(), 0.0);
                    break;
                }
                if (this.hasNeighborOnSide(Direction.SOUTH)) break;
                offset = new Vec3(0.0, rand.nextDouble(), 1.0);
                break;
            }
            case NORTH: {
                if (rand.nextBoolean()) {
                    if (!this.hasNeighborOnSide(Direction.UP) && !this.hasNeighborOnSide(Direction.DOWN)) {
                        offset = new Vec3(rand.nextDouble(), rand.nextBoolean() ? 1.0 : 0.0, 0.0);
                        break;
                    }
                    if (!this.hasNeighborOnSide(Direction.UP)) {
                        offset = new Vec3(rand.nextDouble(), 1.0, 0.0);
                        break;
                    }
                    if (this.hasNeighborOnSide(Direction.DOWN)) break;
                    offset = new Vec3(rand.nextDouble(), 0.0, 0.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.EAST) && !this.hasNeighborOnSide(Direction.WEST)) {
                    offset = new Vec3(rand.nextBoolean() ? 1.0 : 0.0, rand.nextDouble(), 0.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.EAST)) {
                    offset = new Vec3(1.0, rand.nextDouble(), 0.0);
                    break;
                }
                if (this.hasNeighborOnSide(Direction.WEST)) break;
                offset = new Vec3(0.0, rand.nextDouble(), 0.0);
                break;
            }
            case SOUTH: {
                if (rand.nextBoolean()) {
                    if (!this.hasNeighborOnSide(Direction.UP) && !this.hasNeighborOnSide(Direction.DOWN)) {
                        offset = new Vec3(rand.nextDouble(), rand.nextBoolean() ? 1.0 : 0.0, 1.0);
                        break;
                    }
                    if (!this.hasNeighborOnSide(Direction.UP)) {
                        offset = new Vec3(rand.nextDouble(), 1.0, 1.0);
                        break;
                    }
                    if (this.hasNeighborOnSide(Direction.DOWN)) break;
                    offset = new Vec3(rand.nextDouble(), 0.0, 1.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.EAST) && !this.hasNeighborOnSide(Direction.WEST)) {
                    offset = new Vec3(rand.nextBoolean() ? 1.0 : 0.0, rand.nextDouble(), 1.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.EAST)) {
                    offset = new Vec3(1.0, rand.nextDouble(), 1.0);
                    break;
                }
                if (this.hasNeighborOnSide(Direction.WEST)) break;
                offset = new Vec3(0.0, rand.nextDouble(), 1.0);
                break;
            }
            case UP: {
                if (rand.nextBoolean()) {
                    if (!this.hasNeighborOnSide(Direction.EAST) && !this.hasNeighborOnSide(Direction.WEST)) {
                        offset = new Vec3(rand.nextBoolean() ? 1.0 : 0.0, 1.0, rand.nextDouble());
                        break;
                    }
                    if (!this.hasNeighborOnSide(Direction.EAST)) {
                        offset = new Vec3(1.0, 1.0, rand.nextDouble());
                        break;
                    }
                    if (this.hasNeighborOnSide(Direction.WEST)) break;
                    offset = new Vec3(0.0, 1.0, rand.nextDouble());
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH) && !this.hasNeighborOnSide(Direction.SOUTH)) {
                    offset = new Vec3(rand.nextDouble(), 1.0, rand.nextBoolean() ? 1.0 : 0.0);
                    break;
                }
                if (!this.hasNeighborOnSide(Direction.NORTH)) {
                    offset = new Vec3(rand.nextDouble(), 1.0, 0.0);
                    break;
                }
                if (this.hasNeighborOnSide(Direction.SOUTH)) break;
                offset = new Vec3(rand.nextDouble(), 1.0, 1.0);
            }
        }
        if (offset != null) {
            MAParticleType pfx = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get()));
            if (this.color != -1) {
                pfx.setColor(FastColor.ARGB32.red((int)this.color), FastColor.ARGB32.green((int)this.color), FastColor.ARGB32.blue((int)this.color));
            }
            this.getLevel().addParticle((ParticleOptions)pfx, (double)pos.getX() + offset.x, (double)pos.getY() + offset.y, (double)pos.getZ() + offset.z, 0.0, 0.0, 0.0);
        }
    }

    public void setNeighborOnSide(Direction side) {
        if (side != null) {
            this.neighborMatrix = (byte)(this.neighborMatrix | 1 << side.ordinal());
            if (!this.getLevel().isClientSide()) {
                this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public void clearNeighborOnSide(Direction side) {
        if (side != null) {
            this.neighborMatrix = (byte)(this.neighborMatrix & ~(1 << side.ordinal()));
            if (!this.getLevel().isClientSide()) {
                this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public boolean hasNeighborOnSide(Direction side) {
        if (side == null) {
            return false;
        }
        byte value = (byte)(1 << side.ordinal());
        return (this.neighborMatrix & value) != 0;
    }

    public float topFaceAlpha() {
        return this.topFaceAlpha;
    }

    public void saveAdditional(CompoundTag data, net.minecraft.core.HolderLookup.Provider registries) {
        data.putByte("neighbors", this.neighborMatrix);
        data.putInt("color", this.color);
    }

    public void loadAdditional(CompoundTag data, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(data, registries);
        if (data.contains("neighbors")) {
            this.neighborMatrix = data.getByte("neighbors");
        }
        if (data.contains("color")) {
            this.color = data.getInt("color");
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, this.level.registryAccess());
        return tag;
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag data = pkt.getTag();
        if (data.contains("neighbors")) {
            this.neighborMatrix = data.getByte("neighbors");
        }
        if (data.contains("color")) {
            this.color = data.getInt("color");
        }
    }

    private void particleBurst() {
        float velocityMod = 0.2f;
        for (int i = 0; i < 5; ++i) {
            MAParticleType pfx = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get()));
            if (this.color != -1) {
                pfx.setColor(FastColor.ARGB32.red((int)this.color), FastColor.ARGB32.green((int)this.color), FastColor.ARGB32.blue((int)this.color));
            }
            this.getLevel().addParticle((ParticleOptions)pfx, (double)this.worldPosition.getX() + Math.random(), (double)this.worldPosition.getY() + Math.random(), (double)this.worldPosition.getZ() + Math.random(), (Math.random() - 0.5) * (double)velocityMod, Math.random() * (double)velocityMod, (Math.random() - 0.5) * (double)velocityMod);
        }
    }

    public void setColor(int color) {
        this.color = color;
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }
}

