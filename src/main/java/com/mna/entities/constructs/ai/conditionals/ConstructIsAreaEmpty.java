 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ConstructIsAreaEmpty
extends ConstructConditional<ConstructIsAreaEmpty> {
    private AABB area;

    public ConstructIsAreaEmpty(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        if (this.area == null) {
            return false;
        }
        Level world = this.construct.asEntity().level();
        int i = Mth.floor((double)this.area.minX);
        int j = Mth.ceil((double)this.area.maxX);
        int k = Mth.floor((double)this.area.minY);
        int l = Mth.ceil((double)this.area.maxY);
        int i1 = Mth.floor((double)this.area.minZ);
        int j1 = Mth.ceil((double)this.area.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    if (!world.isLoaded((BlockPos)blockpos$mutableblockpos)) {
                        return false;
                    }
                    BlockState blockstate = world.getBlockState((BlockPos)blockpos$mutableblockpos.set(k1, l1, i2));
                    if (blockstate.isAir()) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("area_empty.area").ifPresent(param -> {
            if (param instanceof ConstructTaskAreaParameter) {
                this.area = ((ConstructTaskAreaParameter)param).getArea();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskAreaParameter("area_empty.area"));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.AREA_EMPTY);
    }

    @Override
    public ConstructIsAreaEmpty copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsAreaEmpty) {
            this.area = ((ConstructIsAreaEmpty)other).area;
        }
        return this;
    }

    @Override
    public ConstructIsAreaEmpty duplicate() {
        ConstructIsAreaEmpty output = new ConstructIsAreaEmpty(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null;
    }
}

