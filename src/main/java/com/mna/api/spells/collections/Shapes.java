package com.mna.api.spells.collections;

import com.mna.api.spells.parts.Shape;
import net.minecraft.resources.ResourceLocation;

/**
 * Compat 1.21.1: era @ObjectHolder (removido) -> todos os campos nulos (NPE em jogo).
 * Agora resolve do registry do mod via lookup em tempo de inicializacao.
 */
public class Shapes {
    public static final Shape SELF =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/self"));
    public static final Shape TOUCH =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/touch"));
    public static final Shape BOLT =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/bolt"));
    public static final Shape PROJECTILE =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/projectile"));
    public static final Shape WALL =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/wall"));
    public static final Shape WAVE =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/wave"));
    public static final Shape BEAM =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/beam"));
    public static final Shape EMANATION =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/emanation"));
    public static final Shape RUNE =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/rune"));
    public static final Shape SMITE =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/smite"));
    public static final Shape BOUND_SWORD =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/bound_sword"));
    public static final Shape BOUND_AXE =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/bound_axe"));
    public static final Shape BOUND_BOW =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/bound_bow"));
    public static final Shape BOUND_SHIELD =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/bound_shield"));
    public static final Shape SUMMON =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/summon"));
    public static final Shape WRATH =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/wrath"));
    public static final Shape CHAIN =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/chain"));
    public static final Shape FISSURE =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/fissure"));
    public static final Shape FOCUS =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/focus"));
    public static final Shape EMBERS =
            com.mna.Registries.Shape.getValue(ResourceLocation.parse("shapes/embers"));
}
