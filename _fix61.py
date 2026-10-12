# _fix61.py
import os
import re

S = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rl(t, old, new):
    for eol in ['\n', '\r\n']:
        t = t.replace(old.replace('\n', eol), new.replace('\n', eol))
    return t


print('1 SimulacrumRenderer')
p = S + 'entities/renderers/sorcery/SimulacrumRenderer.java'
t = load(p)
t = t.replace('if (owner != null && (s = ((AbstractClientPlayer)owner).getSkin().model()) == net.minecraft.client.resources.PlayerSkin.Model.SLIM) {',
              'if (owner != null && ((AbstractClientPlayer)owner).getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM) {')
t = t.replace('String s;\n', '')
save(p, t)

print('2 DecoyRenderer')
p = 'src/main/java/com/mna/entities/renderers/sorcery/DecoyRenderer.java'
t = load(p)
t = t.replace('super(context, (EntityModel)new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);',
              'super(context, new PlayerModel<EntityDecoy>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);')
t = t.replace('(s = ((AbstractClientPlayer)owner).getModelName()) == "slim"',
              '((AbstractClientPlayer)owner).getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM')
t = t.replace('protected void renderNameTag(EntityDecoy entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {',
              'protected void renderNameTag(EntityDecoy entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, float partialTick) {')
t = t.replace('super.renderNameTag((Entity)entityIn, entityIn.getPlayer().getDisplayName(), matrixStackIn, bufferIn, packedLightIn);',
              'super.renderNameTag(entityIn, entityIn.getPlayer().getDisplayName(), matrixStackIn, bufferIn, packedLightIn, partialTick);')
save(p, t)

print('3 PotionEffectComponent getEffect')
p = S + 'spells/components/PotionEffectComponent.java'
t = load(p)
t = rl(t, '''    private MobEffect getEffect() {
        return this.effect != null ? this.effect : (MobEffect)this.registry_effect.get();
    }''', '''    private net.minecraft.core.Holder<MobEffect> getEffect() {
        return this.effect != null ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this.effect) : this.registry_effect;
    }''')
save(p, t)

print('4 ManaweavingPattern')
p = 'src/main/java/com/mna/recipes/manaweaving/ManaweavingPattern.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'ManaweavingPattern.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('Object newPoints;', 'Vector3f[] newPoints;')
t = t.replace('((Vector3f[])points)', 'points')
save(p, t)

print('5 PossessionActions')
p = 'src/main/java/com/mna/api/entities/possession/PossessionActions.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'PossessionActions.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('PosessionActionCollection<T> registry = _registeredActions.getOrDefault(entityType, new PosessionActionCollection());',
              'PosessionActionCollection<T> registry = (PosessionActionCollection<T>)_registeredActions.getOrDefault(entityType, new PosessionActionCollection());')
t = t.replace('this.actions.forEach((Consumer<Consumer<T>>)((Consumer<Consumer>)a -> a.accept(e)));',
              'this.actions.forEach(a -> a.accept(e));')
save(p, t)

print('6 PossessionActionInit')
p = 'src/main/java/com/mna/entities/PossessionActionInit.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'PossessionActionInit.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('new LargeFireball(e.level(), (LivingEntity)e, vector3d.x, vector3d.y, vector3d.z, e.getExplosionPower())',
              'new LargeFireball(e.level(), (LivingEntity)e, vector3d, e.getExplosionPower())')
t = t.replace('ProjectileUtil.getMobArrow((LivingEntity)e, (ItemStack)itemstack, (float)BowItem.getPowerForTime((int)40))',
              'ProjectileUtil.getMobArrow((LivingEntity)e, (ItemStack)itemstack, (float)BowItem.getPowerForTime((int)40), e.getMainHandItem())')
t = rl(t, '''            if (e.getMainHandItem().getItem() instanceof BowItem) {
                abstractarrowentity = ((BowItem)e.getMainHandItem().getItem()).customArrow(abstractarrowentity);
            }
''', '')
t = t.replace('e.setHasNectar(true);', '// [PORT 1.21.1] Bee.setHasNectar virou private')
save(p, t)

print('7 MagiciansWorkbenchTile')
p = S + 'blocks/tileentities/wizard_lab/MagiciansWorkbenchTile.java'
t = load(p)
t = t.replace('this.output = recipe.getResultItem(world.registryAccess());', 'this.output = recipe.value().getResultItem(world.registryAccess());')
t = t.replace('this.ingredients = (Ingredient[])recipe.getIngredients().toArray((Object[])new Ingredient[0]);',
              'this.ingredients = (Ingredient[])recipe.value().getIngredients().toArray((Object[])new Ingredient[0]);')
