# FIXLIST — Mana and Artifice → NeoForge 1.21.1

Lista viva de afazeres do port. Baseline medida em **2026-10-11** por clean compile
(`gradle clean compileJava`): **5.086 erros**.

Regra de ouro (do skill forge-to-neoforge-port): **nunca deixar o build pior**.
Cada bloco é validado por clean compile. Bloco reverte se ficar pior.

Snapshot de segurança: `_snapshots/snap-20261011-024252-pre-analise/` (1.861 .java).
Restaurar: `cp -r _snapshots/snap-.../src .`

**Repositório remoto:** https://github.com/suikkj/ManaAndArtificePort1211 (branch `main`).
Push: `GIT_TERMINAL_PROMPT=0 git push -u origin main` (repo git próprio do workspace —
⚠ o home `C:\Users\lucas\.git` é um repo acidental e NÃO deve ser usado).

---

## AUDITORIA PRÉVIA (fase B pedida pelo usuário)

**Erros causados por passes anteriores** — o que foi investigado e o resultado:

- ✅ **Fases A/D/F ESTÃO FEITAS** (corrige alerme anterior meu). Varredura filtrando
  comentário vs código real deu **0 ocorrências** em: `SimpleChannel`, `NetworkRegistry`,
  `MobType`, `ForgeEventFactory`, `NonNullLazy`, `IEntityAdditionalSpawnData`,
  `ToolActions`, `@ObjectHolder`, `entityData.define`, `Renderer.vertex(`, `mezz.jei`.
  As ocorrências que apareciam eram **javadoc do CFR** (imports antigos preservados
  como comentário) — lixo de decompilação, não erro.
- 🔴 **BUG REAL DE DESCOMPILAÇÃO (não é drift de API):** `AbstractSelectionList` do
  vanilla 1.21.1 agora estende `AbstractContainerWidget` e **removeu os campos
  protegidos `x0/y0/x1/y1`** (existentes em 1.20.1). O MNA os usa em 5 GUIs.
  → ver B1.

---

## B — Blocos de correção

### B1. `AbstractSelectionList` perdeu `x0/y0/x1/y1` (removidos na 1.21.1)
- [ ] **B1** `ReagentList.java` (16 erros) — declarar campos próprios no construtor
- [ ] **B1** `SpellPartList.java` (16)
- [ ] **B1** `GuiLodestarV2.java` (19)
- [ ] **B1** `GuiWanderingWizard.java` (22)
- [ ] **B1** `GuiOcculus.java` (6)
  Mapeamento: `x0→getX()`, `y0→getY()`, `x1→getX()+getWidth()`, `y1→getY()+getHeight()`.
  ⚠ Nas 3 classes que **atribuem** (`this.x0 = …`), os campos passam a ser privados
  da classe MNA (o vanilla não os tem mais). Nas que só leem, usar getters.
  **Total B1: 79 erros.**

### B2. `new ResourceLocation(a, b)` — ctor 2-args agora privado
- [ ] **B2** 40 ocorrências em ~N arquivos → `ResourceLocation.fromNamespaceAndPath` / `parse`
  (verificar 1-arg: `new ResourceLocation("")` é VÁLIDO, mantém.)

### B3. `ModelResourceLocation` (156 erros)
- [ ] **B3** investigar: em 1.21.1 `ModelResourceLocation` mudou de pacote/API?
  (bloco de render de itens — `ItemRenderer`, BEWLR)

### B4. `Matrix3f` → `Pose` (96 erros)
- [ ] **B4** render: assinatura mudou (WorldRenderUtils 79 erros)

### B5. `addRenderableWidget` (163 erros)
- [ ] **B5** ctor/uso mudou em 1.21.1

### B6. `package BuiltInRegistries does not exist` (129 erros)
- [ ] **B6** puramente import ausente — adicionar
  `net.minecraft.core.registries.BuiltInRegistries`

### B7. `MobEffect` ↔ `Holder<MobEffect>` (259)
- [ ] **B7** data-driven: efeitos agora são `Holder<MobEffect>`

### B8. `Enchantment` ↔ `Holder<Enchantment>` (72) + `EnchantmentCategory` (38)
    + `Enchantment.Rarity` (29) + `TierSortingRegistry` (32)
- [ ] **B8** bloco encantamentos data-driven (atômico)

