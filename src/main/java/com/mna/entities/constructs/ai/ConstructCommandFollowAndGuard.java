 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.SummonUtils;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;

public class ConstructCommandFollowAndGuard
extends ConstructAITask<ConstructCommandFollowAndGuard> {
    private int leashRange = 32;
    private int targetRate = 20;
    private long targetCounter = 0L;
    private boolean guard = true;
    private int followTarget = -1;
    private LivingEntity cachedFollowTarget;

    public ConstructCommandFollowAndGuard(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.getFollowTarget() != null;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity toFollow = this.getFollowTarget();
        if (toFollow != null && toFollow.isAlive()) {
            AbstractGolem c = this.getConstructAsEntity();
            if (!c.isVehicle()) {
                if (this.guard && this.construct.getConstructData().isAnyCapabilityEnabled(ConstructCapability.MELEE_ATTACK, ConstructCapability.RANGED_ATTACK, ConstructCapability.FLUID_DISPENSE, ConstructCapability.CAST_SPELL)) {
                    ++this.targetCounter;
                    if (this.targetCounter >= (long)this.targetRate) {
                        this.targetNearby();
                    }
                }
            } else {
                c.setTarget(null);
            }
            double followTargetDist = c.distanceToSqr(toFollow.position());
            this.checkLeashRange(followTargetDist);
            if (c.getTarget() == null) {
                this.doFollow(followTargetDist);
            } else if (this.guard && SummonUtils.isTargetFriendly((Entity)c.getTarget(), toFollow) && !this.construct.validateFriendlyTarget(c.getTarget())) {
                c.setTarget(null);
            } else if (!c.isVehicle()) {
                this.doAttackTarget();
            }
        }
    }

    public void setFollowTarget(LivingEntity target) {
        this.followTarget = target != null ? target.getId() : -1;
    }

    public void setShouldGuard(boolean guard) {
        this.guard = guard;
    }

    private void checkLeashRange(double followTargetDist) {
        if (followTargetDist > (double)(this.leashRange * this.leashRange)) {
            AbstractGolem c = this.getConstructAsEntity();
            if (c.getTarget() != null) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.leashed", this.translate((Entity)c.getTarget())));
            }
            c.setTarget(null);
        }
    }

    private void doFollow(double followTargetDist) {
        AbstractGolem c = this.getConstructAsEntity();
        if (c.isVehicle()) {
            return;
        }
        if (followTargetDist < 4.0) {
            c.getNavigation().stop();
        } else {
            this.setMoveTarget((Entity)this.getFollowTarget());
            this.doMove();
        }
    }

    private void doAttackTarget() {
        AbstractGolem c = this.getConstructAsEntity();
        if (!c.getTarget().isAlive()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.victory", this.translate((Entity)c.getTarget())));
            c.setTarget(null);
            return;
        }
        this.doAttack();
    }

    @Override
    public boolean allowSteeringMountedConstructsDuringTask() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        if (this.guard) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.follow_guard", new Object[0]), false);
        } else {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.follow", new Object[0]), false);
        }
    }

    private void targetNearby() {
        this.targetCounter = 0L;
        AbstractGolem c = this.getConstructAsEntity();
        final LivingEntity toFollow = this.getFollowTarget();
        if (toFollow == null || !toFollow.isAlive() || c.getTarget() != null && c.getTarget().isAlive()) {
            return;
        }
        List<LivingEntity> entities = this.construct.getValidAttackTargets(this.cachedFollowTarget);
        if (entities.size() == 0) {
            return;
        }
        if (entities.size() > 1) {
            try {
                entities.sort(new Comparator<LivingEntity>(){

                    @Override
                    public int compare(LivingEntity o1, LivingEntity o2) {
                        Double o1Dist = o1.distanceToSqr((Entity)toFollow);
                        Double o2Dist = o2.distanceToSqr((Entity)toFollow);
                        return o1Dist.compareTo(o2Dist);
                    }
                });
            }
            catch (Exception ex) {
                return;
            }
        }
        c.setTarget(entities.get(0));
        if (c.getTarget() != null) {
            this.setMoveTarget((Entity)c.getTarget());
            this.doMove();
            if (c.getTarget() != this.construct.getOwner()) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.attack", this.translate(c.getTarget().getType().getDescriptionId(), new Object[0])), false);
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.FOLLOW_DEFEND);
    }

    @Override
    public ConstructCommandFollowAndGuard duplicate() {
        return new ConstructCommandFollowAndGuard(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCommandFollowAndGuard copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructCommandFollowAndGuard) {
            this.followTarget = ((ConstructCommandFollowAndGuard)other).followTarget;
            this.guard = ((ConstructCommandFollowAndGuard)other).guard;
        }
        return this;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("guard")) {
            this.guard = nbt.getBoolean("guard");
        }
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        nbt.putBoolean("guardTarget", this.guard);
        return nbt;
    }

    @Nullable
    public LivingEntity getFollowTarget() {
        if (this.cachedFollowTarget == null) {
            if (this.followTarget > -1) {
                Entity entity = this.construct.asEntity().level().getEntity(this.followTarget);
                if (entity != null && entity instanceof LivingEntity) {
                    LivingEntity living;
                    this.cachedFollowTarget = living = (LivingEntity)entity;
                }
            } else {
                this.cachedFollowTarget = this.owner;
            }
        }
        return this.cachedFollowTarget;
    }

    @Override
    public void inflateParameters() {
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

