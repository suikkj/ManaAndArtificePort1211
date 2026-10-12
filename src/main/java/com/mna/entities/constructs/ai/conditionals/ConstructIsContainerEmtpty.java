 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import com.mna.api.tools.MNACaps;

public class ConstructIsContainerEmtpty
extends ConstructConditional<ConstructIsContainerEmtpty> {
    private DirectionalPoint pos;

    public ConstructIsContainerEmtpty(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if (this.pos == null) {
            return false;
        }
        Level world = this.construct.asEntity().level();
        if (!world.isLoaded(this.pos.getPosition())) {
            return false;
        }
        BlockEntity be = world.getBlockEntity(this.pos.getPosition());
        if (be == null) {
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return false;
        }
        java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(be, this.pos.getDirection());
        if (!handler.isPresent()) {
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return false;
        }
        IItemHandler inv = handler.get();
        int slots = inv.getSlots();
        for (int i = 0; i < slots; ++i) {
            if (inv.getStackInSlot(i).isEmpty()) continue;
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return false;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
        return true;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("container_empty.pos").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.pos = ((ConstructTaskPointParameter)param).getPoint();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskPointParameter("container_empty.pos"));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.CONTAINER_EMPTY);
    }

    @Override
    public ConstructIsContainerEmtpty copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsContainerEmtpty) {
            this.pos = ((ConstructIsContainerEmtpty)other).pos;
        }
        return this;
    }

    @Override
    public ConstructIsContainerEmtpty duplicate() {
        ConstructIsContainerEmtpty output = new ConstructIsContainerEmtpty(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.pos != null && this.pos.isValid();
    }
}


