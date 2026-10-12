/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.capabilities;

public class CodexBreadcrumb {
    public final String Key;
    public final Type breadcrumbType;
    public final int Page;
    public final String[] Metadata;

    public CodexBreadcrumb(Type type, String Key2, int Page, String ... metadata) {
        this.Key = Key2;
        this.breadcrumbType = type;
        this.Page = Page;
        this.Metadata = metadata;
    }

    public static enum Type {
        ENTRY,
        RECIPE,
        SEARCH,
        CATEGORY,
        INDEX;

    }
}

