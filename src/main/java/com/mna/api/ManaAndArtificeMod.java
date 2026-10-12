 
package com.mna.api;

import com.mna.api.IPortalHelper;
import com.mna.api.advancements.IAdvancementHelper;
import com.mna.api.blocks.tile.ITileHelper;
import com.mna.api.cantrips.ICantripRegistry;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.capabilities.resource.ICastingResourceGuiRegistry;
import com.mna.api.capabilities.resource.ICastingResourceRegistry;
import com.mna.api.entities.IEntityHelper;
import com.mna.api.entities.ISummonHelper;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.faction.IFaction;
import com.mna.api.faction.IFactionHelper;
import com.mna.api.faction.IRaidHelper;
import com.mna.api.gui.IContainerHelper;
import com.mna.api.gui.IGuiRenderHelper;
import com.mna.api.items.IItemHelper;
import com.mna.api.particles.IParticleSerializerHelper;
import com.mna.api.particles.IWorldRenderUtils;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ISpellHelper;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.config.ISpellConfigHelper;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.IInventoryHelper;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mna.api.tools.MNACap;

public class ManaAndArtificeMod {
    public static final String ID = "mna";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final UUID REACH_DISTANCE_UUID = UUID.fromString("cfb81e6d-8447-4e92-8683-ae23800cecca");
    public static final net.minecraft.resources.ResourceLocation REACH_DISTANCE_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mna", "reach_distance");
    private static java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IPlayerMagic>> __magicCap;
    private static java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IPlayerProgression>> __progressionCap;
    private static java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IWorldMagic>> __worldMagicCap;
    private static java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IChunkMagic>> __chunkMagicCap;
    private static ICantripRegistry __cantripRegistry;
    private static IWorldRenderUtils __worldRenderUtils;
    private static IGuiRenderHelper __guiRenderHelper;
    private static ISummonHelper __summonHelper;
    private static IPortalHelper __portalHelper;
    private static IEntityHelper __entityHelper;
    private static ISpellHelper __spellHelper;
    private static IRaidHelper __raidHelper;
    private static IFactionHelper __factionHelper;
    private static ISpellConfigHelper __configHelper;
    private static IInventoryHelper __inventoryHelper;
    private static IAdvancementHelper __advancementHelper;
    private static IParticleSerializerHelper __particleSerializationHelper;
    private static IItemHelper __itemHelper;
    private static IContainerHelper __containerHelper;
    private static ITileHelper __tileHelper;
    private static ICastingResourceRegistry __castingResourceRegistry;
    private static ICastingResourceGuiRegistry __castingResourceGuiRegistry;

    @Deprecated(forRemoval=true)
    public static void registerSpellAdjuster(Predicate<SpellAdjustingContext> predicate, BiConsumer<ISpellDefinition, LivingEntity> adjuster) {
        ManaAndArtificeMod.getSpellHelper().registerSpellAdjuster(predicate, adjuster);
    }

    public static void registerSpellAdjuster(Predicate<SpellAdjustingContext> predicate, Consumer<SpellAdjustingContext> adjuster) {
        ManaAndArtificeMod.getSpellHelper().registerSpellAdjuster(predicate, adjuster);
    }

