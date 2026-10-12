 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructEntityAreaTask;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.passive.Magmoo;
import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConstructMilkCow
extends ConstructEntityAreaTask<Cow, ConstructMilkCow> {
    private static final String KEY_COW_MILK_TIME = "last_milk_time";
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_STORE, ConstructCapability.FLUID_DISPENSE};
    private int interactTimer = this.getInteractTime(ConstructCapability.FLUID_DISPENSE);
    private FluidStack extractStack;

    public ConstructMilkCow(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, Cow.class);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.locateTarget()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.milk_no_target", new Object[0]), false);
            this.forceFail();
            return;
        }
        this.setMoveTarget((Entity)this.getSelectedTarget());
        if (!this.hasMoveTarget()) {
            this.forceFail();
            return;
        }
        if (this.doMove() && this.milkTarget()) {
            this.releaseEntityMutex((Entity)this.getSelectedTarget());
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.milk_success", this.translate((Entity)this.getSelectedTarget())), false);
            this.setSuccessCode();
        }
    }

    @Override
    protected boolean entityPredicate(Cow candidate) {
        boolean baseline;
        boolean bl = baseline = candidate.isAlive() && !candidate.isBaby() && candidate.getAge() == 0 && this.canCowBeMilked(candidate);
        if (!baseline) {
            return baseline;
        }
        if (candidate instanceof MushroomCow) {
            return false;
        }
        Fluid fluid = (Fluid)NeoForgeMod.MILK.get();
        if (candidate instanceof Magmoo) {
            fluid = Fluids.LAVA;
        }
        this.extractStack = new FluidStack(fluid, 1000);
        return this.construct.isFluidValid(0, this.extractStack);
    }

    @Override
    protected Cow selectTarget(Collection<Cow> entities) {
        if (entities.size() == 0) {
            return null;
        }
        Cow candidate2 = null;
        for (Cow candidate2 : entities) {
            if (!this.claimEntityMutex((Entity)candidate2)) continue;
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.milk_target", this.translate((Entity)candidate2)), false);
            return candidate2;
        }
        return null;
    }

    private boolean canCowBeMilked(Cow cow) {
        return !cow.getPersistentData().contains(KEY_COW_MILK_TIME) || this.construct.asEntity().level().getGameTime() >= cow.getPersistentData().getLong(KEY_COW_MILK_TIME);
    }

    private void setNextMilkTime(Cow cow) {
        cow.getPersistentData().putLong(KEY_COW_MILK_TIME, this.construct.asEntity().level().getGameTime() + 12000L + (long)((int)(Math.random() * 12000.0)));
    }

    private boolean milkTarget() {
        if (this.interactTimer > 0) {
            if (this.interactTimer == 5) {
                this.construct.getHandWithCapability(ConstructCapability.FLUID_DISPENSE).ifPresent(h -> this.construct.asEntity().swing(h));
            }
            --this.interactTimer;
            return false;
        }
        this.setNextMilkTime((Cow)this.getSelectedTarget());
        this.construct.fill(this.extractStack, IFluidHandler.FluidAction.EXECUTE);
        this.interactTimer = this.getInteractTime(ConstructCapability.CARRY);
        return true;
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.clearMoveTarget();
    }

    @Override
    protected String getAreaIdentifier() {
        return "milk.area";
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.MILK);
    }

    @Override
    public ConstructMilkCow duplicate() {
        return new ConstructMilkCow(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructMilkCow copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
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
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}


