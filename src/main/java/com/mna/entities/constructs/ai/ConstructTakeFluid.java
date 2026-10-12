 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.entities.constructs.ai.base.ConstructCommandBlockInteract;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConstructTakeFluid
extends ConstructCommandBlockInteract<ConstructTakeFluid> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_STORE, ConstructCapability.FLUID_DISPENSE};
    private int interactTimer = 20;
    private int containerTank = -1;

    public ConstructTakeFluid(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        BlockState state = this.getBlockState();
        if (this.interactTimer < 0) {
            --this.interactTimer;
            if (this.interactTimer <= -16) {
                this.exitCode = 1;
            }
            return;
        }
        if (this.construct.getFluidInTank(1).getAmount() == this.construct.getTankCapacity(1)) {
            this.exitCode = 1;
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_tank_full", this.translate(state)));
            return;
        }
        if (state != null) {
            if (this.doMove()) {
                if (this.interactTimer > 0) {
                    if (this.interactTimer == 5) {
                        this.construct.getHandWithCapability(ConstructCapability.FLUID_DISPENSE).ifPresent(h -> c.swing(h));
                    }
                    --this.interactTimer;
                } else if (this.interactTimer == 0) {
                    if (state.getBlock() instanceof LayeredCauldronBlock) {
                        this.takeFluidFromCauldron(state, c);
                    } else {
                        this.takeFluidFromHandler(state, c);
                    }
                    this.interactTimer = -1;
                }
            }
        } else {
            this.exitCode = 1;
            c.goalSelector.removeGoal((Goal)this);
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.block_missing", new Object[0]));
        }
    }

    private void takeFluidFromCauldron(BlockState state, AbstractGolem c) {
        int amount;
        Object fluid = state.getBlock() == Blocks.LAVA_CAULDRON ? Fluids.LAVA : (state.getBlock() == Blocks.WATER_CAULDRON ? Fluids.WATER : Fluids.EMPTY);
        FluidStack containedFluid = new FluidStack((Fluid)fluid, amount = ((LayeredCauldronBlock)state.getBlock()).isFull(state) ? 1000 : 0);
        if (!containedFluid.isEmpty() && ((LayeredCauldronBlock)state.getBlock()).isFull(state)) {
            if (this.construct.isFluidValid(0, containedFluid)) {
                this.construct.fill(containedFluid, IFluidHandler.FluidAction.EXECUTE);
                c.level().setBlock(this.blockPos, (BlockState)state.setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(0)), 3);
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_in_tank_success", this.translate(containedFluid), this.translate(state)));
                this.exitCode = 0;
            } else {
                this.exitCode = 1;
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_invalid_type", this.translate(state)));
            }
        } else {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_fail", this.translate(state)));
            this.exitCode = 1;
        }
    }

    private void takeFluidFromHandler(BlockState state, AbstractGolem c) {
        java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> handler = FluidUtil.getFluidHandler((Level)c.level(), (BlockPos)this.blockPos, (Direction)this.side);
        if (handler.isPresent()) {
            IFluidHandler tank = (IFluidHandler)handler.get();
            this.resolveTank(tank);
            if (this.containerTank == -1) {
                this.exitCode = 1;
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_invalid_type", this.translate(state)));
                return;
            }
            FluidStack containedFluid = tank.getFluidInTank(this.containerTank).copy();
            if (!containedFluid.isEmpty()) {
                FluidStack transferred = FluidUtil.tryFluidTransfer((IFluidHandler)this.construct, (IFluidHandler)tank, (int)1000, (boolean)true);
                if (transferred.isEmpty()) {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_in_tank_fail", this.translate(state)));
                    this.exitCode = 1;
                } else {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_in_tank_success", this.translate(containedFluid), this.translate(state)));
                    this.exitCode = 0;
                }
            } else {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.take_fluid_fail", this.translate(state)));
                this.exitCode = 1;
            }
        }
    }

    private void resolveTank(IFluidHandler tankContainer) {
        for (int i = 0; i < tankContainer.getTanks(); ++i) {
            if (!this.construct.isFluidValid(1, tankContainer.getFluidInTank(i))) continue;
            this.containerTank = i;
            return;
        }
        this.containerTank = -1;
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.TAKE_FLUID_FROM_CONTAINER);
    }

    @Override
    public ConstructTakeFluid duplicate() {
        return new ConstructTakeFluid(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructTakeFluid copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        return this;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        super.readNBT(nbt);
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        nbt = super.writeInternal(nbt);
        return nbt;
    }

    @Override
    public void inflateParameters() {
        super.inflateParameters();
    }

    @Override
    public List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        return parameters;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}


