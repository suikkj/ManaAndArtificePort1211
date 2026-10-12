 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ConstructDropItem
extends ConstructAITask<ConstructDropItem> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private int interactTimer = 20;
    protected BlockPos blockPos;

    public ConstructDropItem(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.blockPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (this.construct.getCarryingHands().length == 0) {
            if (!this.isSuccess()) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.drop_item_hands_empty", new Object[0]));
            }
            return;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.blockPos));
        this.setMoveTarget(this.blockPos);
        if (this.doMove()) {
            InteractionHand[] carrying = this.construct.getCarryingHands();
            if (this.interactTimer > 0) {
                if (this.interactTimer == 5 && carrying.length > 0) {
                    c.swing(carrying[0]);
                }
                --this.interactTimer;
            } else if (this.interactTimer == 0) {
                if (carrying.length == 0) {
                    this.exitCode = 1;
                    return;
                }
                String itemTranslated = "";
                InteractionHand hand = carrying[0];
                ItemStack stack = c.getItemInHand(hand);
                ItemEntity item = new ItemEntity(c.level(), (double)this.blockPos.getX() + 0.5, (double)this.blockPos.getY() + 0.5, (double)this.blockPos.getZ() + 0.5, stack.copy());
                item.setDeltaMovement(0.0, 0.25, 0.0);
                item.setDefaultPickUpDelay();
                c.level().addFreshEntity((Entity)item);
                c.setItemInHand(hand, ItemStack.EMPTY);
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.drop_item_success", itemTranslated, this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ()));
                this.exitCode = 0;
            }
        }
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.DROP_ITEM);
    }

    @Override
    public ConstructDropItem duplicate() {
        return new ConstructDropItem(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructDropItem copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructDropItem) {
            this.blockPos = ((ConstructDropItem)other).blockPos;
        }
        return this;
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        if (this.blockPos != null) {
            nbt.put("blockPos", (Tag)NbtUtils.writeBlockPos((BlockPos)this.blockPos));
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("blockPos")) {
            this.blockPos = NbtUtils.readBlockPos(nbt, "blockPos").orElse(null);
        }
    }

    @Override
    public void inflateParameters() {
        this.getParameter("drop.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.blockPos = ((ConstructTaskPointParameter)param).getPosition();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("drop.point"));
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.blockPos != null;
    }
}

