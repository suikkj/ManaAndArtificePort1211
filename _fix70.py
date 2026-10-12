# _fix70.py
import os, re

def find1(name):
    for dp, _, fns in os.walk('src/main/java'):
        if name in fns:
            return os.path.join(dp, name)
    return None

def load(p):
    return open(p, encoding='utf-8', newline='').read()

def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)

def rl(t, old, new):
    for eol in ['\n', '\r\n']:
        t = t.replace(old.replace('\n', eol), new.replace('\n', eol))
    return t

# 1 WitchHunterArrow
p = find1('WitchHunterArrow.java')
t = load(p)
if 'import net.minecraft.world.level.ItemLike;' not in t:
    t = re.sub(r'(import [^\n]*;\r?\n)', r'\1import net.minecraft.world.level.ItemLike;\n', t, count=1)
t = t.replace('protected ItemStack getPickupItem() {', 'protected ItemStack getDefaultPickupItem() {')
save(p, t)
print('1 ok')

# 2 ContainerRunicMalus
p = find1('ContainerRunicMalus.java')
t = load(p)
t = t.replace('HashMap<Enchantment, Float> bonuses = new HashMap<Enchantment, Float>();',
              'HashMap<net.minecraft.core.Holder<Enchantment>, Float> bonuses = new HashMap<net.minecraft.core.Holder<Enchantment>, Float>();')
t = t.replace('bonuses.put((Enchantment)ench, Float.valueOf(((Float)bonuses.get(ench)).floatValue() + bonus));',
              'bonuses.put((net.minecraft.core.Holder<Enchantment>)ench, Float.valueOf(((Float)bonuses.get(ench)).floatValue() + bonus));')
t = t.replace('EnchantmentHelper.setEnchantments(chants, (ItemStack)this.inventory.getStack());',
'''net.minecraft.world.item.enchantment.ItemEnchantments.Mutable chMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            chants.forEach(chMut::set);
            ((ItemStack)this.inventory.getStack()).set(DataComponents.ENCHANTMENTS, chMut.toImmutable());''')
save(p, t)
print('2 ok')

# 3 TrickeryBracelet import
p = find1('TrickeryBracelet.java')
t = load(p)
if 'import net.minecraft.resources.ResourceLocation;' not in t:
    t = re.sub(r'(import [^\n]*;\r?\n)', r'\1import net.minecraft.resources.ResourceLocation;\n', t, count=1)
save(p, t)
print('3 ok')

# 4 BoundAxe
p = find1('BoundAxe.java')
t = load(p)
t = t.replace('super((Tier)Tiers.DIAMOND, attackDamageIn, attackSpeedIn, new Item.Properties());', 'super((Tier)Tiers.DIAMOND, new Item.Properties());')
t = t.replace('Multimap baseAttrs = super.getAttributeModifiers(slot, stack);', 'Multimap baseAttrs = super.getDefaultAttributeModifiers();')
t = re.sub(r'new AttributeModifier\(BASE_RANGE_UUID, ', r'new AttributeModifier(BASE_RANGE_ID, ', t)
t = re.sub(r'private static final UUID BASE_RANGE_UUID = UUID\.fromString\("([0-9a-fA-F-]+)"\);',
           r'private static final ResourceLocation BASE_RANGE_ID = ResourceLocation.fromNamespaceAndPath("mna", "\1");', t)
save(p, t)
print('4 ok')

# 5 ItemAndPatternRecipeHelper
p = find1('ItemAndPatternRecipeHelper.java')
t = load(p)
t = t.replace('List iapList = world.getRecipeManager().getRecipesFor(type, (Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY), world);',
              'List<net.minecraft.world.item.crafting.RecipeHolder<T>> iapList = world.getRecipeManager().getRecipesFor(type, net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY)), world);')
