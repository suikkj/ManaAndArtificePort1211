/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LightLayer
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.ticks.TickPriority
 */
package com.mna.blocks.tileentities.pylon;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.utility.InvisibleLight;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.ticks.TickPriority;

public class LightPylonTile
extends PylonTileBase {
    private static final ResourceLocation icon = RLoc.create("textures/gui/pylon/light.png");
    private static final int MAX_LIGHTS_UPDATED_PER_TICK = 100;
    private HashSet<Long> knownLightPositions = new HashSet();
    private ArrayList<Long> knownLightPositions_traversable = new ArrayList();
    private int lastKnownUpdateIndex = 0;

    public LightPylonTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public LightPylonTile(BlockPos pPos, BlockState pBlockState) {
        this((BlockEntityType)TileEntityInit.LIGHT_PYLON.get(), pPos, pBlockState);
    }

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, LightPylonTile tile) {
        if (tile.tick()) {
            int n = tile.powerUpTicks;
            Objects.requireNonNull(tile);
            if (n == 60) {
                tile.update_known_lights();
                tile.forAllBlocks(100, (cPos, cState) -> {
                    if (cState.getBlock() == BlockInit.INVISIBLE_LIGHT_BLOCK.get()) {
                        long lPos = cPos.asLong();
                        if (tile.knownLightPositions.add(lPos)) {
                            tile.knownLightPositions_traversable.add(lPos);
                        }
                    } else if (cState.isAir()) {
                        long lPos = cPos.asLong();
                        int light = level.getBrightness(LightLayer.BLOCK, cPos);
                        if (light < GeneralConfigValues.LightPylonBrightnessThreshold) {
                            level.setBlock(cPos, ((InvisibleLight)BlockInit.INVISIBLE_LIGHT_BLOCK.get()).defaultBlockState(), 23);
                            level.scheduleTick(cPos, (Block)BlockInit.INVISIBLE_LIGHT_BLOCK.get(), 2400, TickPriority.EXTREMELY_LOW);
                            if (tile.knownLightPositions.add(lPos)) {
                                tile.knownLightPositions_traversable.add(lPos);
                            }
                        }
                    }
                });
            }
        }
    }

    private void update_known_lights() {
        for (int i = 0; i < 100; ++i) {
            long uPosL;
            BlockPos uPos;
            if (this.lastKnownUpdateIndex >= this.knownLightPositions_traversable.size()) {
                this.lastKnownUpdateIndex = 0;
                break;
            }
            if (!this.level.isLoaded(uPos = BlockPos.of((long)(uPosL = this.knownLightPositions_traversable.get(this.lastKnownUpdateIndex++).longValue())))) continue;
            BlockState uState = this.level.getBlockState(uPos);
            if (uState.getBlock() != BlockInit.INVISIBLE_LIGHT_BLOCK.get()) {
                this.knownLightPositions.remove(uPosL);
                while (this.knownLightPositions_traversable.remove(uPosL)) {
                }
                continue;
            }
            this.level.setBlockAndUpdate(uPos, (BlockState)uState.setValue(BlockStateProperties.DELAY, Integer.valueOf(4)));
            if (this.level.getBlockTicks().hasScheduledTick(uPos, ((Block)BlockInit.INVISIBLE_LIGHT_BLOCK.get()))) continue;
            this.level.scheduleTick(uPos, (Block)BlockInit.INVISIBLE_LIGHT_BLOCK.get(), 1200, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    public void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.putLongArray("positions", this.knownLightPositions.stream().mapToLong(Number::longValue).toArray());
        pTag.putInt("idx", this.lastKnownUpdateIndex);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        if (pTag.contains("positions")) {
            long[] positions;
            this.knownLightPositions.clear();
            for (long p : positions = pTag.getLongArray("positions")) {
                this.knownLightPositions.add(p);
                this.knownLightPositions_traversable.add(p);
            }
        }
        if (pTag.contains("idx")) {
            this.lastKnownUpdateIndex = pTag.getInt("idx");
        }
    }
}

