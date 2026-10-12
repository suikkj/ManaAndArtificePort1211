/*
 * Decompiled with CFR 0.152.
 */
package com.mna.rituals;

import com.mna.api.rituals.RitualBlockPos;
import java.util.Comparator;

public class RitualBlockPosComparator
implements Comparator<RitualBlockPos> {
    private boolean compareDisplay = false;

    @Override
    public int compare(RitualBlockPos o1, RitualBlockPos o2) {
        if (this.compareDisplay) {
            if (o1.getDisplayIndex() < o2.getDisplayIndex()) {
                return -1;
            }
            if (o1.getDisplayIndex() == o2.getDisplayIndex()) {
                return 0;
            }
            return 1;
        }
        if (o1.getIndex() < o2.getIndex()) {
            return -1;
        }
        if (o1.getIndex() == o2.getIndex()) {
            return 0;
        }
        return 1;
    }

    public void setCompareDisplay() {
        this.compareDisplay = true;
    }

    public void setCompareIndex() {
        this.compareDisplay = false;
    }
}

