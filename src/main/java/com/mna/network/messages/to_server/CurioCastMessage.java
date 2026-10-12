 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.spells.ICanContainSpell;
import com.mna.network.CurioCastSlotType;
import com.mna.network.messages.BaseServerMessage;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.bus.api.Event;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CurioCastMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CurioCastMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("curio_cast_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CurioCastMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> CurioCastMessage.encode(v, b), CurioCastMessage::decode);

    @Override
    public CustomPacketPayload.Type<CurioCastMessage> type() { return TYPE; }

    private CurioCastSlotType slotType;

    public CurioCastMessage() {
        this.messageIsValid = false;
    }

    public CurioCastMessage(CurioCastSlotType slotType) {
        this.slotType = slotType;
        this.messageIsValid = true;
    }

    public static CurioCastMessage decode(FriendlyByteBuf buf) {
        CurioCastMessage msg = new CurioCastMessage();
        try {
            int ordinal = buf.readInt();
            msg.slotType = CurioCastSlotType.values()[ordinal % CurioCastSlotType.values().length];
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading CurioCastMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(CurioCastMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slotType.ordinal());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }
        CuriosApi.getCuriosInventory((LivingEntity)player).ifPresent(curioInventory -> {
            List slots = curioInventory.findCurios(this.slotType.slotIdentifiers);
            for (int i = 0; i < slots.size(); ++i) {
                ItemStack stack = ((SlotResult)slots.get(i)).stack();
                Item patt1806$temp = stack.getItem();
                if (!(patt1806$temp instanceof ICanContainSpell)) continue;
                ICanContainSpell iSpell = (ICanContainSpell)patt1806$temp;
                LivingEntityUseItemEvent.Start event = new LivingEntityUseItemEvent.Start((LivingEntity)player, stack, 0);
                NeoForge.EVENT_BUS.post(event);
                if (!event.isCanceled() && iSpell.castFromCurioSlot(stack, (Player)player)) break;
            }
        });
    }
}


