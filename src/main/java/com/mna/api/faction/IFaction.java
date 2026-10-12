/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.faction;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.events.FactionAllegianceEvent;
import com.mna.api.spells.attributes.Attribute;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public interface IFaction {
    public ResourceLocation[] getCastingResources();

    public ResourceLocation getCastingResource(Player var1);

    default public boolean is(ResourceLocation factionId) {
        return ManaAndArtificeMod.getFactionsRegistry().getValue(factionId) == this;
    }

    default public float getMaxModifierBonus(Attribute attr) {
        return 0.0f;
    }

    default public float getMinModifierBonus(Attribute attr) {
        return 0.0f;
    }

    default public List<IFaction> getEnemyFactions() {
        return ManaAndArtificeMod.getFactionHelper().getFactionsExcept(this);
    }

    default public List<IFaction> getAlliedFactions() {
        FactionAllegianceEvent event = new FactionAllegianceEvent(this, Arrays.asList(this));
        NeoForge.EVENT_BUS.post(event);
        return event.getAllegiances();
    }

    default public boolean isAlliedTo(IFaction other) {
        return this.getAlliedFactions().contains(other);
    }

    @Nullable
    default public ResourceLocation getSanctumStructure() {
        return null;
    }

    public ItemStack getFactionGrimoire();

    public Item getTokenItem();

    public SoundEvent getRaidSound();

    @Nullable
    public SoundEvent getHornSound();

    public Component getOcculusTaskPrompt(int var1);

    public ResourceLocation getFactionIcon();

    default public int getFactionIconTextureSize() {
        return 8;
    }

    @Nullable
    public int[] getManaweaveRGB();

    public ChatFormatting getTornJournalPageFactionColor();
}


