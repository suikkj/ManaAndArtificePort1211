# _fix78.py
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

# 1 ConstructNodeEvaluator: remover duplicata (bloco inserido)
p = find1('ConstructNodeEvaluator.java')
t = load(p)
dup = '''    @Override
    public PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z, Mob mob) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z, mob);
    }

    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

'''
if t.count('getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext') > 1:
    t = rl(t, dup, '')
    save(p, t)
print('1 ok')

# 2 GreaterAnimus: remover bloco 5-arg duplicado + customArrow
p = find1('GreaterAnimus.java')
t = load(p)
dup2 = '''    protected void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy) {
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333) - projectile.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectile.shoot(d0, d1 + d3 * 0.2, d2, velocity, inaccuracy);
    }

'''
if t.count('shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy)') > 1:
    t = rl(t, dup2, '')
t = rl(t, '''        if (this.getMainHandItem().getItem() instanceof BowItem) {
            abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
        }''', '''        // [PORT 1.21.1] BowItem.customArrow removido''')
save(p, t)
print('2 ok')

# 3 MAParticleType super(false)
p = find1('MAParticleType.java')
t = load(p)
t = t.replace('''    public MAParticleType(ParticleColor color, ParticleFloat scale, ParticleInt life, ParticleInt lifePadding, ParticleFloat gravity, ParticleBoolean physics, ParticleItemStack stack, IParticleMoveType mover, boolean depthDisable) {
        this.color = color;''', '''    public MAParticleType(ParticleColor color, ParticleFloat scale, ParticleInt life, ParticleInt lifePadding, ParticleFloat gravity, ParticleBoolean physics, ParticleItemStack stack, IParticleMoveType mover, boolean depthDisable) {
        super(false);
        this.color = color;''')
save(p, t)
print('3 ok')

# 4 CastingResourceGuiRegistry Holder<Biome>
p = find1('CastingResourceGuiRegistry.java')
t = load(p)
t = re.sub(r'Holder biome = mc\.level\.getBiome\(mc\.player\.blockPosition\(\)\);',
           'Holder<net.minecraft.world.level.biome.Biome> biome = mc.level.getBiome(mc.player.blockPosition());', t)
save(p, t)
print('4 ok')

# 5 EffectWrithingBrambles return true
p = find1('EffectWrithingBrambles.java')
t = load(p)
t = t.replace('''            pLivingEntity.getPersistentData().putDouble("brambles_old_z", pos.z);
            return;''', '''            pLivingEntity.getPersistentData().putDouble("brambles_old_z", pos.z);
            return true;''')
save(p, t)
print('5 ok')

# 6 BossMonster raw cast
p = find1('BossMonster.java')
t = load(p)
t = t.replace('java.util.List<Player> __pl = nearbyPlayers.stream().filter(p -> predicate.test((Player)p)).collect(Collectors.toList());',
              'java.util.List<Player> __pl = (java.util.List<Player>)(java.util.List)nearbyPlayers.stream().filter(p -> predicate.test((Player)p)).collect(Collectors.toList());')
save(p, t)
print('6 ok')

# 7 ConstructIsFluidInContainer: sem cast (orElse direto)
p = find1('ConstructIsFluidInContainer.java')
t = load(p)
t = t.replace('handler = (net.neoforged.neoforge.fluids.capability.IFluidHandler)FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection()).orElse(null);',
              'handler = (net.neoforged.neoforge.fluids.capability.IFluidHandlerItem)FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection()).orElse(null);')
save(p, t)
print('7 ok')

# 8 ConstructCollectFluid Player null
p = find1('ConstructCollectFluid.java')
t = load(p)
t = t.replace('bp.pickupBlock((net.minecraft.world.entity.LivingEntity)null, (LevelAccessor)c.level(), this.currentTarget, state)',
              'bp.pickupBlock((net.minecraft.world.entity.player.Player)null, (LevelAccessor)c.level(), this.currentTarget, state)')
save(p, t)
print('8 ok')

# 9 ThrownManaweaveBottle remover getGravity
p = find1('ThrownManaweaveBottle.java')
t = load(p)
t = rl(t, '''    protected double getGravity() {
        return 0.05;
    }

''', '')
t = rl(t, '''    protected float getGravity() {
        return 0.05f;
    }

''', '')
save(p, t)
print('9 ok')

# 10 BrokerModel animatable direto
p = find1('BrokerModel.java')
t = load(p)
t = t.replace('super.setCustomAnimations((GeoAnimatable)animatable, instanceId, (software.bernie.geckolib.animation.AnimationState)animationState);',
              'super.setCustomAnimations(animatable, instanceId, animationState);')
save(p, t)
print('10 ok')

