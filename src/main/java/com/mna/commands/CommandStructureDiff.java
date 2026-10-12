/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.commands;

import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.multiblock.MultiblockConfiguration;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class CommandStructureDiff {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"structurediff").then(Commands.argument((String)"baseline", (ArgumentType)StringArgumentType.string()).then(Commands.argument((String)"variation", (ArgumentType)StringArgumentType.string()).executes(context -> CommandStructureDiff.computeDiff((CommandSourceStack)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"baseline"), StringArgumentType.getString((CommandContext)context, (String)"variation"))))));
    }

    private static int computeDiff(CommandSourceStack source, String firstStructure, String secondStructure) {
        ServerLevel sw = source.getLevel();
        ArrayList<BlockState> blockStates = new ArrayList<BlockState>();
        MultiblockConfiguration first = MultiblockConfiguration.loadStructure(sw.getStructureManager(), ResourceLocation.parse(firstStructure), blockStates);
        MultiblockConfiguration second = MultiblockConfiguration.loadStructure(sw.getStructureManager(), ResourceLocation.parse(secondStructure), blockStates);
        if (!first.getIsValid()) {
            source.sendFailure((Component)Component.literal((String)"Unable to locate first structure"));
            return 0;
        }
        if (!second.getIsValid()) {
            source.sendFailure((Component)Component.literal((String)"Unable to locate second structure"));
            return 0;
        }
        JsonObject diff = first.computeRecipeDiff(second, blockStates);
        Supplier<Component> response = () -> Component.literal((String)diff.toString());
        ManaAndArtifice.LOGGER.always().log(diff.toString());
        source.sendSuccess(response, true);
        return 1;
    }
}

