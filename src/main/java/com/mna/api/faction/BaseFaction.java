/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 */
package com.mna.api.faction;

import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.faction.IFaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public abstract class BaseFaction
implements IFaction {
    public static final String NBT_CASTING_RESOURCE_IDX = "faction_casting_resource_idx";
    private ResourceLocation[] __castingResources;

    public BaseFaction() {
        this(CastingResourceIDs.MANA);
    }

    public BaseFaction(ResourceLocation ... castingResources) {
        this.__castingResources = castingResources;
    }

    @Override
    public ResourceLocation[] getCastingResources() {
        return this.__castingResources;
    }

    @Override
    public ResourceLocation getCastingResource(Player player) {
        int idx = player.getPersistentData().getInt(NBT_CASTING_RESOURCE_IDX) % this.__castingResources.length;
        return this.__castingResources[idx];
    }
}

