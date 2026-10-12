 
package com.mna;

import com.mna.api.particles.ParticleInit;
import com.mna.api.tools.RLoc;
import com.mna.apibridge.APIBridge;
import com.mna.blocks.BlockClientInit;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.init.TileEntityClientInit;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.brewing.BrewingInit;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.commands.CommandInit;
import com.mna.commands.CommandSerializerInit;
import com.mna.config.ClientConfig;
import com.mna.config.GeneralConfig;
import com.mna.effects.EffectInit;
import com.mna.effects.particles.EffectParticleSpawner;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.entities.EntityClientInit;
import com.mna.entities.EntityInit;
import com.mna.entities.attributes.AttributeInit;
import com.mna.entities.models.constructs.modular.ConstructModelRegistry;
import com.mna.events.ClientEventHandler;
import com.mna.gui.GuiInit;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.gui.containers.ContainerInit;
import com.mna.guide.GuideBookEntries;
import com.mna.items.ItemClientInit;
import com.mna.items.ItemInit;
import com.mna.items.artifice.ThaumaturgicCompass;
import com.mna.items.ritual.ThaumaturgicLink;
import com.mna.loot.GlobalLootModifiers;
import com.mna.particles.ParticleClientInit;
import com.mna.recipes.RecipeInit;
import com.mna.tools.ISidedProxy;
import com.mna.tools.debugging.ForgeLoggerTweaker;
import com.mna.tools.proxy.ClientProxy;
import com.mna.tools.proxy.ServerProxy;
import com.mna.villagers.VillagerRegistry;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(value="mna")
public class ManaAndArtifice {
    public static final Logger LOGGER = LogManager.getLogger();
    public static ManaAndArtifice instance;
    public static final ResourceLocation EMPTY;
    public final IEventBus modEventBus;
    public ISidedProxy proxy;
    public final boolean isDebug;
    public final ArrayList<UUID> enabled_auras = new ArrayList();

