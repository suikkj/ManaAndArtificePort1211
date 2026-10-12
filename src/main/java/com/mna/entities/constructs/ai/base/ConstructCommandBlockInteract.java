/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.animal.AbstractGolem
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.constructs.ai.base;

import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class ConstructCommandBlockInteract<V extends ConstructAITask<?>>
extends ConstructAITask<ConstructCommandBlockInteract<V>> {
    protected BlockPos blockPos;
    protected Direction side;
    private Vec3 lastConstructPos;
    private float stuckPosThreshold = 0.25f;
    private int stuckCount = 0;
    private Direction[] offsetDirections = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public ConstructCommandBlockInteract(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public boolean canUse() {
        AbstractGolem c = this.getConstructAsEntity();
        return super.canUse() && this.blockPos != null && c.level().isLoaded(this.blockPos);
    }

    @Override
    public boolean canContinueToUse() {
        AbstractGolem c = this.getConstructAsEntity();
        return super.canContinueToUse() && this.blockPos != null && c.level().isLoaded(this.blockPos);
    }

    @Override
    public boolean doMove() {
        AbstractGolem c = this.getConstructAsEntity();
        Vec3 invPos = Vec3.atCenterOf((Vec3i)this.blockPos);
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, invPos);
        double dist = c.distanceToSqr(invPos);
        if (dist > 2.5) {
            if (this.moveCooldown == 0) {
                Vec3 constructPos = c.position();
                if (this.lastConstructPos != null) {
                    if (constructPos.distanceToSqr(this.lastConstructPos) < (double)(this.stuckPosThreshold * this.stuckPosThreshold)) {
                        ++this.stuckCount;
                        if (this.stuckCount >= 2) {
                            Direction dir = this.offsetDirections[(int)(Math.random() * (double)this.offsetDirections.length)];
                            c.getNavigation().stop();
                            if (c.getNavigation().moveTo(c.getX() + (double)(dir.getStepX() * 3), c.getY() + (double)(dir.getStepY() * 3), c.getZ() + (double)(dir.getStepZ() * 3), c.getAttributeValue(Attributes.MOVEMENT_SPEED))) {
                                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.move_success", invPos.x, invPos.y, invPos.z), false);
                            } else {
                                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.move_fail", invPos.x, invPos.y, invPos.z), false);
                            }
                            this.moveCooldown = 20;
                            this.stuckCount = 0;
                            return false;
                        }
                    } else {
                        this.stuckCount = 0;
                    }
                }
                this.lastConstructPos = constructPos;
                this.setPathToXYZ(invPos, 1.0f);
                this.moveCooldown = 20;
            }
        } else {
            c.getNavigation().stop();
            this.faceBlockPos(this.blockPos);
            return true;
        }
        return false;
    }

    @Override
    public void inflateParameters() {
        this.getParameter(this.getPointIdentifier()).ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
                this.blockPos = point.getPosition();
                this.side = point.getDirection();
            }
        });
    }

    @Nullable
    protected BlockState getBlockState() {
        if (this.construct.asEntity().level().isLoaded(this.blockPos)) {
            return this.construct.asEntity().level().getBlockState(this.blockPos);
        }
        return null;
    }

    @Override
    public ConstructCommandBlockInteract<V> copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructCommandBlockInteract) {
            this.side = ((ConstructCommandBlockInteract)other).side;
            this.blockPos = ((ConstructCommandBlockInteract)other).blockPos;
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
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter(this.getPointIdentifier()));
        return parameters;
    }

    protected String getPointIdentifier() {
        return "teinteract.point";
    }

    @Override
    public boolean isFullyConfigured() {
        return this.blockPos != null && this.side != null;
    }
}