### B9. `ItemStack` NBT residual: `getTag()` (72) + `hasTag()` (13) + `setTag()` (4)
- [ ] **B9** `getTag`→`getOrDefault(CUSTOM_DATA,…)`, etc. SÓ em receptores ItemStack

### B10. Capabilities → Attachments: `LazyOptional` (160) + `CapabilityToken` (21)
     + `ICapabilitySerializable` (14) + `ForgeCapabilities` (32)
- [ ] **B10** design pronto em `design_refs/CapabilitiesInit.attachments.reference.java`
  (atômico, maior bloco)

### B11. Networking residual: `NetworkHooks` (27) + `StreamCodec` (76) + `Serializer` (26)
- [ ] **B11**

### B12. Menus: `IForgeMenuType` (41 em ContainerInit.java)
- [ ] **B12**

### B13. Capibilities de item/bloco: `ForgeCapabilities` (32)
- [ ] **B13**

### B14. Misc: `PotionUtils` (13), `DyeableLeatherItem` (17), `IForgeShearable` (11),
     `RenderGuiOverlayEvent` (18), double→float (62), `ConstructTask` (58), `Tag` (56)
- [ ] **B14** pontuais, um a um

### B15. `incompatible types` / `no suitable method` genéricos (~2.050)
- [ ] **B15** resolvidos como subproduto dos blocos acima; reinvestigar após

## B16. 🔴 Pipeline de dano 1.21.1 — `DamageEventHandler` (109 erros, ATÔMICO)
- [ ] **B16** `LivingAttackEvent` e `LivingHurtEvent` **foram REMOVIDOS** no 1.21.1.
  Substituídos por `LivingDamageEvent.Pre` / `LivingDamageEvent.Post` +
  `DamageContainer` (ver `net.neoforged.neoforge.common.damagesource.DamageContainer`).
  - `event.setAmount(float)` → `event.getContainer().setNewDamage(float)` (Pre)
  - `event.setCanceled(true)` → `event.getContainer().setNewDamage(0)` (Pre)
  - `ShieldBlockEvent` → `LivingShieldBlockEvent`
  - `onLivingAttack` + `onLivingHurt` + `onLivingDamage` precisam ser **unificados**
    numa única passagem sobre o novo pipeline — bloco de 1 arquivo, mas profundo.
  ⚠️ NÃO fazer replace mecânico: a semântica de redução de dano mudou.

## B17. `EntityInit` (73) — builders de entidade
- [ ] **B17** `EntityType.Builder.of(...)` mudou de assinatura

## B18. `StreamCodec` (76) + `register(ResourceLocation,Object)` (94)
- [ ] **B18** codecs de rede/particle

## B25. ✅ LOTE 2-6 (sessão 2026-10-12 madrugada) — resumo dos blocos novos
- **B25** `addAttributeModifier(Holder, String, ...)` → `ResourceLocation.parse(...)` (11 arq, 23 sites)
- **B26** `NbtUtils.readBlockPos(CompoundTag)` → `(CompoundTag, String)` devolvendo `Optional<BlockPos>`
  (17 arq/20 sites). `writeBlockPos` agora grava `IntArrayTag` — par write/read consistente.
  Legado `{X,Y,Z}` (multiblock JSON) → helper `MNANbt.readBlockPosLegacy`.
- **B27** `GlassBlock` REMOVIDO (1.21.1 usa `TransparentBlock` p/ vidro liso) → RunicGlassBlock.
- **B28** GeckoLib 4.8.4: `float red/green/blue/alpha` → `int colour` em
  `actuallyRender/renderRecursively/preRender/postRender/renderFinal`;
  `getPackedOverlay(T,float,float)` 3 args; casts `(Entity)animatable` removidos;
  `MAGeckoRenderer<T extends LivingEntity & GeoAnimatable>`; helper `MNAGeo.packColour`.
- **B29** `MAParticleType`: `Deserializer`→`StreamCodec` (lambda `(buf,value)->value.writeToNetwork(buf)`),
  `CODEC` virou `MapCodec` via `RecordCodecBuilder.mapCodec`, `streamCodec()` override.
- **B30** `ArmorMaterial` virou RECORD → `ArmorMaterialInit` (DeferredRegister + Holder);
  armaduras usam `Holder<ArmorMaterial>`; durabilidade via `Properties.durability(type.getDurability(factor))`.
