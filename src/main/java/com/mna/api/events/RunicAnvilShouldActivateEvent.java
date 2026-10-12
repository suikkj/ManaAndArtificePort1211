package com.mna.api.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * [PORT 1.21.1] {@code Event.HasResult} foi removido do bus do NeoForge.
 * O resultado agora e carregado no proprio evento (mapeado do enum antigo).
 */
public class RunicAnvilShouldActivateEvent
extends Event {
    public enum Result { ALLOW, DENY, DEFAULT }

    public final ItemStack pattern;
    public final ItemStack material;
    private Result result = Result.DEFAULT;

    public RunicAnvilShouldActivateEvent(ItemStack pattern, ItemStack material) {
        this.pattern = pattern.copy();
        this.material = material.copy();
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}


