/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleOptions$Deserializer
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.particles;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.IParticleMoveType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.particles.parameters.ParticleBoolean;
import com.mna.api.particles.parameters.ParticleColor;
import com.mna.api.particles.parameters.ParticleFloat;
import com.mna.api.particles.parameters.ParticleInt;
import com.mna.api.particles.parameters.ParticleItemStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MAParticleType
extends ParticleType<MAParticleType>
implements ParticleOptions {
    private ParticleType<MAParticleType> type;
    private ParticleColor color;
    private ParticleFloat scale;
    private ParticleInt life;
    private ParticleInt lifePadding;
    private ParticleFloat gravity;
    private ParticleBoolean physics;
    private IParticleMoveType mover;
    private ParticleItemStack stack;
    public boolean disableDepthTest;
    public static final com.mojang.serialization.MapCodec<MAParticleType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.INT.fieldOf("color").forGetter(d -> d.color != null ? d.color.getColor() : 1), Codec.FLOAT.fieldOf("scale").forGetter(d -> Float.valueOf(d.scale != null ? d.scale.value() : 1.0f)), Codec.INT.fieldOf("life").forGetter(d -> d.life != null ? d.life.value() : 20), Codec.INT.fieldOf("lifePadding").forGetter(d -> d.lifePadding != null ? d.lifePadding.value() : 0), Codec.FLOAT.fieldOf("gravity").forGetter(d -> Float.valueOf(d.gravity != null ? d.gravity.value() : 0.0f)), Codec.BOOL.fieldOf("physics").forGetter(d -> d.physics != null ? d.physics.value() : false), Codec.BOOL.fieldOf("disableDepthTest").forGetter(d -> d.disableDepthTest)).apply(instance, MAParticleType::new));

    /** [PORT 1.21.1] ParticleOptions$Deserializer foi REMOVIDO; leitura virou StreamCodec. */
    static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MAParticleType> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, value) -> value.writeToNetwork(buf), MAParticleType::readFromNetwork);

    private static MAParticleType readFromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {


            ParticleColor color = null;
            ParticleFloat scale = null;
            ParticleInt life = null;
            ParticleInt lifePadding = null;
            ParticleFloat gravity = null;
            ParticleBoolean physics = null;
            ParticleItemStack stack = null;
            IParticleMoveType mover = null;
            boolean depthDisable = false;
            if (buffer.readBoolean()) {
                color = ParticleColor.deserialize(buffer.readUtf(Short.MAX_VALUE));
            }
            scale = ParticleFloat.deserialize(buffer);
            life = ParticleInt.deserialize(buffer);
            lifePadding = ParticleInt.deserialize(buffer);
            gravity = ParticleFloat.deserialize(buffer);
            physics = ParticleBoolean.deserialize(buffer);
            stack = ParticleItemStack.deserialize(buffer);
            if (buffer.readBoolean()) {
                mover = ManaAndArtificeMod.getParticleSerializationHelper().fromID(buffer.readInt()).deserialize(buffer);
            }
            depthDisable = buffer.readBoolean();
        return new MAParticleType(color, scale, life, lifePadding, gravity, physics, stack, mover, depthDisable);
    }

    public MAParticleType() {
        super(false);
    }

    public MAParticleType(ParticleType<MAParticleType> type) {
        this();
        this.type = type;
    }

    private MAParticleType(int color, float scale, int life, int lifePadding, float gravity, boolean physics, boolean disableDepth) {
        super(disableDepth);
        this.setColor(color);
        this.setScale(scale);
        this.setGravity(gravity);
        this.setPhysics(physics);
        this.setMaxAge(life);
    }

    private MAParticleType(ParticleType<MAParticleType> type, ParticleColor color, ParticleFloat scale, ParticleInt life, ParticleInt lifePadding, ParticleFloat gravity, ParticleBoolean physics, ParticleItemStack stack, IParticleMoveType mover, boolean disableDepth) {
        super(disableDepth);
        this.color = color;
        this.scale = scale;
        this.life = life;
        this.lifePadding = lifePadding;
        this.gravity = gravity;
        this.physics = physics;
        this.mover = mover;
    }

    public com.mojang.serialization.MapCodec<MAParticleType> codec() {
        return CODEC;
    }

    @Override
    public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, MAParticleType> streamCodec() {
        return STREAM_CODEC;
    }

    public void writeToNetwork(FriendlyByteBuf packetBuffer) {
        if (this.color != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeUtf(this.color.serialize());
        } else {
            packetBuffer.writeBoolean(false);
        }
        ParticleFloat.serialize(this.scale, packetBuffer);
        ParticleInt.serialize(this.life, packetBuffer);
        ParticleInt.serialize(this.lifePadding, packetBuffer);
        ParticleFloat.serialize(this.gravity, packetBuffer);
        ParticleBoolean.serialize(this.physics, packetBuffer);
        ParticleItemStack.serialize(this.stack, packetBuffer);
        if (this.mover != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeInt(this.mover.getId());
            this.mover.serialize(packetBuffer);
        } else {
            packetBuffer.writeBoolean(false);
        }
        packetBuffer.writeBoolean(this.disableDepthTest);
    }

    public String writeToString() {
        return this.getType().toString() + " " + (this.color != null ? this.color.serialize() : "NO COLOR OVERRIDE") + " " + (this.scale != null ? this.scale.serialize() : "NO SCALE OVERRIDE") + (this.life != null ? this.life.serialize() : "NO LIFE OVERRIDE") + (this.lifePadding != null ? this.lifePadding.serialize() : "NO LIFE PADDING") + (this.mover != null ? this.mover.serialize() : "NO MOVE OVERRIDE");
    }

    public MAParticleType setColor(float r, float g, float b, float a) {
        this.color = new ParticleColor(r, g, b, a);
        return this;
    }

    public MAParticleType setColor(float r, float g, float b) {
        this.color = new ParticleColor(r, g, b, 255.0f);
        return this;
    }

    public MAParticleType setColor(double r, double g, double b, double a) {
        this.color = new ParticleColor((float)r, (float)g, (float)b, (float)a);
        return this;
    }

    public MAParticleType setColor(double r, double g, double b) {
        this.color = new ParticleColor((float)r, (float)g, (float)b, 255.0f);
        return this;
    }

    public MAParticleType setColor(int r, int g, int b) {
        this.color = new ParticleColor((float)r, (float)g, (float)b, 255.0f);
        return this;
    }

    public MAParticleType setColor(int packedColor) {
        this.color = ParticleColor.fromInt(packedColor);
        return this;
    }

    public MAParticleType setColor(int r, int g, int b, int a) {
        this.color = new ParticleColor(r, g, b, a);
        return this;
    }

    public MAParticleType setColor(Affinity affinity) {
        this.color = new ParticleColor((float)affinity.getColor()[0], (float)affinity.getColor()[1], (float)affinity.getColor()[2], 255.0f);
        return this;
    }

    public MAParticleType setScale(float scale) {
        this.scale = new ParticleFloat(scale);
        return this;
    }

    public MAParticleType setMaxAge(int age) {
        this.life = new ParticleInt(age);
        return this;
    }

    public MAParticleType setAgePadding(int padding) {
        this.lifePadding = new ParticleInt(padding);
        return this;
    }

    public MAParticleType setGravity(float gravity) {
        this.gravity = new ParticleFloat(gravity);
        return this;
    }

    public MAParticleType setPhysics(boolean physics) {
        this.physics = new ParticleBoolean(physics);
        return this;
    }

    public MAParticleType setStack(ItemStack stack) {
        this.stack = new ParticleItemStack(stack);
        return this;
    }

    public MAParticleType setMover(IParticleMoveType mover) {
        this.mover = mover;
        return this;
    }

    @Nullable
    public IParticleMoveType getMover() {
        return this.mover;
    }

    @Nullable
    public ParticleColor getColor() {
        return this.color;
    }

    @Nullable
    public ParticleFloat getScale() {
        return this.scale;
    }

    @Nullable
    public ParticleInt getLife() {
        return this.life;
    }

    @Nullable
    public ParticleInt getLifePadding() {
        return this.lifePadding;
    }

    @Nullable
    public ParticleFloat getGravity() {
        return this.gravity;
    }

    @Nullable
    public ParticleBoolean getPhysics() {
        return this.physics;
    }

    @Nullable
    public ParticleItemStack getStack() {
        return this.stack;
    }

    public ParticleType<MAParticleType> getType() {
        return this.type == null ? (ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get() : this.type;
    }
}

