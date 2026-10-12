/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 */
package com.mna.events;

import com.mna.api.cantrips.ICantrip;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.events.CantripCastEvent;
import com.mna.api.events.ManaweaveCraftingEvent;
import com.mna.api.events.ManaweavePatternDrawnEvent;
import com.mna.api.events.PlayerMagicLevelUpEvent;
import com.mna.api.events.RitualCompleteEvent;
import com.mna.api.events.RuneforgeCraftingEvent;
import com.mna.api.events.RuneforgeEnchantEvent;
import com.mna.api.events.RunescribeCraftingEvent;
import com.mna.api.events.SpellCastEvent;
import com.mna.api.events.SpellCraftedEvent;
import com.mna.api.events.construct.ConstructCraftedEvent;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.recipes.IManaweavingRecipe;
import com.mna.api.recipes.IRitualRecipe;
import com.mna.api.recipes.IRuneforgeRecipe;
import com.mna.api.recipes.IRunescribeRecipe;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public class EventDispatcher {
    public static void DispatchRitualComplete(IRitualRecipe ritual, NonNullList<RitualEffect> handlers, BlockPos center, List<ItemStack> reagents, Player caster) {
        RitualCompleteEvent evt = new RitualCompleteEvent(ritual, handlers, center, caster, reagents);
        NeoForge.EVENT_BUS.post((Event)evt);
    }

    public static boolean DispatchManaweaveCrafting(IManaweavingRecipe recipe, ItemStack output, Player crafter) {
        ManaweaveCraftingEvent evt = new ManaweaveCraftingEvent(recipe, output, crafter);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static boolean DispatchRuneforgeCraft(IRuneforgeRecipe recipe, ItemStack output, Player crafter) {
        RuneforgeCraftingEvent evt = new RuneforgeCraftingEvent(recipe, output, crafter);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static boolean DispatchRuneforgeEnchant(ItemStack output, Player crafter) {
        RuneforgeEnchantEvent evt = new RuneforgeEnchantEvent(output, crafter);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static boolean DispatchRunescribeCraft(IRunescribeRecipe recipe, ItemStack output, Player crafter) {
        RunescribeCraftingEvent evt = new RunescribeCraftingEvent(recipe, output, crafter);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static boolean DispatchManaweavePatternDrawn(IManaweavePattern pattern, LivingEntity caster) {
        ManaweavePatternDrawnEvent evt = new ManaweavePatternDrawnEvent(pattern, caster);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static boolean DispatchPlayerLevelUp(Player player, int newLevel) {
        PlayerMagicLevelUpEvent evt = new PlayerMagicLevelUpEvent(player, newLevel);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static boolean DispatchSpellCast(ISpellDefinition spell, SpellSource source, ItemStack stack, @Nullable SpellContext context) {
        SpellCastEvent evt = new SpellCastEvent(spell, source, stack, context);
        NeoForge.EVENT_BUS.post((Event)evt);
        return !evt.isCanceled();
    }

    public static void DispatchSpellCrafted(ISpellDefinition spell, Player caster) {
        SpellCraftedEvent evt = new SpellCraftedEvent(spell, caster);
        NeoForge.EVENT_BUS.post((Event)evt);
    }

    public static void DispatchConstructCrafted(IConstruct<?> construct, Player caster) {
        ConstructCraftedEvent evt = new ConstructCraftedEvent(construct, caster);
        NeoForge.EVENT_BUS.post((Event)evt);
    }

    public static void DispatchCantripCast(ICantrip cantrip, Player caster) {
        CantripCastEvent evt = new CantripCastEvent(cantrip, caster);
        NeoForge.EVENT_BUS.post((Event)evt);
    }
}


