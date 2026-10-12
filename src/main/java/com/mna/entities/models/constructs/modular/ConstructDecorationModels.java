/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.entities.models.constructs.modular;

import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.tools.RLoc;
import com.mna.entities.models.constructs.modular.ConstructMutexedModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class ConstructDecorationModels {
    public static final String MATERIAL_COMMON = "common";
    protected final HashMap<ConstructMaterial, List<ConstructMutexedModel>> eye_models = new HashMap();
    protected ConstructMutexedModel TUBE_LEFT;
    protected ConstructMutexedModel TUBE_RIGHT;

    public List<ResourceLocation> getModelIdentifiers() {
        List<ResourceLocation> all = this.eye_models.entrySet().stream().flatMap(e -> ((List)e.getValue()).stream()).map(m -> m.rLoc).collect(Collectors.toList());
        all.add(this.TUBE_LEFT.rLoc);
        all.add(this.TUBE_RIGHT.rLoc);
        return all;
    }

    public List<ResourceLocation> getForMoodlet(int moodlet, ConstructMaterial material) {
        List<ConstructMutexedModel> eyeModels = this.eye_models.getOrDefault(material, this.eye_models.get(ConstructMaterial.UNKNOWN));
        return eyeModels.stream().filter(m -> m.matchesMutex(moodlet)).map(m -> m.rLoc).collect(Collectors.toList());
    }

    public List<ResourceLocation> getForSlot(ConstructSlot slot, IConstructConstruction construction, int mutex) {
        ArrayList<ResourceLocation> rLocs = new ArrayList<ResourceLocation>();
        if (slot == ConstructSlot.TORSO) {
            construction.getPart(ConstructSlot.LEFT_ARM).ifPresent(c -> {
                if (c.isMutex(64)) {
                    rLocs.add(this.TUBE_LEFT.rLoc);
                }
            });
            construction.getPart(ConstructSlot.RIGHT_ARM).ifPresent(c -> {
                if (c.isMutex(64)) {
                    rLocs.add(this.TUBE_RIGHT.rLoc);
                }
            });
        }
        return rLocs;
    }

    private ResourceLocation getEyesRLoc(String mood) {
        return RLoc.create("construct/common/eyes_" + mood);
    }

    private ResourceLocation getBoneEyesRLoc(String mood) {
        return RLoc.create("construct/bone/eyes_" + mood);
    }

    public final void build() {
        this.eye_models.put(ConstructMaterial.UNKNOWN, Arrays.asList(new ConstructMutexedModel(this.getEyesRLoc("angry"), 1), new ConstructMutexedModel(this.getEyesRLoc("concern"), 2), new ConstructMutexedModel(this.getEyesRLoc("confused"), 4), new ConstructMutexedModel(this.getEyesRLoc("happy"), 8), new ConstructMutexedModel(this.getEyesRLoc("neutral"), 16), new ConstructMutexedModel(this.getEyesRLoc("unimpressed"), 32)));
        this.eye_models.put(ConstructMaterial.BONE, Arrays.asList(new ConstructMutexedModel(this.getBoneEyesRLoc("angry"), 1), new ConstructMutexedModel(this.getBoneEyesRLoc("concern"), 2), new ConstructMutexedModel(this.getBoneEyesRLoc("confused"), 4), new ConstructMutexedModel(this.getBoneEyesRLoc("happy"), 8), new ConstructMutexedModel(this.getBoneEyesRLoc("unimpressed"), 32)));
        this.TUBE_LEFT = new ConstructMutexedModel(RLoc.create("construct/common/torso_tube_l"), 8);
        this.TUBE_RIGHT = new ConstructMutexedModel(RLoc.create("construct/common/torso_tube_r"), 8);
    }
}

