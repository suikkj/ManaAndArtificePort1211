/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.shapes;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.sorcery.targeting.SpellEmanation;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ShapeEmanation
extends Shape {
    public ShapeEmanation(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 3.0f, 1.0f, 10.0f, 0.5f), new AttributeValuePair(Attribute.WIDTH, 3.0f, 1.0f, 9.0f, 1.0f), new AttributeValuePair(Attribute.HEIGHT, 2.0f, 1.0f, 9.0f, 1.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        if (!world.isClientSide) {
            SpellEmanation projectile = new SpellEmanation(source.getCaster(), recipe, world);
            world.addFreshEntity((Entity)projectile);
        }
        return Arrays.asList(new SpellTarget((Entity)source.getCaster()));
    }

    @Override
    public boolean spawnsTargetEntity() {
        return true;
    }

    @Override
    public boolean isChanneled() {
        return true;
    }

    @Override
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 12000;
    }
}

