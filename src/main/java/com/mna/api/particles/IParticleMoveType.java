/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.mna.api.particles;

import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.network.FriendlyByteBuf;

public interface IParticleMoveType {
    public void serialize(FriendlyByteBuf var1);

    public String serialize();

    public IParticleMoveType deserialize(FriendlyByteBuf var1);

    public void deserialize(String var1);

    public void configureParticle(TextureSheetParticle var1);

    public int getId();
}

