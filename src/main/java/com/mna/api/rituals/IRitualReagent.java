/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.rituals;

import net.minecraft.resources.ResourceLocation;

public interface IRitualReagent {
    public boolean isOptional();

    public boolean shouldConsumeReagent();

    public boolean isManualReturn();

    public boolean isDynamic();

    public boolean isDynamicSource();

    public ResourceLocation getResourceLocation();

    public void setResourceLocation(ResourceLocation var1);

    public boolean isEmpty();
}

