/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 */
package com.mna.items.ritual;

import com.mna.api.affinity.Affinity;
import com.mna.api.items.TieredItem;
import net.minecraft.world.item.Item;

public class Mote
extends TieredItem {
    private final Affinity relatedAffinity;

    public Mote(Affinity related, Item.Properties itemProperties) {
        super(itemProperties);
        this.relatedAffinity = related;
    }

    public Affinity getRelatedAffinity() {
        return this.relatedAffinity;
    }
}

