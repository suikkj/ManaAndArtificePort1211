/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.commands.synchronization.ArgumentTypeInfo
 *  net.minecraft.commands.synchronization.ArgumentTypeInfos
 *  net.minecraft.commands.synchronization.SingletonArgumentInfo
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.commands;

import com.mna.api.commands.AffinityArgument;
import com.mna.api.commands.FactionArgument;
import com.mna.api.commands.SpellPartArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CommandSerializerInit {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENTS = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, "mna");
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> SPELL_PART_ARG = ARGUMENTS.register("spell_part_arg", () -> ArgumentTypeInfos.registerByClass(SpellPartArgument.class, (ArgumentTypeInfo)SingletonArgumentInfo.contextFree(SpellPartArgument::spell)));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> AFFINITY_ARG = ARGUMENTS.register("affinity_arg", () -> ArgumentTypeInfos.registerByClass(AffinityArgument.class, (ArgumentTypeInfo)SingletonArgumentInfo.contextFree(AffinityArgument::affinity)));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> FACTION_ARG = ARGUMENTS.register("faction_arg", () -> ArgumentTypeInfos.registerByClass(FactionArgument.class, (ArgumentTypeInfo)SingletonArgumentInfo.contextFree(FactionArgument::faction)));
}


