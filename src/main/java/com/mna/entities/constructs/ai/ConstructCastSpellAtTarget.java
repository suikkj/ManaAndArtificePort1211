 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.math.MathUtils;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructCastSpellAtTarget
extends ConstructAITask<ConstructCastSpellAtTarget> {
    protected BlockPos blockPos;
    protected int closeEnoughDistance = 8;
    protected int channelDuration = 8;
    protected boolean mainHandCast = true;
    private boolean casting = false;
    private int activeTicks = 0;
    private static final ConstructCapability[] required_caps = new ConstructCapability[]{ConstructCapability.STORE_MANA, ConstructCapability.CAST_SPELL};

    public ConstructCastSpellAtTarget(IConstruct<?> construct, ResourceLocation guiIcon) {
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
        if (this.closeEnoughDistance < 1) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.cast.cant_see", new Object[0]));
            this.forceFail();
            return;
        }
        if (this.construct.getCastableSpells().length == 0) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.cast.no_spells", new Object[0]));
            this.forceFail();
            return;
        }
        if (this.construct.getMana() == 0.0f) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.cast.no_mana", new Object[0]));
            this.forceFail();
            return;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.blockPos));
        this.setMoveTarget(this.blockPos);
        if (this.casting) {
            ++this.activeTicks;
            boolean done = false;
            if (this.activeTicks == 1) {
                if (!this.construct.setupSpellCast(this.mainHandCast ? ConstructSlot.LEFT_ARM : ConstructSlot.RIGHT_ARM)) {
                    this.forceFail();
                    return;
                }
            } else if (this.activeTicks == 2) {
                Vec3 constructEye = this.construct.asEntity().getEyePosition();
                Vec3 target = Vec3.atCenterOf((Vec3i)this.blockPos);
                ClipContext ctx = new ClipContext(constructEye, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null);
                BlockHitResult hitResult = this.construct.asEntity().level().clip(ctx);
                Direction face = Direction.UP;
                if (hitResult.getType() != HitResult.Type.MISS) {
                    face = hitResult.getDirection();
                }
                this.construct.startSpellCast(new SpellTarget(this.blockPos, face));
            } else if (this.activeTicks < this.channelDuration) {
                done = !this.construct.tickSpellCast();
            } else {
                this.construct.resetSpellCast();
                done = true;
            }
            if (done) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.cast.success", new Object[0]));
                this.exitCode = 0;
                return;
            }
        } else if (this.doMove(this.closeEnoughDistance)) {
            Vec3 target = Vec3.atCenterOf((Vec3i)this.blockPos);
            Vec3 constructEye = this.construct.asEntity().getEyePosition();
            Vec3 delta = constructEye.subtract(target).normalize();
            target = target.add(delta);
            ClipContext ctx = new ClipContext(constructEye, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null);
            BlockHitResult hitResult = this.construct.asEntity().level().clip(ctx);
            if (hitResult.getType() != HitResult.Type.MISS) {
                --this.closeEnoughDistance;
                return;
            }
            if (MathUtils.rotateEntityLookTowards(EntityAnchorArgument.Anchor.FEET, (LivingEntity)this.construct.asEntity(), target, 12.0f)) {
                this.casting = true;
                this.activeTicks = 0;
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.CAST_SPELL_AT_TARGET);
    }

    @Override
    public ConstructCastSpellAtTarget duplicate() {
        return new ConstructCastSpellAtTarget(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCastSpellAtTarget copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructCastSpellAtTarget) {
            ConstructCastSpellAtTarget otherCast = (ConstructCastSpellAtTarget)other;
            this.blockPos = otherCast.blockPos;
            this.closeEnoughDistance = otherCast.closeEnoughDistance;
            this.channelDuration = otherCast.channelDuration;
            this.mainHandCast = otherCast.mainHandCast;
        }
        return this;
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        if (this.blockPos != null) {
            nbt.put("blockPos", (Tag)NbtUtils.writeBlockPos((BlockPos)this.blockPos));
        }
        nbt.putInt("closeEnoughDistance", this.closeEnoughDistance);
        nbt.putInt("channelTime", this.channelDuration);
        nbt.putBoolean("mainHandCast", this.mainHandCast);
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("blockPos")) {
            this.blockPos = NbtUtils.readBlockPos(nbt, "blockPos").orElse(null);
        }
        if (nbt.contains("closeEnoughDistance")) {
            this.closeEnoughDistance = nbt.getInt("closeEnoughDistance");
        }
        if (nbt.contains("channelTime")) {
            this.channelDuration = nbt.getInt("channelTime");
        }
        if (nbt.contains("mainHandCast")) {
            this.mainHandCast = nbt.getBoolean("mainHandCast");
        }
    }

    @Override
    public void inflateParameters() {
        this.getParameter("castAtTarget.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter target = (ConstructTaskPointParameter)param;
                this.blockPos = target.getPosition();
            }
        });
        this.getParameter("castAtTarget.minDistance").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                ConstructTaskIntegerParameter minDist = (ConstructTaskIntegerParameter)param;
                this.closeEnoughDistance = minDist.getValue();
            }
        });
        this.getParameter("castAtTarget.channelTime").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                ConstructTaskIntegerParameter channelTime = (ConstructTaskIntegerParameter)param;
                this.channelDuration = channelTime.getValue() * 20;
            }
        });
        this.getParameter("castAtTarget.mainHand").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                ConstructTaskBooleanParameter mainHand = (ConstructTaskBooleanParameter)param;
                this.mainHandCast = mainHand.getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("castAtTarget.point"));
        parameters.add(new ConstructTaskIntegerParameter("castAtTarget.minDistance", 1, 32));
        parameters.add(new ConstructTaskBooleanParameter("castAtTarget.mainHand", true));
        parameters.add(new ConstructTaskIntegerParameter("castAtTarget.channelTime", 1, 10));
        return parameters;
    }

    public void setDesiredLocation(BlockPos pos) {
        this.blockPos = pos;
        this.getParameter("castAtTarget.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
                point.setPoint(new DirectionalPoint(pos, Direction.UP, "", null));
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return this.blockPos != null;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return required_caps;
    }
}

