/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 */
package com.mna.entities.renderers.faction;

import com.mna.entities.faction.Deathcap;
import com.mna.entities.models.faction.DeathcapModel;
import com.mna.entities.renderers.faction.MAGeckoFactionRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DeathcapRenderer
extends MAGeckoFactionRenderer<Deathcap> {
    public DeathcapRenderer(EntityRendererProvider.Context context) {
        super(context, new DeathcapModel());
        this.enableEmissive();
    }
}

