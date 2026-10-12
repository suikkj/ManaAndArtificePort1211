# _fix75.py
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

# 1 FillerBlock: BlockState.useItemOn publico
p = find1('FillerBlock.java')
t = load(p)
t = t.replace('''net.minecraft.world.ItemInteractionResult __ir = world.getBlockState(offsetPos).getBlock().useItemOn(player.getItemInHand(hand), world.getBlockState(offsetPos), world, offsetPos, player, hand, brtr);''',
'''net.minecraft.world.ItemInteractionResult __ir = world.getBlockState(offsetPos).useItemOn(player.getItemInHand(hand), world, player, hand, brtr);''')
save(p, t)
print('1 ok')

# 2 CoffinBlock
p = find1('CoffinBlock.java')
t = load(p)
t = t.replace('''net.minecraft.world.ItemInteractionResult __ir = super.useItemOn(player.getItemInHand(handIn), state, worldIn, pos, player, handIn, hit);''',
'''net.minecraft.world.ItemInteractionResult __ir = state.useItemOn(player.getItemInHand(handIn), worldIn, player, handIn, hit);''')
save(p, t)
print('2 ok')

# 3 ConstructActivate
p = find1('ConstructActivate.java')
t = load(p)
t = t.replace('state.getBlock().useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), state, c.level(), this.interactPos, player, InteractionHand.MAIN_HAND, brtr);',
              'state.useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), c.level(), player, InteractionHand.MAIN_HAND, brtr);')
save(p, t)
print('3 ok')

# 4 ComponentActivate
p = find1('ComponentActivate.java')
t = load(p)
t = t.replace('state.getBlock().useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), state, c.level(), c.blockPosition(), player, InteractionHand.MAIN_HAND, brtr);',
              'state.useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), c.level(), player, InteractionHand.MAIN_HAND, brtr);')
save(p, t)
print('4 ok')

# 5 ConstructUseItemOnBlock
p = find1('ConstructUseItemOnBlock.java')
t = load(p)
t = re.sub(r'(\w+)\.getBlock\(\)\.use\(\1, c\.level\(\), this\.interactPos, player, (player|hand|handIn|\w+), ([^)]+)\)',
           r'\1.useItemOn(player.getItemInHand(\2), c.level(), player, \2, \3)', t)
save(p, t)
print('5 ok')

# 6 ConstructNodeEvaluator: overrides PathfindingContext + isPathfindable
p = find1('ConstructNodeEvaluator.java')
t = load(p)
if 'getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext' not in t:
    old = '    public PathType getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob mob) {'
    new = '''    @Override
    public PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z, Mob mob) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z, mob);
    }

    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

    public PathType getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob mob) {'''
    t = rl(t, old, new)
t = t.replace('.isPathfindable(this.mob.level(), blockpos, PathComputationType.LAND)', '.isPathfindable(PathComputationType.LAND)')
t = t.replace('.isPathfindable((BlockGetter)this.mob.level(), blockpos, PathComputationType.LAND)', '.isPathfindable(PathComputationType.LAND)')
save(p, t)
print('6 ok')

# 7 FollowOwnerGoal
p = find1('FollowOwnerGoal.java')
t = load(p)
t = t.replace('PathType pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic((BlockGetter)this.world, (BlockPos.MutableBlockPos)pos.mutable());',
              'PathType pathnodetype = WalkNodeEvaluator.getPathTypeStatic((Mob)this.mob, (BlockPos)pos);')
save(p, t)
print('7 ok')

# 8 ConstructMoveControl
p = find1('ConstructMoveControl.java')
t = load(p)
t = re.sub(r'nodeevaluator\.getBlockPathType\(\(BlockGetter\)this\.mob\.level\(\), Mth\.floor\(\(double\)\(this\.mob\.getX\(\) \+ \(double\)pRelativeX\)\), this\.mob\.getBlockY\(\), Mth\.floor\(\(double\)\(this\.mob\.getZ\(\) \+ \(double\)pRelativeZ\)\)\) == PathType\.WALKABLE',
           'WalkNodeEvaluator.getPathTypeStatic((Mob)this.mob, new BlockPos(Mth.floor((double)(this.mob.getX() + (double)pRelativeX)), this.mob.getBlockY(), Mth.floor((double)(this.mob.getZ() + (double)pRelativeZ)))) == PathType.WALKABLE', t)