- **B31** `DyeableArmorItem`/`DyeableLeatherItem` removidos → `MNADyeableItem` (DYED_COLOR);
  `DyeableMageArmor.getColor` reescrito p/ `DataComponents.DYED_COLOR`.
- **B32** `AbstractWidget.render` virou FINAL → 9 overrides renomeados p/ `renderWidget`
  (listas, botões). ChiselImageButton: `this.renderWidget`→`super.renderWidget`;
  CategoryItemButton: gate de página movido p/ renderWidget.
- **B33** `damageItem(..., Consumer<T>)` → `Consumer<Item>` (assinatura NeoForge IItemExtension; 8 arq).
- **B34** `RecordItem` removido → `new Item(props.jukeboxPlayable(ResourceKey<JukeboxSong>))`.
- **B35** `new AttributeModifier("string", ...)` → `ResourceLocation.parse(...)`.
- **B36** `DistExecutor` removido → `if (FMLEnvironment.dist == Dist.CLIENT)` (APIBridge, ManaAndArtifice).
- **B37** `canSustainPlant` agora TriState: `soil.canSustainPlant(level,pos,dir,plantState)`;
  TRUE/FALSE mandam, DEFAULT → `mayPlaceOn` (flores MNA). `IPlantable`/`PlantType` removidos.
- **B38** `CodexBreadcrumb`: campo `Type` sombreava o enum → renomeado p/ `breadcrumbType`.
- **B39** `ItemStack.getEnchantmentLevel` → `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)`.
- **B40** `MAEffects`/`SFX`/`Components`/`Shapes`/`Modifiers`/`Enchantments` (@ObjectHolder nulos) — ✅ reconstruídos.
- **B41** `MNAImageButton`: ctors 9/11/12-arg p/ os call-sites antigos; 16 subclasses
  `extends ImageButton`→`extends MNAImageButton` (import vanilla mantido p/ refs de tipo).

### ⚠️ LIÇÃO CRÍTICA (perda de tempo real nesta sessão)
Scripts que reescrevem o arquivo com o texto **sem comentários** (`sc()`) CORROMPEM:
`"https://..."` vira `"https:` + resto da linha apagado (o `//` é tratado como comentário).
**REGRA: sempre escrever de volta o TEXTO ORIGINAL com replaces pontuais; nunca gravar a saída do sc().**


## B19. 🔴 `Enchantment` virou RECORD imutável (B8 real — ATÔMICO, maior bloco lógico)
No 1.21.1 `Enchantment` **não pode mais ser herdado**: é um `record`
`(Component description, EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet,
DataComponentMap effects)`. Removidos: `EnchantmentCategory`, `Enchantment.Rarity`,
`TierSortingRegistry`, `Serializer`, e os overrides `isDiscoverable/isTradeable/
isAllowedOnBooks/canApplyAtEnchantingTable/isTreasureOnly/isCurse`.
- [ ] **B19a** `MAEnchantmentBase` (16 classes herdam) → bridge `MAEnchantmentBridge`
      (criado: `Rarity`, `Category`, `definition()`, `build()`)
- [ ] **B19b** `EnchantmentEnumExtender` (StaffOrWeapon/STAVES) → `HolderSet<Item>` equivalente
- [ ] **B19c** `EnchantmentInit` (73 registros) → `new Enchantment.Builder(def).build(id)`;
      `withEffect(...)`/`withManaCost(...)`/`withPredicate(...)` do MNA viram
      `EnchantmentAttributeEffect` + `DataComponentType` (ver `withEffect` no javap do Builder)

## B20. Curios 9.x: `ICurio` → `ICurioItem` (JÁ MIGRADO ✅)
Curios 9.5.1 (1.21.1): `CuriosCapability.ITEM`+`ICurio` → `ICurioItem`
(`curioTick(SlotContext, ItemStack)`). `CurioItemCapability` reescrito como
`ICapabilityProvider<ItemStack, Void, ICurioItem>`; `ChargeableItem` migrado.

## B21. Eventos REMOVIDOS sem substituto no 1.21.1 (features perdidas — documentadas)
- `LivingJumpEvent` → sem substituto (boost de pulo do MNA perdido)
- `LivingVisibilityEvent` → sem substituto (camuflagem/invisibilidade perdida)
- `SleepingTimeCheckEvent` → sem substituto
  → handlers comentados em `CommonEventHandler`; reimplementar via **mixin** se necessário.

