# _fix77.py
import os, re, subprocess

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

# 0 javap BucketPickup (para o fix 15)
try:
    jx = '/tmp/jx'
    J = '/c/Users/lucas/.gradle/caches/neoformruntime/intermediate_results/compiledWithNeoForge_5d46d1c494c55beabd9b7ced3a43ba141199b9ae_output.jar'
    subprocess.run(['unzip', '-o', '-q', J, 'net/minecraft/world/level/block/BucketPickup.class'], cwd=jx, capture_output=True)
    r = subprocess.run(['javap', 'net/minecraft/world/level/block/BucketPickup.class'], cwd=jx, capture_output=True, text=True)
    print('BucketPickup:', [l.strip() for l in r.stdout.split('\n') if 'pickup' in l])
except Exception as e:
    print('javap fail', e)

# 1 ManaPylonTile
p = find1('ManaPylonTile.java')
t = load(p)
t = t.replace('import com.mna.entities.rituals.SpellRune;', 'import com.mna.entities.sorcery.targeting.SpellSigil;')
t = t.replace('(SpellRune e) -> {', '(SpellSigil e) -> {')
save(p, t)
print('1 ok')

# 2 MAParticleType depthDisable
p = find1('MAParticleType.java')
t = load(p)
t = t.replace('IParticleMoveType mover, ParticleBoolean depthDisable) {', 'IParticleMoveType mover, boolean depthDisable) {')
t = t.replace('        this.depthDisable = depthDisable;', '        this.disableDepthTest = depthDisable;')
save(p, t)
print('2 ok')

# 3 SlipstreamGeneratorTile
p = find1('SlipstreamGeneratorTile.java')
t = load(p)
t = t.replace('this.bb = new AABB(this.getBlockPos(), bbOffset.offset(this.getLevitateDir().getStepX() == 0 ? 1 : 0, this.getLevitateDir().getStepY() == 0 ? 1 : 0, this.getLevitateDir().getStepZ() == 0 ? 1 : 0));',
              'this.bb = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(this.getBlockPos()), net.minecraft.world.phys.Vec3.atLowerCornerOf(bbOffset.offset(this.getLevitateDir().getStepX() == 0 ? 1 : 0, this.getLevitateDir().getStepY() == 0 ? 1 : 0, this.getLevitateDir().getStepZ() == 0 ? 1 : 0)));')
save(p, t)
print('3 ok')

# 4 ArcaneSentryRenderer
p = find1('ArcaneSentryRenderer.java')
t = load(p)
t = t.replace('this.modelBook.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn, 1.0f, 1.0f, 1.0f, 1.0f);',
              'this.modelBook.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn, -1);')
save(p, t)
print('4 ok')

# 5 PlayerMagicSerializer 2-arg
p = find1('PlayerMagicSerializer.java')
t = load(p)
t = t.replace('MNAItemData.saveItems(inv, items, provider);', 'MNAItemData.saveItems(inv, items);')
t = t.replace('MNAItemData.loadItems(nbt, items, provider);', 'MNAItemData.loadItems(nbt, items);')
save(p, t)
print('5 ok')

# 7 EffectWrithingBrambles return
p = find1('EffectWrithingBrambles.java')
t = load(p)
t = re.sub(r'(if \(!pLivingEntity\.getPersistentData\(\)\.contains\("brambles_old_x"\)\) \{(?:\r?\n[^\n]*){4}\r?\n\s*)return;', r'\1return true;', t)
save(p, t)
print('7 ok')

# 8 AuraEvent.Tick
p = find1('AuraEvent.java')
t = load(p)
if 'ICancellableEvent' not in t:
    t = rl(t, '    public static class Tick\n    extends AuraEvent {', '    public static class Tick\n    extends AuraEvent\n    implements net.neoforged.bus.api.ICancellableEvent {')
    save(p, t)
print('8 ok')

# 9 FollowOwnerGoal
p = find1('FollowOwnerGoal.java')
t = load(p)
t = t.replace('PathType pathnodetype = WalkNodeEvaluator.getPathTypeStatic((Mob)this.mob, (BlockPos)pos);', 'PathType pathnodetype = WalkNodeEvaluator.getPathTypeStatic(this.tameable, (BlockPos)pos);')
save(p, t)
print('9 ok')

# 10 ThrownWeapon casts
p = find1('ThrownWeapon.java')
t = load(p)
t = t.replace('buffer.writeUtf(String.valueOf(net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(this.trailParticle)));',
              'buffer.writeUtf(String.valueOf(net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey((net.minecraft.core.particles.ParticleType)this.trailParticle)));')
t = t.replace('this.trailParticle = net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.get(net.minecraft.resources.ResourceLocation.parse(trailParticle));',
              'this.trailParticle = (net.minecraft.core.particles.ParticleOptions)(Object)net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.get(net.minecraft.resources.ResourceLocation.parse(trailParticle));')
save(p, t)
print('10 ok')

# 11 ThrownAllfatherAxe
p = find1('ThrownAllfatherAxe.java')
t = load(p)
t = rl(t, '        super.loadAdditional(nbt, registries);\n', '')
save(p, t)
print('11 ok')

