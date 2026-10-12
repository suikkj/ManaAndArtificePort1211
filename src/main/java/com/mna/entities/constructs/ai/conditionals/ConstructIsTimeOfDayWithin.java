 
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

public class ConstructIsTimeOfDayWithin
extends ConstructConditional<ConstructIsTimeOfDayWithin> {
    private int timeOfDayMin;
    private int timeOfDayMax;

    public ConstructIsTimeOfDayWithin(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    protected boolean evaluate() {
        Level world = this.construct.asEntity().level();
        long dayTime = world.dayTime() % 24000L;
        return dayTime >= (long)this.timeOfDayMin && dayTime <= (long)this.timeOfDayMax;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("time_of_day.time_min").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                this.timeOfDayMin = ((ConstructTaskIntegerParameter)param).getValue();
            }
        });
        this.getParameter("time_of_day.time_max").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                this.timeOfDayMax = ((ConstructTaskIntegerParameter)param).getValue();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        ArrayList<ConstructAITaskParameter> output = new ArrayList<ConstructAITaskParameter>();
        output.add(new ConstructTaskIntegerParameter("time_of_day.time_min", 0, 24000, 0, 1000));
        output.add(new ConstructTaskIntegerParameter("time_of_day.time_max", 0, 24000, 0, 1000));
        return output;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.Conditions.TIME_OF_DAY);
    }

    @Override
    public ConstructIsTimeOfDayWithin copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructIsTimeOfDayWithin) {
            this.timeOfDayMin = ((ConstructIsTimeOfDayWithin)other).timeOfDayMin;
            this.timeOfDayMax = ((ConstructIsTimeOfDayWithin)other).timeOfDayMax;
        }
        return this;
    }

    @Override
    public ConstructIsTimeOfDayWithin duplicate() {
        ConstructIsTimeOfDayWithin output = new ConstructIsTimeOfDayWithin(this.construct, this.guiIcon);
        output.copyFrom((ConstructAITask)this);
        return output;
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }
}

