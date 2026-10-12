 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.InventoryUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import com.mna.api.tools.MNACaps;

public class ConstructSortItems
extends ConstructAITask<ConstructSortItems> {
    private static final int NUM_DESTINATIONS = 6;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private static final DynamicItemFilter emptyFilter = new DynamicItemFilter();
    ArrayList<DirectionalPoint> points = new ArrayList();
    private int interactTimer = 0;
    private int constructInventoryIndex = -2;
    private ItemStack curSort = ItemStack.EMPTY;
    private Optional<DirectionalPoint> insertLocation = Optional.empty();

    public ConstructSortItems(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.constructInventoryIndex >= this.construct.getSlots()) {
            this.setExitCode();
            return;
        }
        if (this.curSort.isEmpty()) {
            this.curSort = this.getNextSortItem();
            if (this.curSort.isEmpty()) {
                this.setExitCode();
                return;
            }
        }
        if (this.insertLocation.isEmpty()) {
            this.insertLocation = this.getPosFor(this.curSort);
            if (this.insertLocation.isEmpty()) {
                this.curSort = ItemStack.EMPTY;
                return;
            }
            this.setMoveTarget(this.insertLocation.get().getPosition());
        }
        if (this.doMove()) {
            AbstractGolem c = this.construct.asEntity();
            InteractionHand[] carrying = this.construct.getCarryingHands();
            if (this.interactTimer > 0) {
                if (this.interactTimer == 5) {
                    if (carrying.length > 0) {
                        c.swing(carrying[0]);
                    } else {
                        c.swing(InteractionHand.MAIN_HAND);
                    }
                }
                --this.interactTimer;
            } else if (this.interactTimer == 0 && !this.performSortInsert(this.curSort, this.insertLocation.get())) {
                this.insertLocation = Optional.empty();
            }
        }
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (this.construct.getCarryingHands().length > 0 || this.construct.hasItem(emptyFilter));
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.constructInventoryIndex = -2;
        this.interactTimer = 0;
        this.curSort = ItemStack.EMPTY;
        this.insertLocation = Optional.empty();
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    private void setExitCode() {
        if (!this.construct.hasItem(emptyFilter)) {
            this.setSuccessCode();
        } else {
            this.forceFail();
        }
    }

    private Optional<DirectionalPoint> getPosFor(ItemStack sortStack) {
        AbstractGolem c = this.construct.asEntity();
        Level level = c.level();
        return this.points.stream().filter(p -> {
            if (!level.isLoaded(p.getPosition())) {
                return false;
            }
            BlockEntity be = level.getBlockEntity(p.getPosition());
            if (be == null) {
                return false;
            }
            java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(be, p.getDirection());
            if (!handler.isPresent()) {
                return false;
            }
            if (!InventoryUtilities.hasStackInInventory(new ItemStack((ItemLike)sortStack.getItem()), true, true, handler.get(), p.getDirection())) {
                return false;
            }
            return InventoryUtilities.hasRoomForAny(handler.get(), sortStack);
        }).findFirst();
    }

    private boolean performSortInsert(ItemStack sortStack, DirectionalPoint point) {
        AbstractGolem c = this.construct.asEntity();
        Level level = c.level();
        if (!level.isLoaded(point.getPosition())) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(point.getPosition());
        if (be == null) {
            return false;
        }
        java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(be, point.getDirection());
        if (!handler.isPresent()) {
            return false;
        }
        if (!InventoryUtilities.hasStackInInventory(new ItemStack((ItemLike)sortStack.getItem()), true, true, handler.get(), point.getDirection())) {
            return false;
        }
        String stackTranslated = this.translate(sortStack);
        String containerTranslated = this.translate(be);
        int initialSize = sortStack.getCount();
        boolean fullMerge = InventoryUtilities.mergeIntoInventory(handler.get(), sortStack);
        if (fullMerge) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.sort_success", stackTranslated, containerTranslated));
        } else {
            this.insertLocation = Optional.empty();
            if (sortStack.getCount() == initialSize) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.sort_fail", stackTranslated, containerTranslated));
            } else {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.sort_success_partial", stackTranslated, containerTranslated));
                return true;
            }
        }
        return false;
    }

    private ItemStack getNextSortItem() {
        ItemStack next = ItemStack.EMPTY;
        do {
            next = this.constructInventoryIndex == -2 ? this.construct.asEntity().getMainHandItem() : (this.constructInventoryIndex == -1 ? this.construct.asEntity().getOffhandItem() : this.construct.getStackInSlot(this.constructInventoryIndex));
            ++this.constructInventoryIndex;
        } while (next.isEmpty() && this.constructInventoryIndex < this.construct.getSlots());
        return next;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.SORT_ITEMS);
    }

    @Override
    public void inflateParameters() {
        this.points.clear();
        for (int i = 0; i < 6; ++i) {
            this.getParameter("sort_items.point." + i).ifPresent(param -> {
                ConstructTaskPointParameter point;
                if (param instanceof ConstructTaskPointParameter && (point = (ConstructTaskPointParameter)param).getPoint() != null) {
                    this.points.add(point.getPoint());
                }
            });
        }
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> parameters = new ArrayList<ConstructAITaskParameter>();
        for (int i = 0; i < 6; ++i) {
            parameters.add(new ConstructTaskPointParameter("sort_items.point." + i));
        }
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.points.size() > 0;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    protected CompoundTag writeInternal(CompoundTag nbt) {
        return new CompoundTag();
    }

    @Override
    public ConstructSortItems copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructSortItems) {
            ConstructSortItems sort = (ConstructSortItems)other;
            this.points.clear();
            sort.points.forEach(dp -> this.points.add(DirectionalPoint.of(dp.save())));
        }
        return this;
    }

    @Override
    public ConstructSortItems duplicate() {
        return new ConstructSortItems(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }
}