# 11 renderToBuffer callsites -> 5 args
for fn in ['OrangeChickenRenderer.java', 'BubbleBoatRenderer.java', 'SpectralElytraLayer.java', 'AncientCouncilRenderer.java', 'DemonStoneRenderer.java', 'FlatLandsRenderer.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'renderToBuffer\((\w+), (\w+), (\w+), (\w+), [^;]+\);', r'renderToBuffer(\1, \2, \3, \4, -1);', t)
    save(p, t)
print('11 ok')

# 12 super ctor EntityModel casts
for fn, old, new in [
    ('ConstructAssemblyStandRenderer.java', 'super(p_173915_, (EntityModel)new ConstructAssemblyStandModel(p_173915_.', 'super(p_173915_, new ConstructAssemblyStandModel(p_173915_.'),
    ('AnimusBlockRenderer.java', 'super(pContext, (EntityModel)new SlimeModel(pContext.bakeLayer(ModelLaye', 'super(pContext, new SlimeModel(pContext.bakeLayer(ModelLaye'),
    ('GreaterAnimusRenderer.java', 'super(ctx, (HumanoidModel)new GreaterAnimusModel(ctx.bakeLayer(ModelLaye', 'super(ctx, new GreaterAnimusModel(ctx.bakeLayer(ModelLaye'),
]:
    p = find1(fn)
    t = load(p)
    t = t.replace(old, new)
    save(p, t)
print('12 ok')

# 13 ConstructRenderer setNormal
p = find1('ConstructRenderer.java')
t = load(p)
t = t.replace('.setNormal(pose.normal(), (float)normal.x', '.setNormal(pose, (float)normal.x')
save(p, t)
print('13 ok')

# 14 WitchhunterTrickshotRenderer setNormal
p = find1('WitchhunterTrickshotRenderer.java')
t = load(p)
t = re.sub(r'\.setNormal\(\w+\.normal\(\), ', '.setNormal(matrix, ', t)
save(p, t)
print('14 ok')

# 15 FaerieQueenRenderer + LichRenderer MAPFX
for fn in ['FaerieQueenRenderer.java', 'LichRenderer.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('java.util.Optional<MAPFXProvider> pfxCap = MNACap.get(pLivingEntity, MAPFXProvider.MAPFX);', 'java.util.Optional<MAPFX> pfxCap = MNACap.get(pLivingEntity, MAPFXProvider.MAPFX);')
    t = t.replace('((MAPFX)pfxCap.get()).getFlag((LivingEntity)pLivingEntity, MAPFX.Flag.MIST_FORM)', 'pfxCap.get().getFlag((LivingEntity)pLivingEntity, MAPFX.Flag.MIST_FORM)')
    save(p, t)
print('15 ok')

# 16 CommonEventHandler getAllEffects
p = find1('CommonEventHandler.java')
t = load(p)
t = t.replace('pc != null && pc.getActiveEffects().stream()', 'pc != null && pc.getAllEffects().stream()')
save(p, t)
print('16 ok')

# 17 DamageEventHandler setCriticalHit
p = find1('DamageEventHandler.java')
t = load(p)
t = t.replace('''            if (curio.isPresent()) {
                event.setCanceled(true);''', '''            if (curio.isPresent()) {
                event.setCriticalHit(false);''')
save(p, t)
print('17 ok')

# 18 PotionEventHandler value()
p = find1('PotionEventHandler.java')
t = load(p)
t = t.replace('PotionEventHandler.handlePotionRemoved(event.getEffect(), event.getEntity());', 'PotionEventHandler.handlePotionRemoved(event.getEffect().value(), event.getEntity());')
t = t.replace('PotionEventHandler.handlePotionRemoved(event.getEffectInstance().getEffect(), event.getEntity());', 'PotionEventHandler.handlePotionRemoved(event.getEffectInstance().getEffect().value(), event.getEntity());')
save(p, t)
print('18 ok')

# 19 SimpleWizardLabContainer cast V
p = find1('SimpleWizardLabContainer.java')
t = load(p)
t = t.replace('this(type, i, playerInventory, (WizardLabTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));', 'this(type, i, playerInventory, (V)(WizardLabTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos()));')
save(p, t)
print('19 ok')

# 20 ImageItemStackButton
p = find1('ImageItemStackButton.java')
t = load(p)
t = t.replace('List lines;', 'java.util.List<Component> lines;')
t = t.replace('ArrayList<Object> tt = new ArrayList<Object>();', 'java.util.List<Component> tt = new java.util.ArrayList<Component>();')
save(p, t)
print('20 ok')

# 21 MAExplosion Holder sound
p = find1('MAExplosion.java')
t = load(p)
t = t.replace('mode, null, null, SoundEvents.GENERIC_EXPLODE.value()));', 'mode, null, null, SoundEvents.GENERIC_EXPLODE));')
save(p, t)
print('21 ok')

# 22 WanderingWizardRenderer cast
p = find1('WanderingWizardRenderer.java')
t = load(p)
t = t.replace('this.getTextureLocation((Entity)entityIn)', 'this.getTextureLocation((com.mna.entities.faction.WanderingWizard)entityIn)')
save(p, t)
print('22 ok')
print('FIM')
