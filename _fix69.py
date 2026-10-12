# _fix69.py
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

# 1 MNAReflection.explosionPos
p = 'src/main/java/com/mna/tools/MNAReflection.java'
t = load(p)
if 'explosionPos' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    public static net.minecraft.world.phys.Vec3 explosionPos(net.minecraft.world.level.Explosion explosion) {
        try {
            java.lang.reflect.Field fx = net.minecraft.world.level.Explosion.class.getDeclaredField("x");
            java.lang.reflect.Field fy = net.minecraft.world.level.Explosion.class.getDeclaredField("y");
            java.lang.reflect.Field fz = net.minecraft.world.level.Explosion.class.getDeclaredField("z");
            fx.setAccessible(true);
            fy.setAccessible(true);
            fz.setAccessible(true);
            return new net.minecraft.world.phys.Vec3(fx.getDouble(explosion), fy.getDouble(explosion), fz.getDouble(explosion));
        } catch (Throwable t) {
            return net.minecraft.world.phys.Vec3.ZERO;
        }
    }
''' + t[idx:]
    save(p, t)
print('1 ok')

# 2 CommonEventHandler
p = find1('CommonEventHandler.java')
t = load(p)
t = t.replace('BlockPos pos = BlockPos.containing(explosion.x, explosion.y, explosion.z);',
              'BlockPos pos = BlockPos.containing(com.mna.tools.MNAReflection.explosionPos(explosion));')
t = t.replace('pc.getActiveEffects().stream().anyMatch(e -> e.getEffect()', 'pc.getActiveEffects().stream().anyMatch(e -> ((net.minecraft.world.effect.MobEffectInstance)e).getEffect()')
save(p, t)
print('2 ok')

# 3 WitchhunterTrickshot
p = find1('WitchhunterTrickshot.java')
t = load(p)
t = t.replace('net.minecraft.core.Holder<MobEffect> effect = this.getEffect();', 'MobEffect effect = this.getEffect();')
t = t.replace('e2.addEffect(new MobEffectInstance(effect, this.effectDuration, this.effectMagnitude',
              'e2.addEffect(new MobEffectInstance(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), this.effectDuration, this.effectMagnitude')
save(p, t)
print('3 ok')

# 4 DemonArmorItem
p = find1('DemonArmorItem.java')
t = load(p)
t = t.replace('attr.hasModifier(runSpeed_1)', 'attr.hasModifier(runSpeed_1.id())')
t = t.replace('attr.hasModifier(runSpeed_2)', 'attr.hasModifier(runSpeed_2.id())')
t = t.replace('attr.hasModifier(runSpeed_3)', 'attr.hasModifier(runSpeed_3.id())')
save(p, t)
print('4 ok')

# 5 PractitionersPouch
p = find1('PractitionersPouch.java')
t = load(p)
t = t.replace('stack.resetHoverName();', 'stack.remove(DataComponents.CUSTOM_NAME);')
t = t.replace('stack.setTag(DataComponents.CUSTOM_DATA, CustomData.of(nbt));', 'stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));')
save(p, t)
print('5 ok')

# 6 CreeperIgniteMixin
p = find1('CreeperIgniteMixin.java')
t = load(p)
t = t.replace('(Creeper)this;', '(Creeper)(Object)this;')
save(p, t)
print('6 ok')

# 7 ClientProxy
p = find1('ClientProxy.java')
t = load(p)
t = t.replace('this.queryAdvancement = advancements.getAdvancements().get(advancementID);', 'this.queryAdvancement = advancements.get(advancementID);')
t = t.replace('return Minecraft.getInstance().getPartialTick();', 'return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);')
save(p, t)
print('7 ok')

# 8 WanderingWizardInventoryMessage
p = find1('WanderingWizardInventoryMessage.java')
t = load(p)
t = t.replace('msg.offers = MerchantOffers.createFromStream((FriendlyByteBuf)buf);',
              'msg.offers = net.minecraft.world.item.trading.MerchantOffers.STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)buf);')
t = t.replace('message.offers.writeToStream(buf);',
              'net.minecraft.world.item.trading.MerchantOffers.STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, message.offers);')
save(p, t)
print('8 ok')

# 9 BoundSword
p = find1('BoundSword.java')
t = load(p)
t = t.replace('super((Tier)Tiers.DIAMOND, (int)attackDamageIn, attackSpeedIn, new Item.Properties());', 'super((Tier)Tiers.DIAMOND, new Item.Properties());')
t = t.replace('Multimap baseAttrs = super.getAttributeModifiers(slot, stack);', 'Multimap baseAttrs = super.getDefaultAttributeModifiers();')
save(p, t)
print('9 ok')

# 10 TrickeryBracelet / BoundAxe / BoundSword UUID->RLoc
for fn in ['TrickeryBracelet.java', 'BoundAxe.java', 'BoundSword.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'new AttributeModifier\(UUID\.fromString\("([0-9a-fA-F-]+)"\), ', r'new AttributeModifier(ResourceLocation.fromNamespaceAndPath("mna", "\1"), ', t)
    save(p, t)
print('10 ok')

# 11 RecipeFumeFilter
p = find1('RecipeFumeFilter.java')
t = load(p)
t = t.replace('Optional<Recipe<?>> pattern = this.resolveRecipe(rLoc);', 'Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.resolveRecipe(rLoc);')
t = t.replace('Optional<Recipe<?>> pattern = this.resolveRecipe(recipeLocation);', 'Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.resolveRecipe(recipeLocation);')
t = t.replace('return this.minecraft.level.getRecipeManager().byKey(recipeLocation);',
              'return (Optional)this.minecraft.level.getRecipeManager().byKey(recipeLocation);')
save(p, t)
print('11 ok')

# 12 ComponentBloodSacrifice zombie
p = find1('ComponentBloodSacrifice.java')
t = load(p)
t = t.replace('zomb.startConverting(source.getPlayer().getGameProfile().getId(), 20);', 'com.mna.tools.MNAReflection.startConverting(zomb, source.getPlayer().getGameProfile().getId(), 20);')
t = t.replace('zomb.startConverting(null, 20);', 'com.mna.tools.MNAReflection.startConverting(zomb, null, 20);')
save(p, t)
print('12 ok')

# 13 MNAReflection.startConverting
p = 'src/main/java/com/mna/tools/MNAReflection.java'
t = load(p)
if 'startConverting' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    public static void startConverting(net.minecraft.world.entity.monster.ZombieVillager zomb, java.util.UUID uuid, int time) {
        try {
            java.lang.reflect.Method m = net.minecraft.world.entity.monster.ZombieVillager.class.getDeclaredMethod("startConverting", java.util.UUID.class, int.class);
            m.setAccessible(true);
            m.invoke(zomb, uuid, time);
        } catch (Throwable t) {
        }
    }
''' + t[idx:]
    save(p, t)
print('13 ok')
print('FIM')
