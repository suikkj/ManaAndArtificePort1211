/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.capabilities.resource;

import com.mna.api.capabilities.resource.ICastingResource;
import net.minecraft.resources.ResourceLocation;

public interface ICastingResourceRegistry {
    public void register(ResourceLocation var1, Class<? extends ICastingResource> var2);
}

