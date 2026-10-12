 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructCommandTileEntityInteract;
import com.mna.api.items.DynamicItemFilter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.InventoryUtilities;
import java.util.EnumSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import com.mna.api.tools.MNACaps;

public class ConstructPlaceItem
extends ConstructCommandTileEntityInteract<BlockEntity, ConstructPlaceItem> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private int interactTimer = 0;
    private int interactionTime = 5;

    public ConstructPlaceItem(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, BlockEntity.class);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (this.construct.getCarryingHands().length > 0 || !InventoryUtilities.getFirstItemFromContainer(new DynamicItemFilter(), this.construct.getCarrySize(), (IItemHandler)this.construct, this.side, true).isEmpty());
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (this.construct.getCarryingHands().length == 0 && InventoryUtilities.getFirstItemFromContainer(new DynamicItemFilter(), this.construct.getCarrySize(), (IItemHandler)this.construct, this.side, true).isEmpty()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_hands_empty", this.translate((BlockEntity)this.getTileEntity())));
            this.forceFail();
            return;
        }
        if (this.getTileEntity() != null) {
            java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(this.getTileEntity(), this.side);
            if (handler.isPresent()) {
                if (this.doMove()) {
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
                    } else if (this.interactTimer == 0) {
                        this.preInteract();
                        ItemStack stack = ItemStack.EMPTY;
                        stack = carrying.length > 0 ? c.getItemInHand(carrying[0]) : InventoryUtilities.getFirstItemFromContainer(new DynamicItemFilter(), this.construct.getCarrySize(), (IItemHandler)this.construct, this.side, true);
                        if (stack.isEmpty()) {
                            this.forceFail();
                            return;
                        }
                        String stackTranslated = this.translate(stack);
                        String containerTranslated = this.translate((BlockEntity)this.getTileEntity());
                        if (InventoryUtilities.mergeIntoInventory(handler.get(), stack)) {
                            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_success", stackTranslated, containerTranslated));
                            if (carrying.length > 0) {
                                c.setItemInHand(carrying[0], ItemStack.EMPTY);
                            } else {
                                InventoryUtilities.getFirstItemFromContainer(new DynamicItemFilter(), this.construct.getCarrySize(), (IItemHandler)this.construct, this.side);
                            }
                            if (this.construct.getCarryingHands().length == 0 && InventoryUtilities.getFirstItemFromContainer(new DynamicItemFilter(), this.construct.getCarrySize(), (IItemHandler)this.construct, this.side, true).isEmpty()) {
                                this.setSuccessCode();
                                return;
                            }
                        } else {
                            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fail", stackTranslated, containerTranslated));
                            this.forceFail();
                        }
                    }
                }
            } else {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.inv_missing_cap", this.translate((BlockEntity)this.getTileEntity())));
                this.forceFail();
            }
        }
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = this.interactionTime = Math.max(this.getInteractTime(ConstructCapability.CARRY) / 5, 1);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.PLACE_ITEM);
    }

    @Override
    public ConstructCommandTileEntityInteract<BlockEntity, ConstructPlaceItem> duplicate() {
        return new ConstructPlaceItem(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public boolean areCapabilitiesMet() {
        if (this.construct.getConstructData().areCapabilitiesEnabled(ConstructCapability.ITEM_STORAGE)) {
            return true;
        }
        return super.areCapabilitiesMet();
    }
}


