/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package com.mna.recipes;

import com.google.gson.JsonObject;
import com.mna.recipes.AMRecipeBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public abstract class AMRecipeBaseSerializer<T extends AMRecipeBase>
implements RecipeSerializer<T> {
    public final T fromJson(ResourceLocation recipeId, JsonObject json) {
        T inst = this.readExtra(recipeId, json);
        return inst;
    }

    public final T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        T inst = this.readExtra(recipeId, buffer);
        ((AMRecipeBase)inst).tier = buffer.readInt();
        return inst;
    }

    public final void toNetwork(FriendlyByteBuf buffer, T recipe) {
        this.writeExtra(buffer, recipe);
        buffer.writeInt(((AMRecipeBase)recipe).getTier());
    }

    @Override
    public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec() {
        return net.minecraft.network.codec.StreamCodec.of((buf, recipe) -> this.toNetwork(buf, recipe), buf -> this.fromNetwork(null, buf));
    }

    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1 - RUNTIME TODO] codec real de receita nao migrado; receitas destes tipos nao carregam de JSON.
        return com.mojang.serialization.MapCodec.unit((T)null);
    }

    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1 - RUNTIME TODO] codec real de receita nao migrado; receitas destes tipos nao carregam de JSON.
        return com.mojang.serialization.MapCodec.unit((T)null);
    }

    protected abstract T readExtra(ResourceLocation var1, JsonObject var2);

    protected abstract T readExtra(ResourceLocation var1, FriendlyByteBuf var2);

    protected abstract void writeExtra(FriendlyByteBuf var1, T var2);
}

