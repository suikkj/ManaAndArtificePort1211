 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructEntityAreaTask;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ConstructBreed
extends ConstructEntityAreaTask<Animal, ConstructBreed> {
    private static final int MIN_ANIMALS = 2;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.CARRY};
    private int interactTimer = this.getInteractTime(ConstructCapability.CARRY);
    private ArrayList<Animal> currentBreedTargets = new ArrayList();
    private int currentBreedIndex = 0;
    private boolean foundOneAnimalInLove = false;

    public ConstructBreed(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, Animal.class);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getValidHeldBreedItems().size() == 0) {
            this.exitCode = 1;
            return;
        }
        if (this.currentBreedTargets.size() < (this.foundOneAnimalInLove ? 1 : 2) && !this.locateTarget()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.breed_no_target", new Object[0]), false);
            this.exitCode = 1;
            return;
        }
        Animal candidate = this.currentBreedTargets.get(this.currentBreedIndex);
        while (!this.claimEntityMutex((Entity)candidate)) {
            ++this.currentBreedIndex;
            if (this.currentBreedIndex >= this.currentBreedTargets.size()) {
                this.forceFail();
                return;
            }
            candidate = this.currentBreedTargets.get(this.currentBreedIndex);
        }
        this.setSelectedTarget(candidate);
        this.setMoveTarget((Entity)candidate);
        if (!this.hasMoveTarget()) {
            this.forceFail();
            return;
        }
        if (this.doMove() && this.breedTarget()) {
            this.releaseEntityMutex((Entity)this.getSelectedTarget());
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.breed_success", this.translate((Entity)this.currentBreedTargets.get(this.currentBreedIndex))), false);
            ++this.currentBreedIndex;
            if (this.currentBreedIndex >= this.currentBreedTargets.size()) {
                this.setSuccessCode();
            }
        }
    }

    @Override
    protected boolean entityPredicate(Animal candidate) {
        return candidate.isAlive() && !candidate.isBaby() && candidate.getAge() == 0 && this.getHeldItems().stream().anyMatch(i -> candidate.isFood(i));
    }

    @Override
    protected Animal selectTarget(Collection<Animal> entities) {
        this.currentBreedIndex = 0;
        HashMap<EntityType<?>, List<Animal>> locatedEntities = new HashMap<>();
        entities.forEach(e -> {
            if (!locatedEntities.containsKey(e.getType())) {
                locatedEntities.put(e.getType(), new ArrayList<Animal>(Arrays.asList(e)));
            } else {
                ((List)locatedEntities.get(e.getType())).add(e);
            }
        });
        Optional<List> target = locatedEntities.entrySet().stream().filter(e -> ((List)e.getValue()).size() >= 2).filter(e -> !this.getHeldBreedItem((Animal)((List)e.getValue()).get(0)).isEmpty()).map(e -> (List)e.getValue()).findAny();
        if (!target.isPresent()) {
            return null;
        }
        List potentials = target.get();
        this.foundOneAnimalInLove = potentials.stream().anyMatch(e -> e.isInLove());
        if ((potentials = potentials.stream().filter(e -> !e.isInLove()).collect(Collectors.toList())).size() < (this.foundOneAnimalInLove ? 1 : 2)) {
            return null;
        }
        this.currentBreedTargets.clear();
        this.currentBreedTargets.addAll(potentials);
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.breed_target", this.currentBreedTargets.size(), this.translate((Entity)this.currentBreedTargets.get(0))), false);
        return (Animal)potentials.get(0);
    }

    private boolean breedTarget() {
        Animal animal = this.currentBreedTargets.get(this.currentBreedIndex);
        if (animal.isInLove()) {
            this.currentBreedTargets.remove(this.currentBreedIndex);
            this.foundOneAnimalInLove = false;
            return false;
        }
        ItemStack breedStack = this.getHeldBreedItem(animal);
        if (breedStack.isEmpty()) {
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.breed_wrong_item", new Object[0]), false);
            this.exitCode = 1;
            return false;
        }
        if (this.interactTimer > 0) {
            if (this.interactTimer == 5) {
                this.construct.getHandWithCapability(ConstructCapability.CARRY).ifPresent(h -> this.construct.asEntity().swing(h));
            }
            --this.interactTimer;
            return false;
        }
        animal.setInLoveTime(600);
        breedStack.shrink(1);
        this.interactTimer = this.getInteractTime(ConstructCapability.CARRY);
        return true;
    }

    private List<Item> getValidHeldBreedItems() {
        HashMap<Item, Integer> breedItems = new HashMap<Item, Integer>();
        for (ItemStack stack : this.getHeldItems()) {
            if (breedItems.containsKey(stack.getItem())) {
                breedItems.put(stack.getItem(), (Integer)breedItems.get(stack.getItem()) + stack.getCount());
                continue;
            }
            breedItems.put(stack.getItem(), stack.getCount());
        }
        return breedItems.entrySet().stream().filter(e -> (Integer)e.getValue() >= 1).map(e -> (Item)e.getKey()).collect(Collectors.toList());
    }

    private ItemStack getHeldBreedItem(Animal animal) {
        AbstractGolem c = this.getConstructAsEntity();
        if (animal.isFood(c.getMainHandItem())) {
            return c.getMainHandItem();
        }
        if (animal.isFood(c.getOffhandItem())) {
            return c.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.clearMoveTarget();
        this.currentBreedTargets.clear();
        this.currentBreedIndex = 0;
        this.foundOneAnimalInLove = false;
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected String getAreaIdentifier() {
        return "breed.area";
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.BREED);
    }

    @Override
    public ConstructBreed duplicate() {
        return new ConstructBreed(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructBreed copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        if (other instanceof ConstructBreed) {
            this.currentBreedTargets.clear();
            this.currentBreedTargets.addAll(((ConstructBreed)other).currentBreedTargets);
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
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }
}

