 
package com.mna.gui.block;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter;
import com.mna.gui.GuiTextures;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.block.ContainerLodestar;
import com.mna.gui.widgets.lodestar.AreaParameterInput;
import com.mna.gui.widgets.lodestar.BooleanParameterInput;
import com.mna.gui.widgets.lodestar.FilterParameterInput;
import com.mna.gui.widgets.lodestar.IntegerParameterInput;
import com.mna.gui.widgets.lodestar.ItemStackParameterInput;
import com.mna.gui.widgets.lodestar.LodestarGroup;
import com.mna.gui.widgets.lodestar.LodestarNode;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import com.mna.gui.widgets.lodestar.PointParameterInput;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.EditBox;
import com.mna.gui.widgets.MNAImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import net.minecraft.client.gui.components.ImageButton;

public class GuiLodestarV2
extends GuiJEIDisable<ContainerLodestar> {
    public static final int MAIN_WIDTH = 256;
    public static final int MAIN_HEIGHT = 165;
    public static final int BACKGROUND_SIZE = 512;
    public static final int BACKGROUND_OFFSET = 10;
    public static final int COMMANDS_WIDTH = 56;
    public static final int COMMANDS_HEIGHT = 164;
    public static final int PARAMS_WIDTH = 68;
    public static final int PARAMS_HEIGHT = 164;
    public static final int PARAMS_START_X = 5;
    public static final int PARAMS_START_Y = 21;
    public static final int PARAMS_INCREMENT_Y = 22;
    public static final int START_BUTTON_X = 4;
    public static final int START_BUTTON_Y = 10;
    public static final int START_BUTTON_SIZE = 8;
    private CommandScrollList actionsList;
    private EditBox groupBox;
    private EditBox filterBox;
    private Component currentGroupBoxInput;
    private Component currentFilterBoxInput;
    private Component currentTooltip;
    public static float Zoom = 1.0f;
    private int scroll_pos_x = 0;
    private int scroll_pos_y = 0;
    private double scroll_accum_x = 0.0;
    private double scroll_accum_y = 0.0;
    private boolean canBackgroundScroll = false;
    private List<LodestarNode> nodes;
    private List<LodestarGroup> nodeGroups;
    private LodestarNode selectedNode;
    private boolean drawingGroup = false;
    private List<LodestarParameter<?>> parameterWidgets;
    private LodestarNode lineConnectionOrigin;
    private int lineConnectionOriginNodeIndex = -1;
    private boolean draggingStartNode = false;
    private ImageButton startNodeButton = null;
    private LodestarNode draggingNode;
    private LodestarGroup draggingGroup;
    private List<LodestarNode> draggingGroupNodes;
    private LodestarGroup creatingGroup;
    private LodestarGroup selectedGroup;
    private ConstructTask draggingOutTask;
    private boolean isDownloadingLogic = false;

    public GuiLodestarV2(ContainerLodestar screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.nodes = new ArrayList<LodestarNode>();
        this.draggingGroupNodes = new ArrayList<LodestarNode>();
        this.nodeGroups = new ArrayList<LodestarGroup>();
        this.parameterWidgets = new ArrayList();
    }

    protected void init() {
        super.init();
        this.nodes.clear();
        this.nodeGroups.clear();
        this.selectedNode = null;
        this.draggingNode = null;
        this.draggingOutTask = null;
        this.parameterWidgets.clear();
        this.clearWidgets();
        this.actionsList = new CommandScrollList(((ContainerLodestar)this.menu).isLowTier());
        this.addRenderableWidget(this.actionsList);
        this.groupBox = new EditBox(this.minecraft.font, this.leftPos + 4, this.topPos - 12, this.getXSize() - 8, 12, this.currentGroupBoxInput);
        this.groupBox.setMaxLength(100);
        this.groupBox.setResponder(this::onGroupTextChanged);
        this.groupBox.active = false;
        this.groupBox.visible = false;
        this.addRenderableWidget(this.groupBox);
        this.filterBox = new EditBox(this.minecraft.font, this.leftPos - 51, this.topPos - (((ContainerLodestar)this.menu).isLowTier() ? 7 : 5), 50, 12, this.currentFilterBoxInput);
        this.filterBox.setMaxLength(20);
        this.filterBox.setResponder(this::onFilterTextChanged);
        this.filterBox.active = true;
        this.filterBox.visible = true;
        this.addRenderableWidget(this.filterBox);
        this.startNodeButton = new MNAImageButton(this.leftPos + 4, this.topPos + 10, 8, 8, 0, 216, 0, this.getMainTexture(), 256, 256, b -> {
            this.draggingStartNode = true;
        });
        this.addRenderableWidget(this.startNodeButton);
        ImageButton recenterButton = new MNAImageButton(this.leftPos + 14, this.topPos + 165, 14, 14, 140, 242, 0, this.getMainTexture(), 256, 256, b -> this.recenterView());
        recenterButton.setTooltip(Tooltip.create((Component)Component.translatable((String)"gui.mna.lodestar.recenter")));
        this.addRenderableWidget(recenterButton);
        ImageButton drawGroupButton = new MNAImageButton(this.leftPos + this.getXSize() - 28, this.topPos + 165, 14, 14, 140, 228, 0, this.getMainTexture(), 256, 256, b -> {
            this.drawingGroup = !this.drawingGroup;
        });
        drawGroupButton.setTooltip(Tooltip.create((Component)Component.translatable((String)"gui.mna.lodestar.draw_group_toggle")));
        this.addRenderableWidget(drawGroupButton);
        this.isDownloadingLogic = true;
    }

    private void onGroupTextChanged(String newText) {
        if (this.selectedGroup != null) {
            this.selectedGroup.setLabel(newText);
        }
    }

    private void onFilterTextChanged(String newText) {
        this.actionsList.setFilter(newText);
    }

    private void addParameter(LodestarParameter<?> btn) {
        this.parameterWidgets.add(btn);
        this.addRenderableWidget(btn);
    }

    private void addNode(int x, int y, ConstructTask task) {
        LodestarNode added = new LodestarNode(this.leftPos + x, this.topPos + y, ((ContainerLodestar)this.menu).isLowTier(), task, b -> this.nodeSubClick((LodestarNode)b), this::getNodeById);
        this.insertNode(added);
    }

    private void insertNode(LodestarNode node) {
        this.nodes.add(node);
        this.addWidget(node);
    }

    private void deleteNode(LodestarNode node) {
        if (this.selectedNode == node) {
            this.deselectNode();
        }
        this.nodes.remove(node);
        this.removeWidget((GuiEventListener)node);
    }

    private void groupClicked(LodestarGroup group, boolean forDelete) {
        double clickY;
        if (forDelete) {
            this.nodeGroups.remove(group);
            return;
        }
        double clickX = group.getClickX();
        if (!this.isMouseWithinStage(clickX * (double)Zoom, (clickY = group.getClickY()) * (double)Zoom)) {
            return;
        }
        this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        this.draggingGroup = group;
        this.draggingGroupNodes = this.getNodesInGroup(group);
        this.selectedGroup = group;
        this.groupBox.active = true;
        this.groupBox.visible = true;
        this.groupBox.setValue(group.getLabel());
    }

    private void nodeSubClick(LodestarNode node) {
        double clickY;
        double clickX = node.getClickX();
        if (!this.isMouseWithinStage(clickX * (double)Zoom, (clickY = node.getClickY()) * (double)Zoom)) {
            return;
        }
        int widgetIndex = node.getSubClickIndex();
        this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        switch (widgetIndex) {
            case 97: {
                this.deleteNode(node);
                ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
                return;
            }
            case 98: {
                this.nodes.forEach(n -> n.disconnectFromNode(node));
                ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
                return;
            }
            case 99: {
                this.draggingNode = node;
                this.selectNode(node);
                return;
            }
        }
        this.lineConnectionOrigin = node;
        this.lineConnectionOriginNodeIndex = widgetIndex;
    }

    private void deselectNode() {
        if (this.selectedNode == null) {
            return;
        }
        this.selectedNode.saveParameters(this.parameterWidgets);
        this.parameterWidgets.forEach(w -> this.removeWidget((GuiEventListener)w));
        this.parameterWidgets.clear();
        this.selectedNode.deselect();
        this.selectedNode = null;
    }

    private void selectNode(LodestarNode node) {
        if (this.selectedNode != null) {
            this.deselectNode();
        }
        MutableInt paramIndex = new MutableInt(0);
        node.getParameters().forEach(param -> {
            switch (param.getTypeID()) {
                case AREA: {
                    this.addParameter(new AreaParameterInput(((ContainerLodestar)this.menu).isLowTier(), this.getParameterX(), this.getParameterY(paramIndex.getValue()), b -> {}, param.getTooltip()));
                    break;
                }
                case BOOLEAN: {
                    this.addParameter(new BooleanParameterInput(((ContainerLodestar)this.menu).isLowTier(), this.getParameterX(), this.getParameterY(paramIndex.getValue()), b -> {}, param.getTooltip()));
                    break;
                }
                case FILTER: {
                    this.addParameter(new FilterParameterInput(((ContainerLodestar)this.menu).isLowTier(), this.getParameterX(), this.getParameterY(paramIndex.getValue()), b -> {}, param.getTooltip()));
                    break;
                }
                case INTEGER: {
                    this.addParameter(new IntegerParameterInput(((ContainerLodestar)this.menu).isLowTier(), this.getParameterX(), this.getParameterY(paramIndex.getValue()), ((ConstructTaskIntegerParameter)param).getMinimum(), ((ConstructTaskIntegerParameter)param).getMinimum(), ((ConstructTaskIntegerParameter)param).getMaximum(), b -> {}, param.getTooltip()));
                    break;
                }
                case POINT: {
                    this.addParameter(new PointParameterInput(((ContainerLodestar)this.menu).isLowTier(), this.getParameterX(), this.getParameterY(paramIndex.getValue()), b -> {}, param.getTooltip()));
                    break;
                }
                case ITEMSTACK: {
                    this.addParameter(new ItemStackParameterInput(((ContainerLodestar)this.menu).isLowTier(), this.getParameterX(), this.getParameterY(paramIndex.getValue()), b -> {}, param.getTooltip()));
                    break;
                }
                default: {
                    ManaAndArtifice.LOGGER.error("Unknown parameter type for construct task!  No known parameter widget.  Skipping.");
                }
            }
            paramIndex.increment();
        });
        this.selectedNode = node;
        this.selectedNode.select();
        this.selectedNode.loadParameters(this.parameterWidgets);
    }

    private void panBackground(double deltaX, double deltaY) {
        if (!this.canBackgroundScroll) {
            return;
        }
        this.scroll_accum_x += deltaX / (double)Zoom;
        this.scroll_accum_y += deltaY / (double)Zoom;
        if (Math.abs(this.scroll_accum_x) > 1.0 || Math.abs(this.scroll_accum_y) > 1.0) {
            net.minecraft.client.gui.components.AbstractWidget node;
            int i;
            int dX = (int)(Math.floor(Math.abs(this.scroll_accum_x)) * Math.signum(this.scroll_accum_x));
            int dY = (int)(Math.floor(Math.abs(this.scroll_accum_y)) * Math.signum(this.scroll_accum_y));
            this.scroll_accum_x -= (double)dX;
            this.scroll_accum_y -= (double)dY;
            int scroll_pos_x_o = this.scroll_pos_x;
            int scroll_pos_y_o = this.scroll_pos_y;
            this.scroll_pos_x -= dX;
            this.scroll_pos_y -= dY;
            int adX = scroll_pos_x_o - this.scroll_pos_x;
            int adY = scroll_pos_y_o - this.scroll_pos_y;
            for (i = 0; i < this.nodes.size(); ++i) {
                node = this.nodes.get(i);
                node.setX(node.getX() + adX);
                node.setY(node.getY() + adY);
            }
            for (i = 0; i < this.nodeGroups.size(); ++i) {
                node = this.nodeGroups.get(i);
                node.setX(node.getX() + adX);
                node.setY(node.getY() + adY);
            }
        }
    }

    private void startDraggingCommandFromList(ConstructTask action) {
        this.draggingOutTask = action;
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int button) {
        if (mouseX >= (double)(this.leftPos - 56) && mouseX < (double)(this.leftPos + 256 + 68) && mouseY >= (double)this.topPos && mouseY <= (double)(this.topPos + 165)) {
            return false;
        }
        int halfPlayerInvWidth = 88;
        return !(mouseX >= (double)(this.leftPos + this.imageWidth / 2 - halfPlayerInvWidth)) || !(mouseX <= (double)(this.leftPos + this.imageWidth / 2 + halfPlayerInvWidth)) || !(mouseY >= (double)(this.topPos + 165)) || !(mouseY <= (double)(this.topPos + this.imageHeight));
    }

    private ResourceLocation getMainTexture() {
        return ((ContainerLodestar)this.menu).isLowTier() ? GuiTextures.Blocks.LODESTAR_LESSER_MAIN : GuiTextures.Blocks.LODESTAR_MAIN;
    }

    private ResourceLocation getBackgroundTexture() {
        return ((ContainerLodestar)this.menu).isLowTier() ? GuiTextures.Blocks.LODESTAR_LESSER_BACKGROUND : GuiTextures.Blocks.LODESTAR_BACKGROUND;
    }

    private ResourceLocation getExtensionTexture() {
        return ((ContainerLodestar)this.menu).isLowTier() ? GuiTextures.Blocks.LODESTAR_LESSER_EXTENSION : GuiTextures.Blocks.LODESTAR_EXTENSION;
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground
        // chama renderBg -> recursao infinita (StackOverflow).
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        pGuiGraphics.blit(this.getMainTexture(), this.leftPos - 45, this.topPos - 45, 166, 166, 90, 90);
        pGuiGraphics.blit(this.getMainTexture(), this.leftPos + 211, this.topPos + 121, 166, 166, 90, 90);
        this.renderNodes(pGuiGraphics, partialTick, mouseX, mouseY);
        pGuiGraphics.blit(this.getMainTexture(), this.leftPos, this.topPos, 0, 0, 256, 165);
        pGuiGraphics.blit(this.getMainTexture(), this.leftPos, this.topPos + 7, 0, 187, 8, 14);
        pGuiGraphics.blit(this.getExtensionTexture(), this.leftPos - 56 + 2, this.topPos - 2, 0, 0, 56, 164);
        pGuiGraphics.blit(this.getExtensionTexture(), this.leftPos + 256 - 2, this.topPos - 2, 56, 0, 68, 164);
        GuiRenderUtils.renderStandardPlayerInventory(pGuiGraphics, this.leftPos + this.imageWidth / 2, this.topPos + 165 + 45);
        if (this.draggingOutTask != null) {
            pGuiGraphics.blit(this.draggingOutTask.getIconTexture(), mouseX - 8, mouseY - 8, 0, 0.0f, 0.0f, 16, 16, 16, 16);
        }
        if (this.selectedNode != null) {
            pGuiGraphics.blit(this.selectedNode.getTask().getIconTexture(), this.leftPos + 256 + 24, this.topPos, 0.0f, 0.0f, 16, 16, 16, 16);
        }
    }

    private void renderNodes(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        Pair<Integer, Integer> mouseScaled = this.getScaledMouseCoordinates(mouseX, mouseY);
        pGuiGraphics.enableScissor(this.leftPos, this.topPos, this.leftPos + 256, this.topPos + 165);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(Zoom, Zoom, Zoom);
        pGuiGraphics.blit(this.getBackgroundTexture(), (int)((float)(this.leftPos + 5) / Zoom), (int)((float)(this.topPos + 5) / Zoom), (float)Math.round(this.scroll_pos_x), (float)Math.round(this.scroll_pos_y), (int)(246.0f / Zoom), (int)(155.0f / Zoom), 512, 512);
        this.nodeGroups.forEach(group -> group.render(pGuiGraphics, mouseX, mouseY, partialTick));
        this.nodes.forEach(node -> node.renderConnections(pGuiGraphics, this.leftPos, this.topPos, partialTick));
        this.nodes.forEach(node -> {
            node.render(pGuiGraphics, (Integer)mouseScaled.getFirst(), (Integer)mouseScaled.getSecond(), partialTick);
            node.renderErrors(pGuiGraphics, ((ContainerLodestar)this.menu).getNodeErrors());
        });
        if (this.lineConnectionOrigin != null || this.draggingStartNode) {
            int idx;
            Optional node2;
            int lineColor = LodestarNode.COLOR_RED;
            if (this.isMouseWithinStage(mouseX, mouseY) && (node2 = this.getChildAt(mouseX, mouseY)).isPresent() && node2.get() instanceof LodestarNode && (idx = ((LodestarNode)(node2.get())).getSubWidgetAt(mouseX, mouseY)) == 98) {
                int n = lineColor = this.draggingStartNode ? LodestarNode.COLOR_BLUE : LodestarNode.COLOR_GREEN;
            }
            if (this.lineConnectionOrigin != null) {
                this.lineConnectionOrigin.renderConnectingLine(pGuiGraphics, this.lineConnectionOriginNodeIndex, ((Integer)mouseScaled.getFirst()).intValue(), ((Integer)mouseScaled.getSecond()).intValue(), lineColor);
            } else {
                GuiRenderUtils.bezierLineBetween(pGuiGraphics, (int)((float)(this.leftPos + 4 + 4 + 3) / Zoom), (int)(((float)(this.topPos + 10 + 4) + 0.5f) / Zoom), ((Integer)mouseScaled.getFirst()).intValue(), ((Integer)mouseScaled.getSecond()).intValue(), 50.0f, 3.0f, lineColor, lineColor);
            }
        }
        pGuiGraphics.pose().popPose();
        pGuiGraphics.disableScissor();
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.currentTooltip = null;
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.currentTooltip != null) {
            pGuiGraphics.renderTooltip(this.font, this.currentTooltip, mouseX, mouseY);
        } else {
            this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        }
        if (this.isDownloadingLogic && ((ContainerLodestar)this.menu).isLogicDownloaded()) {
            this.isDownloadingLogic = false;
            this.onLogicDownloaded();
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y) {
        String text = "";
        List<Component> tooltipLines = null;
        if (this.drawingGroup) {
            pGuiGraphics.blit(this.getMainTexture(), 40, -14, 154, 220, 12, 12);
            text = Component.translatable((String)"gui.mna.lodestar.draw_group_toggle.prompt").getString();
            pGuiGraphics.drawString(this.font, text, 56, -12, 0xFFFFFF, false);
        } else {
            int y = -14;
            if (this.selectedGroup != null) {
                y = -26;
            }
            if (((ContainerLodestar)this.menu).getErrors().size() > 0) {
                pGuiGraphics.blit(this.getMainTexture(), 40, y, 154, 244, 12, 12);
                text = Component.translatable((String)"gui.mna.lodestar.error").getString();
                pGuiGraphics.drawString(this.font, text, 56, y + 2, 0xFFFFFF, false);
                if (this.isMousedOverWarnings(mouse_x, mouse_y, text)) {
                    tooltipLines = ((ContainerLodestar)this.menu).getErrors();
                }
            } else if (((ContainerLodestar)this.menu).getWarnings().size() > 0) {
                pGuiGraphics.blit(this.getMainTexture(), 40, y, 154, 232, 12, 12);
                text = Component.translatable((String)"gui.mna.lodestar.warning").getString();
                pGuiGraphics.drawString(this.font, text, 56, y + 2, 0xFFFFFF, false);
                if (this.isMousedOverWarnings(mouse_x, mouse_y, text)) {
                    tooltipLines = ((ContainerLodestar)this.menu).getWarnings();
                }
            }
            if (tooltipLines != null) {
                ArrayList split = new ArrayList();
                tooltipLines.forEach(c -> split.addAll(this.font.split((FormattedText)c, this.minecraft.screen.width / 4)));
                pGuiGraphics.renderTooltip(this.font, split, mouse_x - this.leftPos, mouse_y - this.topPos);
            }
        }
    }

    private boolean isMousedOverWarnings(int mouse_x, int mouse_y, String promptText) {
        if (mouse_x >= this.leftPos + 40 && mouse_x <= this.leftPos + 40 + this.font.width(promptText)) {
            return mouse_y >= this.topPos - (this.selectedGroup != null ? 34 : 14) && mouse_y <= this.topPos - (this.selectedGroup != null ? 20 : 0);
        }
        return false;
    }

    public void onLogicDownloaded() {
        this.loadLogic(((ContainerLodestar)this.menu).getLogic());
        this.recenterView();
    }

    public boolean mouseClicked(double x, double y, int button) {
        if (button == 1) {
            if (this.groupBox.isMouseOver(x, y)) {
                this.groupBox.setValue("");
            }
            if (this.filterBox.isMouseOver(x, y)) {
                this.filterBox.setValue("");
            }
            return true;
        }
        this.canBackgroundScroll = this.isMouseWithinStage(x, y);
        if (this.groupBox.mouseClicked(x, y, button)) {
            this.groupBox.setFocused(true);
            return true;
        }
        this.selectedGroup = null;
        this.groupBox.setFocused(false);
        this.groupBox.active = false;
        this.groupBox.visible = false;
        if (this.filterBox.mouseClicked(x, y, button)) {
            this.filterBox.setFocused(true);
            return true;
        }
        this.filterBox.setFocused(false);
        if (this.drawingGroup && this.canBackgroundScroll) {
            Pair<Integer, Integer> scaled = this.getScaledMouseCoordinates(x, y);
            this.creatingGroup = new LodestarGroup((Integer)scaled.getFirst(), (Integer)scaled.getSecond(), 50, 50, ((ContainerLodestar)this.menu).isLowTier(), this::groupClicked);
            this.nodeGroups.add(this.creatingGroup);
            return true;
        }
        for (LodestarParameter<?> lodestarParameter : this.parameterWidgets) {
            if (!lodestarParameter.mouseClicked(x, y, button)) continue;
            if (this.selectedNode != null) {
                this.selectedNode.saveParameters(this.parameterWidgets);
                ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
            }
            this.setFocused((GuiEventListener)lodestarParameter);
            return true;
        }
        if (this.canBackgroundScroll) {
            com.mojang.datafixers.util.Pair<Integer,Integer> mouseScaled = this.getScaledMouseCoordinates(x, y);
            for (LodestarNode lodestarNode : this.nodes) {
                if (!lodestarNode.mouseClicked(((Integer)mouseScaled.getFirst()).intValue(), ((Integer)mouseScaled.getSecond()).intValue(), button)) continue;
                return true;
            }
            for (LodestarGroup lodestarGroup : this.nodeGroups) {
                if (!lodestarGroup.mouseClicked(((Integer)mouseScaled.getFirst()).intValue(), ((Integer)mouseScaled.getSecond()).intValue(), button)) continue;
                return true;
            }
        }
        for (LodestarNode lodestarNode : this.nodes) {
            lodestarNode.active = false;
        }
        for (LodestarGroup lodestarGroup : this.nodeGroups) {
            lodestarGroup.active = false;
        }
        boolean superClick = super.mouseClicked(x, y, button);
        for (LodestarNode lodestarNode : this.nodes) {
            lodestarNode.active = true;
        }
        for (LodestarGroup lodestarGroup : this.nodeGroups) {
            lodestarGroup.active = true;
        }
        return superClick;
    }

    public boolean mouseReleased(double mouse_x, double mouse_y, int button) {
        this.canBackgroundScroll = false;
        if (this.drawingGroup) {
            if (this.creatingGroup != null) {
                this.creatingGroup = null;
                this.drawingGroup = false;
            }
            return true;
        }
        this.setFocused(null);
        if (this.draggingNode != null) {
            this.draggingNode = null;
        }
        if (this.draggingGroup != null) {
            this.draggingGroup.scaling = false;
            this.draggingGroup = null;
            this.draggingGroupNodes.clear();
        }
        Pair<Integer, Integer> mouseScaled = this.getScaledMouseCoordinates(mouse_x, mouse_y);
        if (this.lineConnectionOrigin != null || this.draggingStartNode) {
            this.getNodeAt(((Integer)mouseScaled.getFirst()).intValue(), ((Integer)mouseScaled.getSecond()).intValue()).ifPresent(g -> {
                int idx;
                if (g instanceof LodestarNode && (idx = ((LodestarNode)(g)).getSubWidgetAt(((Integer)mouseScaled.getFirst()).intValue(), ((Integer)mouseScaled.getSecond()).intValue())) == 98) {
                    if (this.draggingStartNode) {
                        this.nodes.forEach(node -> node.setStart(false));
                        ((LodestarNode)(g)).setStart(true);
                        ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
                    } else {
                        this.lineConnectionOrigin.connectToNode((LodestarNode)(g), this.lineConnectionOriginNodeIndex);
                        ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
                    }
                }
            });
            this.lineConnectionOrigin = null;
            this.draggingStartNode = false;
        }
        if (this.draggingOutTask != null) {
            if (this.isMouseWithinStage(mouse_x, mouse_y)) {
                this.addNode((Integer)mouseScaled.getFirst() - this.leftPos - 24, (Integer)mouseScaled.getSecond() - this.topPos - 20, this.draggingOutTask);
                ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
            }
            this.draggingOutTask = null;
        }
        return super.mouseReleased(mouse_x, mouse_y, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.actionsList._scrolling) {
            return this.actionsList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        if (this.drawingGroup) {
            if (this.creatingGroup != null) {
                Pair<Integer, Integer> mouseScaled = this.getScaledMouseCoordinates(mouseX, mouseY);
                int width = (Integer)mouseScaled.getFirst() - this.creatingGroup.getX();
                int height = (Integer)mouseScaled.getSecond() - this.creatingGroup.getY();
                this.creatingGroup.resize(Math.abs(width), Math.abs(height));
            }
            return true;
        }
        if (this.draggingGroup != null) {
            Pair<Integer, Integer> mouseScaled = this.getScaledMouseCoordinates(mouseX, mouseY);
            if (this.draggingGroup.scaling) {
                int width = (Integer)mouseScaled.getFirst() - this.draggingGroup.getX();
                int height = (Integer)mouseScaled.getSecond() - this.draggingGroup.getY();
                this.draggingGroup.resize(Math.abs(width), Math.abs(height));
            } else {
                int newX = (int)((double)((Integer)mouseScaled.getFirst()).intValue() - this.draggingGroup.getClickXOffset());
                int newY = (int)((double)((Integer)mouseScaled.getSecond()).intValue() - this.draggingGroup.getClickYOffset());
                int scaledDeltaX = this.draggingGroup.getX() - newX;
                int scaledDeltaY = this.draggingGroup.getY() - newY;
                this.draggingGroupNodes.forEach(node -> {
                    node.setX(node.getX() - scaledDeltaX);
                    node.setY(node.getY() - scaledDeltaY);
                });
                this.draggingGroup.setX(newX);
                this.draggingGroup.setY(newY);
            }
            return true;
        }
        if (this.draggingNode != null) {
            Pair<Integer, Integer> mouseScaled = this.getScaledMouseCoordinates(mouseX, mouseY);
            int newX = (int)((double)((Integer)mouseScaled.getFirst()).intValue() - this.draggingNode.getClickXOffset());
            int newY = (int)((double)((Integer)mouseScaled.getSecond()).intValue() - this.draggingNode.getClickYOffset());
            this.draggingNode.setX(newX);
            this.draggingNode.setY(newY);
            return true;
        }
        if (this.lineConnectionOrigin != null || this.draggingOutTask != null || this.draggingStartNode) {
            return true;
        }
        this.panBackground(deltaX, deltaY);
        if (this.getFocused() != null && button == 0 && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double p_scrollX) {
        if (this.actionsList.isMouseOver(mouseX, mouseY)) {
            return this.actionsList.mouseScrolled(mouseX, mouseY, delta, 0.0);
        }
        if (this.isMouseWithinStage(mouseX, mouseY)) {
            net.minecraft.client.gui.components.AbstractWidget node;
            int i;
            Pair<Integer, Integer> scaledMousePre = this.getScaledMouseCoordinates(mouseX, mouseY);
            float zoomAmt = (float)(0.2 * Math.signum(delta));
            Zoom += zoomAmt;
            Zoom = MathUtils.clamp(Zoom, 0.2f, 1.2f);
            Pair<Integer, Integer> scaledMousePost = this.getScaledMouseCoordinates(mouseX, mouseY);
            int adX = (Integer)scaledMousePost.getFirst() - (Integer)scaledMousePre.getFirst();
            int adY = (Integer)scaledMousePost.getSecond() - (Integer)scaledMousePre.getSecond();
            this.scroll_pos_x -= adX / 2;
            this.scroll_pos_y -= adY / 2;
            for (i = 0; i < this.nodes.size(); ++i) {
                node = this.nodes.get(i);
                node.setX(node.getX() + adX);
                node.setY(node.getY() + adY);
            }
            for (i = 0; i < this.nodeGroups.size(); ++i) {
                node = this.nodeGroups.get(i);
                node.setX(node.getX() + adX);
                node.setY(node.getY() + adY);
            }
        }
        return true;
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode != 256) {
            if (this.selectedGroup != null && this.groupBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
            if (this.filterBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
            if (this.groupBox.isFocused() && this.groupBox.isVisible()) {
                return true;
            }
            if (this.filterBox.isFocused() && this.filterBox.isVisible()) {
                return true;
            }
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode != 256) {
            if (this.selectedGroup != null && this.groupBox.keyReleased(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
            if (this.filterBox.keyReleased(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
            if (this.groupBox.isFocused() && this.groupBox.isVisible()) {
                return true;
            }
            if (this.filterBox.isFocused() && this.filterBox.isVisible()) {
                return true;
            }
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.selectedGroup != null && this.groupBox.isVisible() && this.groupBox.isFocused()) {
            return this.groupBox.charTyped(pCodePoint, pModifiers);
        }
        if (this.filterBox.isVisible() && this.filterBox.isFocused()) {
            return this.filterBox.charTyped(pCodePoint, pModifiers);
        }
        return super.charTyped(pCodePoint, pModifiers);
    }

    public void onClose() {
        if (this.selectedNode != null) {
            this.deselectNode();
        }
        ((ContainerLodestar)this.menu).setTileLogic(this.saveLogic());
        super.onClose();
    }

    private void recenterView() {
        if (this.nodes.size() == 0 && this.nodeGroups.size() == 0) {
            return;
        }
        int minX = Math.min(this.nodes.size() > 0 ? this.nodes.stream().flatMapToInt(n -> IntStream.of(n.getX())).min().getAsInt() : Integer.MAX_VALUE, this.nodeGroups.size() > 0 ? this.nodeGroups.stream().flatMapToInt(n -> IntStream.of(n.getX())).min().getAsInt() : Integer.MAX_VALUE);
        int maxX = Math.max(this.nodes.size() > 0 ? this.nodes.stream().flatMapToInt(n -> IntStream.of(n.getX() + n.getWidth())).max().getAsInt() : Integer.MIN_VALUE, this.nodeGroups.size() > 0 ? this.nodeGroups.stream().flatMapToInt(n -> IntStream.of(n.getX() + n.getWidth())).max().getAsInt() : Integer.MIN_VALUE);
        int minY = Math.min(this.nodes.size() > 0 ? this.nodes.stream().flatMapToInt(n -> IntStream.of(n.getY())).min().getAsInt() : Integer.MAX_VALUE, this.nodeGroups.size() > 0 ? this.nodeGroups.stream().flatMapToInt(n -> IntStream.of(n.getY())).min().getAsInt() : Integer.MAX_VALUE);
        int maxY = Math.max(this.nodes.size() > 0 ? this.nodes.stream().flatMapToInt(n -> IntStream.of(n.getY() + n.getHeight())).max().getAsInt() : Integer.MIN_VALUE, this.nodeGroups.size() > 0 ? this.nodeGroups.stream().flatMapToInt(n -> IntStream.of(n.getY() + n.getHeight())).max().getAsInt() : Integer.MIN_VALUE);
        int cX = (minX + maxX) / 2;
        int cY = (minY + maxY) / 2;
        int vpCx = (int)(256.0f / Zoom);
        int vpCy = (int)(165.0f / Zoom);
        int deltaX = vpCx - cX + 24;
        int deltaY = vpCy - cY + 20;
        for (LodestarNode lodestarNode : this.nodes) {
            lodestarNode.setX(lodestarNode.getX() + deltaX);
            lodestarNode.setY(lodestarNode.getY() + deltaY);
        }
        for (LodestarGroup lodestarGroup : this.nodeGroups) {
            lodestarGroup.setX(lodestarGroup.getX() + deltaX);
            lodestarGroup.setY(lodestarGroup.getY() + deltaY);
        }
    }

    private CompoundTag saveLogic() {
        ListTag data = new ListTag();
        this.nodes.forEach(node -> data.add(node.toCompoundTag(this.topPos - this.scroll_pos_y, this.leftPos - this.scroll_pos_x)));
        ListTag groups = new ListTag();
        this.nodeGroups.forEach(group -> groups.add(group.toCompoundTag(this.topPos - this.scroll_pos_y, this.leftPos - this.scroll_pos_x)));
        CompoundTag output = new CompoundTag();
        output.put("commands", (Tag)data);
        output.put("groups", (Tag)groups);
        return output;
    }

    private void loadLogic(CompoundTag input) {
        if (input == null || !input.contains("commands")) {
            return;
        }
        ListTag data = input.getList("commands", 10);
        data.forEach(tag -> {
            LodestarNode node = LodestarNode.fromCompoundTag((CompoundTag)tag, this.topPos, this.leftPos, ((ContainerLodestar)this.menu).isLowTier(), b -> this.nodeSubClick((LodestarNode)b), this::getNodeById);
            if (node != null) {
                this.insertNode(node);
            }
        });
        if (input.contains("groups")) {
            ListTag groupData = input.getList("groups", 10);
            groupData.forEach(tag -> {
                LodestarGroup group = LodestarGroup.fromCompound((CompoundTag)tag, this.topPos, this.leftPos, ((ContainerLodestar)this.menu).isLowTier(), this::groupClicked);
                if (group != null) {
                    this.nodeGroups.add(group);
                }
            });
        }
    }

    private boolean isMouseWithinStage(double mouse_x, double mouse_y) {
        return mouse_x >= (double)this.leftPos && mouse_x <= (double)(this.leftPos + 256) && mouse_y >= (double)this.topPos && mouse_y <= (double)(this.topPos + 165);
    }

    private Pair<Integer, Integer> getScaledMouseCoordinates(double mouse_x, double mouse_y) {
        return new Pair(((int)(mouse_x / (double)Zoom)), ((int)(mouse_y / (double)Zoom)));
    }

    public LodestarNode getNodeById(String id) {
        Optional<LodestarNode> found = this.nodes.stream().filter(n -> n.getId().equals(id)).findFirst();
        if (!found.isPresent()) {
            return null;
        }
        return found.get();
    }

    Optional<GuiEventListener> getNodeAt(double pMouseX, double pMouseY) {
        for (LodestarNode node : this.nodes) {
            if (!node.isMouseOver(pMouseX, pMouseY)) continue;
            return Optional.of(node);
        }
        return Optional.empty();
    }

    private List<LodestarNode> getNodesInGroup(LodestarGroup group) {
        ScreenRectangle groupRect = group.getRectangle();
        return this.nodes.stream().filter(n -> groupRect.overlaps(n.getRectangle())).collect(Collectors.toList());
    }

    private int getParameterX() {
        return this.leftPos + this.imageWidth + 5;
    }

    private int getParameterY(int index) {
        return this.topPos + 21 + index * 22;
    }

    @OnlyIn(value=Dist.CLIENT)
    class CommandScrollList
    extends ObjectSelectionList<CommandScrollList.Command> {
        private boolean _scrolling;
        private String _filter;
        private boolean low_tier;
        private int x0;
        private int y0;
        private int x1;
        private int y1;

        public CommandScrollList(boolean low_tier) {
            super(GuiLodestarV2.this.minecraft, 46, 135, GuiLodestarV2.this.topPos + 10, GuiLodestarV2.this.topPos + 153);
            this._scrolling = false;
            this._filter = "";
            this.setRenderHeader(false, 24);
            this.y0 = GuiLodestarV2.this.topPos + 10;
            this.y1 = GuiLodestarV2.this.topPos + 153;
            this.x0 = GuiLodestarV2.this.leftPos - 56 + 1;
            this.x1 = this.x0 + 46;
            this.low_tier = low_tier;
            this.setFilter("");
        }

        public void clear() {
            this.clearEntries();
        }

        private void addIconsForAll(Collection<ConstructTask> parts, Consumer<ConstructTask> clickHandler) {
            ArrayList<ConstructTask> segment = new ArrayList<ConstructTask>();
            for (ConstructTask part : parts) {
                segment.add(part);
                if (segment.size() != 2) continue;
                this.addEntry(new Command(segment, clickHandler, this.y0, this.y1));
                segment.clear();
            }
            if (segment.size() > 0) {
                this.addEntry(new Command(segment, clickHandler, this.y0, this.y1));
            }
        }

        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            int scrollBarStartX = this.getScrollbarPosition();
            pGuiGraphics.enableScissor(this.x0, this.y0, this.x1, this.y1);
            this.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);
            pGuiGraphics.disableScissor();
            if (this.getMaxScroll() > 0) {
                int scrollBarTop = this.y0 + 2;
                int scrollBarBottom = this.y1;
                int top = (int)this.getScrollAmount() * (scrollBarBottom - scrollBarTop) / this.getMaxScroll() + this.y0;
                if (top < scrollBarTop) {
                    top = scrollBarTop;
                }
                pGuiGraphics.blit(GuiLodestarV2.this.getExtensionTexture(), scrollBarStartX, top, 250.0f, 244.0f, 6, 6, 256, 256);
            }
        }

        @Nullable
        protected final Command getEntryAtPos(double mouseX, double mouseY) {
            int lowerXBound = this.getRowLeft();
            int upperXBound = lowerXBound + this.getRowWidth();
            int adjustedY = Mth.floor((double)(mouseY - (double)this.y0)) + (int)this.getScrollAmount();
            int index = adjustedY / this.itemHeight;
            if (index >= 0 && adjustedY >= 0 && index < this.getItemCount() && mouseX < (double)this.getScrollbarPosition() && mouseX >= (double)lowerXBound && mouseX <= (double)upperXBound) {
                return (Command)(this.children().get(index));
            }
            return null;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double delta, double p_scrollX) {
            this.setScrollAmount(this.getScrollAmount() - delta * (double)this.itemHeight);
            return true;
        }

        public void setFilter(String filter) {
            this._filter = filter.trim().toLowerCase();
            this.clear();
            this.addIconsForAll(Registries.ConstructTasks.getValues().stream().filter(c -> this._filter == null || this._filter == "" ? true : Component.translatable((String)("construct." + Registries.ConstructTasks.getKey(c).toString())).getString().toLowerCase().contains(this._filter)).filter(c -> c.isLodestarAssignable() && (c.isLowTierAssignable() || !this.low_tier)).collect(Collectors.toList()), action -> GuiLodestarV2.this.startDraggingCommandFromList((ConstructTask)action));
        }

        public int getRowLeft() {
            return this.x0 + 2;
        }

        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 6;
        }

        protected int getRowTop(int p_getRowTop_1_) {
            return this.y0 - (int)this.getScrollAmount() + p_getRowTop_1_ * this.itemHeight - 2;
        }

        public int getRowWidth() {
            return 36;
        }

        protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
            super.updateScrollingState(p_updateScrollingState_1_, p_updateScrollingState_3_, p_updateScrollingState_5_);
            this._scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double)this.getScrollbarPosition() && p_updateScrollingState_1_ < (double)(this.getScrollbarPosition() + 6);
        }

        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
            if (!this.isMouseOver(p_mouseClicked_1_, p_mouseClicked_3_)) {
                return false;
            }
            Command e = this.getEntryAtPos(p_mouseClicked_1_, p_mouseClicked_3_);
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
            return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 + this.itemHeight / 2)) + 6;
        }

        @OnlyIn(value=Dist.CLIENT)
        public class Command
        extends ObjectSelectionList.Entry<Command> {
            private Collection<ConstructTask> parts;
            private int spacing = 18;
            private ConstructTask _hoveredComponent;
            private Consumer<ConstructTask> _clickHandler;
            private final int y0;
            private final int y1;

            public Command(Collection<ConstructTask> parts, Consumer<ConstructTask> clickHandler, int y0, int y1) {
                this.parts = new ArrayList<ConstructTask>(parts);
                this._hoveredComponent = null;
                this._clickHandler = clickHandler;
                this.y0 = y0;
                this.y1 = y1;
            }

            public boolean isMouseOver(double x, double y) {
                if (y >= (double)this.y0 && y <= (double)this.y1) {
                    return super.isMouseOver(x, y);
                }
                return false;
            }

            public void render(GuiGraphics pGuiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float p_render_9_) {
                int i = 0;
                for (ConstructTask action : this.parts) {
                    if (action == null) continue;
                    int x = 6 + left + i++ * this.spacing;
                    int y = top + 4;
                    pGuiGraphics.blit(action.getIconTexture(), x, y, 0, 0.0f, 0.0f, 16, 16, 16, 16);
                    if (!this.isMouseOver(mouseX, mouseY) || mouseX < x || mouseX > x + this.spacing) continue;
                    GuiLodestarV2.this.currentTooltip = action.getAIClass() == null ? Component.translatable((String)"gui.mna.command.coming-soon").withStyle(ChatFormatting.GOLD) : Component.translatable((String)("construct." + Registries.ConstructTasks.getKey(action).toString()));
                    this._hoveredComponent = action;
                }
            }

            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                if (this._clickHandler != null && this._hoveredComponent != null) {
                    this._clickHandler.accept(this._hoveredComponent);
                }
                return true;
            }

            public Component getNarration() {
                return Component.translatable((String)"gui.mna.lodestar.component");
            }
        }
    }
}