    public static boolean checkMagicUnlock(ServerLevel world, Player player) {
        IPlayerMagic playerMagic = (IPlayerMagic)MNACap.get(player, ManaAndArtificeMod.getMagicCapability()).orElse(null);
        if (playerMagic != null && !playerMagic.isMagicUnlocked()) {
            if (world.getServer().isSingleplayer()) {
                world.setDayTime(world.getGameTime() + 12000L);
            }
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.guide_book.magic_unlocked").withStyle(ChatFormatting.AQUA));
            playerMagic.setMagicLevel(player, 1);
            world.playSound(null, player.blockPosition(), SFX.Event.Player.MAGIC_UNLOCKED, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    public static final IAdvancementHelper getAdvancementHelper() {
        return __advancementHelper;
    }

    public static final ISpellConfigHelper getConfigHelper() {
        return __configHelper;
    }

    @Nullable
    public static final IEntityHelper getEntityHelper() {
        return __entityHelper;
    }

    public static final IFactionHelper getFactionHelper() {
        return __factionHelper;
    }

    @Nullable
    public static final IGuiRenderHelper getGuiRenderHelper() {
        return __guiRenderHelper;
    }

    public static final IInventoryHelper getInventoryHelper() {
        return __inventoryHelper;
    }

    public static final IParticleSerializerHelper getParticleSerializationHelper() {
        return __particleSerializationHelper;
    }

    @Nullable
    public static final IPortalHelper getPortalHelper() {
        return __portalHelper;
    }

    public static final IRaidHelper getRaidHelper() {
        return __raidHelper;
    }

    public static final ISpellHelper getSpellHelper() {
        return __spellHelper;
    }

    public static final ISummonHelper getSummonHelper() {
        return __summonHelper;
    }

    @Nullable
    public static final IWorldRenderUtils getWorldRenderUtils() {
        return __worldRenderUtils;
    }

    public static final IItemHelper getItemHelper() {
        return __itemHelper;
    }

    public static final IContainerHelper getContainerHelper() {
        return __containerHelper;
    }

    public static final ITileHelper getTileHelper() {
        return __tileHelper;
    }

    public static final ICastingResourceRegistry getCastingResourceRegistry() {
        return __castingResourceRegistry;
    }

    public static final ICastingResourceGuiRegistry getCastingResourceGuiRegistry() {
        return __castingResourceGuiRegistry;
    }

    /**
     * [PORT 1.21.1] IForgeRegistry/RegistryManager foram REMOVIDOS.
     * Os registries custom vivem em {@link com.mna.Registries}.
     */
    public static com.mna.MnaRegistry<ConstructTask> getConstructTaskRegistry() {
        return com.mna.Registries.ConstructTasks;
    }

    public static com.mna.MnaRegistry<Shape> getShapeRegistry() {
        return com.mna.Registries.Shape;
    }

    public static com.mna.MnaRegistry<SpellEffect> getComponentRegistry() {
        return com.mna.Registries.SpellEffect;
    }

    public static com.mna.MnaRegistry<Modifier> getModifierRegistry() {
        return com.mna.Registries.Modifier;
    }

    public static com.mna.MnaRegistry<IFaction> getFactionsRegistry() {
        return com.mna.Registries.Factions;
    }

    public static final ICantripRegistry getCantripRegistry() {
        return __cantripRegistry;
    }

    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IWorldMagic>> getWorldMagicCapability() {
        return __worldMagicCap;
    }

    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IPlayerMagic>> getMagicCapability() {
        return __magicCap;
    }

    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IPlayerProgression>> getProgressionCapability() {
        return __progressionCap;
    }

    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IChunkMagic>> getChunkMagicCapability() {
        return __chunkMagicCap;
    }

    public static void setMagicCapability(java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IPlayerMagic>> cap) {
        if (__magicCap == null) {
            __magicCap = cap;
        }
    }

    public static void setProgressionCapability(java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IPlayerProgression>> cap) {
        if (__progressionCap == null) {
            __progressionCap = cap;
        }
    }

    public static void setWorldMagicCapability(java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IWorldMagic>> cap) {
        if (__worldMagicCap == null) {
            __worldMagicCap = cap;
        }
    }

    public static void setChunkMagicCapability(java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IChunkMagic>> cap) {
        if (__chunkMagicCap == null) {
            __chunkMagicCap = cap;
        }
    }

    public static void setCantripRegistry(ICantripRegistry registry) {
        if (__cantripRegistry == null) {
            __cantripRegistry = registry;
        }
    }

    public static void setAPIHelper(IPortalHelper helper) {
        if (__portalHelper == null) {
            __portalHelper = helper;
        }
    }

    public static void setAPIHelper(IEntityHelper helper) {
        if (__entityHelper == null) {
            __entityHelper = helper;
        }
    }

    public static void setAPIHelper(ISpellHelper helper) {
        if (__spellHelper == null) {
            __spellHelper = helper;
        }
    }

    public static void setAPIHelper(IRaidHelper helper) {
        if (__raidHelper == null) {
            __raidHelper = helper;
        }
    }

    public static void setAPIHelper(IWorldRenderUtils registry) {
        if (__worldRenderUtils == null) {
            __worldRenderUtils = registry;
        }
    }

    public static void setAPIHelper(IGuiRenderHelper registry) {
        if (__guiRenderHelper == null) {
            __guiRenderHelper = registry;
        }
    }

    public static void setAPIHelper(ISummonHelper helper) {
        if (__summonHelper == null) {
            __summonHelper = helper;
        }
    }

    public static void setAPIHelper(IFactionHelper helper) {
        if (__factionHelper == null) {
            __factionHelper = helper;
        }
    }

    public static void setAPIHelper(ISpellConfigHelper helper) {
        if (__configHelper == null) {
            __configHelper = helper;
        }
    }

    public static void setAPIHelper(IInventoryHelper helper) {
        if (__inventoryHelper == null) {
            __inventoryHelper = helper;
        }
    }

    public static void setAPIHelper(IAdvancementHelper helper) {
        if (__advancementHelper == null) {
            __advancementHelper = helper;
        }
    }

    public static void setAPIHelper(IParticleSerializerHelper helper) {
        if (__particleSerializationHelper == null) {
            __particleSerializationHelper = helper;
        }
    }

    public static void setAPIHelper(IItemHelper helper) {
        if (__itemHelper == null) {
            __itemHelper = helper;
        }
    }

    public static void setAPIHelper(IContainerHelper helper) {
        if (__containerHelper == null) {
            __containerHelper = helper;
        }
    }

    public static void setAPIHelper(ITileHelper helper) {
        if (__tileHelper == null) {
            __tileHelper = helper;
        }
    }

    public static void setAPIHelper(ICastingResourceRegistry helper) {
        if (__castingResourceRegistry == null) {
            __castingResourceRegistry = helper;
        }
    }

    public static void setAPIHelper(ICastingResourceGuiRegistry helper) {
        if (__castingResourceGuiRegistry == null) {
            __castingResourceGuiRegistry = helper;
        }
    }
}


