 
package com.mna.blocks.tileentities.pylon;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.worlddata.InterdictionData;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.mna.api.tools.MNACap;

public class InterdictionPylonTile
extends PylonTileBase {
    private static final ResourceLocation icon_in = RLoc.create("textures/gui/pylon/interdiction_in.png");
    private static final ResourceLocation icon_out = RLoc.create("textures/gui/pylon/interdiction_out.png");
    private boolean in = true;

    public InterdictionPylonTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.color = new int[]{56, 6, 92};
    }

    public InterdictionPylonTile(BlockPos pPos, BlockState pBlockState) {
        this((BlockEntityType)TileEntityInit.INTERDICTION_PYLON.get(), pPos, pBlockState);
    }

    public InterdictionPylonTile setDirection(boolean in) {
        this.in = in;
        return this;
    }

    @Override
    public ResourceLocation getIcon() {
        return this.in ? icon_in : icon_out;
    }

    @Override
    public String getFactionCheckboxTooltip() {
        return "gui.mna.pylon.faction_included";
    }

     
    public static void Tick(Level level, BlockPos pos, BlockState state, InterdictionPylonTile tile) {
        if (level.isClientSide()) {
            tile.tick();
            return;
        }
        int prevTicks = tile.powerUpTicks;
        if (tile.tick()) {
            int n = tile.powerUpTicks;
            Objects.requireNonNull(tile);
            if (n == 60) {
                if (prevTicks >= tile.powerUpTicks) return;
                tile.UpdateInterdiction(pos, tile.getBounds());
                return;
            }
        }
        Objects.requireNonNull(tile);
        if (prevTicks != 60) return;
        int n = tile.powerUpTicks;
        Objects.requireNonNull(tile);
        if (n >= 60) return;
        tile.RemoveInterdiction(pos);
    }

    @Override
    protected void onBoundsChanged() {
        this.UpdateInterdiction(this.getBlockPos(), this.getBounds());
    }

    @Override
    protected void onFactionOnlyChanged() {
        this.UpdateInterdiction(this.getBlockPos(), this.getBounds());
    }

    @Override
    public void setMapMakerMode() {
        super.setMapMakerMode();
        this.UpdateInterdiction(this.getBlockPos(), this.getBounds());
    }

    private void UpdateInterdiction(BlockPos tePos, AABB area) {
        if (!this.level.isClientSide()) {
            MNACap.get(this.level, WorldMagicProvider.MAGIC).ifPresent(magic -> magic.addInterdictionZone(tePos, new InterdictionData(this.getOwnershipData(), area, this.in, this.isFactionOnly())));
        }
    }

    public void RemoveInterdiction(BlockPos tePos) {
        if (!this.level.isClientSide()) {
            MNACap.get(this.level, WorldMagicProvider.MAGIC).ifPresent(magic -> magic.removeInterdictionZone(tePos));
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        base.putBoolean("in", this.in);
        return base;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.in = tag.getBoolean("in");
    }

    @Override
    public void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.putBoolean("in", this.in);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        this.in = pTag.getBoolean("in");
    }
}

