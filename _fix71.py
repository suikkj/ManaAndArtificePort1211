# _fix71.py
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

def addimport(t, imp):
    if imp not in t:
        t = re.sub(r'(import [^\n]*;\r?\n)', lambda m: m.group(1) + imp + '\n', t, count=1)
    return t

# 1 ManaweavingAltarTile
p = find1('ManaweavingAltarTile.java')
t = load(p)
t = re.sub(r'import net\.neoforged\.neoforge\.capabilities\.Capability;\r?\n', '', t)
t = t.replace('return super.getRenderBoundingBox().expandTowards(0.0, 1.0, 0.0);', 'return new net.minecraft.world.phys.AABB(this.worldPosition).expandTowards(0.0, 1.0, 0.0);')
save(p, t)
print('1 ok')

# 2 PlayerProgressionSerializer
p = find1('PlayerProgressionSerializer.java')
t = load(p)
t = t.replace('import net.minecraft.resources.ResourceLocationException;', 'import net.minecraft.ResourceLocationException;')
save(p, t)
print('2 ok')

# 3 SpellEffect
p = find1('SpellEffect.java')
t = load(p)
t = t.replace('!block.canSurvive(placementState, (LevelReader)world, targetPos))', '!placementState.canSurvive((LevelReader)world, targetPos))')
t = t.replace('for (Direction direction : dirs) {', 'for (Direction direction : (java.util.List<Direction>)dirs) {')
save(p, t)
print('3 ok')

# 4 IFactionEnemy
p = find1('IFactionEnemy.java')
t = load(p)
t = t.replace('(chunkAccess = world.getChunk(pos)) instanceof LevelChunk', '(chunkAccess = (net.minecraft.world.level.chunk.ChunkAccess)world.getChunk(pos)) instanceof LevelChunk')
save(p, t)
print('4 ok')

# 5 ConstructAITask
p = find1('ConstructAITask.java')
t = load(p)
t = t.replace('reloadableRegistries().getLootTable(rLoc)', 'reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, rLoc))')
t = t.replace('getBlockStates(new AABB(pos, pos.above()))', 'getBlockStates(new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.above())))')
save(p, t)
print('5 ok')

# 6 EldrinCapacitorBlock
p = find1('EldrinCapacitorBlock.java')
t = load(p)
t = t.replace('pLevel.getChunkAt(pPos).getCapability(ManaAndArtificeMod.getChunkMagicCapability())', 'com.mna.api.tools.MNACap.get(pLevel.getChunkAt(pPos), com.mna.capabilities.CapabilitiesInit.CHUNK_MAGIC)')
save(p, t)
print('6 ok')

# 7 FluidParameterRegistry
p = find1('FluidParameterRegistry.java')
t = load(p)
t = t.replace('effect.getEffect()', '((net.minecraft.world.effect.MobEffectInstance)effect).getEffect()')
save(p, t)
print('7 ok')

# 8 PossessionActions
p = find1('PossessionActions.java')
t = load(p)
t = t.replace('ProjectileUtil.getMobArrow(reference, (ItemStack)itemstack, (float)1.0f);', 'ProjectileUtil.getMobArrow(reference, (ItemStack)itemstack, (float)1.0f, (ItemStack)null);')
t = rl(t, '''            if (reference.getMainHandItem().getItem() instanceof BowItem) {
                abstractarrowentity = ((BowItem)reference.getMainHandItem().getItem()).customArrow(abstractarrowentity);
            }''', '''            // [PORT 1.21.1] BowItem.customArrow removido''')
save(p, t)
print('8 ok')

# 9 MATags
p = find1('MATags.java')
t = load(p)
t = t.replace('return ((HolderSet.Named)biomeTag.get()).stream().collect(Collectors.toList());', 'return ((net.minecraft.core.HolderSet.Named<net.minecraft.world.level.biome.Biome>)biomeTag.get()).stream().collect(Collectors.toList());')
save(p, t)
print('9 ok')

# 10 CloudBlock
p = find1('CloudBlock.java')
t = load(p)
t = t.replace('if (belowState.getBlock().isRandomlyTicking(belowState)) {', 'if (belowState.isRandomlyTicking()) {')
t = t.replace('belowState.getBlock().randomTick(belowState, pLevel, belowPos, pRandom);', 'belowState.randomTick(pLevel, belowPos, pRandom);')
save(p, t)
print('10 ok')

# 11 BookshelfTile
p = find1('BookshelfTile.java')
t = load(p)
t = t.replace('color = ((MNADyeableItem)this.getItem(i).getItem()).getColor(this.getItem(i));', 'color = MNADyeableItem.getColor(this.getItem(i));')
t = t.replace('        super.onDataPacket(net, pkt);\n', '')
save(p, t)
print('11 ok')

# 12 EldrinAltarTile
p = find1('EldrinAltarTile.java')
t = load(p)
t = t.replace('return new CollectedItem(BlockPos.of((long)nbt.getLong("pos")), ItemStack.parseOptional(this.getLevel().registryAccess(), nbt));',
              'return new CollectedItem(BlockPos.of((long)nbt.getLong("pos")), ItemStack.parseOptional(provider, nbt));')
t = t.replace('public static CollectedItem fromNBT(CompoundTag nbt) {', 'public static CollectedItem fromNBT(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {')
t = t.replace('this.collectedItems.add(CollectedItem.fromNBT(collectedItem));', 'this.collectedItems.add(CollectedItem.fromNBT(collectedItem, this.getLevel().registryAccess()));')
save(p, t)
print('12 ok')

# 13 EldrinAltarRecipe
p = find1('EldrinAltarRecipe.java')
t = load(p)
t = t.replace('if (!(inv instanceof ItemAndPatternCraftingInventory)) {', 'if (!((Object)inv instanceof ItemAndPatternCraftingInventory)) {')
t = t.replace('ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)inv;', 'ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)(Object)inv;')
save(p, t)
print('13 ok')

# 14 getUpdateTag tiles
for fn in ['ElementalSentryTile.java', 'InscriptionTableTile.java', 'SeerStoneTile.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('this.saveAdditional(tag);', 'this.saveAdditional(tag, registries);')
    t = t.replace('this.load(pkt.getTag());', 'this.loadAdditional(pkt.getTag(), this.level.registryAccess());')
    save(p, t)
print('14 ok')

# 15 RunescribingTableTile
p = find1('RunescribingTableTile.java')
t = load(p)
t = addimport(t, 'import com.mna.items.ItemInit;')
save(p, t)
print('15 ok')
print('FIM')
