# _fix60.py - lote grande
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


print('== broadcastBreakEvent -> onEquippedItemBroken (global) ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'hurtAndBreak\(([^,]+), ([^,]+), ([^,]+), (\w+) -> \4\.broadcastBreakEvent\(([^)]+)\)\)',
                   r'hurtAndBreak(\1, \2, \3, brokenItem -> \3.onEquippedItemBroken(brokenItem, \5))', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== PortalRenderer ==')
p = S + 'entities/renderers/ritual/PortalRenderer.java'
t = load(p)
t = t.replace('nArray[0] = (int)(dc.getTextureDiffuseColors()[0] * 255.0f);', 'nArray[0] = (dc.getTextureDiffuseColor() >> 16) & 0xFF;')
t = t.replace('nArray[1] = (int)(dc.getTextureDiffuseColors()[1] * 255.0f);', 'nArray[1] = (dc.getTextureDiffuseColor() >> 8) & 0xFF;')
t = t.replace('nArray[2] = (int)(dc.getTextureDiffuseColors()[2] * 255.0f);', 'nArray[2] = dc.getTextureDiffuseColor() & 0xFF;')
t = t.replace('v1 = nArray;', 'portalDye = nArray;')
t = t.replace('v1 = portalDye = null;', 'portalDye = null;')
save(p, t)
print('  ok')

print('== HUDOverlayRenderer ==')
p = S + 'gui/HUDOverlayRenderer.java'
t = load(p)
t = t.replace('java.util.Optional<Object> lazyCurios = CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity)this.mc.player);',
              'var lazyCurios = CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity)this.mc.player);')
t = t.replace('RenderSystem.setShader(GameRenderer::getPositionColorTexShader);', 'RenderSystem.setShader(GameRenderer::getPositionTexColorShader);')
t = t.replace('FastColor.ARGB32.setColor((int)255, (int)r, (int)g, (int)b)', 'FastColor.ARGB32.color((int)255, (int)r, (int)g, (int)b)')
t = re.sub(r'for \(Map\.Entry aff : affs\) \{', r'for (Object __e : affs) {\n            Map.Entry aff = (Map.Entry)__e;', t)
save(p, t)
print('  ok')

print('== ConstructBreed ==')
p = S + 'entities/constructs/ai/ConstructBreed.java'
t = load(p)
if 'import net.minecraft.world.entity.EntityType;' not in t:
    t = t.replace('import net.minecraft.world.entity.Animal;', 'import net.minecraft.world.entity.Animal;\nimport net.minecraft.world.entity.EntityType;', 1)
t = t.replace('Optional<List> target = locatedEntities.entrySet().stream()', 'Optional target = locatedEntities.entrySet().stream()')
t = t.replace('List potentials = target.get();', 'List<Animal> potentials = (List<Animal>)target.get();')
save(p, t)
print('  ok')

print('== Chatterer ==')
p = S + 'entities/faction/Chatterer.java'
t = load(p)
t = t.replace('ArrayList spawns = new ArrayList();', 'ArrayList<net.minecraft.world.entity.Mob> spawns = new ArrayList<net.minecraft.world.entity.Mob>();')
t = t.replace('for (Player p2 : players) {', 'for (Player p2 : (java.util.List<Player>)players) {')
save(p, t)
print('  ok')

print('== GreaterAnimus ==')
p = S + 'entities/summon/GreaterAnimus.java'
t = load(p)
t = t.replace('''        if (this.getMainHandItem().getItem() instanceof BowItem) {
            abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
        }
''', '')
t = t.replace('ProjectileUtil.getMobArrow((LivingEntity)this, (ItemStack)pArrowStack, (float)pDistanceFactor)',
              'ProjectileUtil.getMobArrow((LivingEntity)this, (ItemStack)pArrowStack, (float)pDistanceFactor, this.getMainHandItem())')
t = t.replace('this.dropAllDeathLoot(this.damageSources().fellOutOfWorld());',
              'this.dropAllDeathLoot((net.minecraft.server.level.ServerLevel)this.level(), this.damageSources().fellOutOfWorld());')
save(p, t)
print('  ok')

print('== ConstructPartAxeArm (optional3) ==')
for f in ['ConstructPartAxeArmLeft', 'ConstructPartAxeArmRight']:
    p = S + 'items/constructs/parts/arms/' + f + '.java'
    t = load(p)
    t = t.replace('Optional<Object> optional3 = Optional.empty();', 'Optional<BlockState> optional3 = Optional.empty();')
    save(p, t)
print('  ok')

print('== MNAEnchantmentHelper Holder overload ==')
p = S + 'api/enchantments/MNAEnchantmentHelper.java'
t = load(p)
if 'getItemEnchantmentLevel(net.minecraft.core.Holder<Enchantment> holder' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] overload para DeferredHolder/Holder (ex.: EnchantmentInit.X). */
    public static int getItemEnchantmentLevel(net.minecraft.core.Holder<Enchantment> holder, ItemStack stack) {
        return getItemEnchantmentLevel(MNAEnchantmentRegistry.keyOf(holder.value()), stack);
    }
