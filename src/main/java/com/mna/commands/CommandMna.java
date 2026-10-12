 
package com.mna.commands;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.commands.AffinityArgument;
import com.mna.api.commands.FactionArgument;
import com.mna.api.commands.SpellPartArgument;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.capabilities.particles.ParticleAuraProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.gui.containers.providers.NamedAuras;
import com.mna.recipes.progression.ProgressionCondition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class CommandMna {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"mna").then(CommandMna.progressionCommands())).then(CommandMna.auraCommands())).then(CommandMna.raidCommands())).then(CommandMna.wellspringCommands())).then(CommandMna.roteCommands())).then(CommandMna.masteryCommands()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> progressionCommands() {
        return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"progression").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"reset_codex").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.resetCodex((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.resetCodex((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"reset_all").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.resetProgression((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.resetProgression((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"complete").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.completeProgression((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.completeProgression((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)Commands.literal((String)"level").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument((String)"level", (ArgumentType)IntegerArgumentType.integer((int)0, (int)75)).executes(context -> CommandMna.setMagicLevel((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException()), IntegerArgumentType.getInteger((CommandContext)context, (String)"level")))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.setMagicLevel((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), IntegerArgumentType.getInteger((CommandContext)context, (String)"level"))))))).then(((LiteralArgumentBuilder)Commands.literal((String)"affinity").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).then(Commands.literal((String)"add").then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)1.0f, (float)100.0f)).executes(context -> CommandMna.addAffinity((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"))))))).then(Commands.literal((String)"remove").then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)1.0f, (float)100.0f)).executes(context -> CommandMna.removeAffinity((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"))))))).then(Commands.literal((String)"set").then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)1.0f, (float)100.0f)).executes(context -> CommandMna.setAffinity((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"))))))).then(Commands.literal((String)"reset").then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).executes(context -> CommandMna.resetAffinity((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity")))))).then(Commands.literal((String)"reset_all").executes(context -> CommandMna.resetAllAffinity((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"))))))).then(((LiteralArgumentBuilder)Commands.literal((String)"faction").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument((String)"faction", (ArgumentType)new FactionArgument()).executes(context -> CommandMna.setFaction((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException()), FactionArgument.getFaction(context, "faction"), 0))).then(((RequiredArgumentBuilder)Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.setFaction((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), FactionArgument.getFaction(context, "faction"), 0))).then(Commands.argument((String)"resIdx", (ArgumentType)IntegerArgumentType.integer()).executes(context -> CommandMna.setFaction((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), FactionArgument.getFaction(context, "faction"), IntegerArgumentType.getInteger((CommandContext)context, (String)"resIdx")))))))).then(((LiteralArgumentBuilder)Commands.literal((String)"tier").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument((String)"tier", (ArgumentType)IntegerArgumentType.integer((int)1, (int)5)).executes(context -> CommandMna.setTier((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException()), IntegerArgumentType.getInteger((CommandContext)context, (String)"tier")))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.setTier((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), IntegerArgumentType.getInteger((CommandContext)context, (String)"tier"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> auraCommands() {
        return ((LiteralArgumentBuilder)Commands.literal((String)"aura").requires(commandSource -> {
            Player player;
            if (ManaAndArtifice.instance.isDebug) {
                return true;
            }
            Entity e = commandSource.getEntity();
            if (e instanceof Player && (player = (Player)e).getGameProfile() != null) {
                return ManaAndArtifice.instance.enabled_auras.contains(player.getGameProfile().getId());
            }
            return false;
        })).executes(context -> CommandMna.openMenu((CommandSourceStack)context.getSource()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> raidCommands() {
        return ((LiteralArgumentBuilder)Commands.literal((String)"raid").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument((String)"faction", (ArgumentType)new FactionArgument()).executes(context -> CommandMna.factionRaid((CommandSourceStack)context.getSource(), FactionArgument.getFaction(context, "faction"), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException()), false))).then(((RequiredArgumentBuilder)Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.factionRaid((CommandSourceStack)context.getSource(), FactionArgument.getFaction(context, "faction"), EntityArgument.getPlayers((CommandContext)context, (String)"player"), false))).then(Commands.argument((String)"force", (ArgumentType)BoolArgumentType.bool()).executes(context -> CommandMna.factionRaid((CommandSourceStack)context.getSource(), FactionArgument.getFaction(context, "faction"), EntityArgument.getPlayers((CommandContext)context, (String)"player"), BoolArgumentType.getBool((CommandContext)context, (String)"force"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wellspringCommands() {
        return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"wellspring").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"create").requires(commandSource -> commandSource.hasPermission(2))).then(Commands.argument((String)"position", (ArgumentType)BlockPosArgument.blockPos()).executes(context -> CommandMna.makeWellspring((CommandSourceStack)context.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)context, (String)"position"))))).then(Commands.argument((String)"position", (ArgumentType)BlockPosArgument.blockPos()).then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).executes(context -> CommandMna.makeWellspring((CommandSourceStack)context.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)context, (String)"position"), AffinityArgument.getAffinity(context, "affinity")))))).then(Commands.argument((String)"position", (ArgumentType)BlockPosArgument.blockPos()).then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"strength", (ArgumentType)FloatArgumentType.floatArg((float)5.0f, (float)25.0f)).executes(context -> CommandMna.makeWellspring((CommandSourceStack)context.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)context, (String)"position"), AffinityArgument.getAffinity(context, "affinity"), FloatArgumentType.getFloat((CommandContext)context, (String)"strength")))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"power").then(((LiteralArgumentBuilder)Commands.literal((String)"set").requires(commandSource -> commandSource.hasPermission(2))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"level", (ArgumentType)IntegerArgumentType.integer((int)0, (int)100)).executes(context -> CommandMna.setWellspringPower((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity"), IntegerArgumentType.getInteger((CommandContext)context, (String)"level")))))))).then(((LiteralArgumentBuilder)Commands.literal((String)"add").requires(commandSource -> commandSource.hasPermission(2))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"level", (ArgumentType)IntegerArgumentType.integer((int)0, (int)1000)).executes(context -> CommandMna.addWellspringPower((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity"), IntegerArgumentType.getInteger((CommandContext)context, (String)"level")))))))).then(((LiteralArgumentBuilder)Commands.literal((String)"remove").requires(commandSource -> commandSource.hasPermission(2))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"affinity", (ArgumentType)new AffinityArgument()).then(Commands.argument((String)"level", (ArgumentType)IntegerArgumentType.integer((int)0, (int)1000)).executes(context -> CommandMna.removeWellspringPower((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), AffinityArgument.getAffinity(context, "affinity"), IntegerArgumentType.getInteger((CommandContext)context, (String)"level"))))))))).then(Commands.literal((String)"delete").then(Commands.argument((String)"position", (ArgumentType)BlockPosArgument.blockPos()).executes(context -> CommandMna.deleteWellspring((CommandSourceStack)context.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)context, (String)"position")))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> roteCommands() {
        return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"rote").then(((LiteralArgumentBuilder)Commands.literal((String)"one").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument((String)"identifier", (ArgumentType)new SpellPartArgument()).executes(context -> CommandMna.addRote((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException()), SpellPartArgument.getSpell(context, "identifier")))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addRote((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), SpellPartArgument.getSpell(context, "identifier"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"shapes").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addRote_Shapes((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addRote_Shapes((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"components").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addRote_SpellEffects((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addRote_SpellEffects((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"modifiers").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addRote_Modifiers((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addRote_Modifiers((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"all").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addRote_All((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addRote_All((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"reset").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.resetRote((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.resetRote((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> masteryCommands() {
        return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"mastery").then(((LiteralArgumentBuilder)Commands.literal((String)"one").requires(commandSource -> commandSource.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument((String)"identifier", (ArgumentType)new SpellPartArgument()).executes(context -> CommandMna.addMastery((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException()), SpellPartArgument.getSpell(context, "identifier")))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addMastery((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player"), SpellPartArgument.getSpell(context, "identifier"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"shapes").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addMastery_Shapes((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addMastery_Shapes((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"components").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addMastery_SpellEffects((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addMastery_SpellEffects((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"modifiers").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addMastery_Modifiers((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addMastery_Modifiers((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"all").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.addMastery_All((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.addMastery_All((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"reset").requires(commandSource -> commandSource.hasPermission(2))).executes(context -> CommandMna.resetMastery((CommandSourceStack)context.getSource(), Collections.singleton(((CommandSourceStack)context.getSource()).getPlayerOrException())))).then(Commands.argument((String)"player", (ArgumentType)EntityArgument.players()).executes(context -> CommandMna.resetMastery((CommandSourceStack)context.getSource(), EntityArgument.getPlayers((CommandContext)context, (String)"player")))));
    }

    private static int openMenu(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MNACap.get(player, ParticleAuraProvider.AURA).ifPresent(a -> player.openMenu((MenuProvider)new NamedAuras(), (Consumer)a));
        return 1;
    }

    private static int completeProgression(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            IPlayerProgression progression = (IPlayerProgression)spe.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (progression == null) {
                return 0;
            }
            for (ProgressionCondition condition : ProgressionCondition.get((Level)source.getLevel(), progression.getTier(), new ArrayList<ResourceLocation>())) {
                progression.addTierProgressionComplete(condition.getId());
            }
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.completeProgression.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.completeProgression.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int resetCodex(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            IPlayerProgression progression = (IPlayerProgression)spe.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (progression == null) {
                return 0;
            }
            progression.resetCodexUnlocks();
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.resetCodex.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.resetCodex.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int resetProgression(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            IPlayerProgression progression = (IPlayerProgression)spe.getData(PlayerProgressionProvider.PROGRESSION.get());
            IPlayerRoteSpells rote = (IPlayerRoteSpells)spe.getData(PlayerRoteSpellsProvider.ROTE.get());
            IPlayerMagic magic = (IPlayerMagic)spe.getData(PlayerMagicProvider.MAGIC.get());
            if (progression == null || rote == null || magic == null) {
                return 0;
            }
            progression.setTier(0, (Player)spe);
            progression.setAlliedFaction(null, (Player)spe);
            progression.setAllyCooldown(0);
            progression.setTierProgression(new ArrayList<ResourceLocation>());
            progression.resetCodexUnlocks();
            rote.resetMastery();
            rote.resetRote();
            magic.setMagicLevel((Player)spe, 0);
            magic.setMagicXP(0);
            magic.setCastingResourceType(CastingResourceIDs.MANA);
            for (Affinity aff : Affinity.values()) {
                magic.setAffinityDepth(aff.getShiftAffinity(), 0.0f);
            }
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.fullReset.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.fullReset.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int makeWellspring(CommandSourceStack source, BlockPos position) {
        return CommandMna.makeWellspring(source, position, Affinity.values()[(int)(Math.random() * (double)Affinity.values().length)], (float)(5.0 + Math.random() * 20.0));
    }

    private static int makeWellspring(CommandSourceStack source, BlockPos position, Affinity aff) {
        return CommandMna.makeWellspring(source, position, aff, (float)(5.0 + Math.random() * 20.0));
    }

    private static int makeWellspring(CommandSourceStack source, BlockPos position, Affinity aff, float strength) {
        MutableBoolean success = new MutableBoolean(true);
        MNACap.get(source.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
            Optional<WellspringNode> existing = m.getWellspringRegistry().getNodeAt(position);
            if (existing.isPresent()) {
                source.sendFailure((Component)Component.translatable((String)"mna.commands.wellspring.create.nodeoverlap"));
                success.setFalse();
                return;
            }
            if (!m.getWellspringRegistry().addNode((Level)source.getLevel(), position, () -> new WellspringNode(aff, strength), true)) {
                source.sendFailure((Component)Component.translatable((String)"mna.commands.wellspring.create.failed", (Object[])new Object[]{position.toShortString()}));
                success.setFalse();
                return;
            }
        });
        if (success.booleanValue()) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspring.create.success", (Object[])new Object[]{aff.name(), position.toString()}), true);
        }
        return 0;
    }

    private static int deleteWellspring(CommandSourceStack source, BlockPos position) {
        MutableBoolean success = new MutableBoolean(true);
        MNACap.get(source.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> success.setValue(m.getWellspringRegistry().deleteNodeAt(position)));
        if (success.booleanValue()) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspring.delete.success", (Object[])new Object[]{position.toString()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspring.delete.failed", (Object[])new Object[]{position.toString()}), true);
        }
        return 0;
    }

    private static int addRote(CommandSourceStack source, Collection<ServerPlayer> players, ISpellComponent component) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> r.addRoteXP(null, component, component.requiredXPForRote()));
        }
        MutableComponent comp = Component.translatable((String)component.getRegistryName().toString());
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.success", (Object[])new Object[]{comp.getString(), ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.success", (Object[])new Object[]{comp.getString(), players.size()}), true);
        }
        return 1;
    }

    private static int addRote_Shapes(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> Registries.Shape.getValues().forEach(s -> r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote())));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.shapes.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.shapes.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addRote_SpellEffects(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> Registries.SpellEffect.getValues().forEach(s -> r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote())));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.components.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.components.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addRote_Modifiers(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> Registries.Modifier.getValues().forEach(s -> r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote())));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.modifiers.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.modifiers.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addRote_All(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                Registries.Shape.getValues().forEach(s -> r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote()));
                Registries.SpellEffect.getValues().forEach(s -> r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote()));
                Registries.Modifier.getValues().forEach(s -> r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote()));
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.all.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.all.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int resetRote(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> r.resetRote());
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.reset.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.rote.reset.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addMastery(CommandSourceStack source, Collection<ServerPlayer> players, ISpellComponent component) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> r.addMastery(null, component, 0.5f));
        }
        MutableComponent comp = Component.translatable((String)component.getRegistryName().toString());
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.success", (Object[])new Object[]{comp.getString(), ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.success", (Object[])new Object[]{comp.getString(), players.size()}), true);
        }
        return 1;
    }

    private static int addMastery_Shapes(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> Registries.Shape.getValues().forEach(s -> r.addMastery(null, (ISpellComponent)s, 0.5f)));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.shapes.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.shapes.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addMastery_SpellEffects(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> Registries.SpellEffect.getValues().forEach(s -> r.addMastery(null, (ISpellComponent)s, 0.5f)));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.components.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.components.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addMastery_Modifiers(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> Registries.Modifier.getValues().forEach(s -> r.addMastery(null, (ISpellComponent)s, 0.5f)));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.modifiers.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.modifiers.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int addMastery_All(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                Registries.Shape.getValues().forEach(s -> r.addMastery(null, (ISpellComponent)s, 0.5f));
                Registries.SpellEffect.getValues().forEach(s -> r.addMastery(null, (ISpellComponent)s, 0.5f));
                Registries.Modifier.getValues().forEach(s -> r.addMastery(null, (ISpellComponent)s, 0.5f));
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.all.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.all.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int resetMastery(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> r.resetMastery());
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.reset.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.mastery.reset.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int factionRaid(CommandSourceStack source, ResourceLocation factionID, Collection<ServerPlayer> players, boolean force) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            IPlayerProgression progression = (IPlayerProgression)spe.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (progression == null) {
                return 0;
            }
            if (!force && progression.getTier() < 3) {
                return 0;
            }
            IFaction faction = (IFaction)Registries.Factions.getValue(factionID);
            if (faction == null) {
                return 0;
            }
            if (force) {
                progression.raidImmediate(faction);
                continue;
            }
            progression.setRaidChance(faction, 1.0);
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.factionRaid.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.factionRaid.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }

    private static int setWellspringPower(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff, int level) {
        MNACap.get(source.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> players.forEach(player -> m.getWellspringRegistry().setWellspringPower((ServerPlayer)player, aff, level)));
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspringpower.set.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName(), aff.name(), level}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspringpower.set.success", (Object[])new Object[]{players.size(), aff.name(), level}), true);
        }
        return 1;
    }

    private static int addWellspringPower(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff, int amount) {
        MNACap.get(source.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> players.forEach(player -> {
            if (player.getGameProfile() != null && player.getGameProfile().getId() != null) {
                m.getWellspringRegistry().insertPower(player.getGameProfile().getId(), player.level(), aff, amount);
            }
        }));
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspringpower.add.success", (Object[])new Object[]{amount, aff.name(), ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspringpower.add.success", (Object[])new Object[]{amount, aff.name(), players.size()}), true);
        }
        return 1;
    }

    private static int removeWellspringPower(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff, int amount) {
        MNACap.get(source.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> players.forEach(player -> {
            if (player.getGameProfile() != null && player.getGameProfile().getId() != null) {
                m.getWellspringRegistry().consumePower(player.getGameProfile().getId(), player.level(), aff, amount);
            }
        }));
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspringpower.remove.success", (Object[])new Object[]{amount, aff.name(), ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.wellspringpower.remove.success", (Object[])new Object[]{amount, aff.name(), players.size()}), true);
        }
        return 1;
    }

    private static int setTier(CommandSourceStack source, Collection<ServerPlayer> players, int tier) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> progression.setTier(tier, (Player)spe));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.tier.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName(), tier}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.tier.success", (Object[])new Object[]{players.size(), tier}), true);
        }
        return 1;
    }

    private static int setFaction(CommandSourceStack source, Collection<ServerPlayer> players, ResourceLocation factionID, int castingResourceIndex) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        IFaction faction = (IFaction)Registries.Factions.getValue(factionID);
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> progression.setAlliedFaction(faction, (Player)spe));
            if (faction == null) continue;
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                int loopedIndex = castingResourceIndex % faction.getCastingResources().length;
                magic.setCastingResourceType(faction.getCastingResources()[loopedIndex]);
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.faction.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName(), factionID}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.faction.success", (Object[])new Object[]{players.size(), factionID}), true);
        }
        return 1;
    }

    private static int setMagicLevel(CommandSourceStack source, Collection<ServerPlayer> players, int magicLevel) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                magic.setMagicLevel((Player)spe, magicLevel);
                magic.setMagicXP(magic.getXPForLevel(magicLevel));
                magic.getCastingResource().setAmount(magic.getCastingResource().getMaxAmount());
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.magicLevel.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName(), magicLevel}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.magicLevel.success", (Object[])new Object[]{players.size(), magicLevel}), true);
        }
        return 1;
    }

    private static int setAffinity(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff, float amount) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                magic.setAffinityDepth(aff.getShiftAffinity(), amount);
                magic.forceSync();
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.set.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName(), aff, Float.valueOf(amount)}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.set.success", (Object[])new Object[]{players.size(), aff, Float.valueOf(amount)}), true);
        }
        return 1;
    }

    private static int addAffinity(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff, float amount) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                magic.setAffinityDepth(aff.getShiftAffinity(), magic.getAffinityDepth(aff.getShiftAffinity()) + amount);
                magic.forceSync();
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.add.success", (Object[])new Object[]{Float.valueOf(amount), aff, ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.add.success", (Object[])new Object[]{Float.valueOf(amount), aff, players.size()}), true);
        }
        return 1;
    }

    private static int removeAffinity(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff, float amount) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                magic.setAffinityDepth(aff.getShiftAffinity(), magic.getAffinityDepth(aff.getShiftAffinity()) - amount);
                magic.forceSync();
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.remove.success", (Object[])new Object[]{Float.valueOf(amount), aff, ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.remove.success", (Object[])new Object[]{Float.valueOf(amount), aff, players.size()}), true);
        }
        return 1;
    }

    private static int resetAffinity(CommandSourceStack source, Collection<ServerPlayer> players, Affinity aff) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                magic.setAffinityDepth(aff.getShiftAffinity(), 0.0f);
                magic.forceSync();
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.reset.success", (Object[])new Object[]{aff, ((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.reset.success", (Object[])new Object[]{aff, players.size()}), true);
        }
        return 1;
    }

    private static int resetAllAffinity(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players == null || players.size() == 0) {
            return 0;
        }
        for (ServerPlayer spe : players) {
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                for (Affinity aff : Affinity.values()) {
                    magic.setAffinityDepth(aff.getShiftAffinity(), 0.0f);
                }
                magic.forceSync();
            });
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.reset_all.success", (Object[])new Object[]{((ServerPlayer)players.iterator().next()).getDisplayName()}), true);
        } else {
            source.sendSuccess(() -> Component.translatable((String)"mna.commands.affinity.reset_all.success", (Object[])new Object[]{players.size()}), true);
        }
        return 1;
    }
}


