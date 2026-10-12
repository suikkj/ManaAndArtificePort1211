 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructEntityAreaTask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

public class ConstructButcher
extends ConstructEntityAreaTask<Animal, ConstructButcher> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.MELEE_ATTACK};
    private int attackTimer = this.getInteractTime(ConstructCapability.MELEE_ATTACK);
    private int pairs = 1;

    public ConstructButcher(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, Animal.class);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getSelectedTarget() == null && !this.locateTarget()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.butcher_no_target", new Object[0]), false);
            this.exitCode = 1;
            return;
        }
        if (!((Animal)this.getSelectedTarget()).isAlive() || ((Animal)this.getSelectedTarget()).isDeadOrDying() || ((Animal)this.getSelectedTarget()).distanceTo((Entity)this.construct.asEntity()) > 64.0f) {
            this.setSelectedTarget(null);
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.butcher_no_target", new Object[0]), false);
            this.exitCode = 1;
            return;
        }
        this.setMoveTarget((Entity)this.getSelectedTarget());
        if (this.doMove() && this.attackButcherTarget()) {
            this.exitCode = 0;
            return;
        }
    }

    @Override
    protected boolean entityPredicate(Animal candidate) {
        return candidate != null && candidate.isAlive() && !candidate.isDeadOrDying() && !candidate.isBaby() && !candidate.isInLove();
    }

    @Override
    protected Animal selectTarget(Collection<Animal> entities) {
        HashMap<EntityType<?>, List<Animal>> locatedEntities = new HashMap<>();
        entities.forEach(e -> {
            if (!locatedEntities.containsKey(e.getType())) {
                locatedEntities.put(e.getType(), new ArrayList<Animal>(Arrays.asList(e)));
            } else {
                ((List)locatedEntities.get(e.getType())).add(e);
            }
        });
        Optional<List> target = locatedEntities.entrySet().stream().filter(e -> ((List)e.getValue()).size() > this.pairs * 2).map(e -> (List)e.getValue()).findAny();
        if (!target.isPresent()) {
            return null;
        }
        List potentials = target.get();
        Optional<Object> potential = potentials.stream().filter(a -> a.getAge() != 0).findAny();
        if (!(potential.isPresent() && potential.get() != null || (potential = potentials.stream().findAny()).isPresent() && potential.get() != null)) {
            return null;
        }
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.butcher_target", this.translate((Entity)potential.get())), false);
        return (Animal)potential.get();
    }

    private boolean attackButcherTarget() {
        if (this.attackTimer > 0) {
            if (this.attackTimer == 5) {
                this.construct.getHandWithCapability(ConstructCapability.MELEE_ATTACK).ifPresent(h -> this.construct.asEntity().swing(h));
            }
            --this.attackTimer;
            return false;
        }
        this.construct.asEntity().doHurtTarget(this.getSelectedTarget());
        this.attackTimer = this.getInteractTime(ConstructCapability.MELEE_ATTACK);
        if (!((Animal)this.getSelectedTarget()).isAlive()) {
            this.clearMoveTarget();
            this.setSelectedTarget(null);
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.butcher_attack_success", new Object[0]), false);
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.clearMoveTarget();
        this.setSelectedTarget(null);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.BUTCHER);
    }

    @Override
    public ConstructButcher duplicate() {
        return new ConstructButcher(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructButcher copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructButcher) {
            ConstructButcher otherButcher = (ConstructButcher)other;
            this.setSelectedTarget((Animal)otherButcher.getSelectedTarget());
            this.pairs = otherButcher.pairs;
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
        this.getParameter("butcher.pairs").ifPresent(param -> {
            if (param instanceof ConstructTaskIntegerParameter) {
                ConstructTaskIntegerParameter pairs = (ConstructTaskIntegerParameter)param;
                this.pairs = pairs.getValue();
            }
        });
    }

    @Override
    protected String getAreaIdentifier() {
        return "butcher.area";
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskIntegerParameter("butcher.pairs", 1, 10));
        return parameters;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}

