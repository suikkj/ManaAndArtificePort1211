/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.commands;

import com.mna.api.ManaAndArtificeMod;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FactionArgument
implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = new ArrayList<String>();
    public static final DynamicCommandExceptionType PART_BAD_ID = new DynamicCommandExceptionType(p_208696_0_ -> Component.translatable((String)"argument.item.id.invalid", (Object[])new Object[]{p_208696_0_}));

    public static final FactionArgument faction() {
        return new FactionArgument();
    }

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        String inputString = reader.readString();
        try {
            return ResourceLocation.parse(inputString);
        }
        catch (Exception exception) {
            reader.setCursor(i);
            throw PART_BAD_ID.createWithContext((ImmutableStringReader)reader, inputString.toString());
        }
    }

    private Collection<String> populateAndGetExamples() {
        if (EXAMPLES.size() == 0) {
            EXAMPLES.add("\"mna:none\"");
            ManaAndArtificeMod.getFactionsRegistry().getKeys().forEach(r -> EXAMPLES.add("\"" + r.toString() + "\""));
        }
        return EXAMPLES;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        ArrayList<String> all = new ArrayList<String>();
        all.addAll(this.populateAndGetExamples());
        return SharedSuggestionProvider.suggest(all, (SuggestionsBuilder)builder);
    }

    public static <S> ResourceLocation getFaction(CommandContext<S> context, String name) {
        return (ResourceLocation)context.getArgument(name, ResourceLocation.class);
    }

    public Collection<String> getExamples() {
        return this.populateAndGetExamples();
    }
}

