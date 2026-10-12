/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 */
package com.mna.entities.renderers.sorcery;

import com.mna.entities.models.BloodTendrilModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.entities.summon.BloodTendril;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BloodTendrilRenderer
extends MAGeckoRenderer<BloodTendril> {
    public BloodTendrilRenderer(EntityRendererProvider.Context context) {
        super(context, new BloodTendrilModel());
    }
}

