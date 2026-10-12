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

public class FeyCourt
extends BaseFaction {
    private static final int[] manaweave_a = new int[]{37, 135, 58};
    private static final int[] manaweave_b = new int[]{213, 112, 225};

    public FeyCourt() {
        super(CastingResourceIDs.SUMMER_FIRE, CastingResourceIDs.WINTER_ICE);
    }

    @Override
    public ItemStack getFactionGrimoire() {
        return new ItemStack((ItemLike)ItemInit.GRIMOIRE_FEY.get());
    }

    @Override
    public SoundEvent getRaidSound() {
        return SFX.Event.Faction.FACTION_RAID_FEY;
    }

    @Override
    public Component getOcculusTaskPrompt(int tier) {
        return Component.translatable((String)"mna:rituals/faerie_courts");
    }

    @Override
    public ResourceLocation getFactionIcon() {
        return GuiTextures.Widgets.FACTION_ICON_FEY;
    }

    @Override
    public SoundEvent getHornSound() {
        return SFX.Event.Faction.FACTION_HORN_FEY;
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
        return ChatFormatting.GREEN;
    }

    @Override
    public Item getTokenItem() {
        return (Item)ItemInit.MARK_OF_THE_FEY.get();
    }

    @Override
    public ResourceLocation getSanctumStructure() {
        return RLoc.create("multiblock/fey_circle");
    }
}

