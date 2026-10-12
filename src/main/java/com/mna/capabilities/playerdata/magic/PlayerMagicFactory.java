/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.playerdata.magic;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagic;
import java.util.concurrent.Callable;

public final class PlayerMagicFactory
implements Callable<IPlayerMagic> {
    @Override
    public IPlayerMagic call() throws Exception {
        return new PlayerMagic();
    }
}

