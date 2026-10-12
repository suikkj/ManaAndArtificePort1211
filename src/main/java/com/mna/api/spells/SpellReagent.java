 
package com.mna.api.spells;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.parts.SpellEffect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class SpellReagent {
    private final ItemStack reagentStack;
    private final boolean compareTag;
    private final boolean ignoreDurability;
    private final boolean optional;
    private boolean consume;
    private final SpellEffect addedBy;
    private final List<IFaction> ignoredBy;

    public SpellReagent(SpellEffect addedBy, ItemStack reagentStack, boolean compareNBT, boolean ignoreDurability, boolean consume) {
        this(addedBy, reagentStack, compareNBT, ignoreDurability, consume, new IFaction[0]);
    }

    public SpellReagent(SpellEffect addedBy, ItemStack reagentStack, boolean compareNBT, boolean ignoreDurability, boolean consume, IFaction ... ignoredBy) {
        this(addedBy, reagentStack, compareNBT, ignoreDurability, consume, false, ignoredBy);
    }

    public SpellReagent(SpellEffect addedBy, ItemStack reagentStack, boolean compareNBT, boolean ignoreDurability, boolean consume, boolean optional, IFaction ... ignoredBy) {
        this.reagentStack = reagentStack;
        this.compareTag = compareNBT;
        this.consume = consume;
        this.optional = optional;
        this.ignoreDurability = ignoreDurability;
        this.addedBy = addedBy;
        this.ignoredBy = Arrays.asList(ignoredBy);
    }

    public ItemStack getReagentStack() {
        return this.reagentStack.copy();
    }

    public boolean getCompareNBT() {
        return this.compareTag;
    }

    public boolean getConsume() {
        return this.consume;
    }

    public boolean getOptional() {
        return this.optional;
    }

    public boolean getIgnoreDurability() {
        return this.ignoreDurability;
    }

    public SpellEffect getAddedBy() {
        return this.addedBy;
    }

    public boolean isIgnoredBy(Player caster) {
        if (this.ignoredBy.size() == 0 || caster == null) {
            return false;
        }
        MutableBoolean ignore = new MutableBoolean(false);
        MNACap.get(caster, ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> {
            if (p.getAlliedFaction() != null) {
                ArrayList<IFaction> cloned = new ArrayList<IFaction>(this.ignoredBy);
                cloned.retainAll(p.getAlliedFaction().getAlliedFactions());
                ignore.setValue(cloned.size() > 0);
            }
        });
        return ignore.booleanValue();
    }

    public boolean equals(Object obj) {
        if (obj instanceof SpellReagent) {
            SpellReagent other = (SpellReagent)obj;
            if (other.getReagentStack().getItem() != this.getReagentStack().getItem()) {
                return false;
            }
            return !this.compareTag && (!other.compareTag || ManaAndArtificeMod.getItemHelper().AreTagsEqual(this.reagentStack, other.reagentStack));
        }
        return false;
    }
}

