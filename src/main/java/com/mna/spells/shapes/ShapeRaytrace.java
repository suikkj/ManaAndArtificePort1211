/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.shapes;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.spells.targeting.SpellTargetHelper;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShapeRaytrace
extends Shape {
    public ShapeRaytrace(float range, float maxRange, ResourceLocation icon) {
        super(icon, new AttributeValuePair(Attribute.RANGE, range, range, maxRange, 1.0f));
    }

    public ShapeRaytrace(ResourceLocation icon, AttributeValuePair ... attributes) {
        super(icon, attributes);
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        if (source == null) {
            return Arrays.asList(SpellTarget.NONE);
        }
        HitResult targetResult = this.performRaytrace(source, world, modificationData, recipe);
        HitResult.Type targetType = targetResult.getType();
        if (targetType == HitResult.Type.MISS) {
            return Arrays.asList(SpellTarget.NONE);
        }
        return targetType == HitResult.Type.BLOCK ? Arrays.asList(new SpellTarget(((BlockHitResult)targetResult).getBlockPos(), ((BlockHitResult)targetResult).getDirection())) : Arrays.asList(new SpellTarget(((EntityHitResult)targetResult).getEntity()));
    }

    @Override
    public List<SpellTarget> TargetNPCCast(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, SpellTarget targetHint) {
        Vec3 hintPos = targetHint.getPosition();
        if (source.getOrigin().distanceTo(hintPos) < (double)modificationData.getValue(Attribute.RANGE)) {
            return Arrays.asList(targetHint);
        }
        return this.Target(source, world, modificationData, recipe);
    }

    private HitResult performRaytrace(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        float range = this.getRange(source, world, modificationData, recipe);
        return SpellTargetHelper.rayTrace((Entity)source.getCaster(), world, source.getOrigin(), source.getForward(), true, false, ClipContext.Block.OUTLINE, entity -> entity.isPickable() && entity.isAlive() && entity != source.getCaster(), source.getBoundingBox().inflate((double)range, (double)range, (double)range), range);
    }

    protected float getRange(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        return modificationData.getValue(Attribute.RANGE);
    }

    @Override
    public float initialComplexity() {
        return 5.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }
}

