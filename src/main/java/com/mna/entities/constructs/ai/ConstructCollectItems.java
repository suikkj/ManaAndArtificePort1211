 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructEntityAreaTask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskFilterParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.InventoryUtilities;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;

public class ConstructCollectItems
extends ConstructEntityAreaTask<ItemEntity, ConstructCollectItems> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private int numItemsCollected = 0;
    private DynamicItemFilter filter = new DynamicItemFilter();

    public ConstructCollectItems(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, ItemEntity.class);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getSelectedTarget() == null && !this.locateTarget()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.collect_no_items", new Object[0]), false);
            this.forceFail();
            return;
        }
        if (!((ItemEntity)this.getSelectedTarget()).isAlive()) {
            this.releaseMutexes();
            this.setSelectedTarget(null);
            if (!this.locateTarget()) {
                this.forceFail();
                return;
            }
        }
        this.setMoveTarget((Entity)this.getSelectedTarget());
        if (this.doMove()) {
            InteractionHand[] emptyHands = this.construct.getEmptyHands();
            ItemStack merge = ((ItemEntity)this.getSelectedTarget()).getItem().copy();
            boolean putInHand = false;
            if (emptyHands.length != 0) {
                for (int i = 0; i < emptyHands.length; ++i) {
                    ItemStack hand = merge.copy();
                    hand.setCount(Math.min(this.construct.getCarrySize(), hand.getCount()));
                    this.construct.asEntity().setItemInHand(emptyHands[i], hand);
                    merge.shrink(hand.getCount());
                    putInHand = true;
                }
            }
            if (merge.isEmpty() || InventoryUtilities.mergeIntoInventory((IItemHandler)this.construct, merge)) {
                this.construct.asEntity().onItemPickup((ItemEntity)this.getSelectedTarget());
                this.construct.asEntity().take(this.getSelectedTarget(), ((ItemEntity)this.getSelectedTarget()).getItem().getCount());
                ((ItemEntity)this.getSelectedTarget()).discard();
                ++this.numItemsCollected;
            } else {
                ((ItemEntity)this.getSelectedTarget()).setItem(merge);
                if (!putInHand && this.numItemsCollected == 0) {
                    this.forceFail();
                } else {
                    this.setSuccessCode();
                }
                return;
            }
            if (!this.locateTarget()) {
                this.setSuccessCode();
                return;
            }
        }
    }

    @Override
    protected boolean entityPredicate(ItemEntity candidate) {
        boolean basePredicate;
        boolean bl = basePredicate = candidate.isAlive() && (this.construct.getEmptyHands().length > 0 || InventoryUtilities.hasRoomFor((IItemHandler)this.construct, candidate.getItem()));
        if (!basePredicate) {
            return false;
        }
        return this.filter.matches(candidate.getItem());
    }

    @Override
    protected ItemEntity selectTarget(Collection<ItemEntity> entities) {
        AbstractGolem c = this.getConstructAsEntity();
        List items = entities.stream().sorted(Comparator.comparing(item -> Float.valueOf(item.distanceTo((Entity)c)))).collect(Collectors.toList());
        int itemIndex = 0;
        ItemEntity candidate = (ItemEntity)items.get(itemIndex);
        while (!this.claimEntityMutex((Entity)candidate)) {
            if (++itemIndex >= items.size()) {
                return null;
            }
            candidate = (ItemEntity)items.get(itemIndex);
        }
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.collect_target", this.translate((Entity)candidate)), false);
        return candidate;
    }

    @Override
    public void stop() {
        this.clearMoveTarget();
        this.setSelectedTarget(null);
        this.construct.asEntity().setCanPickUpLoot(false);
        super.stop();
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.numItemsCollected = 0;
    }

    public void setArea(AABB aabb) {
        this.area = aabb;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.GATHER_ITEMS);
    }

    @Override
    public ConstructCollectItems duplicate() {
        return new ConstructCollectItems(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskFilterParameter("collect.filter"));
        parameters.add(new ConstructTaskItemStackParameter("collect.filter_single"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        super.inflateParameters();
        this.filter.clear();
        this.getParameter("collect.filter_single").ifPresent(param -> {
            ConstructTaskItemStackParameter stackParam;
            if (param instanceof ConstructTaskItemStackParameter && !(stackParam = (ConstructTaskItemStackParameter)param).getStack().isEmpty()) {
                this.filter.getWhiteList().add(stackParam.getStack().copy());
            }
        });
        this.getParameter("collect.filter").ifPresent(param -> {
            ConstructTaskFilterParameter filterParam;
            if (param instanceof ConstructTaskFilterParameter && !(filterParam = (ConstructTaskFilterParameter)param).getValue().isEmpty()) {
                this.filter.copyFrom(filterParam.getValue());
            }
        });
    }

    @Override
    public ConstructCollectItems copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructCollectItems) {
            this.setSelectedTarget((ItemEntity)((ConstructCollectItems)other).getSelectedTarget());
            this.filter.copyFrom(((ConstructCollectItems)other).filter);
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
    protected String getAreaIdentifier() {
        return "collect.area";
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}

