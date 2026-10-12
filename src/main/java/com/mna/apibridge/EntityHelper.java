/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.apibridge;

import com.mna.api.entities.IEntityHelper;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.entities.utility.PresentItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityHelper
implements IEntityHelper {
    @Override
    public Entity createPresentItemEntity(Level world, double x, double y, double z, ItemStack item) {
        PresentItem entity = new PresentItem(world, x, y, z, item);
        world.addFreshEntity((Entity)entity);
        return entity;
    }

    @Override
    public Entity createManaweavePatternEntity(Level world, Player caster, double x, double y, double z, ResourceLocation patternID) {
        Manaweave manaweave = new Manaweave(world);
        manaweave.setPattern(patternID);
        manaweave.setPos(x, y, z);
        world.addFreshEntity((Entity)manaweave);
        return manaweave;
    }
}

