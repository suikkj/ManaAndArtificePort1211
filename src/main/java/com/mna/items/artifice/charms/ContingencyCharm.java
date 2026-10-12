 
package com.mna.items.artifice.charms;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.items.artifice.charms.CharmBaseItem;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import top.theillusivec4.curios.api.CuriosApi;

public class ContingencyCharm
extends CharmBaseItem
implements ICanContainSpell {
    private static final String NBT_CHARGES = "charm_charges";
    private final ContingencyEvent event;
    private final boolean breakOnZeroCharges;
    private final int maxCharges;

    public ContingencyCharm(ContingencyEvent event, boolean breakOnZeroCharges) {
        this(event, breakOnZeroCharges, 1);
    }

    public ContingencyCharm(ContingencyEvent event, boolean breakOnZeroCharges, int maxCharges) {
        this.event = event;
        this.breakOnZeroCharges = breakOnZeroCharges;
        this.maxCharges = maxCharges;
    }

    public boolean isFor(ContingencyEvent event) {
        return this.event == event;
    }

    public boolean isReady(ItemStack stack, ContingencyEvent event) {
        return this.getCharges(stack) > 0 && this.isFor(event);
    }

    @Override
    public boolean consume(ServerPlayer player) {
        MutableInt charges = new MutableInt(0);
        CuriosApi.getCuriosHelper().findCurios((LivingEntity)player, (Item)this).stream().findFirst().ifPresent(slot -> {
            ISpellDefinition spell = this.getSpell(slot.stack());
            if (spell.isValid()) {
                SpellCaster.PlayerCast(slot.stack(), (Player)player, InteractionHand.OFF_HAND, player.position(), player.getForward(), player.level(), false, true);
            }
            charges.setValue(this.consumeCharge(slot.stack(), (Player)player));
            if (charges.getValue() <= 0) {
                if (this.breakOnZeroCharges) {
                    slot.stack().hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, e -> {});
                } else {
                    SpellRecipe.removeSpellFromTag(slot.stack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                }
            }
        });
        return false;
    }

    @Override
    public boolean consume(ServerPlayer player, InteractionHand hand) {
        return false;
    }

    private int consumeCharge(ItemStack stack, Player player) {
        int newCharges = this.getCharges(stack) - 1;
        this.setCharges(stack, newCharges);
        return newCharges;
    }

    @Override
    public boolean canAcceptSpell(ItemStack stack, ISpellDefinition spell) {
        if (spell.isChanneled()) {
            return false;
        }
        if (this.getCharges(stack) >= this.maxCharges) {
            return false;
        }
        ISpellDefinition curSpell = this.getSpell(stack);
        return !curSpell.isValid() || curSpell.isSame(spell, false, true, true);
    }

    @Override
    public ItemStack setSpell(ItemStack stack, ISpellDefinition spell) {
        ISpellDefinition curSpell;
        if (this.containsSpell(stack) && (curSpell = this.getSpell(stack)).isSame(spell, false, true, true)) {
            this.setCharges(stack, this.getCharges(stack) + 1);
            return stack;
        }
        this.setCharges(stack, 1);
        return ICanContainSpell.super.setSpell(stack, spell);
    }

    private void setCharges(ItemStack stack, int charges) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(NBT_CHARGES, Mth.clamp((int)charges, (int)0, (int)this.maxCharges));
    }

    public int getCharges(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(NBT_CHARGES);
        }
        return 0;
    }

    public static void CheckAndConsumeCharmCharge(ServerPlayer player, ContingencyEvent event) {
        CuriosApi.getCuriosHelper().findCurios((LivingEntity)player, is -> is.getItem() instanceof ContingencyCharm && ((ContingencyCharm)is.getItem()).isReady((ItemStack)is, event)).stream().forEach(slot -> ((ContingencyCharm)slot.stack().getItem()).consume(player));
    }

    public void appendHoverText(ItemStack pStack, net.minecraft.world.item.Item.TooltipContext pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        if (player != null) {
            ISpellDefinition recipe = this.getSpell(pStack);
            recipe.addItemTooltip(pStack, pLevel, pTooltipComponents, player);
        }
        pTooltipComponents.add((Component)Component.translatable((String)"item.mna.contingency_charm.charges_display", (Object[])new Object[]{this.getCharges(pStack), this.maxCharges}).withStyle(ChatFormatting.AQUA));
        if (this.breakOnZeroCharges) {
            pTooltipComponents.add((Component)Component.translatable((String)"item.mna.contingency_charm.consume_on_trigger").withStyle(ChatFormatting.GRAY));
        }
    }

    public static enum ContingencyEvent {
        LOW_HEALTH,
        DEATH,
        DAMAGE,
        FALL,
        FACTION_RAID,
        LOW_CASTING_RESOURCE,
        SUFFOCATION;

    }
}

