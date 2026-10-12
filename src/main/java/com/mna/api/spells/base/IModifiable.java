/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package com.mna.api.spells.base;

import com.google.common.collect.ImmutableList;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellComponent;

public interface IModifiable<T extends ISpellComponent> {
    public ImmutableList<AttributeValuePair> getModifiableAttributes();
}

