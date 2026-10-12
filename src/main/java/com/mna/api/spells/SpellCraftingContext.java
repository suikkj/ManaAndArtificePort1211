/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.player.Player
 */
package com.mna.api.spells;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;

public class SpellCraftingContext {
    @Nullable
    private final Player player;

    public SpellCraftingContext(@Nullable Player player) {
        this.player = player;
    }

    @Nullable
    public final Player getPlayer() {
        return this.player;
    }
}

