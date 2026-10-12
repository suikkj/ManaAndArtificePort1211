/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.playerdata.rote;

import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpells;
import java.util.concurrent.Callable;

public class PlayerRoteSpellsFactory
implements Callable<IPlayerRoteSpells> {
    @Override
    public IPlayerRoteSpells call() throws Exception {
        return new PlayerRoteSpells();
    }
}

