 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructGuard
extends ConstructAITask<ConstructGuard> {
    private BlockPos guard_point;
    private Direction guard_direction;
    private int guard_time = 100;
    private static final int LEASH_RANGE = 1024;
    private int targetRate = 20;
    private int guard_counter = 0;
    private long targetCounter = 0L;
    private boolean leashReturning = false;

    public ConstructGuard(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (!this.claimBlockMutex(this.guard_point)) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.guard_block_claimed", new Object[0]));
            this.forceFail();
            return;
        }
        if (this.construct.getConstructData().isAnyCapabilityEnabled(ConstructCapability.MELEE_ATTACK, ConstructCapability.RANGED_ATTACK) && !this.leashReturning && this.targetCounter++ >= (long)this.targetRate) {
            this.targetNearby();
        }
        if (c.getTarget() == null || this.leashReturning) {
            this.doGuard();
        } else {
            this.doAttackTarget();
        }
    }

    private void doGuard() {
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.guard_point));
        this.setMoveTarget(this.guard_point);
        if (this.doMove(1.25f)) {
            AbstractGolem c = this.construct.asEntity();
            Vec3 desiredPoint = Vec3.atBottomCenterOf((Vec3i)this.guard_point);
            BlockHitResult bhr = c.level().clip(new ClipContext(Vec3.atBottomCenterOf((Vec3i)this.guard_point.above(Math.min((int)c.getStepHeight(), 1) + 1)), desiredPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)c));
            Vec3 guardPos = bhr != null && bhr.getType() != HitResult.Type.MISS ? bhr.getLocation() : desiredPoint;
            this.construct.asEntity().setPos(guardPos);
            this.leashReturning = false;
            Vec3 lookPos = Vec3.atCenterOf((Vec3i)this.guard_point.offset(this.guard_direction.getNormal().multiply(5)));
            this.getConstructAsEntity().lookAt(EntityAnchorArgument.Anchor.FEET, lookPos);
            this.construct.getDiagnostics().setMovePos(lookPos);
            ++this.guard_counter;
            if (this.guard_counter >= this.guard_time) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.patrol_complete", new Object[0]));
                this.setSuccessCode();
            }
        }
    }

    private void doAttackTarget() {
        AbstractGolem c = this.getConstructAsEntity();
        if (!c.getTarget().isAlive()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.victory", this.translate((Entity)c.getTarget())));
            c.setTarget(null);
            this.setMoveTarget(this.guard_point);
            return;
        }
        if (this.isOutsideLeashRange()) {
            this.getConstructAsEntity().setTarget(null);
            this.setMoveTarget(this.guard_point);
            this.leashReturning = true;
            return;
        }
        this.doAttack();
    }

    private boolean isOutsideLeashRange() {
        int leash_range = this.construct.getConstructData().calculatePerception();
        if (this.getConstructAsEntity().distanceToSqr(Vec3.atCenterOf((Vec3i)this.guard_point)) > (double)(leash_range * leash_range)) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.leash_distance_exceeded", new Object[0]));
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.leashReturning = false;
        this.guard_counter = 0;
    }

    @Override
    public void start() {
        super.start();
        this.leashReturning = false;
        this.guard_counter = 0;
    }

    private void targetNearby() {
        this.targetCounter = 0L;
        AbstractGolem c = this.getConstructAsEntity();
        if (this.owner == null || !this.owner.isAlive() || c.getTarget() != null && c.getTarget().isAlive()) {
            return;
        }
        List<LivingEntity> entities = this.construct.getValidAttackTargets();
        if (entities.size() == 0) {
            return;
        }
        entities.sort(new Comparator<LivingEntity>(){

            @Override
            public int compare(LivingEntity o1, LivingEntity o2) {
                Double o1Dist = o1.distanceToSqr((Entity)ConstructGuard.this.owner);
                Double o2Dist = o2.distanceToSqr((Entity)ConstructGuard.this.owner);
                return o1Dist.compareTo(o2Dist);
            }
        });
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, entities.get(0).getId());
        c.setTarget(entities.get(0));
        this.setMoveTarget((Entity)c.getTarget());
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.attack", this.translate(c.getTarget().getType().getDescriptionId(), new Object[0])), false);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.GUARD);
    }

    @Override
    public ConstructGuard copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructGuard) {
            ConstructGuard otherGuard = (ConstructGuard)other;
            this.guard_point = otherGuard.guard_point;
            this.guard_direction = otherGuard.guard_direction;
            this.guard_time = otherGuard.guard_time;
        }
        return this;
    }

    @Override
    public ConstructGuard duplicate() {
        return new ConstructGuard(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return new CompoundTag();
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("guard.point"));
        parameters.add(new ConstructTaskIntegerParameter("guard.wait_time", 1, 30));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("guard.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
                this.guard_point = point.getPosition();
                this.guard_direction = point.getDirection();
                if (this.guard_direction == Direction.UP || this.guard_direction == Direction.DOWN) {
                    this.guard_direction = Direction.NORTH;
                }
            }
        });
        this.getParameter("guard.wait_time").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                ConstructTaskIntegerParameter wait = (ConstructTaskIntegerParameter)param;
                this.guard_time = wait.getValue() * 20;
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return this.guard_point != null && this.guard_direction != null && this.guard_time > 0;
    }
}

