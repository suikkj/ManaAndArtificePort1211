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

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.BaseFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
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

public class Demons
extends BaseFaction {
    public Demons() {
        super(CastingResourceIDs.BRIMSTONE);
    }

    @Override
    public float getMaxModifierBonus(Attribute attr) {
        switch (attr) {
            case DAMAGE: {
                return GeneralConfigValues.DemonBonusDamageMod;
            }
            case RANGE: {
                return GeneralConfigValues.DemonBonusRangeMod;
            }
            case WIDTH: 
            case RADIUS: 
            case HEIGHT: 
            case DEPTH: {
                return GeneralConfigValues.DemonBonusRadiusMod;
            }
        }
        return 0.0f;
    }

    @Override
    public float getMinModifierBonus(Attribute attr) {
        return 0.0f;
    }

    @Override
    public ItemStack getFactionGrimoire() {
        return new ItemStack((ItemLike)ItemInit.GRIMOIRE_DEMON.get());
    }

    @Override
    public SoundEvent getRaidSound() {
        return SFX.Event.Faction.FACTION_RAID_DEMONS;
    }

    @Override
    public Component getOcculusTaskPrompt(int tier) {
        return Component.translatable((String)"mna:rituals/burning_hells");
    }

    @Override
    public ResourceLocation getFactionIcon() {
        return GuiTextures.Widgets.FACTION_ICON_DEMONS;
    }

    @Override
    public SoundEvent getHornSound() {
        return SFX.Event.Faction.FACTION_HORN_DEMONS;
    }

    @Override
    public int[] getManaweaveRGB() {
        return Affinity.FIRE.getColor();
    }

    @Override
    public ChatFormatting getTornJournalPageFactionColor() {
        return ChatFormatting.RED;
    }

    @Override
    public Item getTokenItem() {
        return (Item)ItemInit.MARK_OF_THE_NETHER.get();
    }

    @Override
    public ResourceLocation getSanctumStructure() {
        return RLoc.create("multiblock/demon_circle");
    }
}

