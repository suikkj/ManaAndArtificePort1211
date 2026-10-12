 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.gui.containers.providers.NamedRoteBook;
import com.mna.items.renderers.books.RoteBookRenderer;
import com.mna.items.sorcery.SpellBook;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public class BookOfRote
extends SpellBook {
    public BookOfRote() {
        super(false);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new RoteBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedRoteBook();
    }

    @Override
    public ISpellBookInventory getInventory(ItemStack item, IPlayerMagic magic) {
        if (magic == null) {
            return null;
        }
        return magic.getRoteInventory();
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
        ItemStack selectedStack = magic.getRoteInventory().getItem(BookOfRote.getActiveSpellSlot(stack));
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
        if (!world.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            IPlayerRoteSpells rote = (IPlayerRoteSpells)player.getData(PlayerRoteSpellsProvider.ROTE.get());
            IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
            if (rote == null || magic == null) {
                return InteractionResultHolder.fail(held);
            }
            ItemStack bookStack = player.getItemInHand(hand);
            ItemStack selectedStack = this.getSelectedStack(bookStack, player);
            SpellRecipe recipe = SpellRecipe.fromNBT(selectedStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            if (recipe.isValid() && !magic.isModifierPressed()) {
                if (!player.isCreative()) {
                    ArrayList modified_attrs = new ArrayList();
                    if (!rote.isRote((ISpellComponent)((ModifiedSpellPart)recipe.getShape()).getPart())) {
                        String shapeName = Component.translatable((String)((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).getRegistryName().toString()).getString();
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.book_of_rote.part_not_rote", (Object[])new Object[]{shapeName}));
                        return InteractionResultHolder.fail(held);
                    }
                    modified_attrs.addAll(((ModifiedSpellPart)recipe.getShape()).getContainedAttributes().stream().filter(a -> ((ModifiedSpellPart)recipe.getShape()).getValueWithoutMultipliers((Attribute)(a)) != ((ModifiedSpellPart)recipe.getShape()).getDefaultValue((Attribute)(a))).collect(Collectors.toList()));
                    for (IModifiedSpellPart<SpellEffect> component : recipe.getComponents()) {
                        if (!rote.isRote(component.getPart())) {
                            String componentName = Component.translatable((String)component.getPart().getRegistryName().toString()).getString();
                            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.book_of_rote.part_not_rote", (Object[])new Object[]{componentName}));
                            return InteractionResultHolder.fail(held);
                        }
                        modified_attrs.addAll(component.getContainedAttributes().stream().filter(a -> component.getValueWithoutMultipliers((Attribute)(a)) != component.getDefaultValue((Attribute)(a))).collect(Collectors.toList()));
                    }
                    for (int m = 0; m < modified_attrs.size(); ++m) {
                        boolean modifier_rote = false;
                        Attribute attr = (Attribute)(modified_attrs.get(m));
                        for (int i = 0; i < rote.getRoteModifiers().size(); ++i) {
                            Modifier mod = rote.getRoteModifiers().get(i);
                            if (mod == null || !mod.modifiesType(attr)) continue;
                            modifier_rote = true;
                            break;
                        }
                        if (modifier_rote) continue;
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.spell.modifier_not_rote", (Object[])new Object[]{attr}));
                        return InteractionResultHolder.fail(held);
                    }
                }
                CompoundTag bookTag = bookStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                bookTag.putInt("cur_spell_use_duration", recipe.getMaxChannelTime());
                bookStack.set(DataComponents.CUSTOM_DATA, CustomData.of(bookTag));
            }
        }
        return super.use(world, player, hand);
    }

    @Override
    public ItemStack getSelectedStack(ItemStack bookStack, @Nullable Player player) {
        SimpleContainer bookInv;
        if (bookStack.getItem() instanceof SpellBook && (bookInv = (SimpleContainer)this.getInventory(bookStack, player != null ? (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get()) : null)) != null) {
            ItemStack selectedStack = bookInv.getItem(BookOfRote.getActiveSpellSlot(bookStack));
            return selectedStack;
        }
        return ItemStack.EMPTY;
    }

    public int getColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 11278103;
    }
}


