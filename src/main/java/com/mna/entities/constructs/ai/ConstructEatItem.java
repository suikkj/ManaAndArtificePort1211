 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskFilterParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.events.construct.ConstructEatItemEvent;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public class ConstructEatItem
extends ConstructAITask<ConstructEatItem> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private DynamicItemFilter filter = new DynamicItemFilter();
    private int eat_counter = -1;

    public ConstructEatItem(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void tick() {
        super.tick();
        InteractionHand[] hands = this.construct.getCarryingHands(this.filter);
        if (hands.length <= 0) {
            this.forceFail();
            return;
        }
        InteractionHand hand = hands[0];
        if (this.eat_counter == -1) {
            this.getConstructAsEntity().swing(hand);
            this.construct.setEating(hand);
            this.eat_counter = 30;
        } else if (--this.eat_counter <= 0) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.eat_success", new Object[0]));
            ItemStack stackCopy = this.construct.asEntity().getItemInHand(hand).copy();
            NeoForge.EVENT_BUS.post(new ConstructEatItemEvent(this.construct, stackCopy));
            this.construct.asEntity().setItemInHand(hand, ItemStack.EMPTY);
            this.construct.setHappy(100);
            this.construct.resetActions();
            this.setSuccessCode();
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.clearMoveTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.EAT_ITEM);
    }

    @Override
    public ConstructEatItem duplicate() {
        return new ConstructEatItem(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructEatItem copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructEatItem) {
            ConstructEatItem otherEat = (ConstructEatItem)other;
            this.filter.copyFrom(otherEat.filter);
        }
        return this;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public void inflateParameters() {
        this.filter.clear();
        this.getParameter("eat_item.single").ifPresent(param -> {
            ConstructTaskItemStackParameter stackParam;
            if (param instanceof ConstructTaskItemStackParameter && !(stackParam = (ConstructTaskItemStackParameter)param).getStack().isEmpty()) {
                this.filter.getWhiteList().add(stackParam.getStack().copy());
            }
        });
        this.getParameter("eat_item.filter").ifPresent(param -> {
            ConstructTaskFilterParameter filter;
            if (param instanceof ConstructTaskFilterParameter && !(filter = (ConstructTaskFilterParameter)param).getValue().isEmpty()) {
                this.filter.copyFrom(filter.getValue());
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskFilterParameter("eat_item.filter"));
        parameters.add(new ConstructTaskItemStackParameter("eat_item.single"));
        return parameters;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}


