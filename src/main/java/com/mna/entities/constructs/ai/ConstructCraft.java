 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructCommandTileEntityInteract;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.blocks.tileentities.wizard_lab.MagiciansWorkbenchTile;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mojang.datafixers.util.Pair;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

public class ConstructCraft
extends ConstructCommandTileEntityInteract<MagiciansWorkbenchTile, ConstructCraft> {
    private static final int INTERACT_TIME = 20;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY, ConstructCapability.CRAFT};
    private int interactTimer = 20;
    private ItemStack output = ItemStack.EMPTY;

    public ConstructCraft(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, MagiciansWorkbenchTile.class);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        return this.isFullyConfigured();
    }

    @Override
    public void tick() {
        super.tick();
        this.setMoveTarget(this.blockPos);
        if (this.doMove()) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else {
                ItemStack output = this.performCraft();
                if (!output.isEmpty()) {
                    this.insertOrDropItem(output);
                    this.exitCode = 0;
                } else {
                    this.exitCode = 1;
                }
                this.interactTimer = 20;
                this.construct.asEntity().swing(Math.random() < 0.5 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }
        }
    }

    private ItemStack performCraft() {
        MagiciansWorkbenchTile te = (MagiciansWorkbenchTile)this.getTileEntity();
        if (te == null) {
            return ItemStack.EMPTY;
        }
        Pair<MagiciansWorkbenchTile.AutoCraftResult, ItemStack> result = te.craftFromRemembered(this.output);
        if (result.getFirst() == MagiciansWorkbenchTile.AutoCraftResult.NO_RECIPE) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.craft_no_recipe", this.translate(this.output)), true);
            return ItemStack.EMPTY;
        }
        if (result.getFirst() == MagiciansWorkbenchTile.AutoCraftResult.MISSING_ITEMS) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.craft_missing_items", this.translate(this.output)), true);
            return ItemStack.EMPTY;
        }
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.craft_success", this.translate(this.output)), true);
        return ((ItemStack)result.getSecond()).copy();
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = 20;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.CRAFT);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public ConstructCraft copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructCraft) {
            this.output = ((ConstructCraft)other).output;
        }
        return this;
    }

    @Override
    public ConstructCraft duplicate() {
        return new ConstructCraft(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
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
        this.getParameter("craft.stack").ifPresent(param -> {
            if (param instanceof ConstructTaskItemStackParameter) {
                this.output = ((ConstructTaskItemStackParameter)param).getStack();
            }
        });
    }

    @Override
    public List<ConstructAITaskParameter> instantiateParameters() {
        super.instantiateParameters();
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskItemStackParameter("craft.stack"));
        return parameters;
    }

    @Override
    protected String getPointIdentifier() {
        return "craft.point";
    }

    @Override
    public boolean isFullyConfigured() {
        return this.blockPos != null && !this.output.isEmpty();
    }

    @Override
    public int getRequiredIntelligence() {
        return 16;
    }
}

