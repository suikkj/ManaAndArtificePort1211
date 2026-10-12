/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 */
package com.mna.api.entities.construct.ai.parameter;

import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructParameterTypes;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ConstructTaskPointParameter
extends ConstructAITaskParameter {
    private DirectionalPoint point;

    public ConstructTaskPointParameter(String id) {
        super(id, ConstructParameterTypes.POINT);
    }

    @Nullable
    public BlockPos getPosition() {
        if (this.point == null) {
            return null;
        }
        return this.point.getPosition();
    }

    @Nullable
    public Direction getDirection() {
        if (this.point == null) {
            return null;
        }
        return this.point.getDirection();
    }

    @Nullable
    public String getOriginalBlockName() {
        if (this.point == null) {
            return null;
        }
        return this.point.getBlockName();
    }

    @Nullable
    public String getOriginalBlockID() {
        if (this.point == null) {
            return null;
        }
        return this.point.getBlockID();
    }

    @Nullable
    public DirectionalPoint getPoint() {
        return this.point;
    }

    public void setPoint(DirectionalPoint point) {
        this.point = new DirectionalPoint(point.getPosition(), point.getDirection(), point.getBlockName(), point.getBlockID());
    }

    @Override
    public void loadData(CompoundTag nbt) {
        super.loadData(nbt);
        if (nbt.contains("point")) {
            this.point = DirectionalPoint.of(nbt.getCompound("point"));
        }
    }

    @Override
    public CompoundTag saveData() {
        CompoundTag tag = super.saveData();
        if (this.point != null) {
            tag.put("point", (Tag)this.point.save());
        }
        return tag;
    }
}

