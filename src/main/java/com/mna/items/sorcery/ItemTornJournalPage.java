 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.faction.IFaction;
import com.mna.api.items.TieredItem;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.items.ItemInit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ItemTornJournalPage
extends TieredItem {
    public static final String NBT_COMPONENT = "part";
    public static final String NBT_THESIS = "thesis";
    private final boolean isThesis;

    public ItemTornJournalPage(boolean isThesis) {
        super(new Item.Properties().stacksTo(16).rarity(isThesis ? Rarity.UNCOMMON : Rarity.RARE));
        this.isThesis = isThesis;
    }

    public void appendHoverText(ItemStack p_41421_, net.minecraft.world.item.Item.TooltipContext p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        ISpellComponent comp = this.getComponent(p_41421_);
        if (comp != null) {
            IPlayerRoteSpells rote;
            ChatFormatting color = ChatFormatting.WHITE;
            if (comp.getFactionRequirement() != null) {
                color = comp.getFactionRequirement().getTornJournalPageFactionColor();
            }
            if (this.isThesis) {
                p_41423_.add((Component)Component.translatable((String)"item.mna.spell_part_thesis.contains").withStyle(ChatFormatting.ITALIC).append((Component)Component.translatable((String)comp.getRegistryName().toString()).withStyle(color)));
            } else {
                p_41423_.add((Component)Component.translatable((String)"item.mna.torn_journal_page.contains").withStyle(ChatFormatting.ITALIC).append((Component)Component.translatable((String)comp.getRegistryName().toString()).withStyle(color)));
            }
            Player p = ManaAndArtifice.instance.proxy.getClientPlayer();
            if (p != null && (rote = (IPlayerRoteSpells)p.getData(PlayerRoteSpellsProvider.ROTE.get())) != null) {
                if (rote.isRote(comp)) {
                    p_41423_.add((Component)Component.translatable((String)"item.mna.torn_journal_page.rote").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GOLD));
                } else {
                    p_41423_.add((Component)Component.translatable((String)"item.mna.torn_journal_page.not_rote").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GOLD));
                }
            }
        } else {
            p_41423_.add((Component)Component.translatable((String)"item.mna.torn_journal_page.empty").withStyle(ChatFormatting.ITALIC));
        }
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }

    @Nullable
    public ISpellComponent getComponent(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_COMPONENT)) {
            return null;
        }
        ResourceLocation rLoc = ResourceLocation.parse(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(NBT_COMPONENT));
        SpellEffect effect = (SpellEffect)Registries.SpellEffect.getValue(rLoc);
        if (effect != null) {
            return effect;
        }
        Shape shape = (Shape)Registries.Shape.getValue(rLoc);
        if (shape != null) {
            return shape;
        }
        return (ISpellComponent)Registries.Modifier.getValue(rLoc);
    }

    public boolean isThesis(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(NBT_THESIS);
    }

    public void setComponent(ItemStack stack, ISpellComponent component) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_COMPONENT, component.getRegistryName().toString());
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean(NBT_THESIS, this.isThesis);
    }

    public void setRandomComponent(ItemStack stack, @Nullable IFaction faction) {
        ArrayList all = new ArrayList();
        SpellCraftingContext ctx = new SpellCraftingContext(null);
        all.addAll(Registries.Shape.getValues().stream().filter(s -> {
            if (s.isSilverSpell() || !s.isCraftable(ctx)) {
                return false;
            }
            return faction == null || s.getFactionRequirement() == faction;
        }).collect(Collectors.toList()));
        all.addAll(Registries.SpellEffect.getValues().stream().filter(s -> {
            if (s.getUseTag() == SpellPartTags.DONOTUSE || s.isSilverSpell() || !s.isCraftable(ctx)) {
                return false;
            }
            return faction == null || s.getFactionRequirement() == faction;
        }).collect(Collectors.toList()));
        all.addAll(Registries.Modifier.getValues().stream().filter(s -> {
            if (s.isSilverSpell() || !s.isCraftable(ctx)) {
                return false;
            }
            return faction == null || s.getFactionRequirement() == faction;
        }).collect(Collectors.toList()));
        this.setComponent(stack, (ISpellComponent)all.get((int)(Math.random() * (double)all.size())));
    }

    public static ItemStack getRandomPage(RandomSource random) {
        ItemStack rnd = new ItemStack((ItemLike)ItemInit.TORN_JOURNAL_PAGE.get());
        ((ItemTornJournalPage)ItemInit.TORN_JOURNAL_PAGE.get()).setRandomComponent(rnd, null);
        return rnd;
    }

    public static ItemStack getRandomPage(Random random, @Nullable IFaction faction) {
        ItemStack rnd = new ItemStack((ItemLike)ItemInit.TORN_JOURNAL_PAGE.get());
        ((ItemTornJournalPage)ItemInit.TORN_JOURNAL_PAGE.get()).setRandomComponent(rnd, faction);
        return rnd;
    }
}

