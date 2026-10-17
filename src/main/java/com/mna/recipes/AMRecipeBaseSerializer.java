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
        // [PORT 1.21.1] O RecipeHolder.STREAM_CODEC consome o ResourceLocation do holder ANTES de
        // chamar o Recipe.STREAM_CODEC, e não o repassa ao serializer (o 1.20.1 chamava
        // RecipeSerializer.fromNetwork(recipeId, buffer) com o ID). Sem o ID, receitas que o usam
        // no decode (ex.: ManaweavingPattern -> RecognitionEngine.registerTrainingDataSample) quebram
        // com NPE. Solução: serializar o ID dentro do próprio streamCodec (simétrico).
        return net.minecraft.network.codec.StreamCodec.of(
            (buf, recipe) -> {
                buf.writeResourceLocation(((AMRecipeBase) recipe).getId());
                this.toNetwork(buf, recipe);
            },
            buf -> {
                net.minecraft.resources.ResourceLocation id = buf.readResourceLocation();
                return this.fromNetwork(id, buf);
            }
        );
    }

    /** [PORT 1.21.1] Placeholder de ID: o codec do 1.21.1 nao recebe o ResourceLocation
     *  (o loader passa so o MapLike). O ID real e' restaurado pos-load (RecipeIdFixer). */
    public static final ResourceLocation PENDING_ID = ResourceLocation.fromNamespaceAndPath("mna", "__pending__");

    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1] O datapack loader do 1.21.1 usa codec() (MapCodec), nao fromJson.
        // Bridge: converte o MapLike de volta p/ JsonObject e reusa readExtra(...) (logica 1.20.1 intacta).
        final AMRecipeBaseSerializer<T> self = this;
        return new com.mojang.serialization.MapCodec<T>() {
            @Override
            public <O> com.mojang.serialization.DataResult<T> decode(com.mojang.serialization.DynamicOps<O> ops, com.mojang.serialization.MapLike<O> input) {
                try {
                    JsonObject json = new JsonObject();
                    input.entries().forEach(e -> {
                        com.google.gson.JsonElement keyJson = ops.convertTo(com.mojang.serialization.JsonOps.INSTANCE, e.getFirst());
                        com.google.gson.JsonElement valJson = ops.convertTo(com.mojang.serialization.JsonOps.INSTANCE, e.getSecond());
                        json.add(keyJson.getAsString(), valJson);
                    });
                    T inst = self.readExtra(PENDING_ID, json);
                    return inst == null
                        ? com.mojang.serialization.DataResult.error(() -> "MNA recipe serializer returned null")
                        : com.mojang.serialization.DataResult.success(inst);
                } catch (Exception ex) {
                    return com.mojang.serialization.DataResult.error(() -> "MNA recipe parse error: " + ex);
                }
            }

            @Override
            public <O> java.util.stream.Stream<O> keys(com.mojang.serialization.DynamicOps<O> ops) {
                return java.util.stream.Stream.empty();
            }

            @Override
            public <O> com.mojang.serialization.RecordBuilder<O> encode(T input, com.mojang.serialization.DynamicOps<O> ops, com.mojang.serialization.RecordBuilder<O> prefix) {
                return prefix; // serializacao de volta nao e' usada no load de datapack
            }
        };
    }

    protected abstract T readExtra(ResourceLocation var1, JsonObject var2);

    protected abstract T readExtra(ResourceLocation var1, FriendlyByteBuf var2);

    protected abstract void writeExtra(FriendlyByteBuf var1, T var2);
}

