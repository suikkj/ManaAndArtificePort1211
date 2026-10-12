package com.mna.items.armor;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * [PORT 1.21.1] {@code ArmorMaterial} virou um record data-driven registrado.
 * Os valores antigos do enum {@link MAArmorMaterial} sao convertidos aqui.
 */
public class ArmorMaterialInit {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "mna");

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BROKEN = MATERIALS.register("broken", () -> build(MAArmorMaterial.BROKEN));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> INFUSED_SILK = MATERIALS.register("infused_silk", () -> build(MAArmorMaterial.INFUSED_SILK));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BONE = MATERIALS.register("bone", () -> build(MAArmorMaterial.BONE));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DEMON = MATERIALS.register("demon", () -> build(MAArmorMaterial.DEMON));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FEY = MATERIALS.register("fey", () -> build(MAArmorMaterial.FEY));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COUNCIL = MATERIALS.register("council", () -> build(MAArmorMaterial.COUNCIL));

    private static ArmorMaterial build(MAArmorMaterial mat) {
        Map<ArmorItem.Type, Integer> defense = Map.of(
                ArmorItem.Type.BOOTS, mat.getDefenseForType(ArmorItem.Type.BOOTS),
                ArmorItem.Type.LEGGINGS, mat.getDefenseForType(ArmorItem.Type.LEGGINGS),
                ArmorItem.Type.CHESTPLATE, mat.getDefenseForType(ArmorItem.Type.CHESTPLATE),
                ArmorItem.Type.HELMET, mat.getDefenseForType(ArmorItem.Type.HELMET));
        return new ArmorMaterial(
                defense,
                mat.getEnchantmentValue(),
                mat.getEquipSound(),
                mat::getRepairIngredient,
                List.of(new ArmorMaterial.Layer(ResourceLocation.parse(mat.getName()))),
                mat.getToughness(),
                mat.getKnockbackResistance());
    }
}
