 
package com.mna.gui.widgets.lodestar;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.gui.GuiTextures;
import com.mna.gui.block.GuiLodestarV2;
import com.mna.gui.widgets.lodestar.LodestarNodeErrorTypes;
import com.mna.gui.widgets.lodestar.LodestarParameter;
import com.mna.tools.render.GuiRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import com.mna.gui.widgets.MNAImageButton;

public class LodestarNode
extends MNAImageButton {
    private final ConstructTask action;
    public static final int ICON_SIZE = 16;
    public static final int ICON_TEXTURE_SIZE = 16;
    public static final int NODE_SIZE = 8;
    public static final int MAIN_TEXTURE_SIZE = 256;
    public static final int DELETE_INDEX = 97;
    public static final int INPUT_INDEX = 98;
    public static final int DRAG_INDEX = 99;
    public static final int WIDGET_WIDTH = 48;
    public static final int WIDGET_HEIGHT = 40;
    public static final int COLOR_GREY = FastColor.ARGB32.color((int)255, (int)220, (int)220, (int)220);
    public static final int COLOR_BLUE = FastColor.ARGB32.color((int)255, (int)0, (int)0, (int)255);
    public static final int COLOR_WHITE = FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)255);
    public static final int COLOR_RED = FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)0);
    public static final int COLOR_GREEN = FastColor.ARGB32.color((int)255, (int)0, (int)150, (int)0);
    public static final int COLOR_YELLOW = FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)0);
    private int subClickIndex = -1;
    private HashMap<Integer, String> connections;
    Function<String, LodestarNode> nodeResolver;
    private ListTag parameterData;
    private String id;
    private boolean isStart = false;
    private double clickX;
    private double clickY;
    private double clickXOffset;
    private double clickYOffset;

    public LodestarNode(int x, int y, boolean lowTier, ConstructTask action, Button.OnPress pressHandler, Function<String, LodestarNode> resolver) {
        super(x, y, 48, 40, action.isCondition() ? 56 : 8, 171, 45, lowTier ? GuiTextures.Blocks.LODESTAR_LESSER_MAIN : GuiTextures.Blocks.LODESTAR_MAIN, 256, 256, pressHandler);
        this.action = action;
        this.connections = new HashMap();
        this.id = UUID.randomUUID().toString();
        this.parameterData = new ListTag();
        this.nodeResolver = resolver;
    }

    public void select() {
        this.setFocused(true);
    }

    public void deselect() {
        this.setFocused(false);
    }

    public ConstructTask getTask() {
        return this.action;
    }

    public String getId() {
        return this.id;
    }

    public boolean isStart() {
        return this.isStart;
    }

    public boolean isCondition() {
        return this.action.isCondition();
    }

    public void setStart(boolean start) {
        this.isStart = start;
    }

    public ListTag getParameterData() {
        return this.parameterData;
    }

    public void saveParameters(List<LodestarParameter<?>> parameterInputs) {
        List<ConstructAITaskParameter> params = this.getParameters();
        assert (params.size() == parameterInputs.size());
        this.parameterData.clear();
        for (int i = 0; i < params.size(); ++i) {
            ConstructAITaskParameter param = params.get(i);
            LodestarParameter<?> paramInput = parameterInputs.get(i);
            paramInput.saveTo(param);
            this.parameterData.add(param.saveData());
        }
    }

    public void loadParameters(List<LodestarParameter<?>> parameterInputs) {
        int i;
        List<ConstructAITaskParameter> params = this.getParameters();
        ListTag parameterData = this.getParameterData();
        assert (params.size() == parameterInputs.size());
        for (i = 0; i < params.size() && i < parameterData.size(); ++i) {
            String id;
            CompoundTag tag = parameterData.getCompound(i);
            if (tag == null || (id = tag.getString("parameter_id")) == null) continue;
            Optional<ConstructAITaskParameter> param = params.stream().filter(p -> p.getId().equals(id)).findFirst();
            if (param.isPresent()) {
                param.get().loadData(tag);
                continue;
            }
            ManaAndArtificeMod.LOGGER.warn("M&A >> construct task parameter " + id + " not found.  Skipping.");
        }
        for (i = 0; i < params.size(); ++i) {
            ConstructAITaskParameter param = params.get(i);
            LodestarParameter<?> paramInput = parameterInputs.get(i);
            paramInput.loadFrom(param);
        }
    }

    public CompoundTag toCompoundTag(int guiTop, int guiLeft) {
        CompoundTag tag = new CompoundTag();
        tag.put("parameters", (Tag)this.parameterData);
        ListTag connectionData = new ListTag();
        this.connections.entrySet().forEach(c -> {
            CompoundTag connection = new CompoundTag();
            connection.putInt("index", ((Integer)c.getKey()).intValue());
            connection.putString("target", (String)c.getValue());
            connectionData.add(connection);
        });
        tag.put("connections", (Tag)connectionData);
        tag.putInt("x", this.getX() - guiLeft);
        tag.putInt("y", this.getY() - guiTop);
        tag.putString("id", this.id);
        tag.putBoolean("start", this.isStart);
        tag.putString("task", Registries.ConstructTasks.getKey(this.action).toString());
        return tag;
    }

    @Nullable
    public static LodestarNode fromCompoundTag(CompoundTag tag, int guiTop, int guiLeft, boolean lowTier, Button.OnPress pressHandler, Function<String, LodestarNode> resolver) {
        if (!(tag.contains("x") && tag.contains("y") && tag.contains("task") && tag.contains("id"))) {
            return null;
        }
        int x = tag.getInt("x") + guiLeft;
        int y = tag.getInt("y") + guiTop;
        ResourceLocation taskID = ResourceLocation.parse(tag.getString("task"));
        ConstructTask action = (ConstructTask)Registries.ConstructTasks.getValue(taskID);
        if (action == null) {
            ManaAndArtifice.LOGGER.error("Failed to look up task (" + taskID.toString() + "); Node skipped.  This will likely break connections.");
            return null;
        }
        LodestarNode newNode = new LodestarNode(x, y, lowTier, action, pressHandler, resolver);
        if (tag.contains("parameters", 9)) {
            newNode.parameterData = tag.getList("parameters", 10);
        }
        if (tag.contains("connections")) {
            ListTag connections = tag.getList("connections", 10);
            connections.forEach(t -> {
                CompoundTag connection;
                if (t instanceof CompoundTag && (connection = (CompoundTag)t).contains("index") && connection.contains("target")) {
                    String id = connection.getString("target");
                    int index = connection.getInt("index");
                    newNode.connections.put(index, id);
                }
            });
        }
        newNode.id = tag.getString("id");
        newNode.isStart = tag.getBoolean("start");
        return newNode;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.isHovered) {
            this.renderDeleteButton(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        if (this.action != null) {
            this.renderInputNode(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            this.renderOutputNodeSuccess(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            for (int i = 1; i < this.action.getOutputs(); ++i) {
                this.renderOutputNodeFailure(pGuiGraphics, pMouseX, pMouseY, i - 1, pPartialTick);
            }
            pGuiGraphics.blit(this.action.getIconTexture(), this.getX() + 16, this.getY() + 15, 0.0f, 0.0f, 16, 16, 16, 16);
        }
    }

    public void renderErrors(GuiGraphics pGuiGraphics, HashMap<String, LodestarNodeErrorTypes> nodeErrors) {
        LodestarNodeErrorTypes err = nodeErrors.getOrDefault(this.id, LodestarNodeErrorTypes.None);
        switch (err) {
            case Disconnected: {
                pGuiGraphics.blit(this.resourceLocation, this.getX() + 2, this.getY() - 2, 12, 12, 154.0f, 208.0f, 12, 12, 256, 256);
            }
            case Misconfigured: {
                pGuiGraphics.blit(this.resourceLocation, this.getX() + 4, this.getY() + this.getHeight() - 8, 8, 8, 154.0f, 232.0f, 12, 12, 256, 256);
                break;
            }
            default: {
                return;
            }
        }
    }

    public void renderConnections(GuiGraphics pGuiGraphics, int x, int y, float partialTick) {
        if (this.isStart && this.getX() > x) {
            GuiRenderUtils.bezierLineBetween(pGuiGraphics, (float)(x + 4 + 4 + 3) / GuiLodestarV2.Zoom, ((float)(y + 10 + 4) + 0.5f) / GuiLodestarV2.Zoom, this.getX() + 8, (float)this.getY() + 14.5f, 50.0f, 3.0f, COLOR_BLUE, COLOR_GREY);
        }
        this.connections.forEach((i, n) -> {
            if (n != null) {
                this.renderConnectingLine(pGuiGraphics, (int)i, (String)n);
            }
        });
    }

    private void renderDeleteButton(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partialTick) {
        pGuiGraphics.blit(this.resourceLocation, this.getX() + 39, this.getY() + 1, 0.0f, 224.0f, 8, 8, 256, 256);
    }

    private void renderInputNode(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partialTick) {
        pGuiGraphics.blit(this.resourceLocation, this.getX() + 4, this.getY() + 10, 0.0f, 232.0f, 8, 8, 256, 256);
    }

    private void renderOutputNodeSuccess(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partialTick) {
        pGuiGraphics.blit(this.resourceLocation, this.getX() + 36, this.getY() + 10, 0.0f, 248.0f, 8, 8, 256, 256);
    }

    private void renderOutputNodeFailure(GuiGraphics pGuiGraphics, int x, int y, int index, float partialTick) {
        pGuiGraphics.blit(this.resourceLocation, this.getX() + 36, this.getY() + 18 + index * 8, 0.0f, 240.0f, 8, 8, 256, 256);
    }

    public void renderConnectingLine(GuiGraphics pGuiGraphics, int nodeIndex, String other) {
        LodestarNode otherResolved = this.nodeResolver.apply(other);
        if (otherResolved == null) {
            return;
        }
        float endX = otherResolved.getX() + 8;
        float endY = (float)otherResolved.getY() + 14.5f;
        if (nodeIndex == 0) {
            if (((otherResolved)).equals(this)) {
                this.renderSelfConnectingLine(pGuiGraphics, nodeIndex, COLOR_GREEN, COLOR_GREY);
            } else {
                this.renderConnectingLine(pGuiGraphics, nodeIndex, endX, endY, COLOR_GREEN, COLOR_GREY);
            }
        } else if (((otherResolved)).equals(this)) {
            this.renderSelfConnectingLine(pGuiGraphics, nodeIndex, COLOR_RED, COLOR_GREY);
        } else {
            this.renderConnectingLine(pGuiGraphics, nodeIndex, endX, endY, COLOR_RED, COLOR_GREY);
        }
    }

    public void renderConnectingLine(GuiGraphics pGuiGraphics, int nodeIndex, float endX, float endY, int color) {
        this.renderConnectingLine(pGuiGraphics, nodeIndex, endX, endY, color, color);
    }

    private void renderSelfConnectingLine(GuiGraphics pGuiGraphics, int nodeIndex, int startColor, int endColor) {
        float startX = this.getX() + 44;
        float startY = (float)this.getY() + 14.5f + (float)(nodeIndex * 8);
        float endX = this.getX() + 8;
        float endY = (float)this.getY() + 14.5f;
        float midX = (startX + endX) / 2.0f;
        float midY = this.getY() - 20;
        GuiRenderUtils.bezierLineBetween(pGuiGraphics, startX, startY, midX, midY, 0.0f, 3.0f, startColor, endColor, false);
        GuiRenderUtils.bezierLineBetween(pGuiGraphics, midX, midY, endX, endY, 0.0f, 3.0f, startColor, endColor, false);
    }

    public void renderConnectingLine(GuiGraphics pGuiGraphics, int nodeIndex, float endX, float endY, int startColor, int endColor) {
        float startX = this.getX() + 44;
        float startY = (float)this.getY() + 14.5f + (float)(nodeIndex * 8);
        GuiRenderUtils.bezierLineBetween(pGuiGraphics, startX, startY, endX, endY, 0.0f, 3.0f, startColor, endColor);
    }

    public void connectToNode(LodestarNode other, int index) {
        this.connections.put(index, other.getId());
    }

    public void disconnectFromNode(LodestarNode node) {
        ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();
        this.connections.forEach((i, c) -> {
            if (c.equals(node.getId())) {
                indicesToRemove.add((Integer)i);
            }
        });
        indicesToRemove.forEach(i -> this.connections.remove(i));
    }

    public int getSubWidgetAt(double mouse_x, double mouse_y) {
        if (this.mouseWithinWidgetCoords(mouse_x, mouse_y, 39, 1)) {
            return 97;
        }
        if (this.mouseWithinWidgetCoords(mouse_x, mouse_y, 4, 10)) {
            return 98;
        }
        for (int i = 0; i < this.action.getOutputs(); ++i) {
            if (!this.mouseWithinWidgetCoords(mouse_x, mouse_y, 36, 10 + i * 8)) continue;
            return i;
        }
        return 99;
    }

    public int getSubClickIndex() {
        return this.subClickIndex;
    }

    public void onClick(double mouse_x, double mouse_y) {
        this.clickX = mouse_x;
        this.clickY = mouse_y;
        this.clickXOffset = mouse_x - (double)this.getX();
        this.clickYOffset = mouse_y - (double)this.getY();
        this.subClickIndex = this.getSubWidgetAt(mouse_x, mouse_y);
        super.onClick(mouse_x, mouse_y);
    }

    private boolean mouseWithinWidgetCoords(double mouse_x, double mouse_y, int x, int y) {
        return mouse_x >= (double)(this.getX() + x) && mouse_x <= (double)(this.getX() + x + 8) && mouse_y >= (double)(this.getY() + y) && mouse_y <= (double)(this.getY() + y + 8);
    }

    public List<ConstructAITaskParameter> getParameters() {
        return this.action.getParameters();
    }

    public void playDownSound(SoundManager p_93665_) {
    }

    public double getClickX() {
        return this.clickX;
    }

    public double getClickY() {
        return this.clickY;
    }

    public double getClickXOffset() {
        return this.clickXOffset;
    }

    public double getClickYOffset() {
        return this.clickYOffset;
    }
}

