/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.animal.AbstractGolem
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.entities.construct.ai;

import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class ConstructBlockAreaTask<T extends ConstructBlockAreaTask<?>>
extends ConstructAITask<ConstructBlockAreaTask<T>> {
    protected static final int MAX_SIZE = 32;
    private static final int MAX_BLOCKS_CHECKED_PER_TICK = 50;
    protected AABB area = null;
    protected NonNullList<BlockPos> knownTargets;
    protected BlockPos currentTarget;
    protected int lastLocation = 0;
    protected boolean tooBig = false;
    protected int interactTimer;
    protected boolean searchingSurroundings = false;
    protected int x;
    protected int y;
    protected int z;

    public ConstructBlockAreaTask(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.knownTargets = NonNullList.create();
    }

    protected abstract boolean isValidBlock(BlockState var1, BlockPos var2);

    protected abstract int getInteractTimer();

    protected boolean findBlockTarget() {
        return this.findBlockTarget(false);
    }

    protected boolean findBlockTarget(boolean searchBelow) {
        if (this.currentTarget != null) {
            return true;
        }
        if (this.area == null) {
            return false;
        }
        if (!this.searchingSurroundings && this.knownTargets.size() == 0) {
            this.setupRecheck();
        }
        if (this.searchingSurroundings) {
            this.searchSurroundings(searchBelow);
            return false;
        }
        this.pickLocation();
        if (this.currentTarget == null) {
            this.forceFail();
            return false;
        }
        return true;
    }

    protected boolean moveToCurrentTarget() {
        this.setMoveTarget(this.currentTarget);
        if (this.doMove()) {
            AbstractGolem c;
            if (this.interactTimer > 0) {
                --this.interactTimer;
                if (this.interactTimer != 4) {
                    return false;
                }
            }
            if ((c = this.getConstructAsEntity()).level().isLoaded(this.currentTarget)) {
                BlockState state = c.level().getBlockState(this.currentTarget);
                if (this.isValidBlock(state, this.currentTarget)) {
                    if (this.interactTimer == 4) {
                        this.construct.getHandWithCapability(this.getSwingArmCap()).ifPresent(h -> c.swing(h));
                        return false;
                    }
                    return true;
                }
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_badstate", this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ(), this.translate(state)));
                this.removeBlockTarget(this.currentTarget);
                this.currentTarget = null;
                this.exitCode = 1;
            }
        }
        return false;
    }

    protected ConstructCapability getSwingArmCap() {
        return ConstructCapability.HARVEST;
    }

    protected void removeBlockTarget(BlockPos pos) {
        this.knownTargets.remove(pos);
        this.lastLocation = 0;
    }

    private void setupRecheck() {
        this.searchingSurroundings = true;
        this.knownTargets.clear();
        this.lastLocation = 0;
        this.x = (int)this.area.minX;
        this.y = (int)this.area.minY;
        this.z = (int)this.area.minZ;
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_scan_starting", new Object[0]));
    }

    private void searchSurroundings(boolean searchBelow) {
        AbstractGolem c = this.getConstructAsEntity();
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, this.area);
        int count = 0;
        while ((double)this.x < this.area.maxX) {
            while ((double)this.y < this.area.maxY) {
                while ((double)this.z < this.area.maxZ) {
                    BlockPos curSearch = new BlockPos(this.x, this.y, this.z);
                    if (c.level().isLoaded(curSearch)) {
                        if (this.isValidBlock(c.level().getBlockState(curSearch), curSearch)) {
                            this.knownTargets.add(curSearch);
                        }
                        if (searchBelow) {
                            BlockPos curSearchBelow = curSearch.below();
                            if (this.isValidBlock(c.level().getBlockState(curSearchBelow), curSearchBelow)) {
                                this.knownTargets.add(curSearchBelow);
                            }
                        }
                    }
                    if (++count >= 50) {
                        return;
                    }
                    ++this.z;
                }
                this.z = (int)this.area.minZ;
                ++this.y;
            }
            this.y = (int)this.area.minY;
            ++this.x;
        }
        this.searchingSurroundings = false;
        if (this.knownTargets.size() == 0) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_scan_no_results", new Object[0]));
            this.exitCode = 1;
            return;
        }
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_scan_success", new Object[0]));
    }

    private void pickLocation() {
        int count = 0;
        if (this.knownTargets.size() == 0) {
            this.exitCode = 1;
            return;
        }
        AbstractGolem c = this.getConstructAsEntity();
        while (this.lastLocation < this.knownTargets.size()) {
            BlockPos curSearch = (BlockPos)this.knownTargets.get(this.lastLocation);
            if (c.level().isLoaded(curSearch)) {
                if (this.isValidBlock(c.level().getBlockState(curSearch), curSearch)) {
                    if (this.claimBlockMutex(curSearch)) {
                        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)curSearch));
                        this.currentTarget = curSearch;
                        return;
                    }
                } else {
                    this.knownTargets.remove(this.lastLocation);
                    --this.lastLocation;
                }
            }
            if (++count >= 50) {
                return;
            }
            ++this.lastLocation;
        }
        this.lastLocation = 0;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            this.forceFail();
            return false;
        }
        if (this.area == null) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_not_configured", new Object[0]), false);
            this.forceFail();
            return false;
        }
        if (this.tooBig) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_too_big", 32, this.area.getXsize(), this.area.getYsize(), this.area.getZsize()), false);
            this.forceFail();
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public void inflateParameters() {
        this.getParameter(this.getAreaIdentifier()).ifPresent(param -> {
            if (param instanceof ConstructTaskAreaParameter) {
                this.area = ((ConstructTaskAreaParameter)param).getArea();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskAreaParameter(this.getAreaIdentifier()));
        return parameters;
    }

    protected abstract String getAreaIdentifier();

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.interactTimer = this.getInteractTimer();
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public ConstructBlockAreaTask<T> copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructBlockAreaTask) {
            this.area = ((ConstructBlockAreaTask)other).area;
        }
        return this;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null;
    }

    public void setAreaManually(BlockPos cornerA, BlockPos cornerB) {
        this.getParameter(this.getAreaIdentifier()).ifPresent(param -> {
            if (param instanceof ConstructTaskAreaParameter) {
                ConstructTaskAreaParameter area = (ConstructTaskAreaParameter)param;
                area.setPoints(new DirectionalPoint(cornerA, Direction.UP, "", null), new DirectionalPoint(cornerB, Direction.UP, "", null));
                this.area = area.getArea();
            }
        });
    }
}

