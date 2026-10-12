/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.spells.config;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellComponent;
import net.minecraft.resources.ResourceLocation;

public interface ISpellConfigHelper {
    public void initForPart(ISpellComponent var1, AttributeValuePair ... var2);

    public boolean isPartInitialized(ISpellComponent var1);

    public boolean isDimensionBlacklisted(ISpellComponent var1, ResourceLocation var2);

    public boolean isBiomeBlacklisted(ISpellComponent var1, ResourceLocation var2);

    public float getConfiguredValue(ISpellComponent var1, Attribute var2, Value var3, float var4);

    public static enum Value {
        MINIMUM,
        MAXIMUM,
        DEFAULT,
        STEP,
        COMPLEXITY;

    }
}

