 
package com.mna.entities.constructs.ai;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructCommandTileEntityInteract;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskFilterParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.InventoryUtilities;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import com.mna.api.tools.MNACaps;

public class ConstructTakeItem
extends ConstructCommandTileEntityInteract<BlockEntity, ConstructTakeItem> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private DynamicItemFilter filter;
    private boolean randomItemFromFilter = false;
    private int minimumQuantityToTake = 0;
    private int interactTimer = 20;

    public ConstructTakeItem(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, BlockEntity.class);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.filter = new DynamicItemFilter();
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (this.interactTimer < 0) {
            --this.interactTimer;
            if (this.interactTimer <= -16) {
                this.forceFail();
            }
            return;
        }
        if (!InventoryUtilities.hasEmptySlot((IItemHandler)this.construct) && this.construct.getEmptyHands().length == 0) {
            this.forceFail();
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_hands_full", this.translate((BlockEntity)this.getTileEntity())));
            return;
        }
        if (this.getTileEntity() != null) {
            java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(this.getTileEntity(), this.side);
            if (handler.isPresent()) {
                if (this.doMove()) {
                    if (this.interactTimer > 0) {
                        if (this.interactTimer == 5 && !InventoryUtilities.getFirstItemFromContainer(this.filter, this.construct.getCarrySize(), handler.get(), this.side, this.randomItemFromFilter, true).isEmpty()) {
                            this.construct.getHandWithCapability(ConstructCapability.CARRY).ifPresent(h -> c.swing(h));
                        }
                        --this.interactTimer;
                    } else if (this.interactTimer == 0) {
                        this.preInteract();
                        ItemStack toTake = ItemStack.EMPTY;
                        boolean didTake = false;
                        if (this.randomItemFromFilter || this.construct.getIntelligence() <= 8) {
                            toTake = InventoryUtilities.getFirstItemFromContainer(this.filter, this.construct.getCarrySize(), handler.get(), this.side, this.randomItemFromFilter, true);
                            didTake = this.take(toTake, handler.get(), c);
                        } else {
                            toTake = InventoryUtilities.getFirstItemFromContainer(this.filter, this.construct.getCarrySize(), handler.get(), this.side, false, true);
                            if (this.take(toTake, handler.get(), c)) {
                                didTake = true;
                            }
                        }
                        if (didTake) {
                            this.setSuccessCode();
                        } else {
                            this.forceFail();
                        }
                        this.interactTimer = -1;
                    }
                }
            } else {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.inv_missing_cap", this.translate((BlockEntity)this.getTileEntity())));
            }
        } else {
            this.exitCode = 1;
            c.goalSelector.removeGoal((Goal)this);
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.te_missing", new Object[0]));
        }
    }

    private boolean take(ItemStack toTake, IItemHandler handler, AbstractGolem c) {
        if (!toTake.isEmpty()) {
            int count = InventoryUtilities.countItem(toTake, handler, this.side, this.filter.getWhitelistMatchDurability(), this.filter.getWhitelistMatchTag());
            if (count > this.minimumQuantityToTake) {
                DynamicItemFilter tempFilter = new DynamicItemFilter();
                tempFilter.getWhiteList().add(toTake);
                ItemStack stack = InventoryUtilities.getFirstItemFromContainer(tempFilter, this.construct.getCarrySize(), handler, this.side, false, true);
                InteractionHand[] emptyHands = this.construct.getEmptyHands();
                if (emptyHands.length != 0) {
                    stack = InventoryUtilities.getFirstItemFromContainer(tempFilter, this.construct.getCarrySize(), handler, this.side, false, false);
                    c.setItemInHand(emptyHands[0], stack);
                } else if (InventoryUtilities.hasRoomFor((IItemHandler)this.construct, stack)) {
                    stack = InventoryUtilities.getFirstItemFromContainer(tempFilter, this.construct.getCarrySize(), handler, this.side, false, false);
                    InventoryUtilities.mergeIntoInventory((IItemHandler)this.construct, stack);
                } else {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fail", this.translate((BlockEntity)this.getTileEntity())));
                    return false;
                }
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_success", this.translate(toTake), this.translate((BlockEntity)this.getTileEntity())));
                return true;
            }
            return false;
        }
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fail", this.translate((BlockEntity)this.getTileEntity())));
        return false;
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.TAKE);
    }

    @Override
    public ConstructTakeItem duplicate() {
        return new ConstructTakeItem(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructTakeItem copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructTakeItem) {
            ConstructTakeItem otherTake = (ConstructTakeItem)other;
            this.filter.copyFrom(otherTake.filter);
            this.randomItemFromFilter = otherTake.randomItemFromFilter;
            this.minimumQuantityToTake = otherTake.minimumQuantityToTake;
        }
        return this;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        super.readNBT(nbt);
        if (nbt.contains("filter")) {
            this.filter.loadFromTag(nbt.getCompound("filter"));
        }
        if (nbt.contains("random")) {
            this.randomItemFromFilter = nbt.getBoolean("random");
        }
        if (nbt.contains("minimum")) {
            this.minimumQuantityToTake = nbt.getInt("minimum");
        }
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        nbt = super.writeInternal(nbt);
        nbt.put("filter", (Tag)this.filter.getTag());
        nbt.putBoolean("random", this.randomItemFromFilter);
        nbt.putInt("minimum", this.minimumQuantityToTake);
        return nbt;
    }

    @Override
    public void inflateParameters() {
        super.inflateParameters();
        this.filter.clear();
        this.getParameter("take.stack").ifPresent(param -> {
            ConstructTaskItemStackParameter stackParam;
            if (param instanceof ConstructTaskItemStackParameter && !(stackParam = (ConstructTaskItemStackParameter)param).getStack().isEmpty()) {
                this.filter.getWhiteList().add(stackParam.getStack().copy());
            }
        });
        this.getParameter("take.filter").ifPresent(param -> {
            ConstructTaskFilterParameter filter;
            if (param instanceof ConstructTaskFilterParameter && !(filter = (ConstructTaskFilterParameter)param).getValue().isEmpty()) {
                this.filter.copyFrom(filter.getValue());
            }
        });
        this.getParameter("take.random").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                ConstructTaskBooleanParameter takeRandom = (ConstructTaskBooleanParameter)param;
                this.randomItemFromFilter = takeRandom.getValue();
            }
        });
        this.getParameter("take.quantity").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                ConstructTaskIntegerParameter takeQuantity = (ConstructTaskIntegerParameter)param;
                this.minimumQuantityToTake = takeQuantity.getValue();
            }
        });
    }

    @Override
    public List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskFilterParameter("take.filter"));
        parameters.add(new ConstructTaskItemStackParameter("take.stack"));
        parameters.add(new ConstructTaskBooleanParameter("take.random"));
        parameters.add(new ConstructTaskIntegerParameter("take.quantity", 0, 128, 1, 0));
        return parameters;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}


