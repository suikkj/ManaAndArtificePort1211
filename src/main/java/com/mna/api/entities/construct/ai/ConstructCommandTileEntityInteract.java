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
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.entities.construct.ai;

import com.mna.api.blocks.IRequirePlayerReference;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class ConstructCommandTileEntityInteract<T extends BlockEntity, V extends ConstructAITask<?>>
extends ConstructAITask<ConstructCommandTileEntityInteract<T, V>> {
    protected BlockPos blockPos;
    protected Direction side;
    private Class<T> teClass;
    private T _cached_te_ref;
    private Vec3 lastConstructPos;
    private float stuckPosThreshold = 0.25f;
    private int stuckCount = 0;
    private Direction[] offsetDirections = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public ConstructCommandTileEntityInteract(IConstruct<?> construct, ResourceLocation guiIcon, Class<T> tileEntityClass) {
        super(construct, guiIcon);
        this.teClass = tileEntityClass;
    }

    public void setTileEntity(BlockPos pos, Direction side) {
        this.blockPos = pos;
        this.side = side;
        this._cached_te_ref = null;
    }

    @Override
    public boolean canUse() {
        AbstractGolem c = this.getConstructAsEntity();
        if (!super.canUse()) {
            return false;
        }
        if (this.blockPos == null) {
            return false;
        }
        if (!c.level().isLoaded(this.blockPos)) {
            return false;
        }
        BlockEntity be = c.level().getBlockEntity(this.blockPos);
        if (be == null) {
            return false;
        }
        return this.teClass.isInstance(c.level().getBlockEntity(this.blockPos));
    }

    @Override
    public boolean canContinueToUse() {
        AbstractGolem c = this.getConstructAsEntity();
        if (!super.canContinueToUse()) {
            return false;
        }
        if (this.blockPos == null) {
            return false;
        }
        if (!c.level().isLoaded(this.blockPos)) {
            return false;
        }
        BlockEntity be = c.level().getBlockEntity(this.blockPos);
        if (be == null) {
            return false;
        }
        return this.teClass.isInstance(c.level().getBlockEntity(this.blockPos));
    }

    @Override
    public void start() {
        super.start();
        this._cached_te_ref = null;
        T te = this.getTileEntity();
        if (te != null && this.teClass.isInstance(te)) {
            this._cached_te_ref = te;
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.blockPos));
        }
    }

    @Nullable
    protected T getTileEntity() {
        BlockEntity te;
        if (this.blockPos == null) {
            return null;
        }
        AbstractGolem c = this.getConstructAsEntity();
        if (this._cached_te_ref == null && c.level().isLoaded(this.blockPos) && this.teClass.isInstance(te = c.level().getBlockEntity(this.blockPos))) {
            this._cached_te_ref = te;
        }
        return this._cached_te_ref;
    }

    protected void preInteract() {
        if (this.getTileEntity() instanceof IRequirePlayerReference) {
            ((IRequirePlayerReference)this.getTileEntity()).setPlayerReference(this.createFakePlayer());
        }
    }

    @Override
    public boolean doMove() {
        BlockPos movePos;
        Vec3 invPos;
        if (this.getTileEntity() == null) {
            return false;
        }
        AbstractGolem c = this.getConstructAsEntity();
        double dist = c.distanceToSqr(invPos = Vec3.atBottomCenterOf((Vec3i)(movePos = this.blockPos.offset(this.side.getNormal()))));
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
                this.blockPos = ((ConstructTaskPointParameter)param).getPosition();
                this.side = ((ConstructTaskPointParameter)param).getDirection();
            }
        });
    }

    @Override
    public ConstructCommandTileEntityInteract<T, V> copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructCommandTileEntityInteract && this.teClass == ((ConstructCommandTileEntityInteract)other).teClass) {
            this.side = ((ConstructCommandTileEntityInteract)other).side;
            this.blockPos = ((ConstructCommandTileEntityInteract)other).blockPos;
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

