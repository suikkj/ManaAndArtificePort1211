/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
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
import com.mna.entities.sorcery.targeting.SpellWall;
import com.mna.spells.shapes.ShapeRaytrace;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ShapeWall
extends ShapeRaytrace {
    public ShapeWall(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 3.0f, 1.0f, 10.0f, 0.5f, 10.0f), new AttributeValuePair(Attribute.WIDTH, 3.0f, 1.0f, 9.0f, 1.0f, 3.0f), new AttributeValuePair(Attribute.HEIGHT, 2.0f, 1.0f, 9.0f, 1.0f, 5.0f), new AttributeValuePair(Attribute.RANGE, 8.0f, 8.0f, 32.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        List<SpellTarget> tgts = super.Target(source, world, modificationData, recipe);
        SpellTarget tgt = tgts.get(0);
        if (tgt == SpellTarget.NONE) {
            return tgts;
        }
        if (!world.isClientSide) {
            SpellWall projectile = new SpellWall(source.getCaster(), recipe, world, modificationData.getValue(Attribute.PRECISION) == 1.0f);
            if (tgt.isBlock()) {
                BlockPos pos = tgt.getBlock().relative(tgt.getBlockFace(null));
                projectile.setPos((float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f);
            } else if (tgt.isEntity()) {
                projectile.setPos(tgt.getEntity().getX(), tgt.getEntity().getY(), tgt.getEntity().getZ());
            }
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
        return false;
    }

    @Override
    public boolean allowChanneledComponents() {
        return false;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public int baselineCooldown() {
        return 40;
    }
}

