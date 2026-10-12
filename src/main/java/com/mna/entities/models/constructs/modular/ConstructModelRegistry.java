/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.client.event.ModelEvent$RegisterAdditional
 *  net.neoforged.bus.api.SubscribeEvent
 */
package com.mna.entities.models.constructs.modular;

import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.IConstructModelRegistrationHelper;
import com.mna.api.entities.construct.ModelsTypes;
import com.mna.entities.models.constructs.modular.ConstructMaterialModel;
import com.mna.entities.models.constructs.modular.ConstructModelCollection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ConstructModelRegistry
implements IConstructModelRegistrationHelper {
    public static final String MODEL_BASE = "construct/";
    private static HashMap<ConstructMaterial, ConstructMaterialModel> material_model = new HashMap();
    private static final ConstructMaterialModel STONE = new ConstructMaterialModel("stone").build();
    private static final ConstructMaterialModel WOOD = new ConstructMaterialModel("wood").build();
    private static final ConstructMaterialModel BONE = new ConstructMaterialModel("bone").build();
    private static final ConstructMaterialModel IRON = new ConstructMaterialModel("iron").build();
    private static final ConstructMaterialModel GOLD = new ConstructMaterialModel("gold").build();
    private static final ConstructMaterialModel DIAMOND = new ConstructMaterialModel("diamond").build();
    private static final ConstructMaterialModel OBSIDIAN = new ConstructMaterialModel("obsidian").build();
    private static final ConstructMaterialModel WICKERWOOD = new ConstructMaterialModel("wickerwood").setPredicate(ModelsTypes.HEAD, ConstructModelCollection.BASIC_ONLY).setPredicate(ModelsTypes.LEG, ConstructModelCollection.BASIC_ONLY).setPredicate(ModelsTypes.TORSO, s -> Arrays.asList("basic", "storage").contains(s)).setPredicate(ModelsTypes.ARM, s -> Arrays.asList("axe", "blade", "grabber", "hammer", "fisher").contains(s)).build();

    public static ConstructMaterialModel getMaterialModelFor(ConstructMaterial material) {
        return material_model.getOrDefault(material, STONE);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModels(ModelEvent.RegisterAdditional event) {
        ConstructMaterial.ALL_MATERIALS.forEach(mat -> {
            if (material_model.containsKey(mat)) {
                material_model.get(mat).getModelIdentifiers().forEach(rLoc -> event.register(rLoc));
            }
        });
    }

    @Override
    public void register(ConstructMaterial material, String material_type, Predicate<String> headTypePredicate, Predicate<String> torsoTypePredicate, Predicate<String> legTypePredicate, Predicate<String> armTypePredicate) {
        material_model.put(material, new ConstructMaterialModel(material_type).setPredicate(ModelsTypes.HEAD, headTypePredicate).setPredicate(ModelsTypes.TORSO, torsoTypePredicate).setPredicate(ModelsTypes.LEG, legTypePredicate).setPredicate(ModelsTypes.ARM, armTypePredicate));
    }

    static {
        material_model.put(ConstructMaterial.WICKERWOOD, WICKERWOOD);
        material_model.put(ConstructMaterial.STONE, STONE);
        material_model.put(ConstructMaterial.WOOD, WOOD);
        material_model.put(ConstructMaterial.BONE, BONE);
        material_model.put(ConstructMaterial.IRON, IRON);
        material_model.put(ConstructMaterial.GOLD, GOLD);
        material_model.put(ConstructMaterial.DIAMOND, DIAMOND);
        material_model.put(ConstructMaterial.OBSIDIAN, OBSIDIAN);
    }
}


