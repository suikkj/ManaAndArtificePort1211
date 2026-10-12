/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.network.NetworkDirection
 *  org.joml.Vector3f
 *  top.theillusivec4.curios.api.SlotContext
 */
package com.mna.network;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.parts.Modifier;
import com.mna.blocks.tileentities.ParticleEmitterTile;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.blocks.tileentities.wizard_lab.RunescribingTableTile;
import com.mna.entities.constructs.animated.Construct;
import com.mna.gui.containers.item.ContainerRoteBook;
import com.mna.network.CurioCastSlotType;
import com.mna.network.MAPacketHandler;
import com.mna.network.messages.to_server.AdjustAreaMessage;
import com.mna.network.messages.to_server.AnimatedConstructDropItemMessage;
import com.mna.network.messages.to_server.AnimatedConstructSyncRequestMessage;
import com.mna.network.messages.to_server.AuraSyncMessageToServer;
import com.mna.network.messages.to_server.CantripPatternUpdateMessage;
import com.mna.network.messages.to_server.ConstructHornMessage;
import com.mna.network.messages.to_server.CurioCastMessage;
import com.mna.network.messages.to_server.EndControlEffectEarlyMessage;
import com.mna.network.messages.to_server.EnderDiscIndexSetMessage;
import com.mna.network.messages.to_server.EnderDiscPatternSetMessage;
import com.mna.network.messages.to_server.InscriptionTableAttributeChangeMessage;
import com.mna.network.messages.to_server.InscriptionTableRequestStartCraftingMessage;
import com.mna.network.messages.to_server.InscriptionTableSetComponentMessage;
import com.mna.network.messages.to_server.InscriptionTableSetModifierMessage;
import com.mna.network.messages.to_server.InscriptionTableSetShapeMessage;
import com.mna.network.messages.to_server.LodestarLogicSetMessage;
import com.mna.network.messages.to_server.MAPFXSyncRequestMessage;
import com.mna.network.messages.to_server.MagiciansWorkbenchClearMessage;
import com.mna.network.messages.to_server.MagiciansWorkbenchRecipeSetMessage;
import com.mna.network.messages.to_server.ManaweavePatternDrawnMessage;
import com.mna.network.messages.to_server.ManaweaveWandSlotChangeMessage;
import com.mna.network.messages.to_server.MultiblockSyncRequestMessage;
import com.mna.network.messages.to_server.OpenBeltBagMessage;
import com.mna.network.messages.to_server.OpenSpellRenameMessage;
import com.mna.network.messages.to_server.PatterningPrismPasteMessage;
import com.mna.network.messages.to_server.PlayerBounceMessage;
import com.mna.network.messages.to_server.PlayerFocusDistanceMessage;
import com.mna.network.messages.to_server.PlayerJumpMessage;
import com.mna.network.messages.to_server.PossessionInputMessage;
import com.mna.network.messages.to_server.RadialInventorySlotChangeMessage;
import com.mna.network.messages.to_server.RequestLootTableItems;
import com.mna.network.messages.to_server.RequestWardDataMessage;
import com.mna.network.messages.to_server.RequestWellspringPowerNetworkSyncMessage;
import com.mna.network.messages.to_server.RitualKitIndexSetMessage;
import com.mna.network.messages.to_server.RoteSpellsSyncMessageToServer;
import com.mna.network.messages.to_server.RunescribingTableMutexChangeMessage;
import com.mna.network.messages.to_server.SelectedModifierMessage;
import com.mna.network.messages.to_server.SendCodexEntryReadMessage;
import com.mna.network.messages.to_server.SpellAdjustmentsMessage;
import com.mna.network.messages.to_server.SpellBookSlotChangeMessage;
import com.mna.network.messages.to_server.SpellNameAndIconMessage;
import com.mna.network.messages.to_server.TradeSelectedMessage;
import com.mna.network.messages.to_server.UIModifierPress;
import com.mna.network.messages.to_server.UpdateCodexUnlocksMessage;
import com.mna.network.messages.to_server.WizardLabSelectSpellComponentMessage;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.NBTUtilities;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.SlotContext;

public class ClientMessageDispatcher {
    public static void sendInscriptionTableAttributeChange(InscriptionTableTile te, Attribute attr, float newVal, InscriptionTableAttributeChangeMessage.ChangeType type) {
        if (te.hasLevel() && te.getLevel().isClientSide) {
            PacketDistributor.sendToServer(new InscriptionTableAttributeChangeMessage(te.getBlockPos(), attr, newVal, type));
        }
    }

    public static void sendInscriptionTableSetShape(InscriptionTableTile te) {
        if (te.hasLevel() && te.getLevel().isClientSide) {
            PacketDistributor.sendToServer(InscriptionTableSetShapeMessage.fromInscriptionTable(te));
        }
    }

    public static void requestLootTableDrops(ResourceLocation lootTable) {
        PacketDistributor.sendToServer(new RequestLootTableItems(lootTable));
    }

