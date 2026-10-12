 
package com.mna.entities.constructs.ai.conditionals;

import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.ai.conditionals.ConstructConditional;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ConstructRandomChance
extends ConstructConditional<ConstructRandomChance> {
    private int pctSuccess;

    public ConstructRandomChance(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        Level world = this.construct.asEntity().level();
        float pct = (float)this.pctSuccess / 100.0f;
        return world.random.nextFloat() < pct;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("random_chance.success_pct").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                this.pctSuccess = ((ConstructTaskIntegerParameter)param).getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskIntegerParameter("random_chance.success_pct", 0, 100, 0, 1));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.RANDOM_CHANCE);
    }

    @Override
    public ConstructRandomChance copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructRandomChance) {
            this.pctSuccess = ((ConstructRandomChance)other).pctSuccess;
        }
        return this;
    }

    @Override
    public ConstructRandomChance duplicate() {
        ConstructRandomChance output = new ConstructRandomChance(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

