 
package com.mna.events;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.KeybindInit;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.items.IAreaItem;
import com.mna.api.items.IPositionalItem;
import com.mna.api.items.IRelic;
import com.mna.api.items.ITieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.effects.EffectHelper;
import com.mna.effects.EffectInit;
import com.mna.effects.interfaces.IDoubleTapEndEarly;
import com.mna.effects.interfaces.IInputDisable;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.entities.constructs.BubbleBoat;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.constructs.animated.ConstructDiagnostics;
import com.mna.events.InputDisabler;
import com.mna.factions.Factions;
import com.mna.gui.GuiTextures;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.gui.radial.BellOfBiddingRadialSelect;
import com.mna.gui.radial.EnderDiscPatternRadialSelect;
import com.mna.gui.radial.ItemstackRadialSelect;
import com.mna.gui.radial.ManaweavingWandRadialSelect;
import com.mna.gui.radial.ModifierRadialSelect;
import com.mna.gui.radial.RitualKitRadialSelect;
import com.mna.gui.radial.SpellRadialSelect;
import com.mna.gui.widgets.PlayerInventoryButton;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.armor.DemonArmorItem;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.items.constructs.BellOfBidding;
import com.mna.items.manaweaving.ItemManaweaverWand;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.sorcery.SpellBook;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.CurioCastSlotType;
import com.mna.network.messages.to_server.PossessionInputMessage;
import com.mna.particles.types.movers.ParticleBezierMover;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.DidYouKnowHelper;
import com.mna.tools.PossessionHelper;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.MultiblockRenderer;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.mna.api.tools.MNACap;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ClientEventHandler {
    private static boolean toolMenuKeyWasDown;
    private static boolean uiModifierKeyWasDown;
    private static boolean beltInventoryKeyWasDown;
    private static boolean beltSpellKeyWasDown;
    private static boolean handSpellKeyWasDown;
    private static boolean backSpellKeyWasDown;
    private static boolean anySpellKeyWasDown;
    private static boolean spellChordModifierKeyWasUp;
    private static boolean[] slotKeysWereDown;
    public static boolean shiftPressed;
    private static float fogAmount;
    private static float fogDecay;
    public static int decrementFog;
    public static float[] fogColor;
    private static long spaceTapTimer;
    private static boolean lastTickJump;
    private static Entity cachedRenderEntity;
    private static final ResourceLocation oredict_ores;

    public static void setFogAmount(float amount) {
        fogAmount = amount;
        decrementFog = 20;
    }

    public static float getFogAmount() {
        return fogAmount;
    }

    public static void wipeOpen() {
        while (((KeyMapping)KeybindInit.RadialMenuOpen.get()).consumeClick()) {
        }
    }

    @SubscribeEvent
    public static void handleKeys(TickEvent.ClientTickEvent ev) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            ClientEventHandler.handleRadialKeyDown();
            ClientEventHandler.handleUIModifierKeyDown();
            ClientEventHandler.handleSpellBookChordsKeyDown();
            ClientEventHandler.handleBeltBagKeyDown();
            ClientEventHandler.handleCurioCastKeysDown();
        } else {
            toolMenuKeyWasDown = true;
            uiModifierKeyWasDown = true;
            spellChordModifierKeyWasUp = true;
            beltInventoryKeyWasDown = false;
            handSpellKeyWasDown = false;
            beltSpellKeyWasDown = false;
            backSpellKeyWasDown = false;
            anySpellKeyWasDown = false;
            for (int i = 0; i < slotKeysWereDown.length; ++i) {
                ClientEventHandler.slotKeysWereDown[i] = true;
            }
        }
    }

    private static void handleSpellBookChordsKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        boolean spellChordModifierKeyIsDown = ((KeyMapping)KeybindInit.SpellBookChord.get()).isDown();
        if (spellChordModifierKeyIsDown) {
            if (spellChordModifierKeyWasUp) {
                HUDOverlayRenderer.instance.isRenderingSpellBookChords = !HUDOverlayRenderer.instance.isRenderingSpellBookChords;
                spellChordModifierKeyWasUp = false;
            }
        } else {
            spellChordModifierKeyWasUp = true;
        }
        if (HUDOverlayRenderer.instance.isRenderingSpellBookChords) {
            KeyMapping[] slotKeyBinds = mc.options.keyHotbarSlots;
            for (int i = 0; i < slotKeyBinds.length; ++i) {
                boolean slotKeyDown = slotKeyBinds[i].isDown();
                if (slotKeyDown != slotKeysWereDown[i]) {
                    while (slotKeyBinds[i].consumeClick()) {
                    }
                    ItemStack mainHandItem = mc.player.getMainHandItem();
                    ItemStack offHandItem = mc.player.getOffhandItem();
                    InteractionHand hand = null;
                    ItemStack inHand = ItemStack.EMPTY;
                    if (mainHandItem.getItem() instanceof SpellBook) {
                        inHand = mainHandItem;
                        hand = InteractionHand.MAIN_HAND;
                    } else if (offHandItem.getItem() instanceof SpellBook) {
                        inHand = offHandItem;
                        hand = InteractionHand.OFF_HAND;
                    }
                    if (!inHand.isEmpty() && hand != null) {
                        SpellBook.setSlot((Player)mc.player, inHand, i, hand, null, true);
                    }
                    HUDOverlayRenderer.instance.isRenderingSpellBookChords = false;
                    spellChordModifierKeyWasUp = true;
                }
                ClientEventHandler.slotKeysWereDown[i] = slotKeyDown;
            }
        }
    }

    private static void handleUIModifierKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        boolean uiModifierKeyIsDown = ((KeyMapping)KeybindInit.InventoryItemOpen.get()).isDown();
        if (uiModifierKeyIsDown != uiModifierKeyWasDown) {
            ClientMessageDispatcher.sendItemUIOpenMessage(uiModifierKeyIsDown);
            MNACap.get(mc.player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.setModifierPressed(uiModifierKeyIsDown));
        }
        uiModifierKeyWasDown = uiModifierKeyIsDown;
    }

    private static void handleBeltBagKeyDown() {
        boolean keyDown = ((KeyMapping)KeybindInit.OpenBeltPouch.get()).isDown();
        if (keyDown != beltInventoryKeyWasDown) {
            ClientMessageDispatcher.sendOpenBeltSlotMessage();
        }
        beltInventoryKeyWasDown = keyDown;
    }

    private static void handleCurioCastKeysDown() {
        boolean keyDown = ((KeyMapping)KeybindInit.CastAnySlot.get()).isDown();
        if (keyDown && keyDown != anySpellKeyWasDown) {
            ClientMessageDispatcher.sendCurioCastMessage(CurioCastSlotType.FIRST_FOUND);
        }
        anySpellKeyWasDown = keyDown;
        keyDown = ((KeyMapping)KeybindInit.CastHandSlot.get()).isDown();
        if (keyDown && keyDown != handSpellKeyWasDown) {
            ClientMessageDispatcher.sendCurioCastMessage(CurioCastSlotType.SPELL);
        }
        handSpellKeyWasDown = keyDown;
        keyDown = ((KeyMapping)KeybindInit.CastBackSlot.get()).isDown();
        if (keyDown && keyDown != backSpellKeyWasDown) {
            ClientMessageDispatcher.sendCurioCastMessage(CurioCastSlotType.BACK);
        }
        backSpellKeyWasDown = keyDown;
        keyDown = ((KeyMapping)KeybindInit.CastBeltSlot.get()).isDown();
        if (keyDown && keyDown != beltSpellKeyWasDown) {
            ClientMessageDispatcher.sendCurioCastMessage(CurioCastSlotType.BELT);
        }
        beltSpellKeyWasDown = keyDown;
    }

    private static void handleRadialKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        boolean toolMenuKeyIsDown = ((KeyMapping)KeybindInit.RadialMenuOpen.get()).isDown();
        if (toolMenuKeyIsDown && !toolMenuKeyWasDown) {
            while (((KeyMapping)KeybindInit.RadialMenuOpen.get()).consumeClick()) {
                Optional<SlotResult> curio;
                if (mc.screen != null) continue;
                ItemStack inHand = mc.player.getMainHandItem();
                if (inHand.getItem() instanceof SpellBook) {
                    mc.setScreen((Screen)new SpellRadialSelect(InteractionHand.MAIN_HAND, null));
                } else if (inHand.getItem() == ItemInit.MODIFIER_BOOK.get()) {
                    mc.setScreen((Screen)new ModifierRadialSelect(false));
                } else if (inHand.getItem() == ItemInit.ENDER_DISK.get()) {
                    mc.setScreen((Screen)new EnderDiscPatternRadialSelect(false));
                } else if (inHand.getItem() instanceof PractitionersPouch) {
                    mc.setScreen((Screen)new RitualKitRadialSelect(false));
                } else if (inHand.getItem() instanceof BellOfBidding) {
                    mc.setScreen((Screen)new BellOfBiddingRadialSelect(false));
                } else if (inHand.getItem() instanceof ItemManaweaverWand) {
                    mc.setScreen((Screen)new ManaweavingWandRadialSelect(false));
                } else {
                    Item item = inHand.getItem();
                    if (item instanceof IRadialInventorySelect) {
                        IRadialInventorySelect radial = (IRadialInventorySelect)item;
                        mc.setScreen((Screen)new ItemstackRadialSelect(false).setOverrideStackCount(radial.capacity()).showFullTooltip(radial.showFullTooltip()));
                    }
                }
                if (mc.screen == null) {
                    inHand = mc.player.getOffhandItem();
                    if (inHand.getItem() instanceof SpellBook) {
                        mc.setScreen((Screen)new SpellRadialSelect(InteractionHand.OFF_HAND, null));
                    } else if (inHand.getItem() == ItemInit.MODIFIER_BOOK.get()) {
                        mc.setScreen((Screen)new ModifierRadialSelect(true));
                    } else if (inHand.getItem() == ItemInit.ENDER_DISK.get()) {
                        mc.setScreen((Screen)new EnderDiscPatternRadialSelect(true));
                    } else if (inHand.getItem() instanceof PractitionersPouch) {
                        mc.setScreen((Screen)new RitualKitRadialSelect(true));
                    } else if (inHand.getItem() instanceof BellOfBidding) {
                        mc.setScreen((Screen)new BellOfBiddingRadialSelect(true));
                    } else if (inHand.getItem() instanceof ItemManaweaverWand) {
                        mc.setScreen((Screen)new ManaweavingWandRadialSelect(true));
                    } else if (inHand.getItem() instanceof IRadialInventorySelect) {
                        mc.setScreen((Screen)new ItemstackRadialSelect(true).setOverrideStackCount(((IRadialInventorySelect)inHand.getItem()).capacity()));
                    }
                }
                if (mc.screen != null || !(curio = CuriosInterop.GetSingleItem((LivingEntity)mc.player, SpellBook.class)).isPresent()) continue;
                mc.setScreen((Screen)new SpellRadialSelect(InteractionHand.OFF_HAND, curio.get().slotContext()));
            }
        }
        toolMenuKeyWasDown = toolMenuKeyIsDown;
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        Level world;
        if (Minecraft.getInstance().isPaused()) {
        }
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        if (player != null) {
            DidYouKnowHelper.CheckAndShowDidYouKnow(player, "helptip.mna.pretty_models");
            world = ManaAndArtifice.instance.proxy.getClientWorld();
            ClientEventHandler.renderWellsprings(world, player);
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SNOWBLIND.get()))) {
                double amount;
                double dist;
                fogDecay = 0.01f;
                if (fogAmount < fogDecay * 2.0f) {
                    fogAmount = fogDecay * 2.0f;
                }
                Vec3 pos = player.position();
                Vec3 lastPos = ManaAndArtifice.instance.proxy.getClientLastTickPosition();
                lastPos = new Vec3(lastPos.x, 0.0, lastPos.z);
                pos = new Vec3(pos.x, 0.0, pos.z);
                if (lastPos != Vec3.ZERO && (dist = pos.distanceToSqr(lastPos)) > 0.078 && (double)fogAmount < (amount = (dist - 0.078) * 10.0)) {
                    fogAmount = (float)amount;
                }
                if (fogAmount > 2.0f) {
                    fogAmount = 2.0f;
                }
            } else {
                fogDecay = 0.01f;
            }
        }
        if (fogAmount > 0.0f && decrementFog-- < 0) {
            fogAmount -= fogDecay;
        }
        ManaAndArtifice.instance.proxy.incrementTick();
        if (player != null) {
            world = ManaAndArtifice.instance.proxy.getClientWorld();
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHOOSING_WELLSPRING.get()))) {
                MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> {
                    int dist = GeneralConfigValues.WellspringDistance;
                    int halfDist = dist / 2;
                    HashMap<BlockPos, WellspringNode> nodes = m.getWellspringRegistry().getNearbyNodes(player.blockPosition(), 50, dist + halfDist);
                    Vec3 start = player.position();
                    for (Map.Entry<BlockPos, WellspringNode> entry : nodes.entrySet()) {
                        BlockPos targetPos = entry.getKey();
                        Vec3 targetVec3d = new Vec3((double)targetPos.getX(), player.getY() + 1.0, (double)targetPos.getZ());
                        Vec3 dir = targetVec3d.subtract(start).normalize();
                        WellspringNode node = entry.getValue();
                        MAParticleType particle = null;
                        if (node.isClaimed()) {
                            int[] color = node.getAffinity().getShiftAffinity().getColor();
                            particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())).setColor(color[0], color[1], color[2]);
                        } else {
                            particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())).setColor(255, 255, 255);
                        }
                        particle.setScale(0.05f);
                        world.addParticle((ParticleOptions)particle, player.getX() + dir.x() + Math.random() * 0.2 - 0.1, player.getY() + 1.0 + dir.y() + Math.random() * 0.2 - 0.1, player.getZ() + dir.z() + Math.random() * 0.2 - 0.1, dir.x, dir.y, dir.z);
                    }
                });
            }
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.COLD_DARK.get()))) {
                fogAmount = 0.95f;
                ClientEventHandler.fogColor[0] = 0.05f;
                ClientEventHandler.fogColor[1] = 0.05f;
                ClientEventHandler.fogColor[2] = 0.05f;
            }
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_SIGHT.get())) && ManaAndArtifice.instance.proxy.getGameTicks() % 80L == 0L) {
                ClientMessageDispatcher.requestWardData(new AABB(player.blockPosition()).inflate(2.0));
            }
        }
    }

    private static void renderWellsprings(Level world, Player player) {
        MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().getNearbyNodes(player.blockPosition(), 0, 32).forEach((pos, node) -> {
            int i;
            float spread;
            if (!(node.isClaimed() || player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_SIGHT.get())) || player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.WELLSPRING_SIGHT.get())))) {
            }
            if (node.hasForcedYLevel() && Math.abs(player.getY() - (double)node.getYLevel()) > 32.0) {
            }
            Affinity aff = node.getAffinity().getShiftAffinity();
            double yPos = node.hasForcedYLevel() ? (double)node.getYLevel() : player.getY();
            int[] colors = aff.getColor();
            int[] sparkleColors = null;
            float scale = 1.0f;
            float strength = node.getStrength();
            float lightSpread = spread = strength / 25.0f / 2.0f;
            int affPfxAmount = 2;
            float lightScale = 1.0f;
            float beamVelUp = 0.1f;
            int lightLifetime = 20;
            float[] velocities = new float[]{0.0f, 0.2f, 0.0f};
            Vec3 llPos = new Vec3((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            MAParticleType type = null;
            switch (aff) {
                case FIRE: 
                case HELLFIRE: {
                    type = (MAParticleType)(((ParticleInit.FLAME.get())));
                    scale = 0.3f;
                }
                case WATER: 
                case ICE: {
                    type = (MAParticleType)(((ParticleInit.WATER.get())));
                    scale = 0.02f + (float)Math.random() * 0.04f;
                    spread = (float)((double)spread + 0.35);
                    lightScale = 0.75f;
                }
                case EARTH: {
                    type = (MAParticleType)(((ParticleInit.DUST.get())));
                    scale = 0.1f + (float)(Math.random() * (double)0.2f) * spread;
                    velocities = new float[]{0.0f, -0.13f, 0.0f};
                    sparkleColors = new int[]{54, 33, 5};
                    colors = new int[]{28, 85, 17};
                    affPfxAmount = 3;
                    lightScale = 0.625f * spread + 0.1f;
                    lightLifetime = 20;
                    beamVelUp = 0.01f;
                    lightSpread = 0.02f * spread;
                    if (!(Math.random() < (double)(spread + 0.25f))) break;
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())).setPhysics(false).setMaxAge(60).setScale(0.1f).setMover(new ParticleOrbitMover(llPos.x(), yPos, llPos.z(), 0.1, -0.025, 1.0f * spread)), llPos.x(), yPos - 3.0 + Math.random() * 10.0, llPos.z(), 0.0, 0.0, 0.0);
                }
                case WIND: {
                    type = (MAParticleType)(((ParticleInit.AIR_ORBIT.get())));
                    affPfxAmount = (int)Math.ceil(8.0f * spread);
                    scale = 0.2f;
                    sparkleColors = new int[]{20, 20, 20};
                    colors = new int[]{40, 40, 40};
                    velocities = new float[]{0.3f, 0.2f, spread * 2.0f};
                    lightScale = 0.5f * spread + 0.15f;
                    beamVelUp = 0.2f;
                    spread *= 0.0f;
                }
                case ENDER: {
                    type = (MAParticleType)(((ParticleInit.ENDER_VELOCITY.get())));
                    scale = 0.3f;
                    lightScale = 0.4f;
                    lightSpread = 0.5f - lightSpread;
                    spread = 0.5f - spread;
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())).setPhysics(false).setMaxAge(60).setScale(0.1f).setMover(new ParticleOrbitMover(llPos.x(), yPos, llPos.z(), Math.random() * 0.1, Math.random() * 0.07, 0.4)), llPos.x(), yPos - 3.0 + Math.random() * 10.0, llPos.z(), 0.0, 0.0, 0.0);
                }
                case ARCANE: {
                    type = (MAParticleType)(((ParticleInit.ARCANE_MAGELIGHT.get())));
                    scale = 0.15f;
                    affPfxAmount = 3;
                    colors = new int[]{102, 32, 232};
                    lightSpread = 0.05f;
                    if (player.level().getGameTime() % 20L != 0L) break;
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.TRAIL_ORBIT.get())).setColor(colors[0], colors[1], colors[2]).setPhysics(false).setMaxAge(60), llPos.x(), yPos - 4.0, llPos.z(), 0.25, 0.25, (double)spread);
                }
            }
            if (aff != Affinity.UNKNOWN) {
                for (i = 0; i < 15; ++i) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHT_VELOCITY.get())).setScale(lightScale).setColor(colors[0], colors[1], colors[2]).setMaxAge(lightLifetime), llPos.x() - (double)lightSpread + Math.random() * (double)lightSpread * 2.0, yPos - 3.0 + Math.random() * 10.0, llPos.z() - (double)lightSpread + Math.random() * (double)lightSpread * 2.0, 0.0, (double)beamVelUp, 0.0);
                }
            }
            if (type != null) {
                for (i = 0; i < affPfxAmount; ++i) {
                    MAParticleType particle = new MAParticleType(type).setScale(scale).setPhysics(false);
                    if (sparkleColors != null) {
                        particle.setColor((int)sparkleColors[0], sparkleColors[1], sparkleColors[2]);
                    }
                    world.addParticle((ParticleOptions)particle, llPos.x() - (double)spread + Math.random() * (double)spread * 2.0, yPos - 3.0 + Math.random() * 10.0, llPos.z() - (double)spread + Math.random() * (double)spread * 2.0, (double)velocities[0], (double)velocities[1], (double)velocities[2]);
                }
            }
            if (player.level().getGameTime() % 20L == 0L) {
                if (!node.hasForcedYLevel()) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.TRAIL_ORBIT.get())).setPhysics(false).setMaxAge(100).setColor(colors[0], colors[1], colors[2]).setScale(0.2f), llPos.x(), yPos - 10.0, llPos.z(), 0.25, 0.25, 0.95);
                } else {
                    int rnd = (int)(Math.random() * 4.0);
                    Vec3 start = new Vec3(llPos.x() - 0.5, yPos - 2.0, llPos.z() - 0.5);
                    Vec3 end = null;
                    Vec3 control_a = null;
                    Vec3 control_b = null;
                    switch (rnd) {
                        case 0: {
                            end = new Vec3(llPos.x(), yPos + 2.0, llPos.z() + 5.0);
                            control_a = new Vec3(llPos.x(), yPos + 4.0, llPos.z() + 1.0);
                            control_b = new Vec3(llPos.x(), yPos + 4.0, llPos.z() + 3.0);
                        }
                        case 1: {
                            end = new Vec3(llPos.x() + 5.0, yPos + 2.0, llPos.z());
                            control_a = new Vec3(llPos.x() + 1.0, yPos + 4.0, llPos.z());
                            control_b = new Vec3(llPos.x() + 3.0, yPos + 4.0, llPos.z());
                        }
                        case 2: {
                            end = new Vec3(llPos.x() - 5.0, yPos + 2.0, llPos.z());
                            control_a = new Vec3(llPos.x() - 1.0, yPos + 4.0, llPos.z());
                            control_b = new Vec3(llPos.x() - 3.0, yPos + 4.0, llPos.z());
                        }
                        case 3: {
                            end = new Vec3(llPos.x(), yPos + 2.0, llPos.z() - 5.0);
                            control_a = new Vec3(llPos.x(), yPos + 4.0, llPos.z() - 1.0);
                            control_b = new Vec3(llPos.x(), yPos + 4.0, llPos.z() - 3.0);
                        }
                    }
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.TRAIL_BEZIER.get())).setColor(colors[0], colors[1], colors[2]).setMaxAge(30).setAgePadding(5).setScale(0.2f).setMover(new ParticleBezierMover(start, end, control_a, control_b)), start.x, start.y, start.z, end.x, end.y, end.z);
                }
            }
        }));
    }

    @SubscribeEvent
    public static void entityTick(EntityTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || (LivingEntity)event.getEntity() == null) {
        }
        if ((LivingEntity)event.getEntity() == mc.player) {
        }
        MobEffectInstance eldrinSight = mc.player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_SIGHT.get()));
        MobEffectInstance senseLife = mc.player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SENSE_LIFE.get()));
        int range = 0;
        if (eldrinSight != null) {
            range = 256;
        } else if (senseLife != null) {
            range = 64 * (senseLife.getAmplifier() + 1);
        }
        if (range == 0) {
        }
        if ((LivingEntity)event.getEntity().distanceToSqr((Entity)mc.player) > (double)range) {
        }
        MobEffectInstance existingEffect = (LivingEntity)event.getEntity().getEffect(MobEffects.GLOWING);
        if (existingEffect == null || existingEffect.getDuration() < 5) {
            ((LivingEntity)event.getEntity()).addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
        }
        MobEffectInstance posession = mc.player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()));
        if (event.isAttack() && posession != null && posession.getAmplifier() > 0) {
            event.setCanceled(true);
            ClientMessageDispatcher.sendPosessionClickInput();
        }
    }

    @SubscribeEvent
    public static void onMouseWheel(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
        }
        if (mc.player.isUsingItem()) {
            ItemStack using = mc.player.getUseItem();
            ISpellDefinition recipe = null;
            if (using.getItem() instanceof SpellBook) {
                recipe = SpellRecipe.fromNBT(((SpellBook)using.getItem()).getSpellCompound(using, (Player)mc.player));
            } else if (using.getItem() instanceof ICanContainSpell) {
                recipe = ((ICanContainSpell)using.getItem()).getSpell(using);
            }
            if (recipe != null && recipe.isValid() && recipe.isChanneled()) {
                event.setCanceled(true);
                float maximum = recipe.getShape().getValue(Attribute.RANGE);
                MNACap.get(mc.player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                    float delta = (float)event.getScrollDelta();
                    m.offsetFocusDistance(delta, maximum);
                    ClientMessageDispatcher.sendPlayerFocusDistanceChange(delta, maximum);
                });
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void playerInputEvent(MovementInputUpdateEvent event) {
        boolean teleporting;
        MobEffectInstance posession;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
        }
        if (spaceTapTimer > 0L) {
            --spaceTapTimer;
        }
        if (mc.player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CONFUSION.get())) && GeneralConfigValues.ConfuseAffectsPlayers) {
            event.getInput().forwardImpulse = -event.getInput().forwardImpulse;
            event.getInput().leftImpulse = -event.getInput().leftImpulse;
        }
        if ((posession = mc.player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()))) != null && posession.getAmplifier() > 0) {
            ClientMessageDispatcher.sendPosessionMovementInput(event.getInput().up ? 1.0f : (event.getInput().down ? -1.0f : 0.0f), event.getInput().left ? 1.0f : (event.getInput().right ? -1.0f : 0.0f), event.getInput().jumping, event.getInput().shiftKeyDown, mc.player.getYRot(), mc.player.yHeadRot, mc.player.getXRot());
            PossessionInputMessage msg = PossessionInputMessage.movement(event.getInput().up ? 1.0f : (event.getInput().down ? -1.0f : 0.0f), event.getInput().left ? 1.0f : (event.getInput().right ? -1.0f : 0.0f), event.getInput().jumping, event.getInput().shiftKeyDown, mc.player.getYRot(), mc.player.yHeadRot, mc.player.getXRot());
            if (mc.player.getPersistentData().contains("posessed_entity_id")) {
                int id = mc.player.getPersistentData().getInt("posessed_entity_id");
                Entity e2 = mc.player.level().getEntity(id);
                if (e2 != null && e2 instanceof Mob) {
                    PossessionHelper.handleRemoteInput(msg, (Player)mc.player, (Mob)e2);
                }
            }
        }
        if (!mc.player.onGround() && event.getInput().jumping && mc.player.noJumpDelay == 0) {
            ItemStack boots = mc.player.getItemBySlot(EquipmentSlot.FEET);
            int enchLevel = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.CLOUDSTEP, player);
            if (!boots.isEmpty() && enchLevel > 0) {
                mc.player.noJumpDelay = 10;
                ClientMessageDispatcher.sendPlayerJump();
            }
        }
        if (!lastTickJump && event.getInput().jumping && mc.player.getActiveEffects().stream().anyMatch(e -> e.getEffect() instanceof IDoubleTapEndEarly)) {
            if (spaceTapTimer == 0L) {
                spaceTapTimer = 7L;
            } else {
                EffectHelper.removeDoubleTapEvents((Player)mc.player);
                ClientMessageDispatcher.sendRequestEndControlEffectEarlyMessage();
                spaceTapTimer = 0L;
            }
        }
        lastTickJump = event.getInput().jumping;
        int mask = InputDisabler.getDisableInputMask((LivingEntity)event.getEntity());
        IPlayerMagic magic = (IPlayerMagic)mc.player.getData(PlayerMagicProvider.MAGIC.get());
        boolean bl = magic == null ? false : (teleporting = magic.getIsTeleporting() || magic.getTeleportElapsedTicks() > 0);
        if ((mask & IInputDisable.InputMask.MOVEMENT.mask()) != 0 || teleporting) {
            event.getInput().up = false;
            event.getInput().down = false;
            event.getInput().left = false;
            event.getInput().right = false;
            event.getInput().jumping = false;
            event.getInput().forwardImpulse = 0.0f;
            event.getInput().leftImpulse = 0.0f;
            event.getInput().shiftKeyDown = false;
        } else if ((mask & IInputDisable.InputMask.JUMP.mask()) != 0 || teleporting) {
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
    }

    public static boolean isKeyDown(KeyMapping keybind) {
        if (keybind.isUnbound()) {
            return false;
        }
        boolean isDown = false;
        switch (keybind.getKey().getType()) {
            case KEYSYM: {
                isDown = InputConstants.isKeyDown((long)Minecraft.getInstance().getWindow().getWindow(), (int)keybind.getKey().getValue());
            }
            case MOUSE: {
                isDown = GLFW.glfwGetMouseButton((long)Minecraft.getInstance().getWindow().getWindow(), (int)keybind.getKey().getValue()) == 1;
            }
        }
        return isDown && keybind.getKeyConflictContext().isActive() && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent rwl) {
        if (rwl.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
        }
        PoseStack matrixStack = rwl.getPoseStack();
        Minecraft mc = Minecraft.getInstance();
        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        matrixStack.pushPose();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        ClientEventHandler.renderMarkingRunes(mc, matrixStack, projectedView, buffers);
        ClientEventHandler.renderWardedBlocks((Level)mc.level, rwl.getPartialTick(), matrixStack, (MultiBufferSource)buffers, (Player)mc.player);
        ClientEventHandler.renderConstructWorldDiagnostics(mc, matrixStack, projectedView, buffers);
        MobEffectInstance inst = mc.player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DIVINATION.get()));
        if (inst != null) {
            ClientEventHandler.renderNearbyOres(mc, matrixStack, projectedView, buffers, inst.getAmplifier() + 2);
        }
        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        ClientEventHandler.renderDistantWellsprings((Level)mc.level, mc.getFrameTime(), matrixStack, (MultiBufferSource)buffers, (Player)mc.player);
        matrixStack.popPose();
        buffers.endBatch();
    }

    private static void renderWardedBlocks(Level world, float partialTicks, PoseStack matrixStack, MultiBufferSource buffers, Player player) {
        VertexConsumer consumer = buffers.getBuffer(MARenderTypes.entityTranslucent((ResourceLocation)MARenderTypes.TEXTURE_WARD));
        ManaAndArtifice.instance.proxy.getWardRenderers().forEach((pos, dur) -> {
            BlockPos bp = BlockPos.of((long)pos);
            if (bp.distToCenterSqr((Position)player.position()) < 512.0) {
                BlockState state = world.getBlockState(bp);
                VoxelShape collision = state.getShape((BlockGetter)world, bp);
                AABB bounds = null;
                bounds = collision.isEmpty() ? MathUtils.createInclusiveBB(bp, bp) : collision.bounds().move(bp);
                int alpha = dur > 20 ? 255 : (int)Math.floor(255.0f * ((float)dur.intValue() / 20.0f));
                WorldRenderUtils.renderWardBox(matrixStack, consumer, bounds.inflate(0.005), alpha);
            }
        });
    }

    private static void renderDistantWellsprings(Level world, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, Player player) {
        MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().getNearbyNodes(player.blockPosition(), 0, 128).forEach((pos, node) -> {
            double distAdjusted;
            double distPct;
            if (!(node.isClaimed() || player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_SIGHT.get())) || player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.WELLSPRING_SIGHT.get())))) {
            }
            if (node.hasForcedYLevel() && Math.abs(player.getY() - (double)node.getYLevel()) > 32.0) {
            }
            BlockPos adjPos = new BlockPos(pos.getX(), player.blockPosition().getY(), pos.getZ());
            double dist = Math.sqrt(player.blockPosition().distSqr((Vec3i)adjPos));
            Affinity aff = node.getAffinity().getShiftAffinity();
            int alpha = 176;
            int distFalloff = 12;
            int distFalloffStart = 38;
            int height = 13;
            if (dist < (double)distFalloffStart && aff != Affinity.ENDER && aff != Affinity.UNKNOWN && (alpha = (int)((double)alpha * (distPct = 1.0 - (distAdjusted = (double)distFalloffStart - dist) / (double)distFalloff))) < 0) {
                alpha = 0;
            }
            if (!node.hasForcedYLevel()) {
                height = 255;
            }
            int[] color = aff.getColor();
            double yPos = node.hasForcedYLevel() ? (double)(node.getYLevel() - 4) : 0.0;
            Vec3 llPos = new Vec3((double)pos.getX() + 0.5, yPos, (double)pos.getZ() + 0.5);
            matrixStack.pushPose();
            matrixStack.translate(llPos.x, llPos.y, llPos.z);
            WorldRenderUtils.renderBeam(world, partialTicks, matrixStack, buffer, 0xF00000, llPos, llPos.add(0.0, (double)height, 0.0), 1.0f, color, alpha, 0.25f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            matrixStack.popPose();
        }));
    }

    private static void renderConstructWorldDiagnostics(Minecraft mc, PoseStack matrixStack, Vec3 projectedView, MultiBufferSource.BufferSource buffers) {
        Construct c = HUDOverlayRenderer.instance.getTrackedConstruct();
        if (c == null) {
        }
        LinkedList<ConstructDiagnostics.TaskHistoryEntry> taskHistory = c.getDiagnostics().getTaskHistory();
        if (taskHistory.size() == 0) {
        }
        int drawDist = 4096;
        float opacity = 1.0f;
        float opacityDecrease = 0.9f / (float)taskHistory.size();
        RenderSystem.disableDepthTest();
        for (int i = taskHistory.size() - 1; i >= 0; --i) {
            matrixStack.pushPose();
            ConstructDiagnostics.TaskHistoryEntry mostRecentTask = taskHistory.get(i);
            float[] color = mostRecentTask.colorFromStatusCode();
            if (mostRecentTask.getEntityID() > 0) {
                if (cachedRenderEntity == null || cachedRenderEntity.getId() != mostRecentTask.getEntityID()) {
                    cachedRenderEntity = mc.level.getEntity(mostRecentTask.getEntityID());
                }
                if (cachedRenderEntity != null && cachedRenderEntity.isAlive() && mc.player.distanceToSqr(cachedRenderEntity) < (double)drawDist) {
                    LevelRenderer.renderLineBox((PoseStack)matrixStack, (VertexConsumer)buffers.getBuffer(MARenderTypes.MARKING_RUNE_MARK), (AABB)new AABB(cachedRenderEntity.blockPosition()).deflate(0.375), (float)color[0], (float)color[1], (float)color[2], (float)opacity);
                }
            }
            if (mostRecentTask.getArea() != null && mc.player.distanceToSqr(mostRecentTask.getArea().getCenter()) < (double)drawDist + Math.pow(mostRecentTask.getArea().getSize() / 2.0, 2.0)) {
                LevelRenderer.renderLineBox((PoseStack)matrixStack, (VertexConsumer)buffers.getBuffer(MARenderTypes.MARKING_RUNE_MARK), (AABB)mostRecentTask.getArea(), (float)color[0], (float)color[1], (float)color[2], (float)opacity);
            }
            if (mostRecentTask.getPos() != null && mc.player.distanceToSqr(mostRecentTask.getPos()) < (double)drawDist) {
                LevelRenderer.renderLineBox((PoseStack)matrixStack, (VertexConsumer)buffers.getBuffer(MARenderTypes.MARKING_RUNE_MARK), (AABB)new AABB(BlockPos.containing((Position)mostRecentTask.getPos())).deflate(0.375), (float)color[0], (float)color[1], (float)color[2], (float)opacity);
            }
            opacity -= opacityDecrease;
            matrixStack.popPose();
        }
        Vec3 targetPos = c.getDiagnostics().getMovePos();
        if (targetPos != null && mc.player.distanceToSqr(targetPos) < (double)drawDist && mc.player.distanceToSqr((Entity)c) < (double)drawDist) {
            VertexConsumer consumer = buffers.getBuffer(MARenderTypes.MARKING_RUNE_MARK);
            BlockPos blockPos = BlockPos.containing((Position)targetPos);
            LevelRenderer.renderLineBox((PoseStack)matrixStack, (VertexConsumer)consumer, (AABB)new AABB(blockPos).deflate(0.375), (float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            targetPos = Vec3.atCenterOf((Vec3i)blockPos);
            Matrix4f positions = matrixStack.last().pose();
            PoseStack.Pose normals = matrixStack.last();
            Vec3 cPos = c.position();
            consumer.addVertex(positions, (float)cPos.x, (float)cPos.y, (float)cPos.z).setColor(1, 1, 1, 1).setNormal(normals, 1.0f, 0.0f, 0.0f);
            consumer.addVertex(positions, (float)targetPos.x, (float)targetPos.y, (float)targetPos.z).setColor(1, 1, 1, 1).setNormal(normals, 1.0f, 0.0f, 0.0f);
        }
        RenderSystem.enableDepthTest();
    }

    private static void renderMarkingRunes(Minecraft mc, PoseStack matrixStack, Vec3 projectedView, MultiBufferSource.BufferSource buffers) {
        Direction face;
        int drawDist = 4096;
        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        DirectionalPoint mainHandPos = null;
        DirectionalPoint offhandPos = null;
        AABB renderBounds = null;
        boolean render = false;
        boolean drawMainHand = false;
        boolean drawOffHand = false;
        Item item = mainHand.getItem();
        if (item instanceof IAreaItem) {
            IAreaItem mhAreaItem = (IAreaItem)item;
            renderBounds = mhAreaItem.getBounds(mainHand);
            if (renderBounds != null) {
                renderBounds = renderBounds.expandTowards(1.0, 1.0, 1.0);
                render = true;
                drawMainHand = true;
                drawOffHand = true;
            }
        } else {
            item = offHand.getItem();
            if (item instanceof IAreaItem) {
                IAreaItem ohAreaItem = (IAreaItem)item;
                renderBounds = ohAreaItem.getBounds(offHand);
                if (renderBounds != null) {
                    renderBounds = renderBounds.expandTowards(1.0, 1.0, 1.0);
                    render = true;
                    drawMainHand = true;
                    drawOffHand = true;
                }
            } else {
                Item item2 = mainHand.getItem();
                if (item2 instanceof IPositionalItem) {
                    IPositionalItem mhPosItem = (IPositionalItem)item2;
                    mainHandPos = mhPosItem.getDirectionalPoint(mainHand);
                    render = true;
                }
                if ((item2 = offHand.getItem()) instanceof IPositionalItem) {
                    IPositionalItem ohPosItem = (IPositionalItem)item2;
                    offhandPos = ohPosItem.getDirectionalPoint(offHand);
                    render = true;
                }
            }
        }
        if (!render) {
        }
        RenderSystem.disableDepthTest();
        if (mainHandPos != null && mainHandPos.isValid() && mc.player.distanceToSqr((double)mainHandPos.getPosition().getX(), (double)mainHandPos.getPosition().getY(), (double)mainHandPos.getPosition().getZ()) < (double)drawDist) {
            face = mainHandPos.getDirection();
            WorldRenderUtils.renderAxisPoint(matrixStack, buffers.getBuffer(MARenderTypes.MARKING_RUNE_AREA), mainHandPos.getPosition(), face, 0.0f, 1.0f, 0.0f, 0.25f);
            drawMainHand = true;
        }
        if (offhandPos != null && offhandPos.isValid() && mc.player.distanceToSqr((double)offhandPos.getPosition().getX(), (double)offhandPos.getPosition().getY(), (double)offhandPos.getPosition().getZ()) < (double)drawDist) {
            face = offhandPos.getDirection();
            WorldRenderUtils.renderAxisPoint(matrixStack, buffers.getBuffer(MARenderTypes.MARKING_RUNE_AREA), offhandPos.getPosition(), face, 1.0f, 0.0f, 0.0f, 0.25f);
            drawOffHand = true;
        }
        if (drawMainHand && drawOffHand) {
            if (renderBounds == null) {
                renderBounds = MathUtils.createInclusiveBB(mainHandPos.getPosition(), offhandPos.getPosition());
            }
            VertexConsumer consumer = buffers.getBuffer(MARenderTypes.MARKING_RUNE_AREA);
            WorldRenderUtils.renderAxisBox(matrixStack, consumer, renderBounds);
        }
        RenderSystem.enableDepthTest();
    }

    private static void renderNearbyOres(Minecraft mc, PoseStack matrixStack, Vec3 projectedView, MultiBufferSource.BufferSource buffers, int radius) {
        ItemStack offhand = mc.player.getOffhandItem();
        boolean searchOffhand = offhand.getItem() instanceof BlockItem;
        BlockPos offset = BlockPos.containing((Position)mc.player.getForward().normalize().scale((double)radius));
        BlockPos origin = mc.player.blockPosition().offset((Vec3i)offset);
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos target = origin.offset(x, y, z);
                    BlockState state = mc.level.getBlockState(target);
                    if ((!searchOffhand || state.getBlock() != ((BlockItem)offhand.getItem()).getBlock()) && (searchOffhand || !(state.getBlock() instanceof DropExperienceBlock) && !MATags.isBlockIn(state.getBlock(), oredict_ores))) continue;
                    LevelRenderer.renderLineBox((PoseStack)matrixStack, (VertexConsumer)buffers.getBuffer(MARenderTypes.MARKING_RUNE_MARK), (AABB)new AABB(target), (float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getBlockState().getBlock() == Blocks.FIRE) {
            Entity ridingEntity = event.getPlayer().getVehicle();
            if (ridingEntity != null && ridingEntity instanceof BubbleBoat && ((BubbleBoat)ridingEntity).isBrimstone()) {
                event.setCanceled(true);
            } else if (event.getPlayer().getItemBySlot(EquipmentSlot.HEAD).getItem() == ItemInit.BRIMSTONE_BOAT.get()) {
                event.setCanceled(true);
            } else if (CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)event.getPlayer(), (Item)ItemInit.EMBERGLOW_BRACELET.get()).isPresent()) {
                event.setCanceled(true);
            } else if (((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped((LivingEntity)event.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onInventoryGuiInit(ScreenEvent.Init.Post evt) {
        Screen screen = evt.getScreen();
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
            boolean isCreative = screen instanceof CreativeModeInventoryScreen;
            Tuple<Integer, Integer> offsets = PlayerInventoryButton.getOffsets(isCreative);
            int x = (Integer)offsets.getA();
            int y = (Integer)offsets.getB();
            int textureOffsetX = 6;
            AbstractContainerScreen gui = (AbstractContainerScreen)screen;
            if (HUDOverlayRenderer.instance.getPinnedrecipe() != null || HUDOverlayRenderer.instance.getConstructDisplay().isValid()) {
                evt.addListener((GuiEventListener)new PlayerInventoryButton(gui, gui.getGuiLeft() + x, gui.height / 2 + y, 5, 10, textureOffsetX, 27, 10, GuiTextures.Widgets.WIDGETS, button -> {
                    HUDOverlayRenderer.instance.setPinnedRecipe(null);
                    if (HUDOverlayRenderer.instance.getTrackedConstruct() != null) {
                        HUDOverlayRenderer.instance.getTrackedConstruct().setRequestingDiagnostics(false);
                        HUDOverlayRenderer.instance.setTrackedConstruct(null);
                    }
                    button.active = false;
                    button.visible = false;
                }, (Component)Component.translatable((String)"gui.mna.clear_pinned_prompt")));
            }
            if (MultiblockRenderer.hasMultiblock) {
                evt.addListener((GuiEventListener)new PlayerInventoryButton(gui, gui.getGuiLeft() + x, gui.height / 2 + y + 10, 5, 5, textureOffsetX, 47, 5, GuiTextures.Widgets.WIDGETS, button -> MultiblockRenderer.setMultiblock(null, null, true), (Component)Component.translatable((String)"gui.mna.clear_multiblock_prompt")));
                evt.addListener((GuiEventListener)new PlayerInventoryButton(gui, gui.getGuiLeft() + x, gui.height / 2 + y + 20, 5, 5, textureOffsetX, 57, 5, GuiTextures.Widgets.WIDGETS, button -> MultiblockRenderer.toggleLowestLayerMode(), (Component)Component.translatable((String)"gui.mna.multiblock_mode_prompt")));
            }
        }
    }

    @SubscribeEvent
    public static void onFogRenderEvent(ViewportEvent.RenderFog event) {
        Minecraft mc;
        if (event.getCamera().getFluidInCamera() == FogType.LAVA) {
            mc = Minecraft.getInstance();
            if (((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped((LivingEntity)mc.player) || CuriosInterop.IsItemInCurioSlot((Item)ItemInit.EMBERGLOW_BRACELET.get(), (LivingEntity)mc.player, SlotTypePreset.BRACELET)) {
                event.setNearPlaneDistance(-8.0f);
                event.setFarPlaneDistance(192.0f);
                event.setCanceled(true);
            }
        }
        if (event.getCamera().getFluidInCamera() == FogType.WATER) {
            mc = Minecraft.getInstance();
            MNACap.get(mc.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                if (p.getAlliedFaction() == Factions.UNDEAD) {
                    event.setNearPlaneDistance(-8.0f);
                    event.setFarPlaneDistance(192.0f);
                    event.setCanceled(true);
                }
            });
        }
        if (fogAmount > 0.0f) {
            float f1 = Mth.lerp((float)Math.min(1.0f, fogAmount), (float)event.getFarPlaneDistance(), (float)5.0f);
            float f2 = 0.0f;
            float f3 = f1 * 0.8f;
            RenderSystem.setShaderFogStart((float)f2);
            RenderSystem.setShaderFogEnd((float)f3);
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (fogAmount > 0.0f) {
            event.setRed(fogColor[0]);
            event.setGreen(fogColor[1]);
            event.setBlue(fogColor[2]);
        }
    }

    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event) {
        Level world = ManaAndArtifice.instance.proxy.getClientWorld();
        if (world != null) {
            IRelic relic;
            Item c;
            Item item = event.getItemStack().getItem();
            if (item instanceof IRelic && (c = (relic = (IRelic)item).getHoverAddition(event.getItemStack(), world, event.getToolTip(), event.getFlags())) != null) {
                event.getToolTip().add(1, c);
            }
            if ((c = event.getItemStack().getItem()) instanceof ITieredItem) {
                ITieredItem item2 = (ITieredItem)c;
                if (event.getItemStack().has(DataComponents.CUSTOM_DATA) && event.getItemStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("hideTier")) {
                }
                int tier = item2.getTier(world, event.getItemStack());
                if (tier == -2) {
                    event.getToolTip().add(Component.translatable((String)"gui.mna.item-tier.none").withStyle(ChatFormatting.GREEN));
                } else {
                    MutableBoolean metTier = new MutableBoolean(false);
                    if (tier > 0) {
                        Player clientPlayer = ManaAndArtifice.instance.proxy.getClientPlayer();
                        if (clientPlayer != null) {
                            MNACap.get(clientPlayer, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                                if (p.getTier() >= tier) {
                                    metTier.setTrue();
                                }
                            });
                        }
                        event.getToolTip().add(Component.translatable((String)"gui.mna.item-tier", (Object[])new Object[]{tier}).withStyle(metTier.getValue() != false ? ChatFormatting.GREEN : ChatFormatting.RED));
                    } else {
                        event.getToolTip().add(Component.translatable((String)"gui.mna.item-tier.none").withStyle(ChatFormatting.GREEN));
                    }
                }
            }
        }
    }

    static {
        slotKeysWereDown = new boolean[]{false, false, false, false, false, false, false, false, false, false};
        shiftPressed = false;
        fogAmount = 0.0f;
        fogDecay = 0.01f;
        decrementFog = 0;
        fogColor = new float[]{1.0f, 1.0f, 1.0f};
        spaceTapTimer = 0L;
        lastTickJump = false;
        cachedRenderEntity = null;
        oredict_ores = ResourceLocation.fromNamespaceAndPath("forge", "ores");
    }
}


