/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.FormattedCharSink
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.guide.sections;

import com.google.gson.JsonObject;
import com.mna.guide.interfaces.IEntrySection;
import com.mna.guide.sections.TextSection;
import com.mna.tools.TextConsumer;
import java.util.Collection;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class TitleSection
extends TextSection {
    public TitleSection() {
        this.CENTER = true;
        this.SCALE_FACTOR = 1.5f;
    }

    @Override
    public Collection<IEntrySection> parse(JsonObject element, int startY, int maxHeight, int maxWidth, int page) {
        if (startY >= maxHeight - 30) {
            ++page;
            startY = 10;
        }
        return super.parse(element, startY, maxHeight, maxWidth, page);
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (FormattedCharSequence p : this.lines) {
            TextConsumer tc = new TextConsumer();
            p.accept((FormattedCharSink)tc);
            sb.append(tc.getString() + " ");
        }
        return sb.toString();
    }
}


