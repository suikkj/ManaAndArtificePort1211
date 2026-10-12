 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.faction.FactionIDs;
import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.providers.NamedGrimoire;
import com.mna.items.renderers.books.CouncilGrimoireBookRenderer;
import com.mna.items.renderers.books.DemonGrimoireBookRenderer;
import com.mna.items.renderers.books.FeyGrimoireBookRenderer;
import com.mna.items.renderers.books.GrimoireBookRenderer;
import com.mna.items.renderers.books.UndeadGrimoireBookRenderer;
import com.mna.items.sorcery.SpellBook;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public class Grimoire
extends SpellBook {
    private final boolean showSubtitle;
    private final ResourceLocation forFaction;

    public Grimoire() {
        this(null, false);
    }

    public Grimoire(ResourceLocation faction, boolean renderBook) {
        super(renderBook);
        this.tier = faction == null ? 3 : 4;
        this.showSubtitle = faction != null;
        this.forFaction = faction;
    }

    public Grimoire(Item.Properties properties, @Nullable ResourceLocation faction, @Nullable ResourceLocation openModel, @Nullable ResourceLocation closedModel, boolean renderInFirstPerson) {
        super(properties, openModel, closedModel, renderInFirstPerson);
        this.tier = faction == null ? 3 : 4;
        this.showSubtitle = faction != null;
        this.forFaction = faction;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> {
                if (Grimoire.this.forFaction == FactionIDs.COUNCIL) {
                    return new CouncilGrimoireBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                if (Grimoire.this.forFaction == FactionIDs.DEMONS) {
                    return new DemonGrimoireBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                if (Grimoire.this.forFaction == FactionIDs.FEY) {
                    return new FeyGrimoireBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                if (Grimoire.this.forFaction == FactionIDs.UNDEAD) {
                    return new UndeadGrimoireBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                return new GrimoireBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            };

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedGrimoire();
    }

    @Override
    public ISpellBookInventory getInventory(ItemStack item, IPlayerMagic magic) {
        if (magic == null) {
            return null;
        }
        return magic.getGrimoireInventory();
    }

    @Override
    public CompoundTag getSpellCompound(ItemStack stack, Player player) {
        if (player == null) {
            return new CompoundTag();
        }
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic == null) {
            return new CompoundTag();
        }
        ItemStack selectedStack = magic.getGrimoireInventory().getItem(Grimoire.getActiveSpellSlot(stack));
        return selectedStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("cur_spell_use_duration")) {
            return tag.getInt("cur_spell_use_duration");
        }
        return 9999;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack bookStack;
        ItemStack selectedStack;
        SpellRecipe recipe;
        if (!world.isClientSide && (recipe = SpellRecipe.fromNBT((selectedStack = this.getSelectedStack(bookStack = player.getItemInHand(hand), player)).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())).isValid()) {
            CompoundTag bookTag = bookStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            bookTag.putInt("cur_spell_use_duration", recipe.getMaxChannelTime());
            bookStack.set(DataComponents.CUSTOM_DATA, CustomData.of(bookTag));
        }
        return super.use(world, player, hand);
    }

    @Override
    protected ItemStack getSelectedStack(ItemStack bookStack, @Nullable Player player) {
        SimpleContainer bookInv;
        if (bookStack.getItem() instanceof SpellBook && (bookInv = (SimpleContainer)this.getInventory(bookStack, player != null ? (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get()) : null)) != null) {
            ItemStack selectedStack = bookInv.getItem(Grimoire.getActiveSpellSlot(bookStack));
            return selectedStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (this.showSubtitle) {
            tooltip.add((Component)Component.translatable((String)(stack.getDescriptionId() + ".subtitle")));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public int getColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 5841080;
    }
}


