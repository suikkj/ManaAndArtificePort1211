/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.shapes;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.shapes.ShapeRaytrace;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShapeBolt
extends ShapeRaytrace {
    public ShapeBolt(float range, float maxRange, ResourceLocation icon) {
        super(range, maxRange, icon);
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        List<SpellTarget> tgts = super.Target(source, world, modificationData, recipe);
        SpellTarget tgt = tgts.get(0);
        if (!world.isClientSide && tgt != SpellTarget.NONE) {
            Vec3 offset = Vec3.ZERO;
            if (source.isPlayerCaster()) {
                offset = source.getForward();
            }
            int color = recipe.getParticleColorOverride() == -1 ? 0xFF000000 | recipe.getHighestAffinity().getColor()[0] << 16 | recipe.getHighestAffinity().getColor()[1] << 8 | recipe.getHighestAffinity().getColor()[2] : recipe.getParticleColorOverride();
            ServerMessageDispatcher.sendParticleSpawn(source.getOrigin().x() + offset.x, source.isPlayerCaster() ? source.getOrigin().y() - 0.5 + offset.y : source.getOrigin().y() + offset.y, source.getOrigin().z() + offset.z, tgt.getPosition().x, tgt.isEntity() ? tgt.getPosition().y + (double)tgt.getEntity().getEyeHeight() : tgt.getPosition().y, tgt.getPosition().z, color, 64.0f, (ResourceKey<Level>)world.dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
        }
        return tgts;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }
}

