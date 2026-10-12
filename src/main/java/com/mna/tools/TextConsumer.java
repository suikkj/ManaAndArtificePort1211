/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.FormattedCharSink
 */
package com.mna.tools;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;

public class TextConsumer
implements FormattedCharSink {
    private StringBuilder output = new StringBuilder();

    public boolean accept(int index, Style p_accept_2_, int charCode) {
        this.output.append((char)charCode);
        return true;
    }

    public String getString() {
        return this.output.toString();
    }
}

