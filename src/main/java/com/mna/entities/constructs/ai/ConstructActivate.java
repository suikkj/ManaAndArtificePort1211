 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructActivate
extends ConstructAITask<ConstructActivate> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[0];
    private int interactTimer = 20;
    private BlockPos interactPos = null;
    private boolean shouldSimulateSneak = false;

    public ConstructActivate(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (this.interactPos == null || !this.construct.asEntity().level().isLoaded(this.interactPos)) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.activate_missing", new Object[0]), false);
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.interactPos));
        this.setMoveTarget(this.interactPos);
        if (this.doMove()) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else {
                AbstractGolem c = this.getConstructAsEntity();
                BlockState state = c.level().getBlockState(this.interactPos);
                BlockEntity be = c.level().getBlockEntity(this.interactPos);
                if (be != null && be instanceof EldrinFumeTile) {
                    EldrinFumeTile wlTile = (EldrinFumeTile)be;
                    this.activate_wizard_lab(state, wlTile, c);
                } else {
                    this.activate_standard(state, c);
                }
            }
        }
    }

    private void activate_standard(BlockState state, AbstractGolem c) {
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.activate_success", this.translate(state), this.interactPos.getX(), this.interactPos.getY(), this.interactPos.getZ()), true);
        Player player = this.createFakePlayer();
        if (this.shouldSimulateSneak) {
            player.setPose(Pose.CROUCHING);
        }
        BlockHitResult brtr = new BlockHitResult(new Vec3((double)this.interactPos.getX() + 0.5, (double)this.interactPos.getY() + 0.5, (double)this.interactPos.getZ() + 0.5), Direction.DOWN, this.interactPos, true);
        state.getBlock().use(state, c.level(), this.interactPos, player, InteractionHand.MAIN_HAND, brtr);
        this.interactTimer = 20;
        c.swing(Math.random() < 0.5 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        player.setPose(Pose.STANDING);
        this.setSuccessCode();
    }

    private void activate_wizard_lab(BlockState state, WizardLabTile wlTile, AbstractGolem c) {
        Player player = this.createFakePlayer();
        if (this.shouldSimulateSneak) {
            player.setPose(Pose.CROUCHING);
        }
        if (wlTile.canStart(player)) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.activate_success", this.translate(state), this.interactPos.getX(), this.interactPos.getY(), this.interactPos.getZ()), true);
            wlTile.setActive(player);
            this.setSuccessCode();
        } else {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.activate_fail", this.translate(state), this.interactPos.getX(), this.interactPos.getY(), this.interactPos.getZ()), true);
            this.forceFail();
        }
        this.interactTimer = 20;
        c.swing(Math.random() < 0.5 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        player.setPose(Pose.STANDING);
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.ACTIVATE);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructActivate duplicate() {
        return new ConstructActivate(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructActivate copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructActivate) {
            ConstructActivate activate = (ConstructActivate)other;
            this.interactPos = activate.interactPos;
            this.shouldSimulateSneak = activate.shouldSimulateSneak;
        }
        return this;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("activate.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
                this.interactPos = point.getPosition();
            }
        });
        this.getParameter("activate.sneak").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                ConstructTaskBooleanParameter bool = (ConstructTaskBooleanParameter)param;
                this.shouldSimulateSneak = bool.getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("activate.point"));
        parameters.add(new ConstructTaskBooleanParameter("activate.sneak"));
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.interactPos != null;
    }
}

