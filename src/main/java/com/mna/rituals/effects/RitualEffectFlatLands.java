/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.FlatLands;
import com.mna.items.ItemInit;
import com.mna.items.runes.RuneMarkingPair;
import java.util.Iterator;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RitualEffectFlatLands
extends RitualEffect {
    public static final int MAX_DISTANCE = 6400;
    public static final int MAX_SIZE = 64;

    public RitualEffectFlatLands(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        ItemStack location_parameter = ItemStack.EMPTY;
        Iterator<ItemStack> iterator = context.getCollectedReagents(i -> i.getItem() == ItemInit.RUNE_MARKING_PAIR.get()).iterator();
        if (iterator.hasNext()) {
            ItemStack stack;
            location_parameter = stack = iterator.next();
        }
        if (location_parameter.isEmpty()) {
            this.dropCollectedReagents(context);
            return false;
        }
        AABB bb = ((RuneMarkingPair)ItemInit.RUNE_MARKING_PAIR.get()).getBounds(location_parameter);
        if (bb == null) {
            this.dropCollectedReagents(context);
            return false;
        }
        if (bb.getXsize() > 64.0 || bb.getYsize() > 64.0 || bb.getZsize() > 64.0) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/flat_lands.tooBig"));
            this.dropCollectedReagents(context);
            return false;
        }
        Vec3 ritualCenter = Vec3.atCenterOf((Vec3i)context.getCenter());
        double dist = bb.distanceToSqr(ritualCenter);
        if (dist > 6400.0) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/flat_lands.tooFar"));
            this.dropCollectedReagents(context);
            return false;
        }
        FlatLands flat_lands = new FlatLands((EntityType)EntityInit.FLAT_LANDS.get(), context.getLevel());
        flat_lands.setPos(Vec3.atCenterOf((Vec3i)context.getCenter().above(2)));
        flat_lands.setCaster(context.getCaster());
        flat_lands.setBounds(bb);
        context.getLevel().addFreshEntity((Entity)flat_lands);
        return true;
    }

    private void dropCollectedReagents(IRitualContext context) {
        Vec3 drop = Vec3.atCenterOf((Vec3i)context.getCenter().above());
        context.getCollectedReagents().forEach(is -> {
            ItemEntity ie = new ItemEntity(context.getLevel(), drop.x, drop.y, drop.z, is);
            context.getLevel().addFreshEntity((Entity)ie);
        });
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }
}