save(p, t)
print('8 ok')

# 9 EffectGrow/Shrink packets -> broadcastAndSend
for fn in ['EffectGrow.java', 'EffectShrink.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'PacketDistributor\.sendToPlayersTrackingEntity\(pLivingEntity, (new Clientbound(?:Update|Remove)MobEffectPacket\([^;]+\))\);',
               r'((net.minecraft.server.level.ServerLevel)pLivingEntity.level()).getChunkSource().broadcastAndSend(pLivingEntity, \1);', t)
    save(p, t)
print('9 ok')

# 10 WorldMagicProvider
p = find1('WorldMagicProvider.java')
t = load(p)
t = t.replace('IWorldMagic holder = () -> new WorldMagic(this.isOverworld);', 'IWorldMagic holder = new WorldMagic(this.isOverworld);')
save(p, t)
print('10 ok')

# 11 MADamageEnchantmentBase extends MAEnchantmentBase
p = find1('MADamageEnchantmentBase.java')
t = load(p)
t = t.replace('public abstract class MADamageEnchantmentBase {', 'public abstract class MADamageEnchantmentBase extends com.mna.enchantments.framework.MAEnchantmentBase {')
save(p, t)
print('11 ok')

# 12 Aura post split + ICancellableEvent
p = find1('Aura.java')
t = load(p)
t = t.replace('''        if (NeoForge.EVENT_BUS.post(new AuraEvent.Tick(this, (LivingEntity)source))) {
            return;
        }''', '''        AuraEvent.Tick __tickEvt = new AuraEvent.Tick(this, (LivingEntity)source);
        NeoForge.EVENT_BUS.post(__tickEvt);
        if (__tickEvt.isCanceled()) {
            return;
        }''')
save(p, t)
p = find1('AuraEvent.java')
if p:
    t = load(p)
    if 'ICancellableEvent' not in t:
        t = re.sub(r'(class Tick[^{]*extends Event)( \{)', r'\1\nimplements net.neoforged.bus.api.ICancellableEvent\2', t)
        save(p, t)
print('12 ok')

# 13 EffectHelper
p = find1('EffectHelper.java')
t = load(p)
t = t.replace('for (MobEffectInstance inst : toRemove) {', 'for (MobEffectInstance inst : (java.util.List<MobEffectInstance>)(java.util.List)toRemove) {')
save(p, t)
print('13 ok')