# 12 BossMonster getRandom
p = find1('BossMonster.java')
t = load(p)
t = t.replace('CollectionUtils.getRandom(nearbyPlayers.stream().filter(p -> predicate.test((Player)p)).collect(Collectors.toList())).ifPresent(p -> selection.setValue(p));',
'''java.util.List<Player> __pl = nearbyPlayers.stream().filter(p -> predicate.test((Player)p)).collect(Collectors.toList());
            if (!__pl.isEmpty()) {
                selection.setValue(__pl.get(this.getRandom().nextInt(__pl.size())));
            }''')
save(p, t)
print('12 ok')

# 13 ConstructHasName import
p = find1('ConstructHasName.java')
t = load(p)
t = addimport(t, 'import net.minecraft.core.component.DataComponents;')
save(p, t)
print('13 ok')

# 14 ConstructIsFluidInContainer
p = find1('ConstructIsFluidInContainer.java')
t = load(p)
t = t.replace('if (!(handler = FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection())).isPresent()) {',
'''handler = (net.neoforged.neoforge.fluids.capability.IFluidHandler)FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection()).orElse(null);
        if (handler == null) {''')
save(p, t)
print('14 ok')

# 16 ConstructUseItemOnBlock
p = find1('ConstructUseItemOnBlock.java')
t = load(p)
t = t.replace('result = state.useItemOn(player.getItemInHand(playerHand), c.level(), player, playerHand, simResult);',
'''net.minecraft.world.ItemInteractionResult __uir = state.useItemOn(player.getItemInHand(playerHand), c.level(), player, playerHand, simResult);
                            result = __uir.consumesAction() ? InteractionResult.sidedSuccess(c.level().isClientSide) : InteractionResult.PASS;''')
save(p, t)
print('16 ok')

# 17 ConstructMoveControl import
p = find1('ConstructMoveControl.java')
t = load(p)
t = addimport(t, 'import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;')
save(p, t)
print('17 ok')

# 18 ConstructNodeEvaluator overrides
p = find1('ConstructNodeEvaluator.java')
t = load(p)
if 'getPathType(net.minecraft.world.level.pathfinder.PathfindingContext' not in t and 'getPathType(PathfindingContext' not in t:
    anchor = '    public PathType getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob mob) {'
    ins = '''    @Override
    public PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z, Mob mob) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z, mob);
    }

    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

    public PathType getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob mob) {'''
    t = rl(t, anchor, ins)
    save(p, t)
print('18 ok')

# 19 SpellSigil
p = find1('SpellSigil.java')
t = load(p)
t = t.replace('this.entityData.set(CHARGES, (byte)1);', 'this.entityData.set(CHARGES, 1);')
t = t.replace('builder.define(PLAYERLESS, (byte)1);', 'builder.define(PLAYERLESS, false);')
save(p, t)
print('19 ok')

# 20 GreaterAnimus
p = find1('GreaterAnimus.java')
t = load(p)
t = rl(t, '''            if (this.getMainHandItem().getItem() instanceof BowItem) {
                abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
            }''', '''            // [PORT 1.21.1] BowItem.customArrow removido''')
if 'shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy)' not in t:
    anchor = '    public void shootCrossbowProjectile(LivingEntity p_33275_, ItemStack p_33276_, Projectile p_33277_, float p_33278_) {'
    ins = '''    protected void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy) {
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333) - projectile.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectile.shoot(d0, d1 + d3 * 0.2, d2, velocity, inaccuracy);
    }

    public void shootCrossbowProjectile(LivingEntity p_33275_, ItemStack p_33276_, Projectile p_33277_, float p_33278_) {'''
    t = rl(t, anchor, ins)
save(p, t)
print('20 ok')

# 21 WanderingWizard containerCounter
p = 'src/main/java/com/mna/tools/MNAReflection.java'
t = load(p)
if 'containerCounter' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    public static int containerCounter(net.minecraft.server.level.ServerPlayer player) {
        try {
            java.lang.reflect.Field f = net.minecraft.server.level.ServerPlayer.class.getDeclaredField("containerCounter");
            f.setAccessible(true);
            return f.getInt(player);
        } catch (Throwable t) {
            return 0;
        }
    }
