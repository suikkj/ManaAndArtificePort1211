 
package com.mna.items.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class InfiniteFluidCapabilityHandler
implements IFluidHandlerItem,
ICapabilityProvider {
    private final Fluid contained;
    private final int CAPACITY = 8000;
    public static final String FLUID_NBT_KEY = "Fluid";
    @Nonnull
    protected ItemStack container;

    public InfiniteFluidCapabilityHandler(Fluid contained, @Nonnull ItemStack container) {
        this.container = container;
        this.contained = contained;
    }

    @Nonnull
    public ItemStack getContainer() {
        return this.container;
    }

    @Nonnull
    public FluidStack getFluid() {
        return new FluidStack(this.contained, 8000);
    }

    protected void setFluid(FluidStack fluid) {
    }

    public int getTanks() {
        return 1;
    }

    @Nonnull
    public FluidStack getFluidInTank(int tank) {
        return this.getFluid();
    }

    public int getTankCapacity(int tank) {
        return 8000;
    }

    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return stack.getFluid().equals(this.contained);
    }

    public int fill(@Nonnull FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isFluidEqual(this.getFluid())) {
            return resource.getAmount();
        }
        return 0;
    }

    @Nonnull
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (this.container.getCount() != 1 || resource.isEmpty() || !resource.isFluidEqual(this.getFluid())) {
            return FluidStack.EMPTY;
        }
        return this.drain(resource.getAmount(), action);
    }

    @Nonnull
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return new FluidStack(this.contained, maxDrain);
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.isFluidEqual(this.getFluid());
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return fluid.isFluidEqual(this.getFluid());
    }

    protected void setContainerToEmpty() {
        com.mna.api.tools.MNAItemData.remove(this.container, FLUID_NBT_KEY);
    }

}


