package com.mna.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Field;
import java.util.Deque;

/**
 * [PORT 1.21.1] PoseStack.poseStack (Deque interno) virou private.
 * Acesso por reflexao (usado pelo ConstructRenderer p/ snapshot de pose).
 */
public final class MNAPoseStackAccess {
    private static final Field FIELD = field();

    private MNAPoseStackAccess() {
    }

    private static Field field() {
        try {
            Field f = PoseStack.class.getDeclaredField("poseStack");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[MNA] campo nao encontrado: PoseStack.poseStack", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void add(PoseStack stack, PoseStack.Pose pose) {
        try {
            ((Deque<PoseStack.Pose>)FIELD.get(stack)).add(pose);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
