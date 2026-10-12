/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.math.Axis
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.mna.recipes.manaweaving;
import net.minecraft.world.item.crafting.CraftingInput;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.items.ItemInit;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import com.mna.tools.manaweave.RecognitionEngine;
import com.mojang.math.Axis;
import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ManaweavingPattern
extends AMRecipeBase
implements IManaweavePattern {
    private byte[][] pattern;
    public static final int xBound = 11;
    public static final int yBound = 11;

    public ManaweavingPattern(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject jsonobject) {
        JsonArray arr = jsonobject.getAsJsonArray("pattern");
        this.pattern = new byte[arr.size()][];
        for (int i = 0; i < arr.size(); ++i) {
            JsonElement elem = arr.get(i);
            if (!elem.isJsonArray()) continue;
            JsonArray subArr = elem.getAsJsonArray();
            this.pattern[i] = new byte[subArr.size()];
            for (int j = 0; j < subArr.size(); ++j) {
                this.pattern[i][j] = subArr.get(j).getAsByte();
            }
        }
        this.initializePattern();
    }

    private void initializePattern() {
        if (this.pattern.length == 0) {
            ManaAndArtifice.LOGGER.error("Manaweaving pattern {0} has a length of 0 - this won't work right!", this.getId());
            return;
        }
        if (this.pattern.length != 11) {
            throw new MalformedParametersException("Manaweave Pattern Array Bounds must be 11x11");
        }
        for (int i = 1; i < this.pattern.length; ++i) {
            if (this.pattern[i].length == 11) continue;
            throw new MalformedParametersException("Manaweave Pattern Array Bounds must be 11x11");
        }
        RecognitionEngine.instance.registerTrainingDataSample(this.getId(), this.pattern);
    }

    @Override
    public byte[][] get() {
        return this.pattern;
    }

    public void setPatternBytes(byte[][] value) {
        this.pattern = value;
        this.initializePattern();
    }

    private static final byte[][] blank() {
        byte[][] comp = new byte[11][];
        for (int i = 0; i < 11; ++i) {
            comp[i] = new byte[11];
        }
        return comp;
    }

    public static final ManaweavingPattern match(Level world, int tier, Vector3f[] points, Vector3f[] directions) {
        int j;
        int i;
        Vector3f[] newPoints;
        if (points.length == 1) {
            return null;
        }
        if (points.length == 2) {
            newPoints = new Vector3f[3];
            newPoints[0] = points[0];
            newPoints[2] = points[1];
            newPoints[1] = new Vector3f((points[0].x() + points[1].x()) / 2.0f, (points[0].y() + points[1].y()) / 2.0f, (points[0].z() + points[1].z()) / 2.0f);
            points = newPoints;
        }
        while (points.length < 100) {
            int next;
            java.util.List<Vector3f> newPointsList = new java.util.ArrayList<Vector3f>();
            for (i = 0; i < points.length && (next = i + 1) != points.length; ++i) {
                newPointsList.add(points[i]);
                newPointsList.add(new Vector3f((points[i].x() + points[next].x()) / 2.0f, (points[i].y() + points[next].y()) / 2.0f, (points[i].z() + points[next].z()) / 2.0f));
            }
            points = newPointsList.toArray(new Vector3f[0]);
        }
        byte[][] transformedPattern = ManaweavingPattern.normalizePoints(world, points, directions);
        if (ManaAndArtifice.instance.isDebug) {
            ManaAndArtifice.LOGGER.debug("Pattern From Player Drawing:");
            for (i = 0; i < 11; ++i) {
                Object str = "[ ";
                for (j = 0; j < 11; ++j) {
                    str = (String)str + (transformedPattern[i][j] == 0 ? ". " : "O ");
                }
                str = (String)str + "]";
                ManaAndArtifice.LOGGER.debug((String)str);
            }
        }
        boolean[][] bwPattern = new boolean[transformedPattern.length][transformedPattern[0].length];
        for (int i2 = 0; i2 < 11; ++i2) {
            for (j = 0; j < 11; ++j) {
                bwPattern[i2][j] = transformedPattern[i2][j] != 0;
            }
        }
        ResourceLocation matched = RecognitionEngine.instance.recognize(bwPattern);
        if (matched == null) {
            return null;
        }
        Optional recipe = world.getRecipeManager().byKey(matched);
        if (recipe != null && recipe.isPresent() && recipe.get() instanceof ManaweavingPattern) {
            return (ManaweavingPattern)recipe.get();
        }
        return null;
    }

    private static byte[][] normalizePoints(Level world, Vector3f[] points, Vector3f[] directions) {
        Vector3f[] __points = new Vector3f[points.length];
        for (int i = 0; i < points.length; ++i) {
            __points[i] = new Vector3f(points[i].x, points[i].y, points[i].z);
        }
        Vector3f min = new Vector3f(2.1474836E9f, 2.1474836E9f, 2.1474836E9f);
        Vector3f max = new Vector3f(-2.1474836E9f, -2.1474836E9f, -2.1474836E9f);
        ManaweavingPattern.projectPointsAlongZ(__points, directions, min, max);
        Vector3f delta = new Vector3f(max.x(), max.y(), max.z());
        delta.sub((Vector3fc)min);
        Vector3f centerPoint = new Vector3f(0.0f, 0.0f, 0.0f);
        for (int i = 0; i < __points.length; ++i) {
            centerPoint.add(__points[i].x(), __points[i].y(), 0.0f);
            __points[i].x = (__points[i].x() - min.x()) / delta.x();
            __points[i].y = (__points[i].y() - min.y()) / delta.y();
        }
        centerPoint.mul(1.0f / (float)__points.length);
        byte[][] comp = ManaweavingPattern.blank();
        for (int i = 0; i < __points.length; ++i) {
            int x = (int)Math.floor(__points[i].x() * 11.0f);
            int y = (int)Math.floor(__points[i].y() * 11.0f);
            if (x >= 11 || x < 0 || y >= 11 || y < 0) continue;
            comp[x][y] = 1;
        }
        int M = comp.length;
        int N = comp[0].length;
        byte[][] ret = new byte[N][M];
        for (int r = 0; r < M; ++r) {
            for (int c = 0; c < N; ++c) {
                ret[r][c] = comp[r][M - 1 - c];
            }
        }
        return ret;
    }

    private static void projectPointsAlongZ(Vector3f[] points, Vector3f[] directions, Vector3f out_min, Vector3f out_max) {
        Vector3f c = new Vector3f(0.0f, 0.0f, 0.0f);
        for (int i = 0; i < points.length; ++i) {
            c.add(points[i].x(), points[i].y(), points[i].z());
        }
        c.mul(1.0f / (float)points.length);
        Vector3f d = directions[0];
        float yaw = d.x();
        float pitch = d.y();
        for (int i = 0; i < points.length; ++i) {
            points[i].sub((Vector3fc)c);
            points[i].rotate((Quaternionfc)Axis.YP.rotationDegrees(yaw));
            points[i].rotate((Quaternionfc)Axis.XP.rotationDegrees(pitch));
            points[i].add((Vector3fc)c);
            points[i].z = c.z();
            out_min.x = Math.min(out_min.x(), points[i].x());
            out_min.y = Math.min(out_min.y(), points[i].y());
            out_min.z = Math.min(out_min.z(), points[i].z());
            out_max.x = Math.max(out_max.x(), points[i].x());
            out_max.y = Math.max(out_max.y(), points[i].y());
            out_max.z = Math.max(out_max.z(), points[i].z());
        }
    }

    public boolean matches(net.minecraft.world.item.crafting.CraftingInput inv, Level worldIn) {
        return true;
    }

    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput inv, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.MANAWEAVING_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.MANAWEAVING_PATTERN_TYPE.get();
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        ItemStack stack = new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND.get());
        stack.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)this.getId().toString()));
        return stack;
    }

    @Override
    public ManaweavingPattern copy() {
        ManaweavingPattern clone = new ManaweavingPattern(this.getId());
        clone.setPatternBytes(this.pattern);
        return clone;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}

