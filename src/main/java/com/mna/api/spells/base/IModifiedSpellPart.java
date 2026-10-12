/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package com.mna.api.spells.base;

import com.google.common.collect.ImmutableList;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiable;
import com.mna.api.spells.base.ISpellComponent;

public interface IModifiedSpellPart<T extends ISpellComponent & IModifiable<T>> {
    public T getPart();

    public ImmutableList<Attribute> getContainedAttributes();

    public float getValue(Attribute var1);

    public float getValueWithoutMultipliers(Attribute var1);

    public float getMultiplier(Attribute var1);

    public boolean setValue(Attribute var1, float var2);

    public boolean setMultiplier(Attribute var1, float var2);

    public boolean resetMultiplier(Attribute var1);

    public float getDefaultValue(Attribute var1);

    public float getStep(Attribute var1);

    public float getStepComplexity(Attribute var1);

    public void resetValueToDefault(Attribute var1);

    public float stepUp(Attribute var1, float var2);

    public float stepDown(Attribute var1, float var2);

    public float getMinimumValue(Attribute var1);

    public float getMaximumValue(Attribute var1);

    public float stepUpIgnoreMax(Attribute var1);

    public boolean isSame(IModifiedSpellPart<T> var1);
}