    public ManaAndArtifice(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        this.modEventBus = modEventBus;
        boolean HIDE_CONSOLE_NOISE = this.isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
        if (HIDE_CONSOLE_NOISE) {
            ForgeLoggerTweaker.setMinimumLevel(org.apache.logging.log4j.Level.WARN);
            ForgeLoggerTweaker.applyLoggerFilter();
        }
        modContainer.registerConfig(ModConfig.Type.CLIENT, (IConfigSpec)ClientConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, (IConfigSpec)GeneralConfig.GENERAL_SPEC, "mna-general.toml");
        APIBridge.earlyInit(modContainer);
        LOGGER.info("M&A -> Configs Registered");
        NeoForgeMod.enableMilkFluid();
        instance = this;
        ItemInit.ITEMS.register(this.modEventBus);
        com.mna.items.artifice.FluidJug.COMPONENTS.register(this.modEventBus);
        com.mna.items.armor.ArmorMaterialInit.MATERIALS.register(this.modEventBus);
        BlockInit.BLOCKS.register(this.modEventBus);
        TileEntityInit.TILE_ENTITY_TYPES.register(this.modEventBus);
        AttributeInit.ATTRIBUTES.register(this.modEventBus);
        EntityInit.ENTITY_TYPES.register(this.modEventBus);
        ParticleInit.PARTICLES.register(this.modEventBus);
        ContainerInit.CONTAINERS.register(this.modEventBus);
        RecipeInit.SERIALIZERS.register(this.modEventBus);
        RecipeInit.TYPES.register(this.modEventBus);
        VillagerRegistry.POI_TYPES.register(this.modEventBus);
        VillagerRegistry.PROFESSIONS.register(this.modEventBus);
        CommandSerializerInit.ARGUMENTS.register(this.modEventBus);
        GlobalLootModifiers.LOOT_MODIFIERS.register(this.modEventBus);
        EffectInit.EFFECTS.register(this.modEventBus);
        EnchantmentInit.ENCHANTMENTS.register(this.modEventBus);
        BrewingInit.POTIONS.register(this.modEventBus);
        Registries.register(this.modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(HUDOverlayRenderer.class);
            NeoForge.EVENT_BUS.register(ClientEventHandler.class);
            NeoForge.EVENT_BUS.register(EffectParticleSpawner.class);
            NeoForge.EVENT_BUS.register(GuideBookEntries.class);
            this.modEventBus.addListener(this::clientInit);
            this.modEventBus.register(GuiInit.class);
            this.modEventBus.register(ParticleClientInit.class);
            this.modEventBus.register(TileEntityClientInit.class);
            this.modEventBus.register(BlockClientInit.class);
            this.modEventBus.register(ItemClientInit.class);
            this.modEventBus.register(ConstructModelRegistry.class);
            this.modEventBus.register(EntityClientInit.class);
            this.proxy = new ClientProxy();
        } else {
            this.proxy = new ServerProxy();
        }
        NeoForge.EVENT_BUS.register(CommandInit.class);
        this.modEventBus.register(CommandSerializerInit.class);
        try {
            String inputLine;
            URL url = new URL("https://manaandartifice.com/auras_enabled.txt");
            HttpURLConnection httpcon = (HttpURLConnection)url.openConnection();
            httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                try {
                    UUID uuid = UUID.fromString(inputLine);
                    this.enabled_auras.add(uuid);
                }
                catch (Exception exception) {}
            }
            in.close();
            LOGGER.info("M&A -> Retrieved " + this.enabled_auras.size() + " auras from the server");
        }
        catch (Exception e) {
            LOGGER.error("M&A -> Failed to query auras from the server");
        }
        LOGGER.info("M&A -> Mod Event Bus Handlers Registered");
    }

    @OnlyIn(value=Dist.CLIENT)
    public void clientInit(FMLClientSetupEvent event) {
        ItemProperties.register((Item)((Item)ItemInit.THAUMATURGIC_COMPASS.get()), (ResourceLocation)ResourceLocation.parse("angle"), (ItemPropertyFunction)new ClampedItemPropertyFunction(){
            private final Angle wobble;
            private final Angle wobbleRandom;
            {
                this.wobble = new Angle();
                this.wobbleRandom = new Angle();
            }

            public float unclampedCall(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity living, int some_number) {
                LivingEntity entity;
                Object object = entity = living != null ? living : (LivingEntity)stack.getEntityRepresentation();
                if (entity == null) {
                    return 0.0f;
                }
                if (world == null && entity.level() instanceof ClientLevel) {
                    world = (ClientLevel)entity.level();
                }
                BlockPos blockpos = ((ThaumaturgicCompass)(ItemInit.THAUMATURGIC_COMPASS.get())).getTrackedPosition(stack, (ResourceKey<Level>)world.dimension());
                long i = world.getGameTime();
                if (blockpos != null && !(entity.position().distanceToSqr((double)blockpos.getX() + 0.5, entity.position().y(), (double)blockpos.getZ() + 0.5) < (double)1.0E-5f)) {
                    double d3;
                    boolean flag = living instanceof Player && ((Player)living).isLocalPlayer();
                    double d1 = 0.0;
                    if (flag) {
                        d1 = living.getYRot();
                    } else if (entity instanceof ItemFrame) {
                        d1 = this.getFrameRotation((ItemFrame)entity);
                    } else if (entity instanceof ItemEntity) {
                        d1 = 180.0f - ((ItemEntity)entity).getSpin(0.5f) / ((float)Math.PI * 2) * 360.0f;
                    } else if (living != null) {
                        d1 = living.yBodyRot;
                    }
                    d1 = Mth.positiveModulo((double)(d1 / 360.0), (double)1.0);
                    double d2 = this.getAngleTo(Vec3.atCenterOf((Vec3i)blockpos), (Entity)entity) / 6.2831854820251465;
                    if (flag) {
                        if (this.wobble.shouldUpdate(i)) {
                            this.wobble.update(i, 0.5 - (d1 - 0.25));
                        }
                        d3 = d2 + this.wobble.rotation;
                    } else {
                        d3 = 0.5 - (d1 - 0.25 - d2);
                    }
                    return Mth.positiveModulo((float)((float)d3), (float)1.0f);
                }
                if (this.wobbleRandom.shouldUpdate(i)) {
                    this.wobbleRandom.update(i, Math.random());
                }
                double d0 = this.wobbleRandom.rotation + (double)((float)stack.hashCode() / 2.1474836E9f);
                return Mth.positiveModulo((float)((float)d0), (float)1.0f);
            }

            private double getFrameRotation(ItemFrame p_239441_1_) {
                Direction direction = p_239441_1_.getDirection();
                int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
                return Mth.wrapDegrees((int)(180 + direction.get2DDataValue() * 90 + p_239441_1_.getRotation() * 45 + i));
            }

            private double getAngleTo(Vec3 p_239443_1_, Entity p_239443_2_) {
                return Math.atan2(p_239443_1_.z() - p_239443_2_.getZ(), p_239443_1_.x() - p_239443_2_.getX());
            }
        });
        ItemProperties.register((Item)((Item)ItemInit.HELLFIRE_TRIDENT.get()), (ResourceLocation)RLoc.create("throwing"), (ItemPropertyFunction)new ClampedItemPropertyFunction(){

            public float unclampedCall(ItemStack pStack, ClientLevel pLevel, LivingEntity pEntity, int pSeed) {
                return pEntity != null && pEntity.isUsingItem() ? 1.0f : 0.0f;
            }
        });
        ItemProperties.register((Item)((Item)ItemInit.THAUMATURGIC_LINK.get()), (ResourceLocation)ResourceLocation.parse("biome"), (ItemPropertyFunction)new ClampedItemPropertyFunction(){

            public float unclampedCall(ItemStack pStack, ClientLevel pLevel, LivingEntity pEntity, int pSeed) {
                ThaumaturgicLink link;
                Item item = pStack.getItem();
                if (item instanceof ThaumaturgicLink && (link = (ThaumaturgicLink)item).isBiome(pStack)) {
                    return 1.0f;
                }
                return 0.0f;
            }
        });
        CastingResourceGuiRegistry.Instance.registerDefaults();
    }

    static {
        EMPTY = ResourceLocation.fromNamespaceAndPath("mna", "empty");
    }

    @OnlyIn(value=Dist.CLIENT)
    class Angle {
        private double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        private Angle() {
        }

        private boolean shouldUpdate(long p_239448_1_) {
            return this.lastUpdateTick != p_239448_1_;
        }

        private void update(long p_239449_1_, double p_239449_3_) {
            this.lastUpdateTick = p_239449_1_;
            double d0 = p_239449_3_ - this.rotation;
            d0 = Mth.positiveModulo((double)(d0 + 0.5), (double)1.0) - 0.5;
            this.deltaRotation += d0 * 0.1;
            this.deltaRotation *= 0.8;
            this.rotation = Mth.positiveModulo((double)(this.rotation + this.deltaRotation), (double)1.0);
        }
    }
}


