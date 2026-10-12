 
package com.mna.gui.widgets;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.gui.GuiTextures;
import com.mna.recipes.ItemAndPatternRecipe;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mna.api.tools.MNACap;

@OnlyIn(value=Dist.CLIENT)
public class SpellPartList
extends ObjectSelectionList<SpellPartList.SpellPartEntry> {
    private boolean _scrolling = false;
    public boolean _active = true;
    private final int tier;
    private final IPlayerRoteSpells rote;
    private Consumer<Shape> shapeClick;
    private Consumer<SpellEffect> componentClick;
    private Consumer<Modifier> modifierClick;
    private Consumer<Component> tooltip;
    public ISpellComponent _selected;
    private final boolean includeModifiers;
    private final boolean roteOnly;
    private String searchTerm;
    private boolean tierChecks;
    private int x0;
    private int y0;
    private int x1;
    private int y1;

    public SpellPartList(boolean includeModifiers, boolean roteOnly, int screenLeft, int screenTop, int x, int y, int width, int height, Consumer<Shape> onShapeClicked, Consumer<SpellEffect> onComponentClicked, Consumer<Modifier> onModifierClicked, Consumer<Component> tooltip) {
        super(Minecraft.getInstance(), width, height, screenTop + y, 24);
        this.setRenderHeader(false, 24);
        this.rote = (IPlayerRoteSpells)this.minecraft.player.getData(PlayerRoteSpellsProvider.ROTE.get());
        this.shapeClick = onShapeClicked;
        this.componentClick = onComponentClicked;
        this.modifierClick = onModifierClicked;
        this.tooltip = tooltip;
        this.includeModifiers = includeModifiers;
        this.roteOnly = roteOnly;
        this.tierChecks = false;
        this.x0 = screenLeft + x;
        this.x1 = screenLeft + x + width;
        this.y0 = screenTop + y;
        this.y1 = screenTop + y + height;
        this.tier = ((IPlayerProgression)this.minecraft.player.getData(PlayerProgressionProvider.PROGRESSION.get())).getTier();
        this.reInit("");
    }

    public SpellPartList enableTierChecks() {
        this.tierChecks = true;
        return this;
    }

    public void clear() {
        this.clearEntries();
    }

    public boolean isScrolling() {
        return this._scrolling;
    }

    private int compareSpellParts(ISpellComponent a, ISpellComponent b) {
        String aName = Component.translatable((String)a.getRegistryName().toString()).getString();
        String bName = Component.translatable((String)b.getRegistryName().toString()).getString();
        return aName.compareTo(bName);
    }

    private boolean bySearchTerm(ISpellComponent comp) {
        String translatedName = I18n.get((String)comp.getRegistryName().toString(), (Object[])new Object[0]).toLowerCase();
        return this.searchTerm == "" || translatedName.contains(this.searchTerm);
    }

    private boolean validParts(ISpellComponent p) {
        if (!this.minecraft.player.isCreative() && this.roteOnly && !this.rote.isRote(p)) {
            return false;
        }
        if (!this.roteOnly && p.isSilverSpell()) {
            return false;
        }
        boolean valid = p.isCraftable(new SpellCraftingContext((Player)this.minecraft.player));
        Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.minecraft.level.getRecipeManager().getRecipes().stream().filter(r -> r.id().equals(p.getRegistryName())).findFirst();
        if (pattern.isPresent() && pattern.get().value() instanceof ItemAndPatternRecipe) {
            valid &= ((ItemAndPatternRecipe)pattern.get().value()).getTier() <= this.tier;
        }
        return valid;
    }

    public void reInit(String searchTerm) {
        this.searchTerm = searchTerm;
        this.addIconsForAll(Registries.Shape.getValues().stream().sorted(this::compareSpellParts).filter(this::validParts).filter(this::bySearchTerm).collect(Collectors.toList()), s -> this.shapeClick.accept((Shape)s));
        this.addIconsForAll(Registries.SpellEffect.getValues().stream().sorted(this::compareSpellParts).filter(this::validParts).filter(this::bySearchTerm).collect(Collectors.toList()), s -> this.componentClick.accept((SpellEffect)s));
        if (this.includeModifiers) {
            this.addIconsForAll(Registries.Modifier.getValues().stream().sorted(this::compareSpellParts).filter(this::validParts).filter(this::bySearchTerm).collect(Collectors.toList()), s -> this.modifierClick.accept((Modifier)s));
        }
    }

    private <T extends ISpellComponent> void addIconsForAll(Collection<T> parts, Consumer<ISpellComponent> clickHandler) {
        int itemsPerRow = (this.width + 1) / 20;
        ArrayList<ISpellComponent> segment = new ArrayList<ISpellComponent>();
        for (ISpellComponent part : parts) {
            segment.add(part);
            if (segment.size() != itemsPerRow) continue;
            this.addEntry(new SpellPartEntry(segment, clickHandler));
            segment.clear();
        }
        if (segment.size() > 0) {
            this.addEntry(new SpellPartEntry(segment, clickHandler));
        }
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this._active) {
            return;
        }
        int scrollBarStartX = this.getScrollbarPosition();
        double scale = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int)((int)((double)this.x0 * scale)), (int)((int)((double)this.minecraft.getWindow().getHeight() - (double)this.y1 * scale)), (int)((int)((double)this.width * scale)), (int)((int)((double)this.height * scale)));
        this.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.getMaxScroll() > 0) {
            int scrollBarHeight = 20;
            int top = (int)this.getScrollAmount() * (this.y1 - this.y0 - scrollBarHeight) / this.getMaxScroll() + this.y0;
            if (top < this.y0) {
                top = this.y0;
            }
            pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, scrollBarStartX, top, 25.0f, 20.0f, 4, scrollBarHeight, 128, 128);
        }
        RenderSystem.disableScissor();
    }

    protected void renderList(GuiGraphics pGuiGraphics, int pX, int pY, float pPartialTick) {
        int i = this.getItemCount();
        for (int j = 0; j < i; ++j) {
            int k = this.getRowTop(j);
            int l = this.getRowBottom(j);
            if (l <= this.y0 || k >= this.y1 - 10) continue;
            int j1 = this.itemHeight - 4;
            SpellPartEntry e = (SpellPartEntry)this.getEntry(j);
            int k1 = this.getRowWidth();
            int j2 = this.getRowLeft();
            e.render(pGuiGraphics, j, k, j2, k1, j1, pX, pY, this.isMouseOver(pX, pY) && Objects.equals(this.getEntryAtPosition(pX, pY), e), pPartialTick);
        }
    }

    @Nullable
    protected final SpellPartEntry getEntryAtPos(double mouseX, double mouseY) {
        int lowerXBound = this.getRowLeft();
        int upperXBound = lowerXBound + this.getRowWidth();
        int adjustedY = Mth.floor((double)(mouseY - (double)this.y0)) + (int)this.getScrollAmount();
        int index = adjustedY / this.itemHeight;
        if (index >= 0 && adjustedY >= 0 && index < this.getItemCount() && mouseX < (double)this.getScrollbarPosition() && mouseX >= (double)lowerXBound && mouseX <= (double)upperXBound) {
            return (SpellPartEntry)(this.children().get(index));
        }
        return null;
    }

    protected int getScrollbarPosition() {
        return this.getRowLeft() + this.getRowWidth() - 4;
    }

    protected int getRowTop(int p_getRowTop_1_) {
        return this.y0 - (int)this.getScrollAmount() + p_getRowTop_1_ * this.itemHeight - 4;
    }

    protected int getRowBottom(int p_230948_1_) {
        return this.getRowTop(p_230948_1_) + this.itemHeight;
    }

    public int getRowLeft() {
        return this.x0;
    }

    public int getRowWidth() {
        return this.width;
    }

    protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
        super.updateScrollingState(p_updateScrollingState_1_, p_updateScrollingState_3_, p_updateScrollingState_5_);
        this._scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double)this.getScrollbarPosition() && p_updateScrollingState_1_ < (double)(this.getScrollbarPosition() + 6);
    }

    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (!this._active) {
            return false;
        }
        this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (!this.isMouseOver(p_mouseClicked_1_, p_mouseClicked_3_)) {
            return false;
        }
        SpellPartEntry e = this.getEntryAtPos(p_mouseClicked_1_, p_mouseClicked_3_);
        if (e != null) {
            if (e.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                e.setFocused(true);
                return true;
            }
        } else if (p_mouseClicked_5_ == 0) {
            this.setDragging(true);
            return true;
        }
        return this._scrolling;
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public void setSelected(@Nullable SpellPartEntry selected) {
        super.setSelected(selected);
    }

    @OnlyIn(value=Dist.CLIENT)
    public class SpellPartEntry
    extends ObjectSelectionList.Entry<SpellPartEntry> {
        private Collection<ISpellComponent> parts;
        private int spacing = 18;
        private ISpellComponent _hoveredComponent;
        private Consumer<ISpellComponent> _clickHandler;

        public SpellPartEntry(Collection<ISpellComponent> parts, Consumer<ISpellComponent> clickHandler) {
            this.parts = new ArrayList<ISpellComponent>(parts);
            this._hoveredComponent = null;
            this._clickHandler = clickHandler;
        }

        public void render(GuiGraphics pGuiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float p_render_9_) {
            int i = 0;
            for (ISpellComponent part : this.parts) {
                if (part == null) continue;
                boolean crossFaction = false;
                if (SpellPartList.this.tierChecks) {
                    IFaction playerFaction = (MNACap.get((IPlayerProgression)ManaAndArtifice.instance.proxy.getClientPlayer(), PlayerProgressionProvider.PROGRESSION).orElse(null)).getAlliedFaction();
                    crossFaction = playerFaction != null && part.getFactionRequirement() != null && playerFaction != part.getFactionRequirement();
                }
                int x = 5 + left + i++ * this.spacing;
                int y = top + 5;
                if (isHovered && mouseX >= x && mouseX <= x + this.spacing) {
                    if (SpellPartList.this.tooltip != null) {
                        if (part.isSilverSpell()) {
                            SpellPartList.this.tooltip.accept((Component)Component.translatable((String)part.getRegistryName().toString()).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.AQUA));
                        } else {
                            SpellPartList.this.tooltip.accept((Component)Component.translatable((String)part.getRegistryName().toString()));
                        }
                        if (crossFaction) {
                            SpellPartList.this.tooltip.accept((Component)Component.translatable((String)"gui.mna.inscription.wrong_faction"));
                        }
                    }
                    this._hoveredComponent = part;
                    pGuiGraphics.blit(GuiTextures.WizardLab.THESIS_DESK, x - 1, y - 1, 0, 208, 20, 20);
                }
                if (SpellPartList.this._selected == part) {
                    pGuiGraphics.blit(GuiTextures.WizardLab.THESIS_DESK, x - 1, y - 1, 0, 208, 20, 20);
                }
                this.renderIcon(pGuiGraphics, x, y, part.getGuiIcon());
                if (part.isSilverSpell()) {
                    GuiRenderUtils.renderSilverSpellBorder(pGuiGraphics, x, y, 16, 16);
                }
                if (!crossFaction) continue;
                int size = 16;
                pGuiGraphics.blit(GuiTextures.WizardLab.INSCRIPTION_TABLE_WIDGETS, x, y, 0, 0.0f, 112.0f, size, size, 128, 128);
            }
        }

        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            SpellPartList.this.setSelected(this);
            SpellPartList.this._selected = this._hoveredComponent;
            if (this._clickHandler != null && this._hoveredComponent != null) {
                this._clickHandler.accept(this._hoveredComponent);
            }
            return true;
        }

        private void renderIcon(GuiGraphics pGuiGraphics, int x, int y, ResourceLocation item) {
            int size = 16;
            pGuiGraphics.blit(item, x, y, 0, 0.0f, 0.0f, size, size, size, size);
        }

        public Component getNarration() {
            return Component.translatable((String)"narrator.select", (Object[])new Object[]{this._hoveredComponent != null ? this._hoveredComponent.getRegistryName().toString() : "none"});
        }
    }
}


