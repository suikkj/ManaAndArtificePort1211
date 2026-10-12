 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructUseItemOnBlock
extends ConstructAITask<ConstructUseItemOnBlock> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private int interactTimer = 20;
    private BlockPos interactPos = null;
    private boolean leftHand = false;

    public ConstructUseItemOnBlock(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (this.interactPos == null || !this.construct.asEntity().level().isLoaded(this.interactPos)) {
            return false;
        }
        boolean handCanUse = false;
        Optional<ItemConstructPart> arm = this.construct.getConstructData().getPart(this.leftHand ? ConstructSlot.LEFT_ARM : ConstructSlot.RIGHT_ARM);
        if (arm.isPresent()) {
            ConstructCapability[] lhCaps = arm.get().getEnabledCapabilities();
            for (int i = 0; i < lhCaps.length; ++i) {
                if (lhCaps[i] != ConstructCapability.CARRY) continue;
                handCanUse = true;
                break;
            }
        }
        return handCanUse;
    }

    @Override
    public void tick() {
        super.tick();
        this.setMoveTarget(this.interactPos);
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.interactPos));
        if (this.doMove()) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else {
                AbstractGolem c = this.getConstructAsEntity();
                InteractionHand hand = this.leftHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                InteractionHand playerHand = this.leftHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                ItemStack stack = c.getItemInHand(hand);
                if (stack.isEmpty()) {
                    this.exitCode = 1;
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.use_item_on_block.empty_hand", new Object[0]), false);
                    return;
                }
                Vec3 interactCenter = Vec3.atCenterOf((Vec3i)this.interactPos);
                Player player = this.createFakePlayer();
                player.getInventory().clearContent();
                player.lookAt(EntityAnchorArgument.Anchor.FEET, interactCenter);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, interactCenter);
                player.setItemInHand(playerHand, stack.copy());
                BlockHitResult simResult = new BlockHitResult(interactCenter, Direction.UP, this.interactPos, true);
                InteractionResult result = InteractionResult.FAIL;
                if (simResult.getType() == HitResult.Type.BLOCK) {
                    BlockState state = c.level().getBlockState(this.interactPos);
                    result = stack.getItem().useOn(new UseOnContext(player, playerHand, simResult));
                    ItemStack output = player.getItemInHand(playerHand).copy();
                    if (!result.consumesAction()) {
                        InteractionResultHolder useResult = stack.getItem().use(c.level(), player, playerHand);
                        result = useResult.getResult();
                        if (result.consumesAction()) {
                            output = ((ItemStack)useResult.getObject()).copy();
                        } else {
                            result = state.useItemOn(player.getItemInHand(playerHand), c.level(), player, playerHand, simResult);
                            if (result.consumesAction()) {
                                output = player.getItemInHand(playerHand).copy();
                            }
                        }
                    }
                    if (result != InteractionResult.FAIL && result != InteractionResult.PASS) {
                        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.use_item_on_block.success", this.translate(stack), this.translate(state)), false);
                    } else {
                        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.use_item_on_block.fail", this.translate(stack), this.translate(state)), false);
                    }
                    c.setItemInHand(hand, output);
                }
                c.swing(hand);
                this.exitCode = result == InteractionResult.FAIL ? 1 : 0;
            }
        }
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.USE_ITEM_ON_BLOCK);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructUseItemOnBlock duplicate() {
        return new ConstructUseItemOnBlock(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructUseItemOnBlock copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructUseItemOnBlock) {
            ConstructUseItemOnBlock use = (ConstructUseItemOnBlock)other;
            this.interactPos = use.interactPos;
            this.leftHand = use.leftHand;
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
        this.getParameter("use_item_on_block.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                ConstructTaskPointParameter point = (ConstructTaskPointParameter)param;
                this.interactPos = point.getPosition();
            }
        });
        this.getParameter("use_item_on_block.left_hand").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                ConstructTaskBooleanParameter bool = (ConstructTaskBooleanParameter)param;
                this.leftHand = bool.getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("use_item_on_block.point"));
        parameters.add(new ConstructTaskBooleanParameter("use_item_on_block.left_hand"));
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.interactPos != null;
    }
}

