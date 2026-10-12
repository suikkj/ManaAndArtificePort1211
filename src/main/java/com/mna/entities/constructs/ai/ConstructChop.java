 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructBlockAreaTask;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.BlockUtils;
import java.util.ArrayList;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.block.state.BlockState;

public class ConstructChop
extends ConstructBlockAreaTask<ConstructChop> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CHOP_WOOD};
    private int lastPlantLocation = 0;
    private int breakProgress = 0;
    private int lastBreakProgressSync = 0;
    private ArrayList<Long> checkedBlocks;
    private int _breakTime = 80;

    public ConstructChop(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.checkedBlocks = new ArrayList();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.currentTarget == null) {
            this.findBlockTarget();
            return;
        }
        if (this.moveToCurrentTarget()) {
            this.harvest();
        }
    }

    private void harvest() {
        ++this.breakProgress;
        int breakProgressSync = (int)((float)this.breakProgress / (float)this._breakTime * 10.0f);
        AbstractGolem c = this.getConstructAsEntity();
        if (this.breakProgress >= this._breakTime) {
            this.checkedBlocks.clear();
            if (!((Boolean)BlockUtils.breakTreeRecursive(this.createFakePlayer(), c.level(), this.currentTarget, false).getFirst()).booleanValue()) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.chop_break_failed", this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ()), false);
                this.forceFail();
            } else {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.chop_success", new Object[0]), false);
                this.setSuccessCode();
            }
            this.knownTargets.remove(this.currentTarget);
            ++this.lastPlantLocation;
            if (this.lastPlantLocation > this.knownTargets.size()) {
                this.lastPlantLocation = 0;
            }
            this.breakProgress = 0;
            this.lastBreakProgressSync = 0;
        } else {
            if (breakProgressSync != this.lastBreakProgressSync) {
                c.level().destroyBlockProgress(c.getId(), this.currentTarget, breakProgressSync);
                this.lastBreakProgressSync = breakProgressSync;
            }
            if (this.breakProgress % 5 == 0) {
                this.construct.getHandWithCapability(ConstructCapability.CHOP_WOOD).ifPresent(h -> c.swing(h));
            }
        }
    }

    @Override
    protected boolean isValidBlock(BlockState state, BlockPos pos) {
        AbstractGolem c = this.getConstructAsEntity();
        BlockState below = c.level().getBlockState(pos.below());
        if (c.level().isEmptyBlock(pos.below())) {
            return false;
        }
        return state.is(BlockTags.LOGS) && !below.is(BlockTags.LOGS);
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = this.getInteractTime(ConstructCapability.HARVEST);
    }

    @Override
    public void stop() {
        super.stop();
        this.interactTimer = this.getInteractTime(ConstructCapability.HARVEST);
        AbstractGolem c = this.getConstructAsEntity();
        if (this.currentTarget != null) {
            c.level().destroyBlockProgress(c.getId(), this.currentTarget, -1);
        }
        this.lastBreakProgressSync = 0;
        this.breakProgress = 0;
    }

    @Override
    public void setConstruct(IConstruct<?> construct) {
        super.setConstruct(construct);
        this._breakTime = Math.max(this.getInteractTime(ConstructCapability.CHOP_WOOD, this._breakTime), 20);
    }

    @Override
    protected String getAreaIdentifier() {
        return "chop.area";
    }

    @Override
    protected int getInteractTimer() {
        return this.getInteractTime(ConstructCapability.CHOP_WOOD);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.CHOP);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public ConstructChop duplicate() {
        return new ConstructChop(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructChop copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        return this;
    }
}

