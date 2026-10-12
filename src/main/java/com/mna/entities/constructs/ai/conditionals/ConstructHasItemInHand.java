 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskFilterParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ConstructHasItemInHand
extends ConstructConditional<ConstructHasItemInHand> {
    private DynamicItemFilter _filter = new DynamicItemFilter();

    public ConstructHasItemInHand(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if (this._filter == null) {
            return this.construct.getCarryingHands().length > 0;
        }
        return this.construct.hasItem(this._filter);
    }

    @Override
    public void inflateParameters() {
        this._filter.clear();
        this.getParameter("has_item.stack").ifPresent(param -> {
            ConstructTaskItemStackParameter stackParam;
            ItemStack stack;
            if (param instanceof ConstructTaskItemStackParameter && !(stack = (stackParam = (ConstructTaskItemStackParameter)param).getStack()).isEmpty()) {
                this._filter.getWhiteList().add(stack);
            }
        });
        this.getParameter("has_item.filter").ifPresent(param -> {
            ConstructTaskFilterParameter filter;
            if (param instanceof ConstructTaskFilterParameter && !(filter = (ConstructTaskFilterParameter)param).getValue().isEmpty()) {
                this._filter.copyFrom(filter.getValue());
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskFilterParameter("has_item.filter"));
        output.add(new ConstructTaskItemStackParameter("has_item.stack"));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.HAS_ITEM);
    }

    @Override
    public ConstructHasItemInHand copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructHasItemInHand) {
            ConstructHasItemInHand hasItem = (ConstructHasItemInHand)other;
            this._filter.copyFrom(hasItem._filter);
        }
        return this;
    }

    @Override
    public ConstructHasItemInHand duplicate() {
        ConstructHasItemInHand output = new ConstructHasItemInHand(this.construct, this.guiIcon);
        output._filter.copyFrom(this._filter);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

