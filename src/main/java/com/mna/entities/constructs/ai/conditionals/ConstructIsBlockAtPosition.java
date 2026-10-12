 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ConstructIsBlockAtPosition
extends ConstructConditional<ConstructIsBlockAtPosition> {
    private ItemStack block = ItemStack.EMPTY;
    private DirectionalPoint pos;

    public ConstructIsBlockAtPosition(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if (this.pos == null) {
            return false;
        }
        Level world = this.construct.asEntity().level();
        if (!world.isLoaded(this.pos.getPosition())) {
            return false;
        }
        BlockState state = world.getBlockState(this.pos.getPosition());
        if (this.block.isEmpty()) {
            if (!state.isAir()) {
                this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
                return true;
            }
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
            return false;
        }
        Item item = this.block.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            if (state.getBlock() == blockItem.getBlock()) {
                this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.SUCCESS, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
                return true;
            }
        }
        this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.FAILURE, Vec3.atCenterOf((Vec3i)this.pos.getPosition()));
        return false;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("find_block.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.pos = ((ConstructTaskPointParameter)param).getPoint();
            }
        });
        this.getParameter("find_block.block").ifPresent(param -> {
            if (param instanceof ConstructTaskItemStackParameter) {
                this.block = ((ConstructTaskItemStackParameter)param).getStack();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskItemStackParameter("find_block.block"));
        output.add(new ConstructTaskPointParameter("find_block.point"));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.FIND_BLOCK);
    }

    @Override
    public ConstructIsBlockAtPosition copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsBlockAtPosition) {
            this.block = ((ConstructIsBlockAtPosition)other).block.copy();
            this.pos = ((ConstructIsBlockAtPosition)other).pos;
        }
        return this;
    }

    @Override
    public ConstructIsBlockAtPosition duplicate() {
        ConstructIsBlockAtPosition output = new ConstructIsBlockAtPosition(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.pos != null && this.pos.isValid();
    }
}