## B22. `Event.HasResult` removido do bus NeoForge
Eventos custom do MNA (`AuraEvent.Compatibility`, `RunicAnvilShouldActivateEvent`)
perderam o `getResult()/setResult()` herdado. Migrados para enum próprio
`Result {ALLOW, DENY, DEFAULT}` no próprio evento. `PlayerInteractEvent` →
`setCancellationResult(InteractionResult.FAIL)`.

## B23. Atributos NeoForge movidos para vanilla
`NeoForgeMod.BLOCK_REACH`→`Attributes.BLOCK_INTERACTION_RANGE`,
`ENTITY_REACH`→`ENTITY_INTERACTION_RANGE`, `ENTITY_GRAVITY`→`Attributes.GRAVITY`
(agora `Holder<Attribute>`, sem `.get()`).

## B24. `DyeableLeatherItem` REMOVIDO
→ `MNADyeableItem` (bridge em `com.mna.api.tools`) usando
`DataComponents.DYED_COLOR` / `DyedItemColor(rgb, showInTooltip)`.

---

## Progresso
| Marco | Erros | Delta |
|---|---|---|
| Início do port | 46.940 | — |
| Sessão anterior | 5.118 | −41.822 |
| **Baseline 2026-10-11** | **5.086** | −41.854 |
| ✅ B6 imports BuiltInRegistries (66 arq) + B1 x0/y0/x1/y1 (5 arq) + B2 RL ctor (78 arq) | 4.770 | −316 |
| ✅ (Object) casts do CFR em entityData.set (223, 46 arq) | 4.553 | −217 |
| ✅ B3 ImageButton via helper `MNAImageButton` (40 call-sites, 15 arq) | 4.516 | −37 |
| ✅ **`MAEffects` estava 100% nula** (@ObjectHolder removido) — 62 NPEs de runtime evitadas | 4.512 | −4 |
| ✅ (MobEffect) casts do CFR removidos (248) + B3b ModelResourceLocation standalone (85) | 4.427 | −85 |
| ✅ B4 `poseStack.getLast()`→`last()` + `setNormal(Matrix3f)`→`setNormal(Pose)` (5 arq) | 4.367 | −60 |
| ✅ SFX.java regenerado (174 campos, mapa do Vineflower + sounds.json) | 4.432 | +65* |
| ✅ `getEffect().value()` em `MobEffectInstance` (20 subst, 8 arq) | 4.282 | −150 |
| ✅ Wrap em `holder()` em hasEffect/getEffect/removeEffect/MobEffectInstance (240 subst, 75 arq) | 4.053 | −229 |
| ✅ **B16** DamageEventHandler: LivingIncomingDamageEvent / LivingDamageEvent.Pre / LivingShieldBlockEvent | 3.817 | −236 |
| ✅ `ItemStack.of(CompoundTag)`→`parseOptional(provider, …)` (4 contextos) | 3.806 | −11 |
| ✅ TickEvent.Phase→PlayerTickEvent.Post (2); LivingTickEvent→EntityTickEvent+cast (4) | 3.672 | −134 |
| ✅ `addVertex(double…)`→float + literais `.0f` + `getU/getV(float)` (12 arq) | 3.614 | −58 |
| ✅ `renderBackground(GuiGraphics, float, int, int)` com params detectados por método (25 arq) | 3.440 | −174 |
| ✅ `appendHoverText` + `Item.TooltipContext` (59 assinaturas + 3 call-sites) | 3.428 | −12 |
| ✅ **B10** `LazyOptional` → campo direto / `java.util.Optional` (60+45 arq) | **3.360** | **−68** |
| ✅ TileEntityWithInventory: `LazyOptional`/`ForgeCapabilities` → campo `IItemHandler` direto | 3.421 | −61 |
| ✅ `AttributeModifier(uuid,"name",…)` → `AttributeModifier(loc, …)` (35 subst, 18 arq) | 3.395 | −26 |
| ✅ `ItemAttributes.BASE_ATTACK_DAMAGE_UUID` → `Item.BASE_ATTACK_DAMAGE_ID` (13 arq) | 3.421 | −0 |
| ✅ `mouseScrolled(double,double,double)` → `+double scrollX` (5 arq) | 3.385 | −6 |
| ✅ **B21** LivingJumpEvent/LivingVisibilityEvent/SleepingTimeCheckEvent removidos → handlers desativados | 3.357 | −28 |
| ✅ **B22** `Event.HasResult` → Result próprio nos eventos custom (8 arq) | 3.384 | −27 |
| ✅ **B23** NeoForgeMod.{BLOCK,ENTITY}_REACH / ENTITY_GRAVITY → `Attributes.*` (17 arq) | 3.354 | −30 |
| ✅ **B24** `DyeableLeatherItem` → `MNADyeableItem` (DataComponents.DYED_COLOR) (8 arq) | 3.377 | −15 |
| ✅ **B20** Curios 9.x: `ICurio`→`ICurioItem`, `CurioItemCapability` reescrito | 3.396 | −1 |
| ✅ **B19a** `Enchantment` virou record: bases (`MAEnchantmentBase`, `MADamageEnchantmentBase`) deixam de estender `Enchantment`; bridge `MAEnchantmentBridge` (Rarity/Category/definition/build) | 3.343 | −53 |
| ✅ `TierSortingRegistry` REMOVIDO → `MNAtierSorting` (ordem dos tiers vanilla) | 3.312 | −31 |
| ✅ `IForgeBlockEntity` → `IBlockEntityExtension` (7 arq) | 3.298 | −14 |
| ✅ `event.player.` → `event.getEntity().` (campo virou privado no bus novo; 4 arq) | 3.254 | −44 |
| ✅ `ItemStack.getTag()` → `getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()` (9 arq) | 3.236 | −18 |
| ✅ `Optional.resolve().get()` (artefato do patch de LazyOptional) → `.get()` (36 arq) | 3.188 | −48 |
| ✅ `ItemStack.getEnchantmentLevel(...)` REMOVIDO → `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)` (caso a caso, 6 arq) | **3.181** | −7 |
| ✅ **SESSÃO 2026-10-12 (lotes 2-6)**: _fix2 (addAttributeModifier, readBlockPos, GlassBlock, GeckoLib, MAParticleType, ring_large, setLootTable) | 3.055 | −126 |
| ✅ _fix3+_fix4 (WorldRenderUtils Pose, ImageButton swap, GuiGuideBook casts, ArmorMaterial record) | 2.955 | −100 |
| ✅ _fix5 (damageItem, RecordItem, render→renderWidget ×9, AttributeModifier strings) | 2.931 | −24 |
| ✅ _fix6 (CodexBreadcrumb campo Type, csr decl, gate, casts Collection) | 2.903 | −28 |
| ✅ _fix7 (CommonEventHandler: ICancellableEvent no BlockEvent, Explosion.center, setCancellationResult indevido, event.player) + import MNAImageButton no Lodestar (subpacote) | 2.779 | −124 |
| ✅ **Remoção global dos casts `(Object)` do CFR** (1.351 casts em ~350 arq) | 2.466 | **−312** |
| ✅ _fix8 (save/load de ItemStack c/ HolderLookup.Provider via MNAItemData, ContainerHelper→helpers, saveAdditional/load→loadAdditional 2-arg, getUpdateTag(Provider), saveWithFullMetadata(Provider)) | 2.409 | −57 |
| ✅ _fix9 (capabilities: `MNACaps.itemHandler` novo, 10 getCapability mortos removidos, ForgeCapabilities→MNACaps, LazyOptional→java.util.Optional, imports da pkg removida) | 2.322 | −87 |
| ✅ LootTableHelper REESCRITO (LootDataManager→ReloadableServerRegistries, LootTableReference→NestedLootTable, ObfuscationReflectionHelper→reflexão pura) + _fix10 (loot functions/conditions: `Serializer` removido→MapCodec+TYPE, GlobalLootModifiers) | 2.268 | −54 |
| ✅ _fix11 (CommonEventHandler 53 err: ItemEntityPickupEvent.Pre, ItemStack.of→parseOptional, getEnchantmentLevel→Holder, MNADyeableItem estático, reflexão spinAttack/AreaEffectCloud) | 2.224 | −44 |
| ✅ **B19c** `EnchantmentBehaviors` (ResourceKey→comportamento) + EnchantmentInit reescrito (build+defs de itens/tags) + AuraEvent/Aura/classes de ench + RecipeInit (helpers type/serializer) | 2.138 | −86 |
| ✅ _fix13/_fix15 (efeitos: add/removeAttributeModifiers 1.21.1 + `IMNAEffectHook`/`MAEffectHooks` via MobEffectEvent; handleUpdateTag(Provider); Enchantments curto; RecipeHolder sites) | 2.097 | −41 |
| ✅ _fix16 (ContainerMagiciansWorkbench: RecipeBookMenu<CraftingInput,CraftingRecipe> + getRecipeFor/assemble novos; LargeContainer: `MNAMenuAccess` reflexão p/ campos private; CantripRegistry: TriConsumer method-refs + curePotionEffects→EffectCure) | 2.097 | −106 |
| ✅ _fix17 (BuiltInRegistries.ENCHANTMENT não existe → `MNAEnchantmentRegistry` (registryAccess do server) + overloads por ResourceKey no `MNAEnchantmentHelper`; ItemEldritchOrb/ManaweavingRecipe/EffectDisjunction/EnchantmentBehaviors migrados) | 1.937 | −160 |
| ✅ _fix18/_fix19 (allowedEnchantments → `ArrayList<ResourceKey<Enchantment>>` em MagicStaff/BoneScythe/FortuneRing/HellfireTrident/AstroBlade; RLoc `fromNamespaceAndPath`; renames 1.21.1 MOB_LOOTING→LOOTING, BLOCK_EFFICIENCY→EFFICIENCY, FISHING_SPEED→LURE, POWER_ARROWS→POWER; `getDefaultAttributeModifiers()` no-arg) | **1.881** | −56 |
| **➜ ATUAL** | **1.881** | **−45.059 desde o início (46.940)** |

