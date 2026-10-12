/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.blocks.tile;

public enum PowerStatus {
    SUPPLIED(true),
    PARTIAL(true),
    NO_POWER(false),
    NO_CONDUIT(false),
    NOT_REQUESTING(false);

    boolean allow_machine_operation = false;

    private PowerStatus(boolean allow_machine_operation) {
        this.allow_machine_operation = allow_machine_operation;
    }

    public boolean allowMachineOperation() {
        return this.allow_machine_operation;
    }
}

