 
package com.mna.blocks.tileentities;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.blocks.manaweaving.ManaweaveProjectorBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.pylon.ManaPylonTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class ManaResevoirTile
extends BlockEntity {
    public static final float MAX_MANA = 100.0f;
    private static final float maxStealPerSecond = 5.0f;
    private static final int RADIUS = 2;
    private static final int PYLON_RADIUS = 8;
    private int tickCount = 0;
    private float mana = 0.0f;
    private ArrayList<BlockPos> knownPylonPositions = new ArrayList();
    private ArrayList<BlockPos> toRemove = new ArrayList();

    public ManaResevoirTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.MANA_RESEVOIR.get(), pos, state);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ManaResevoirTile tile) {
        ++tile.tickCount;
        if (level.isClientSide()) {
            tile.spawnParticles();
        } else {
            tile.runLogic_server();
        }
    }

    private void spawnParticles() {
        BlockPos blockPos = this.getBlockPos();
        if ((Integer)this.getBlockState().getValue((Property)ManaweaveProjectorBlock.FILL_LEVEL) != 4) {
            if (this.tickCount % 40 == 0) {
                this.scanForNearbyPylons(8);
            }
            if (this.tickCount % 2 == 0) {
                List players = this.level.getEntitiesOfClass(Player.class, new AABB(this.getBlockPos()).inflate(2.0));
                for (Player player : players) {
                    Vec3 pos = player.position();
                    this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_LERP_POINT.get())), pos.x(), pos.y() + (double)1.2f, pos.z(), (double)((float)blockPos.getX() + 0.5f), (double)((float)blockPos.getY() + 0.5f), (double)((float)blockPos.getZ() + 0.5f));
                }
                this.toRemove.clear();
                for (BlockPos pos : this.knownPylonPositions) {
                    if (!this.getLevel().isLoaded(pos)) continue;
                    if (this.getLevel().getBlockState(pos).getBlock() == BlockInit.PYLON_MANA.get()) {
                        this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_LERP_POINT.get())), (double)((float)pos.getX() + 0.5f), (double)((float)pos.getY() + 1.2f), (double)((float)pos.getZ() + 0.5f), (double)((float)blockPos.getX() + 0.5f), (double)((float)blockPos.getY() + 0.5f), (double)((float)blockPos.getZ() + 0.5f));
                        continue;
                    }
                    this.toRemove.add(pos);
                }
                this.knownPylonPositions.removeAll(this.toRemove);
            }
        }
    }

    private void runLogic_server() {
        if (this.mana < 100.0f && !this.getLevel().hasNeighborSignal(this.getBlockPos())) {
            if (this.tickCount % 40 == 0) {
                this.scanForNearbyPylons(8);
            }
            if (this.tickCount % 2 == 0) {
                this.getManaFromAllNearby();
                this.getManaFromPylons();
                this.updateBlockState();
            }
        }
    }

    private void updateBlockState() {
        if (this.getLevel().isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        boolean hasChange = false;
        int fillAmt = (int)Math.floor(this.mana / 100.0f * 4.0f);
        if ((Integer)state.getValue((Property)ManaweaveProjectorBlock.FILL_LEVEL) != fillAmt) {
            state = (BlockState)state.setValue((Property)ManaweaveProjectorBlock.FILL_LEVEL, (Comparable)Integer.valueOf(fillAmt));
            hasChange = true;
        }
        if (hasChange) {
            this.getLevel().setBlock(this.getBlockPos(), state, 2);
            this.getLevel().updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    private void getManaFromAllNearby() {
        List players = this.level.getEntitiesOfClass(Player.class, new AABB(this.getBlockPos()).inflate(2.0));
        for (Player player : players) {
            this.getManaFromPlayer(player);
        }
    }

    private void getManaFromPylons() {
        this.toRemove.clear();
        for (BlockPos pos : this.knownPylonPositions) {
            BlockEntity blockEntity;
            if (!this.getLevel().isLoaded(pos)) continue;
            if (this.getLevel().getBlockState(pos).getBlock() == BlockInit.PYLON_MANA.get() && (blockEntity = this.getLevel().getBlockEntity(pos)) instanceof ManaPylonTile) {
                ManaPylonTile tile = (ManaPylonTile)blockEntity;
                if (!tile.isActiveAndPoweredUp()) continue;
                float consumed = tile.consumePower(0.25f) * 40.0f;
                this.mana = Math.min(this.mana + consumed, 100.0f);
                continue;
            }
            this.toRemove.add(pos);
        }
        this.knownPylonPositions.removeAll(this.toRemove);
    }

    private void scanForNearbyPylons(int radius) {
        this.knownPylonPositions.clear();
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    BlockPos pos = this.getBlockPos().offset(i, j, k);
                    if (!this.getLevel().isLoaded(pos) || this.level.getBlockState(pos).getBlock() != BlockInit.PYLON_MANA.get()) continue;
                    this.knownPylonPositions.add(pos);
                }
            }
        }
    }

    private void getManaFromPlayer(Player player) {
        float stealAmount = Math.min(100.0f - this.mana, 5.0f);
        if (stealAmount == 0.0f) {
            return;
        }
        java.util.Optional<PlayerMagicProvider> magicContainer = MNACap.get(player, PlayerMagicProvider.MAGIC);
        if (magicContainer.isPresent()) {
            IPlayerMagic magic = (IPlayerMagic)magicContainer.get();
            if (magic.getCastingResource().getAmount() < stealAmount) {
                stealAmount = magic.getCastingResource().getAmount();
            }
            magic.getCastingResource().consume((LivingEntity)player, stealAmount);
            this.mana += stealAmount;
        }
    }

    public float getMana() {
        return this.mana;
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        compound.putFloat("mana", this.mana);
    }

    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        if (compound.contains("mana")) {
            this.mana = compound.getFloat("mana");
        }
        super.loadAdditional(compound, registries);
    }
}