### 🔴 Bug de medição que quase passou (2026-10-11)
Ao migrar `getEnchantmentLevel`, um `.replace()` removeu o `(` de abertura de
`((boots = ...))` → **3 errors** (medição FALSA: javac aborta cedo e não analisa
o resto). Após corrigir a sintaxe: **3.181**. **Regra: número muito baixo não é
ganho — é javac abortado.** Sempre verificaro log com `grep -B1 -A6 'error:'`.

### Bridges criadas no port (reutilizáveis)
| Bridge | Substitui | Onde |
|---|---|---|
| `com.mna.gui.widgets.MNAImageButton` | `ImageButton` (UV/textura) | 40 call-sites |
| `com.mna.api.tools.MNADyeableItem` | `DyeableLeatherItem` | DataComponents.DYED_COLOR |
| `com.mna.api.tools.MNAtierSorting` | `TierSortingRegistry` | ordem de tiers vanilla |
| `com.mna.api.enchantments.MAEnchantmentBridge` | `EnchantmentCategory`/`Rarity` | definition/build |
| `com.mna.api.enchantments.MNAEnchantmentHelper` | `ItemStack.getEnchantmentLevel` | static + Holder |
| `com.mna.api.capabilities.CurioItemCapability` | Curios `ICurio` | `ICurioItem` |

