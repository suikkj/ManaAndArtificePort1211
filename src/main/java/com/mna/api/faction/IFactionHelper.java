/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.faction;

import com.mna.api.faction.IFaction;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public interface IFactionHelper {
    public List<IFaction> getAllFactions();

    public IFaction getFaction(ResourceLocation var1);

    public List<IFaction> getFactionsExcept(IFaction ... var1);
}

