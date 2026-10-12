 
package com.mna.items;

import java.util.function.Supplier;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.CodexBreadcrumb;
import com.mna.api.items.TieredItem;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.containers.providers.NamedGuideBook;
import com.mna.items.renderers.books.CodexBookRenderer;
import com.mna.tools.DidYouKnowHelper;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import com.mna.api.tools.MNACap;


public class CodexArcana
extends TieredItem {
    private int guiOpenCount = 0;
    private long lastActivateTime = 0L;

    public CodexArcana() {
        super(new Item.Properties().stacksTo(1));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new CodexBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND) {
            if (!world.isClientSide) {
                if (!ManaAndArtificeMod.checkMagicUnlock((ServerLevel)world, player)) {
                    ((ServerPlayer)((ServerPlayer)player)).openMenu((MenuProvider)new NamedGuideBook());
                }
            } else {
                MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                    if (p.peekCodexBreadcrumb() != null && p.peekCodexBreadcrumb().breadcrumbType == CodexBreadcrumb.Type.RECIPE) {
                        if (world.getGameTime() - this.lastActivateTime < 200L) {
                            ++this.guiOpenCount;
                            if (this.guiOpenCount >= 4) {
                                DidYouKnowHelper.CheckAndShowDidYouKnow(player, "helptip.mna.pin_recipe");
                            }
                        }
                    } else {
                        this.guiOpenCount = 0;
                    }
                    this.lastActivateTime = world.getGameTime();
                });
            }
        }
        return InteractionResultHolder.success(book);
    }

    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockpos;
        Level world = context.getLevel();
        BlockState blockstate = world.getBlockState(blockpos = context.getClickedPos());
        if (blockstate.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook((net.minecraft.world.entity.LivingEntity)context.getPlayer(), (Level)world, (BlockPos)blockpos, (BlockState)blockstate, (ItemStack)context.getItemInHand()) ? InteractionResult.sidedSuccess((boolean)world.isClientSide) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
}


