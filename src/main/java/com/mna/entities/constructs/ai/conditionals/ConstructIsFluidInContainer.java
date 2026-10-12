 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class ConstructIsFluidInContainer
extends ConstructConditional<ConstructIsFluidInContainer> {
    private DirectionalPoint pos;
    private ItemStack fluid = ItemStack.EMPTY;
    private int minPct;

    public ConstructIsFluidInContainer(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler;
        Level world;
        if (this.pos == null) {
            return false;
        }
        FluidStack searchFluid = FluidStack.EMPTY;
        java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandlerItem> itemHandler = net.neoforged.neoforge.fluids.FluidUtil.getFluidHandler(this.fluid);
        if (itemHandler.isPresent()) {
            IFluidHandlerItem flHItem = (IFluidHandlerItem)itemHandler.get();
            for (int i = 0; i < flHItem.getTanks() && (searchFluid = flHItem.getFluidInTank(i)).isEmpty(); ++i) {
            }
            if (searchFluid.isEmpty()) {
                return false;
            }
        }
        if (!(world = this.construct.asEntity().level()).isLoaded(this.pos.getPosition())) {
            return false;
        }
        BlockState state = world.getBlockState(this.pos.getPosition());
        if (state.getBlock() instanceof LayeredCauldronBlock) {
            net.minecraft.world.level.material.Fluid fluid = state.getBlock() == Blocks.LAVA_CAULDRON ? Fluids.LAVA : (state.getBlock() == Blocks.WATER_CAULDRON ? Fluids.WATER : Fluids.EMPTY);
            int amount = ((LayeredCauldronBlock)state.getBlock()).isFull(state) ? 1000 : (Integer)state.getValue((Property)LayeredCauldronBlock.LEVEL) * 333;
            FluidStack contained = new FluidStack((Fluid)fluid, amount);
            if ((searchFluid.isEmpty() || searchFluid.isFluidEqual(contained)) && amount > 0) {
                this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
                return true;
            }
        }
        if (!(handler = FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection())).isPresent()) {
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return false;
        }
        IFluidHandler tank = (IFluidHandler)handler.get();
        for (int i = 0; i < tank.getTanks(); ++i) {
            FluidStack contained = tank.getFluidInTank(i);
            int capacity = tank.getTankCapacity(i);
            float pct = (float)contained.getAmount() / (float)capacity * 100.0f;
            if (searchFluid.isEmpty()) {
                if (contained.isEmpty() || !(pct >= (float)this.minPct)) continue;
                this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
                return true;
            }
            if (contained.getFluid() != searchFluid.getFluid() || !(pct >= (float)this.minPct)) continue;
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return true;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
        return false;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("fluid_in_container.filter").ifPresent(param -> {
            if (param instanceof ConstructTaskItemStackParameter) {
                this.fluid = ((ConstructTaskItemStackParameter)param).getStack();
            }
        });
        this.getParameter("fluid_in_container.pos").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.pos = ((ConstructTaskPointParameter)param).getPoint();
            }
        });
        this.getParameter("fluid_in_container.pct").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                this.minPct = ((ConstructTaskIntegerParameter)param).getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskItemStackParameter("fluid_in_container.filter"));
        output.add(new ConstructTaskPointParameter("fluid_in_container.pos"));
        output.add(new ConstructTaskIntegerParameter("fluid_in_container.pct", 0, 100));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.CONTAINER_FLUID);
    }

    @Override
    public ConstructIsFluidInContainer copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsFluidInContainer) {
            ConstructIsFluidInContainer flOther = (ConstructIsFluidInContainer)other;
            this.pos = flOther.pos;
            this.fluid = flOther.fluid.copy();
            this.minPct = flOther.minPct;
        }
        return this;
    }

    @Override
    public ConstructIsFluidInContainer duplicate() {
        ConstructIsFluidInContainer output = new ConstructIsFluidInContainer(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.pos != null && this.pos.isValid();
    }
}


