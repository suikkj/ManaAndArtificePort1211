 
package com.mna.spells.crafting;

import com.mna.MnaRegistry;
import com.google.common.collect.ImmutableList;
import com.mna.ManaAndArtifice;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiable;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class ModifiedSpellPart<T extends ISpellComponent & IModifiable<T>>
implements IModifiedSpellPart<T> {
    private final T part;
    private ImmutableList<AttributeValuePair> attributeValues;

    public ModifiedSpellPart(T part) {
        this.part = part;
        this.attributeValues = ImmutableList.copyOf(((IModifiable)part).getModifiableAttributes());
    }

    @Override
    public final T getPart() {
        return this.part;
    }

    @Override
    public final float getMaximumValue(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getMaximum();
        }
        return 0.0f;
    }

    @Override
    public final float getMinimumValue(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getMinimum();
        }
        return 0.0f;
    }

    @Override
    public final float getValue(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getValue();
        }
        return 0.0f;
    }

    @Override
    public final float getValueWithoutMultipliers(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getValueWithoutMultipliers();
        }
        return 0.0f;
    }

    @Override
    public final float getMultiplier(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getMultiplier();
        }
        return 1.0f;
    }

    @Override
    public final boolean setValue(Attribute attribute, float value) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute || pair.getValue() == value) continue;
            pair.setValue(value);
            return true;
        }
        return false;
    }

    @Override
    public final boolean setMultiplier(Attribute attribute, float value) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute || pair.getValue() == value) continue;
            pair.setMultiplier(value);
            return true;
        }
        return false;
    }

    @Override
    public final boolean resetMultiplier(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            pair.setMultiplier(1.0f);
            return true;
        }
        return false;
    }

    @Override
    public final float getDefaultValue(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getDefaultValue();
        }
        return 0.0f;
    }

    @Override
    public final float getStep(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getStep();
        }
        return 1.0f;
    }

    @Override
    public final float getStepComplexity(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.getStepComplexity();
        }
        return 1.0f;
    }

    @Override
    public final void resetValueToDefault(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            pair.setValue(pair.getDefaultValue());
        }
    }

    @Override
    public final float stepUp(Attribute attribute, float maxModifier) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.stepUp(maxModifier);
        }
        return 0.0f;
    }

    @Override
    public final float stepUpIgnoreMax(Attribute attribute) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.stepUpIgnoreMax();
        }
        return 0.0f;
    }

    @Override
    public final float stepDown(Attribute attribute, float maxModifier) {
        for (AttributeValuePair pair : this.attributeValues) {
            if (pair.getAttribute() != attribute) continue;
            return pair.stepDown(maxModifier);
        }
        return 0.0f;
    }

    @Override
    public final ImmutableList<Attribute> getContainedAttributes() {
        Attribute[] attrs = new Attribute[this.attributeValues.size()];
        for (int i = 0; i < this.attributeValues.size(); ++i) {
            attrs[i] = ((AttributeValuePair)this.attributeValues.get(i)).getAttribute();
        }
        return ImmutableList.copyOf((Object[])attrs);
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        if (this.part != null) {
            nbt.putString("resource_location", this.part.getRegistryName().toString());
            nbt.putInt("num_attributes", this.getContainedAttributes().size());
            int count = 0;
            for (Attribute attr : this.getContainedAttributes()) {
                float value = this.getValueWithoutMultipliers(attr);
                float mult = this.getMultiplier(attr);
                nbt.putString("attribute_" + count, attr.name());
                nbt.putFloat("value_" + count, value);
                nbt.putFloat("mult_" + count, mult);
                ++count;
            }
        } else {
            nbt.putString("resource_location", ManaAndArtifice.EMPTY.toString());
            nbt.putInt("num_attributes", 0);
        }
        return nbt;
    }

    @Override
    public boolean isSame(IModifiedSpellPart<T> other) {
        if (this.getPart() != other.getPart()) {
            return false;
        }
        ImmutableList<Attribute> myAttrs = this.getContainedAttributes();
        ImmutableList<Attribute> theirAttrs = other.getContainedAttributes();
        return myAttrs.size() == theirAttrs.size() && !myAttrs.stream().anyMatch(a -> !theirAttrs.contains(a) || this.getValue((Attribute)(a)) != other.getValue((Attribute)(a)));
    }

    public static <T extends IModifiable<T> & ISpellComponent> ModifiedSpellPart<T> fromNBT(CompoundTag nbt, MnaRegistry<T> registry) {
        if (!nbt.contains("resource_location")) {
            return null;
        }
        ResourceLocation rLoc = ResourceLocation.parse(nbt.getString("resource_location"));
        if (!registry.containsKey(rLoc)) {
            return null;
        }
        IModifiable inst = (IModifiable)registry.getValue(rLoc);
        ModifiedSpellPart<IModifiable> msp = new ModifiedSpellPart<IModifiable>(inst);
        if (!nbt.contains("num_attributes")) {
            return msp;
        }
        int attributes = nbt.getInt("num_attributes");
        for (int i = 0; i < attributes; ++i) {
            if (!nbt.contains("attribute_" + i) || !nbt.contains("value_" + i)) continue;
            try {
                Attribute attr = Attribute.valueOf(nbt.getString("attribute_" + i));
                float value = nbt.getFloat("value_" + i);
                float mult = nbt.getFloat("mult_" + i);
                msp.setValue(attr, value);
                msp.setMultiplier(attr, mult);
                continue;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                 
            }
        }
        return msp;
    }
}

