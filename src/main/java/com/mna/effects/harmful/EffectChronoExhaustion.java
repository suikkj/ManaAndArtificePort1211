/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.effects.harmful;

import com.mna.effects.interfaces.INoCreeperLingering;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

public class EffectChronoExhaustion
extends MobEffect
implements INoCreeperLingering {
    public EffectChronoExhaustion() {
        super(MobEffectCategory.NEUTRAL, 0);
    }

    public List<ItemStack> getCurativeItems() {
        return new ArrayList<ItemStack>();
    }
}

