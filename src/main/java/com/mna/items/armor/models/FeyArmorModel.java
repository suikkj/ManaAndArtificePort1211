 
package com.mna.items.armor.models;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.factions.Factions;
import com.mna.items.armor.FeyArmorItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.model.GeoModel;
import com.mna.api.tools.MNACap;

public class FeyArmorModel
extends GeoModel<FeyArmorItem> {
    public ResourceLocation getModelResource(FeyArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("mna", "geo/armor/fey_armor.geo.json");
    }

    public ResourceLocation getTextureResource(FeyArmorItem object) {
        IPlayerMagic magic;
        Player player;
        IPlayerProgression progression;
        LivingEntity livingEntity = FeyArmorItem.renderEntity;
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            IPlayerProgression progression = MNACap.get(player, PlayerProgressionProvider.PROGRESSION).orElse(null);
            IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
            if (progression != null && progression.getAlliedFaction() == Factions.FEY && magic != null && magic.getCastingResource().getRegistryName().equals(CastingResourceIDs.WINTER_ICE)) {
                return ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/model/fey_armor_winter.png");
            }
        }
        return ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/model/fey_armor.png");
    }

    public ResourceLocation getAnimationResource(FeyArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mna", "animations/armor/fey_armor.animation.json");
    }
}

