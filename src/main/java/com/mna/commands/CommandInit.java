/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  net.minecraft.commands.CommandSourceStack
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 *  net.neoforged.bus.api.SubscribeEvent
 */
package com.mna.commands;

import com.mna.ManaAndArtifice;
import com.mna.commands.CommandMna;
import com.mna.commands.CommandStructureDiff;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class CommandInit {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        CommandMna.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        if (ManaAndArtifice.instance.isDebug) {
            CommandStructureDiff.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        }
    }
}


