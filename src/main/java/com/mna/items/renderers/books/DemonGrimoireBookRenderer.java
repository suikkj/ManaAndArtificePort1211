/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 */
package com.mna.items.renderers.books;

import com.mna.api.tools.RLoc;
import com.mna.items.renderers.ItemSpellBookRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class DemonGrimoireBookRenderer
extends ItemSpellBookRenderer {
    public DemonGrimoireBookRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems, RLoc.create("item/special/grimoire_demon_open"), RLoc.create("item/special/grimoire_demon_closed"), true);
    }
}

