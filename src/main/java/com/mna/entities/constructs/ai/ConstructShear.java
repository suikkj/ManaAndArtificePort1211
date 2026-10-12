 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructBlockAreaTask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.ShearHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IForgeShearable;

public class ConstructShear
extends ConstructBlockAreaTask<ConstructShear> {
    private static final String KEY_CHICKEN_SHEAR_TIME = "last_shear_time";
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.SHEAR};
    private boolean includeBlocks = false;
    private List<Animal> currentShearTargets;
    private int currentShearIndex = 0;
    private ItemStack shearsStack = new ItemStack((ItemLike)Items.SHEARS);

    public ConstructShear(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.currentShearTargets = new ArrayList<Animal>();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.interactTimer > 0) {
            --this.interactTimer;
            return;
        }
        if (!this.hasShearTarget() && !this.locateShearTarget()) {
            if (!this.searchingSurroundings && this.knownTargets.size() == 0) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_no_target", new Object[0]), false);
                this.forceFail();
            }
            return;
        }
        if (this.currentTarget == null) {
            this.locateShearTarget();
        }
        if (!this.includeBlocks && this.currentShearIndex >= this.currentShearTargets.size()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_no_target", new Object[0]), false);
            this.forceFail();
            return;
        }
        this.setMoveTarget();
        if (!this.hasMoveTarget()) {
            this.forceFail();
            return;
        }
        if (this.doMove() && this.shearTarget()) {
            if (this.currentShearIndex < this.currentShearTargets.size()) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_success", this.translate((Entity)this.currentShearTargets.get(this.currentShearIndex))), false);
            }
            if (this.includeBlocks) {
                this.removeBlockTarget(this.currentTarget);
                this.currentTarget = null;
            }
            ++this.currentShearIndex;
            if (this.currentShearIndex >= this.currentShearTargets.size()) {
                this.setSuccessCode();
                return;
            }
        }
    }

    private boolean hasShearTarget() {
        if (this.includeBlocks) {
            return this.knownTargets.size() > 0;
        }
        return this.currentShearTargets.size() > 0;
    }

    private boolean locateShearTarget() {
        if (this.includeBlocks) {
            return this.findBlockTarget();
        }
        return this.locateShearEntity();
    }

    private void setMoveTarget() {
        if (this.includeBlocks) {
            this.setMoveTarget(this.currentTarget);
        } else {
            Animal candidate = this.currentShearTargets.get(this.currentShearIndex);
            while (!this.claimEntityMutex((Entity)candidate)) {
                ++this.currentShearIndex;
                if (this.currentShearIndex >= this.currentShearTargets.size()) {
                    this.clearMoveTarget();
                    return;
                }
                candidate = this.currentShearTargets.get(this.currentShearIndex);
            }
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, candidate.getId());
            this.setMoveTarget((Entity)candidate);
        }
    }

    private boolean shearTarget() {
        if (this.includeBlocks) {
            return this.shearBlock();
        }
        return this.shearEntity();
    }

    private boolean locateShearEntity() {
        AbstractGolem c = this.getConstructAsEntity();
        this.currentShearIndex = 0;
        this.currentShearTargets = c.level().getEntitiesOfClass(Animal.class, this.area, e -> e.isAlive() && (e instanceof IForgeShearable && ((IForgeShearable)e).isShearable(this.shearsStack, c.level(), e.blockPosition()) || e instanceof Chicken && this.canChickenBeSheared((Chicken)e)));
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_target", this.currentShearTargets.size()), false);
        return this.currentShearTargets.size() > 0;
    }

    private boolean shearBlock() {
        AbstractGolem c = this.getConstructAsEntity();
        List<ItemStack> loot = ShearHelper.shearBlock((ServerLevel)this.construct.asEntity().level(), this.getMoveBlockTarget(), Direction.UP, this.createFakePlayer());
        if (loot == null) {
            BlockState state = c.level().getBlockState(this.getMoveBlockTarget());
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_not_shearable", this.translate(state)), false);
            return false;
        }
        Random rand = new Random();
        loot.forEach(d -> {
            ItemEntity ent = c.spawnAtLocation(d, 1.0f);
            ent.setDeltaMovement(ent.getDeltaMovement().add((double)((rand.nextFloat() - rand.nextFloat()) * 0.1f), (double)(rand.nextFloat() * 0.05f), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1f)));
        });
        this.construct.getHandWithCapability(ConstructCapability.SHEAR).ifPresent(h -> c.swing(h));
        c.level().playSound(null, c.getX(), c.getY(), c.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0f, (float)((int)(0.9 + Math.random() * 0.2)));
        this.interactTimer = this.getInteractTime(ConstructCapability.SHEAR);
        return true;
    }

     
    private boolean shearEntity() {
        if (this.interactTimer > 0) {
            --this.interactTimer;
            return false;
        }
        Animal animal = this.currentShearTargets.get(this.currentShearIndex);
        List<ItemStack> loot = null;
        AbstractGolem c = this.getConstructAsEntity();
        if (animal instanceof Chicken) {
            Chicken chicken = (Chicken)animal;
            if (!this.canChickenBeSheared(chicken)) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_not_shearable", this.translate((Entity)animal)), false);
                return false;
            }
            this.setNextShearTime(chicken);
            loot = new ArrayList<ItemStack>();
            loot.add(new ItemStack((ItemLike)Items.FEATHER, (int)(Math.random() * 3.0 + 1.0)));
            if (this.construct.getIntelligence() < 16) {
                chicken.hurt(c.damageSources().mobAttack((LivingEntity)c), 1.0f);
            }
        } else {
            loot = ShearHelper.shearEntity((ServerLevel)c.level(), this.createFakePlayer(), (Entity)animal, InteractionHand.MAIN_HAND);
        }
        if (loot == null) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.shear_not_shearable", this.translate((Entity)animal)), false);
            return false;
        }
        Random rand = new Random();
        loot.forEach(d -> {
            ItemEntity ent = c.spawnAtLocation(d, 1.0f);
            ent.setDeltaMovement(ent.getDeltaMovement().add((double)((rand.nextFloat() - rand.nextFloat()) * 0.1f), (double)(rand.nextFloat() * 0.05f), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1f)));
        });
        this.construct.getHandWithCapability(ConstructCapability.SHEAR).ifPresent(h -> c.swing(h));
        c.level().playSound(null, c.getX(), c.getY(), c.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0f, (float)((int)(0.9 + Math.random() * 0.2)));
        this.interactTimer = this.getInteractTime(ConstructCapability.SHEAR);
        return true;
    }

    private boolean canChickenBeSheared(Chicken chicken) {
        return !chicken.getPersistentData().contains(KEY_CHICKEN_SHEAR_TIME) || this.construct.asEntity().level().getGameTime() >= chicken.getPersistentData().getLong(KEY_CHICKEN_SHEAR_TIME);
    }

    private void setNextShearTime(Chicken chicken) {
        chicken.getPersistentData().putLong(KEY_CHICKEN_SHEAR_TIME, this.construct.asEntity().level().getGameTime() + 12000L + (long)((int)(Math.random() * 12000.0)));
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.clearMoveTarget();
        this.currentShearTargets.clear();
        this.currentShearIndex = 0;
    }

    @Override
    protected boolean isValidBlock(BlockState state, BlockPos pos) {
        return ShearHelper.canBlockBeSheared(this.construct.asEntity().level(), state, pos);
    }

    @Override
    protected int getInteractTimer() {
        return this.getInteractTime(ConstructCapability.SHEAR);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.SHEAR);
    }

    @Override
    public ConstructShear duplicate() {
        return new ConstructShear(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructShear copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructShear) {
            this.currentShearTargets.clear();
            this.currentShearTargets.addAll(((ConstructShear)other).currentShearTargets);
            this.includeBlocks = ((ConstructShear)other).includeBlocks;
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
        super.inflateParameters();
        this.getParameter("shear.boolean").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                this.includeBlocks = ((ConstructTaskBooleanParameter)param).getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskBooleanParameter("shear.boolean"));
        return parameters;
    }

    @Override
    protected String getAreaIdentifier() {
        return "shear.area";
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}


