 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.tools.MATags;
import com.mna.api.tools.RLoc;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.math.MathUtils;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConstructWaterCrops
extends ConstructAITask<ConstructWaterCrops> {
    private static final int MAX_SIZE = 9;
    private static final int WAIT_TIME_PER_BLOCK = 5;
    private AABB waterArea = null;
    private boolean tooBig = false;
    private int waitTicks = 0;

    public ConstructWaterCrops(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.tooBig) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_too_big", 9, this.waterArea.getXsize(), this.waterArea.getYsize(), this.waterArea.getZsize()), false);
            return false;
        }
        int blocks = this.AABBBlocks();
        int mbUsed = blocks * 100;
        if (this.construct.getStoredFluidAmount() < mbUsed) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.not_enough_water", new Object[0]), false);
            return false;
        }
        return super.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.waitTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.waitTicks = 0;
        this.construct.resetActions();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.waterArea == null) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.water_crops_fail", new Object[0]), false);
            this.construct.resetActions();
            this.exitCode = 1;
            return;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, this.waterArea.getCenter());
        this.setMoveTarget(BlockPos.containing((Position)this.waterArea.getCenter()));
        if (this.doMove()) {
            this.construct.setWatering();
            if (this.waitTicks++ > this.AABBBlocks() * 5) {
                this.completeTask();
                this.exitCode = 0;
            }
            return;
        }
    }

    private void completeTask() {
        AbstractGolem c = this.getConstructAsEntity();
        if (!c.level().isLoaded(BlockPos.containing((Position)this.waterArea.getCenter()))) {
            return;
        }
        int blocks = this.AABBBlocks();
        int mbUsed = blocks * 100;
        this.construct.drain(mbUsed, IFluidHandler.FluidAction.EXECUTE);
        for (int i = (int)this.waterArea.minX; i <= (int)this.waterArea.maxX; ++i) {
            for (int j = (int)this.waterArea.minY; j <= (int)this.waterArea.maxY; ++j) {
                for (int k = (int)this.waterArea.minZ; k <= (int)this.waterArea.maxZ; ++k) {
                    boolean valid;
                    BlockPos pos = new BlockPos(i, j, k);
                    BlockState state = c.level().getBlockState(pos);
                    boolean no_age = MATags.isBlockIn(state.getBlock(), RLoc.create("construct_harvestables_no_age"));
                    boolean bl = valid = no_age || MATags.isBlockIn(state.getBlock(), RLoc.create("construct_harvestables"));
                    if (!valid) continue;
                    if (state.getBlock().isRandomlyTicking(state)) {
                        state.getBlock().randomTick(state, (ServerLevel)c.level(), pos, c.level().getRandom());
                    }
                    c.level().scheduleTick(pos, state.getBlock(), 1);
                }
            }
        }
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.water_crops_success", new Object[0]), false);
        this.construct.resetActions();
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.WATER);
    }

    @Override
    public ConstructWaterCrops duplicate() {
        return new ConstructWaterCrops(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructWaterCrops copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructWaterCrops) {
            this.waterArea = ((ConstructWaterCrops)other).waterArea;
        }
        return this;
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        if (this.waterArea != null) {
            nbt.put("blockPos1", (Tag)NbtUtils.writeBlockPos((BlockPos)BlockPos.containing((double)this.waterArea.minX, (double)this.waterArea.minY, (double)this.waterArea.minZ)));
            nbt.put("blockPos2", (Tag)NbtUtils.writeBlockPos((BlockPos)BlockPos.containing((double)this.waterArea.maxX, (double)this.waterArea.maxY, (double)this.waterArea.maxZ)));
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("blockPos1") && nbt.contains("blockPos2")) {
            BlockPos blockPos1 = NbtUtils.readBlockPos(nbt, "blockPos1").orElse(null);
            BlockPos blockPos2 = NbtUtils.readBlockPos(nbt, "blockPos2").orElse(null);
            this.createBoundsFromCornerPositions(blockPos1, blockPos2);
        }
    }

    @Override
    public void inflateParameters() {
        this.getParameter("water_crops.area").ifPresent(param -> {
            if (param instanceof ConstructTaskAreaParameter) {
                this.waterArea = ((ConstructTaskAreaParameter)param).getArea();
            }
            if (this.waterArea == null || this.waterArea.getXsize() > 9.0 || this.waterArea.getYsize() > 9.0 || this.waterArea.getZsize() > 9.0) {
                this.tooBig = true;
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskAreaParameter("water_crops.area"));
        return parameters;
    }

    private void createBoundsFromCornerPositions(BlockPos a, BlockPos b) {
        if (a != null && b != null) {
            this.waterArea = MathUtils.createInclusiveBB(a, b);
            if (this.waterArea.getXsize() > 9.0 || this.waterArea.getYsize() > 9.0 || this.waterArea.getZsize() > 9.0) {
                this.tooBig = true;
            }
        }
    }

    private int AABBBlocks() {
        return (int)(this.waterArea.getXsize() * this.waterArea.getZsize()) - 1;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.waterArea != null;
    }
}

