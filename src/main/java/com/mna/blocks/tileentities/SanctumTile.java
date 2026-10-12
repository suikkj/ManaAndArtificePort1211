 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.blocks.tile.IMultiblockDefinition;
import com.mna.api.blocks.tile.MultiblockTile;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.recipes.multiblock.MultiblockDefinition;
import com.mna.tools.render.MultiblockRenderer;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mna.api.tools.MNACap;

public class SanctumTile
extends MultiblockTile {
    private IFaction alignedFaction;

    public SanctumTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.CIRCLE_OF_POWER.get(), pos, state, null);
    }

    public void setFaction(IFaction faction) {
        this.alignedFaction = faction;
    }

    public boolean hasFaction() {
        return this.alignedFaction != null;
    }

    @Override
    protected Optional<IMultiblockDefinition> getDefinition() {
        ResourceLocation recipeId = this.alignedFaction.getSanctumStructure();
        if (!this.hasFaction() || recipeId == null) {
            return Optional.empty();
        }
        if (this._cachedMultiblockRecipe == null) {
            this.getLevel().getRecipeManager().byKey(recipeId).ifPresent(r -> {
                if (r instanceof MultiblockDefinition) {
                    this._cachedMultiblockRecipe = (MultiblockDefinition)r;
                }
            });
        }
        return Optional.ofNullable(this._cachedMultiblockRecipe);
    }

    public static void ServerTick(Level level, BlockPos pos, BlockState state, SanctumTile tile) {
        MultiblockTile.Tick(level, pos, state, tile);
        if (!tile.getLevel().isClientSide() && tile.structureMatched && tile.getLevel().getGameTime() % 300L == 0L) {
            tile.getDefinition().ifPresent(r -> {
                if (tile.alignedFaction == null) {
                    return;
                }
                float sanctumScale = 32.0f;
                int hX = (int)((Math.ceil((float)r.getSize().getX() / 2.0f) + 1.0) * (double)sanctumScale);
                int hZ = (int)((Math.ceil((float)r.getSize().getZ() / 2.0f) + 1.0) * (double)sanctumScale);
                AABB bb = new AABB(pos).expandTowards((double)hZ, (double)((float)(r.getSize().getY() + 1) * sanctumScale), (double)hZ).expandTowards((double)(-hX), 0.0, (double)(-hZ));
                List<LivingEntity> entities = tile.getLevel().getEntitiesOfClass(LivingEntity.class, bb);
                if (entities.size() > 0) {
                    entities.forEach(entity -> {
                        if (entity instanceof Player) {
                            MNACap.get(entity, PlayerProgressionProvider.PROGRESSION).ifPresent(pr -> {
                                if (pr.getAlliedFaction() != null && (pr.getAlliedFaction() == tile.alignedFaction || pr.getAlliedFaction().isAlliedTo(tile.alignedFaction))) {
                                    entity.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.CIRCLE_OF_POWER.get()), 600));
                                } else {
                                    tile.setSacrifice((LivingEntity)entity);
                                }
                            });
                        } else {
                            tile.setSacrifice((LivingEntity)entity);
                        }
                    });
                }
            });
        }
    }

    @Override
    protected boolean advancementSelector(LivingEntity candidate) {
        if (this.alignedFaction == null) {
            return false;
        }
        if (!(candidate instanceof ServerPlayer)) {
            return false;
        }
        IPlayerProgression progression = (IPlayerProgression)candidate.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null) {
            return false;
        }
        return progression.getAlliedFaction() == this.alignedFaction;
    }

    private void setSacrifice(LivingEntity entity) {
        if (this.alignedFaction == null) {
            return;
        }
        float sacrificeDistance = 9.0f;
        if ((this.alignedFaction == Factions.UNDEAD || this.alignedFaction.isAlliedTo(Factions.UNDEAD)) && entity.blockPosition().distSqr((Vec3i)this.getBlockPos()) <= (double)sacrificeDistance) {
            entity.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SOUL_VULNERABILITY.get()), 100, 0, true, true));
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setGhostMultiblock() {
        this.getDefinition().ifPresent(r -> {
            BlockPos anchorPos = this.worldPosition.above();
            MultiblockRenderer.setMultiblock(r, (Component)Component.translatable((String)r.getId().toString()), false);
            MultiblockRenderer.anchorTo(anchorPos, Rotation.NONE);
        });
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag data = super.getUpdateTag(registries);
        this.writeFactionToNBT(data);
        return data;
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag nbt = pkt.getTag();
        this.readFactionFromNBT(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag data, net.minecraft.core.HolderLookup.Provider registries) {
        this.writeFactionToNBT(data);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.readFactionFromNBT(nbt);
    }

    private void writeFactionToNBT(CompoundTag nbt) {
        if (this.alignedFaction != null) {
            nbt.putString("faction", Registries.Factions.getKey(this.alignedFaction).toString());
        }
    }

    private void readFactionFromNBT(CompoundTag nbt) {
        if (nbt.contains("faction")) {
            try {
                this.alignedFaction = (IFaction)Registries.Factions.getValue(ResourceLocation.parse(nbt.getString("faction")));
            }
            catch (Exception ex) {
                ManaAndArtifice.LOGGER.error("Error parsing aligned faction on Circle of Power, setting to NONE.");
                ManaAndArtifice.LOGGER.error(ex);
                this.alignedFaction = null;
            }
        }
    }

    @Override
    protected BlockPos getMatchOrigin() {
        return this.getBlockPos().above();
    }
}


