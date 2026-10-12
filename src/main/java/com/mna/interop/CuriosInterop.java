 
package com.mna.interop;

import com.mna.interop.BookRenderer;
import com.mna.interop.CurioRenderer;
import com.mna.interop.HatRenderer;
import com.mna.interop.StaffRenderer;
import com.mna.interop.WandRenderer;
import com.mna.items.ItemInit;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class CuriosInterop {
    @SubscribeEvent
    public static void clientCurioSetup(FMLClientSetupEvent evt) {
        CuriosRendererRegistry.register((Item)((Item)ItemInit.FLUID_JUG.get()), () -> new CurioRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.FLUID_JUG_INFINITE_LAVA.get()), () -> new CurioRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.FLUID_JUG_INFINITE_WATER.get()), () -> new CurioRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_PHYLACTERY.get()), () -> new CurioRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_AMETHYST.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_AUM.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_BLUEICE.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.BONE_SCYTHE.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_CERUBLOSSOM.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_CHIMERITE.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_DESERTNOVA.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_EMERALD.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_GLASS.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_GOLD.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_ICE.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_IRON.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_LAPIS.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_NETHERQUARTZ.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_PRISMARINECRYSTAL.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_PRISMARINESHARD.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_REDSTONE.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_SKULL.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_SKULL_ALT.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_TARMA.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_VINTEUM.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_WAKEBLOOM.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.STAFF_FOX.get()), () -> new StaffRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_AMETHYST.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_AUM.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_BLUEICE.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_CERUBLOSSOM.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_CHIMERITE.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_DESERTNOVA.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_EMERALD.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_GLASS.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_GOLD.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_ICE.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_IRON.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_LAPIS.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_NETHERQUARTZ.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_PRISMARINECRYSTAL.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_PRISMARINESHARD.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_REDSTONE.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_SKULL.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_SKULL_ALT.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_TARMA.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_VINTEUM.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.WAND_WAKEBLOOM.get()), () -> new WandRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.SPELL_BOOK.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.GRIMOIRE.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.GRIMOIRE_COUNCIL.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.GRIMOIRE_DEMON.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.GRIMOIRE_FEY.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.GRIMOIRE_UNDEAD.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.ROTE_BOOK.get()), () -> new BookRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.HAT_FEZ.get()), () -> new HatRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.HAT_LARGE_WITCH.get()), () -> new HatRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.HAT_STYLED_SKULL.get()), () -> new HatRenderer());
        CuriosRendererRegistry.register((Item)((Item)ItemInit.HAT_TALL_COWBOY.get()), () -> new HatRenderer());
    }

    public static boolean IsItemInCurioSlot(Item item, LivingEntity entity, SlotTypePreset slot) {
        MutableBoolean exists = new MutableBoolean(false);
        CuriosApi.getCuriosInventory((LivingEntity)entity).ifPresent(handler -> handler.findFirstCurio(item).ifPresent(slotResult -> exists.setValue(true)));
        return exists.booleanValue();
    }

    public static Optional<SlotResult> GetSingleItem(LivingEntity entity, Item ... items) {
        Optional output = Optional.empty();
        var curioInventory = CuriosApi.getCuriosInventory((LivingEntity)entity);
        if (curioInventory.isPresent()) {
            for (Item item : items) {
                Optional slotRes = ((ICuriosItemHandler)curioInventory.get()).findFirstCurio(item);
                if (!slotRes.isPresent()) continue;
                output = slotRes;
                break;
            }
        }
        return output;
    }

    public static Optional<SlotResult> GetSingleItem(LivingEntity entity, Class<? extends Item> clazz) {
        Optional slotRes;
        Optional output = Optional.empty();
        var curioInventory = CuriosApi.getCuriosInventory((LivingEntity)entity);
        if (curioInventory.isPresent() && (slotRes = ((ICuriosItemHandler)curioInventory.get()).findFirstCurio(is -> clazz.isInstance(is.getItem()))).isPresent()) {
            output = slotRes;
        }
        return output;
    }

    public static ItemStack GetNthCurio(LivingEntity entity, String identifier, int n) {
        Optional slotResult;
        var inv = CuriosApi.getCuriosInventory((LivingEntity)entity);
        if (inv.isPresent() && (slotResult = ((ICuriosItemHandler)inv.get()).findCurio(identifier, n)).isPresent()) {
            return ((SlotResult)slotResult.get()).stack();
        }
        return ItemStack.EMPTY;
    }

    public static void DamageCurioInSlot(Item item, LivingEntity entity, SlotTypePreset slot, int damage) {
        CuriosApi.getCuriosInventory((LivingEntity)entity).ifPresent(handler -> handler.findFirstCurio(item).ifPresent(slotResult -> {
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                slotResult.stack().hurtAndBreak(damage, serverLevel, entity, damager -> CuriosApi.broadcastCurioBreakEvent((SlotContext)slotResult.slotContext()));
            } else {
                slotResult.stack().hurtAndBreak(damage, entity, entity.getEquipmentSlotForItem(slotResult.stack()));
            }
        }));
    }

    public static ItemStack getCurioFromSlot(LivingEntity entity, SlotContext curioSlotContext) {
        Optional slotResult;
        var inv = CuriosApi.getCuriosInventory((LivingEntity)entity);
        if (inv.isPresent() && (slotResult = ((ICuriosItemHandler)inv.get()).findCurio(curioSlotContext.identifier(), curioSlotContext.index())).isPresent()) {
            return ((SlotResult)slotResult.get()).stack();
        }
        return ItemStack.EMPTY;
    }
}