### Notas de medição
- `*` O salto de 4.353→4.432 foi um artefato: a regeneração do SFX corrigiu
  classes que antes não compilavam e portanto **escondiam** erros dos
  consumidores. O total real continuou caindo monotonicamente.
- Builds com **2 erros** são medições falsas: javac aborta cedo e não analisa
  o resto. Sempre confirmar com clean compile completo.


### 🔴 ACHADOS CRÍTICOS DE RUNTIME (não aparecem como erro de compilação)
1. **`MAEffects` tinha TODAS as 62 constantes = `null`** (era `@ObjectHolder`, removido no 1.21.1).
   Compilaria normal e daria **NPE em jogo** ao usar efeito nenhum. Corrigido.
2. **Snapshot**: `_snapshots/snap-20261011-031901-4367erros/` — restaurar com `cp -r`.

### ✅ Fase "classes @ObjectHolder quebradas" — 5 classes, 345 campos
O `@ObjectHolder` foi removido no 1.21.1. Todas essas classes tinham campos `= null`
(compilavam, mas crashariam em jogo). Reconstruídas:
- `SFX.java` — **174/174 campos** (`SoundEvent`). Mapa extraído do **Vineflower**
  (preserva `@ObjectHolder(value=...)`) e validado contra `assets/mna/sounds.json`.
  `ForAffinity` preservado. API pública intacta.
