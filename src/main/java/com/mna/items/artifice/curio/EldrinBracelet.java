 
package com.mna.items.artifice.curio;

import com.mna.api.faction.IFaction;
import com.mna.api.items.ChargeableItem;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.factions.Factions;
import com.mna.items.artifice.curio.IPreEnchantedItem;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class EldrinBracelet
extends ChargeableItem
implements IPreEnchantedItem<EldrinBracelet> {
    public EldrinBracelet(Item.Properties properties, float maxMana) {
        super(properties, maxMana);
    }

    @Override
    protected boolean beforeCurioTick(LivingEntity entity, int index, ItemStack stack) {
        if (!entity.level().isClientSide() && entity.level().getGameTime() % 200L == 0L && entity instanceof ServerPlayer) {
            ServerMessageDispatcher.sendWellspringSyncMessage((ServerLevel)entity.level(), (ServerPlayer)entity);
        }
        return false;
    }

    @Override
    protected boolean tickEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        MutableBoolean applied = new MutableBoolean(false);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(magic -> MNACap.get(player.level().getChunkAt(player.blockPosition()), ChunkMagicProvider.MAGIC).ifPresent(cm -> {
            float missingMana = magic.getCastingResource().getMaxAmount() - magic.getCastingResource().getAmount();
            if (missingMana > 0.0f && cm.getResidualMagic() > 24.0f) {
                float manaRestored = Math.min(cm.getResidualMagic() / 2.0f, missingMana);
                magic.getCastingResource().setAmount(magic.getCastingResource().getAmount() + manaRestored);
                cm.removeResidualMagic(manaRestored);
                applied.setTrue();
            }
        }));
        return applied.booleanValue();
    }

    @Override
    protected boolean tickCurio() {
        return true;
    }

    @Override
    public IFaction getFaction() {
        return Factions.COUNCIL;
    }
}

