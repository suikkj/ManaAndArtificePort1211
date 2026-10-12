/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.entities.construct;

import com.mna.api.entities.construct.ConstructMaterial;
import java.util.function.Predicate;

public interface IConstructModelRegistrationHelper {
    public void register(ConstructMaterial var1, String var2, Predicate<String> var3, Predicate<String> var4, Predicate<String> var5, Predicate<String> var6);
}

