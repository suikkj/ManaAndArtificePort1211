/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.entities.models;

import com.mna.tools.math.Vector3;
import java.util.HashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class ModelPositionData {
    private HashMap<ModelPart, VectorPair> positionData = new HashMap();

    public ModelPositionData addPositionDegrees(ModelPart renderer, Vector3 startRotation, Vector3 endRotation) {
        startRotation = startRotation.scale((float)(-Math.PI) / 180);
        endRotation = endRotation.scale((float)(-Math.PI) / 180);
        this.positionData.put(renderer, new VectorPair(startRotation, endRotation));
        return this;
    }

    public ModelPositionData addPositionDegrees(ModelPart renderer, Vector3 endRotation) {
        Vector3 startRotation = new Vector3(renderer.xRot, renderer.yRot, renderer.zRot);
        endRotation = endRotation.scale((float)(-Math.PI) / 180);
        this.positionData.put(renderer, new VectorPair(startRotation, endRotation));
        return this;
    }

    public ModelPositionData addPositionDegrees(ModelPart renderer) {
        Vector3 startRotation = new Vector3(renderer.xRot, renderer.yRot, renderer.zRot);
        this.positionData.put(renderer, new VectorPair(startRotation, startRotation));
        return this;
    }

    public void startLerp() {
        this.positionData.forEach((renderer, pair) -> pair.setStart(new Vector3(renderer.xRot, renderer.yRot, renderer.zRot)));
    }

    public void lerpRotations(float t) {
        this.positionData.forEach((renderer, pair) -> {
            Vector3 interp = pair.lerp(t);
            renderer.xRot = interp.x;
            renderer.yRot = interp.y;
            renderer.zRot = interp.z;
        });
    }

    private class VectorPair {
        private Vector3 start;
        private final Vector3 end;

        public VectorPair(Vector3 start, Vector3 end) {
            this.start = start;
            this.end = end;
        }

        public void setStart(Vector3 start) {
            this.start = start;
        }

        public Vector3 lerp(float t) {
            return Vector3.lerp(this.start, this.end, t);
        }
    }
}