''' + t[idx:]
    save(p, t)
print('  ok')

print('== InventorySoulboundDrop ==')
p = S + 'mixins/InventorySoulboundDrop.java'
t = load(p)
t = t.replace('Inventory self = (Inventory)this;', 'Inventory self = (Inventory)(Object)this;')
t = t.replace('stackInSlot.save(item);', 'com.mna.api.tools.MNAItemData.save(stackInSlot, item);')
save(p, t)
print('  ok')

print('== PotionEffectComponent getEffect ==')
p = S + 'spells/components/PotionEffectComponent.java'
t = load(p)
t = t.replace('private MobEffect getEffect() {\n        return this.effect != null ? this.effect : (MobEffect)this.registry_effect.get();\n    }',
              'private net.minecraft.core.Holder<MobEffect> getEffect() {\n        return this.effect != null ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this.effect) : this.registry_effect;\n    }')
save(p, t)
print('  ok')

print('== BlockUtils ==')
p = S + 'tools/BlockUtils.java'
t = load(p)
t = t.replace('ArrayList<? extends String> configuredTiers = new ArrayList<String>(GeneralConfigValues.BreakMagnitudeMapping);',
              'ArrayList<String> configuredTiers = new ArrayList<String>(GeneralConfigValues.BreakMagnitudeMapping);')
t = t.replace('Tier resolved = TierSortingRegistry.byName((ResourceLocation)rLoc);',
              'Tier resolved = (Tier)com.mna.api.tools.MNAtierSorting.getSortedTiers().get(Math.min(harvestLevel, com.mna.api.tools.MNAtierSorting.getSortedTiers().size() - 1));')
save(p, t)
print('  ok')

print('== InfiniteFluidCapabilityHandler ==')
p = S + 'items/base/InfiniteFluidCapabilityHandler.java'
t = load(p)
t = t.replace('implements IFluidHandlerItem,\nICapabilityProvider {', 'implements IFluidHandlerItem {')
t = re.sub(r'import net\.neoforged\.neoforge\.capabilities\.ICapabilityProvider;\r?\n', '', t)
t = t.replace('    IFluidHandlerItem holder = () -> this;\n', '').replace('    IFluidHandlerItem holder = () -> this;\r\n', '')
t = t.replace('this.container.removeTagKey(FLUID_NBT_KEY);', 'com.mna.api.tools.MNAItemData.remove(this.container, FLUID_NBT_KEY);')
save(p, t)
print('  ok')

print('== LootConditionHelper ==')
p = S + 'tools/loot/LootConditionHelper.java'
t = load(p)
t = t.replace('import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;',
              'import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;')
t = t.replace('lootDrop.chance = ((LootItemRandomChanceCondition)condition).probability;',
              'lootDrop.chance = 1.0f; // [PORT 1.21.1] probability virou NumberProvider (exige LootContext)')
t = t.replace('condition instanceof LootItemRandomChanceWithLootingCondition', 'condition instanceof LootItemRandomChanceWithEnchantedBonusCondition')
t = t.replace('lootDrop.chance = ((LootItemRandomChanceWithLootingCondition)condition).percent;',
              'lootDrop.chance = ((LootItemRandomChanceWithEnchantedBonusCondition)condition).unenchantedChance();')
save(p, t)
print('  ok')

print('== ParticleItemStack ==')
p = S + 'api/particles/parameters/ParticleItemStack.java'
t = load(p)
if 'import com.mna.ManaAndArtifice;' not in t:
    t = t.replace('import ', 'import com.mna.ManaAndArtifice;\nimport ', 1)
t = t.replace('net.minecraft.server.MinecraftServer.getServer().registryAccess()',
              'net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null ? net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess() : net.minecraft.core.RegistryAccess.EMPTY')
t = t.replace('packetBuffer.writeNbt(inst.value().serializeNBT());',
              'packetBuffer.writeNbt((net.minecraft.nbt.Tag)inst.value().save(com.mna.api.tools.MNAItemData.provider()));')
save(p, t)
print('  ok')

print('== ModifiedSpellPart ==')
p = S + 'spells/crafting/ModifiedSpellPart.java'
t = load(p)
t = t.replace('return ImmutableList.copyOf((Object[])attrs);', 'return ImmutableList.copyOf(attrs);')
t = t.replace('IModifiable inst = (IModifiable)registry.getValue(rLoc);', 'T inst = registry.getValue(rLoc);')
t = t.replace('ModifiedSpellPart<IModifiable> msp = new ModifiedSpellPart<IModifiable>(inst);', 'ModifiedSpellPart<T> msp = new ModifiedSpellPart<T>(inst);')
save(p, t)
print('  ok')

print('== SpellRecipe ==')
p = S + 'spells/crafting/SpellRecipe.java'
t = load(p)
t = t.replace('modified_attrs.addAll(c.getContainedAttributes().stream()', 'modified_attrs.addAll((java.util.Collection)c.getContainedAttributes().stream()')
t = t.replace('CompoundTag nbt = recipeStack.getTag();', 'CompoundTag nbt = com.mna.api.tools.MNAItemData.tag(recipeStack);')
save(p, t)
print('  ok')

print('== ProgressionEventHandler ==')
p = S + 'progression/ProgressionEventHandler.java'
t = load(p)
t = t.replace('import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;',
              'import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;')
t = t.replace('public static void onPlayerSleep(PlayerSleepInBedEvent event) {', 'public static void onPlayerSleep(CanPlayerSleepEvent event) {')
t = t.replace('for (Advancement adv : advancementMgr.getAllAdvancements()) {', 'for (net.minecraft.advancements.AdvancementHolder adv : advancementMgr.getAllAdvancements()) {')
save(p, t)
print('  ok')
print()
print('FIM')
