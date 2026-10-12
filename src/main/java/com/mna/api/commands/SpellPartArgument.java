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
import com.mna.api.spells.ISpellHelper;
import com.mna.api.spells.base.ISpellComponent;
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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SpellPartArgument
implements ArgumentType<ISpellComponent> {
    private static final Collection<String> EXAMPLES = Arrays.asList("mna:shapes/touch", "mna:components/break", "mna:modifiers/damage");
    public static final DynamicCommandExceptionType PART_BAD_ID = new DynamicCommandExceptionType(p_208696_0_ -> Component.translatable((String)"argument.item.id.invalid", (Object[])new Object[]{p_208696_0_}));

    public static SpellPartArgument spell() {
        return new SpellPartArgument();
    }

    public ISpellComponent parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        ResourceLocation resourcelocation = ResourceLocation.read((StringReader)reader);
        ISpellHelper helper = ManaAndArtificeMod.getSpellHelper();
        if (helper.getShapeRegistry().containsKey(resourcelocation)) {
            return (ISpellComponent)ManaAndArtificeMod.getShapeRegistry().getValue(resourcelocation);
        }
        if (helper.getComponentRegistry().containsKey(resourcelocation)) {
            return (ISpellComponent)ManaAndArtificeMod.getComponentRegistry().getValue(resourcelocation);
        }
        if (helper.getModifierRegistry().containsKey(resourcelocation)) {
            return (ISpellComponent)ManaAndArtificeMod.getModifierRegistry().getValue(resourcelocation);
        }
        reader.setCursor(i);
        throw PART_BAD_ID.createWithContext((ImmutableStringReader)reader, resourcelocation.toString());
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        ArrayList all = new ArrayList();
        all.addAll(ManaAndArtificeMod.getShapeRegistry().getKeys());
        all.addAll(ManaAndArtificeMod.getComponentRegistry().getKeys());
        all.addAll(ManaAndArtificeMod.getModifierRegistry().getKeys());
        return SharedSuggestionProvider.suggestResource(all, (SuggestionsBuilder)builder);
    }

    public static <S> ISpellComponent getSpell(CommandContext<S> context, String name) {
        return (ISpellComponent)context.getArgument(name, ISpellComponent.class);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

