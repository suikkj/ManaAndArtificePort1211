/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.entities.models.constructs.modular;

import com.mna.api.entities.construct.ConstructSlot;
import com.mna.entities.models.constructs.modular.ConstructMutexedModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public abstract class ConstructModelCollection {
    public static final Predicate<String> ALL = s -> true;
    public static final Predicate<String> BASIC_ONLY = s -> s == "basic";
    private final HashMap<String, Integer> modelDefs;
    protected final String materialIdentifier;
    protected Predicate<String> part_type_predicate = ALL;
    protected final ArrayList<ConstructMutexedModel> models;

    public ConstructModelCollection(String materialIdentifier) {
        this.materialIdentifier = materialIdentifier;
        this.models = new ArrayList();
        this.modelDefs = new HashMap();
    }

    public void setPartTypePredicate(Predicate<String> predicate) {
        this.part_type_predicate = predicate;
    }

    public List<ResourceLocation> getForMutex(int mutex) {
        return this.models.stream().filter(m -> m.matchesMutex(mutex)).map(m -> m.rLoc).collect(Collectors.toList());
    }

    public List<ResourceLocation> getModelIdentifiers() {
        return this.models.stream().map(m -> m.rLoc).collect(Collectors.toList());
    }

    protected final void defineModel(String part_type, int mutex) {
        this.modelDefs.put(part_type, mutex);
    }

    public final void build() {
        this.modelDefs.entrySet().stream().filter(e -> this.part_type_predicate.test((String)e.getKey())).forEach(e -> this.models.add(new ConstructMutexedModel(this.getRLoc((String)e.getKey()), (Integer)e.getValue())));
    }

    public abstract ConstructSlot getSlot();

    protected abstract ResourceLocation getRLoc(String var1);
}

