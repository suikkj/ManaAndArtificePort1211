package com.mna.recipes;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * [PORT 1.21.1] No 1.21.1 o codec de receita NAO recebe o ResourceLocation (o loader de datapack
 * passa apenas o MapLike/JSON), entao as receitas do MNA nascem com o ID placeholder
 * ({@link AMRecipeBaseSerializer#PENDING_ID}) ou null (via streamCodec no cliente).
 * Este fixer percorre o RecipeManager DEPOIS do load/sync e restaura o ID real de cada
 * {@link AMRecipeBase} a partir do RecipeHolder correspondente.
 */
public final class RecipeIdFixer {
    private RecipeIdFixer() {}

    public static void fix(RecipeManager manager) {
        if (manager == null) {
            return;
        }
        int fixed = 0;
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof AMRecipeBase base) {
                if (base.getId() == null || AMRecipeBaseSerializer.PENDING_ID.equals(base.getId())) {
                    base.setId(holder.id());
                    fixed++;
                }
            }
        }
        if (fixed > 0) {
            com.mna.ManaAndArtifice.LOGGER.info("Mana And Artifice >> Restored recipe IDs for {} MNA recipes", fixed);
        }
    }

    /** Servidor (inclui singleplayer integrado): receitas carregadas no reload inicial. */
    @EventBusSubscriber(modid = "mna")
    public static final class Server {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            fix(event.getServer().getRecipeManager());
        }
    }

    /** Cliente: receitas sincronizadas do servidor via pacote (streamCodec -> ID null). */
    @EventBusSubscriber(modid = "mna", value = Dist.CLIENT)
    public static final class Client {
        @SubscribeEvent
        public static void onRecipesUpdated(net.neoforged.neoforge.client.event.RecipesUpdatedEvent event) {
            fix(event.getRecipeManager());
        }
    }
}
