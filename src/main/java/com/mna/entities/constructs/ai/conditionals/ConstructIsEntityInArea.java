 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.items.IPhylacteryItem;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ConstructIsEntityInArea
extends ConstructConditional<ConstructIsEntityInArea> {
    private ItemStack phylactery = ItemStack.EMPTY;
    private AABB area;

    public ConstructIsEntityInArea(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if (!(this.phylactery.getItem() instanceof IPhylacteryItem)) {
            return false;
        }
        EntityType<? extends Mob> type = ((IPhylacteryItem)this.phylactery.getItem()).getContainedEntity(this.phylactery);
        if (type == null) {
            return false;
        }
        if (this.construct.asEntity().level().getEntities((Entity)this.construct.asEntity(), this.area, e -> e.getType() == type && e.isAlive() && e.isPickable()).size() > 0) {
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, this.area);
            return true;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, this.area);
        return false;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("find_entity.filter").ifPresent(param -> {
            if (param instanceof ConstructTaskItemStackParameter) {
                this.phylactery = ((ConstructTaskItemStackParameter)param).getStack();
            }
        });
        this.getParameter("find_entity.area").ifPresent(param -> {
            if (param instanceof ConstructTaskAreaParameter) {
                this.area = ((ConstructTaskAreaParameter)param).getArea();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskItemStackParameter("find_entity.filter"));
        output.add(new ConstructTaskAreaParameter("find_entity.area"));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.FIND_ENTITY);
    }

    @Override
    public ConstructIsEntityInArea copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsEntityInArea) {
            this.phylactery = ((ConstructIsEntityInArea)other).phylactery.copy();
            this.area = ((ConstructIsEntityInArea)other).area;
        }
        return this;
    }

    @Override
    public ConstructIsEntityInArea duplicate() {
        ConstructIsEntityInArea output = new ConstructIsEntityInArea(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        if (this.phylactery.isEmpty()) {
            return false;
        }
        if (!(this.phylactery.getItem() instanceof IPhylacteryItem)) {
            return false;
        }
        if (this.area == null) {
            return false;
        }
        return ((IPhylacteryItem)this.phylactery.getItem()).getContainedEntity(this.phylactery) != null;
    }
}

