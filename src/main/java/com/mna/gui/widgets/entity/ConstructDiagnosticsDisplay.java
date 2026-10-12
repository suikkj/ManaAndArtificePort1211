/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.FastColor$ARGB32
 */
package com.mna.gui.widgets.entity;

import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.constructs.animated.ConstructDiagnostics;
import com.mna.gui.GuiTextures;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;

public class ConstructDiagnosticsDisplay
extends AbstractWidget {
    private Construct construct;
    private LinkedList<ConstructDiagnostics.LogEntry> lastDiagnostics;
    private Font font;

    public ConstructDiagnosticsDisplay(int pX, int pY, Construct construct) {
        super(pX, pY, 142, 231, (Component)Component.literal((String)""));
        this.construct = construct;
        Minecraft mc = Minecraft.getInstance();
        this.font = mc.font;
    }

    public boolean isValid() {
        return this.construct != null;
    }

    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.construct == null) {
            return;
        }
        if (this.construct.isAlive()) {
            this.lastDiagnostics = (LinkedList)this.construct.getDiagnostics().getMessages().clone();
        } else {
            ConstructDiagnostics.LogEntry iLoveYou = new ConstructDiagnostics.LogEntry(Component.translatable((String)"mna.constructs.feedback.death").getString(), null);
            ConstructDiagnostics.LogEntry lastLogLine = this.lastDiagnostics.peekLast();
            if (lastLogLine == null || !lastLogLine.equals(iLoveYou)) {
                this.lastDiagnostics.add(iLoveYou);
            }
        }
        pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, this.getX(), this.getY(), 0, 0, 143, 207);
        int zebraCount = 0;
        int textXOffset = 16;
        int xPos = this.getX();
        int yPos = this.getY() + 4;
        Iterator<ConstructDiagnostics.LogEntry> diagnosticsIterator = this.lastDiagnostics.descendingIterator();
        while (diagnosticsIterator.hasNext()) {
            ConstructDiagnostics.LogEntry s = diagnosticsIterator.next();
            if (s.icon != null) {
                pGuiGraphics.blit(s.icon, this.getX() + 2, yPos, 0.0f, 0.0f, 16, 16, 16, 16);
            }
            for (FormattedText props : this.font.getSplitter().splitLines((FormattedText)Component.literal((String)s.message), 135 - textXOffset, Style.EMPTY)) {
                pGuiGraphics.drawString(this.font, props.getString(), this.getX() + textXOffset + 4, yPos, zebraCount % 2 == 0 ? FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255) : FastColor.ARGB32.color((int)255, (int)100, (int)100, (int)100));
                Objects.requireNonNull(this.font);
                if ((yPos += 9) <= this.getY() + 195) continue;
                break;
            }
            ++zebraCount;
            if ((yPos += 3) <= this.getY() + 195) continue;
            break;
        }
        int statusIconOffset = 14;
        int taskXStep = 24;
        xPos = this.getX();
        yPos = this.getY() - 24;
        Iterator<ConstructDiagnostics.TaskHistoryEntry> taskHistoryIterator = this.construct.getDiagnostics().getTaskHistory().descendingIterator();
        while (taskHistoryIterator.hasNext()) {
            ConstructDiagnostics.TaskHistoryEntry e = taskHistoryIterator.next();
            pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, xPos, yPos, 0, 217, 20, 20);
            if (e.getIcon() != null) {
                pGuiGraphics.blit(e.getIcon(), xPos + 2, yPos + 2, 0.0f, 0.0f, 16, 16, 16, 16);
            }
            switch (e.getStatus()) {
                case SUCCESS: {
                    pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, xPos + statusIconOffset, yPos + statusIconOffset, 0, 237, 8, 8);
                    break;
                }
                case FAILURE: {
                    pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, xPos + statusIconOffset, yPos + statusIconOffset, 8, 237, 8, 8);
                    break;
                }
                case RUNNING: {
                    pGuiGraphics.blit(GuiTextures.Entities.CONSTRUCT_DIAGNOSTICS, xPos + statusIconOffset, yPos + statusIconOffset, 16, 237, 8, 8);
                }
            }
            xPos += taskXStep;
        }
    }

    public void setConstruct(Construct construct) {
        this.construct = construct;
    }

    public Construct getConstruct() {
        return this.construct;
    }

    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}

