/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 */
package com.mna.entities.renderers.faction;

import com.mna.entities.faction.Chatterer;
import com.mna.entities.models.faction.ChattererModel;
import com.mna.entities.renderers.faction.MAGeckoFactionRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ChattererRenderer
extends MAGeckoFactionRenderer<Chatterer> {
    public ChattererRenderer(EntityRendererProvider.Context context) {
        super(context, new ChattererModel());
        this.enableEmissive();
    }
}