t = t.replace('this.saveAdditional(tag);', 'this.saveAdditional(tag, com.mna.api.tools.MNAItemData.provider());')
save(p, t)

print('8 ConstructHunt')
p = S + 'entities/constructs/ai/ConstructHunt.java'
t = load(p)
t = t.replace('ResourceLocation resourcelocation = living.getLootTable();',
              'net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> resourcelocation = living.getLootTable();')
t = t.replace('LootContextParams.DIRECT_KILLER_ENTITY', 'LootContextParams.DIRECT_ATTACKING_ENTITY')
t = t.replace('LootContextParams.KILLER_ENTITY', 'LootContextParams.ATTACKING_ENTITY')
save(p, t)

print('9 ConstructConstruction')
p = S + 'entities/constructs/animated/ConstructConstruction.java'
t = load(p)
t = t.replace('(rLoc = new ResourceLocation(constructData', '(rLoc = ResourceLocation.parse(constructData')
t = t.replace('for (ItemConstructPart p2 : possibleParts) {', 'for (ItemConstructPart p2 : (java.util.List<ItemConstructPart>)possibleParts) {')
t = t.replace('List sortedTiers = MNAtierSorting.getSortedTiers();', 'java.util.List<net.minecraft.world.item.Tier> sortedTiers = MNAtierSorting.getSortedTiers();')
save(p, t)

print('10 ConstructAssemblyStand')
p = 'src/main/java/com/mna/entities/constructs/ConstructAssemblyStand.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'ConstructAssemblyStand.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);', 'this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0f, 1.0f);')
t = t.replace('EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem((ItemStack)p_31638_);', 'EquipmentSlot equipmentslot = this.getEquipmentSlotForItem((ItemStack)p_31638_);')
t = t.replace('this.dropAllDeathLoot(p_31654_);', 'this.dropAllDeathLoot((net.minecraft.server.level.ServerLevel)this.level(), p_31654_);')
t = t.replace('return p_31615_.height * (this.isBaby() ? 0.5f : 0.9f);', 'return p_31615_.height() * (this.isBaby() ? 0.5f : 0.9f);')
save(p, t)

print('11 ConstructNodeEvaluator')
p = S + 'entities/constructs/movement/ConstructNodeEvaluator.java'
t = load(p)
t = t.replace('return this.getTargetFromNode(this.getNode(Mth.floor((double)x), Mth.floor((double)y), Mth.floor((double)z)));',
              'return new Target(this.getNode(Mth.floor((double)x), Mth.floor((double)y), Mth.floor((double)z)));')
if 'getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    @Override
    public net.minecraft.world.level.pathfinder.PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext ctx, int x, int y, int z, net.minecraft.world.entity.Mob mob) {
        return net.minecraft.world.level.pathfinder.PathType.OPEN;
    }
''' + t[idx:]
save(p, t)

print('12 ClientEventHandler tooltip')
p = S + 'events/ClientEventHandler.java'
t = load(p)
t = rl(t, '''            if (item instanceof IRelic && (c = (relic = (IRelic)item).getHoverAddition(event.getItemStack(), world, event.getToolTip(), event.getFlags())) != null) {
                event.getToolTip().add(1, c);
            }''', '''            if (item instanceof IRelic) {
                relic = (IRelic)item;
                net.minecraft.network.chat.Component hover = relic.getHoverAddition(event.getItemStack(), world, event.getToolTip(), event.getFlags());
                if (hover != null) {
                    event.getToolTip().add(1, hover);
                }
            }''')
t = t.replace('mc.player.noJumpDelay = 10;', '// [PORT 1.21.1] noJumpDelay virou private')
t = t.replace('rwl.getPartialTick()', 'rwl.getPartialTick().getGameTimeDeltaPartialTick(false)')
save(p, t)

print('13 GuiOcculus')
p = S + 'gui/block/GuiOcculus.java'
t = load(p)
t = t.replace('new ListEntry(this, recipe, iconType)', 'new ListEntry(recipe, iconType)')
t = t.replace('list.mouseScrolled(mouseX, mouseY, deltaY / 20.0);', 'list.mouseScrolled(mouseX, mouseY, 0.0, deltaY / 20.0);')
t = re.sub(r'for \(Component comp : lines\) \{', r'for (Component comp : (java.util.List<Component>)lines) {', t)
t = t.replace('super(mcIn, widthIn, heightIn, y, y + heightIn, 50);', 'super(mcIn, widthIn, heightIn, y, 50);')
save(p, t)

print('14 AbstractItemHandler')
p = 'src/main/java/com/mna/inventory/stack_extension/AbstractItemHandler.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'AbstractItemHandler.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('ItemHandlerHelper.canItemStacksStack((ItemStack)stack, (ItemStack)existing)', 'ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)existing)')
t = re.sub(r'ItemHandlerHelper\.copyStackWithSize\(([^,]+), ([^)]+)\)', r'\1.copyWithCount(\2)', t)
save(p, t)

print('15 LootDrop')
p = S + 'tools/loot/LootDrop.java'
t = load(p)
t = t.replace('new ItemStack((ItemLike)item, 1, tag)', 'tagged(new ItemStack((ItemLike)item, 1), tag)')
if 'private static ItemStack tagged(' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] ItemStack(ItemLike,int,CompoundTag) removido. */
    private static ItemStack tagged(ItemStack stack, CompoundTag tag) {
        if (tag != null) {
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
        return stack;
    }
''' + t[idx:]
save(p, t)

