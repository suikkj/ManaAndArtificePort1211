/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.entities.attributes;

import com.mna.entities.attributes.AttributeIntelligence;
import com.mna.entities.attributes.AttributePerceptionDistance;
import com.mna.entities.attributes.AttributeRangedDamage;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AttributeInit {
    public static DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, "mna");
    public static DeferredHolder<Attribute, ? extends Attribute> PERCEPTION_DISTANCE = ATTRIBUTES.register("perception_distance", () -> new AttributePerceptionDistance(8.0));
    public static DeferredHolder<Attribute, ? extends Attribute> INTELLIGENCE = ATTRIBUTES.register("intelligence", () -> new AttributeIntelligence(8.0));
    public static DeferredHolder<Attribute, ? extends Attribute> RANGED_DAMAGE = ATTRIBUTES.register("ranged_damage", () -> new AttributeRangedDamage(0.0));
}


