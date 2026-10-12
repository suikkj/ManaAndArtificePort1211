 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskFilterParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACaps;

public class ConstructIsItemInContainer
extends ConstructConditional<ConstructIsItemInContainer> {
    private DirectionalPoint pos;
    private DynamicItemFilter filter = new DynamicItemFilter();
    private int minimum;
    private boolean individual_items = false;

    public ConstructIsItemInContainer(IConstruct<?> construct, ResourceLocation guiIcon) {
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
        HashMap<Integer, Integer> hashCount = new HashMap<Integer, Integer>();
        IItemHandler inv = handler.get();
        int slots = inv.getSlots();
        int total = 0;
        for (int i = 0; i < slots; ++i) {
            ItemStack invStack = inv.getStackInSlot(i);
            if (!this.filter.matches(invStack)) continue;
            int stackHash = this.filter.hashStack(invStack);
            hashCount.compute(stackHash, (k, v) -> {
                int count = invStack.getCount();
                return v == null ? count : v + count;
            });
            if (this.individual_items || (total += invStack.getCount()) < this.minimum) continue;
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return true;
        }
        MutableBoolean individualCount = new MutableBoolean(false);
        if (this.individual_items) {
            hashCount.forEach((k, v) -> {
                if (v >= this.minimum) {
                    individualCount.setTrue();
                }
            });
        }
        if (individualCount.booleanValue()) {
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return true;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
        return false;
    }

    @Override
    public void inflateParameters() {
        this.filter.clear();
        this.getParameter("item_in_container.amount").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                ConstructTaskIntegerParameter intParam = (ConstructTaskIntegerParameter)param;
                this.minimum = intParam.getValue();
            }
        });
        this.getParameter("item_in_container.stack").ifPresent(param -> {
            ConstructTaskItemStackParameter stackParam;
            ItemStack stack;
            if (param instanceof ConstructTaskItemStackParameter && !(stack = (stackParam = (ConstructTaskItemStackParameter)param).getStack()).isEmpty()) {
                this.filter.getWhiteList().add(stack);
            }
        });
        this.getParameter("item_in_container.filter").ifPresent(param -> {
            ConstructTaskFilterParameter filterParam;
            if (param instanceof ConstructTaskFilterParameter && !(filterParam = (ConstructTaskFilterParameter)param).getValue().isEmpty()) {
                this.filter.copyFrom(filterParam.getValue());
            }
        });
        this.getParameter("item_in_container.pos").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter pointParam = (ConstructTaskPointParameter)param;
                this.pos = pointParam.getPoint();
            }
        });
        this.getParameter("item_in_container.individual").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                ConstructTaskBooleanParameter boolParam = (ConstructTaskBooleanParameter)param;
                this.individual_items = boolParam.getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskFilterParameter("item_in_container.filter"));
        output.add(new ConstructTaskItemStackParameter("item_in_container.stack"));
        output.add(new ConstructTaskPointParameter("item_in_container.pos"));
        output.add(new ConstructTaskIntegerParameter("item_in_container.amount", 1, 128, 1, 1));
        output.add(new ConstructTaskBooleanParameter("item_in_container.individual"));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.CONTAINER_FIND);
    }

    @Override
    public ConstructIsItemInContainer copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsItemInContainer) {
            ConstructIsItemInContainer itemInContainer = (ConstructIsItemInContainer)other;
            this.pos = itemInContainer.pos;
            this.filter.copyFrom(itemInContainer.filter);
            this.minimum = itemInContainer.minimum;
            this.individual_items = itemInContainer.individual_items;
        }
        return this;
    }

    @Override
    public ConstructIsItemInContainer duplicate() {
        ConstructIsItemInContainer output = new ConstructIsItemInContainer(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.pos != null && this.pos.isValid();
    }
}