# 14 EffectUndeadFrostWalker / EffectWrithingBrambles return
for fn in ['EffectUndeadFrostWalker.java', 'EffectWrithingBrambles.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('        if (living.level().isClientSide()) {\n            return;\n        }', '        if (living.level().isClientSide()) {\n            return true;\n        }')
    t = t.replace('        if (living.level().isClientSide()) {\r\n            return;\r\n        }', '        if (living.level().isClientSide()) {\r\n            return true;\r\n        }')
    save(p, t)
print('14 ok')

# 15 EffectMistForm
p = find1('EffectMistForm.java')
t = load(p)
t = t.replace('player.getFeetBlockState().canOcclude()', 'player.level().getBlockState(player.blockPosition()).canOcclude()')
save(p, t)
print('15 ok')

# 16 EffectDisjunction import
p = find1('EffectDisjunction.java')
t = load(p)
t = addimport(t, 'import java.util.ArrayList;')
save(p, t)
print('16 ok')

# 17 PumpkinKingEntangle
p = find1('PumpkinKingEntangle.java')
t = load(p)
t = t.replace('((Optional)this.entityData.get(TARGET)).ifPresent(u -> {', '((Optional<java.util.UUID>)this.entityData.get(TARGET)).ifPresent(u -> {')
save(p, t)
print('17 ok')

# 18 ThrownWeapon particle serialize
p = find1('ThrownWeapon.java')
t = load(p)
t = t.replace('buffer.writeUtf(this.trailParticle.writeToString());',
              'buffer.writeUtf(String.valueOf(net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(this.trailParticle)));')
old = '''        String trailParticle = additionalData.readUtf();
        try {
            this.trailParticle = ParticleArgument.readParticle((StringReader)new StringReader(trailParticle), (HolderLookup)this.level().holderLookup(Registries.PARTICLE_TYPE));
        }
        catch (CommandSyntaxException commandsyntaxexception) {
            ManaAndArtifice.LOGGER.warn("Couldn't load custom particle {}", trailParticle, commandsyntaxexception);
        }'''
new = '''        String trailParticle = additionalData.readUtf();
        try {
            this.trailParticle = net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.get(net.minecraft.resources.ResourceLocation.parse(trailParticle));
        }
        catch (Exception commandsyntaxexception) {
            ManaAndArtifice.LOGGER.warn("Couldn't load custom particle {}", trailParticle, commandsyntaxexception);
        }'''
t = rl(t, old, new)
save(p, t)
print('18 ok')

# 19 BossMonster dropAllDeathLoot
p = find1('BossMonster.java')
t = load(p)
t = t.replace('this.dropAllDeathLoot(pCause);', 'this.dropAllDeathLoot(serverLevel, pCause);')
save(p, t)
print('19 ok')

# 20 DemonLord
p = find1('DemonLord.java')
t = load(p)
t = t.replace('projectile.setSpellRecipe(this.hellball.getTag());',
              'projectile.setSpellRecipe(this.hellball.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());')
save(p, t)
print('20 ok')

# 21 entityData byte (generico)
for fn in ['WitherLich.java', 'SpellSigil.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'entityData\.set\((?!DATA_)([A-Z_]+), (\d+)\)', r'entityData.set(\1, (byte)\2)', t)
    save(p, t)
print('21 ok')

# 22 ConstructHasName
p = find1('ConstructHasName.java')
t = load(p)
t = t.replace('this.nameTagStack.hasCustomHoverName()', 'this.nameTagStack.has(DataComponents.CUSTOM_NAME)')
save(p, t)
print('22 ok')

# 23 ConstructBreed import
p = find1('ConstructBreed.java')
t = load(p)
t = addimport(t, 'import net.minecraft.world.entity.EntityType;')
save(p, t)
print('23 ok')

# 24 ConstructCollectFluid pickupBlock
p = find1('ConstructCollectFluid.java')
t = load(p)
t = t.replace('bp.pickupBlock((LevelAccessor)c.level(), this.currentTarget, state)', 'bp.pickupBlock((net.minecraft.world.entity.LivingEntity)null, (LevelAccessor)c.level(), this.currentTarget, state)')
save(p, t)
print('24 ok')

# 25 ConstructFish loot
p = find1('ConstructFish.java')
t = load(p)
t = t.replace('this.getLootRoll(BuiltInLootTables.FISHING,', 'this.getLootRoll(BuiltInLootTables.FISHING.location(),')
save(p, t)
print('25 ok')

# 26 ConstructMilkCow duplicate var
p = find1('ConstructMilkCow.java')
t = load(p)
t = t.replace('for (Cow candidate2 : entities) {', 'for (Cow candidate3 : entities) {')
save(p, t)
print('26 ok')

# 27 ConstructWaterCrops
p = find1('ConstructWaterCrops.java')
t = load(p)
t = t.replace('if (state.getBlock().isRandomlyTicking(state)) {', 'if (state.isRandomlyTicking()) {')
t = t.replace('state.getBlock().randomTick(state, (ServerLevel)c.level(), pos, c.level().getRandom());', 'state.randomTick((ServerLevel)c.level(), pos, c.level().getRandom());')
save(p, t)
print('27 ok')

# 28 ConstructConstruction
p = find1('ConstructConstruction.java')
t = load(p)
t = t.replace('Collections.sort(mats, (a, b) -> Integer.compare(sortedTiers.indexOf(a.getEquivalentTier()), sortedTiers.indexOf(b.getEquivalentTier())));',
              'Collections.sort(mats, (a, b) -> Integer.compare(sortedTiers.indexOf(((com.mna.api.entities.construct.ConstructMaterial)a).getEquivalentTier()), sortedTiers.indexOf(((com.mna.api.entities.construct.ConstructMaterial)b).getEquivalentTier())));')
save(p, t)
print('28 ok')

# 29 ConstructPathNavigator isInLiquid
p = find1('ConstructPathNavigator.java')
t = load(p)
t = t.replace('return this.canFloat() && this.isInLiquid() || !this.mob.isPassenger();', 'return this.canFloat() && this.mob.isInWater() || !this.mob.isPassenger();')
t = t.replace('if (this.isInLiquid()) {', 'if (this.mob.isInWater()) {')
save(p, t)
print('29 ok')

# 30 Ritual serializeNBT
p = find1('Ritual.java')
t = load(p)
old = 'ritualData.put("dynamicSourceCache", (Tag)this.cachedDynamicStack.serializeNBT());'
new = '''CompoundTag __dyn = new CompoundTag();
        com.mna.api.tools.MNAItemData.save(this.cachedDynamicStack, __dyn);
        ritualData.put("dynamicSourceCache", __dyn);'''
t = rl(t, old, new)
save(p, t)
print('30 ok')

# 31 Manaweave cantrip cast
p = find1('Manaweave.java')
t = load(p)
t = t.replace('ICantrip cantrip = magic.getCantripData().matchAndCastCantrip(player, hand, this.getPatterns().stream().map(r -> (Recipe)r).collect(Collectors.toList()));',
              'ICantrip cantrip = magic.getCantripData().matchAndCastCantrip(player, hand, (java.util.List<net.minecraft.world.item.crafting.Recipe<?>>)(java.util.List)this.getPatterns().stream().map(r -> (Recipe)r).collect(Collectors.toList()));')
save(p, t)
print('31 ok')

# 32 BaseFactionMob removeModifier
p = find1('BaseFactionMob.java')
t = load(p)
t = t.replace('maxHPAttr.removePermanentModifier(TierHealthID);', 'maxHPAttr.removeModifier(TierHealthID);')
t = t.replace('maxArmorAttr.removePermanentModifier(TierArmorID);', 'maxArmorAttr.removeModifier(TierArmorID);')
save(p, t)
print('32 ok')

# 33 BaseFlyingFactionMob finalizeSpawn
p = find1('BaseFlyingFactionMob.java')
t = load(p)
t = t.replace('return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);', 'return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);')
save(p, t)
print('33 ok')

# 34 Deathcap setPotion
p = find1('Deathcap.java')
t = load(p)
t = t.replace('areaeffectcloud.setPotion(Potions.POISON);', 'areaeffectcloud.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(Potions.POISON));')
save(p, t)
print('34 ok')

# 35 SpellSigil define byte
p = find1('SpellSigil.java')
t = load(p)
t = t.replace('builder.define(PLAYERLESS, (byte)true);', 'builder.define(PLAYERLESS, (byte)1);')
save(p, t)
print('35 ok')

# 36 WitchHunterArrow Items import
p = find1('WitchHunterArrow.java')
t = load(p)
t = addimport(t, 'import net.minecraft.world.item.Items;')
save(p, t)
print('36 ok')

# 37 ICancellableEvent eventos rote
for fn in ['RoteProgressGainedEvent.java', 'MasteryGainedEvent.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    if 'ICancellableEvent' not in t:
        t = re.sub(r'(class \w+[^{]*extends Event)( \{)', r'\1\nimplements net.neoforged.bus.api.ICancellableEvent\2', t)
        save(p, t)
print('37 ok')

# 38 ConstructAdventure / ConstructMine loot keys
for fn in ['ConstructAdventure.java', 'ConstructMine.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'getLootTable\((lootTableId|ADVENTURE_LOOT_OVERWORLD|ADVENTURE_LOOT_NETHER|MINING_LOOT_\w+)\)',
               r'getLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, \1))', t)
    save(p, t)
print('38 ok')
print('FIM')