''' + t[idx:]
    save(p, t)
p = find1('WanderingWizard.java')
t = load(p)
t = t.replace('((ServerPlayer)player).containerCounter', 'com.mna.tools.MNAReflection.containerCounter((ServerPlayer)player)')
save(p, t)
print('21 ok')

# 22 Broker
p = find1('Broker.java')
t = load(p)
t = t.replace('BuiltInRegistries.ITEM.getValues().stream().filter(', 'BuiltInRegistries.ITEM.stream().filter(')
t = t.replace('this.customerLimitedOffers = new MerchantOffers(this.offers.createTag());', 'this.customerLimitedOffers = new MerchantOffers();\n            this.customerLimitedOffers.addAll(this.offers);')
save(p, t)
print('22 ok')

# 23 ThrownManaweaveBottle
p = find1('ThrownManaweaveBottle.java')
t = load(p)
t = t.replace('    protected float getGravity() {\n        return 0.05f;\n    }', '    protected double getGravity() {\n        return 0.05;\n    }')
t = t.replace('    protected float getGravity() {\r\n        return 0.05f;\r\n    }', '    protected double getGravity() {\r\n        return 0.05;\r\n    }')
save(p, t)
print('23 ok')

# 24 RisingBlock
p = find1('RisingBlock.java')
t = load(p)
t = t.replace('tileentity.loadAdditional(compoundnbt, com.mna.api.tools.MNAItemData.provider())', 'com.mna.api.tools.MNATileAccess.loadAdditional(tileentity, compoundnbt, com.mna.api.tools.MNAItemData.provider())')
save(p, t)
print('24 ok')

# 25 pickup items
for fn in ['BoundBowProjectile.java', 'SkeletonAssassinShuriken.java', 'SkeletonAssassinBolo.java']:
    p = find1(fn)
    t = load(p)
    if 'getDefaultPickupItem' not in t:
        idx = t.rstrip().rfind('}')
        t = t[:idx] + '''
    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
''' + t[idx:]
        save(p, t)
print('25 ok')

# 26 BloodGolem
p = find1('BloodGolem.java')
t = load(p)
t = t.replace('.add((Attribute)Attributes.STEP_HEIGHT.get(), 1.0)', '.add(Attributes.STEP_HEIGHT, 1.0)')
save(p, t)
print('26 ok')

# 27 Simulacrum getOwner
p = find1('Simulacrum.java')
t = load(p)
t = t.replace('if (this.__owner == null && (resolved = super.getOwner()) instanceof Player) {', 'if (this.__owner == null && (resolved = null) instanceof Player) {')
save(p, t)
print('27 ok')

# 28 MultiblockConfiguration palettes
p = 'src/main/java/com/mna/tools/MNAReflection.java'
t = load(p)
if 'structurePalettes' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    public static java.util.List<net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette> structurePalettes(net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate template) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.class.getDeclaredField("palettes");
            f.setAccessible(true);
            return (java.util.List<net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette>)f.get(template);
        } catch (Throwable t) {
            return java.util.List.of();
        }
    }
''' + t[idx:]
    save(p, t)
p = find1('MultiblockConfiguration.java')
t = load(p)
t = t.replace('template.palettes.forEach(', 'com.mna.tools.MNAReflection.structurePalettes(template).forEach(')
save(p, t)
print('28 ok')

# 29 models renderToBuffer
for fn in ['AncientWizardModel.java', 'BubbleBoatModel.java', 'DemonStoneModel.java', 'MagicBroomModel.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'public void renderToBuffer\(PoseStack (\w+), VertexConsumer (\w+), int (\w+), int (\w+), float (\w+), float (\w+), float (\w+), float (\w+)\) \{',
               r'public void renderToBuffer(PoseStack \1, VertexConsumer \2, int \3, int \4, int color) {', t)
    save(p, t)
print('29 ok')

# 30 ConstructDecorationModels
p = find1('ConstructDecorationModels.java')
t = load(p)
t = t.replace('List<ResourceLocation> all = this.eye_models.entrySet().stream().flatMap(e -> ((List)e.getValue()).stream()).map(m -> m.rLoc).collect(Collectors.toList());',
              'List<ResourceLocation> all = this.eye_models.entrySet().stream().flatMap(e -> ((List<ConstructMutexedModel>)e.getValue()).stream()).map(m -> m.rLoc).collect(Collectors.toList());')
save(p, t)
print('30 ok')

# 31 BrokerModel
p = find1('BrokerModel.java')
t = load(p)
t = t.replace('super.setCustomAnimations((GeoAnimatable)animatable, instanceId, animationState);', 'super.setCustomAnimations((GeoAnimatable)animatable, instanceId, (software.bernie.geckolib.animation.AnimationState)animationState);')
save(p, t)
print('31 ok')

# 32 AMRecipeBaseSerializer streamCodec
p = find1('AMRecipeBaseSerializer.java')
t = load(p)
if 'streamCodec' not in t:
    anchor = '    public final void toNetwork(FriendlyByteBuf buffer, T recipe) {'
    idx = t.find(anchor)
    if idx != -1:
        end = t.find('\n    }', idx)
        ins = '''
    @Override
    public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec() {
        return net.minecraft.network.codec.StreamCodec.of((buf, recipe) -> this.toNetwork(buf, recipe), buf -> this.fromNetwork(null, buf));
    }'''
        t = t[:end + len('\n    }')] + '\n' + ins + t[end + len('\n    }'):]
        save(p, t)
print('32 ok')

# 33 ClientProxy onPlaySound
p = find1('ClientProxy.java')
t = load(p)
t = t.replace('public void onPlaySound(SoundInstance inst, WeighedSoundEvents weight) {', 'public void onPlaySound(SoundInstance inst, WeighedSoundEvents weight, float range) {')
save(p, t)
print('33 ok')
print('FIM')
