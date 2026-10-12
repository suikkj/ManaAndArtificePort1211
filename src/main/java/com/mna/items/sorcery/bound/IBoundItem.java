 
package com.mna.items.sorcery.bound;

import com.mna.tools.EnchantmentUtils;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.BoundItemDismissedEvent;
import com.mna.api.events.BoundItemSummonedEvent;
import com.mna.api.items.IShowHud;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedSpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.interop.CuriosInterop;
import com.mna.items.relic.BoundCharm;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import org.apache.commons.lang3.mutable.MutableBoolean;
import software.bernie.geckolib.animatable.GeoItem;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;

public interface IBoundItem
extends GeoItem,
IShowHud {
    public static final UUID BASE_RANGE_UUID = UUID.fromString("84468043-330D-428A-A8A3-688DB03E029F");

    public float getPassiveManaDrain(Entity var1);

    default public ItemStack createFromSpell(LivingEntity caster, Item outputItem, ItemStack originalItem, ISpellDefinition spell) {
        CompoundTag nbt = new CompoundTag();
        originalItem.save(nbt);
        ItemStack bound = new ItemStack((ItemLike)outputItem);
        bound.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put("original_item", (Tag)nbt);
        bound.set(DataComponents.ITEM_NAME, originalItem.getHoverName());
        spell.writeToNBT(bound.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        CuriosInterop.GetSingleItem(caster, BoundCharm.class).ifPresent(slotResult -> {
            Map enchantments = EnchantmentUtils.enchantmentMap((ItemStack)slotResult.stack());
            EnchantmentHelper.setEnchantments((Map)enchantments, (ItemStack)bound);
        });
        NeoForge.EVENT_BUS.post((Event)new BoundItemSummonedEvent(bound));
        return bound;
    }

    default public ItemStack restoreItem(ItemStack stack) {
        ItemStack restored = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)MNAItemData.element(stack, "original_item")).orElse(ItemStack.EMPTY);
        NeoForge.EVENT_BUS.post((Event)new BoundItemDismissedEvent(stack, restored));
        return restored;
    }

    default public SpellRecipe getRecipe(ItemStack stack) {
        return SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    default public void handleInventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof Player) {
            Player player = (Player)entityIn;
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                if (!m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, this.getPassiveManaDrain(entityIn))) {
                    int restoreSlot = itemSlot;
                    if (restoreSlot == 0 && ((ItemStack)player.getInventory().offhand.get(0)).hashCode() == stack.hashCode()) {
                        restoreSlot = 40;
                    }
                    player.getInventory().setItem(restoreSlot, this.restoreItem(stack));
                } else {
                    m.getCastingResource().consume((LivingEntity)player, this.getPassiveManaDrain(entityIn));
                }
            });
        }
    }

    default public boolean canCastSpell(ISpellDefinition recipe, LivingEntity attacker, InteractionHand hand) {
        if (!(attacker instanceof Player)) {
            return false;
        }
        Player caster = (Player)attacker;
        MutableBoolean canCast = new MutableBoolean(true);
        MNACap.get(caster, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (!caster.isCreative()) {
                if (recipe.getComplexity() > (float)p.getTierMaxComplexity() || recipe.getShape().getPart().getTier(attacker.level()) > p.getTier()) {
                    canCast.setFalse();
                }
                recipe.iterateComponents(c -> {
                    if (((SpellEffect)c.getPart()).getTier(attacker.level()) > p.getTier()) {
                        canCast.setFalse();
                    }
                });
            }
        });
        return canCast.booleanValue();
    }

    default public boolean affectTarget(ISpellDefinition recipe, LivingEntity attacker, LivingEntity target, InteractionHand hand) {
        MutableBoolean applied = new MutableBoolean(false);
        if (!attacker.level().isClientSide() && recipe.isValid()) {
            IPlayerProgression progression;
            if (!this.canCastSpell(recipe, attacker, hand)) {
                return false;
            }
            SpellContext context = new SpellContext(attacker.level(), recipe);
            SpellSource source = new SpellSource(attacker, hand);
            SpellCaster.applyAdjusters(attacker.getItemInHand(hand), attacker, hand, false, recipe, SpellCastStage.CASTING);
            ArrayList<SpellEffect> appliedEffects = new ArrayList<SpellEffect>();
            recipe.iterateComponents(c -> {
                int delay = (int)(c.getValue(Attribute.DELAY) * 20.0f);
                boolean appliedComponent = false;
                if (delay > 0) {
                    DelayedEventQueue.pushEvent(attacker.level(), new TimedDelayedSpellEffect(((SpellEffect)c.getPart()).getRegistryName().toString(), delay, source, new SpellTarget((Entity)target), (IModifiedSpellPart<SpellEffect>)c, context));
                    applied.setTrue();
                    appliedComponent = true;
                } else if (((SpellEffect)c.getPart()).ApplyEffect(source, new SpellTarget((Entity)target), (IModifiedSpellPart<SpellEffect>)c, context) == ComponentApplicationResult.SUCCESS) {
                    applied.setTrue();
                    appliedEffects.add((SpellEffect)c.getPart());
                    appliedComponent = true;
                }
                if (appliedComponent && attacker instanceof Player) {
                    SpellCaster.addComponentRoteProgress((Player)attacker, (SpellEffect)c.getPart());
                }
            });
            if (applied.isTrue() && !attacker.level().isClientSide()) {
                SpellCaster.spawnClientFX(attacker.level(), attacker.position(), Vec3.ZERO, source, appliedEffects);
            }
            if (attacker instanceof Player && applied.isTrue() && (progression = MNACap.get((IPlayerProgression)((Player)attacker), PlayerProgressionProvider.PROGRESSION).orElse(null)) != null && !recipe.canFactionCraft(progression) && recipe instanceof SpellRecipe) {
                ((SpellRecipe)recipe).usedByPlayer((Player)attacker);
            }
        }
        return applied.booleanValue();
    }
}


