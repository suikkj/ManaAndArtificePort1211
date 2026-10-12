/*
 * Decompiled with CFR 0.152.
 */
package com.mna.network;

public enum CurioCastSlotType {
    SPELL("spell"),
    BACK("back"),
    BELT("belt"),
    FIRST_FOUND("spell", "back", "belt");

    public final String[] slotIdentifiers;

    private CurioCastSlotType(String ... slotIdentifier) {
        this.slotIdentifiers = slotIdentifier;
    }
}