    public static void sendInscriptionTableSetComponent(InscriptionTableTile te) {
        if (te.hasLevel() && te.getLevel().isClientSide) {
            PacketDistributor.sendToServer(InscriptionTableSetComponentMessage.fromInscriptionTable(te));
        }
    }

    public static void sendInscriptionTableSetModifier(InscriptionTableTile te, ResourceLocation modifier, int index) {
        if (te.hasLevel() && te.getLevel().isClientSide) {
            PacketDistributor.sendToServer(new InscriptionTableSetModifierMessage(te.getBlockPos(), modifier, index));
        }
    }

    public static void sendInscriptionTableRequestStartCrafting(InscriptionTableTile te) {
        if (te.hasLevel() && te.getLevel().isClientSide) {
            PacketDistributor.sendToServer(InscriptionTableRequestStartCraftingMessage.fromInscriptionTable(te));
        }
    }

    public static void sendManaweavePatternDrawn(Player player, ResourceLocation patternID, Vec3 position, InteractionHand hand, int ticksDrawn) {
        if (player.level().isClientSide()) {
            PacketDistributor.sendToServer(new ManaweavePatternDrawnMessage(patternID, position, hand, ticksDrawn));
        }
    }

    public static void sendRunescribingMutexChange(RunescribingTableTile table, long hMutex, long vMutex, int tier, boolean isDelete) {
        if (table.hasLevel() && table.getLevel().isClientSide) {
            PacketDistributor.sendToServer(new RunescribingTableMutexChangeMessage(table.getBlockPos(), hMutex, vMutex, tier, isDelete));
        }
    }

    public static void sendSpellBookSlotChange(int selectedSlot, InteractionHand hand, @Nullable SlotContext curioContext) {
        PacketDistributor.sendToServer(new SpellBookSlotChangeMessage(selectedSlot, hand, curioContext));
    }

    public static void sendPlayerFocusDistanceChange(float delta, float maximum) {
        PacketDistributor.sendToServer(new PlayerFocusDistanceMessage(delta, maximum));
    }

    public static void sendRadialInventorySlotChange(int slot, boolean offhand) {
        PacketDistributor.sendToServer(new RadialInventorySlotChangeMessage(slot, offhand));
    }

    public static void sendManaweaveWandSlotChange(ResourceLocation selected, boolean offhand) {
        PacketDistributor.sendToServer(new ManaweaveWandSlotChangeMessage(selected, offhand));
    }

    public static void sendEnderDiscPatternChange(ArrayList<ResourceLocation> patterns, ResourceLocation dimension, int patternIndex, String name) {
        PacketDistributor.sendToServer(new EnderDiscPatternSetMessage(patterns, dimension, patternIndex, name));
    }

    public static void sendEnderDiscIndexChange(int patternIndex, boolean offhand) {
        PacketDistributor.sendToServer(new EnderDiscIndexSetMessage(patternIndex, offhand));
    }

    public static void sendRitualKitIndexChange(int patternIndex, boolean offhand) {
        PacketDistributor.sendToServer(new RitualKitIndexSetMessage(patternIndex, offhand));
    }

    public static void sendSpellCustomizationValues(String name, int iconIndex, InteractionHand hand) {
        PacketDistributor.sendToServer(new SpellNameAndIconMessage(name, iconIndex, hand));
    }

    public static void sendAnimatedConstructSyncRequestMessage(Construct construct, boolean diagOnly) {
        PacketDistributor.sendToServer(AnimatedConstructSyncRequestMessage.fromConstruct(construct, diagOnly));
    }

    public static void sendAnimatedConstructDropItemMessage(Construct construct, int slot) {
        PacketDistributor.sendToServer(AnimatedConstructDropItemMessage.fromConstruct(construct, slot));
    }

    public static void sendRequestWellspringNetworkSyncMessage(boolean nearbyNodes) {
        Minecraft mc = Minecraft.getInstance();
        PacketDistributor.sendToServer(new RequestWellspringPowerNetworkSyncMessage(nearbyNodes));
    }

