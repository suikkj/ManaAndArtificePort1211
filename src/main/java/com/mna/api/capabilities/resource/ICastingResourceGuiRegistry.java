/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.capabilities.resource;

import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import net.minecraft.resources.ResourceLocation;

public interface ICastingResourceGuiRegistry {
    public void registerResourceGui(ResourceLocation var1, ICastingResourceGuiProvider var2);
}

