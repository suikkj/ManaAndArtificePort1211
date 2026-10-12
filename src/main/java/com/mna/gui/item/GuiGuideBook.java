 
package com.mna.gui.item;

import com.mna.tools.EnchantmentUtils;
import com.google.common.collect.Lists;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.CodexBreadcrumb;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.ClientConfigValues;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.sound.SFX;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.config.ClientConfig;
import com.mna.gui.GuiTextures;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.item.ContainerGuideBook;
import com.mna.gui.widgets.ImageItemStackButton;
import com.mna.guide.EntryCategory;
import com.mna.guide.GuideBookEntries;
import com.mna.guide.GuidebookEntry;
import com.mna.guide.RelatedRecipe;
import com.mna.guide.interfaces.IEntrySection;
import com.mna.guide.recipe.RecipeBlank;
import com.mna.recipes.AMRecipeBase;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.ImageButton;

@OnlyIn(value=Dist.CLIENT)
public class GuiGuideBook
extends GuiJEIDisable<ContainerGuideBook> {
    private static final int X_PAGE_LEFT = 15;
    private static final int X_PAGE_RIGHT = 133;
    public static final int X_MAX = 108;
    public static final int Y_PAGE_START = 10;
    public static final int Y_MAX = 168;
    GuidebookEntry currentEntry = null;
    ImageButton prevPage = null;
    ImageButton nextPage = null;
    ImageButton back = null;
    ImageButton switchConfig_HUD = null;
    ImageButton switchConfig_HUDPOS = null;
    ImageButton switchConfig_BACK = null;
    ImageButton switchConfig_PINSIZE = null;
    ImageButton search = null;
    Component searchTerm;
    EditBox searchBox;
    RecipeBlank searchBackground;
    RecipeRendererBase currentRecipe = null;
    ImageButton closeRecipe = null;
    ImageButton pinRecipe = null;
    int page = 0;
    int max_pages = 1;
    boolean isSearching = false;
    IPlayerProgression progression = null;
    static List<Component> currentTooltip = new ArrayList<Component>();
    private ArrayList<ImageButton> relatedTabs;
    private ArrayList<CategoryItemButton> categoryButtons;
    private ArrayList<CodexSearchResult> searchResults;
    private HashMap<Integer, Collection<AbstractWidget>> pageWidgets;
    private EntryCategory currentCategory = EntryCategory.BASICS;

    public GuiGuideBook(ContainerGuideBook container, Inventory playerInventory, Component textComponent) {
        super(container, playerInventory, textComponent);
        this.imageWidth = 256;
        this.imageHeight = 178;
        this.relatedTabs = new ArrayList();
        this.categoryButtons = new ArrayList();
        this.searchResults = new ArrayList();
        this.pageWidgets = new HashMap();
        if (this.minecraft == null) {
            this.minecraft = Minecraft.getInstance();
        }
        this.progression = (IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get());
    }

    protected void init() {
        super.init();
        GuideBookEntries.INSTANCE.reload();
        GuideBookEntries.INSTANCE.updateUnlockedEntries(null);
        this.categoryButtons.clear();
        int i = this.leftPos;
        int j = this.topPos;
        this.prevPage = new PaperImageButton(i + 10, j + 158, 14, 14, 0, 0, 14, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.decrementPage());
        this.nextPage = new PaperImageButton(i + 233, j + 158, 14, 14, 14, 0, 14, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.incrementPage());
        this.prevPage.active = false;
        this.prevPage.visible = false;
        this.addRenderableWidget(this.prevPage);
        this.addRenderableWidget(this.nextPage);
        this.search = new PaperImageButton(i + 15, j + this.imageHeight - 6, 18, 28, 118, 0, 28, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.toggleSearch()).setTooltip((Component)Component.translatable((String)"gui.mna.codex_search"));
        this.searchTerm = Component.translatable((String)"gui.mna.codex_search");
        this.searchBox = new EditBox(this.minecraft.font, i + this.imageWidth / 2 - 80, j - 33, 160, 20, this.searchTerm);
        this.searchBox.setMaxLength(60);
        this.searchBox.setResponder(this::runSearch);
        this.searchBackground = new RecipeBlank(this.leftPos + (this.imageWidth - 218) / 2, this.topPos + (this.imageHeight - 256) / 2);
        this.addRenderableWidget(this.search);
        this.back = new PaperImageButton(i - 20, j + this.imageHeight / 2 - 14, 20, 38, 84, 0, 38, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.displayBreadcrumb(false));
        this.addRenderableWidget(this.back);
        this.switchConfig_HUD = new PaperImageButton(i + 35, j + this.imageHeight - 6, 18, 28, 136, 0, 28, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> {
            int cur = ClientConfigValues.CodexBackMode.ordinal();
            ClientConfig.setCodexBackStyle((cur + 1) % ClientConfigValues.CodexMode.values().length);
            ClientConfig.bakeConfig();
            ((PaperImageButton)button).setTooltip((Component)Component.translatable((String)ClientConfigValues.CodexBackMode.getLocalizationKey()));
        }).setTooltip((Component)Component.translatable((String)ClientConfigValues.CodexBackMode.getLocalizationKey()));
        this.switchConfig_HUDPOS = new PaperImageButton(i + 55, j + this.imageHeight - 6, 18, 28, 172, 0, 28, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> {
            int cur = ClientConfigValues.HudPosition.ordinal();
            ClientConfig.setHudPosition((cur + 1) % ClientConfigValues.HudPos.values().length);
            ClientConfig.bakeConfig();
            ((PaperImageButton)button).setTooltip((Component)Component.translatable((String)ClientConfigValues.HudPosition.getLocalizationKey()));
        }).setTooltip((Component)Component.translatable((String)ClientConfigValues.HudPosition.getLocalizationKey()));
        this.switchConfig_PINSIZE = new PaperImageButton(i + 75, j + this.imageHeight - 6, 18, 28, 190, 0, 28, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> {
            int cur = ClientConfigValues.PinnedRecipeScale.ordinal();
            ClientConfig.setPinnedRecipeScale((cur + 1) % ClientConfigValues.HudMode.values().length);
            ClientConfig.bakeConfig();
            ((PaperImageButton)button).setTooltip((Component)Component.translatable((String)ClientConfigValues.PinnedRecipeScale.getLocalizationKey()));
        }).setTooltip((Component)Component.translatable((String)ClientConfigValues.PinnedRecipeScale.getLocalizationKey()));
        this.switchConfig_BACK = new PaperImageButton(i + 95, j + this.imageHeight - 6, 18, 28, 154, 0, 28, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> {
            int cur = ClientConfigValues.ShowHudMode.ordinal();
            ClientConfig.setHudMode((cur + 1) % ClientConfigValues.HudMode.values().length);
            ClientConfig.bakeConfig();
            ((PaperImageButton)button).setTooltip((Component)Component.translatable((String)ClientConfigValues.ShowHudMode.getLocalizationKey()));
        }).setTooltip((Component)Component.translatable((String)ClientConfigValues.ShowHudMode.getLocalizationKey()));
        this.addRenderableWidget(this.switchConfig_HUD);
        this.addRenderableWidget(this.switchConfig_HUDPOS);
        this.addRenderableWidget(this.switchConfig_PINSIZE);
        this.addRenderableWidget(this.switchConfig_BACK);
        this.closeRecipe = new PaperImageButton(i + 210, j - 30, 14, 14, 104, 0, 14, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.displayBreadcrumb(false));
        this.pinRecipe = new MNAImageButton(i + 30, j - 30, 14, 14, 104, 43, 14, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> {
            if (HUDOverlayRenderer.instance.getPinnedrecipe() != null && HUDOverlayRenderer.instance.getPinnedrecipe().equals(this.currentRecipe)) {
                HUDOverlayRenderer.instance.setPinnedRecipe(null);
            } else {
                HUDOverlayRenderer.instance.setPinnedRecipe(this.currentRecipe.clone(0, 0, true));
            }
        });
        this.displayBreadcrumb(false);
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_OPEN);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((ClientConfigValues.CodexBackMode == ClientConfigValues.CodexMode.EscUIBack || ClientConfigValues.CodexBackMode == ClientConfigValues.CodexMode.RMouseUIEscBack) && keyCode == 256) {
            if (this.isSearching) {
                this.clearSearch(false);
            } else if (this.currentRecipe != null) {
                this.clearDisplayedRecipe(false);
            } else if (this.progression.peekCodexBreadcrumb() == null) {
                this.onClose();
                return true;
            }
            this.displayBreadcrumb(false);
            return true;
        }
        if (this.isSearching && keyCode != 256 && this.getFocused() != null) {
            this.getFocused().keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((ClientConfigValues.CodexBackMode == ClientConfigValues.CodexMode.RMouseUIBackEscOut || ClientConfigValues.CodexBackMode == ClientConfigValues.CodexMode.RMouseUIEscBack) && button == 1) {
            if (this.isSearching) {
                if (this.searchBox.isMouseOver(mouseX, mouseY)) {
                    this.searchBox.setValue("");
                    for (CodexSearchResult r : this.searchResults) {
                        this.removeWidget((GuiEventListener)r);
                    }
                    this.searchResults.clear();
                    return true;
                }
                this.clearSearch(false);
            } else if (this.currentRecipe != null) {
                this.clearDisplayedRecipe(false);
            }
            this.displayBreadcrumb(false);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double p_scrollX) {
        if (this.currentRecipe == null && !this.isSearching) {
            if (delta > 0.0) {
                this.decrementPage();
            } else {
                this.incrementPage();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta, 0.0);
    }

    private void toggleSearch() {
        if (this.isSearching) {
            this.clearSearch(true);
        } else {
            this.showSearch(true);
        }
    }

    private void showSearch(boolean pushBreadcrumb) {
        if (pushBreadcrumb) {
            this.pushCategoryBreadcrumb();
            this.clearDisplayedRecipe(true);
        }
        for (Renderable widget : this.renderables) {
            if (!(widget instanceof AbstractWidget)) continue;
            ((AbstractWidget)widget).active = false;
        }
        for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page, new ArrayList())) {
            if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
            ((AbstractWidget)widget).active = false;
            ((AbstractWidget)widget).visible = false;
        }
        for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page + 1, new ArrayList())) {
            if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
            ((AbstractWidget)widget).active = false;
            ((AbstractWidget)widget).visible = false;
        }
        this.addRenderableWidget(this.searchBackground);
        this.addRenderableWidget(this.searchBox);
        this.addRenderableWidget(this.closeRecipe);
        DelayedEventQueue.pushEvent((Level)this.minecraft.level, new TimedDelayedEvent<String>("focus", 10, "focus", this::fixFocus));
        this.isSearching = true;
    }

    private void clearSearch(boolean pushBreadcrumb) {
        if (pushBreadcrumb) {
            this.pushBreadcrumb(CodexBreadcrumb.Type.SEARCH, this.searchBox.getValue(), new String[0]);
        }
        this.removeWidget((GuiEventListener)this.searchBox);
        this.removeWidget((GuiEventListener)this.searchBackground);
        this.removeWidget((GuiEventListener)this.closeRecipe);
        for (CodexSearchResult r : this.searchResults) {
            this.removeWidget((GuiEventListener)r);
        }
        this.searchResults.clear();
        this.searchBox.setValue("");
        for (Renderable widget : this.renderables) {
            if (!(widget instanceof AbstractWidget)) continue;
            ((AbstractWidget)widget).active = true;
        }
        for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page, new ArrayList())) {
            if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
            ((AbstractWidget)widget).active = true;
            ((AbstractWidget)widget).visible = true;
        }
        for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page + 1, new ArrayList())) {
            if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
            ((AbstractWidget)widget).active = true;
            ((AbstractWidget)widget).visible = true;
        }
        this.isSearching = false;
    }

    private void runSearch(String term) {
        if (term.length() < 3) {
            return;
        }
        for (CodexSearchResult r : this.searchResults) {
            this.removeWidget((GuiEventListener)r);
        }
        this.searchResults.clear();
        MutableInt yCoord = new MutableInt(this.topPos - 10);
        List<GuideBookEntries.GuidebookSearchResult> results = GuideBookEntries.INSTANCE.searchEntries(term, this.progression.getTier(), 24);
        CodexSearchResult csr = null;
        for (GuideBookEntries.GuidebookSearchResult e : results) {
            if (e.isEntry()) {
                csr = new CodexSearchResult(this.leftPos + 35, yCoord.getValue(), e.getEntry().getCategory().getDisplayStack().getHoverName().getString() + " > " + e.getEntry().getFirstTitle(), button -> {
                    this.clearSearch(true);
                    this.setCurrentEntry(e.getEntry().getName(), false);
                });
                this.addRenderableWidget(csr);
                this.searchResults.add(csr);
                yCoord.add(csr.getHeight() + 2);
            } else if (e.isRecipe()) {
                MutableComponent ttc = Component.translatable((String)("gui.mna.codex_search.recipe." + e.getRecipe().getType()));
                if (e.getOutputStack().isEnchanted()) {
                    java.util.Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> enchs = EnchantmentUtils.enchantmentMap((ItemStack)e.getOutputStack());
                    for (Map.Entry enchantment : enchs.entrySet()) {
                        ttc.append(((Enchantment)enchantment.getKey()).getFullname(((Integer)enchantment.getValue()).intValue()));
                    }
                    csr = new CodexSearchResult(this.leftPos + 35, yCoord.getValue(), ttc.getString(), button -> {
                        this.clearSearch(true);
                        this.displayRecipe(e.getRecipe(), false);
                    });
                } else {
                    csr = new CodexSearchResult(this.leftPos + 35, yCoord.getValue(), ttc.append(e.getOutputStack().getHoverName()).getString(), button -> {
                        this.clearSearch(true);
                        this.displayRecipe(e.getRecipe(), false);
                    });
                }
                this.addRenderableWidget(csr);
                this.searchResults.add(csr);
                yCoord.add(csr.getHeight() + 2);
            }
            int n = yCoord.intValue();
            Objects.requireNonNull(this.font);
            if (n <= this.topPos + 200 - 9) continue;
            break;
        }
    }

    private ItemStack lookupResource(ResourceLocation rLoc) {
        Item item = (Item)BuiltInRegistries.ITEM.get(rLoc);
        if (item != null && item != Items.AIR) {
            return new ItemStack((ItemLike)item);
        }
        Block block = (Block)BuiltInRegistries.BLOCK.get(rLoc);
        if (block != null && block != Blocks.AIR) {
            return new ItemStack((ItemLike)block);
        }
        return ItemStack.EMPTY;
    }

    private ISpellComponent lookupSpell(ResourceLocation rLoc) {
        ISpellComponent output = null;
        output = (ISpellComponent)Registries.Shape.getValue(rLoc);
        if (output != null) {
            return output;
        }
        output = (ISpellComponent)Registries.SpellEffect.getValue(rLoc);
        if (output != null) {
            return output;
        }
        output = (ISpellComponent)Registries.Modifier.getValue(rLoc);
        return output;
    }

    private void fixFocus(String identifier, String data) {
        this.searchBox.setFocused(true);
        this.setFocused((GuiEventListener)this.searchBox);
    }

    private void displayRecipe(RelatedRecipe rr, boolean pushBreadcrumb) {
        if (pushBreadcrumb) {
            this.pushBreadcrumb(CodexBreadcrumb.Type.ENTRY, this.currentEntry.getName(), new String[0]);
        }
        this.clearDisplayedRecipe(true);
        this.currentRecipe = rr.constructRenderer(this.leftPos + (this.imageWidth - 218) / 2, this.topPos + (this.imageHeight - 256) / 2, this::setTooltip);
        if (this.currentRecipe != null) {
            for (Renderable widget : this.renderables) {
                if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
                ((AbstractWidget)widget).active = false;
            }
            for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page, new ArrayList())) {
                if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
                ((AbstractWidget)widget).active = false;
                ((AbstractWidget)widget).visible = false;
            }
            for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page + 1, new ArrayList())) {
                if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
                ((AbstractWidget)widget).active = false;
                ((AbstractWidget)widget).visible = false;
            }
            this.addRenderableWidget(this.currentRecipe);
            this.addRenderableWidget(this.closeRecipe);
            this.addRenderableWidget(this.pinRecipe);
        }
    }

    private void clearDisplayedRecipe(boolean pushBreadcrumb) {
        if (this.currentRecipe == null) {
            return;
        }
        if (pushBreadcrumb) {
            CodexBreadcrumb lastBreadcrumb = this.progression.peekCodexBreadcrumb();
            if (lastBreadcrumb.breadcrumbType == CodexBreadcrumb.Type.RECIPE) {
                this.progression.popCodexBreadcrumb();
            }
            this.pushBreadcrumb(CodexBreadcrumb.Type.RECIPE, this.currentRecipe.getRegistryName().toString(), Arrays.asList(this.currentRecipe.getRecipeIds()).stream().map(rLoc -> rLoc.toString()).collect(Collectors.toList()).toArray(new String[0]));
        }
        this.removeWidget((GuiEventListener)this.currentRecipe);
        this.removeWidget((GuiEventListener)this.closeRecipe);
        this.removeWidget((GuiEventListener)this.pinRecipe);
        for (Renderable widget : this.renderables) {
            if (!(widget instanceof AbstractWidget)) continue;
            ((AbstractWidget)widget).active = true;
        }
        for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page, new ArrayList())) {
            if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
            ((AbstractWidget)widget).active = true;
            ((AbstractWidget)widget).visible = true;
        }
        for (Renderable widget : (Collection<Renderable>)this.pageWidgets.getOrDefault(this.page + 1, new ArrayList())) {
            if (!(widget instanceof AbstractWidget) || this.relatedTabs.contains(widget)) continue;
            ((AbstractWidget)widget).active = true;
            ((AbstractWidget)widget).visible = true;
        }
        this.currentRecipe = null;
    }

    private void lookupAndShowRecipe(String recipeId, boolean pushBreadcrumb) {
        ResourceLocation search = ResourceLocation.parse(recipeId);
        for (Map.Entry<String, GuidebookEntry> e : GuideBookEntries.INSTANCE.getAllEntries()) {
            for (RelatedRecipe rr : e.getValue().getRelatedRecipes()) {
                for (ResourceLocation s : rr.getResourceLocations()) {
                    if (!s.equals(search)) continue;
                    this.displayRecipe(rr, pushBreadcrumb);
                    return;
                }
            }
        }
    }

    private void decrementPage() {
        if (this.hasOverlay()) {
            return;
        }
        this.page -= 2;
        if (this.max_pages > 1) {
            this.nextPage.visible = true;
            this.nextPage.active = true;
        }
        if (this.page <= 0) {
            this.page = 0;
            this.prevPage.visible = false;
            this.prevPage.active = false;
        }
    }

    private void incrementPage() {
        if (this.hasOverlay()) {
            return;
        }
        this.page += 2;
        if (this.max_pages > 1) {
            this.prevPage.visible = true;
            this.prevPage.active = true;
        }
        if (this.page >= this.max_pages - 1) {
            this.page = this.max_pages - 1;
            if (this.page % 2 == 1) {
                ++this.page;
            }
            this.nextPage.visible = false;
            this.nextPage.active = false;
        }
    }

    private void pushCategoryBreadcrumb() {
        if (this.currentCategory != null && this.currentRecipe == null && !this.isSearching) {
            String key = this.currentCategory.getId();
            this.pushBreadcrumb(CodexBreadcrumb.Type.CATEGORY, key, new String[0]);
        }
    }

    private void pushBreadcrumb(CodexBreadcrumb.breadcrumbType type, String key, String ... metadata) {
        this.progression.pushCodexBreadcrumb(type, key, this.page, metadata);
    }

    private void displayBreadcrumb(boolean peek) {
        CodexBreadcrumb breadcrumb;
        CodexBreadcrumb codexBreadcrumb = breadcrumb = peek ? this.progression.peekCodexBreadcrumb() : this.progression.popCodexBreadcrumb();
        if (breadcrumb == null) {
            this.setupIndexView(false, 0);
            return;
        }
        this.clearDisplayedRecipe(false);
        this.clearSearch(false);
        switch (breadcrumb.breadcrumbType) {
            case ENTRY: {
                this.setCurrentEntry(breadcrumb.Key, false);
                break;
            }
            case RECIPE: {
                this.lookupAndShowRecipe(breadcrumb.Key, false);
                break;
            }
            case SEARCH: {
                this.showSearch(false);
                this.searchBox.setValue(breadcrumb.Key);
                break;
            }
            case CATEGORY: {
                try {
                    this.setupCategoryView(EntryCategory.valueOf(breadcrumb.Key), breadcrumb.Page, false);
                    this.setupIndexView(false, breadcrumb.Page);
                }
                catch (Throwable t) {
                    this.setupIndexView(false, 0);
                }
                break;
            }
            case INDEX: {
                this.setupIndexView(false, breadcrumb.Page);
                break;
            }
            default: {
                return;
            }
        }
    }

    private void setupIndexView(boolean pushBreadcrumb, int page) {
        if (pushBreadcrumb) {
            this.pushCategoryBreadcrumb();
        }
        this.currentEntry = null;
        this.clearRelatedTabs();
        this.setupCategorySelectionButtons();
        this.setupCategoryItemButtons();
        this.setupCategoryView(this.currentCategory, page, false);
        this.pageWidgets.clear();
    }

    private void setupCategoryView(EntryCategory value, boolean pushBreadcrumb) {
        this.setupCategoryView(value, 0, pushBreadcrumb);
    }

    private void setupCategoryView(EntryCategory value, int page, boolean pushBreadcrumb) {
        if (pushBreadcrumb) {
            this.pushCategoryBreadcrumb();
        }
        int playerTier = this.progression.getTier();
        this.max_pages = 1;
        this.categoryButtons.forEach(c -> {
            c.visible = c.active = c.category == value && c.tier <= playerTier;
            if (c.active) {
                this.max_pages = Math.max(this.max_pages, c.page);
            }
        });
        this.prevPage.active = page > 0;
        this.prevPage.visible = this.prevPage.active;
        this.page = Mth.clamp((int)page, (int)0, (int)this.max_pages);
        if (this.max_pages > 1) {
            this.nextPage.visible = true;
            this.nextPage.active = true;
        } else {
            this.nextPage.visible = false;
            this.nextPage.active = false;
        }
        this.back.visible = false;
        this.back.active = false;
        this.currentCategory = value;
    }

    private void setCurrentEntry(String id, boolean pushBreadcrumb) {
        if (pushBreadcrumb) {
            this.pushCategoryBreadcrumb();
        }
        this.hideCategoryView();
        this.clearRelatedTabs();
        this.currentEntry = GuideBookEntries.INSTANCE.getEntry(id);
        this.currentEntry.setRead();
        this.setupEntryRelatedButtons();
        this.setupEntryPages();
        this.page = 0;
    }

    private void hideCategoryView() {
        this.categoryButtons.forEach(c -> {
            c.visible = false;
            c.active = false;
        });
        this.back.visible = true;
        this.back.active = true;
    }

    private void setupCategoryItemButtons() {
        for (CategoryItemButton catBtn : this.categoryButtons) {
            this.removeWidget((GuiEventListener)catBtn);
        }
        this.categoryButtons.clear();
        for (EntryCategory value : EntryCategory.values()) {
            int x = this.leftPos + 15;
            int y = this.topPos + 10 + 20;
            int y_max = this.topPos + 168;
            int render_page = 0;
            int entryCount = 1;
            String entryPrefix = "* ";
            List entries = GuideBookEntries.INSTANCE.getEntries(value).stream().collect(Collectors.toList());
            Collections.sort(entries, new Comparator<GuidebookEntry>(){

                @Override
                public int compare(GuidebookEntry o1, GuidebookEntry o2) {
                    Integer index2;
                    Integer index1 = o1.getIndex();
                    if (index1 == (index2 = Integer.valueOf(o2.getIndex()))) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return index1.compareTo(index2);
                }
            });
            for (GuidebookEntry e : entries) {
                if (e.getTier() > this.progression.getTier()) continue;
                Object disp = I18n.get((String)e.getFirstTitle(), (Object[])new Object[0]);
                if (!e.isRead()) {
                    disp = entryPrefix + (String)disp;
                }
                int countWidth = this.font.width(" " + entryCount);
                List split_lines = this.font.getSplitter().splitLines((String)disp, 108 - countWidth, Style.EMPTY);
                disp = ((FormattedText)split_lines.get(split_lines.size() - 1)).getString();
                char spacer = '.';
                int width = this.font.width((String)disp);
                int padding = 15 - countWidth;
                int spacerWidth = this.font.width("" + spacer);
                int nonPaddedWidth = 108 - countWidth - padding;
                int requiredPaddingWidth = nonPaddedWidth - width;
                int numSpacers = 0;
                if (nonPaddedWidth > 0 && requiredPaddingWidth > spacerWidth) {
                    numSpacers = requiredPaddingWidth / spacerWidth;
                }
                if (numSpacers > 0) {
                    char[] spacers = new char[numSpacers];
                    Arrays.fill(spacers, spacer);
                    disp = (String)disp + String.copyValueOf(spacers);
                }
                split_lines.set(split_lines.size() - 1, Component.literal((String)disp));
                ArrayList<Component> split_lines_cast = new ArrayList<Component>();
                for (FormattedText split : split_lines) {
                    String s = split.getString();
                    if (s.startsWith(entryPrefix)) {
                        split_lines_cast.add((Component)Component.literal((String)entryPrefix).withStyle(ChatFormatting.AQUA).append((Component)Component.literal((String)s.substring(2)).withStyle(ChatFormatting.BLACK)));
                        continue;
                    }
                    split_lines_cast.add((Component)Component.literal((String)s));
                }
                CategoryItemButton btn = new CategoryItemButton(value, render_page, entryCount, x, y, e, split_lines_cast, button -> {
                    this.hideCategoryView();
                    this.setCurrentEntry(e.getName(), true);
                });
                if (y + btn.getHeight() > y_max) {
                    y = this.topPos + 10 + 20;
                    x = ++render_page % 2 == 1 ? this.leftPos + 133 : this.leftPos + 15;
                    btn.setX(x);
                    btn.setY(y);
                    btn.page = render_page;
                }
                this.addRenderableWidget(btn);
                this.categoryButtons.add(btn);
                y += btn.getHeight();
                ++entryCount;
            }
        }
    }

    private void clearRelatedTabs() {
        for (ImageButton btn : this.relatedTabs) {
            this.removeWidget((GuiEventListener)btn);
        }
    }

    private void setupEntryPages() {
        this.max_pages = 1;
        this.pageWidgets.entrySet().forEach(e -> ((Collection)e.getValue()).forEach(widget -> this.removeWidget((GuiEventListener)widget)));
        this.pageWidgets.clear();
        if (this.currentEntry != null) {
            int x = 15;
            int y = 10;
            int page = -1;
            for (IEntrySection section : this.currentEntry.getSections().stream().sorted((a, b) -> b.getPage() - a.getPage()).collect(Collectors.toList())) {
                if (section.getPage() > this.max_pages) {
                    this.max_pages = section.getPage();
                }
                if (page != section.getPage()) {
                    page = section.getPage();
                    y = this.topPos + 10;
                    x = this.leftPos + (page % 2 == 0 ? 15 : 133);
                }
                ArrayList<AbstractWidget> curPageWidgets = this.pageWidgets.getOrDefault(section.getPage(), new ArrayList());
                curPageWidgets.addAll(section.getWidgets(this, x, y, 108, 168, this::setTooltip, this::lookupAndShowRecipe, this::setCurrentEntry));
                this.pageWidgets.put(section.getPage(), curPageWidgets);
                y += section.getHeight(168);
            }
        }
        this.prevPage.visible = false;
        this.prevPage.active = false;
        if (this.max_pages > 1) {
            this.nextPage.visible = true;
            this.nextPage.active = true;
        } else {
            this.nextPage.visible = false;
            this.nextPage.active = false;
        }
    }

    private void setupCategorySelectionButtons() {
        int step = 19;
        int x = this.leftPos + this.imageWidth - 7;
        int y = this.topPos + 4;
        int x_custom = this.leftPos + this.imageWidth - step - 7;
        int y_custom = this.topPos + this.imageHeight - 4;
        for (EntryCategory value : EntryCategory.values()) {
            ItemStack stack;
            boolean entryShown = false;
            List entries = GuideBookEntries.INSTANCE.getEntries(value).stream().collect(Collectors.toList());
            for (GuidebookEntry e : entries) {
                if (e.getTier() > this.progression.getTier()) continue;
                entryShown = true;
                break;
            }
            if (!entryShown || (stack = value.getDisplayStack()).isEmpty()) continue;
            int width = value.isCustom() ? 18 : 28;
            int height = value.isCustom() ? 28 : 18;
            int u = value.isCustom() ? 208 : 28;
            int v = value.isCustom() ? 0 : 54;
            int hoverOffset = height;
            ImageItemStackButton btn = new ImageItemStackButton(value.isCustom() ? x_custom : x, value.isCustom() ? y_custom : y, width, height, u, v, hoverOffset, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.setupCategoryView(value, true), this::setTooltip, stack, false);
            this.addRenderableWidget(btn);
            this.relatedTabs.add(btn);
            if (value.isCustom()) {
                x_custom -= step;
                continue;
            }
            y += step;
        }
    }

    private void setupEntryRelatedButtons() {
        if (this.currentEntry == null) {
            return;
        }
        int i = this.leftPos;
        int j = this.topPos;
        int x = i + this.imageWidth - 7;
        int y = j + 5;
        int y_step = 19;
        if (this.currentEntry.getRelatedRecipes().size() > 0) {
            y_step = Math.min(20, 170 / this.currentEntry.getRelatedRecipes().size());
        }
        int playerTier = this.progression.getTier();
        for (RelatedRecipe rr : this.currentEntry.getRelatedRecipes()) {
            if (rr.getTier() > playerTier) continue;
            if (rr.getType().equals("spell_part")) {
                ISpellComponent part = this.lookupSpell(rr.getResourceLocations()[0]);
                if (part == null) continue;
                ImageButton btn = new ImageSpellPartButton(x, y, 28, 18, 56, 0, 18, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.displayRecipe(rr, true), part.getGuiIcon(), part.getRegistryName());
                this.addRenderableWidget(btn);
                this.relatedTabs.add(btn);
            } else {
                Optional pattern;
                ItemStack stack = this.lookupResource(rr.getResourceLocations()[0]);
                if (stack.isEmpty() && (pattern = this.minecraft.level.getRecipeManager().byKey(rr.getResourceLocations()[0])).isPresent()) {
                    stack = pattern.get() instanceof AMRecipeBase ? ((AMRecipeBase)pattern.get()).getGuiRepresentationStack() : ((Recipe)pattern.get()).getResultItem(this.minecraft.level.registryAccess());
                }
                if (!stack.isEmpty()) {
                    ImageButton btn = new ImageItemStackButton(x, y, 28, 18, 56, 0, 18, GuiTextures.Items.GUIDE_BOOK_WIDGETS, 256, 256, button -> this.displayRecipe(rr, true), this::setTooltip, stack);
                    this.addRenderableWidget(btn);
                    this.relatedTabs.add(btn);
                }
            }
            y += y_step;
        }
    }

    private boolean hasOverlay() {
        return this.isSearching || this.currentRecipe != null;
    }

    public void onClose() {
        if (this.isSearching) {
            this.pushBreadcrumb(CodexBreadcrumb.Type.SEARCH, this.searchBox.getValue(), new String[0]);
        } else if (this.currentRecipe != null) {
            this.pushBreadcrumb(CodexBreadcrumb.Type.RECIPE, this.currentRecipe.getRegistryName().toString(), new String[0]);
        } else if (this.currentEntry != null) {
            this.pushBreadcrumb(CodexBreadcrumb.Type.ENTRY, this.currentEntry.getName(), new String[0]);
        } else if (this.currentCategory != null) {
            this.pushBreadcrumb(CodexBreadcrumb.Type.CATEGORY, this.currentCategory.name(), new String[0]);
        }
        ManaAndArtifice.instance.proxy.playSingleSound(SFX.Gui.BOOK_CLOSE);
        super.onClose();
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick) {
        currentTooltip.clear();
        super.render(pGuiGraphics, mouseX, mouseY, partialTick);
        if (this.isSearching) {
            this.fixFocus("focus", "focus");
        }
        ((Collection<Renderable>)this.pageWidgets.getOrDefault(this.page, new ArrayList())).forEach(w -> w.render(pGuiGraphics, mouseX, mouseY, partialTick));
        ((Collection<Renderable>)this.pageWidgets.getOrDefault(this.page + 1, new ArrayList())).forEach(w -> w.render(pGuiGraphics, mouseX, mouseY, partialTick));
        if (!currentTooltip.isEmpty()) {
            pGuiGraphics.renderTooltip(this.font, Lists.transform(currentTooltip, Component::getVisualOrderText), mouseX, mouseY);
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(GuiTextures.Items.GUIDE_BOOK, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        if (this.currentRecipe != null || this.isSearching) {
            return;
        }
        if (this.currentEntry == null) {
            this.renderIndex(pGuiGraphics);
        }
    }

    private void renderIndex(GuiGraphics pGuiGraphics) {
        String str = this.currentCategory.getDisplayStack().getHoverName().getString();
        int padding = (108 - this.minecraft.font.width(str)) / 2;
        pGuiGraphics.drawString(this.font, str, 15 + padding, 15, 0x404040, false);
    }

    private void setTooltip(List<Component> tooltip) {
        currentTooltip.clear();
        currentTooltip.addAll(tooltip);
    }

    public class PaperImageButton
    extends MNAImageButton {
        private Component tooltip;

        public PaperImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, Button.OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
        }

        public PaperImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int p_i51136_9_, int p_i51136_10_, Button.OnPress onPressIn, Component textIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, p_i51136_9_, p_i51136_10_, onPressIn, textIn);
        }

        public PaperImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int p_i51135_9_, int p_i51135_10_, Button.OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, p_i51135_9_, p_i51135_10_, onPressIn);
        }

        public PaperImageButton setTooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
            if (this.active && this.visible && this.isHoveredOrFocused() && this.tooltip != null) {
                currentTooltip.add(this.tooltip);
            }
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }
    }

    public class CodexSearchResult
    extends Button {
        private List<FormattedText> split_lines;

        public CodexSearchResult(int x, int y, String text, Button.OnPress pressedAction) {
            Objects.requireNonNull(GuiGuideBook.this.font);
            super(x, y, 205, 9 * GuiGuideBook.this.font.getSplitter().splitLines((FormattedText)Component.literal((String)text), 200, Style.EMPTY).size(), (Component)Component.literal((String)text), pressedAction, DEFAULT_NARRATION);
            this.split_lines = GuiGuideBook.this.font.getSplitter().splitLines((FormattedText)Component.literal((String)text), 200, Style.EMPTY);
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            Font fontrenderer = minecraft.font;
            int renderY = this.getY();
            for (FormattedText props : this.split_lines) {
                pGuiGraphics.drawString(fontrenderer, props.getString(), this.getX(), renderY, this.getFGColor(), false);
                Objects.requireNonNull(minecraft.font);
                renderY += 9;
            }
        }

        public int getFGColor() {
            return this.isHoveredOrFocused() ? 0xB300B3 : 0x444444;
        }
    }

    public class CategoryItemButton
    extends Button {
        public int page;
        public final EntryCategory category;
        private final String count;
        private final int tier;
        private final int overrideColor;
        private final List<Component> tooltipLines;
        private final List<Component> textLines;

        public CategoryItemButton(EntryCategory category, int page, int count, int x, int y, GuidebookEntry entry, List<Component> text, Button.OnPress onPress) {
            Objects.requireNonNull(((GuiGuideBook)GuiGuideBook.this).minecraft.font);
            super(x, y, 108, 9 * text.size() + 5, text.get(0), onPress, DEFAULT_NARRATION);
            this.page = page;
            this.category = category;
            this.count = "" + count;
            this.textLines = text;
            this.tier = entry.getTier();
            this.overrideColor = entry.getOverrideColor();
            this.tooltipLines = entry.getTooltip();
        }

        public void setPage(int page) {
            this.page = page;
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }

        protected boolean clicked(double mouseX, double mouseY) {
            return (this.page == GuiGuideBook.this.page || this.page - 1 == GuiGuideBook.this.page) && this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
            if (GuiGuideBook.this.page != this.page && GuiGuideBook.this.page != this.page - 1) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            Font fontrenderer = minecraft.font;
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.alpha);
            int j = this.getFGColor();
            int renderY = this.getY();
            for (FormattedText formattedText : this.textLines) {
                int color = j | Mth.ceil((float)(this.alpha * 255.0f)) << 24;
                int xOffset = 0;
                if (formattedText instanceof MutableComponent) {
                    MutableComponent mutableComponent = (MutableComponent)formattedText;
                    for (Component c : mutableComponent.toFlatList()) {
                        if (c.getStyle().getColor() != null) {
                            color = c.getStyle().getColor().getValue();
                        }
                        if (color == 0) {
                            pGuiGraphics.drawString(fontrenderer, c.getString(), this.getX() + xOffset, renderY, j | Mth.ceil((float)(this.alpha * 255.0f)) << 24, false);
                            continue;
                        }
                        pGuiGraphics.drawString(fontrenderer, c, this.getX() + xOffset, renderY, color | Mth.ceil((float)(this.alpha * 255.0f)) << 24, false);
                        xOffset += fontrenderer.width((FormattedText)c);
                    }
                } else {
                    pGuiGraphics.drawString(fontrenderer, formattedText.getString(), this.getX(), renderY, color, false);
                }
                Objects.requireNonNull(minecraft.font);
                renderY += 9;
            }
            Objects.requireNonNull(minecraft.font);
            pGuiGraphics.drawString(fontrenderer, this.count, this.getX() + this.width - fontrenderer.width(this.count), renderY -= 9, j | Mth.ceil((float)(this.alpha * 255.0f)) << 24, false);
            if (this.tooltipLines != null && this.isHovered) {
                GuiGuideBook.this.setTooltip(this.tooltipLines);
            }
        }

        public int getFGColor() {
            if (this.isHovered && this.active) {
                return 0xB300B3;
            }
            return this.overrideColor == -1 ? 0x444444 : this.overrideColor;
        }
    }

    public class ImageSpellPartButton
    extends MNAImageButton {
        final ResourceLocation spellTexture;
        final String translateKey;

        public ImageSpellPartButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int hoverOffset, ResourceLocation textureFile, int texWidth, int texHeight, Button.OnPress clickHandler, ResourceLocation spellTexture, ResourceLocation spellName) {
            super(x, y, width, height, xTexStart, yTexStart, hoverOffset, textureFile, texWidth, texHeight, clickHandler, (Component)Component.literal((String)""));
            this.spellTexture = spellTexture;
            this.translateKey = spellName.toString();
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
            super.renderWidget(pGuiGraphics, p_renderButton_1_, p_renderButton_2_, p_renderButton_3_);
            RenderSystem.disableDepthTest();
            pGuiGraphics.blit(this.spellTexture, this.getX() + 3, this.getY() + 1, 0.0f, 0.0f, 16, 16, 16, 16);
            RenderSystem.enableDepthTest();
            if (this.active && this.isHovered) {
                ArrayList<Component> tt = new ArrayList<Component>();
                tt.add((Component)Component.translatable((String)this.translateKey));
                currentTooltip = tt;
            }
        }

        public void playDownSound(SoundManager soundHandler) {
            soundHandler.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SFX.Gui.PAGE_FLIP, (float)((float)(0.8 + Math.random() * 0.4))));
        }
    }
}


