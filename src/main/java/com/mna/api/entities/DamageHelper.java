/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.Holder
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 */
package com.mna.api.entities;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public class DamageHelper {
    public static final ResourceKey<DamageType> FROST = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "spell_frost"));
    public static final ResourceKey<DamageType> LIGHTNING = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "spell_lightning"));
    public static final ResourceKey<DamageType> BRIARS = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "spell_briars"));
    public static final ResourceKey<DamageType> DISPERSE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "spell_disperse"));
    public static final ResourceKey<DamageType> WTF_BOOM = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "wtf_boom"));
    public static final ResourceKey<DamageType> CONFLAGRATE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "spell_conflagrate"));

    public static DamageSource createSourcedType(ResourceKey<DamageType> pDamageTypeKey, RegistryAccess pRegistry, Entity causingEntity) {
        return DamageHelper.createSourcedType(pDamageTypeKey, pRegistry, causingEntity, null);
    }

    public static DamageSource createSourcedType(ResourceKey<DamageType> pDamageTypeKey, RegistryAccess pRegistry, Entity causingEntity, @Nullable Entity pDirectEntity) {
        return new DamageSource((Holder)pRegistry.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(pDamageTypeKey), pDirectEntity, causingEntity);
    }

    public static DamageSource forType(ResourceKey<DamageType> pDamageTypeKey, RegistryAccess pRegistry) {
        return new DamageSource((Holder)pRegistry.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(pDamageTypeKey));
    }
}

