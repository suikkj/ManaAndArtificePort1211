 
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
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ConstructPlaceFluidInWorld
extends ConstructAITask<ConstructPlaceFluidInWorld> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_STORE, ConstructCapability.FLUID_DISPENSE};
    private int interactTimer = 20;
    protected BlockPos blockPos;
    protected Direction side;

    public ConstructPlaceFluidInWorld(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.blockPos != null && this.side != null;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.construct.getFluidInTank(0).getAmount() < 1000) {
            this.forceFail();
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_tank_empty", new Object[0]));
            return;
        }
        BlockPos pos = this.blockPos;
        if (!this.validatePosition(pos) && !this.validatePosition(pos = pos.offset(this.side.getNormal()))) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_not_clear", new Object[0]));
            this.forceFail();
            return;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)pos));
        this.setMoveTarget(pos);
        if (this.doMove(2.0f)) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else if (this.interactTimer == 0) {
                FluidStack fluid = this.construct.getFluidInTank(0);
                String blockTranslated = this.translate(fluid);
                boolean placed = FluidUtil.tryPlaceFluid(null, (Level)this.construct.asEntity().level(), (InteractionHand)InteractionHand.MAIN_HAND, (BlockPos)pos, (IFluidHandler)this.construct, (FluidStack)fluid);
                if (!placed) {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_failed", new Object[0]));
                    this.forceFail();
                } else {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_fluid_success", blockTranslated, pos.getX(), pos.getY(), pos.getZ()));
                    this.setSuccessCode();
                }
            }
        }
    }

    private boolean validatePosition(BlockPos pos) {
        FluidState flState;
        AbstractGolem c = this.getConstructAsEntity();
        if (!c.level().isLoaded(pos)) {
            return false;
        }
        return c.level().isEmptyBlock(pos) || !(flState = c.level().getFluidState(pos)).isEmpty() && flState.getFluidType().canBePlacedInLevel((BlockAndTintGetter)c.level(), pos, this.construct.getFluidInTank(0));
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.PLACE_FLUID);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructPlaceFluidInWorld copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructPlaceFluidInWorld) {
            this.side = ((ConstructPlaceFluidInWorld)other).side;
            this.blockPos = ((ConstructPlaceFluidInWorld)other).blockPos;
        }
        return this;
    }

    @Override
    public ConstructPlaceFluidInWorld duplicate() {
        return new ConstructPlaceFluidInWorld(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        if (this.blockPos != null) {
            nbt.put("blockPos", (Tag)NbtUtils.writeBlockPos((BlockPos)this.blockPos));
        }
        if (this.side != null) {
            nbt.putInt("direction", this.side.get3DDataValue());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("blockPos")) {
            this.blockPos = NbtUtils.readBlockPos(nbt, "blockPos").orElse(null);
        }
        if (nbt.contains("direction")) {
            this.side = Direction.from3DDataValue((int)nbt.getInt("direction"));
        }
    }

    @Override
    public void inflateParameters() {
        this.getParameter("place_fluid.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.blockPos = ((ConstructTaskPointParameter)param).getPosition();
                this.side = ((ConstructTaskPointParameter)param).getDirection();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("place_fluid.point"));
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.blockPos != null && this.side != null;
    }
}

