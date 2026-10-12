/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.legacy.lucent.api.EntityBrightness
 *  com.legacy.lucent.api.plugin.ILucentPlugin
 *  com.legacy.lucent.api.plugin.LucentPlugin
 *  com.legacy.lucent.api.registry.EntityLightSourcePosRegistry
 *  com.legacy.lucent.api.registry.EntityLightingRegistry
 *  com.legacy.lucent.api.registry.ItemLightingRegistry
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 */
package com.mna.interop.lucent;

import com.legacy.lucent.api.EntityBrightness;
import com.legacy.lucent.api.plugin.ILucentPlugin;
import com.legacy.lucent.api.plugin.LucentPlugin;
import com.legacy.lucent.api.registry.EntityLightSourcePosRegistry;
import com.legacy.lucent.api.registry.EntityLightingRegistry;
import com.legacy.lucent.api.registry.ItemLightingRegistry;
import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import com.mna.tools.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

@LucentPlugin
public class MnALucentPlugin
implements ILucentPlugin {
    public String ownerModID() {
        return "mna";
    }

    public void registerEntityLightings(EntityLightingRegistry registry) {
        registry.register((EntityType)EntityInit.SPELL_PROJECTILE.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_BEAM.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_CONE.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_EMANATION.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_EMBER.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_FISSURE.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_FOCUS.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_WALL.get(), 13);
        registry.register((EntityType)EntityInit.SPELL_WAVE.get(), 13);
        registry.register((EntityType)EntityInit.SMITE_PROJECTILE.get(), 13);
        registry.register((EntityType)EntityInit.SENTRY_PROJECTILE.get(), 13);
        registry.register((EntityType)EntityInit.PORTAL_ENTITY.get(), 8);
        registry.register((EntityType)EntityInit.RIFT.get(), 8);
        registry.register((EntityType)EntityInit.DEMON_RETURN_PORTAL.get(), 8);
        registry.register((EntityType)EntityInit.BIFROST.get(), 15);
        registry.register((EntityType)EntityInit.FAERIE_QUEEN.get(), 10);
        registry.register((EntityType)EntityInit.FEY_LIGHT.get(), 15);
        registry.register((EntityType)EntityInit.LANTERN_WRAITH.get(), 10);
        registry.register((EntityType)EntityInit.LIVING_WARD.get(), 6);
        registry.register((EntityType)EntityInit.MANAWEAVE_ENTITY.get(), 8);
        registry.register((EntityType)EntityInit.PIXIE.get(), 6);
        registry.register((EntityType)EntityInit.PRESENTATION_ENTITY.get(), 12);
        registry.register((EntityType)EntityInit.STARBALL_ENTITY.get(), 15);
    }

    public void registerEntityLightSourcePositionGetter(EntityLightSourcePosRegistry registry) {
        registry.register((EntityType)EntityInit.BIFROST.get(), e -> e.position().subtract(0.0, 10.0, 0.0));
        registry.register((EntityType)EntityInit.SPELL_BEAM.get(), e -> e.getLastTickImpact());
    }

    public void registerItemLightings(ItemLightingRegistry registry) {
        registry.register((Item)ItemInit.SPELL.get(), 11);
        registry.register((Item)ItemInit.ROTE_BOOK.get(), 11);
        registry.register((Item)ItemInit.BOUND_AXE.get(), 11);
        registry.register((Item)ItemInit.BOUND_SWORD.get(), 11);
        registry.register((Item)ItemInit.BOUND_BOW.get(), 11);
        registry.register((Item)ItemInit.BOUND_SHIELD.get(), 11);
        registry.register((Item)ItemInit.HELLFIRE_STAFF.get(), 11);
        registry.register((Item)ItemInit.PUNKIN_STAFF.get(), 11);
        registry.register((Item)ItemInit.SPELL_BOOK.get(), 11);
        registry.register((Item)ItemInit.GRIMOIRE.get(), 11);
        registry.register((Item)ItemInit.STAFF_AMETHYST.get(), 11);
        registry.register((Item)ItemInit.STAFF_AUM.get(), 11);
        registry.register((Item)ItemInit.STAFF_CERUBLOSSOM.get(), 11);
        registry.register((Item)ItemInit.STAFF_CHIMERITE.get(), 11);
        registry.register((Item)ItemInit.STAFF_DESERTNOVA.get(), 11);
        registry.register((Item)ItemInit.STAFF_EMERALD.get(), 11);
        registry.register((Item)ItemInit.STAFF_GLASS.get(), 11);
        registry.register((Item)ItemInit.STAFF_GOLD.get(), 11);
        registry.register((Item)ItemInit.STAFF_IRON.get(), 11);
        registry.register((Item)ItemInit.STAFF_LAPIS.get(), 11);
        registry.register((Item)ItemInit.STAFF_NETHERQUARTZ.get(), 11);
        registry.register((Item)ItemInit.STAFF_PHYLACTERY.get(), 11);
        registry.register((Item)ItemInit.STAFF_PRISMARINECRYSTAL.get(), 11);
        registry.register((Item)ItemInit.STAFF_PRISMARINESHARD.get(), 11);
        registry.register((Item)ItemInit.STAFF_REDSTONE.get(), 11);
        registry.register((Item)ItemInit.STAFF_SKULL.get(), 11);
        registry.register((Item)ItemInit.STAFF_SKULL_ALT.get(), 11);
        registry.register((Item)ItemInit.STAFF_TARMA.get(), 11);
        registry.register((Item)ItemInit.STAFF_WAKEBLOOM.get(), 11);
        registry.register((Item)ItemInit.STAFF_VINTEUM.get(), 11);
        registry.register((Item)ItemInit.WAND_AMETHYST.get(), 11);
        registry.register((Item)ItemInit.WAND_AUM.get(), 11);
        registry.register((Item)ItemInit.WAND_CERUBLOSSOM.get(), 11);
        registry.register((Item)ItemInit.WAND_CHIMERITE.get(), 11);
        registry.register((Item)ItemInit.WAND_DESERTNOVA.get(), 11);
        registry.register((Item)ItemInit.WAND_EMERALD.get(), 11);
        registry.register((Item)ItemInit.WAND_GLASS.get(), 11);
        registry.register((Item)ItemInit.WAND_GOLD.get(), 11);
        registry.register((Item)ItemInit.WAND_IRON.get(), 11);
        registry.register((Item)ItemInit.WAND_LAPIS.get(), 11);
        registry.register((Item)ItemInit.WAND_NETHERQUARTZ.get(), 11);
        registry.register((Item)ItemInit.WAND_PRISMARINECRYSTAL.get(), 11);
        registry.register((Item)ItemInit.WAND_PRISMARINESHARD.get(), 11);
        registry.register((Item)ItemInit.WAND_REDSTONE.get(), 11);
        registry.register((Item)ItemInit.WAND_SKULL.get(), 11);
        registry.register((Item)ItemInit.WAND_SKULL_ALT.get(), 11);
        registry.register((Item)ItemInit.WAND_TARMA.get(), 11);
        registry.register((Item)ItemInit.WAND_WAKEBLOOM.get(), 11);
        registry.register((Item)ItemInit.WAND_VINTEUM.get(), 11);
        registry.register((Item)ItemInit.CHIMERITE_GEM.get(), 8);
        registry.register((Item)ItemInit.VINTEUM_INGOT_SUPERHEATED.get(), 8);
        registry.register((Item)ItemInit.VINTEUM_INGOT_PURIFIED_SUPERHEATED.get(), 10);
        registry.register((Item)ItemInit.MOTE_AIR.get(), 8);
        registry.register((Item)ItemInit.MOTE_WATER.get(), 8);
        registry.register((Item)ItemInit.MOTE_EARTH.get(), 8);
        registry.register((Item)ItemInit.MOTE_ENDER.get(), 8);
        registry.register((Item)ItemInit.MOTE_ARCANE.get(), 8);
        registry.register((Item)ItemInit.MOTE_FIRE.get(), 8);
        registry.register((Item)ItemInit.GREATER_MOTE_AIR.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_WATER.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_EARTH.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_ENDER.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_ARCANE.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_FIRE.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_HELLFIRE.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_LIGHTNING.get(), 12);
        registry.register((Item)ItemInit.GREATER_MOTE_ICE.get(), 12);
        registry.register((Item)ItemInit.CRYSTAL_PHYLACTERY.get(), 8);
        registry.register((Item)ItemInit.CRYSTAL_OF_MEMORIES.get(), 8);
        registry.register((Item)ItemInit.ELDRITCH_ORB.get(), 10);
        registry.register((Item)ItemInit.EMBERGLOW_BRACELET.get(), 8);
        registry.register((Item)ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get(), 8);
        registry.register((Item)ItemInit.LIVING_FLAME.get(), 10);
        registry.register((Item)ItemInit.MAJOR_MANA_GEM.get(), 14);
        registry.register((Item)ItemInit.MINOR_MANA_GEM.get(), 10);
        registry.register((Item)ItemInit.MANA_CRYSTAL_FRAGMENT.get(), 8);
        registry.register((Item)ItemInit.RESONATING_LUMP.get(), 8);
        registry.register((Item)ItemInit.RESONATING_DUST.get(), 8);
        registry.register((Item)ItemInit.DEMON_LORD_AXE.get(), 12);
        registry.register((Item)ItemInit.DEMON_LORD_SWORD.get(), 12);
    }

    public void getEntityLightLevel(EntityBrightness entityBrightness) {
        Entity entity = entityBrightness.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (EntityUtil.shouldBeInvisible((LivingEntity)player)) {
                entityBrightness.setLightLevel(0);
            }
        } else {
            super.getEntityLightLevel(entityBrightness);
        }
    }
}

