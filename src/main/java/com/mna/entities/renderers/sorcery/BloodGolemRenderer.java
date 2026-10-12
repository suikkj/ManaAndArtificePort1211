/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 */
package com.mna.entities.renderers.sorcery;

import com.mna.entities.models.BloodGolemModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.entities.summon.BloodGolem;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BloodGolemRenderer
extends MAGeckoRenderer<BloodGolem> {
    public BloodGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new BloodGolemModel());
    }
}

