/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.factions;

import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.faction.BaseFaction;
import com.mna.api.sound.SFX;
import com.mna.api.tools.RLoc;
import com.mna.gui.GuiTextures;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class Undead
extends BaseFaction {
    private static final int[] manaweave_a = new int[]{125, 201, 198};
    private static final int[] manaweave_b = new int[]{135, 126, 201};

    public Undead() {
        super(CastingResourceIDs.SOULS);
    }

    @Override
    public ItemStack getFactionGrimoire() {
        return new ItemStack((ItemLike)ItemInit.GRIMOIRE_UNDEAD.get());
    }

    @Override
    public SoundEvent getRaidSound() {
        return SFX.Event.Faction.FACTION_RAID_DEMONS;
    }

    @Override
    public Component getOcculusTaskPrompt(int tier) {
        return Component.translatable((String)"mna:rituals/cold_dark");
    }

    @Override
    public ResourceLocation getFactionIcon() {
        return GuiTextures.Widgets.FACTION_ICON_UNDEAD;
    }

    @Override
    public SoundEvent getHornSound() {
        return SFX.Event.Faction.FACTION_HORN_UNDEAD;
    }

    @Override
    public int[] getManaweaveRGB() {
        if (Math.random() < 0.5) {
            return manaweave_a;
        }
        return manaweave_b;
    }

    @Override
    public ChatFormatting getTornJournalPageFactionColor() {
        return ChatFormatting.DARK_GRAY;
    }

    @Override
    public Item getTokenItem() {
        return (Item)ItemInit.MARK_OF_THE_UNDEAD.get();
    }

    @Override
    public ResourceLocation getSanctumStructure() {
        return RLoc.create("multiblock/undead_circle");
    }
}

