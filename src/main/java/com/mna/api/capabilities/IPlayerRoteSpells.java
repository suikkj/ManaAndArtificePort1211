/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 */
package com.mna.api.capabilities;

import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface IPlayerRoteSpells {
    public boolean isRote(ISpellComponent var1);

    public boolean addRoteXP(@Nullable Player var1, ISpellComponent var2);

    public boolean addRoteXP(@Nullable Player var1, ISpellComponent var2, float var3);

    public float getMastery(ISpellComponent var1);

    public boolean addMastery(@Nullable Player var1, ISpellComponent var2, float var3);

    public void setMastery(ResourceLocation var1, float var2);

    public List<Shape> getRoteShapes();

    public List<SpellEffect> getRoteComponents();

    public List<Modifier> getRoteModifiers();

    public HashMap<ISpellComponent, Float> getRoteData();

    public HashMap<ISpellComponent, Float> getMasteryData();

    public boolean isDirty();

    public void setDirty();

    public void clearDirty();

    public void copyFrom(IPlayerRoteSpells var1);

    public void setRoteXP(ResourceLocation var1, float var2);

    public float getRoteProgression(ISpellComponent var1);

    public void resetRote();

    public void resetMastery();
}

