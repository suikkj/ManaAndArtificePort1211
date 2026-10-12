 
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructPlaceBlock
extends ConstructAITask<ConstructPlaceBlock> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private int interactTimer = 20;
    protected BlockPos blockPos;
    protected Direction side;

    public ConstructPlaceBlock(IConstruct<?> construct, ResourceLocation guiIcon) {
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
        AbstractGolem c = this.getConstructAsEntity();
        if (this.construct.getCarryingHands().length == 0) {
            if (!this.isSuccess()) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_block_hands_empty", new Object[0]));
            }
            this.exitCode = 1;
            c.goalSelector.removeGoal((Goal)this);
            return;
        }
        if (!c.level().isLoaded(this.blockPos) || !c.level().isEmptyBlock(this.blockPos)) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_block_not_clear", new Object[0]));
            this.exitCode = 1;
            return;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.blockPos));
        this.setMoveTarget(this.blockPos);
        if (this.doMove(8.0f)) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else if (this.interactTimer == 0) {
                InteractionHand[] carrying = this.construct.getCarryingHands(i -> i.getItem() instanceof BlockItem);
                if (carrying.length == 0) {
                    this.exitCode = 1;
                    return;
                }
                boolean placed = false;
                String blockTranslated = "";
                for (int i2 = 0; i2 < carrying.length; ++i2) {
                    ItemStack stack = c.getItemInHand(carrying[i2]);
                    InteractionResult result = ((BlockItem)stack.getItem()).place(new BlockPlaceContext(this.createFakePlayer(), carrying[i2], stack, new BlockHitResult(new Vec3((double)this.blockPos.getX(), (double)this.blockPos.getY(), (double)this.blockPos.getZ()), this.side, this.blockPos, true)));
                    if (result != InteractionResult.SUCCESS && result != InteractionResult.CONSUME) continue;
                    blockTranslated = this.translate(stack);
                    stack.shrink(1);
                    c.swing(carrying[i2]);
                    break;
                }
                if (!placed) {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_block_failed", new Object[0]));
                    this.exitCode = 1;
                } else {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.place_block_success", blockTranslated, this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ()));
                    this.exitCode = 0;
                }
                return;
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
        return Registries.ConstructTasks.getKey(ConstructTasks.PLACE_BLOCK);
    }

    @Override
    public ConstructPlaceBlock duplicate() {
        return new ConstructPlaceBlock(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public void inflateParameters() {
        this.getParameter("place_block.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.blockPos = ((ConstructTaskPointParameter)param).getPosition();
                this.side = ((ConstructTaskPointParameter)param).getDirection();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("place_block.point"));
        return parameters;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructPlaceBlock copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructPlaceBlock) {
            this.side = ((ConstructPlaceBlock)other).side;
            this.blockPos = ((ConstructPlaceBlock)other).blockPos;
        }
        return this;
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
    public boolean isFullyConfigured() {
        return this.blockPos != null && this.side != null;
    }
}