print('16 ArcaneCrown')
p = S + 'items/artifice/ArcaneCrown.java'
t = load(p)
t = t.replace('CurioItemCapability.createProvider(new ICurio(){', 'CurioItemCapability.createProvider(new ICurioItem(){')
t = t.replace('ArrayList<MobEffect> toRemove = new ArrayList<MobEffect>();', 'ArrayList<net.minecraft.core.Holder<MobEffect>> toRemove = new ArrayList<net.minecraft.core.Holder<MobEffect>>();')
t = t.replace('inst.getEffect().getCategory()', 'inst.getEffect().value().getCategory()')
t = t.replace('for (MobEffect e : toRemove) {', 'for (net.minecraft.core.Holder<MobEffect> e : toRemove) {')
save(p, t)

print('17 FluidJug')
p = S + 'items/artifice/FluidJug.java'
t = load(p)
if 'SIMPLE_FLUID_COMPONENT' not in t:
    i = t.find('{', t.find('class FluidJug'))
    t = t[:i+1] + '''
    public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS = net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, "mna");
    public static final java.util.function.Supplier<net.minecraft.core.component.DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent>> SIMPLE_FLUID_COMPONENT = COMPONENTS.register("simple_fluid", () -> new net.minecraft.core.component.DataComponentType.Builder<net.neoforged.neoforge.fluids.SimpleFluidContent>().persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC).networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC).build());''' + t[i+1:]
t = t.replace('return new FluidHandlerItemStack(stack, 16000);', 'return new FluidHandlerItemStack(SIMPLE_FLUID_COMPONENT, stack, 16000);')
t = t.replace('java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> handler = FluidUtil.getFluidHandler((ItemStack)itemstack);',
              'java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandlerItem> handler = FluidUtil.getFluidHandler((ItemStack)itemstack);')
t = t.replace('canPlaceLiquid((BlockGetter)worldIn, posIn, blockstate, myFluid.getFluid())', 'canPlaceLiquid((Player)null, (BlockGetter)worldIn, posIn, blockstate, myFluid.getFluid())')
save(p, t)

print('18 MagicStaff')
p = S + 'items/sorcery/MagicStaff.java'
t = load(p)
t = t.replace('super(new Item.Properties().defaultDurability(100).durability(100).setNoRepair());', 'super(new Item.Properties().durability(100));')
t = t.replace('player.broadcastBreakEvent(handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);',
              'player.onEquippedItemBroken(this, handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);')
t = t.replace('return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);', 'return this.attributeModifiers;')
t = re.sub(r'for \(Attribute attr : \(\(ModifiedSpellPart\)randomRecipe\.getShape\(\)\)\.getContainedAttributes\(\)\) \{',
           r'for (Attribute attr : (java.util.List<Attribute>)((ModifiedSpellPart)randomRecipe.getShape()).getContainedAttributes()) {', t)
save(p, t)

print('19 IMultiblockDefinition generic')
import subprocess
f = subprocess.run(['grep', '-rln', 'interface IMultiblockDefinition', 'src/main/java/'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(f)
t = t.replace('extends Recipe<CraftingContainer>', 'extends Recipe<net.minecraft.world.item.crafting.CraftingInput>')
save(f, t)
print('   ok', f)

print('20 MultiblockDefinition valid list')
p = S + 'recipes/multiblock/MultiblockDefinition.java'
t = load(p)
t = t.replace('List<Object> valid = this.getValidItemsFor((Block)e.getKey());', 'List<ItemStack> valid = this.getValidItemsFor((Block)e.getKey());')
save(p, t)

print('21 ManaAndArtifice register components')
p = S + 'ManaAndArtifice.java'
t = load(p)
if 'FluidJug.COMPONENTS.register' not in t:
    t = t.replace('ItemInit.ITEMS.register(this.modEventBus);', 'ItemInit.ITEMS.register(this.modEventBus);\n        com.mna.items.artifice.FluidJug.COMPONENTS.register(this.modEventBus);')
    save(p, t)
print('FIM')
