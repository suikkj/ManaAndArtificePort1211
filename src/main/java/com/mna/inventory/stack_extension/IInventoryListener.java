/*
 * Decompiled with CFR 0.152.
 */
package com.mna.inventory.stack_extension;

import com.mna.gui.containers.IExtendedItemHandler;

@FunctionalInterface
public interface IInventoryListener {
    public void inventoryChanged(IExtendedItemHandler var1, int var2);
}

