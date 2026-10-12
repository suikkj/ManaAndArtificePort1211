 
package com.mna.blocks.tileentities;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.artifice.EldrinMatrixBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.particles.types.movers.ParticleSphereOrbitMover;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class EldrinMatrixTile
extends BlockEntity {
    protected PlayerOwnershipRecord placedBy = PlayerOwnershipRecord.of((CompoundTag)null);
    private static final float GENERATION_RATE_PER_TICK = 0.005f;
    private static final float DIMINISHING_RETURNS_RATE = 0.5f;

    public EldrinMatrixTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public EldrinMatrixTile(BlockPos pPos, BlockState pBlockState) {
        this((BlockEntityType)TileEntityInit.ELDRIN_MATRIX.get(), pPos, pBlockState);
    }

    public Affinity getGeneratingAffinity() {
        Affinity affinity;
        Block block = this.getBlockState().getBlock();
        if (block instanceof EldrinMatrixBlock) {
            EldrinMatrixBlock emb = (EldrinMatrixBlock)block;
            affinity = emb.getGeneratingAffinity();
        } else {
            affinity = Affinity.UNKNOWN;
        }
        return affinity;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, EldrinMatrixTile tile) {
        Affinity aff = tile.getGeneratingAffinity();
        if (aff == Affinity.UNKNOWN) {
            return;
        }
        if (level.isClientSide()) {
            long ticks = level.getGameTime();
            MAParticleType particle = null;
            switch (aff.getShiftAffinity()) {
                case ENDER: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get()));
                    break;
                }
                case EARTH: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get()));
                    break;
                }
                case WATER: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setColor(Affinity.WATER);
                    break;
                }
                case FIRE: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get()));
                    break;
                }
                case WIND: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setColor(30, 30, 30);
                    break;
                }
                default: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get()));
                }
            }
            Vec3 pPos = Vec3.atCenterOf((Vec3i)pos);
            level.addParticle((ParticleOptions)particle.setMaxAge(200).setMover(new ParticleSphereOrbitMover(pPos.x, pPos.y, pPos.z, 0.05f, 57.29577951308232 * (double)(ticks % 360L), 0.1f)), pPos.x, pPos.y, pPos.z, 0.0, 0.0, 0.0);
            level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENCHANT.get())).setMaxAge(200).setScale(0.01f).setMover(new ParticleSphereOrbitMover(pPos.x, pPos.y, pPos.z, 0.1f, 57.29577951308232 * (double)(ticks % 360L), 0.15f)), pPos.x, pPos.y, pPos.z, 0.0, 0.0, 0.0);
        } else {
            UUID placedBy = tile.getPlacedBy();
            if (placedBy == null) {
                return;
            }
            MNACap.get(level, WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().insertPowerDiminishing(placedBy, level, aff, 0.005f, 0.5f));
        }
    }

    public void setPlacedBy(Player player) {
        this.placedBy = PlayerOwnershipRecord.of(player);
    }

    public UUID getPlacedBy() {
        return this.placedBy.getPlayerProfileID();
    }

    protected void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.put("owner", (Tag)this.placedBy.save(this.getLevel()));
    }

    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        if (pTag.contains("owner")) {
            this.placedBy = PlayerOwnershipRecord.of(pTag.getCompound("owner"));
        }
    }
}

