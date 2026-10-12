 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ConstructWorkbenchTile;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;

public class ConstructCommandReturnToTable
extends ConstructAITask<ConstructCommandReturnToTable> {
    public ConstructCommandReturnToTable(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        AbstractGolem c = this.getConstructAsEntity();
        return this.hasMoveTarget() && this.construct != null && c.level().isLoaded(this.getMoveBlockTarget()) && c.level().getBlockState(this.getMoveBlockTarget()).getBlock() == BlockInit.CONSTRUCT_WORKBENCH.get();
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (c.blockPosition().closerThan((Vec3i)this.getMoveBlockTarget(), 4.0)) {
            if (!c.level().isClientSide() && !this.construct.asEntity().isRemoved()) {
                ConstructWorkbenchTile table = (ConstructWorkbenchTile)c.level().getBlockEntity(this.getMoveBlockTarget());
                if (table != null) {
                    if (!table.getIsCrafting() && table.isEmpty()) {
                        this.construct.dropAllItems();
                        table.setConstructData(this.construct.getConstructData(), c.hasCustomName() ? c.getCustomName().getString() : null);
                        c.remove(Entity.RemovalReason.DISCARDED);
                    } else {
                        this.construct.setCurrentCommand(this.owner, ConstructTasks.STAY.instantiateTask(this.construct));
                        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.return_to_workbench_not_empty", new Object[0]));
                    }
                } else {
                    this.construct.setCurrentCommand(this.owner, ConstructTasks.STAY.instantiateTask(this.construct));
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.return_to_workbench_invalid", new Object[0]));
                }
            }
        } else {
            this.doMove();
        }
    }

    @Override
    public void start() {
        super.start();
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.return_to_workbench", new Object[0]));
    }

    public void setTablePos(BlockPos pos) {
        this.setMoveTarget(pos);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.MODIFY);
    }

    @Override
    public ConstructCommandReturnToTable copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructCommandReturnToTable) {
            this.setMoveTarget(((ConstructCommandReturnToTable)other).getMoveBlockTarget());
        }
        return this;
    }

    @Override
    public ConstructCommandReturnToTable duplicate() {
        return new ConstructCommandReturnToTable(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("tablePos")) {
            this.setMoveTarget(NbtUtils.readBlockPos(nbt, "tablePos").orElse(null));
        }
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        if (this.getMoveBlockTarget() != null) {
            nbt.put("tablePos", (Tag)NbtUtils.writeBlockPos((BlockPos)this.getMoveBlockTarget()));
        }
        return nbt;
    }

    @Override
    public void inflateParameters() {
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