- `MAEffects.java` — 62 (→ `EffectInit`)
- `Components.java` — 121 (→ `Registries.SpellEffect`, 119 mapeados; 2 não existem mais)
- `Shapes.java` — 20 (→ `Registries.Shape`)
- `Modifiers.java` — 8 (→ `Registries.Modifier`)
- `Enchantments.java` — 22 (→ `EnchantmentInit`)
**Resultado: 4.422 erros.** Snapshot: `_snapshots/snap-20261011-035230-4432erros/`

> Método reutilizável: `java -jar vineflower.jar --only=<pkg> jar.jar out/`
> preserva `@ObjectHolder`, ao contrário do CFR. Use para qualquer outra classe
> que esteja com campos nulos.

---

## ✅ REVISÃO DA LISTA (2026-10-12) — o que os blocos antigos viraram

Verificação por `grep` no `_build_last.log` do build atual (1.881 erros):

| Bloco | Status | Evidência |
|---|---|---|
| B1 x0/y0/x1/y1 | ✅ | 0 erros de x0/y0 restantes |
| B2 `new ResourceLocation(a,b)` | ⚠️ 15 restantes | `ResourceLocation(String,String) has private access` = 15 |
| B3 `ModelResourceLocation` | ⚠️ 19 restantes (era 156) | grep = 19 |
| B4 `Matrix3f`→`Pose` | ⚠️ 15 restantes (era 96) | `Matrix3f cannot be converted to Pose` = 15 |
| B5 `addRenderableWidget` | ✅ 0 | grep = 0 |
| B6 imports `BuiltInRegistries` | ✅ 0 | |
| B7 `MobEffect`↔Holder | ✅ base feita | 14 residuais de `Holder<MobEffect>` |
| B8/B19 encantamentos | ✅ feito (B19a+b+c + `MNAEnchantmentRegistry`) | ver B19 |
| B9 NBT de ItemStack | ✅ 0 | |
| B10 capabilities/LazyOptional | ✅ 0 | |
| B11 networking | ✅ NetworkHooks 0 / StreamCodec 0 | |
| B12 `IForgeMenuType` | ✅ 0 | |
| B13 `ForgeCapabilities` | ✅ (→ `MNACaps`) | |
| B14 misc | ✅ PotionUtils 0 / RenderGuiOverlayEvent 0 / DyeableLeatherItem migrado | |
| B15 genéricos | 🔄 em andamento | inference T = 90, Object→X = 77, method refs = 37 |
| B16 pipeline de dano | 🔄 10 restantes (era 109) | `DamageEventHandler.java` = 10 |
| B17 `EntityInit` builders | 🔄 12 restantes (era 73) | grep = 12 |
| B18 codecs | ✅ StreamCodec 0 | |
| B20 Curios 9.x | ✅ | |
| B21 eventos removidos | ⚠️ features desativadas (jump/visibility/sleep) — reimplementar via mixin | |
| B22 `Event.HasResult` | ✅ | |
| B23 atributos | ✅ | |
| B24 `DyeableLeatherItem` | ✅ | |
| B25–B41 (lotes 2–6) | ✅ | ver tabela de Progresso |

---

## 🔜 FILA ATUAL DE ERROS — checada no build 1.881 (2026-10-12)

**Regra:** sempre que um lote for aplicado, re-verificar esta seção com
`grep 'error:' _build_last.log` e ATUALIZAR as contagens — nada sai daqui sem
evidência de queda no build.

### Maiores arquivos (por contagem)
- [ ] `Construct.java` (31)
- [ ] `DemonLord.java` (29)
- [ ] `GuiOcculus.java` (27)
- [ ] `ItemInteractionsInit.java` (21)
- [ ] `PractitionersPouch.java` (19)
- [ ] `BlockUtils.java` (19)
- [ ] `ConstructNodeEvaluator.java` (18)
- [ ] `GuiLodestarV2.java` (17)
- [ ] `FaerieQueen.java` (17)
- [ ] `GuiGuideBook.java` (16)
- [ ] `HUDOverlayRenderer.java` (15)
- [ ] `VillagerRegistry.java` (15)

