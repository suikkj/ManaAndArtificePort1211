/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 */
package com.mna.items.renderers.books;

import com.mna.api.tools.RLoc;
import com.mna.items.renderers.books.ItemBookRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class RecipeCopyBookRenderer
extends ItemBookRenderer {
    public RecipeCopyBookRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems, RLoc.create("item/special/recipe_copy_book_open"), RLoc.create("item/special/recipe_copy_book_closed"));
        this.addLayer(RLoc.create("item/special/recipe_copy_book_open_2"), RLoc.create("item/special/recipe_copy_book_closed_2"));
    }
}

