/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.sound.SFX;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class RitualEffectForgottenLore
extends RitualEffectCreateEssence {
    public RitualEffectForgottenLore(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        if (this.getBookshelfLocations(context).size() < 10) {
            return Component.translatable((String)"ritual.mna.forgotten_lore.no_bookshelves");
        }
        return null;
    }

    private ArrayList<BlockPos> getBookshelfLocations(IRitualContext context) {
        int searchDist = 4;
        int lb = context.getRecipe().getLowerBound();
        ArrayList<BlockPos> searched = new ArrayList<BlockPos>();
        ArrayList<BlockPos> found = new ArrayList<BlockPos>();
        for (int y = -1; y <= 1; ++y) {
            for (int i = -lb - 1; i <= lb + 1; ++i) {
                BlockPos a = context.getCenter().offset(-searchDist, y, i);
                BlockPos b = context.getCenter().offset(searchDist, y, i);
                BlockPos c = context.getCenter().offset(i, y, -searchDist);
                BlockPos d = context.getCenter().offset(i, y, searchDist);
                if (!searched.contains(a)) {
                    searched.add(a);
                }
                if (!searched.contains(b)) {
                    searched.add(b);
                }
                if (!searched.contains(c)) {
                    searched.add(c);
                }
                if (searched.contains(d)) continue;
                searched.add(d);
            }
        }
        for (BlockPos pos : searched) {
            BlockState state = context.getLevel().getBlockState(pos);
            if (!(state.getEnchantPowerBonus((LevelReader)context.getLevel(), pos) > 0.0f)) continue;
            found.add(pos);
        }
        return found;
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return SFX.Loops.ARCANE;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        ArrayList<BlockPos> shelves = this.getBookshelfLocations(context);
        if (shelves.size() == 0) {
            return false;
        }
        BlockPos end = context.getCenter().above();
        BlockPos start = shelves.get((int)(Math.random() * (double)shelves.size()));
        context.getLevel().addParticle((ParticleOptions)ParticleTypes.ENCHANT, (double)end.getX() + 0.5, (double)end.getY() + 0.5, (double)end.getZ() + 0.5, (double)(-(end.getX() - start.getX())), -0.5, (double)(-(end.getZ() - start.getZ())));
        return true;
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack((ItemLike)ItemInit.MOTE_ARCANE.get());
    }
}

