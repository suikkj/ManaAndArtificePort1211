/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package com.mna.api.spells.attributes;

import com.google.common.collect.ImmutableList;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import java.util.Collection;

public class ModifiedSpellAttributes {
    private final ImmutableList<AttributeValuePair> attributeValues;

    public ModifiedSpellAttributes(Collection<AttributeValuePair> modified) {
        this.attributeValues = ImmutableList.copyOf(modified);
    }

    public float GetModifiedAttribute(Attribute attribute, float defaultValue) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getValue();
        }
        return defaultValue;
    }
}

