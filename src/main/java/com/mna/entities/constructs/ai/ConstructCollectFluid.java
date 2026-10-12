 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructBlockAreaTask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConstructCollectFluid
extends ConstructBlockAreaTask<ConstructCollectFluid> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_STORE, ConstructCapability.FLUID_DISPENSE};
    private ItemStack fluidStack;

    public ConstructCollectFluid(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.construct.getTankCapacity(1) == this.construct.getStoredFluidAmount()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_tank_full", new Object[0]));
            this.exitCode = 1;
            return;
        }
        if (!this.findBlockTarget()) {
            return;
        }
        if (this.moveToCurrentTarget()) {
            this.harvest();
        }
    }

    private void harvest() {
        AbstractGolem c = this.getConstructAsEntity();
        BlockState state = c.level().getBlockState(this.currentTarget);
        FluidState flState = state.getFluidState();
        if (flState == null || !flState.isSource()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.fluid_invalid", this.translate(state), this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ()));
        } else {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.collecting_fluid", this.translate(state), this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ()));
            Fluid fluid = flState.getType();
            int fluidAmt = Math.max(flState.getAmount(), 1000);
            this.construct.fill(new FluidStack(fluid, fluidAmt), IFluidHandler.FluidAction.EXECUTE);
            if (state.getBlock() instanceof BucketPickup) {
                BucketPickup bp = (BucketPickup)state.getBlock();
                bp.pickupBlock((net.minecraft.world.entity.player.Player)null, (LevelAccessor)c.level(), this.currentTarget, state);
            }
            c.level().gameEvent((Entity)c, GameEvent.FLUID_PICKUP, this.currentTarget);
        }
        this.removeBlockTarget(this.currentTarget);
        this.currentTarget = null;
        this.exitCode = 0;
    }

    @Override
    protected boolean isValidBlock(BlockState state, BlockPos pos) {
        return ConstructCollectFluid.blockValid(state);
    }

    public static boolean blockValid(BlockState state) {
        FluidState flState = state.getFluidState();
        if (flState == null) {
            return false;
        }
        return flState.isSource();
    }

    @Override
    public void stop() {
        super.stop();
        this.interactTimer = this.getInteractTime(ConstructCapability.HARVEST);
    }

    @Override
    public void inflateParameters() {
        super.inflateParameters();
        this.getParameter("collect_fluid.stack").ifPresent(param -> {
            if (param instanceof ConstructTaskItemStackParameter) {
                this.fluidStack = ((ConstructTaskItemStackParameter)param).getStack();
            }
        });
    }

    @Override
    public List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskItemStackParameter("collect_fluid.stack"));
        return parameters;
    }

    @Override
    public ConstructCollectFluid duplicate() {
        return new ConstructCollectFluid(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCollectFluid copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructCollectFluid) {
            this.fluidStack = ((ConstructCollectFluid)other).fluidStack;
        }
        return this;
    }

    @Override
    protected int getInteractTimer() {
        return this.getInteractTime(ConstructCapability.FLUID_DISPENSE);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.GATHER_FLUID);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    protected String getAreaIdentifier() {
        return "collect_fluid.area";
    }
}

