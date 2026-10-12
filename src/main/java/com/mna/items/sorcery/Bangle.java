 
package com.mna.items.sorcery;

import java.util.function.Supplier;
import com.mna.items.renderers.ItemSpellRenderer;
import com.mna.items.sorcery.MagicStaff;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import com.mna.api.tools.MNADyeableItem;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public class Bangle
extends MagicStaff
implements MNADyeableItem {
    public Bangle(float attackDamage) {
        super(attackDamage);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new ItemSpellRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }
}


