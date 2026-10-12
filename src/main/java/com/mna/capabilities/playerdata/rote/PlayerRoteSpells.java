/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 */
package com.mna.capabilities.playerdata.rote;

import com.mna.Registries;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.events.MasteryGainedEvent;
import com.mna.api.events.RoteProgressGainedEvent;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.tools.math.MathUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public class PlayerRoteSpells
implements IPlayerRoteSpells {
    public static final float MAX_MASTERY = 0.5f;
    private HashMap<ISpellComponent, Float> _roteParts = new HashMap();
    private HashMap<ISpellComponent, Float> _mastery = new HashMap();
    private boolean isDirty = true;

    @Override
    public boolean isRote(ISpellComponent part) {
        if (!this._roteParts.containsKey(part)) {
            return false;
        }
        return this._roteParts.get(part).floatValue() >= (float)part.requiredXPForRote();
    }

    @Override
    public boolean addRoteXP(Player player, ISpellComponent part) {
        return this.addRoteXP(player, part, 1.0f);
    }

    @Override
    public boolean addRoteXP(Player player, ISpellComponent part, float xp) {
        if (this.isRote(part) || part == null) {
            return false;
        }
        if (player != null) {
            RoteProgressGainedEvent evt = new RoteProgressGainedEvent(player, part, xp);
            if (NeoForge.EVENT_BUS.post((Event)evt)) {
                return false;
            }
            xp = evt.getAmount();
        }
        if (!this._roteParts.containsKey(part)) {
            this._roteParts.put(part, Float.valueOf(xp));
        } else {
            this._roteParts.put(part, Float.valueOf(Math.max(this._roteParts.get(part).floatValue() + xp, 0.0f)));
        }
        this.setDirty();
        return this.isRote(part);
    }

    @Override
    public void setRoteXP(ResourceLocation rLoc, float xp) {
        ISpellComponent comp = this.lookupSpellComponent(rLoc);
        if (comp != null) {
            this._roteParts.put(comp, Float.valueOf(xp));
        }
    }

    @Override
    public void resetRote() {
        this._roteParts.clear();
        this.setDirty();
    }

    @Override
    public void resetMastery() {
        this._mastery.clear();
        this.setDirty();
    }

    private ISpellComponent lookupSpellComponent(ResourceLocation rLoc) {
        if (rLoc == null) {
            return null;
        }
        if (Registries.Shape.containsKey(rLoc)) {
            return (ISpellComponent)Registries.Shape.getValue(rLoc);
        }
        if (Registries.SpellEffect.containsKey(rLoc)) {
            return (ISpellComponent)Registries.SpellEffect.getValue(rLoc);
        }
        if (Registries.Modifier.containsKey(rLoc)) {
            return (ISpellComponent)Registries.Modifier.getValue(rLoc);
        }
        return null;
    }

    @Override
    public List<Shape> getRoteShapes() {
        return this._roteParts.entrySet().stream().filter(e -> e.getKey() instanceof Shape && ((Float)e.getValue()).floatValue() >= (float)((ISpellComponent)e.getKey()).requiredXPForRote()).map(e -> (Shape)e.getKey()).collect(Collectors.toList());
    }

    @Override
    public List<SpellEffect> getRoteComponents() {
        return this._roteParts.entrySet().stream().filter(e -> e.getKey() instanceof SpellEffect && ((Float)e.getValue()).floatValue() >= (float)((ISpellComponent)e.getKey()).requiredXPForRote()).map(e -> (SpellEffect)e.getKey()).collect(Collectors.toList());
    }

    @Override
    public List<Modifier> getRoteModifiers() {
        return this._roteParts.entrySet().stream().filter(e -> e.getKey() instanceof Modifier && ((Float)e.getValue()).floatValue() >= (float)((ISpellComponent)e.getKey()).requiredXPForRote()).map(e -> (Modifier)e.getKey()).collect(Collectors.toList());
    }

    @Override
    public HashMap<ISpellComponent, Float> getRoteData() {
        return this._roteParts;
    }

    @Override
    public boolean isDirty() {
        return this.isDirty;
    }

    @Override
    public void setDirty() {
        this.isDirty = true;
    }

    @Override
    public void clearDirty() {
        this.isDirty = false;
    }

    @Override
    public void copyFrom(IPlayerRoteSpells other) {
        this._roteParts = new HashMap();
        for (Map.Entry<ISpellComponent, Float> p : other.getRoteData().entrySet()) {
            this._roteParts.put(p.getKey(), p.getValue());
        }
        this._mastery = new HashMap();
        for (Map.Entry<ISpellComponent, Float> p : other.getMasteryData().entrySet()) {
            this._mastery.put(p.getKey(), p.getValue());
        }
        this.setDirty();
    }

    @Override
    public float getRoteProgression(ISpellComponent spellPart) {
        if (!this._roteParts.containsKey(spellPart)) {
            return 0.0f;
        }
        return MathUtils.clamp01(this._roteParts.get(spellPart).floatValue() / (float)spellPart.requiredXPForRote());
    }

    @Override
    public float getMastery(ISpellComponent part) {
        return this._mastery.getOrDefault(part, Float.valueOf(0.0f)).floatValue();
    }

    @Override
    public boolean addMastery(Player player, ISpellComponent part, float amount) {
        float existing = this._mastery.getOrDefault(part, Float.valueOf(0.0f)).floatValue();
        float amt = existing + amount;
        if (player != null) {
            MasteryGainedEvent evt = new MasteryGainedEvent(player, part, amt);
            if (NeoForge.EVENT_BUS.post((Event)evt)) {
                return false;
            }
            amt = evt.getAmount();
        }
        existing = MathUtils.clamp(amt, 0.0f, 0.5f);
        this._mastery.put(part, Float.valueOf(existing));
        this.setDirty();
        return true;
    }

    @Override
    public HashMap<ISpellComponent, Float> getMasteryData() {
        return this._mastery;
    }

    @Override
    public void setMastery(ResourceLocation identifier, float amount) {
        ISpellComponent part = this.lookupSpellComponent(identifier);
        if (part != null) {
            this._mastery.put(part, Float.valueOf(MathUtils.clamp(amount, 0.0f, 0.5f)));
            this.setDirty();
        }
    }
}


