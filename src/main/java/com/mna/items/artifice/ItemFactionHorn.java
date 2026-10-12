 
package com.mna.items.artifice;

import com.mna.Registries;
import com.mna.api.faction.IFaction;
import com.mna.api.items.TieredItem;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.faction.util.FactionRaid;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class ItemFactionHorn
extends TieredItem {
    private final ResourceLocation faction;
    private IFaction _cachedFaction;

    public ItemFactionHorn(ResourceLocation faction) {
        super(new Item.Properties().rarity(Rarity.EPIC));
        this.faction = faction;
    }

    public IFaction getFaction() {
        if (this._cachedFaction == null) {
            this._cachedFaction = (IFaction)Registries.Factions.getValue(this.faction);
        }
        return this._cachedFaction;
    }

    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MNACap.get(context.getPlayer(), PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getTier() < 5) {
                context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.faction_horn.low_tier"));
                return;
            }
            if (p.getAlliedFaction() == null || !p.getAlliedFaction().equals(this.getFaction())) {
                context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.faction_horn.wrong_faction"));
                return;
            }
            context.getPlayer().getCooldowns().addCooldown((Item)this, 18000);
            if (this.getHornSound() != null) {
                world.playSound(null, (double)context.getClickedPos().getX(), (double)context.getClickedPos().getY(), (double)context.getClickedPos().getZ(), this.getHornSound(), SoundSource.PLAYERS, 1.0f, (float)((double)0.9f + (double)0.2f * Math.random()));
            }
            this.spawnAllies(world, context.getPlayer(), context.getClickedPos().above());
        });
        return InteractionResult.SUCCESS;
    }

    private void spawnAllies(Level world, Player player, BlockPos pos) {
        int strength = 150;
        FactionRaid fre = new FactionRaid(world, null, strength);
        fre.setPos((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5);
        fre.setFaction(this.faction);
        fre.setProtective(player);
        world.addFreshEntity((Entity)fre);
    }

    private SoundEvent getHornSound() {
        IFaction faction = (IFaction)Registries.Factions.getValue(this.faction);
        if (faction != null) {
            return faction.getHornSound();
        }
        return null;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    }
}

