package com.mna.api.tools;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

/**
 * Utilitarios de compatibilidade 1.21.1.
 *
 * No 1.21.1 a API de efeitos passou a usar {@code Holder<MobEffect>} em vez de
 * {@code MobEffect} direto. Este helper converte uma instancia crua em Holder
 * para os call-sites do mod que ainda trabalham com MobEffect.
 */
public final class MAEffects {
    private MAEffects() {
    }

    /** Convolve um MobEffect em Holder<MobEffect> (compat 1.21.1). */
    public static Holder<MobEffect> holder(MobEffect effect) {
        if (effect == null) {
            return null;
        }
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    /** Versao tolerante: devolve Holder vazio quando o efeito nao existe. */
    public static Holder<MobEffect> holderOrEmpty(MobEffect effect) {
        return effect == null ? null : BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }
}
