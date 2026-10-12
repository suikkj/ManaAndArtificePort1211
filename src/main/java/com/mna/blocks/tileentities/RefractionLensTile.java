 
package com.mna.blocks.tileentities;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.MultiblockTile;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.tools.RLoc;
import com.mna.blocks.artifice.RefractionLensBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.messages.to_client.ShowDidYouKnow;
import com.mna.tools.render.MultiblockRenderer;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import com.mna.api.tools.MNACap;

public class RefractionLensTile
extends MultiblockTile {
    private Affinity affinity;
    private UUID placedBy;
    private ResourceLocation placedByFaction;
    private boolean initialRequest = false;

    public RefractionLensTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Affinity aff) {
        super(type, pos, state, RLoc.create("multiblock/wellspring_capture"));
        this.affinity = aff;
        this.placedBy = null;
        this.placedByFaction = null;
    }

    public RefractionLensTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, Affinity.UNKNOWN);
    }

    public RefractionLensTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.REFRACTION_LENS.get(), pos, state);
    }

    public RefractionLensTile(Affinity affinity, BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.REFRACTION_LENS.get(), pos, state, affinity);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, RefractionLensTile tile) {
        boolean wasValid = tile.structureMatched;
        MultiblockTile.Tick(level, pos, state, tile);
        if (level.isClientSide()) {
            if (!tile.initialRequest) {
                tile.initialRequest = true;
                ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(true);
            }
        } else if (wasValid && !tile.structureMatched) {
            tile.removePowerFromNetwork();
        } else if (!wasValid && tile.structureMatched) {
            tile.addPowerToNetwork();
        }
    }

    public void onBlockBroken() {
        if (!this.getLevel().isClientSide()) {
            this.removePowerFromNetwork();
        }
    }

    @Override
    protected BlockPos getMatchOrigin() {
        return this.getBlockPos().below(4);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        nbt.putString("affinity", this.affinity.name());
        if (this.placedBy != null) {
            nbt.putString("placedBy", this.placedBy.toString());
        }
        if (this.placedByFaction != null) {
            nbt.putString("placedByFaction", this.placedByFaction.toString());
        }
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        if (nbt.contains("affinity")) {
            this.affinity = Affinity.valueOf(nbt.getString("affinity"));
        }
        if (nbt.contains("placedByFaction")) {
            this.placedByFaction = ResourceLocation.parse(nbt.getString("placedByFaction"));
        }
        if (nbt.contains("placedBy")) {
            this.placedBy = UUID.fromString(nbt.getString("placedBy"));
        }
        super.loadAdditional(nbt, registries);
    }

    private void addPowerToNetwork() {
        if (this.placedBy == null || !(this.level instanceof ServerLevel)) {
            return;
        }
        Player crafter = this.level.getPlayerByUUID(this.placedBy);
        if (this.placedByFaction == null) {
            if (crafter != null) {
                crafter.sendSystemMessage((Component)Component.translatable((String)"gui.mna.wellspring-no-faction"));
            }
            return;
        }
        if (crafter != null) {
            NeoForge.EVENT_BUS.post((Event)new GenericProgressionEvent(crafter, ProgressionEventIDs.CAPTURE_WELLSPRING));
            if (crafter instanceof ServerPlayer) {
                CustomAdvancementTriggers.CAPTURE_WELLSPRING.trigger((ServerPlayer)crafter, this.affinity);
            }
        }
        MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
            Optional<WellspringNode> entry = m.getWellspringRegistry().getNodeAt(this.getBlockPos());
            entry.ifPresent(n -> {
                if (n.getAffinity().getShiftAffinity() == this.affinity || n.getAffinity() == Affinity.UNKNOWN) {
                    if (!m.getWellspringRegistry().claimNode((ServerLevel)this.getLevel(), this.placedBy, this.placedByFaction, this.getBlockPos(), this.affinity)) {
                        this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RefractionLensBlock.INVALID, Boolean.valueOf(true)));
                    } else {
                        this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RefractionLensBlock.INVALID, Boolean.valueOf(false)));
                    }
                } else {
                    this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RefractionLensBlock.INVALID, Boolean.valueOf(true)));
                    ManaAndArtifice.instance.proxy.showDidYouKnow(crafter, ShowDidYouKnow.Messages.LENS_CAPTURE);
                }
            });
            if (!entry.isPresent()) {
                this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RefractionLensBlock.INVALID, Boolean.valueOf(true)));
            }
        });
    }

    private void removePowerFromNetwork() {
        MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().unclaimNode((ServerLevel)this.level, this.getBlockPos()));
    }

    public void setPlacedBy(Player player) {
        if (this.getLevel().isClientSide()) {
            return;
        }
        this.placedBy = player.getUUID();
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getAlliedFaction() != null) {
                this.placedByFaction = Registries.Factions.getKey(p.getAlliedFaction());
            }
        });
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setGhostMultiblock() {
        this.getDefinition().ifPresent(r -> {
            BlockPos anchorPos = this.getMatchOrigin();
            MultiblockRenderer.setMultiblock(r, (Component)Component.translatable((String)r.getId().toString()), false);
            MultiblockRenderer.anchorTo(anchorPos, Rotation.NONE);
        });
    }
}


