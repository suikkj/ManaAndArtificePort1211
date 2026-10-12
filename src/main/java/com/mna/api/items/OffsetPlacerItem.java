/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.block.Block
 */
package com.mna.api.items;

import com.mna.api.items.TieredBlockItem;
import java.util.function.Function;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class OffsetPlacerItem
extends TieredBlockItem {
    Function<BlockPlaceContext, BlockPlaceContext> adjuster;

    public OffsetPlacerItem(Block forBlock, Item.Properties properties, Function<BlockPlaceContext, BlockPlaceContext> adjuster) {
        super(forBlock, properties);
        this.adjuster = adjuster;
    }

    public InteractionResult place(BlockPlaceContext context) {
        if (this.adjuster != null) {
            return super.place(this.adjuster.apply(context));
        }
        return super.place(context);
    }
}

