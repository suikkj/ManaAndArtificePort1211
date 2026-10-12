 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructBlockAreaTask;
import com.mna.api.tools.MATags;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ConstructPlant
extends ConstructBlockAreaTask<ConstructPlant> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.PLANT, ConstructCapability.CARRY};
    ResourceLocation plantBlockTag;

    public ConstructPlant(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        this.plantBlockTag = this.getHeldItemPlantBlockTag();
        if (this.plantBlockTag == null) {
            this.exitCode = 1;
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.plant_empty_hands", new Object[0]), false);
            return;
        }
        if (this.currentTarget == null) {
            this.findBlockTarget();
            return;
        }
        if (!MATags.isBlockIn(c.level().getBlockState(this.currentTarget).getBlock(), this.plantBlockTag)) {
            this.currentTarget = null;
            this.findBlockTarget();
            return;
        }
        if (this.moveToCurrentTarget()) {
            if (c.level().isEmptyBlock(this.currentTarget.above())) {
                InteractionHand[] carrying;
                for (InteractionHand hand : carrying = this.getHeldItem(this.plantBlockTag)) {
                    ItemStack stack = c.getItemInHand(hand);
                    if (!(stack.getItem() instanceof BlockItem)) {
                        this.exitCode = 1;
                        this.currentTarget = null;
                        return;
                    }
                    BlockState plant = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                    if (plant == null) continue;
                    String translatedStack = this.translate(stack);
                    stack.shrink(1);
                    c.level().setBlockAndUpdate(this.currentTarget.above(), plant);
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.planting", translatedStack, this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ()));
                    this.exitCode = 0;
                    this.currentTarget = null;
                    return;
                }
            } else {
                this.removeBlockTarget(this.currentTarget);
                this.currentTarget = null;
            }
        }
    }

    private ResourceLocation getHeldItemPlantBlockTag() {
        if (this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.SOUL_SAND_PLANTABLE)).length != 0) {
            return MATags.Blocks.CONSTRUCT_PLANTABLE_SOUL_SAND;
        }
        if (this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.SAND_PLANTABLE)).length != 0) {
            return MATags.Blocks.CONSTRUCT_PLANTABLE_SAND;
        }
        if (this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.GRASS_PLANTABLE)).length != 0) {
            return MATags.Blocks.CONSTRUCT_PLANTABLE_GRASS;
        }
        if (this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.FARMLAND_PLANTABLE)).length != 0) {
            return MATags.Blocks.CONSTRUCT_PLANTABLE_FARMLAND;
        }
        return null;
    }

    private InteractionHand[] getHeldItem(ResourceLocation targetTag) {
        if (targetTag.equals(MATags.Blocks.CONSTRUCT_PLANTABLE_FARMLAND)) {
            return this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.FARMLAND_PLANTABLE));
        }
        if (targetTag.equals(MATags.Blocks.CONSTRUCT_PLANTABLE_GRASS)) {
            return this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.GRASS_PLANTABLE));
        }
        if (targetTag.equals(MATags.Blocks.CONSTRUCT_PLANTABLE_SAND)) {
            return this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.SAND_PLANTABLE));
        }
        if (targetTag.equals(MATags.Blocks.CONSTRUCT_PLANTABLE_SOUL_SAND)) {
            return this.construct.getCarryingHands(i -> MATags.isItemEqual(i, MATags.Items.Constructs.SOUL_SAND_PLANTABLE));
        }
        return new InteractionHand[0];
    }

    @Override
    protected ConstructCapability getSwingArmCap() {
        return ConstructCapability.PLANT;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            this.forceFail();
            return false;
        }
        if (this.getHeldItemPlantBlockTag() == null) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.plant_empty_hands", new Object[0]), false);
            this.exitCode = -1;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    protected int getInteractTimer() {
        return this.getInteractTime(ConstructCapability.PLANT);
    }

    @Override
    protected boolean isValidBlock(BlockState state, BlockPos pos) {
        if (!this.construct.asEntity().level().isEmptyBlock(pos.above())) {
            return false;
        }
        if (this.plantBlockTag != null) {
            ResourceLocation blockTag = this.getHeldItemPlantBlockTag();
            return MATags.isBlockIn(state.getBlock(), blockTag);
        }
        return MATags.isBlockIn(state.getBlock(), MATags.Blocks.CONSTRUCT_PLANTABLE_FARMLAND) || MATags.isBlockIn(state.getBlock(), MATags.Blocks.CONSTRUCT_PLANTABLE_SAND) || MATags.isBlockIn(state.getBlock(), MATags.Blocks.CONSTRUCT_PLANTABLE_GRASS) || MATags.isBlockIn(state.getBlock(), MATags.Blocks.CONSTRUCT_PLANTABLE_SOUL_SAND);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.interactTimer = this.getInteractTime(ConstructCapability.CARRY);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.PLANT);
    }

    @Override
    public ConstructPlant duplicate() {
        return new ConstructPlant(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructPlant copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
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
    protected String getAreaIdentifier() {
        return "plant.area";
    }
}

