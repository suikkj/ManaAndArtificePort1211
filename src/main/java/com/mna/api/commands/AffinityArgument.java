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
 */
package com.mna.api.commands;

import com.mna.api.affinity.Affinity;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class AffinityArgument
implements ArgumentType<Affinity> {
    private static final Collection<String> EXAMPLES = Arrays.asList(Affinity.values()).stream().map(a -> a.name()).collect(Collectors.toList());
    public static final DynamicCommandExceptionType PART_BAD_ID = new DynamicCommandExceptionType(p_208696_0_ -> Component.translatable((String)"argument.item.id.invalid", (Object[])new Object[]{p_208696_0_}));

    public static AffinityArgument affinity() {
        return new AffinityArgument();
    }

    public Affinity parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        String inputString = reader.readString();
        try {
            return Affinity.valueOf(inputString);
        }
        catch (Exception exception) {
            reader.setCursor(i);
            throw PART_BAD_ID.createWithContext((ImmutableStringReader)reader, inputString.toString());
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        ArrayList<String> all = new ArrayList<String>();
        all.addAll(EXAMPLES);
        return SharedSuggestionProvider.suggest(all, (SuggestionsBuilder)builder);
    }

    public static <S> Affinity getAffinity(CommandContext<S> context, String name) {
        return (Affinity)(context.getArgument(name, Affinity.class));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

