/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.targeting.TargetingConditions
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package com.mna.api.blocks.tile;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.blocks.tile.IMultiblockDefinition;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class MultiblockTile
extends TileEntityWithInventory {
    protected final ResourceLocation structure_path;
    protected IMultiblockDefinition _cachedMultiblockRecipe;
    protected boolean structureMatched = false;
    protected int reMatchRate = 20;
    protected int playerRadius = 32;

    public MultiblockTile(BlockEntityType<?> type, ResourceLocation structure, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
        this.structure_path = structure;
    }

    public MultiblockTile(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation structure) {
        this(type, structure, pos, state, 0);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, MultiblockTile tile) {
        tile.match(false);
    }

    protected void match(boolean force) {
        Player nearby;
        if ((this.level.getGameTime() % (long)this.reMatchRate == 0L || force) && (nearby = this.level.getNearestPlayer((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), (double)this.playerRadius, null)) != null) {
            boolean wasStructureMatched = this.structureMatched;
            this.structureMatched = false;
            this.getDefinition().ifPresent(r -> {
                this.structureMatched = r.match(this.level, this.getMatchOrigin(), Rotation.NONE, this.matchOriginIsCenter());
                if (this.structureMatched && this.reMatchRate == this.getFastRematchRate()) {
                    this.reMatchRate = this.getSlowRematchRate();
                } else if (!this.structureMatched) {
                    this.reMatchRate = this.getFastRematchRate();
                }
                if (!wasStructureMatched && this.structureMatched && !nearby.level().isClientSide()) {
                    this.getAdvancementPlayers(nearby).forEach(sp -> ManaAndArtificeMod.getAdvancementHelper().triggerCompleteMultiblock((ServerPlayer)sp, r.getId()));
                }
            });
        }
    }

    private List<ServerPlayer> getAdvancementPlayers(Player nearby) {
        return this.level.getNearbyPlayers(new TargetingConditions(false).ignoreInvisibilityTesting().ignoreLineOfSight().range((double)this.playerRadius).selector(this::advancementSelector), null, new AABB(this.getMatchOrigin()).inflate((double)this.playerRadius)).stream().filter(p -> p instanceof ServerPlayer).map(p -> (ServerPlayer)p).collect(Collectors.toList());
    }

    protected boolean advancementSelector(LivingEntity candidate) {
        return candidate instanceof ServerPlayer;
    }

    protected boolean matchOriginIsCenter() {
        return true;
    }

    protected BlockPos getMatchOrigin() {
        return this.getBlockPos();
    }

    protected int getSlowRematchRate() {
        this.reMatchRate = 1000 + (int)(Math.random() * 100.0);
        return this.reMatchRate;
    }

    protected int getFastRematchRate() {
        this.reMatchRate = 20;
        return 20;
    }

    protected Optional<IMultiblockDefinition> getDefinition() {
        if (this._cachedMultiblockRecipe == null) {
            this.level.getRecipeManager().byKey(this.structure_path).ifPresent(r -> {
                if (r.value() instanceof IMultiblockDefinition) {
                    this._cachedMultiblockRecipe = (IMultiblockDefinition)r.value();
                }
            });
        }
        return Optional.ofNullable(this._cachedMultiblockRecipe);
    }
}

