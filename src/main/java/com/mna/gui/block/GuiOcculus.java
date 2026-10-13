 
package com.mna.gui.block;

import net.minecraft.core.component.DataComponents;
import com.google.common.collect.Lists;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.block.ContainerOcculus;
import com.mna.items.ItemInit;
import com.mna.network.ClientMessageDispatcher;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.progression.ProgressionCondition;
import com.mna.recipes.spells.ISpellComponentRecipe;
import com.mna.tools.render.GuiRenderUtils;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class GuiOcculus
extends GuiJEIDisable<ContainerOcculus> {
    private static final int ICON_WIDTH = 50;
    private static final int ICON_HEIGHT = 40;
    private static final int ICON_SPACING = 10;
    private static final int ICON_SHEET_SIZE = 256;
    float backgroundPositionX = 0.0f;
    float backgroundPositionY = 0.0f;
    int backgroundWidth = 1024;
    int backgroundHeight = 1024;
    int categoryButtonOffset = 0;
    int startTooltipY = 52;
    int tooltipYPos = 52;
    int minTooltipY = this.startTooltipY;
    float backgroundPositionXMax = 1000.0f;
    HashMap<IconTypes, OcculusEntryList> lists;
    OcculusEntryList currentList;
    List<Component> currentTooltip = new ArrayList<Component>();
    ArrayList<TextFormatData> currentProgressionTooltip = new ArrayList();
    IPlayerProgression progression;
    IPlayerMagic magic;
    IPlayerRoteSpells rote;
    TextFormatData tierConditions = null;

    public GuiOcculus(ContainerOcculus screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.lists = new HashMap();
    }

    protected void init() {
        super.init();
        ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
        this.clearWidgets();
        this.categoryButtonOffset = 0;
        Minecraft mc = Minecraft.getInstance();
        RecipeManager recipeManager = mc.level.getRecipeManager();
        ArrayList<ListEntry<AMRecipeBase>> spell_parts = new ArrayList<ListEntry<AMRecipeBase>>();
        this.getRecipes(recipeManager, IconTypes.SHAPE, RecipeInit.SHAPE_TYPE.get(), spell_parts, null);
        this.getRecipes(recipeManager, IconTypes.COMPONENT, RecipeInit.COMPONENT_TYPE.get(), spell_parts, null);
        this.getRecipes(recipeManager, IconTypes.MODIFIER, RecipeInit.MODIFIER_TYPE.get(), spell_parts, null);
        this.setupTierBadges(IconTypes.PROGRESSION, new ArrayList<ListEntry<AMRecipeBase>>(), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.GUIDE_BOOK.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:progression")));
        this.setupTierBadges(IconTypes.FACTION_AFFINITY, new ArrayList<ListEntry<AMRecipeBase>>(), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.MOTE_ARCANE.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:faction_affinity")));
        this.setupTierBadges(IconTypes.SHAPE, spell_parts, com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.VELLUM.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:sorcery")));
        this.setupTierBadges(IconTypes.RITUAL, this.getRecipes(recipeManager, IconTypes.RITUAL, RecipeInit.RITUAL_TYPE.get(), null), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.WIZARD_CHALK.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:rituals")));
        this.setupTierBadges(IconTypes.MANAWEAVE_PATTERN, this.getRecipes(recipeManager, IconTypes.MANAWEAVE_PATTERN, RecipeInit.MANAWEAVING_PATTERN_TYPE.get(), null), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:manaweave_patterns")));
        this.setupTierBadges(IconTypes.MANAWEAVE_ALTAR, this.getRecipes(recipeManager, IconTypes.MANAWEAVE_ALTAR, RecipeInit.MANAWEAVING_RECIPE_TYPE.get(), r -> !r.isEnchantment() && !(r.getResultItem().getItem() instanceof ItemConstructPart)), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)BlockInit.MANAWEAVING_ALTAR.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:manaweave_crafting")));
        this.setupTierBadges(IconTypes.ELDRIN_ALTAR, this.getRecipes(recipeManager, IconTypes.ELDRIN_ALTAR, RecipeInit.ELDRIN_ALTAR_TYPE.get(), r -> !(r.getResultItem().getItem() instanceof ItemConstructPart)), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)BlockInit.ELDRIN_ALTAR.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:eldrin_altar")));
        this.setupTierBadges(IconTypes.RUNESCRIBING, this.getRecipes(recipeManager, IconTypes.RUNESCRIBING, RecipeInit.RUNESCRIBING_TYPE.get(), null), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)BlockInit.RUNESCRIBING_TABLE.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:runescribing")));
        this.setupTierBadges(IconTypes.RUNIC_ANVIL, this.getRecipes(recipeManager, IconTypes.RUNIC_ANVIL, RecipeInit.RUNEFORGING_TYPE.get(), null), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)BlockInit.RUNIC_ANVIL.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:runesmithing")));
        List<ListEntry<AMRecipeBase>> construct_recipes = this.getRecipes(recipeManager, IconTypes.CONSTRUCTS, RecipeInit.MANAWEAVING_RECIPE_TYPE.get(), r -> !r.isEnchantment() && r.getResultItem().getItem() instanceof ItemConstructPart);
        construct_recipes.addAll(this.getRecipes(recipeManager, IconTypes.CONSTRUCTS, RecipeInit.ELDRIN_ALTAR_TYPE.get(), r -> r.getResultItem().getItem() instanceof ItemConstructPart));
        this.setupTierBadges(IconTypes.CONSTRUCTS, construct_recipes, com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.CONSTRUCT_BASIC_HEAD_OBSIDIAN.get()), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:constructs")));
        this.setupTierBadges(IconTypes.ENCHANTMENT, this.getRecipes(recipeManager, IconTypes.ENCHANTMENT, RecipeInit.MANAWEAVING_RECIPE_TYPE.get(), r -> r.isEnchantment()), com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)Items.ENCHANTED_BOOK), DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mechanic.mna:enchantments")));
        this.finalizeLists();
        this.setActiveList(IconTypes.PROGRESSION);
        this.setActiveButton((Button)this.children().stream().filter(e -> e instanceof ImageItemStackButton).map(e -> (ImageItemStackButton)(e)).findFirst().get());
        this.progression = (IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get());
        this.magic = (IPlayerMagic)this.minecraft.player.getData(PlayerMagicProvider.MAGIC.get());
        this.rote = (IPlayerRoteSpells)this.minecraft.player.getData(PlayerRoteSpellsProvider.ROTE.get());
        this.currentProgressionTooltip.clear();
        if (this.progression != null) {
            List<ProgressionCondition> conditions = ProgressionCondition.get((Level)this.minecraft.level, this.progression.getTier(), new ArrayList<ResourceLocation>());
            Collections.sort(conditions, new Comparator<ProgressionCondition>(){

                @Override
                public int compare(ProgressionCondition o1, ProgressionCondition o2) {
                    MutableComponent ttc1 = Component.translatable((String)o1.getId().toString());
                    MutableComponent ttc2 = Component.translatable((String)o2.getId().toString());
                    return ttc1.getString().compareTo(ttc2.getString());
                }
            });
            List<ResourceLocation> completedConditions = this.progression.getCompletedProgressionSteps();
            this.tierConditions = this.progression.getTier() < 5 ? new TextFormatData((Component)Component.translatable((String)"gui.mna.tier_progress", (Object[])new Object[]{ProgressionCondition.getCompletionRequirementForTier((Level)this.minecraft.level, this.progression.getTier()), conditions.size()}), ChatFormatting.WHITE.getColor()) : null;
            for (ProgressionCondition cond : conditions) {
                if (completedConditions.contains(cond.getId())) {
                    this.currentProgressionTooltip.add(new TextFormatData(cond.getDescription(), ChatFormatting.GREEN.getColor()));
                    continue;
                }
                this.currentProgressionTooltip.add(new TextFormatData(cond.getDescription(), ChatFormatting.RED.getColor()));
            }
            if (this.progression.getTierProgress((Level)this.minecraft.level) >= 1.0f) {
                int advancementColor = ChatFormatting.YELLOW.getColor();
                this.currentProgressionTooltip.add(new TextFormatData((Component)Component.literal((String)""), ChatFormatting.WHITE.getColor()));
                int tier = this.progression.getTier();
                if (tier == 1) {
                    this.currentProgressionTooltip.add(new TextFormatData((Component)Component.translatable((String)"mna:progresscondition.t1_complete"), advancementColor));
                } else if (tier == 2) {
                    this.currentProgressionTooltip.add(new TextFormatData((Component)Component.translatable((String)"mna:progresscondition.t2_complete"), advancementColor));
                    Registries.Factions.forEach(f -> this.currentProgressionTooltip.add(new TextFormatData(f.getOcculusTaskPrompt(tier), advancementColor)));
                } else {
                    this.currentProgressionTooltip.add(new TextFormatData((Component)Component.translatable((String)"mna:progresscondition.t3+_complete"), advancementColor));
                    this.currentProgressionTooltip.add(new TextFormatData(this.progression.getAlliedFaction().getOcculusTaskPrompt(tier), advancementColor));
                }
            }
        }
    }

    private <T extends AMRecipeBase> List<ListEntry<AMRecipeBase>> getRecipes(RecipeManager recipeManager, IconTypes iconType, RecipeType<T> recipeType, Function<T, Boolean> predicate) {
        return this.getRecipes(recipeManager, iconType, recipeType, null, predicate);
    }

    private <T extends AMRecipeBase> List<ListEntry<AMRecipeBase>> getRecipes(RecipeManager recipeManager, IconTypes iconType, RecipeType<T> recipeType, @Nullable List<ListEntry<AMRecipeBase>> appendTo, @Nullable Function<T, Boolean> predicate) {
        List<net.minecraft.world.item.crafting.RecipeHolder<T>> recipes = recipeManager.getAllRecipesFor(recipeType);
        if (appendTo == null) {
            appendTo = new ArrayList<ListEntry<AMRecipeBase>>();
        }
        for (net.minecraft.world.item.crafting.RecipeHolder<T> __h : recipes) {
            AMRecipeBase recipe = __h.value();
            if (predicate != null && !predicate.apply((T)recipe).booleanValue()) continue;
            appendTo.add(new ListEntry(recipe, iconType));
        }
        return appendTo;
    }

    private void setupViewButton(IconTypes iconType, ItemStack displayStack) {
        this.addRenderableWidget(new ImageItemStackButton(this.leftPos - 22, this.topPos + this.categoryButtonOffset, 22, 22, 51, 232, -22, GuiTextures.Blocks.OCCULUS_BORDER, 256, 256, button -> {
            this.setActiveList(iconType);
            this.setActiveButton(button);
        }, displayStack, this.minecraft.getItemRenderer(), false));
        this.categoryButtonOffset += 25;
    }

    private void setupTierBadges(IconTypes iconType, List<ListEntry<AMRecipeBase>> recipes, ItemStack displayStack) {
        OcculusEntryList oel = new OcculusEntryList(this.minecraft, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        this.lists.put(iconType, oel);
        Collections.sort(recipes, new Comparator<ListEntry<AMRecipeBase>>(){

            @Override
            public int compare(ListEntry<AMRecipeBase> o1, ListEntry<AMRecipeBase> o2) {
                Integer tier2;
                Integer tier1 = o1.getTier();
                int tierComp = tier1.compareTo(tier2 = Integer.valueOf(o2.getTier()));
                if (tierComp != 0) {
                    return tierComp;
                }
                String name1 = o1.getRecipe().getGuiRepresentationStack().getHoverName().getString();
                String name2 = o2.getRecipe().getGuiRepresentationStack().getHoverName().getString();
                return name1.compareTo(name2);
            }
        });
        for (ListEntry<AMRecipeBase> entry : recipes) {
            oel.addIcon(entry.getRecipe(), entry.getIcon());
        }
        this.addWidget(oel);
        this.setupViewButton(iconType, displayStack);
    }

    private void finalizeLists() {
        for (OcculusEntryList list : this.lists.values()) {
            list.finalize();
        }
    }

    private void setActiveList(IconTypes type) {
        this.currentList = this.lists.get(type);
        this.currentList.setScrollAmount(0.0);
    }

    private void setActiveButton(Button btn) {
        this.children().stream().forEach(l -> {
            if (l instanceof ImageItemStackButton) {
                ((ImageItemStackButton)(l)).setCurrent(l == btn);
            }
        });
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        return mouseX < (double)guiLeftIn || mouseY < (double)guiTopIn || mouseX >= (double)(guiLeftIn + this.imageWidth) || mouseY >= (double)(guiTopIn + this.imageHeight);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < (double)this.leftPos) {
            for (GuiEventListener b : this.children()) {
                if (!(b instanceof ImageItemStackButton) || !b.mouseClicked(mouseX, mouseY, button)) continue;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int pMouseX, int pMouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        int i = this.leftPos;
        int j = this.topPos;
        if (this.progression != null && (this.currentList == this.lists.get(IconTypes.PROGRESSION) || this.currentList == this.lists.get(IconTypes.FACTION_AFFINITY))) {
            pGuiGraphics.fillGradient(i + 2, j + 2, i + this.imageWidth, j + this.imageHeight, FastColor.ARGB32.color((int)255, (int)34, (int)0, (int)61), FastColor.ARGB32.color((int)255, (int)0, (int)0, (int)0));
        } else {
            pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_BACKGROUND, i + 2, j + 2, this.backgroundPositionX, this.backgroundPositionY, this.imageWidth - 4, this.imageHeight - 4, 100, 100);
            this.backgroundPositionX = (float)((double)this.backgroundPositionX + Math.cos((float)this.minecraft.level.getGameTime() / 200.0f) * (double)0.2f * (double)partialTick);
            this.backgroundPositionY = (float)((double)this.backgroundPositionY - Math.sin((float)this.minecraft.level.getGameTime() / 200.0f) * (double)0.2f * (double)partialTick);
        }
        if (this.currentList != null) {
            this.currentList.render(pGuiGraphics, pMouseX, pMouseY, partialTick);
        }
        if (this.progression != null) {
            int height = 210;
            pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_BORDER, i + this.imageWidth, j, 232.0f, 0.0f, 11, height, 256, 256);
            float fillPct = this.progression.getTierProgress(ManaAndArtifice.instance.proxy.getClientWorld());
            pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_BORDER, i + this.imageWidth, (int)((float)j + (float)height * (1.0f - fillPct)), 221.0f, 0.0f, 11, (int)((float)height * fillPct), 256, 256);
            pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_BORDER, i + this.imageWidth, j, 210.0f, 0.0f, 11, this.imageHeight, 256, 256);
            if (pMouseX >= i + this.imageWidth && pMouseX <= i + this.imageWidth + 11 && pMouseY >= j && pMouseY <= j + this.imageHeight) {
                if (this.progression.getTier() < 5) {
                    MutableComponent progressSummary = Component.translatable((String)"gui.mna.your_tier_progress", (Object[])new Object[]{this.progression.getCompletedProgressionSteps().size()});
                    pGuiGraphics.renderTooltip(this.font, (Component)progressSummary, pMouseX, pMouseY);
                } else {
                    pGuiGraphics.renderTooltip(this.font, (Component)Component.translatable((String)"gui.mna.your_tier_max"), pMouseX, pMouseY);
                }
            }
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int i = 0;
        int j = 0;
        int tooltipYStart = 30;
        int tooltipX = 10;
        int tooltipY = this.tooltipYPos;
        int maxWidth = this.imageWidth - 20;
        int totalTooltipHeight = 0;
        if (this.progression != null && this.currentList == this.lists.get(IconTypes.PROGRESSION)) {
            List split_lines;
            String gui_player_tier = I18n.get((String)"gui.mna.your_tier", (Object[])new Object[]{this.progression.getTier()});
            pGuiGraphics.drawString(this.font, gui_player_tier, 10, 10, 0xFFFFFF, false);
            if (this.tierConditions != null) {
                split_lines = this.font.getSplitter().splitLines(this.tierConditions.itc.getString(), maxWidth, Style.EMPTY);
                for (FormattedText line : (java.util.List<FormattedText>)split_lines) {
                    pGuiGraphics.drawString(this.font, line.getString(), tooltipX, tooltipYStart, this.tierConditions.color, false);
                    Objects.requireNonNull(this.font);
                    tooltipYStart += 9;
                }
            } else {
                Objects.requireNonNull(this.font);
                tooltipYStart += 9 * 2;
            }
            pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_BORDER, i, j + tooltipYStart, 0.0f, 0.0f, this.imageWidth, 2, 310, 312);
            if (this.progression.getTier() >= 5) {
                pGuiGraphics.drawString(this.font, Component.translatable((String)"gui.mna.your_tier_max").getString(), tooltipX, tooltipY, ChatFormatting.WHITE.getColor().intValue(), false);
            } else {
                for (TextFormatData tfd : this.currentProgressionTooltip) {
                    split_lines = this.font.getSplitter().splitLines(tfd.itc.getString(), maxWidth, Style.EMPTY);
                    for (FormattedText line : (java.util.List<FormattedText>)split_lines) {
                        if (tooltipY >= tooltipYStart) {
                            Objects.requireNonNull(this.font);
                            if (tooltipY <= this.imageHeight - 9) {
                                pGuiGraphics.drawString(this.font, line.getString(), tooltipX, tooltipY, tfd.color, false);
                            }
                        }
                        Objects.requireNonNull(this.font);
                        tooltipY += 9;
                        Objects.requireNonNull(this.font);
                        totalTooltipHeight += 9;
                    }
                    tooltipY += 2;
                    totalTooltipHeight += 2;
                }
            }
        } else if (this.progression != null && this.currentList == this.lists.get(IconTypes.FACTION_AFFINITY)) {
            int y = 10;
            int x = 10;
            String gui_player_faction = I18n.get((String)"gui.mna.your_faction", (Object[])new Object[0]);
            pGuiGraphics.drawString(this.font, gui_player_faction, x, y, 0xFFFFFF, false);
            x += this.font.width(gui_player_faction) + 3;
            if (this.progression.hasAlliedFaction()) {
                IFaction faction = this.progression.getAlliedFaction();
                pGuiGraphics.blit(faction.getFactionIcon(), x, y, 0.0f, 0.0f, faction.getFactionIconTextureSize(), faction.getFactionIconTextureSize(), faction.getFactionIconTextureSize(), faction.getFactionIconTextureSize());
            } else {
                gui_player_faction = I18n.get((String)"gui.mna.your_faction.none", (Object[])new Object[0]);
                pGuiGraphics.drawString(this.font, gui_player_faction, x, y, 0xFFFFFF, false);
            }
            y = 30;
            x = 10;
            String gui_player_affinity = I18n.get((String)"gui.mna.affinity", (Object[])new Object[0]);
            pGuiGraphics.drawString(this.font, gui_player_affinity, x, y, 0xFFFFFF, false);
            Objects.requireNonNull(this.font);
            y += 9;
            for (Affinity aff : Affinity.values()) {
                ItemStack affStack = GuiTextures.affinityIcons.get(aff);
                if (affStack.isEmpty()) continue;
                pGuiGraphics.renderItem(affStack, x, y);
                pGuiGraphics.drawString(this.font, String.format("%.1f%%", Float.valueOf(this.magic.getAffinityDepth(aff))), x += 20, y + 5, 0xFFFFFF, false);
                x = 10;
                y += 20;
            }
            MNACap.get(this.minecraft.level, WorldMagicProvider.MAGIC).ifPresent(m -> {
                int posY = 30;
                int posX = this.getXSize() - 30;
                String gui_power_network = I18n.get((String)"gui.mna.power_network", (Object[])new Object[0]);
                pGuiGraphics.drawString(this.font, gui_power_network, posX - this.font.width(gui_power_network) + 16, posY, 0xFFFFFF, false);
                Objects.requireNonNull(this.font);
                posY += 9;
                if (ManaAndArtifice.instance.proxy.getGameTicks() % 20L == 0L) {
                    ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
                }
                IWellspringNodeRegistry wsRegistry = m.getWellspringRegistry();
                HashMap<Affinity, Float> nodeAmounts = wsRegistry.getNodeNetworkAmountFor((Player)this.minecraft.player);
                for (Affinity aff : Affinity.values()) {
                    ItemStack affStack = GuiTextures.affinityIcons.get(aff);
                    if (affStack.isEmpty() || !nodeAmounts.containsKey(aff)) continue;
                    pGuiGraphics.renderItem(affStack, posX, posY);
                    float textScale = 0.75f;
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().scale(textScale, textScale, textScale);
                    float amt = nodeAmounts.get(aff).floatValue();
                    amt = (float)(Math.floor(amt * 10.0f) / 10.0);
                    MutableComponent line = Component.translatable((String)"gui.mna.power_network.stored", (Object[])new Object[]{Float.valueOf(amt)});
                    float mult = wsRegistry.getEldrinGenerationMultiplierFor((Player)this.minecraft.player, aff);
                    mult = (float)(Math.floor(mult * 10.0f) / 10.0);
                    MutableComponent genBonus = Component.translatable((String)"gui.mna.power_network.gen_bonus", (Object[])new Object[]{Float.valueOf(mult)});
                    int valueWidth = this.font.width((FormattedText)line);
                    pGuiGraphics.drawString(this.font, (Component)line, (int)((float)(posX - 5) / textScale) - valueWidth, (int)((float)(posY + 2) / textScale), 0xFFFFFF, false);
                    float f = posY;
                    Objects.requireNonNull(this.font);
                    posY = (int)(f + (float)(9 + 1) * textScale);
                    valueWidth = this.font.width((FormattedText)genBonus);
                    pGuiGraphics.drawString(this.font, (Component)genBonus, (int)((float)(posX - 5) / textScale) - valueWidth, (int)((float)(posY + 2) / textScale), 0xFFFFFF, false);
                    pGuiGraphics.pose().popPose();
                    posY += 20;
                }
            });
        }
        pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_BORDER, i, j, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 310, 312);
        this.minTooltipY = totalTooltipHeight <= this.tooltipYPos - tooltipY + this.imageHeight - tooltipYStart + 20 ? 52 : this.tooltipYPos - tooltipY + this.imageHeight - tooltipYStart + 20;
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.currentTooltip.clear();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (!this.currentTooltip.isEmpty()) {
            pGuiGraphics.renderTooltip(this.font, Lists.transform(this.currentTooltip, Component::getVisualOrderText), pMouseX, pMouseY);
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.backgroundPositionX = (float)((double)this.backgroundPositionX - deltaX);
        this.backgroundPositionY = (float)((double)this.backgroundPositionY - deltaY);
        for (OcculusEntryList list : this.lists.values()) {
            list.mouseScrolled(mouseX, mouseY, 0.0, deltaY / 20.0);
        }
        if (this.currentList == this.lists.get(IconTypes.PROGRESSION)) {
            this.tooltipYPos += (int)Math.ceil(deltaY);
            if (this.tooltipYPos > 52) {
                this.tooltipYPos = 52;
            }
            if (this.tooltipYPos < this.minTooltipY) {
                this.tooltipYPos = this.minTooltipY;
            }
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double p_scrollX) {
        for (OcculusEntryList list : this.lists.values()) {
            list.mouseScrolled(mouseX, mouseY, delta, 0.0);
        }
        if (this.currentList == this.lists.get(IconTypes.PROGRESSION)) {
            this.tooltipYPos += (int)Math.ceil(delta) * 10;
            if (this.tooltipYPos > 52) {
                this.tooltipYPos = 52;
            }
            if (this.tooltipYPos < this.minTooltipY) {
                this.tooltipYPos = this.minTooltipY;
            }
        }
        return true;
    }

    class TextFormatData {
        public Component itc;
        public int color;

        public TextFormatData(Component itc, int color) {
            this.itc = itc;
            this.color = color;
        }
    }

    static enum IconTypes {
        PROGRESSION(0, 0),
        FACTION_AFFINITY(0, 0),
        MANAWEAVE_PATTERN(0, 0),
        MANAWEAVE_ALTAR(50, 0),
        ELDRIN_ALTAR(50, 0),
        SHAPE(100, 0),
        COMPONENT(150, 0),
        MODIFIER(200, 0),
        RUNESCRIBING(0, 80),
        RUNIC_ANVIL(50, 80),
        ENCHANTMENT(100, 80),
        RITUAL(150, 80),
        CONSTRUCTS(200, 80);

        private int texStartX;
        private int texStartY;

        private IconTypes(int texStartX, int texStartY) {
            this.texStartX = texStartX;
            this.texStartY = texStartY;
        }

        public int getTexX() {
            return this.texStartX;
        }

        public int getTexY() {
            return this.texStartY;
        }
    }

    static class ListEntry<T extends AMRecipeBase> {
        private T recipe;
        private IconTypes iconType;
        public ListEntry(T recipe, IconTypes icon) {
            this.recipe = recipe;
            this.iconType = icon;
        }

        public int getTier() {
            return ((AMRecipeBase)this.recipe).getTier();
        }

        public IconTypes getIcon() {
            return this.iconType;
        }

        public T getRecipe() {
            return this.recipe;
        }
    }

    public class ImageItemStackButton
    extends MNAImageButton {
        final ItemStack iconStack;
        final ItemRenderer itemRenderer;
        final boolean includeTooltip;
        private boolean current;

        public ImageItemStackButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, Button.OnPress clickHandler, ItemStack displayStack, ItemRenderer itemRenderer) {
            this(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, displayStack, itemRenderer, true);
        }

        public ImageItemStackButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, Button.OnPress clickHandler, ItemStack displayStack, ItemRenderer itemRenderer, boolean displayFullTooltip) {
            super(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, displayStack.getHoverName());
            this.iconStack = displayStack;
            this.itemRenderer = itemRenderer;
            this.includeTooltip = displayFullTooltip;
        }

        public boolean isHoveredOrFocused() {
            return this.current;
        }

        public void setCurrent(boolean current) {
            this.current = current;
        }

        protected boolean clicked(double mouseX, double mouseY) {
            return super.clicked(mouseX, mouseY);
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.renderItem(this.iconStack, this.getX() + 3, this.getY() + 3);
            if (this.active && this.isHovered) {
                List lines;
                if (this.includeTooltip) {
                    lines = this.iconStack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, (Player)((GuiOcculus)GuiOcculus.this).minecraft.player, (TooltipFlag)TooltipFlag.Default.NORMAL);
                } else {
                    lines = new ArrayList<Component>();
                    lines.add(this.iconStack.getHoverName());
                }
                ArrayList<Component> tt = new ArrayList<Component>();
                for (Component comp : (java.util.List<Component>)lines) {
                    if (!this.includeTooltip) {
                        String unformatted = ChatFormatting.stripFormatting((String)comp.getString());
                        MutableComponent stc = Component.literal((String)unformatted);
                        stc.withStyle(ChatFormatting.ITALIC);
                        tt.add((Component)stc);
                        continue;
                    }
                    tt.add(comp);
                }
                GuiOcculus.this.currentTooltip = tt;
            }
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }
    }

    class OcculusEntryList
    extends ObjectSelectionList<OcculusEntryList.OcculusEntry> {
        private int x;
        private int y0;
        private int y1;
        private OcculusEntry current;

        public <T extends AMRecipeBase> OcculusEntryList(Minecraft mcIn, int x, int y, int widthIn, int heightIn) {
            super(mcIn, widthIn, heightIn, y, 50);
            this.x = x;
            this.y0 = y;
            this.y1 = y + heightIn;
            this.setRenderHeader(false, 50);
        }

        public <T extends AMRecipeBase> void addIcon(T recipe, IconTypes type) {
            if (this.current == null || this.current.countIcons() == 4 || recipe.getTier() > this.current.getTier()) {
                this.current = recipe instanceof ISpellComponentRecipe ? new OcculusSpellEntry(recipe.getTier()) : (recipe instanceof ManaweavingPattern ? new OcculusManaweaveEntry(recipe.getTier()) : new OcculusEntry(recipe.getTier()));
                this.addEntry(this.current);
            }
            if (this.current.getTier() > recipe.getTier()) {
                throw new InvalidParameterException("Cannot add a recipe for a lower tier.  Sort recipes ascending by tier for this to work.");
            }
            this.current.addOutputItem(recipe, type);
        }

        public void finalize() {
            for (OcculusEntry e : this.children()) {
                e.finalize();
            }
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            int xPos = this.getRowLeft();
            int yPos = this.y0 - (int)this.getScrollAmount();
            this.renderList(pGuiGraphics, xPos, yPos, pPartialTick);
            int lastTier = 0;
            int tierItemCount = 0;
            for (OcculusEntry entry : this.children()) {
                while (lastTier < entry.getTier()) {
                    ++lastTier;
                    if (yPos + 15 > this.y0) {
                        Objects.requireNonNull(this.minecraft.font);
                        if (yPos + 15 + 9 < this.y1) {
                            Object tierStr = String.format("Tier %d", lastTier);
                            if (tierItemCount == 0 && lastTier < entry.getTier()) {
                                tierStr = (String)tierStr + " (No Items)";
                            }
                            pGuiGraphics.drawString(this.minecraft.font, (String)tierStr, xPos, yPos + 15, 0xFFFFFF, false);
                        }
                    }
                    tierItemCount = 0;
                    yPos += 10;
                }
                ++tierItemCount;
                yPos += 50;
            }
        }

        protected void renderList(GuiGraphics pGuiGraphics, int p_238478_4_, int p_238478_5_, float partialTicks) {
            int count = this.getItemCount();
            for (int i = 0; i < count; ++i) {
                int top = this.getRowTop(i);
                int bottom = top + this.itemHeight;
                if (top < this.y0 || bottom > this.y1) continue;
                int j1 = this.itemHeight - 4;
                OcculusEntry e = (OcculusEntry)this.getEntry(i);
                e.render(pGuiGraphics, i, top, this.getRowLeft(), this.getRowWidth(), j1, p_238478_4_, p_238478_5_, false, partialTicks);
            }
        }

        public int getRowWidth() {
            return this.width - 40;
        }

        public int getRowLeft() {
            return this.x + 12;
        }

        protected int getRowTop(int p_230962_1_) {
            OcculusEntry oe = (OcculusEntry)(this.children().get(p_230962_1_));
            return this.y0 + 15 - (int)this.getScrollAmount() + p_230962_1_ * this.itemHeight + oe.getTier() * 10;
        }

        protected int getMaxPosition() {
            return (this.getItemCount() + 1) * this.itemHeight + 50;
        }

        public class OcculusEntry
        extends ObjectSelectionList.Entry<OcculusEntry> {
            private static final int MAX_ITEMS = 4;
            private ArrayList<ItemStack> outputItems;
            private ArrayList<AMRecipeBase> outputRecipes;
            private ArrayList<IconTypes> iconTypes;
            protected ItemRenderer itemRenderer;
            protected Font fontRenderer;
            protected int tier;

            public OcculusEntry(int tier) {
                this.itemRenderer = OcculusEntryList.this.minecraft.getItemRenderer();
                this.fontRenderer = ((OcculusEntryList)OcculusEntryList.this).minecraft.font;
                this.outputItems = new ArrayList();
                this.outputRecipes = new ArrayList();
                this.iconTypes = new ArrayList();
                this.tier = tier;
            }

            public void addOutputItem(AMRecipeBase recipe, IconTypes type) {
                if (this.outputItems.size() == 4) {
                    return;
                }
                if (recipe.getGuiRepresentationStack().isEmpty()) {
                    return;
                }
                this.outputItems.add(recipe.getGuiRepresentationStack());
                this.outputRecipes.add(recipe);
                this.iconTypes.add(type);
            }

            public void finalize() {
                while (this.outputItems.size() < 4) {
                    this.outputItems.add(ItemStack.EMPTY);
                }
            }

            public int countIcons() {
                return this.outputItems.size();
            }

            public int getTier() {
                return this.tier;
            }

            protected IFaction getFaction(int index) {
                return index >= this.outputRecipes.size() || this.outputRecipes.get(index) == null ? null : this.outputRecipes.get(index).getFactionRequirement();
            }

            public void render(GuiGraphics pGuiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
                for (int i = 0; i < 4 && !this.outputItems.get(i).isEmpty(); ++i) {
                    boolean tierMet;
                    if (GuiOcculus.this.progression == null) {
                        return;
                    }
                    boolean bl = tierMet = this.tier <= GuiOcculus.this.progression.getTier();
                    if (!tierMet) {
                        pGuiGraphics.setColor(0.5f, 0.0f, 0.0f, 1.0f);
                    } else {
                        pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    }
                    this.renderBackground(pGuiGraphics, left + i * 58, top, this.iconTypes.get(i), i, tierMet);
                    this.renderForeground(pGuiGraphics, left + i * 58, top, partialTicks, this.outputItems.get(i), i);
                }
            }

            protected void renderBackground(GuiGraphics pGuiGraphics, int x, int y, IconTypes type, int index, boolean tierMet) {
                pGuiGraphics.blit(GuiTextures.Blocks.OCCULUS_ICONS, x + 2, y + 2, (float)type.getTexX(), (float)type.getTexY(), 50, 40, 256, 256);
                IFaction faction = this.getFaction(index);
                if (faction != null) {
                    pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    pGuiGraphics.blit(faction.getFactionIcon(), x + 50 - faction.getFactionIconTextureSize(), y + 40 - faction.getFactionIconTextureSize(), 0.0f, 0.0f, faction.getFactionIconTextureSize(), faction.getFactionIconTextureSize(), faction.getFactionIconTextureSize(), faction.getFactionIconTextureSize());
                    if (!tierMet) {
                        pGuiGraphics.setColor(0.5f, 0.0f, 0.0f, 1.0f);
                    }
                }
            }

            public void renderForeground(GuiGraphics pGuiGraphics, int x, int y, float partialTick, ItemStack stack, int index) {
                pGuiGraphics.renderItem(stack, x + 19, y + 3);
                this.renderTextLines(pGuiGraphics, stack, x, y);
            }

            protected void renderTextLines(GuiGraphics pGuiGraphics, ItemStack stack, int x, int y) {
                ArrayList<Component> tooltip = new ArrayList<Component>();
                tooltip.add(stack.getHoverName());
                if (GuiOcculus.this.progression == null) {
                    return;
                }
                float scale = 0.7f;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().scale(scale, scale, 1.0f);
                int yCoord = y + 22;
                for (Component tc : tooltip) {
                    List lines = this.fontRenderer.getSplitter().splitLines((FormattedText)tc, (int)(47.0f / scale), Style.EMPTY);
                    for (FormattedText line : (java.util.List<FormattedText>)lines) {
                        int len = (int)((float)this.fontRenderer.width(line.getString()) * scale);
                        int xCoord = x + 2 + 25 - len / 2;
                        pGuiGraphics.drawString(((OcculusEntryList)OcculusEntryList.this).minecraft.font, line.getString(), (int)((float)xCoord / scale), (int)((float)yCoord / scale), 0xFFFFFF, false);
                        float f = yCoord;
                        Objects.requireNonNull(this.fontRenderer);
                        yCoord = (int)(f + 9.0f * scale);
                    }
                }
                pGuiGraphics.pose().popPose();
            }

            public Component getNarration() {
                return Component.translatable((String)"gui.mna.occulus.select");
            }
        }

        public class OcculusSpellEntry
        extends OcculusEntry {
            ArrayList<ISpellComponent> spellParts;

            public OcculusSpellEntry(int tier) {
                super(tier);
                this.spellParts = new ArrayList();
            }

            @Override
            public void addOutputItem(AMRecipeBase recipe, IconTypes type) {
                super.addOutputItem(recipe, type);
                if (recipe instanceof ISpellComponentRecipe) {
                    ResourceLocation spellRLoc;
                    ISpellComponent comp = ((ISpellComponentRecipe)(recipe)).getComponent();
                    ResourceLocation resourceLocation = spellRLoc = comp != null ? comp.getRegistryName() : ResourceLocation.parse("");
                    if (Registries.Shape.containsKey(spellRLoc)) {
                        this.spellParts.add((ISpellComponent)Registries.Shape.getValue(spellRLoc));
                    } else if (Registries.SpellEffect.containsKey(spellRLoc)) {
                        this.spellParts.add((ISpellComponent)Registries.SpellEffect.getValue(spellRLoc));
                    } else if (Registries.Modifier.containsKey(spellRLoc)) {
                        this.spellParts.add((ISpellComponent)Registries.Modifier.getValue(spellRLoc));
                    }
                }
            }

            @Override
            protected void renderBackground(GuiGraphics pGuiGraphics, int x, int y, IconTypes type, int index, boolean tierMet) {
                ISpellComponent comp;
                super.renderBackground(pGuiGraphics, x, y, type, index, tierMet);
                if (index < this.spellParts.size() && (comp = this.spellParts.get(index)) != null && GuiOcculus.this.rote != null) {
                    int color = GuiOcculus.this.rote.isRote(comp) ? FastColor.ARGB32.color((int)255, (int)0, (int)255, (int)0) : FastColor.ARGB32.color((int)255, (int)237, (int)230, (int)9);
                    float prog = GuiOcculus.this.rote.getRoteProgression(comp);
                    if (prog > 0.0f && prog < 0.02f) {
                        prog = 0.02f;
                    }
                    int width = (int)(prog * 50.0f);
                    pGuiGraphics.fill(x + 2, y + 40, x + 2 + width, y + 40 + 2, color);
                }
            }

            @Override
            public void renderForeground(GuiGraphics pGuiGraphics, int x, int y, float partialTick, ItemStack stack, int index) {
                ISpellComponent part;
                if (index >= 0 && index < this.spellParts.size() && (part = this.spellParts.get(index)) != null) {
                    pGuiGraphics.blit(part.getGuiIcon(), x + 19, y + 3, 0.0f, 0.0f, 16, 16, 16, 16);
                    if (part.isSilverSpell()) {
                        GuiRenderUtils.renderSilverSpellBorder(pGuiGraphics, x + 19, y + 3, 16, 16);
                    }
                }
                this.renderTextLines(pGuiGraphics, stack, x, y);
            }

            @Override
            protected IFaction getFaction(int index) {
                return index >= this.spellParts.size() || this.spellParts.get(index) == null ? null : this.spellParts.get(index).getFactionRequirement();
            }
        }

        public class OcculusManaweaveEntry
        extends OcculusEntry {
            private ArrayList<ManaweavingPattern> patterns;

            public OcculusManaweaveEntry(int tier) {
                super(tier);
                this.patterns = new ArrayList();
            }

            @Override
            public void addOutputItem(AMRecipeBase recipe, IconTypes type) {
                super.addOutputItem(recipe, type);
                if (recipe instanceof ManaweavingPattern) {
                    this.patterns.add((ManaweavingPattern)recipe);
                }
            }

            @Override
            public void renderForeground(GuiGraphics pGuiGraphics, int x, int y, float partialTick, ItemStack stack, int index) {
                float scale = 0.1f;
                GuiRenderUtils.renderManaweavePattern(pGuiGraphics, (int)((float)(x + 33) / scale), (int)((float)(y + 4) / scale), scale, this.patterns.get(index));
                ArrayList<Component> tooltip = new ArrayList<Component>();
                tooltip.add(stack.getHoverName());
                if (GuiOcculus.this.progression == null) {
                    return;
                }
                scale = 0.7f;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().scale(scale, scale, 1.0f);
                int yCoord = y + 22;
                for (Component tc : tooltip) {
                    List lines = this.fontRenderer.getSplitter().splitLines((FormattedText)tc, (int)(47.0f / scale), Style.EMPTY);
                    for (FormattedText line : (java.util.List<FormattedText>)lines) {
                        int len = (int)((float)this.fontRenderer.width(line.getString()) * scale);
                        int xCoord = x + 2 + 25 - len / 2;
                        pGuiGraphics.drawString(this.fontRenderer, line.getString(), (int)((float)xCoord / scale), (int)((float)yCoord / scale), 0xFFFFFF, false);
                        float f = yCoord;
                        Objects.requireNonNull(this.fontRenderer);
                        yCoord = (int)(f + 9.0f * scale);
                    }
                }
                pGuiGraphics.pose().popPose();
            }

            @Override
            public Component getNarration() {
                return Component.translatable((String)"gui.mna.occulus.select");
            }
        }
    }
}

