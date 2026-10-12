 
package com.mna.items.relic;

import java.util.function.Supplier;
import com.mna.api.items.IRelic;
import com.mna.items.ItemInit;
import com.mna.items.renderers.CrystallizedWindRenderer;
import com.mna.tools.InventoryUtilities;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;

public class CrystallizedWind
extends Item
implements IRelic {
    public CrystallizedWind() {
        super(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new CrystallizedWindRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.literal((String)"  "));
        tooltip.add((Component)Component.translatable((String)"item.mna.crystallized_wind.lore_1").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        tooltip.add((Component)Component.literal((String)"  "));
        tooltip.add((Component)Component.translatable((String)"item.mna.crystallized_wind.lore_2").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public static boolean FindOn(LivingEntity living) {
        MutableBoolean found = new MutableBoolean(false);
        CuriosApi.getCuriosInventory((LivingEntity)living).ifPresent(curios -> found.setValue(curios.findCurios((Item)ItemInit.CRYSTALLIZED_WIND.get()).size() > 0));
        if (!found.booleanValue() && living instanceof Player) {
            Player player = (Player)living;
            found.setValue(InventoryUtilities.hasStackInInventory(new ItemStack((ItemLike)ItemInit.CRYSTALLIZED_WIND.get()), false, false, (IItemHandler)new InvWrapper((Container)player.getInventory())));
        }
        return found.booleanValue();
    }
}


