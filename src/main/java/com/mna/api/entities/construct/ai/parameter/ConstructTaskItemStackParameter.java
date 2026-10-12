 
package com.mna.api.entities.construct.ai.parameter;

import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructParameterTypes;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNAItemData;

public class ConstructTaskItemStackParameter
extends ConstructAITaskParameter {
    private ItemStack stack = ItemStack.EMPTY;

    public ConstructTaskItemStackParameter(String id) {
        super(id, ConstructParameterTypes.ITEMSTACK);
    }

    @Nullable
    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack.copy();
    }

    @Override
    public void loadData(CompoundTag nbt) {
        super.loadData(nbt);
        this.stack = ItemStack.parseOptional(this.level.registryAccess(), nbt);
    }

    @Override
    public CompoundTag saveData() {
        CompoundTag tag = super.saveData();
        MNAItemData.save(this.stack, tag);
        return tag;
    }
}

