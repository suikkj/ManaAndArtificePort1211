/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.phys.AABB
 */
package com.mna.api.entities.construct.ai.parameter;

import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructParameterTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;

public class ConstructTaskAreaParameter
extends ConstructAITaskParameter {
    private AABB area;
    private Pair<DirectionalPoint, DirectionalPoint> points;

    public ConstructTaskAreaParameter(String id) {
        super(id, ConstructParameterTypes.AREA);
    }

    public AABB getArea() {
        return this.area;
    }

    public Pair<DirectionalPoint, DirectionalPoint> getPoints() {
        return this.points;
    }

    public void setPoints(DirectionalPoint firstCorner, DirectionalPoint secondCorner) {
        this.points = new Pair(firstCorner, secondCorner);
        this.createInclusiveAABB();
    }

    private void createInclusiveAABB() {
        if (this.points != null && this.points.getFirst() != null && this.points.getSecond() != null) {
            this.area = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(((DirectionalPoint)this.points.getFirst()).getPosition()), net.minecraft.world.phys.Vec3.atLowerCornerOf(((DirectionalPoint)this.points.getSecond()).getPosition())).expandTowards(1.0, 1.0, 1.0);
        }
    }

    @Override
    public void loadData(CompoundTag nbt) {
        super.loadData(nbt);
        DirectionalPoint first = null;
        DirectionalPoint second = null;
        if (nbt.contains("firstPoint")) {
            first = DirectionalPoint.of(nbt.getCompound("firstPoint"));
        }
        if (nbt.contains("secondPoint")) {
            second = DirectionalPoint.of(nbt.getCompound("secondPoint"));
        }
        this.points = new Pair(first, second);
        if (first != null && second != null) {
            this.createInclusiveAABB();
        }
    }

    @Override
    public CompoundTag saveData() {
        CompoundTag tag = super.saveData();
        if (this.points != null) {
            if (this.points.getFirst() != null) {
                tag.put("firstPoint", (Tag)((DirectionalPoint)this.points.getFirst()).save());
            }
            if (this.points.getSecond() != null) {
                tag.put("secondPoint", (Tag)((DirectionalPoint)this.points.getSecond()).save());
            }
        }
        return tag;
    }
}

