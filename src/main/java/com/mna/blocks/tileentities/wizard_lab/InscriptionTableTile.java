 
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.Registries;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.spells.SpellValidityResult;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.BlockInit;
import com.mna.blocks.sorcery.InscriptionTableBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.gui.containers.block.ContainerInscriptionTable;
import com.mna.items.ItemInit;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_server.InscriptionTableAttributeChangeMessage;
import com.mna.network.messages.to_server.InscriptionTableCraftingUpdateMessage;
import com.mna.network.messages.to_server.InscriptionTableRequestStartCraftingMessage;
import com.mna.network.messages.to_server.InscriptionTableSetComponentMessage;
import com.mna.network.messages.to_server.InscriptionTableSetModifierMessage;
import com.mna.network.messages.to_server.InscriptionTableSetShapeMessage;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class InscriptionTableTile
extends TileEntityWithInventory
implements MenuProvider,
Consumer<FriendlyByteBuf>,
GeoBlockEntity {
    private static final int SIZE_INVENTORY = 4;
    private static final int SLOT_INK = 0;
    private static final int SLOT_PAPER = 1;
    private static final int SLOT_FUEL = 2;
    private static final int SLOT_OUTPUT = 3;
    private static final int TICKS_PER_FUEL = 160;
    private static final int SCRIBE_TICKS_PER_COMPLEXITY = 20;
    private SpellRecipe recipe;
    private boolean isCrafting = false;
    private int craftTicksTotal = 0;
    private int craftTicksConsumed = 0;
    private int burnTicksRemaining = 0;
    private int inkDamageRemaining = 0;
    private int paperRemaining = 0;
    private int fuelRemaining = 0;
    private boolean creative;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public InscriptionTableTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 4);
        this.recipe = new SpellRecipe();
    }

    public static void ServerTick(Level level, BlockPos pos, BlockState state, InscriptionTableTile tile) {
        if (tile.isCrafting) {
            if (!tile.creative) {
                if (tile.paperRemaining > 0 || tile.inkDamageRemaining > 0) {
                    tile.consumeMaterials();
                    return;
                }
                if (tile.burnTicksRemaining <= 5) {
                    if (!level.isClientSide()) {
                        tile.tryConsumeFuel();
                    }
                    return;
                }
            }
            ++tile.craftTicksConsumed;
            --tile.burnTicksRemaining;
            if (!level.isClientSide()) {
                if (tile.craftTicksConsumed >= tile.craftTicksTotal) {
                    tile.completeSpellPart();
                } else if (tile.craftTicksConsumed > 0 && tile.craftTicksConsumed % 20 == 0) {
                    ServerMessageDispatcher.sendInscriptionTableCraftingUpdate(tile);
                }
            }
        }
    }

    public void setCreative(boolean creative) {
        this.creative = creative;
    }

    public boolean isCraftingPaused() {
        return this.isCrafting && (this.paperRemaining > 0 || this.inkDamageRemaining > 0);
    }

    private void completeSpellPart() {
        this.isCrafting = false;
        this.burnTicksRemaining = 0;
        this.craftTicksConsumed = 0;
        this.craftTicksTotal = 0;
        ItemStack outputStack = this.creative ? new ItemStack((ItemLike)ItemInit.SPELL.get()) : new ItemStack((ItemLike)ItemInit.ENCHANTED_VELLUM.get());
        this.recipe.setMysterious(false);
        this.recipe.writeToNBT(outputStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.recipe.writeRecipeForRitual(this.level, outputStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.setItem(3, outputStack);
        ServerMessageDispatcher.sendInscriptionTableCraftingUpdate(this);
        this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
    }

    private void tryConsumeFuel() {
        ItemStack consumeStack = this.removeItem(2, 1);
        if (!consumeStack.isEmpty()) {
            this.burnTicksRemaining = 160;
            ServerMessageDispatcher.sendInscriptionTableCraftingUpdate(this);
        }
    }

    public SpellValidityResult canBeginCrafting(IPlayerProgression progression) {
        if (this.isCrafting) {
            return SpellValidityResult.ALREADY_CRAFTING;
        }
        if (!this.getItem(3).isEmpty()) {
            return SpellValidityResult.OUTPUT_FULL;
        }
        if (this.getCurrentShape() == null) {
            return SpellValidityResult.NO_SHAPE;
        }
        if (this.getCurrentComponent() == null) {
            return SpellValidityResult.NO_COMPONENT;
        }
        if (!(!this.getCurrentShape().getPart().isChanneled() && this.getCurrentShape().getPart().allowChanneledComponents() || this.getCurrentComponent().getPart().canBeChanneled())) {
            return SpellValidityResult.CANNOT_CHANNEL;
        }
        if (this.creative) {
            return SpellValidityResult.READY;
        }
        if (progression.getTier() < this.getCurrentRecipe().getTier(this.level)) {
            return SpellValidityResult.LOW_TIER;
        }
        if (this.getCurrentRecipe().getComplexity() > (float)progression.getTierMaxComplexity()) {
            return SpellValidityResult.TOO_COMPLEX;
        }
        if (!this.getCurrentRecipe().canFactionCraft(progression)) {
            return SpellValidityResult.WRONG_FACTION;
        }
        this.calculateMaterialRequirements();
        if (this.getItem(1).getCount() < this.paperRemaining) {
            return SpellValidityResult.MISSING_PAPER;
        }
        if (this.getItem(0).getMaxDamage() - this.getItem(0).getDamageValue() < this.inkDamageRemaining) {
            return SpellValidityResult.MISSING_INK;
        }
        if (this.getItem(2).getCount() == 0) {
            return SpellValidityResult.MISSING_ASH;
        }
        return SpellValidityResult.READY;
    }

    private void consumeMaterials() {
        int availableInk;
        ItemStack ink;
        boolean updated = false;
        ItemStack paperconsumed = this.removeItem(1, this.paperRemaining);
        if (!paperconsumed.isEmpty()) {
            this.paperRemaining -= paperconsumed.getCount();
            updated = true;
        }
        if (!(ink = this.getItem(0)).isEmpty() && (availableInk = this.getItem(0).getMaxDamage() - this.getItem(0).getDamageValue()) > 0) {
            if (availableInk > this.inkDamageRemaining) {
                this.getItem(0).setDamageValue(this.getItem(0).getDamageValue() + this.inkDamageRemaining);
                this.inkDamageRemaining = 0;
            } else {
                this.inkDamageRemaining -= availableInk;
                this.getItem(0).setDamageValue(this.getItem(0).getMaxDamage());
            }
            updated = true;
        }
        if (updated) {
            ServerMessageDispatcher.sendInscriptionTableCraftingUpdate(this);
        }
    }

    private void calculateMaterialRequirements() {
        this.paperRemaining = (int)Math.ceil(this.getComplexity() / 5.0f);
        this.inkDamageRemaining = (int)Math.ceil(this.getComplexity() / 2.5f);
        this.fuelRemaining = (int)Math.ceil((float)((int)this.getComplexity() * 20) / 160.0f);
    }

    public InscriptionTableTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.INSCRIPTION_TABLE.get(), pos, state);
    }

    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerInscriptionTable(id, playerInventory, this);
    }

    public Component getDisplayName() {
        return Component.translatable((String)"mna:container.inscription_table");
    }

    public void setCurrentShape(Shape shape) {
        if (this.isCrafting) {
            return;
        }
        this.recipe.setShape(shape);
        if (this.hasLevel()) {
            if (!this.level.isClientSide()) {
                this.setChanged();
            } else {
                ClientMessageDispatcher.sendInscriptionTableSetShape(this);
            }
        }
        this.calculateMaterialRequirements();
    }

    public void setCurrentComponent(SpellEffect component) {
        if (this.isCrafting) {
            return;
        }
        this.recipe.clearComponents();
        if (component != null) {
            this.recipe.addComponent(component);
        }
        if (this.hasLevel()) {
            if (!this.level.isClientSide()) {
                this.setChanged();
            } else {
                ClientMessageDispatcher.sendInscriptionTableSetComponent(this);
            }
        }
        this.calculateMaterialRequirements();
    }

    public void setCurrentModifier(Modifier modifier, int index) {
        if (this.isCrafting) {
            return;
        }
        this.recipe.setModifier(modifier, index);
        if (this.hasLevel()) {
            if (this.level.isClientSide()) {
                ClientMessageDispatcher.sendInscriptionTableSetModifier(this, modifier != null ? modifier.getRegistryName() : ResourceLocation.parse(""), index);
            } else {
                this.setChanged();
            }
        }
        this.calculateMaterialRequirements();
    }

    public SpellRecipe getCurrentRecipe() {
        return this.recipe;
    }

    public ModifiedSpellPart<Shape> getCurrentShape() {
        return this.recipe.getShape();
    }

    public ModifiedSpellPart<SpellEffect> getCurrentComponent() {
        return this.recipe.getComponent(0);
    }

    public Modifier getCurrentModifier(int index) {
        return this.recipe.getModifier(index);
    }

    public void build() {
        if (this.recipe.isValid() && this.getItem(3).isEmpty()) {
            if (this.level.isClientSide()) {
                ClientMessageDispatcher.sendInscriptionTableRequestStartCrafting(this);
            } else {
                this.isCrafting = true;
                this.craftTicksTotal = this.creative ? 5 : (int)this.getComplexity() * 20;
                this.craftTicksConsumed = 0;
                if (this.hasLevel() && !this.level.isClientSide()) {
                    this.calculateMaterialRequirements();
                    ServerMessageDispatcher.sendInscriptionTableCraftingUpdate(this);
                    this.setChanged();
                    this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
                }
            }
        }
    }

    public boolean isBuilding() {
        return this.isCrafting;
    }

    public void changeShapeAttributeValue(Attribute attribute, float newValue) {
        if (this.recipe.changeShapeAttributeValue(attribute, newValue) && this.hasLevel()) {
            if (this.level.isClientSide()) {
                ClientMessageDispatcher.sendInscriptionTableAttributeChange(this, attribute, newValue, InscriptionTableAttributeChangeMessage.ChangeType.SHAPE);
            } else {
                this.setChanged();
            }
        }
        this.calculateMaterialRequirements();
    }

    public void changeComponentAttributeValue(Attribute attribute, float newValue) {
        if (this.recipe.changeComponentAttributeValue(0, attribute, newValue) && this.hasLevel()) {
            if (this.level.isClientSide()) {
                ClientMessageDispatcher.sendInscriptionTableAttributeChange(this, attribute, newValue, InscriptionTableAttributeChangeMessage.ChangeType.COMPONENT);
            } else {
                this.setChanged();
            }
        }
        this.calculateMaterialRequirements();
    }

    public float getComplexity() {
        return this.recipe.getComplexity();
    }

    public float getManaCost(Player player) {
        this.recipe.calculateManaCost();
        SpellCaster.applyAdjusters(ItemStack.EMPTY, (LivingEntity)player, InteractionHand.MAIN_HAND, false, this.recipe, SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE);
        return this.recipe.isChanneled() ? this.recipe.getManaCost() * 20.0f : this.recipe.getManaCost();
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        CompoundTag recipeData = new CompoundTag();
        this.recipe.writeToNBT(recipeData);
        compound.put("recipe_data", (Tag)recipeData);
        compound.putBoolean("isCrafting", this.isCrafting);
        compound.putInt("craftTicksTotal", this.craftTicksTotal);
        compound.putInt("craftTicksConsumed", this.craftTicksConsumed);
        compound.putInt("burnTicksRemaining", this.burnTicksRemaining);
        compound.putInt("inkDamageRemaining", this.inkDamageRemaining);
        compound.putInt("paperRemaining", this.paperRemaining);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        if (compound.contains("recipe_data")) {
            this.recipe = SpellRecipe.fromNBT(compound.getCompound("recipe_data"));
        }
        this.isCrafting = compound.getBoolean("isCrafting");
        this.craftTicksTotal = compound.getInt("craftTicksTotal");
        this.craftTicksConsumed = compound.getInt("craftTicksConsumed");
        this.burnTicksRemaining = compound.getInt("burnTicksRemaining");
        this.inkDamageRemaining = compound.getInt("inkDamageRemaining");
        this.paperRemaining = compound.getInt("paperRemaining");
        super.loadAdditional(compound, registries);
    }

    @Override
    public void accept(FriendlyByteBuf data) {
        data.writeBlockPos(this.getBlockPos());
        CompoundTag nbt = new CompoundTag();
        this.recipe.writeToNBT(nbt);
        data.writeNbt(nbt);
        data.writeInt(this.craftTicksTotal);
        data.writeInt(this.craftTicksConsumed);
        data.writeInt(this.burnTicksRemaining);
        data.writeInt(this.inkDamageRemaining);
        data.writeInt(this.paperRemaining);
    }

    public InscriptionTableTile readFrom(FriendlyByteBuf data) {
        this.recipe = SpellRecipe.fromNBT(data.readNbt());
        this.craftTicksTotal = data.readInt();
        this.craftTicksConsumed = data.readInt();
        this.burnTicksRemaining = data.readInt();
        this.inkDamageRemaining = data.readInt();
        this.paperRemaining = data.readInt();
        return this;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public void handleUpdateTag(CompoundTag tag) {
        this.loadAdditional(tag, com.mna.api.tools.MNAItemData.provider());
    }

    public int getCraftTicks() {
        return this.craftTicksTotal;
    }

    public int getCraftTicksConsumed() {
        return this.craftTicksConsumed;
    }

    public int getBurnTicksRemaining() {
        return this.burnTicksRemaining;
    }

    public int getPaperRemaining() {
        return this.paperRemaining;
    }

    public int getInkRemaining() {
        return this.inkDamageRemaining;
    }

    public int getFuelRemaining() {
        return this.fuelRemaining;
    }

    public void setChanged() {
        super.setChanged();
        int modelStateFlag = 0;
        if (!this.getItem(0).isEmpty()) {
            modelStateFlag |= 1;
        }
        if (!this.getItem(1).isEmpty()) {
            modelStateFlag |= 2;
        }
        if (!this.getItem(2).isEmpty()) {
            modelStateFlag |= 4;
        }
        if (this.level != null) {
            this.level.setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)InscriptionTableBlock.CONTAINED_RESOURCES, (Comparable)Integer.valueOf(modelStateFlag)));
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN || side == Direction.UP) {
            return new int[]{3};
        }
        return new int[]{0, 1, 2};
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !this.isCrafting && direction != Direction.UP && direction != Direction.DOWN;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.isCrafting) {
            return false;
        }
        ItemStack existing = this.getItem(index);
        switch (index) {
            case 0: {
                return stack.getItem() == ItemInit.ARCANIST_INK.get() && existing.isEmpty();
            }
            case 1: {
                return stack.getItem() == ItemInit.VELLUM.get() && (existing.isEmpty() || existing.getCount() < existing.getMaxStackSize());
            }
            case 2: {
                return stack.getItem() == ItemInit.ARCANE_ASH.get() && (existing.isEmpty() || existing.getCount() < existing.getMaxStackSize());
            }
        }
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> {
            if (this.isBuilding() && !this.isCraftingPaused()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.inscription_table.quillwrite"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.inscription_table.quillidle"));
        })});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    private static InscriptionTableTile getAndVerify(Level world, BlockPos position) {
        BlockEntity te;
        if (world.isLoaded(position) && world.getBlockState(position).getBlock() == BlockInit.INSCRIPTION_TABLE.get() && (te = world.getBlockEntity(position)) != null && te instanceof InscriptionTableTile) {
            return (InscriptionTableTile)te;
        }
        return null;
    }

    public static final void handleShapeSet(ServerPlayer sendingPlayer, InscriptionTableSetShapeMessage message) {
        InscriptionTableTile te = InscriptionTableTile.getAndVerify(sendingPlayer.level(), message.getPosition());
        if (te == null) {
            return;
        }
        Shape spellShape = (Shape)Registries.Shape.getValue(message.getShape());
        te.setCurrentShape(spellShape);
    }

    public static final void handleComponentSet(ServerPlayer sendingPlayer, InscriptionTableSetComponentMessage message) {
        InscriptionTableTile te = InscriptionTableTile.getAndVerify(sendingPlayer.level(), message.getPosition());
        if (te == null) {
            return;
        }
        SpellEffect spellComponent = (SpellEffect)Registries.SpellEffect.getValue(message.getComponent());
        te.setCurrentComponent(spellComponent);
    }

    public static final void handleModifierSet(ServerPlayer sendingPlayer, InscriptionTableSetModifierMessage message) {
        if (message.getIndex() < 0 || message.getIndex() >= 3) {
            return;
        }
        InscriptionTableTile te = InscriptionTableTile.getAndVerify(sendingPlayer.level(), message.getPosition());
        if (te == null) {
            return;
        }
        Modifier spellModifier = (Modifier)Registries.Modifier.getValue(message.getModifier());
        te.setCurrentModifier(spellModifier, message.getIndex());
    }

    public static final void handleAttributeValueChange(ServerPlayer sendingPlayer, InscriptionTableAttributeChangeMessage message) {
        InscriptionTableTile te = InscriptionTableTile.getAndVerify(sendingPlayer.level(), message.getPosition());
        if (te == null) {
            return;
        }
        switch (message.getChangeType()) {
            case COMPONENT: {
                te.changeComponentAttributeValue(message.getAttribute(), message.getValue());
                break;
            }
            case SHAPE: {
                te.changeShapeAttributeValue(message.getAttribute(), message.getValue());
            }
        }
    }

    public static final void handleRequestStartCrafting(ServerPlayer sendingPlayer, InscriptionTableRequestStartCraftingMessage message) {
        InscriptionTableTile te = InscriptionTableTile.getAndVerify(sendingPlayer.level(), message.getPosition());
        if (te == null) {
            return;
        }
        if (!te.isBuilding()) {
            te.build();
        }
    }

    public static final void handleCraftingUpdate(Level world, InscriptionTableCraftingUpdateMessage message) {
        InscriptionTableTile te = InscriptionTableTile.getAndVerify(world, message.getPosition());
        if (te == null) {
            return;
        }
        te.isCrafting = message.getBurnTimeRemaining() > 0;
        te.craftTicksTotal = message.getTotalCraftTicks();
        te.craftTicksConsumed = message.getCraftTicksPassed();
        te.burnTicksRemaining = message.getBurnTimeRemaining();
        te.inkDamageRemaining = message.getInkRemaining();
        te.paperRemaining = message.getPaperRemaining();
    }
}

