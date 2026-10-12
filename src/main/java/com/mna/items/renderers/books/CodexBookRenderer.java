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

public class CodexBookRenderer
extends ItemBookRenderer {
    public CodexBookRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems, RLoc.create("item/special/guide_book_open"), RLoc.create("item/special/guide_book_closed"));
    }
}

