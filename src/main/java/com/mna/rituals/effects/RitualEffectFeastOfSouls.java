 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.EntityCrystal;
import com.mna.tools.EntityUtil;
import com.mna.tools.SummonUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class RitualEffectFeastOfSouls
extends RitualEffect {
    public RitualEffectFeastOfSouls(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        if (context.getCaster() == null) {
            return Component.literal((String)"No player reference found for ritual, aborting.");
        }
        IPlayerProgression p = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (p == null || p.getAlliedFaction() != Factions.UNDEAD) {
            return Component.translatable((String)"mna:rituals/consume_crystal_soul.not_undead");
        }
        return null;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        ItemStack crystalStack = ItemStack.EMPTY;
        for (ItemStack stack : context.getCollectedReagents()) {
            if (stack.getItem() != ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get()) continue;
            crystalStack = stack;
            break;
        }
        if (crystalStack.isEmpty() || EntityCrystal.getEntityType(crystalStack) == null) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/consume_crystal_soul.crystal_empty"));
            return false;
        }
        Entity e = EntityCrystal.restoreEntity(context.getLevel(), crystalStack);
        if (SummonUtils.isSummon(e)) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/consume_crystal_soul.no_summons"));
            return false;
        }
        float souls = EntityUtil.getSoulsRestored(context.getCaster(), e) * 20.0f;
        MNACap.get(context.getCaster(), PlayerMagicProvider.MAGIC).ifPresent(m -> {
            m.getCastingResource().restore(souls);
            m.getCastingResource().setNeedsSync();
        });
        Vec3 center = Vec3.atCenterOf((Vec3i)context.getCenter());
        ItemEntity itemEntity = new ItemEntity(context.getLevel(), center.x, center.y, center.z, new ItemStack((ItemLike)Items.DIAMOND));
        context.getLevel().addFreshEntity((Entity)itemEntity);
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}

