package com.mna.tools;

import java.lang.reflect.Field;
import java.util.Set;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

/**
 * [PORT 1.21.1] {@code com.mna.tools.MNAGoalSelectorAccess.availableGoals(GoalSelector)} virou {@code private}.
 * Acesso por reflexao.
 */
public final class MNAGoalSelectorAccess {
    private static final Field FIELD = field();

    private MNAGoalSelectorAccess() {
    }

    private static Field field() {
        try {
            Field f = GoalSelector.class.getDeclaredField("availableGoals");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[MNA] campo nao encontrado: com.mna.tools.MNAGoalSelectorAccess.availableGoals(GoalSelector)", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<WrappedGoal> availableGoals(GoalSelector selector) {
        try {
            return (Set<WrappedGoal>)FIELD.get(selector);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
