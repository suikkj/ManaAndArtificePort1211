 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ConstructIsRaining
extends ConstructConditional<ConstructIsRaining> {
    private boolean storming;

    public ConstructIsRaining(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        Level world = this.construct.asEntity().level();
        return this.storming ? world.isThundering() : world.isRaining();
    }

    @Override
    public void inflateParameters() {
        this.getParameter("is_raining.storming").ifPresent(param -> {
            if (param instanceof ConstructTaskBooleanParameter) {
                this.storming = ((ConstructTaskBooleanParameter)param).getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskBooleanParameter("is_raining.storming", false));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.RAINING_STORMING);
    }

    @Override
    public ConstructIsRaining copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsRaining) {
            ConstructIsRaining otherRain = (ConstructIsRaining)other;
            this.storming = otherRain.storming;
        }
        return this;
    }

    @Override
    public ConstructIsRaining duplicate() {
        ConstructIsRaining output = new ConstructIsRaining(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

