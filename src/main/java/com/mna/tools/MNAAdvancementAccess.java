package com.mna.tools;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;

/**
 * [PORT 1.21.1] ClientAdvancements.progress virou private. Acesso via reflexao.
 */
public class MNAAdvancementAccess {

    @SuppressWarnings("unchecked")
    public static AdvancementProgress getProgress(ClientAdvancements ca, AdvancementHolder holder) {
        try {
            java.lang.reflect.Field f = ClientAdvancements.class.getDeclaredField("progress");
            f.setAccessible(true);
            java.util.Map<AdvancementHolder, AdvancementProgress> map =
                (java.util.Map<AdvancementHolder, AdvancementProgress>) f.get(ca);
            return map == null ? null : map.get(holder);
        } catch (Throwable t) {
            return null;
        }
    }
}
