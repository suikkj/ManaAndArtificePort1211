 
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
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConstructPlaceFluidInTank
extends ConstructCommandBlockInteract<ConstructPlaceFluidInTank> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_STORE, ConstructCapability.FLUID_DISPENSE};
    private int interactTimer = 20;
    private int containerTank = -1;

    public ConstructPlaceFluidInTank(IConstruct<?> construct, ResourceLocation guiIcon) {
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
        if (this.construct.getFluidInTank(1).getAmount() == 0) {
            this.exitCode = 1;
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_tank_empty", this.translate(this.getBlockState())));
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
                    if (state.getBlock() instanceof AbstractCauldronBlock) {
                        this.placeFluidInCauldron(c, state);
                    } else {
                        this.placeFluidInHandler(c, state);
                    }
                }
            }
        } else {
            this.exitCode = 1;
            c.goalSelector.removeGoal((Goal)this);
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.block_missing", new Object[0]));
        }
    }

    private void placeFluidInCauldron(AbstractGolem c, BlockState state) {
        int amount;
        Object fluid = state.getBlock() == Blocks.LAVA_CAULDRON ? Fluids.LAVA : (state.getBlock() == Blocks.WATER_CAULDRON ? Fluids.WATER : Fluids.EMPTY);
        FluidStack containedFluid = new FluidStack((Fluid)fluid, amount = ((AbstractCauldronBlock)state.getBlock()).isFull(state) ? 1000 : 0);
        if (containedFluid.isEmpty()) {
            FluidStack constructFluid = this.construct.getFluidInTank(0);
            if (!constructFluid.isFluidEqual(new FluidStack((Fluid)Fluids.LAVA, 1000)) && !constructFluid.isFluidEqual(new FluidStack((Fluid)Fluids.WATER, 1000))) {
                this.exitCode = 1;
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_invalid_type", this.translate(state)));
                return;
            }
            FluidStack drainStack = new FluidStack(constructFluid.getFluid(), 1000);
            if (constructFluid.getAmount() >= 1000 && this.construct.drain(drainStack, IFluidHandler.FluidAction.SIMULATE).getAmount() == drainStack.getAmount()) {
                this.construct.drain(drainStack, IFluidHandler.FluidAction.EXECUTE);
                c.level().setBlock(this.blockPos, constructFluid.isFluidEqual(new FluidStack((Fluid)Fluids.LAVA, 1000)) ? (BlockState)Blocks.LAVA_CAULDRON.defaultBlockState().setValue((Property)LayeredCauldronBlock.LEVEL, (Comparable)Integer.valueOf(3)) : (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue((Property)LayeredCauldronBlock.LEVEL, (Comparable)Integer.valueOf(3)), 3);
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_success", this.translate(containedFluid), this.translate(state)));
                this.exitCode = 0;
            }
        } else {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_fail", this.translate(state)));
            this.exitCode = 1;
        }
    }

    private void placeFluidInHandler(AbstractGolem c, BlockState state) {
        java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> handler = FluidUtil.getFluidHandler((Level)c.level(), (BlockPos)this.blockPos, (Direction)this.side);
        if (handler.isPresent()) {
            IFluidHandler tank = (IFluidHandler)handler.get();
            this.resolveTank(tank);
            if (this.containerTank == -1) {
                this.exitCode = 1;
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_invalid_type", this.translate(state)));
                return;
            }
            if (this.doMove()) {
                if (this.interactTimer > 0) {
                    if (this.interactTimer == 5) {
                        this.construct.getHandWithCapability(ConstructCapability.CARRY).ifPresent(h -> c.swing(h));
                    }
                    --this.interactTimer;
                } else if (this.interactTimer == 0) {
                    FluidStack stack = this.construct.getFluidInTank(1).copy();
                    if (!stack.isEmpty()) {
                        FluidStack transferred = FluidUtil.tryFluidTransfer((IFluidHandler)tank, (IFluidHandler)this.construct, (int)1000, (boolean)true);
                        if (transferred.isEmpty()) {
                            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_fail", this.translate(state)));
                            this.exitCode = 1;
                        } else {
                            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_success", this.translate(stack), this.translate(state)));
                            this.exitCode = 0;
                        }
                    } else {
                        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_in_tank_fail", this.translate(state)));
                        this.exitCode = 1;
                    }
                    this.interactTimer = -1;
                }
            }
        } else {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.fluid_missing_cap", this.translate(this.getBlockState())));
        }
    }

    private void resolveTank(IFluidHandler tankContainer) {
        for (int i = 0; i < tankContainer.getTanks(); ++i) {
            if (!tankContainer.isFluidValid(i, this.construct.getFluidInTank(1))) continue;
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
        return Registries.ConstructTasks.getKey(ConstructTasks.PLACE_FLUID_IN_CONTAINER);
    }

    @Override
    public ConstructPlaceFluidInTank duplicate() {
        return new ConstructPlaceFluidInTank(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructPlaceFluidInTank copyFrom(ConstructAITask<?> other) {
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


