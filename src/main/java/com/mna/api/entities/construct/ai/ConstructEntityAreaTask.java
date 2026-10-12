/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.animal.AbstractGolem
 *  net.minecraft.world.phys.AABB
 */
package com.mna.api.entities.construct.ai;

import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import java.util.Collection;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.phys.AABB;

public abstract class ConstructEntityAreaTask<E extends Entity, T extends ConstructEntityAreaTask<E, ?>>
extends ConstructAITask<T> {
    protected static final int MAX_SIZE = 64;
    protected AABB area;
    private E selectedTarget;
    private boolean tooBig = false;
    private Class<E> entityClass;

    public ConstructEntityAreaTask(IConstruct<?> construct, ResourceLocation guiIcon, Class<E> entityClass) {
        super(construct, guiIcon);
        this.entityClass = entityClass;
    }

    protected void setSelectedTarget(E target) {
        this.selectedTarget = target;
        this.updateDiagnostics();
    }

    protected E getSelectedTarget() {
        return this.selectedTarget;
    }

    private void updateDiagnostics() {
        if (this.construct != null) {
            if (this.selectedTarget != null) {
                this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, this.selectedTarget.getId());
            } else {
                this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, -1);
            }
        }
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.updateDiagnostics();
    }

    protected boolean locateTarget() {
        AbstractGolem c = this.getConstructAsEntity();
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, this.area);
        List entities = c.level().getEntitiesOfClass(this.entityClass, this.area, e -> {
            if (e == null || !e.isAlive()) {
                return false;
            }
            return this.entityPredicate(e);
        });
        if (entities.size() == 0) {
            return false;
        }
        E selected = (E)this.selectTarget(entities);
        if (selected != null) {
            this.setSelectedTarget(selected);
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, selected.getId());
            return true;
        }
        return false;
    }

    protected abstract boolean entityPredicate(E var1);

    protected abstract E selectTarget(Collection<E> var1);

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
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (this.area == null) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_not_configured", new Object[0]), false);
            return false;
        }
        if (this.tooBig) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.area_too_big", 64, this.area.getXsize(), this.area.getYsize(), this.area.getZsize()), false);
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public T copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructEntityAreaTask) {
            this.area = ((ConstructEntityAreaTask)other).area;
            this.entityClass = ((ConstructEntityAreaTask)other).entityClass;
        }
        return (T)(this);
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    protected CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }
}

