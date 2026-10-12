 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.crush.CrushingRecipe;
import com.mna.tools.ContainerTools;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ConstructCrush
extends ConstructAITask<ConstructCrush> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY, ConstructCapability.SMITH};
    private int interactTimer = 20;
    private BlockPos interactPos = null;

    public ConstructCrush(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        return this.interactPos != null && this.construct.asEntity().level().isLoaded(this.interactPos);
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (this.construct.getCarryingHands().length == 0) {
            if (!this.isSuccess()) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.crush_hands_empty", new Object[0]));
            }
            this.forceFail();
            return;
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.interactPos));
        this.setMoveTarget(this.interactPos);
        if (this.doMove()) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else {
                Pair<ItemStack, ArrayList<ItemStack>> output = this.performCrush();
                if (!((ItemStack)output.getFirst()).isEmpty()) {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.crush_success", new Object[0]), true);
                    this.insertOrDropItem((ItemStack)output.getFirst());
                    if (output.getSecond() != null) {
                        ((ArrayList)output.getSecond()).forEach(is -> this.insertOrDropItem((ItemStack)is));
                    }
                    this.setSuccessCode();
                } else {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.crush_failed", new Object[0]), true);
                    this.forceFail();
                }
                this.interactTimer = 20;
                c.swing(Math.random() < 0.5 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }
        }
    }

    private Pair<ItemStack, ArrayList<ItemStack>> performCrush() {
        AbstractGolem c = this.getConstructAsEntity();
        if (!c.level().isLoaded(this.interactPos)) {
            return new Pair(ItemStack.EMPTY, null);
        }
        BlockState state = c.level().getBlockState(this.interactPos);
        if (state == null || state.getBlock() != Blocks.GRINDSTONE) {
            return new Pair(ItemStack.EMPTY, null);
        }
        InteractionHand[] carrying = this.construct.getCarryingHands();
        for (int i = 0; i < carrying.length; ++i) {
            ItemStack stack = c.getItemInHand(carrying[i]);
            if (stack.isEmpty()) continue;
            CraftingContainer inv = ContainerTools.createTemporaryContainer(stack);
            CrushingRecipe smeltRecipe = c.level().getRecipeManager().getRecipeFor((RecipeType)RecipeInit.CRUSHING_TYPE.get(), (Container)inv, c.level()).orElse(null);
            if (smeltRecipe == null) {
                return new Pair(ItemStack.EMPTY, null);
            }
            c.level().playSound(null, c.getX(), c.getY(), c.getZ(), SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.construct.getHandWithCapability(ConstructCapability.SMITH).ifPresent(h -> c.swing(h));
            ItemStack output = smeltRecipe.getResultItem().copy();
            output.setCount(stack.getCount() * smeltRecipe.getOutputQuantity());
            c.setItemInHand(carrying[i], ItemStack.EMPTY);
            return new Pair(output, smeltRecipe.rollByproducts(c.level().getRandom(), stack.getCount()));
        }
        return new Pair(ItemStack.EMPTY, null);
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.CRUSH);
    }

    @Override
    public ConstructCrush duplicate() {
        return new ConstructCrush(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructCrush copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructCrush) {
            this.interactPos = ((ConstructCrush)other).interactPos;
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
        this.getParameter("crush.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.interactPos = ((ConstructTaskPointParameter)param).getPosition();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("crush.point"));
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.interactPos != null;
    }
}

