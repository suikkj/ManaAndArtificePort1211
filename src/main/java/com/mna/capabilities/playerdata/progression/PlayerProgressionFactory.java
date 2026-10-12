/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.playerdata.progression;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import java.util.concurrent.Callable;

public class PlayerProgressionFactory
implements Callable<IPlayerProgression> {
    @Override
    public IPlayerProgression call() throws Exception {
        return new PlayerProgression();
    }
}

