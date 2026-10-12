 
package com.mna.items.artifice.curio;

import com.mna.api.items.ChargeableItem;
import com.mna.items.artifice.curio.IPreEnchantedItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;

public class AntidoteBracelet
extends ChargeableItem
implements IPreEnchantedItem<AntidoteBracelet> {
    public AntidoteBracelet() {
        super(new Item.Properties().setNoRepair().rarity(Rarity.UNCOMMON), 60.0f);
    }

    @Override
    protected boolean tickEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        if (player.getEffect(MobEffects.POISON) != null) {
            CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)player, (Item)this).ifPresent(t -> {
                player.removeEffectNoUpdate(MobEffects.POISON);
                t.stack().hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, damager -> CuriosApi.getCuriosHelper().onBrokenCurio(t.slotContext()));
            });
            return true;
        }
        return false;
    }
}

