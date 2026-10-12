 
package com.mna.spells;

import com.mna.tools.EnchantmentUtils;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.enchantments.base.ModifierEnchantment;
import com.mna.factions.Factions;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.artifice.curio.EldrinBracelet;
import com.mna.items.base.IHellfireItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public final class SpellAdjusters {
    private static final float AFFINITY_MANA_COST_FACTOR = 0.5f;

    public static final void modifyBasedOnAffinity(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        java.util.Optional<PlayerMagicProvider> magicContainer = MNACap.get(context.caster, PlayerMagicProvider.MAGIC);
        if (!magicContainer.isPresent()) {
            return;
        }
        IPlayerMagic magic = (IPlayerMagic)magicContainer.get();
        if (magic == null) {
            return;
        }
        float base_mana_cost = context.spell.getManaCost();
        float modification_reduce = 0.0f;
        float modification_increase = 0.0f;
        HashMap<Affinity, Float> affList = context.spell.getAffinity();
        for (Affinity affinity : affList.keySet()) {
            Affinity mainAdjuster = affinity.getShiftAffinity();
            Affinity oppositeAdjuster = affinity.getOpposite().getShiftAffinity();
            modification_reduce += magic.getAffinityDepth(mainAdjuster) * 0.5f / 100.0f * affList.get(affinity).floatValue();
            modification_increase += magic.getAffinityDepth(oppositeAdjuster) * 0.5f / 100.0f * affList.get(affinity).floatValue();
        }
        context.spell.setManaCost(base_mana_cost - base_mana_cost * modification_reduce + base_mana_cost * modification_increase);
    }

    public static final void modifyChanneled(SpellAdjustingContext context) {
        if (context.spell.isChanneled()) {
            float manaCostPerTick = context.spell.getManaCost() / 20.0f;
            context.spell.setManaCost(manaCostPerTick * 0.5f);
        }
    }

    public static final void modifySigils(SpellAdjustingContext context) {
        if (context.spell.getShape() != null && context.spell.getShape().getPart() != null && context.spell.getShape().getPart() == Shapes.RUNE) {
            context.spell.setManaCost(context.spell.getManaCost() / 2.0f);
        }
    }

    public static final boolean checkHellfireStaff(SpellAdjustingContext context) {
        return context.stack.getItem() instanceof IHellfireItem;
    }

    public static final void modifyHellfireStaff(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        if (context.spell.getAffinity().containsKey(Affinity.FIRE)) {
            context.spell.iterateComponents(c -> {
                if (((SpellEffect)c.getPart()).getAffinity() == Affinity.FIRE) {
                    for (Attribute attr : c.getContainedAttributes()) {
                        if (!((SpellEffect)c.getPart()).isHellfireBoosted(attr)) continue;
                        c.setValue(attr, c.getValue(attr) * 2.0f);
                    }
                }
            });
            context.spell.setOverrideAffinity(Affinity.HELLFIRE);
            context.spell.setManaCost(context.spell.getManaCost() * 3.0f);
        }
    }

    public static final boolean checkArcaneCrown(SpellAdjustingContext context) {
        if (context.caster == null || !(context.caster instanceof Player)) {
            return false;
        }
        return ((ItemStack)((Player)context.caster).getInventory().armor.get(EquipmentSlot.HEAD.getIndex())).getItem() == ItemInit.ARCANE_CROWN.get() || CuriosInterop.IsItemInCurioSlot((Item)ItemInit.ARCANE_CROWN.get(), context.caster, SlotTypePreset.HEAD);
    }

    public static final void modifyArcaneCrown(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        context.spell.setManaCost(context.spell.getManaCost() * 0.85f);
    }

    public static final void modifyBreakRings(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        context.spell.getComponents().stream().filter(c -> c.getPart() == Components.BREAK || c.getPart() == Components.EXCHANGE).findFirst().ifPresent(c -> {
            int magnitudeIncrease = 0;
            if (CuriosApi.getCuriosHelper().findFirstCurio(context.caster, (Item)ItemInit.BREAK_RING_GREATER.get()).isPresent()) {
                magnitudeIncrease = 2;
            } else if (CuriosApi.getCuriosHelper().findFirstCurio(context.caster, (Item)ItemInit.BREAK_RING_LESSER.get()).isPresent()) {
                magnitudeIncrease = 1;
            }
            for (int i = 0; i < magnitudeIncrease; ++i) {
                c.stepUpIgnoreMax(Attribute.MAGNITUDE);
            }
        });
    }

    public static final boolean checkRingOfTheSkies(SpellAdjustingContext context) {
        if (context.caster == null) {
            return false;
        }
        if (context.stage == SpellCastStage.CASTING || context.stage == SpellCastStage.SPELL_TOOLTIP) {
            return CuriosInterop.IsItemInCurioSlot((Item)ItemInit.AIR_CAST_RING.get(), context.caster, SlotTypePreset.RING);
        }
        return false;
    }

    public static final void modifyAirCastRing(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        context.spell.getComponents().stream().filter(c -> c.getPart() == Components.FLING).findFirst().ifPresent(c -> c.stepUpIgnoreMax(Attribute.SPEED));
    }

    public static final void modifyBattlemageAmulet(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        if (!CuriosApi.getCuriosHelper().findFirstCurio(context.caster, (Item)ItemInit.BATTLEMAGE_AMULET.get()).isPresent()) {
            return;
        }
        ItemStack mainHand = context.caster.getMainHandItem();
        ItemStack offHand = context.caster.getOffhandItem();
        boolean mainHandIsSword = MATags.isItemIn(mainHand.getItem(), MATags.Items.BATTLEMAGE_AMULET_SWORDS);
        boolean offhandIsSword = MATags.isItemIn(offHand.getItem(), MATags.Items.BATTLEMAGE_AMULET_SWORDS);
        if (mainHandIsSword && (context.curioCast || offHand.getItem() instanceof ICanContainSpell) || offhandIsSword && mainHand.getItem() instanceof ICanContainSpell) {
            context.spell.getComponents().forEach(c -> c.setMultiplier(Attribute.DAMAGE, c.getMultiplier(Attribute.DAMAGE) + 0.3f));
        }
    }

    public static final boolean checkEldrinBracelet(SpellAdjustingContext context) {
        if (context.caster == null || !(context.caster instanceof Player)) {
            return false;
        }
        if (context.stage == SpellCastStage.SPELL_TOOLTIP || context.stage == SpellCastStage.CALCULATING_MANA_COST) {
            return ((EldrinBracelet)ItemInit.ELDRIN_BRACELET.get()).isEquippedAndHasMana(context.caster, 1.0f, false);
        }
        if (context.stage == SpellCastStage.CASTING && ((EldrinBracelet)ItemInit.ELDRIN_BRACELET.get()).isEquippedAndHasMana(context.caster, 1.0f, true)) {
            ((EldrinBracelet)ItemInit.ELDRIN_BRACELET.get()).usedByPlayer((Player)context.caster);
            return true;
        }
        return false;
    }

    public static final void modifyEldrinBracelet(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        MNACap.get(context.caster.level(), WorldMagicProvider.MAGIC).ifPresent(m -> {
            BlockPos casterPos = new BlockPos(context.caster.blockPosition().getX(), 0, context.caster.blockPosition().getZ());
            m.getWellspringRegistry().getNearbyNodes(casterPos, 0, 10).forEach((pos, node) -> {
                double dist = casterPos.distSqr((Vec3i)pos);
                float pctReduction = (float)(0.5 * ((100.0 - dist) / 100.0));
                context.spell.setManaCost(context.spell.getManaCost() - context.spell.getManaCost() * pctReduction);
            });
        });
    }

    public static final boolean checkCircleOfPower(SpellAdjustingContext context) {
        if (context.caster == null) {
            return false;
        }
        if (context.stage == SpellCastStage.CASTING || context.stage == SpellCastStage.SPELL_TOOLTIP) {
            return context.caster.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CIRCLE_OF_POWER.get()));
        }
        return false;
    }

    public static final void modifyCircleOfPower(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        float originalMana = context.spell.getManaCost();
        int amp = context.caster.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CIRCLE_OF_POWER.get())).getAmplifier() + 1;
        context.spell.iterateComponents(c -> c.getContainedAttributes().forEach(attr -> {
            if (attr == Attribute.SPEED) {
                c.setValue((Attribute)((attr)), c.getValue((Attribute)((attr))) + (float)amp);
            }
            if (attr == Attribute.DAMAGE) {
                c.setMultiplier((Attribute)((attr)), c.getMultiplier((Attribute)((attr))) + 0.25f * (float)amp);
            }
            if (attr == Attribute.DURATION) {
                c.setMultiplier((Attribute)((attr)), c.getMultiplier((Attribute)((attr))) + (float)(2 * amp));
            }
        }));
        context.spell.setManaCost(originalMana);
    }

    public static final void modifyAmplifyMagic(SpellAdjustingContext context) {
        int amplifier;
        if (context.caster == null) {
            return;
        }
        if (context.caster.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.AMPLIFY_MAGIC.get()))) {
            amplifier = context.caster.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.AMPLIFY_MAGIC.get())).getAmplifier();
            context.spell.setManaCost(context.spell.getManaCost() * (1.0f - 0.05f * (float)amplifier));
        }
        if (context.caster.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DAMPEN_MAGIC.get()))) {
            amplifier = context.caster.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DAMPEN_MAGIC.get())).getAmplifier();
            context.spell.setManaCost(context.spell.getManaCost() * (1.0f + 0.05f * (float)amplifier));
        }
    }

    public static final void modifyEfficiencyEnchant(SpellAdjustingContext context) {
        if (context.caster == null) {
            return;
        }
        int level = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.EFFICIENCY, context.stack);
        if (level == 0) {
            return;
        }
        context.spell.setManaCost(context.spell.getManaCost() * (1.0f - 0.05f * (float)level));
    }

    public static final void modifyStepEnchantments(SpellAdjustingContext context) {
        if (!context.spell.isValid()) {
            return;
        }
        ItemStack spellStack = context.stack;
        java.util.Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)spellStack);
        for (Map.Entry e : enchantments.entrySet()) {
            if (!(e.getKey() instanceof ModifierEnchantment)) continue;
            ModifierEnchantment me = (ModifierEnchantment)(e.getKey());
            int steps = me.bonusStepsPerLevel * (Integer)e.getValue();
            if (context.spell.getShape().getContainedAttributes().contains(me.boost)) {
                for (int i = 0; i < steps; ++i) {
                    context.spell.getShape().stepUpIgnoreMax(me.boost);
                }
            }
            context.spell.iterateComponents(c -> {
                if (c.getContainedAttributes().contains(me.boost)) {
                    for (int i = 0; i < steps; ++i) {
                        c.stepUpIgnoreMax(me.boost);
                    }
                }
            });
        }
    }

    public static final boolean checkSummerFire(SpellAdjustingContext context) {
        if (!(context.caster instanceof Player) || context.stage != SpellCastStage.CASTING && context.stage != SpellCastStage.SPELL_TOOLTIP) {
            return false;
        }
        Player player = (Player)context.caster;
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null || progression.getAlliedFaction() != Factions.FEY) {
            return false;
        }
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        return magic != null && magic.getCastingResource().getRegistryName() == CastingResourceIDs.SUMMER_FIRE;
    }

    public static final boolean checkWinterIce(SpellAdjustingContext context) {
        if (!(context.caster instanceof Player) || context.stage != SpellCastStage.CASTING && context.stage != SpellCastStage.SPELL_TOOLTIP) {
            return false;
        }
        Player player = (Player)context.caster;
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null || progression.getAlliedFaction() != Factions.FEY) {
            return false;
        }
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        return magic != null && magic.getCastingResource().getRegistryName() == CastingResourceIDs.WINTER_ICE;
    }

    public static final void modifySummerFire(SpellAdjustingContext context) {
        boolean isDay;
        if (!context.spell.isValid()) {
            return;
        }
        long time = context.caster.level().getDayTime() % 24000L;
        boolean bl = isDay = time <= 13000L;
        if (isDay) {
            SpellAdjusters.multiplyAttributes(context, 1.25f, Attribute.DURATION, Attribute.DAMAGE);
            SpellAdjusters.multiplyAttributes(context, 1.5f, Attribute.SPEED);
        } else {
            SpellAdjusters.stepDownAttributes(context, 2, Attribute.DURATION, Attribute.SPEED);
            SpellAdjusters.stepDownAttributes(context, 5, Attribute.DAMAGE);
        }
    }

    public static final void modifyWinterIce(SpellAdjustingContext context) {
        boolean isNight;
        if (!context.spell.isValid()) {
            return;
        }
        long time = context.caster.level().getDayTime() % 24000L;
        boolean bl = isNight = time >= 13000L;
        if (isNight) {
            SpellAdjusters.multiplyAttributes(context, 1.25f, Attribute.DURATION, Attribute.DAMAGE);
            SpellAdjusters.multiplyAttributes(context, 1.5f, Attribute.SPEED);
        } else {
            SpellAdjusters.stepDownAttributes(context, 2, Attribute.DURATION, Attribute.SPEED);
            SpellAdjusters.stepDownAttributes(context, 5, Attribute.DAMAGE);
        }
    }

    private static final void multiplyAttributes(SpellAdjustingContext context, float multiplier, Attribute ... attributes) {
        for (Attribute attr : attributes) {
            context.spell.getShape().setMultiplier(attr, context.spell.getShape().getMultiplier(attr) + multiplier);
            context.spell.iterateComponents(c -> c.setMultiplier(attr, c.getMultiplier(attr) + multiplier));
        }
    }

    private static final void stepDownAttributes(SpellAdjustingContext context, int steps, Attribute ... attributes) {
        for (Attribute attr : attributes) {
            for (int i = 0; i < steps; ++i) {
                context.spell.getShape().stepDown(attr, 0.0f);
            }
            context.spell.iterateComponents(c -> {
                for (int i = 0; i < steps; ++i) {
                    c.stepDown(attr, 0.0f);
                }
            });
        }
    }
}


