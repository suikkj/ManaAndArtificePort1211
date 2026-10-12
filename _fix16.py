# _fix16.py - ContainerMagiciansWorkbench (RecipeBookMenu 2-arg), LargeContainer (reflexao), CantripRegistry
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = new.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
    return t.replace(o, n)


def add_import(t, imp):
    line = 'import ' + imp + ';'
    if line in t:
        return t
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    idx = None
    for i, l in enumerate(lines):
        if l.startswith('import '):
            idx = i
    lines.insert(idx + 1, line)
    return sep.join(lines)


# ============================================================
# 1) ContainerMagiciansWorkbench
# ============================================================
print('== ContainerMagiciansWorkbench ==')
p = 'src/main/java/com/mna/gui/containers/block/ContainerMagiciansWorkbench.java'
t = load(p)
t = rep(t, 'extends RecipeBookMenu<CraftingContainer> {', 'extends RecipeBookMenu<CraftingInput, CraftingRecipe> {', 'cmw decl')
t = rep(t, '''    protected void updateCraftingResult(int id, Level world, Player player, CraftingContainer inventory, ResultContainer inventoryResult, int syncSlot) {
        if (!world.isClientSide) {
            CraftingRecipe icraftingrecipe;
            ServerPlayer serverplayerentity = (ServerPlayer)player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (Container)inventory, world);
            if (optional.isPresent() && inventoryResult.setRecipeUsed(world, serverplayerentity, (Recipe)(icraftingrecipe = (CraftingRecipe)optional.get()))) {
                itemstack = icraftingrecipe.assemble((Container)inventory, world.registryAccess());
            }
            inventoryResult.setItem(0, itemstack);
            serverplayerentity.connection.send((Packet)new ClientboundContainerSetSlotPacket(id, this.incrementStateId(), syncSlot, itemstack));
        }
    }''',
        '''    protected void updateCraftingResult(int id, Level world, Player player, CraftingContainer inventory, ResultContainer inventoryResult, int syncSlot) {
        if (!world.isClientSide) {
            ServerPlayer serverplayerentity = (ServerPlayer)player;
            ItemStack itemstack = ItemStack.EMPTY;
            CraftingInput craftingInput = inventory.asCraftInput();
            Optional<RecipeHolder<CraftingRecipe>> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, world);
            if (optional.isPresent()) {
                inventoryResult.setRecipeUsed(optional.get());
                itemstack = optional.get().value().assemble(craftingInput, world.registryAccess());
            }
            inventoryResult.setItem(0, itemstack);
            serverplayerentity.connection.send((Packet)new ClientboundContainerSetSlotPacket(id, this.incrementStateId(), syncSlot, itemstack));
        }
    }''', 'cmw updateCraftingResult')
t = rep(t, '''    public boolean recipeMatches(Recipe<? super CraftingContainer> p_39719_) {
        return p_39719_.matches((Container)this.firstCraftMatrix, this.playerInv.player.level());
    }''',
        '''    public boolean recipeMatches(RecipeHolder<CraftingRecipe> p_39719_) {
        return p_39719_.value().matches(this.firstCraftMatrix.asCraftInput(), this.playerInv.player.level());
    }''', 'cmw recipeMatches')
t = t.replace('ItemStack.isSameItemSameTags(', 'ItemStack.isSameItemSameComponents(')
t = add_import(t, 'net.minecraft.world.item.crafting.CraftingInput')
t = add_import(t, 'net.minecraft.world.item.crafting.RecipeHolder')
save(p, t)

# ============================================================
# 2) LargeContainer: campos privados -> MNAMenuAccess
# ============================================================
print('== LargeContainer ==')
p = 'src/main/java/com/mna/gui/containers/LargeContainer.java'
t = load(p)
# atribuicoes primeiro
t = re.sub(r'this\.quickcraftStatus = ([^;]+);', r'MNAMenuAccess.setQuickcraftStatus(this, \1);', t)
t = re.sub(r'this\.quickcraftType = ([^;]+);', r'MNAMenuAccess.setQuickcraftType(this, \1);', t)
t = t.replace('this.quickcraftStatus', 'MNAMenuAccess.quickcraftStatus(this)')
t = t.replace('this.quickcraftType', 'MNAMenuAccess.quickcraftType(this)')
t = t.replace('this.quickcraftSlots', 'MNAMenuAccess.quickcraftSlots(this)')
t = t.replace('this.lastSlots', 'MNAMenuAccess.lastSlots(this)')
t = t.replace('this.containerListeners', 'MNAMenuAccess.containerListeners(this)')
t = t.replace('this.dataSlots', 'MNAMenuAccess.dataSlots(this)')
save(p, t)
print('  ok')

# ============================================================
# 3) CantripRegistry
# ============================================================
print('== CantripRegistry ==')
p = 'src/main/java/com/mna/cantrips/CantripRegistry.java'
t = load(p)
n = t.count('(TriConsumer<Player, ICantrip, InteractionHand>)((TriConsumer)CantripRegistry::')
t = t.replace('(TriConsumer<Player, ICantrip, InteractionHand>)((TriConsumer)CantripRegistry::',
              '(TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::')
print('  triConsumer:', n)
t = t.replace('c.getStack().getTag()', 'c.getStack().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag()')
t = t.replace('player.curePotionEffects(new ItemStack((ItemLike)Items.MILK_BUCKET));',
              'player.removeEffectsCuredBy(net.neoforged.neoforge.common.EffectCure.MILK);')
t = t.replace('.pickupBlock((LevelAccessor)world, blockpos1, blockstate)',
              '.pickupBlock(player, (LevelAccessor)world, blockpos1, blockstate)')
save(p, t)
print('  ok')

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
