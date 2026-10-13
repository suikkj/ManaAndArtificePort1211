/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.biome.Biome
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.capabilities.playerdata.magic.resources;

import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import com.mna.api.capabilities.resource.ICastingResourceGuiRegistry;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceRegistry;
import com.mna.gui.GuiTextures;
import com.mna.items.ItemInit;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@OnlyIn(value=Dist.CLIENT)
public class CastingResourceGuiRegistry
implements ICastingResourceGuiRegistry {
    private HashMap<ResourceLocation, ICastingResourceGuiProvider> _guiProviders = new HashMap();
    public static final CastingResourceGuiRegistry Instance;
    private static final Supplier<ItemStack> HUD_BRIMSTONE_COLD;
    private static final Supplier<ItemStack> HUD_BRIMSTONE_WARM;
    private static final Supplier<ItemStack> HUD_BRIMSTONE_HOT;
    private static final Supplier<ItemStack> HUD_COUNCIL;
    private static final Supplier<ItemStack> HUD_FEY_SUMMER;
    private static final Supplier<ItemStack> HUD_FEY_WINTER;
    private static final Supplier<ItemStack> HUD_UNDEAD;

    public void registerDefaults() {
        Instance.registerResourceGui(CastingResourceIDs.MANA, new ICastingResourceGuiProvider(){

            @Override
            public ResourceLocation getTexture() {
                return GuiTextures.Hud.BARS;
            }

            @Override
            public int getXPBarColor() {
                return -12039016;
            }

            @Override
            public int getBarColor() {
                return -7504641;
            }

            @Override
            public int getBarManaCostEstimateColor() {
                return -13357731;
            }

            @Override
            public int getResourceNumericTextColor() {
                return -5394;
            }

            @Override
            public int getBadgeSize() {
                return 64;
            }

            @Override
            public int getFrameU() {
                return 0;
            }

            @Override
            public int getFrameV() {
                return 0;
            }

            @Override
            public int getFrameWidth() {
                return 153;
            }

            @Override
            public int getFrameHeight() {
                return 24;
            }

            @Override
            public int getLevelDisplayY() {
                return this.getFrameHeight() - 6;
            }
        });
        Instance.registerResourceGui(CastingResourceIDs.COUNCIL_MANA, new ICastingResourceGuiProvider(){

            @Override
            public ResourceLocation getTexture() {
                return GuiTextures.Hud.BARS;
            }

            @Override
            public int getXPBarColor() {
                return -12039016;
            }

            @Override
            public int getBarColor() {
                return -7504641;
            }

            @Override
            public int getBarManaCostEstimateColor() {
                return -13357731;
            }

            @Override
            public int getResourceNumericTextColor() {
                return -5394;
            }

            @Override
            public int getBadgeSize() {
                return 64;
            }

            @Override
            public int getFrameU() {
                return 0;
            }

            @Override
            public int getFrameV() {
                return 24;
            }

            @Override
            public int getFrameWidth() {
                return 153;
            }

            @Override
            public int getFrameHeight() {
                return 24;
            }

            @Override
            public ItemStack getBadgeItem() {
                return HUD_COUNCIL.get();
            }

            @Override
            public int getBadgeItemOffsetY() {
                return 10;
            }

            @Override
            public int getLevelDisplayY() {
                return this.getFrameHeight() - 2;
            }
        });
        Instance.registerResourceGui(CastingResourceIDs.SOULS, new ICastingResourceGuiProvider(){

            @Override
            public ResourceLocation getTexture() {
                return GuiTextures.Hud.BARS;
            }

            @Override
            public int getXPBarColor() {
                return -9666636;
            }

            @Override
            public int getBarColor() {
                return -12497052;
            }

            @Override
            public int getBarManaCostEstimateColor() {
                return -12929864;
            }

            @Override
            public int getResourceNumericTextColor() {
                return -1909793;
            }

            @Override
            public int getBadgeSize() {
                return 64;
            }

            @Override
            public int getFrameU() {
                return 0;
            }

            @Override
            public int getFrameV() {
                return 169;
            }

            @Override
            public int getFrameWidth() {
                return 153;
            }

            @Override
            public int getFrameHeight() {
                return 26;
            }

            @Override
            public ItemStack getBadgeItem() {
                return HUD_UNDEAD.get();
            }

            @Override
            public int getBadgeItemOffsetY() {
                return 13;
            }

            @Override
            public int getXPBarOffsetY() {
                return 18;
            }

            @Override
            public int getFillStartY() {
                return 8;
            }

            @Override
            public int getResourceNumericOffsetY() {
                return 11;
            }

            @Override
            public int getLevelDisplayY() {
                return this.getFrameHeight() - 2;
            }

            @Override
            public int getLowChargeOffsetY() {
                return 0;
            }
        });
        Instance.registerResourceGui(CastingResourceIDs.BRIMSTONE, new ICastingResourceGuiProvider(){

            @Override
            public ResourceLocation getTexture() {
                return GuiTextures.Hud.BARS;
            }

            @Override
            public int getXPBarColor() {
                return FastColor.ARGB32.color((int)255, (int)128, (int)32, (int)32);
            }

            @Override
            public int getBarColor() {
                return -3917309;
            }

            @Override
            public int getBarManaCostEstimateColor() {
                return FastColor.ARGB32.color((int)255, (int)245, (int)126, (int)32);
            }

            @Override
            public int getResourceNumericTextColor() {
                return -2927;
            }

            @Override
            public int getBadgeSize() {
                return 64;
            }

            @Override
            public int getFrameU() {
                return 0;
            }

            @Override
            public int getFrameV() {
                Minecraft mc = Minecraft.getInstance();
                Holder<net.minecraft.world.level.biome.Biome> biome = mc.level.getBiome(mc.player.blockPosition());
                if (biome.value().getBaseTemperature() < 0.15f) {
                    return 96;
                }
                if (biome.is(BiomeTags.SNOW_GOLEM_MELTS)) {
                    return 144;
                }
                return 120;
            }

            @Override
            public int getFrameWidth() {
                return 153;
            }

            @Override
            public int getFrameHeight() {
                return 24;
            }

            @Override
            public ItemStack getBadgeItem() {
                Minecraft mc = Minecraft.getInstance();
                Holder<net.minecraft.world.level.biome.Biome> biome = mc.level.getBiome(mc.player.blockPosition());
                if (biome.value().getBaseTemperature() < 0.15f) {
                    return HUD_BRIMSTONE_COLD.get();
                }
                if (biome.is(BiomeTags.SNOW_GOLEM_MELTS)) {
                    return HUD_BRIMSTONE_HOT.get();
                }
                return HUD_BRIMSTONE_WARM.get();
            }

            @Override
            public int getBadgeItemOffsetY() {
                Minecraft mc = Minecraft.getInstance();
                Biome biome = (Biome)mc.level.getBiome(mc.player.blockPosition()).value();
                if (biome.coldEnoughToSnow(mc.player.blockPosition())) {
                    return 11;
                }
                return ICastingResourceGuiProvider.super.getBadgeItemOffsetY();
            }
        });
        Instance.registerResourceGui(CastingResourceIDs.SUMMER_FIRE, new ICastingResourceGuiProvider(){

            @Override
            public ResourceLocation getTexture() {
                return GuiTextures.Hud.BARS;
            }

            @Override
            public int getXPBarColor() {
                return -10833371;
            }

            @Override
            public int getBarColor() {
                return -1405640;
            }

            @Override
            public int getBarManaCostEstimateColor() {
                return -8896481;
            }

            @Override
            public int getResourceNumericTextColor() {
                return 16711634;
            }

            @Override
            public int getBadgeSize() {
                return 64;
            }

            @Override
            public int getFrameU() {
                return 0;
            }

            @Override
            public int getFrameWidth() {
                return 153;
            }

            @Override
            public int getFrameHeight() {
                return 24;
            }

            @Override
            public int getFrameV() {
                return 48;
            }

            @Override
            public ItemStack getBadgeItem() {
                return HUD_FEY_SUMMER.get();
            }

            @Override
            public int getBadgeItemOffsetY() {
                return 9;
            }

            @Override
            public int getFillWidth() {
                return 127;
            }

            @Override
            public int getLevelDisplayY() {
                return this.getFrameHeight() - 3;
            }
        });
        Instance.registerResourceGui(CastingResourceIDs.WINTER_ICE, new ICastingResourceGuiProvider(){

            @Override
            public ResourceLocation getTexture() {
                return GuiTextures.Hud.BARS;
            }

            @Override
            public int getXPBarColor() {
                return -10390889;
            }

            @Override
            public int getBarColor() {
                return -4924696;
            }

            @Override
            public int getBarManaCostEstimateColor() {
                return -8482116;
            }

            @Override
            public int getResourceNumericTextColor() {
                return -14667956;
            }

            @Override
            public int getBadgeSize() {
                return 64;
            }

            @Override
            public int getFrameU() {
                return 0;
            }

            @Override
            public int getFrameWidth() {
                return 153;
            }

            @Override
            public int getFrameHeight() {
                return 24;
            }

            @Override
            public int getFrameV() {
                return 72;
            }

            @Override
            public ItemStack getBadgeItem() {
                return HUD_FEY_WINTER.get();
            }

            @Override
            public int getBadgeItemOffsetY() {
                return 9;
            }

            @Override
            public int getFillWidth() {
                return 127;
            }

            @Override
            public int getLevelDisplayY() {
                return this.getFrameHeight() - 1;
            }
        });
    }

    @Override
    public void registerResourceGui(ResourceLocation identifier, ICastingResourceGuiProvider guiProvider) {
        if (identifier == null) {
            return;
        }
        if (this._guiProviders.containsKey(identifier)) {
            throw new InvalidParameterException("The Casting Resource Identifier " + identifier.toString() + " is already in use.");
        }
        if (CastingResourceRegistry.Instance.getRegisteredClass(identifier) == null) {
            throw new InvalidParameterException("The Casting Resource Identifier " + identifier.toString() + " was not registered in the CastingResourceRegistrationEvent event.  Subscribe to this and map the resource to a class.");
        }
        this._guiProviders.put(identifier, guiProvider);
    }

    public ICastingResourceGuiProvider getGuiProvider(ResourceLocation key) {
        return this._guiProviders.getOrDefault(key, this._guiProviders.get(CastingResourceIDs.MANA));
    }

    static {
        HUD_BRIMSTONE_COLD = () -> new ItemStack((ItemLike)ItemInit.MOTE_FIRE.get());
        HUD_BRIMSTONE_WARM = () -> new ItemStack((ItemLike)ItemInit.GREATER_MOTE_FIRE.get());
        HUD_BRIMSTONE_HOT = () -> new ItemStack((ItemLike)ItemInit.GREATER_MOTE_HELLFIRE.get());
        HUD_COUNCIL = () -> new ItemStack((ItemLike)ItemInit.COUNCIL_HUD_BADGE.get());
        HUD_FEY_SUMMER = () -> new ItemStack((ItemLike)ItemInit.FEY_SUMMER_HUD_BADGE.get());
        HUD_FEY_WINTER = () -> new ItemStack((ItemLike)ItemInit.FEY_WINTER_HUD_BADGE.get());
        HUD_UNDEAD = () -> new ItemStack((ItemLike)ItemInit.UNDEAD_HUD_BADGE.get());
        Instance = new CastingResourceGuiRegistry();
    }
}