    public static boolean sendMAPFXSyncRequestMessage(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null || mc.getConnection().getConnection() == null) {
            return false;
        }
        PacketDistributor.sendToServer(new MAPFXSyncRequestMessage(entity.getId(), false));
        return true;
    }

    public static boolean sendAuraXSyncRequestMessage(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null || mc.getConnection().getConnection() == null) {
            return false;
        }
        PacketDistributor.sendToServer(MAPFXSyncRequestMessage.forAura(player));
        return true;
    }

    public static boolean sendMultiblockSyncRequestMessage(LivingEntity entity, ResourceLocation recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null || mc.getConnection().getConnection() == null) {
            return false;
        }
        PacketDistributor.sendToServer(new MultiblockSyncRequestMessage(entity.getId(), recipe));
        return true;
    }

    public static void sendRoteSpellsUpdate(ContainerRoteBook container) {
        PacketDistributor.sendToServer(RoteSpellsSyncMessageToServer.fromRoteBookContainer(container));
    }

    public static void sendPlayerBounce(Vec3 velocity) {
        PacketDistributor.sendToServer(new PlayerBounceMessage(velocity));
    }

    public static void sendPlayerJump() {
        PacketDistributor.sendToServer(new PlayerJumpMessage());
    }

    public static void sendCantripUpdateMessage(IPlayerMagic magic) {
        PacketDistributor.sendToServer(CantripPatternUpdateMessage.fromCapability(magic));
    }

    public static void sendItemUIOpenMessage(boolean pressed) {
        PacketDistributor.sendToServer(new UIModifierPress(pressed));
    }

    public static void sendRequestOpenSpellNameAndIconGUI() {
        PacketDistributor.sendToServer(new OpenSpellRenameMessage());
    }

    public static void sendSpellAdjustmentMessage(SpellRecipe recipe, InteractionHand hand) {
        CompoundTag nbt = new CompoundTag();
        recipe.writeToNBT(nbt);
        PacketDistributor.sendToServer(new SpellAdjustmentsMessage(nbt, hand));
    }

    public static void sendSelectedModifierMessage(Modifier m, boolean offhand) {
        PacketDistributor.sendToServer(new SelectedModifierMessage(m.getRegistryName(), offhand));
    }

    public static void dispatchConstructHorn(int entityID) {
        PacketDistributor.sendToServer(new ConstructHornMessage(entityID));
    }

    public static void sendTradeSelected(int index) {
        PacketDistributor.sendToServer(new TradeSelectedMessage(index));
    }

    public static void sendPosessionMovementInput(float forward, float strafe, boolean jump, boolean sneak, float yaw, float yawHead, float pitch) {
        PacketDistributor.sendToServer(PossessionInputMessage.movement(forward, strafe, jump, sneak, yaw, yawHead, pitch));
    }

    public static void sendPosessionClickInput() {
        PacketDistributor.sendToServer(PossessionInputMessage.click());
    }

    public static void sendRequestEndControlEffectEarlyMessage() {
        PacketDistributor.sendToServer(new EndControlEffectEarlyMessage());
    }

    public static void sendMagiciansWorkbenchRecipeSetMessage(int index) {
        PacketDistributor.sendToServer(new MagiciansWorkbenchRecipeSetMessage(index));
    }

    public static void sendMagiciansWorkbenchClearMessage(boolean second) {
        PacketDistributor.sendToServer(new MagiciansWorkbenchClearMessage(second));
    }

    public static void sendLodestarLogicMessage(BlockPos pos, CompoundTag logic) {
        byte[] bytes = NBTUtilities.toBytes(logic);
        int numMessages = (int)Math.ceil((float)bytes.length / 30000.0f);
        for (int i = 0; i < numMessages; ++i) {
            int startIndex = i * 30000;
            int bufferSize = Math.min(30000, bytes.length - startIndex);
            byte[] buf = new byte[bufferSize];
            System.arraycopy(bytes, startIndex, buf, 0, bufferSize);
            PacketDistributor.sendToServer(new LodestarLogicSetMessage(pos, i, numMessages, bytes.length, buf));
        }
    }

    public static void sendPatterningPrismCopyMessage(CompoundTag logic, InteractionHand hand, boolean isParticleEmitter) {
        PacketDistributor.sendToServer(new PatterningPrismPasteMessage(logic, hand, isParticleEmitter));
    }

    public static void sendAuraSyncMessage() {
        Minecraft m = Minecraft.getInstance();
        PacketDistributor.sendToServer(AuraSyncMessageToServer.fromPlayer((Player)m.player));
    }

    public static void sendAuraSyncMessage(ParticleEmitterTile tile) {
        PacketDistributor.sendToServer(AuraSyncMessageToServer.fromTile(tile));
    }

    public static void sendWizardLabSelectSpellComponentMessage(ResourceLocation selected) {
        PacketDistributor.sendToServer(new WizardLabSelectSpellComponentMessage(selected));
    }

    public static void sendOpenBeltSlotMessage() {
        PacketDistributor.sendToServer(new OpenBeltBagMessage());
    }

    public static void sendCurioCastMessage(CurioCastSlotType type) {
        PacketDistributor.sendToServer(new CurioCastMessage(type));
    }

    public static void sendCodexEntryRead(long hash) {
        PacketDistributor.sendToServer(new SendCodexEntryReadMessage(hash));
    }

    public static void updateCodexEntryUnlocks(long[] hashes) {
        PacketDistributor.sendToServer(new UpdateCodexUnlocksMessage(hashes));
    }

    public static void sendAreaAdjust(Vector3f direction, boolean grow) {
        PacketDistributor.sendToServer(new AdjustAreaMessage(direction, grow));
    }

    public static void requestWardData(AABB area) {
        PacketDistributor.sendToServer(new RequestWardDataMessage(area));
    }
}