t = t.replace('''        for (ItemAndPatternRecipe recipe : iapList) {
            if (recipe.getId().compareTo(rLoc) != 0) continue;
            return (T)recipe;
        }'''.replace('\n', '\r\n'),
'''        for (net.minecraft.world.item.crafting.RecipeHolder<T> __h : iapList) {
            ItemAndPatternRecipe recipe = __h.value();
            if (recipe.getId().compareTo(rLoc) != 0) continue;
            return (T)recipe;
        }'''.replace('\n', '\r\n'))
t = t.replace('''        for (ItemAndPatternRecipe recipe : iapList) {
            if (recipe.getId().compareTo(rLoc) != 0) continue;
            return (T)recipe;
        }''',
'''        for (net.minecraft.world.item.crafting.RecipeHolder<T> __h : iapList) {
            ItemAndPatternRecipe recipe = __h.value();
            if (recipe.getId().compareTo(rLoc) != 0) continue;
            return (T)recipe;
        }''')
t = t.replace('return world.getRecipeManager().getRecipesFor(type, (Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY), world);',
              'return world.getRecipeManager().getRecipesFor(type, net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY)), world).stream().map(h -> h.value()).collect(java.util.stream.Collectors.toList());')
save(p, t)
print('5 ok')

# 6 RitualRecipeHelper
p = find1('RitualRecipeHelper.java')
t = load(p)
t = t.replace('List collection = world.getRecipeManager().getRecipesFor(RecipeInit.RITUAL_TYPE.get(), null, world).stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList());',
              'List<RitualRecipe> collection = world.getRecipeManager().getRecipesFor(RecipeInit.RITUAL_TYPE.get(), null, world).stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList());')
save(p, t)
print('6 ok')

# 7 BlockUtils
p = find1('BlockUtils.java')
t = load(p)
t = t.replace('        return !NeoForge.EVENT_BUS.post(event);',
              '        NeoForge.EVENT_BUS.post(event);\n        return !event.isCanceled();')
t = t.replace('        if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), playerEntity))) {',
'''        BlockEvent.BreakEvent __breakEvt = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), playerEntity);
        NeoForge.EVENT_BUS.post(__breakEvt);
        if (__breakEvt.isCanceled()) {''')
t = t.replace('return (S)((StateHolder)pStateHolder.setValue(pProperty, optional.get()));',
              'return (S)((StateHolder)pStateHolder.setValue(pProperty, (Comparable)optional.get()));')
save(p, t)
print('7 ok')

# 8 InfiniteFluidCapabilityHandler
p = find1('InfiniteFluidCapabilityHandler.java')
t = load(p)
t = re.sub(r'import net\.neoforged\.neoforge\.capabilities\.Capability;\r?\n', '', t)
t = rl(t, '''public class InfiniteFluidCapabilityHandler
implements IFluidHandlerItem,
ICapabilityProvider {''', '''public class InfiniteFluidCapabilityHandler
implements IFluidHandlerItem {''')
save(p, t)
print('8 ok')

# 9 BoundBow GeckoLibNetwork
p = find1('BoundBow.java')
t = load(p)
t = re.sub(r'import software\.bernie\.geckolib\.network\.GeckoLibNetwork;\r?\n', '', t)
t = t.replace('GeckoLibNetwork.registerSyncedAnimatable((GeoAnimatable)this);', '// [PORT] GeckoLibNetwork removido no GeckoLib 4.x')
save(p, t)
print('9 ok')

# 10 RecipeFumeFilter
p = find1('RecipeFumeFilter.java')
t = load(p)
t = t.replace('Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.resolveRecipe(rLoc);', 'Optional pattern = this.resolveRecipe(rLoc);')
t = t.replace('Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.resolveRecipe(recipeLocation);', 'Optional pattern = this.resolveRecipe(recipeLocation);')
t = t.replace('return (Optional)this.minecraft.level.getRecipeManager().byKey(recipeLocation);', 'return (Optional)this.minecraft.level.getRecipeManager().byKey(recipeLocation);')
save(p, t)
print('10 ok')
print('FIM')