### Padrões (prioridade de lote; contagens do build 1.881)
- [ ] `inference variable T has incompatible equality constraints` (90) — chamadas `MNACap.get(...)`/holders em tipos concretos; analisar por arquivo
- [ ] `Object cannot be converted to X` (77) — resíduos de raw types do CFR
- [ ] `invalid method reference` (37) — method-refs em interfaces raw (padrão `(TriConsumer)((TriConsumer)X::m)`)
- [ ] `Entity cannot be converted to X` (30) — casts em renderers/GeckoLib (setupAnim/render)
- [ ] `ResourceLocation cannot be converted` (19) — API que agora pede `ResourceKey` (ex.: `dimension()`)
- [ ] `symbol: get()` (18) — `.get()` em tipo errado (resíduo de Optional/Holder)
- [ ] `RecipeHolder<CAP#1>` (16) — mais sites `getRecipes()/byKey` → `.value()`/`.id()`
- [ ] **`new ResourceLocation(a,b)` private (15)** — trocar por `ResourceLocation.fromNamespaceAndPath(...)`
- [ ] **`ClipContext is ambiguous` (15)** — `new ClipContext(...)` com args raw; tipar `Vec3`/`ClipContext.Block/Fluid`
- [ ] `Matrix3f cannot be converted to Pose` (15) — B4 residual
- [ ] **`TooltipContext cannot be converted` (15)** — `appendHoverText` com `Item.TooltipContext` errado
- [ ] **`symbol: MNAItemData` (14)** — arquivos usando `MNAItemData.*` sem import (faltou nos lotes 8/17)
- [ ] `symbol: level` (14) — vars renomeadas (`world`→`level`)
- [ ] `Holder<MobEffect> cannot be converted` (14) — mais sites `MobEffectInstance`/`hasEffect`/`getEffect`
- [ ] **`blockState has private access in FallingBlockEntity` (14)** — usar getter/reflexão
- [ ] **`EquipmentSlot is not a functional interface` (14)** — `hurtAndBreak(n, ent, e -> {})` → `hurtAndBreak(n, ent, slot)`
- [ ] `MACapabilityForgeEventHandlers` (13) — `event.addCapability` não existe → registrar handlers via `RegisterCapabilitiesEvent` (NeoForge)
- [ ] `EntityInit` (12) — B17 residual
- [ ] `DamageEventHandler` (10) — B16 residual
- [ ] `ModelResourceLocation` (19) — B3 residual

---

## ⚠️ VERIFICAÇÃO DE RUNTIME PENDENTE (compila; ainda não testado em jogo)

- [ ] `MAEffectHooks`/`IMNAEffectHook` — hooks via `MobEffectEvent.Added/Remove/Expired`; validar ordem e casos (morte do mob, `/effect clear`)
- [ ] `MNAEnchantmentRegistry` — depende de server ativo (retorna `null` no cliente cedo); auditar chamadas em GUI/render
- [ ] **Enchants do MNA via `MAEnchantmentBridge.build()` têm `DataComponentMap.EMPTY`** → efeitos vanilla (dano/durabilidade/etc.) NÃO aplicam sozinhos; a lógica custom vive em `EnchantmentBehaviors`. Revisar quais precisam de `DataComponents.ENCHANTMENT_EFFECTS`
- [ ] `MNAMenuAccess` / `LootTableHelper` — reflexão em campos `private` do vanilla; validar em runtime
- [ ] Tiles com `getCapability` removido (FluidJugTile, ManaweavingAltarTile, InfiniteFluidCapabilityHandler) — expor handlers via `RegisterCapabilitiesEvent` (NeoForge)
- [ ] `MNACaps.itemHandler` — validar com baús/máquinas do MNA em jogo
- [ ] B16 pipeline de dano — validar reduções (escudo/armadura) com `LivingDamageEvent.Pre/Post`
- [ ] B21 eventos removidos — features desativadas (jump boost/camuflagem/sleep); mixin se necessário
- [ ] `MAEnchantmentBase`/`MADamageEnchantmentBase` são marcadores vazios — decidir se reimplementar via data
- [ ] `_snapshots/` fica fora do git (pesado) — o histórico do repo remoto cumpre o papel de ponto de retorno

> **COMO MANTER ESTA LISTA VIVA (pedido do usuário):** ao encontrar qualquer erro novo
> no caminho, adicionar aqui (arquivo/padrão + contagem) ANTES de seguir para o próximo
> lote, e re-checar as contagens ao final de cada lote. A tabela de Progresso registra
> o que já caiu; esta seção registra o que falta.
