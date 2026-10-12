 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.blocks.tileentities.wizard_lab.ISelectSpellComponents;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WizardLabSelectSpellComponentMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WizardLabSelectSpellComponentMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("wizard_lab_select_spell_component_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, WizardLabSelectSpellComponentMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> WizardLabSelectSpellComponentMessage.encode(v, b), WizardLabSelectSpellComponentMessage::decode);

    @Override
    public CustomPacketPayload.Type<WizardLabSelectSpellComponentMessage> type() { return TYPE; }

    private ResourceLocation spellComponent;

    public WizardLabSelectSpellComponentMessage() {
        this.messageIsValid = false;
    }

    public WizardLabSelectSpellComponentMessage(ResourceLocation spellComponent) {
        this.spellComponent = spellComponent;
        this.messageIsValid = true;
    }

    public final ResourceLocation getSpellComponent() {
        return this.spellComponent;
    }

    public static final WizardLabSelectSpellComponentMessage decode(FriendlyByteBuf buf) {
        WizardLabSelectSpellComponentMessage msg = new WizardLabSelectSpellComponentMessage();
        try {
            msg.spellComponent = buf.readResourceLocation();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading WizardLabSelectSpellComponentMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(WizardLabSelectSpellComponentMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.spellComponent);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu;
        if (player.containerMenu != null && (abstractContainerMenu = player.containerMenu) instanceof ISelectSpellComponents) {
            ISelectSpellComponents componentMenu = (ISelectSpellComponents)abstractContainerMenu;
            ResourceLocation comp = this.getSpellComponent();
            ISpellComponent resolved = null;
            if (Registries.Shape.containsKey(comp)) {
                resolved = (ISpellComponent)Registries.Shape.getValue(comp);
            } else if (Registries.Modifier.containsKey(comp)) {
                resolved = (ISpellComponent)Registries.Modifier.getValue(comp);
            } else if (Registries.SpellEffect.containsKey(comp)) {
                resolved = (ISpellComponent)Registries.SpellEffect.getValue(comp);
            }
            if (resolved != null) {
                componentMenu.setSpellComponent(resolved);
            }
        }
    }
}

