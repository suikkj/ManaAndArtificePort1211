/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.tileentities;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.EldrinCapacitorTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.particles.types.movers.ParticleOrbitMover;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EldrinConduitTile
extends EldrinCapacitorTile {
    public static final float CRYSTAL_OFFSET_LESSER = 1.5f;
    public static final float CRYSTAL_OFFSET = 0.75f;
    private boolean isLesser;

    public EldrinConduitTile(BlockEntityType<?> type, Affinity affinity, float capacity, boolean isLesser, BlockPos pos, BlockState state) {
        super(type, pos, state, capacity, affinity);
        this.isLesser = isLesser;
    }

    public EldrinConduitTile(Affinity affinity, float capacity, boolean isLesser, BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.ELDRIN_CONDUIT_TILE.get(), affinity, capacity, isLesser, pos, state);
    }

    public EldrinConduitTile(BlockPos pos, BlockState state) {
        this(Affinity.UNKNOWN, 1.0f, false, pos, state);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, EldrinConduitTile tile) {
        if (level.isClientSide()) {
            switch (tile.getAffinities().get(0)) {
                case FIRE: 
                case HELLFIRE: {
                    tile.spawnFireParticles();
                    break;
                }
                case WATER: 
                case ICE: {
                    tile.spawnWaterParticles();
                    break;
                }
                case WIND: {
                    tile.spawnWindParticles();
                    break;
                }
                case EARTH: {
                    tile.spawnEarthParticles();
                    break;
                }
                case ENDER: {
                    tile.spawnEnderParticles();
                    break;
                }
                default: {
                    tile.spawnArcaneParticles();
                }
            }
            int[] color = tile.getAffinities().get(0).getColor();
            for (int i = 0; i < 5; ++i) {
                tile.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHT_VELOCITY.get())).setColor(color[0], color[1], color[2]), (double)((float)tile.getBlockPos().getX() + 0.4f) + Math.random() * (double)0.2f, (double)((float)tile.getBlockPos().getY() + 1.4f), (double)((float)tile.getBlockPos().getZ() + 0.4f) + Math.random() * (double)0.2f, 0.0, 0.01, 0.0);
            }
        } else {
            EldrinCapacitorTile.Tick(level, pos, state, tile);
        }
    }

    @Override
    public float getChargeRate() {
        return this.isLesser ? 0.05f : 2.5f;
    }

    @Override
    public float getDispersalRadius() {
        return this.isLesser ? 8.0f : 16.0f;
    }

    @Override
    public float getRateLimit() {
        return this.isLesser ? 0.05f : 2.5f;
    }

    public boolean isLesser() {
        return this.isLesser;
    }

    private void spawnFireParticles() {
        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), (double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + this.getCrystalOffset()), (double)((float)this.getBlockPos().getZ() + 0.5f), 0.0, 0.01, 0.0);
    }

    private void spawnWaterParticles() {
        Vec3 center = new Vec3((double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + this.getCrystalOffset()), (double)((float)this.getBlockPos().getZ() + 0.5f));
        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())).setMaxAge(2).setMover(new ParticleOrbitMover(center.x, center.y, center.z, 0.25, 0.0, 0.05)), center.x, center.y, center.z, 0.0, 0.0, 0.0);
    }

    private void spawnWindParticles() {
        Vec3 center = new Vec3((double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + this.getCrystalOffset() - 0.2f), (double)((float)this.getBlockPos().getZ() + 0.5f));
        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setMaxAge(2).setScale(0.04f).setColor(10, 10, 10).setMover(new ParticleOrbitMover(center.x, center.y, center.z, 0.25, 0.01, 0.05)), center.x, center.y, center.z, 0.0, 0.0, 0.0);
    }

    private void spawnEarthParticles() {
        Vec3 center = new Vec3((double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + this.getCrystalOffset() + 0.35f), (double)((float)this.getBlockPos().getZ() + 0.5f));
        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setGravity(0.002f), center.x, center.y, center.z, 0.0, 0.0, 0.0);
    }

    private void spawnArcaneParticles() {
        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())).setScale(0.1f).setMaxAge(13), (double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + this.getCrystalOffset()), (double)((float)this.getBlockPos().getZ() + 0.5f), 0.0, 0.0, 0.0);
    }

    private void spawnEnderParticles() {
        BlockPos bp = this.getBlockPos();
        float angle = this.getLevel().getGameTime() % 36L * 10L;
        Vec3 rotationOffset = new Vec3(0.25, 0.0, 0.0);
        Vec3 point = rotationOffset.yRot((float)((double)angle * Math.PI / 180.0));
        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (double)((float)bp.getX() + 0.5f) + point.x, (double)((float)bp.getY() + this.getCrystalOffset()), (double)((float)bp.getZ() + 0.5f) + point.z, (double)((float)bp.getX() + 0.5f), (double)((float)bp.getY() + this.getCrystalOffset()), (double)((float)bp.getZ() + 0.5f));
    }

    private float getCrystalOffset() {
        return this.isLesser() ? 1.5f : 0.75f;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.putBoolean("lesser", this.isLesser);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        if (pTag.contains("lesser")) {
            this.isLesser = pTag.getBoolean("lesser");
        }
    }
}

