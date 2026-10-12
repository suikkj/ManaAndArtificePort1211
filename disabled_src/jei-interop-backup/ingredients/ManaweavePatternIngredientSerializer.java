/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.neoforged.neoforge.common.crafting.IIngredientSerializer
 */
package com.mna.interop.jei.ingredients;

import com.google.gson.JsonObject;
import com.mna.interop.jei.ingredients.ManaweavePatternIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.crafting.IIngredientSerializer;

public class ManaweavePatternIngredientSerializer
implements IIngredientSerializer<ManaweavePatternIngredient> {
    public ManaweavePatternIngredient parse(FriendlyByteBuf buffer) {
        return new ManaweavePatternIngredient(buffer.readResourceLocation());
    }

    public ManaweavePatternIngredient parse(JsonObject json) {
        return null;
    }

    public void write(FriendlyByteBuf buffer, ManaweavePatternIngredient ingredient) {
        buffer.writeResourceLocation(ingredient.getWeaveId());
    }
}


