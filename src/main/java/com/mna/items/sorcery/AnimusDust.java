/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.items.sorcery;

import com.mna.api.items.TieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AnimusDust
extends TieredItem {
    public AnimusDust() {
        super(new Item.Properties());
    }

    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        context.getPlayer().swing(context.getHand());
        if (world.isClientSide) {
            if (world.getBlockEntity(context.getClickedPos()) == null) {
                AnimusDust.spawnParticles(world, context.getClickedPos());
            }
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        SpellRecipe animus = new SpellRecipe();
        animus.setShape(Shapes.TOUCH);
        animus.addComponent(Components.ANIMUS);
        HashMap<SpellEffect, ComponentApplicationResult> result = SpellCaster.ApplyComponents(animus, new SpellSource((LivingEntity)context.getPlayer(), context.getHand()), new SpellTarget(pos, face), new SpellContext(world, animus));
        if (result.get(Components.ANIMUS).is_success) {
            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return super.useOn(context);
    }

    public static void spawnParticles(Level world, BlockPos pos) {
        int i;
        Vec3 center = Vec3.atCenterOf((Vec3i)pos);
        int[] blue = new int[]{79, 106, 158};
        int[] purple = new int[]{144, 102, 147};
        int[] red = new int[]{241, 54, 64};
        for (i = 0; i < 15; ++i) {
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())).setColor(blue[0], blue[1], blue[2]).setScale(0.075f), center.x() - 0.5 + Math.random(), center.y() - 0.5 + Math.random(), center.z() - 0.5 + Math.random(), (-0.5 + Math.random()) * (double)0.1f, 0.1, (-0.5 + Math.random()) * (double)0.1f);
        }
        for (i = 0; i < 5; ++i) {
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())).setColor(purple[0], purple[1], purple[2]), center.x() - 1.0 + Math.random() * 2.0, center.y() - 1.0 + Math.random() * 2.0, center.z() - 1.0 + Math.random() * 2.0, (-0.5 + Math.random()) * (double)0.1f, 0.1, (-0.5 + Math.random()) * (double)0.1f);
        }
        for (i = 0; i < 5; ++i) {
            world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())).setColor(red[0], red[1], red[2]), center.x() - 1.0 + Math.random() * 2.0, center.y() - 1.0 + Math.random() * 2.0, center.z() - 1.0 + Math.random() * 2.0, (-0.5 + Math.random()) * (double)0.1f, 0.1, (-0.5 + Math.random()) * (double)0.1f);
        }
    }
}

