 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.gui.containers.providers.NamedRitualKit;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.network.messages.BaseServerMessage;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class OpenBeltBagMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenBeltBagMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("open_belt_bag_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, OpenBeltBagMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> OpenBeltBagMessage.encode(v, b), OpenBeltBagMessage::decode);

    @Override
    public CustomPacketPayload.Type<OpenBeltBagMessage> type() { return TYPE; }

    public static OpenBeltBagMessage decode(FriendlyByteBuf buf) {
        OpenBeltBagMessage msg = new OpenBeltBagMessage();
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(OpenBeltBagMessage msg, FriendlyByteBuf buf) {
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        CuriosApi.getCuriosInventory((LivingEntity)player).ifPresent(curioInventory -> {
            List slots = curioInventory.findCurios(new String[]{"belt"});
            for (SlotResult slot : slots) {
                Item patt1024$temp = slot.stack().getItem();
                if (!(patt1024$temp instanceof PractitionersPouch)) continue;
                PractitionersPouch pouch = (PractitionersPouch)patt1024$temp;
                player.openMenu((MenuProvider)new NamedRitualKit(slot.stack(), "belt"), t -> {
                    t.writeBoolean(true);
                    t.writeUtf("belt");
                    t.writeInt(slot.slotContext().index());
                });
                break;
            }
        });
    }
}


