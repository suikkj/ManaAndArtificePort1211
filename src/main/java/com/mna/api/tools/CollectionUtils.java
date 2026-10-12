/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.tools;

import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import java.util.Collection;
import java.util.Optional;

public class CollectionUtils {
    public static <T> Optional<T> getRandom(Collection<T> collection) {
        if (collection == null) {
            return Optional.empty();
        }
        return collection.stream().skip((int)((double)collection.size() * Math.random())).findFirst();
    }

    public static boolean componentMatchesShapeAndTag(Shape shape, SpellEffect component, SpellPartTags tag) {
        if (shape.isChanneled() && !component.canBeChanneled()) {
            return false;
        }
        switch (tag) {
            case FRIENDLY: {
                return component.getUseTag() == SpellPartTags.FRIENDLY || component.getUseTag() == SpellPartTags.NEUTRAL;
            }
            case HARMFUL: {
                return component.getUseTag() == SpellPartTags.HARMFUL || component.getUseTag() == SpellPartTags.NEUTRAL;
            }
            case NEUTRAL: {
                return true;
            }
            case SELF: {
                return component.getUseTag() == SpellPartTags.SELF || component.getUseTag() == SpellPartTags.FRIENDLY;
            }
            case UTILITY: {
                return component.getUseTag() == SpellPartTags.UTILITY;
            }
        }
        return false;
    }
}

