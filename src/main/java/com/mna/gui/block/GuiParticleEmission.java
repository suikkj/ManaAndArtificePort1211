/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.neoforged.neoforge.client.event.ScreenEvent$BackgroundRendered
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 */
package com.mna.gui.block;

import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.particle.ParticleEmissionContainer;
import com.mna.gui.widgets.MASliderNotifiable;
import com.mna.particles.emitter.EmitterData;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public class GuiParticleEmission
extends GuiJEIDisable<ParticleEmissionContainer> {
    private Button enableButton;

    public GuiParticleEmission(ParticleEmissionContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    protected void init() {
        super.init();
        if (((ParticleEmissionContainer)this.menu).isForPlayer() && this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON) {
            this.minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        int yPos = 10;
        int spacing = 12;
        int buttonWidth = 120;
        Minecraft mc = Minecraft.getInstance();
        this.enableButton = Button.builder((Component)Component.literal((String)("Enabled: " + (((ParticleEmissionContainer)this.menu).getData().enabled ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().enabled = !((ParticleEmissionContainer)this.menu).getData().enabled;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Enabled: " + (((ParticleEmissionContainer)this.menu).getData().enabled ? "Yes" : "No"))));
            this.setControlsEnabled(((ParticleEmissionContainer)this.menu).getData().enabled);
        }).pos(mc.screen.width / 2 - buttonWidth / 2, 5).size(buttonWidth, 20).build();
        this.addRenderableWidget(this.enableButton);
        this.addRenderableWidget(new MASliderNotifiable(10, yPos, 120, 10, (Component)Component.literal((String)"Amount: "), (Component)Component.literal((String)""), 1.0, 20.0, ((ParticleEmissionContainer)this.menu).getData().amount, 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().amount = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Rate: "), (Component)Component.literal((String)""), 1.0, 40.0, ((ParticleEmissionContainer)this.menu).getData().rate, 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().rate = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing * 2, 120, 10, (Component)Component.literal((String)"Red: "), (Component)Component.literal((String)""), -1.0, 255.0, ((ParticleEmissionContainer)this.menu).getData().rgbStart[0], 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().rgbStart[0] = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Green: "), (Component)Component.literal((String)""), -1.0, 255.0, ((ParticleEmissionContainer)this.menu).getData().rgbStart[1], 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().rgbStart[1] = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Blue: "), (Component)Component.literal((String)""), -1.0, 255.0, ((ParticleEmissionContainer)this.menu).getData().rgbStart[2], 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().rgbStart[2] = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Alpha: "), (Component)Component.literal((String)""), 30.0, 255.0, ((ParticleEmissionContainer)this.menu).getData().rgbStart[3], 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().rgbStart[3] = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing * 2, 120, 10, (Component)Component.literal((String)"Vel X: "), (Component)Component.literal((String)""), -1.0, 1.0, ((ParticleEmissionContainer)this.menu).getData().velocity[0], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().velocity[0] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Vel Y: "), (Component)Component.literal((String)""), -1.0, 1.0, ((ParticleEmissionContainer)this.menu).getData().velocity[1], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().velocity[1] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Vel Z: "), (Component)Component.literal((String)""), -1.0, 1.0, ((ParticleEmissionContainer)this.menu).getData().velocity[2], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().velocity[2] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing * 2, 120, 10, (Component)Component.literal((String)"Offset X: "), (Component)Component.literal((String)""), -8.0, 8.0, ((ParticleEmissionContainer)this.menu).getData().offset[0], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().offset[0] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Offset Y: "), (Component)Component.literal((String)""), -8.0, 8.0, ((ParticleEmissionContainer)this.menu).getData().offset[1], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().offset[1] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Offset Z: "), (Component)Component.literal((String)""), -8.0, 8.0, ((ParticleEmissionContainer)this.menu).getData().offset[2], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().offset[2] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing * 2, 120, 10, (Component)Component.literal((String)"X Spread: "), (Component)Component.literal((String)""), 0.0, 2.0, ((ParticleEmissionContainer)this.menu).getData().spread[0], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().spread[0] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Y Spread: "), (Component)Component.literal((String)""), 0.0, 2.0, ((ParticleEmissionContainer)this.menu).getData().spread[1], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().spread[1] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Z Spread: "), (Component)Component.literal((String)""), 0.0, 2.0, ((ParticleEmissionContainer)this.menu).getData().spread[2], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().spread[2] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing * 2, 120, 10, (Component)Component.literal((String)"Target X: "), (Component)Component.literal((String)""), -8.0, 8.0, ((ParticleEmissionContainer)this.menu).getData().target[0], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().target[0] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Target Y: "), (Component)Component.literal((String)""), -8.0, 8.0, ((ParticleEmissionContainer)this.menu).getData().target[1], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().target[1] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Target Z: "), (Component)Component.literal((String)""), -8.0, 8.0, ((ParticleEmissionContainer)this.menu).getData().target[2], 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().target[2] = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing * 2, 120, 10, (Component)Component.literal((String)"Scale: "), (Component)Component.literal((String)""), 0.25, 10.0, ((ParticleEmissionContainer)this.menu).getData().scale * 25.0f, 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().scale = (float)s.getValue() / 25.0f;
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Gravity: "), (Component)Component.literal((String)""), 0.0, 1.0, ((ParticleEmissionContainer)this.menu).getData().gravity, 0.0, 4, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().gravity = (float)s.getValue();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        this.addRenderableWidget(new MASliderNotifiable(10, yPos += spacing, 120, 10, (Component)Component.literal((String)"Max Age: "), (Component)Component.literal((String)""), 0.0, 60.0, ((ParticleEmissionContainer)this.menu).getData().age, 1.0, 0, true, s -> {
            ((ParticleEmissionContainer)this.menu).getData().age = s.getValueInt();
            ((ParticleEmissionContainer)this.menu).update(false);
        }));
        yPos += spacing;
        yPos = 10;
        spacing = 22;
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Type: " + ((ParticleEmissionContainer)this.menu).getData().type.name())), b -> {
            int ordinal = (((ParticleEmissionContainer)this.menu).getData().type.ordinal() + 1) % EmitterData.ParticleTypes.values().length;
            ((ParticleEmissionContainer)this.menu).getData().type = EmitterData.ParticleTypes.values()[ordinal];
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Type: " + ((ParticleEmissionContainer)this.menu).getData().type.name())));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Random Color: " + (((ParticleEmissionContainer)this.menu).getData().randomColor ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().randomColor = !((ParticleEmissionContainer)this.menu).getData().randomColor;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Random Color: " + (((ParticleEmissionContainer)this.menu).getData().randomColor ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Random Scale: " + (((ParticleEmissionContainer)this.menu).getData().randomScale ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().randomScale = !((ParticleEmissionContainer)this.menu).getData().randomScale;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Random Scale: " + (((ParticleEmissionContainer)this.menu).getData().randomScale ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Random Target: " + (((ParticleEmissionContainer)this.menu).getData().randomTarget ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().randomTarget = !((ParticleEmissionContainer)this.menu).getData().randomTarget;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Random Target: " + (((ParticleEmissionContainer)this.menu).getData().randomTarget ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Random Speed: " + (((ParticleEmissionContainer)this.menu).getData().randomSpeed ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().randomSpeed = !((ParticleEmissionContainer)this.menu).getData().randomSpeed;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Random Speed: " + (((ParticleEmissionContainer)this.menu).getData().randomSpeed ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Mirror: " + (((ParticleEmissionContainer)this.menu).getData().mirror ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().mirror = !((ParticleEmissionContainer)this.menu).getData().mirror;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Mirror: " + (((ParticleEmissionContainer)this.menu).getData().mirror ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Collision: " + (((ParticleEmissionContainer)this.menu).getData().collision ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().collision = !((ParticleEmissionContainer)this.menu).getData().collision;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Collision: " + (((ParticleEmissionContainer)this.menu).getData().collision ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)("Show in First Person: " + (((ParticleEmissionContainer)this.menu).getData().showInFirstPerson ? "Yes" : "No"))), b -> {
            ((ParticleEmissionContainer)this.menu).getData().showInFirstPerson = !((ParticleEmissionContainer)this.menu).getData().showInFirstPerson;
            ((ParticleEmissionContainer)this.menu).update(false);
            b.setMessage((Component)Component.literal((String)("Show in First Person: " + (((ParticleEmissionContainer)this.menu).getData().showInFirstPerson ? "Yes" : "No"))));
        }).pos(mc.screen.width - 10 - buttonWidth, yPos += spacing).size(buttonWidth, 20).build());
        yPos += spacing;
        this.setControlsEnabled(((ParticleEmissionContainer)this.menu).getData().enabled);
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return this.getFocused() != null && this.isDragging() && pButton == 0 ? this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) : false;
    }

    public void setControlsEnabled(boolean enabled) {
        this.children().forEach(c -> {
            if (c instanceof Button) {
                ((Button)c).active = enabled;
                ((Button)c).visible = enabled;
            }
        });
        this.enableButton.active = true;
        this.enableButton.visible = true;
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        NeoForge.EVENT_BUS.post((Event)new ScreenEvent.BackgroundRendered((Screen)this, pGuiGraphics));
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int p_97809_, int p_97810_) {
    }

    public void onClose() {
        super.onClose();
        ((ParticleEmissionContainer)this.menu).update(true);
    }
}


