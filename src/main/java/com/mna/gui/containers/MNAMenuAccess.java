package com.mna.gui.containers;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * [PORT 1.21.1] Os campos de estado de quickcraft/listeners de {@link AbstractContainerMenu}
 * viraram {@code private} no 1.21.1. Esta classe expoe acesso via reflexao para o
 * LargeContainer do MNA (que herdava o acesso protegido no 1.20.1).
 */
public final class MNAMenuAccess {
    private static final Field QUICKCRAFT_TYPE = field("quickcraftType");
    private static final Field QUICKCRAFT_STATUS = field("quickcraftStatus");
    private static final Field QUICKCRAFT_SLOTS = field("quickcraftSlots");
    private static final Field LAST_SLOTS = field("lastSlots");
    private static final Field CONTAINER_LISTENERS = field("containerListeners");
    private static final Field DATA_SLOTS = field("dataSlots");

    private MNAMenuAccess() {
    }

    private static Field field(String name) {
        try {
            Field f = AbstractContainerMenu.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[MNA] campo nao encontrado: AbstractContainerMenu." + name, e);
        }
    }

    public static int quickcraftType(AbstractContainerMenu menu) {
        try {
            return (Integer)QUICKCRAFT_TYPE.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setQuickcraftType(AbstractContainerMenu menu, int value) {
        try {
            QUICKCRAFT_TYPE.set(menu, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int quickcraftStatus(AbstractContainerMenu menu) {
        try {
            return (Integer)QUICKCRAFT_STATUS.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setQuickcraftStatus(AbstractContainerMenu menu, int value) {
        try {
            QUICKCRAFT_STATUS.set(menu, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<Slot> quickcraftSlots(AbstractContainerMenu menu) {
        try {
            return (Set<Slot>)QUICKCRAFT_SLOTS.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static NonNullList<ItemStack> lastSlots(AbstractContainerMenu menu) {
        try {
            return (NonNullList<ItemStack>)LAST_SLOTS.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ContainerListener> containerListeners(AbstractContainerMenu menu) {
        try {
            return (List<ContainerListener>)CONTAINER_LISTENERS.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<DataSlot> dataSlots(AbstractContainerMenu menu) {
        try {
            return (List<DataSlot>)DATA_SLOTS.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
