 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.blocks.manaweaving.ManaweaveProjectorBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.pylon.ManaPylonTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.events.EventDispatcher;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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
import com.mna.api.tools.MNAItemData;

public class ManaweaveProjectorTile
extends BlockEntity {
    private static final String NBT_PATTERN_ID = "manaweave_pattern_id";
    public static final float MAX_MANA = 100.0f;
    private static final float maxStealPerSecond = 5.0f;
    private static final int RADIUS = 2;
    private static final int PYLON_RADIUS = 8;
    private int tickCount = 0;
    private float mana = 0.0f;
    private ResourceLocation __patternRLoc = null;
    private ManaweavingPattern __pattern;
    private boolean spawning = false;
    private LivingEntity activator;
    private ArrayList<BlockPos> knownPylonPositions = new ArrayList();
    private ArrayList<BlockPos> toRemove = new ArrayList();

    public ManaweaveProjectorTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.MANAWEAVE_PROJECTOR.get(), pos, state);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ManaweaveProjectorTile tile) {
        ++tile.tickCount;
        if (level.isClientSide()) {
            tile.spawnParticles();
        } else {
            tile.runLogic_server();
        }
    }

    private void spawnParticles() {
        BlockPos blockPos = this.getBlockPos();
        if (((Boolean)this.getBlockState().getValue((Property)ManaweaveProjectorBlock.PROJECTING)).booleanValue()) {
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())), (double)((float)blockPos.getX() + 0.5f), (double)((float)blockPos.getY() + 0.5f), (double)((float)blockPos.getZ() + 0.5f), (double)-0.1f + Math.random() * (double)0.2f, (double)0.15f, (double)-0.1f + Math.random() * (double)0.2f);
        } else if ((Integer)this.getBlockState().getValue((Property)ManaweaveProjectorBlock.FILL_LEVEL) != 4) {
            if (this.tickCount % 40 == 0) {
                this.scanForNearbyPylons(8);
            }
            if (this.tickCount % 2 == 0) {
                List<Player> players = this.level.getEntitiesOfClass(Player.class, new AABB(this.getBlockPos()).inflate(2.0));
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
        if (!this.spawning) {
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
        } else {
            this.mana -= 1.0f;
            if (this.mana <= 0.0f) {
                this.mana = 0.0f;
                if (!EventDispatcher.DispatchManaweavePatternDrawn(this.getPattern(), this.activator)) {
                    return;
                }
                BlockPos spawnPos = this.getBlockPos();
                Manaweave weave = new Manaweave(this.getLevel());
                weave.setPos((double)spawnPos.getX() + 0.5, spawnPos.getY() + 1, (double)spawnPos.getZ() + 0.5);
                weave.setPattern(this.getPattern().getId());
                weave.setCaster(this.activator, InteractionHand.MAIN_HAND);
                this.getLevel().addFreshEntity((Entity)weave);
                this.activator = null;
                this.spawning = false;
                this.scanForNearbyPylons(8);
            }
            this.updateBlockState();
        }
    }

    private void updateBlockState() {
        if (this.getLevel().isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        boolean hasChange = false;
        if ((Boolean)state.getValue((Property)ManaweaveProjectorBlock.PROJECTING) != this.spawning) {
            state = (BlockState)state.setValue(ManaweaveProjectorBlock.PROJECTING, Boolean.valueOf(this.spawning));
            hasChange = true;
        }
        int fillAmt = (int)Math.floor(this.mana / 100.0f * 4.0f);
        if ((Integer)state.getValue((Property)ManaweaveProjectorBlock.FILL_LEVEL) != fillAmt) {
            state = (BlockState)state.setValue(ManaweaveProjectorBlock.FILL_LEVEL, Integer.valueOf(fillAmt));
            hasChange = true;
        }
        if (hasChange) {
            this.getLevel().setBlock(this.getBlockPos(), state, 2);
            this.getLevel().updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    public void trySpawnManaweaveEntity(Player player) {
        if (this.mana < 100.0f || this.getLevel().isClientSide() || this.getPattern() == null || this.spawning) {
            return;
        }
        this.activator = player;
        this.spawning = true;
        this.updateBlockState();
    }

    private void getManaFromAllNearby() {
        List<Player> players = this.level.getEntitiesOfClass(Player.class, new AABB(this.getBlockPos()).inflate(2.0));
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
        java.util.Optional<com.mna.api.capabilities.IPlayerMagic> magicContainer = MNACap.get(player, PlayerMagicProvider.MAGIC);
        if (magicContainer.isPresent()) {
            IPlayerMagic magic = (IPlayerMagic)magicContainer.get();
            if (magic.getCastingResource().getAmount() < stealAmount) {
                stealAmount = magic.getCastingResource().getAmount();
            }
            magic.getCastingResource().consume((LivingEntity)player, stealAmount);
            this.mana += stealAmount;
        }
    }

    private void lookupManaweavePattern(ResourceLocation rLoc) {
        this.__pattern = ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), rLoc);
    }

    public float getMana() {
        return this.mana;
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        compound.putFloat("mana", this.mana);
        compound.putString(NBT_PATTERN_ID, this.__pattern != null ? this.__pattern.getId().toString() : (this.__patternRLoc != null ? this.__patternRLoc.toString() : ""));
    }

    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        if (compound.contains("mana")) {
            this.mana = compound.getFloat("mana");
        }
        if (compound.contains(NBT_PATTERN_ID)) {
            this.__patternRLoc = ResourceLocation.parse(compound.getString(NBT_PATTERN_ID));
        }
        super.loadAdditional(compound, registries);
    }

    @Nullable
    public ManaweavingPattern getPattern() {
        if (this.__pattern == null) {
            this.lookupManaweavePattern(this.__patternRLoc != null ? this.__patternRLoc : ResourceLocation.fromNamespaceAndPath("mna", "manaweave_patterns/triangle"));
        }
        return this.__pattern;
    }

    public void setPattern(ResourceLocation rLoc, @Nullable Player player) {
        if (player != null) {
            ManaweavingPattern newPattern = ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), rLoc);
            IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (progression != null && newPattern != null && newPattern.getTier() > progression.getTier()) {
                player.sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.low_tier"));
                return;
            }
        }
        this.__patternRLoc = rLoc;
        this.__pattern = null;
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        base.putString(NBT_PATTERN_ID, this.__pattern != null ? this.__pattern.getId().toString() : (this.__patternRLoc != null ? this.__patternRLoc.toString() : ""));
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this.setPattern(ResourceLocation.parse(tag.getString(NBT_PATTERN_ID)), null);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag data = pkt.getTag();
        this.setPattern(ResourceLocation.parse(data.getString(NBT_PATTERN_ID)), null);
    }
}


