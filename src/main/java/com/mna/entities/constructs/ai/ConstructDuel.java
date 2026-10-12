 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructEntityAreaTask;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.animated.Construct;
import java.util.Collection;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.ItemStack;

public class ConstructDuel
extends ConstructEntityAreaTask<Construct, ConstructDuel> {
    private int targetRate = 20;
    private long targetCounter = 0L;
    private boolean lastTickTargetDefeated = false;

    public ConstructDuel(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, Construct.class);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.getConstruct().getConstructData().getBanner().isEmpty()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.duel.no_banner", new Object[0]), false);
            this.forceFail();
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        LivingEntity target = this.getConstructAsEntity().getTarget();
        if (target == null || target instanceof Construct && !((Construct)target).isDefeated() && target.isAlive()) {
            if (this.construct.getConstructData().isAnyCapabilityEnabled(ConstructCapability.MELEE_ATTACK, ConstructCapability.RANGED_ATTACK, ConstructCapability.FLUID_DISPENSE, ConstructCapability.CAST_SPELL)) {
                ++this.targetCounter;
                if (this.targetCounter >= (long)this.targetRate) {
                    this.locateTarget();
                    if (this.lastTickTargetDefeated && !this.construct.isDefeated()) {
                        this.exitCode = 0;
                        return;
                    }
                }
            }
        } else {
            if (!(c.getTarget() instanceof Construct)) {
                c.setTarget(null);
                return;
            }
            Construct targetConstruct = (Construct)c.getTarget();
            if (targetConstruct.isDefeated()) {
                c.setTarget(null);
                this.pushDuelDiagnosticWithTarget("mna.constructs.feedback.duel.takedown", c.getTarget());
                this.getConstruct().setHappy(300);
                this.lastTickTargetDefeated = true;
                return;
            }
            if (!targetConstruct.isAlive() || !targetConstruct.isDueling()) {
                c.setTarget(null);
                return;
            }
            if (this.construct.isDefeated()) {
                this.forceFail();
            } else {
                this.doAttack();
            }
        }
        this.lastTickTargetDefeated = false;
    }

    @Override
    public void start() {
        super.start();
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.duel.start", new Object[0]), false);
    }

    @Override
    protected boolean entityPredicate(Construct candidate) {
        ItemStack myBanner = this.construct.getConstructData().getBanner();
        if (candidate.isDefeated() || !candidate.isAlive()) {
            return false;
        }
        if (!candidate.isDueling()) {
            return false;
        }
        ItemStack theirBanner = candidate.getConstructData().getBanner();
        return !ItemStack.isSameItemSameComponents((ItemStack)theirBanner, (ItemStack)myBanner);
    }

    @Override
    protected Construct selectTarget(Collection<Construct> entities) {
        AbstractGolem c = this.getConstructAsEntity();
        Construct target = entities.iterator().next();
        c.setTarget((LivingEntity)target);
        this.setMoveTarget((Entity)c.getTarget());
        this.doMove();
        this.pushDuelDiagnosticWithTarget("mna.constructs.feedback.duel.target_acquire", c.getTarget());
        return target;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.DUEL);
    }

    @Override
    public ConstructDuel duplicate() {
        return (ConstructDuel)new ConstructDuel(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public void onTaskSet() {
        this.lastTickTargetDefeated = false;
        super.onTaskSet();
    }

    @Override
    protected String getAreaIdentifier() {
        return "duel.area";
    }

    private void pushDuelDiagnosticWithTarget(String key, LivingEntity target) {
        if (target == null) {
            return;
        }
        Component opponentName = target.getCustomName();
        if (opponentName == null) {
            opponentName = Component.translatable((String)"mna.constructs.feedback.target.no_name");
        }
        this.pushDiagnosticMessage(this.translate(key, opponentName), false);
    }
}

