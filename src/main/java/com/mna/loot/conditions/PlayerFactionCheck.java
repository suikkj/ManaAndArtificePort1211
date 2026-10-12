 
package com.mna.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlayerFactionCheck
implements LootItemCondition {
    public static final com.mojang.serialization.MapCodec<PlayerFactionCheck> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
        ResourceLocation.CODEC.optionalFieldOf("faction").forGetter(c -> java.util.Optional.ofNullable(c.factionID))
    ).apply(inst, faction -> new PlayerFactionCheck(faction.orElse(null))));
    public static final LootItemConditionType PLAYER_FACTION = new LootItemConditionType(CODEC);
    private final ResourceLocation factionID;

    private PlayerFactionCheck(ResourceLocation factionID) {
        this.factionID = factionID;
    }

    public LootItemConditionType getType() {
        return PLAYER_FACTION;
    }

    public boolean test(LootContext context) {
        Player lastPlayer = (Player)context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        if (lastPlayer == null || this.factionID == null) {
            return false;
        }
        MutableBoolean factionMatch = new MutableBoolean(false);
        MNACap.get(lastPlayer, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getAlliedFaction() != null) {
                IFaction faction = (IFaction)Registries.Factions.getValue(this.factionID);
                factionMatch.setValue(faction == p.getAlliedFaction());
            }
        });
        return factionMatch.booleanValue();
    }

}

