 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;

public class ConstructCommandStay
extends ConstructAITask<ConstructCommandStay> {
    public ConstructCommandStay(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public void start() {
        super.start();
        this.construct.asEntity().getNavigation().stop();
        this.construct.asEntity().setTarget(null);
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.stay", new Object[0]));
    }

    @Override
    public boolean allowSteeringMountedConstructsDuringTask() {
        return true;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.STAY);
    }

    @Override
    public ConstructCommandStay copyFrom(ConstructAITask<?> other) {
        return this;
    }

    @Override
    public ConstructCommandStay duplicate() {
        return new ConstructCommandStay(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return new CompoundTag();
    }

    @Override
    public void inflateParameters() {
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

