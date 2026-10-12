package com.mna.api.spells.collections;

import com.mna.api.spells.parts.Modifier;
import net.minecraft.resources.ResourceLocation;

/**
 * Compat 1.21.1: era @ObjectHolder (removido) -> todos os campos nulos (NPE em jogo).
 * Agora resolve do registry do mod via lookup em tempo de inicializacao.
 */
public class Modifiers {
    public static final Modifier DAMAGE =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/damage"));
    public static final Modifier RANGE =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/range"));
    public static final Modifier DURATION =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/duration"));
    public static final Modifier SPEED =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/speed"));
    public static final Modifier RADIUS =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/radius"));
    public static final Modifier MAGNITUDE =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/magnitude"));
    public static final Modifier DELAY =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/delay"));
    public static final Modifier PRECISION =
            com.mna.Registries.Modifier.getValue(ResourceLocation.parse("modifiers/precision"));
}
