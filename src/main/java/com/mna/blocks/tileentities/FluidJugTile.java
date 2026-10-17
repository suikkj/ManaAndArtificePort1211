 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.blocks.artifice.FluidJugBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNAItemData;

public class FluidJugTile
extends BlockEntity
implements IFluidHandler {
    private FluidStack infiniteFluid = null;
    private FluidStack containedFluid = FluidStack.EMPTY;
    private int capacity = 16000;

    public FluidJugTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public FluidJugTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.FLUID_JUG.get(), pos, state);
    }

    private void spawnFluidFillParticles() {
        int numParticles = this.containedFluid.getAmount() == this.capacity ? 50 : 1;
        for (int i = 0; i < numParticles; ++i) {
            this.getLevel().addParticle((ParticleOptions)ParticleTypes.SPLASH, (double)this.getBlockPos().getX() + 0.5, (double)(this.getBlockPos().getY() + 1), (double)this.getBlockPos().getZ() + 0.5, ((double)this.getLevel().getRandom().nextFloat() - 0.5) * 2.0, (double)this.getLevel().getRandom().nextFloat(), ((double)this.getLevel().getRandom().nextFloat() - 0.5) * 2.0);
        }
    }

    public boolean isInfinite() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof FluidJugBlock) {
            return ((FluidJugBlock)block).is_infinite();
        }
        return false;
    }

    private FluidStack infiniteFluidType() {
        if (this.infiniteFluid == null) {
            ResourceLocation flType;
            this.infiniteFluid = FluidStack.EMPTY;
            Block block = this.getBlockState().getBlock();
            if (block instanceof FluidJugBlock && BuiltInRegistries.FLUID.containsKey(flType = ((FluidJugBlock)block).getFluidType())) {
                this.infiniteFluid = new FluidStack((Fluid)BuiltInRegistries.FLUID.get(flType), this.capacity);
            }
        }
        return this.infiniteFluid;
    }

    public int getTanks() {
        return 1;
    }

    public FluidStack getFluidInTank(int tank) {
        if (this.isInfinite()) {
            return this.infiniteFluidType();
        }
        return this.containedFluid;
    }

    public int getTankCapacity(int tank) {
        return this.capacity;
    }

    public boolean isFluidValid(int tank, FluidStack stack) {
        if (this.isInfinite()) {
            return stack.getFluid().equals(this.infiniteFluidType().getFluid());
        }
        return true;
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (this.isInfinite()) {
            return resource.getFluid().equals(this.infiniteFluidType().getFluid()) ? resource.getAmount() : 0;
        }
        if (resource.isEmpty()) {
            return 0;
        }
        if (!this.containedFluid.isEmpty() && !this.containedFluid.isFluidEqual(resource)) {
            return 0;
        }
        int amount = Math.min(resource.getAmount(), this.capacity - this.containedFluid.getAmount());
        if (action != IFluidHandler.FluidAction.SIMULATE) {
            if (this.containedFluid.isEmpty()) {
                this.containedFluid = new FluidStack(resource.getFluid(), 0);
            }
            this.containedFluid.setAmount(this.containedFluid.getAmount() + amount);
            if (!this.getLevel().isClientSide()) {
                this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return amount;
    }

    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!(this.isInfinite() || !this.containedFluid.isEmpty() && this.containedFluid.isFluidEqual(resource))) {
            return FluidStack.EMPTY;
        }
        return this.drain(resource.getAmount(), action);
    }

    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (this.isInfinite()) {
            FluidStack flOutput = this.infiniteFluidType().copy();
            flOutput.setAmount(maxDrain);
            return flOutput;
        }
        if (this.containedFluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int amount = Math.min(maxDrain, this.containedFluid.getAmount());
        FluidStack output = new FluidStack(this.containedFluid.getFluid(), amount);
        if (action != IFluidHandler.FluidAction.SIMULATE) {
            this.containedFluid.shrink(amount);
            if (this.containedFluid.getAmount() <= 0) {
                this.containedFluid = FluidStack.EMPTY;
            }
            if (!this.getLevel().isClientSide()) {
                this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return output;
    }

    public float getFillPct() {
        if (this.isInfinite()) {
            return 1.0f;
        }
        return (float)this.containedFluid.getAmount() / (float)this.capacity;
    }

    public FluidStack getContainedFluid() {
        return this.getFluidInTank(0);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.containedFluid = FluidStack.parseOptional(registries, tag);
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // [PORT 1.21.1] FluidStack.save lanca "Cannot encode empty FluidStack" (o writeToNBT do 1.20.1
        // tolerava vazio). Sem o guard, o getUpdateTag crasha o SERVIDOR ao enviar o chunk (PlayerChunkSender).
        if (!this.containedFluid.isEmpty()) {
            this.containedFluid.save(registries, tag);
        }
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        this.saveAdditional(base, registries);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        int prevAmt = this.containedFluid.getAmount();
        this.loadAdditional(tag, com.mna.api.tools.MNAItemData.provider());
        if (this.containedFluid.getAmount() != prevAmt && this.getLevel().isClientSide()) {
            this.spawnFluidFillParticles();
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        int prevAmt = this.containedFluid.getAmount();
        this.loadAdditional(tag, com.mna.api.tools.MNAItemData.provider());
        if (this.containedFluid.getAmount() > prevAmt && this.getLevel().isClientSide()) {
            this.spawnFluidFillParticles();
        }
    }
}


