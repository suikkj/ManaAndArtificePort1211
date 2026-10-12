/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package com.mna.recipes.multiblock.block_matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface IBlockMatcher {
    public boolean match(Level var1, BlockPos var2, BlockPos var3, BlockState var4, BlockState var5, boolean var6);

    public ResourceLocation getId();

    default public boolean MatchStateProperties(BlockState desired, BlockState inWorld, String ... propertyNames) {
        MutableBoolean matched = new MutableBoolean(true);
        List<String> propNames = Arrays.asList(propertyNames);
        desired.getProperties().forEach(p -> {
            if (propNames.size() == 0 || propNames.contains(p.getName())) {
                if (inWorld.hasProperty(p)) {
                    if (inWorld.getValue(p) != desired.getValue(p)) {
                        matched.setFalse();
                    }
                } else {
                    matched.setFalse();
                }
            }
        });
        return matched.booleanValue();
    }

    public ArrayList<ItemStack> getValidBlocks(Block var1);

    default public ArrayList<ItemStack> getValidItems(Block block) {
        return this.getValidBlocks(block);
    }
}

