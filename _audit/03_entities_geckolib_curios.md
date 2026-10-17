# Auditoria 03 — Entidades, GeckoLib e Integração Curios

**Escopo:** fonte 1.20.1 (`decompiled/`, CFR) × port 1.21.1 (`mna-port-workspace/`)
**Versões:** GeckoLib 4.7 → 4.9.3 · Curios forge 5.14.1 → neoforge 9.5.1
**Método:** diff de contagens, diff de imports, diff de paths de asset, leitura de código. Nada foi editado.

---

## 1. Entidades

| Métrica | 1.20.1 (fonte) | 1.21.1 (port) | Status |
|---|---|---|---|
| Arquivos `.java` em `com/mna/entities` | 300 | 300 | OK |
| `EntityInit.java` — linhas | 369 | 371 | OK |
| `EntityInit` — chamadas `register(` | 96 | 96 | OK |
| `EntityInit` — `EntityType.Builder` | 85 | 85 | OK |
| `EntityClientInit` — renderers registrados | 84 | 84 | OK |
| EntityTypes com renderer registrado (lista) | 84 | 84 | OK — lista byte-idêntica |
| Subpacotes de `entities/` | ai, attributes, boss, constructs, faction, manaweaving, models, passive, projectile, renderers, rituals, sorcery, state, summon, utility | idênticos | OK |
| `models/` (GeoModel + EntityModel) | — | 42 arquivos | OK |
| Classes-base de entidade (`UtilityEntityBase`, `LivingUtilityEntity`, `WandClone`, `IAnimPacketSync`, `PossessionActionInit`) | presentes | presentes | OK |

**Conclusão:** `EntityInit` e o registro de renderers estão **completos e 1:1**. As 84 entidades com renderer batem exatamente (de `DAMAGE_NUMBERS` a `WITCH_HUNTER_ARROW`). Nenhuma entidade ficou sem renderer. O log `M&A -> Entity Renderers Registered` está preservado.

---

## 2. GeckoLib

### 2.1 Assets

| Métrica | 1.20.1 (jar) | 1.21.1 (port) | Status |
|---|---|---|---|
| `.geo.json` (models) | 52 | 52 | OK |
| `.animation.json` + `.anim.json` | 50 | 50 | OK |
| Total de assets geo/anim | 99 | 99 | OK — **zero** divergentes |
| Paths referenciados no código Java que não existem no port | — | **0 de 98** | OK |

Comparação de conteúdo arquivo-a-arquivo: `NO JAR mas FALTA no PORT: []` e `NO PORT mas não no JAR: []`. Os 98 paths de recurso (strings `"geo/..."` / `"animations/..."`) são idênticos entre as duas bases — inclusive os fora do padrão (`hulking_zombie.anim.json`, `lantern_wraith_anim.json`, `spellbreaker_anim.json`, `the_broker.anim.json`, `wandering_wizard.anim.json`, `witchhunter_anim.json`, `skeleton_assassin.anim.json`, `none.anim.json`).

### 2.2 Imports — migração de pacote 4.7 → 4.9.3

Renomeação de pacote do GeckoLib 4.9.3 (core → raiz/animation) aplicada **em todos os 134 arquivos** que tocam GeckoLib:

| Import 1.20.1 | 1.21.1 | Ocorrências | Status |
|---|---|---|---|
| `core.animation.RawAnimation` | `animation.RawAnimation` | 46 | OK |
| `core.object.PlayState` | `animation.PlayState` | 37 | OK |
| `core.animatable.GeoAnimatable` | `animatable.GeoAnimatable` | 37 | OK |
| `core.animation.AnimationState` | `animation.AnimationState` | 33 | OK |
| `core.animation.AnimationController` | `animation.AnimationController` | 33 | OK |
| `core.animation.AnimatableManager` | `animation.AnimatableManager` | 29 | OK |
| `core.object.Color` | `util.Color` | 2 | OK |
| `core.animation.EasingType` | `animation.EasingType` | 1 | OK |
| `core.keyframe.event.*` | `animation.keyframe.event.*` | 11 | OK |
| `core.animatable.model.CoreGeoBone` | **removido (sem equivalente)** | 11 | **ver 2.4** |

Imports inalterados e válidos em 4.9.3 (verificados por reflexão contra `libs/geckolib-neoforge-1.21.1-4.9.3.jar`, 263 classes): `GeoModel` (66), `GeoBone` (26), `BakedGeoModel` (11), `RenderUtil` (10), `GeoEntity` (10), `GeoBlockEntity` (10), `GeoBlockRenderer` (9), `GeoArmorRenderer` (8), `GeoItem` (8), `GeoItemRenderer` (6), `GeoEntityRenderer` (3), `GeoRenderer`, `GeoRenderLayer`, `GeckoLibUtil`, `GeckoLibNetwork`, `AnimatableInstanceCache`, `GeckoLib`.

### 2.3 Classes-base e topologia de animação

| Item | 1.20.1 | 1.21.1 | Status |
|---|---|---|---|
| Arquivos com `AnimationController` | 33 | 33 | OK |
| Instanciações de `AnimationController` | 58 | 58 | OK |
| Arquivos com `registerControllers` | 29 | 29 | OK |
| Arquivos com `RawAnimation.begin` | 46 | 46 | OK |
| `implements GeoEntity` | 6 | 6 | OK |
| `extends GeoEntity` | 4 | 4 | OK |
| Arquivos com `getAnimationResource` | 51 | 51 | OK |
| `MAGeckoRenderer<T extends LivingEntity>` | — | `MAGeckoRenderer<T extends LivingEntity & GeoAnimatable>` | OK (bound corrigido para 4.9.3) |
| Subclasses de renderer em `renderers/` | 14 entradas | 14 entradas (idênticas) | OK |

Verificação de assinatura por bytecode: `GeoModel` do 4.9.3 expõe `getAnimationResource(LGeoAnimatable;)Lnet/minecraft/resources/ResourceLocation;` — **`ResourceLocation`, não `ResourceKey<Animation>`**. Os 51 overrides do port com `public ResourceLocation getAnimationResource(...)` batem com a API real do 4.9.3. Os 311 usos de `ResourceKey` no port vêm da migração vanilla 1.21.1 (effects/registries), não de GeckoLib.

Padrão de animatables confirmado idêntico (ex. `Construct`): `implements GeoEntity, AnimationController.CustomKeyframeHandler<Construct>`, `AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this)`, `registerControllers(AnimatableManager.ControllerRegistrar)` + `registrar.add(new AnimationController[]{controller})`.

### 2.4 `CoreGeoBone` — migração manual, não quebrada

`CoreGeoBone` não existe no 4.9.3 (substituído por `GeoBone` em `cache.object`). Os 11 arquivos que o usavam foram todos refatorados para `software.bernie.geckolib.cache.object.GeoBone`, e `RenderUtil.translateMatrixToBone` continua sendo chamado com o `GeoBone`. Todos os 11 arquivos existem no port e estão atualizados:

`ConstructWorkbenchRenderer`, `LodestarRenderer`, `RunicAnvilRenderer`, `SpectralRenderer`, `WizardLabRenderer`, `BrokerModel`, `CouncilWardenRenderer`, `ConstructRenderer`, `WanderingWizardRenderer`, `EnderDiscRenderer`, `RunicMalusRenderer`.

Único uso de API: `GeoBone head = this.getAnimationProcessor().getBone("head")` em `BrokerModel` — método preservado no 4.9.3.

### 2.5 "Loaded 0 entity animations" no boot — **NÃO é bug**

Investigado a fundo. Não é problema de path, nem de assets, nem de registro:

1. Os 99 assets geo/anim estão 100% presentes e idênticos ao 1.20.1.
2. Os 98 paths referenciados no código Java **todos** existem no port.
3. `GeoModel.getAnimationResource` do 4.9.3 espera `assets/<modid>/animations/` — e é exatamente onde estão (`assets/mna/animations/...`). O próprio bytecode do 4.9.3 carrega a validação `Invalid animation resource path provided - GeckoLib animations must be placed in assets/<modid>/animations/`, satisfeita.
4. Os 58 `AnimationController` e 29 `registerControllers` estão presentes e idênticos.
5. GeckoLib 4.9.3 **não emite** a mensagem "Loaded N entity animations" — a string não existe no jar. Varredura de constantes do 4.9.3 só encontrou `Loaded {} for service {}` (boot de services do GeckoLibServices).

**Diagnóstico:** a mensagem não vem do GeckoLib. É ou (a) log de uma versão/ferramenta diferente, ou (b) uma mensagem do próprio MNA/não-GeckoLib. Com paths, assets e controllers 100% presentes, o carregamento de animações está estruturalmente correto. **Recomendação:** validar em jogo — spawnar um `Construct` (`construct.geo.json`, 98 KB de animação) e um `Imp`; se animarem, o log era ruído. Não há evidência estática de bug.

---

## 3. Curios — integração

### 3.1 API e interop

| Item | 1.20.1 | 1.21.1 | Status |
|---|---|---|---|
| Arquivos em `com/mna/interop` | BookRenderer, CurioRenderer, CuriosInterop, HatRenderer, StaffRenderer, WandRenderer, jei/, lootr/, lucent/ | mesmos **exceto `lucent/`** | PARCIAL |
| `CuriosInterop` — `CuriosRendererRegistry.register(...)` | 59 | 59 | OK — itens idênticos |
| Renderers: CurioRenderer / StaffRenderer / WandRenderer / HatRenderer / BookRenderer | 5 classes | 5 classes, imports idênticos | OK |
| Registro via `FMLClientSetupEvent` | sim | sim (`net.neoforged.fml.event.lifecycle.FMLClientSetupEvent`) | OK |
| Anotação do evento | `@Mod.EventBusSubscriber` (forge) | `@EventBusSubscriber(modid="mna", bus=Bus.MOD)` | OK |
| Arquivos que usam `top.theillusivec4.curios.*` | 45 | 45 — **lista byte-idêntica** | OK |
| Arquivos que chamam `CuriosInterop.*` | 18 | 18 | OK |
| `implements ICurioRenderer` nos 5 renderers | sim | sim | OK |

Migração de API Forge→NeoForge: `net.minecraftforge.bus` → `net.neoforged.bus`, `net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.Mod`, `net.minecraftforge.fluids.FluidStack` → `net.neoforged.neoforge.fluids.FluidStack`, `net.minecraftforge.client.extensions` → `net.neoforged.neoforge.client.extensions`. Todos corretos.

**Validação de API contra o jar real** (`libs/curios-neoforge-9.5.1+1.21.1.jar`, 152 classes): todos os 8 tipos importados pelo MNA existem — `CuriosApi`, `SlotContext`, `SlotResult`, `SlotTypePreset`, `CuriosRendererRegistry`, `ICurioRenderer`, `ICurio`, `ICuriosItemHandler`. **A API do Curios 9.5.1 é compatível com a usada pelo MNA.**

### 3.2 Migração `ICurio` → `ICurioItem` / capabilities

| 1.20.1 | 1.21.1 | Status |
|---|---|---|
| `new ICurio()` em `ChargeableItem`, `ArcaneCrown` | `new ICurioItem()` em `ChargeableItem` | **PARCIAL** — ver regressão R2 |
| `CurioItemCapability.createProvider(ICurio)` + `LazyOptional` + `CuriosCapability.ITEM` | `createProvider(ICurioItem)` tipado `ICapabilityProvider<ItemStack, Void, ICurioItem>` | OK |
| `curioTick(String identifier, int index, LivingEntity)` | `curioTick(SlotContext slotContext, ItemStack slotStack)` | OK (nova assinatura do Curios 9) |
| `LazyOptional` (221 usos) | `Optional`/`var` + `curioInventory.get()` (3) | OK — `LazyOptional` foi removido do NeoForge |
| `ICuriosItemHandler` — `resolve().get()` | `.get()` | OK |

`CuriosApi.getCuriosInventory(entity)` retorna `Optional<ICuriosItemHandler>` em ambas; `findFirstCurio`, `findCurio(identifier, index)`, `broadcastCurioBreakEvent` preservados.

### 3.3 Data — tags de slot e configuração

| Asset | 1.20.1 | 1.21.1 | Status |
|---|---|---|---|
| `data/curios/tags/item/{back,belt,bracelet,charm,head,necklace,ring,spell}.json` | 8 tags | 8 tags | OK |
| Path da tag | `tags/items/` (plural, Curios antigo) | `tags/item/` (singular, Curios 9) | OK — path correto p/ 9.5.1 |
| Conteúdo das tags (item a item) | 6/12/4/20/7/7/12/11 valores | **idênticos, 0 divergentes** | OK |
| `data/mna/curios/entities/entities.json` (player + 11 slots) | sim | idêntico | OK |
| `data/mna/curios/slots/ring.json` (`size:2`, `add_cosmetic:true`) | sim | idêntico | OK |
| `data/mna/curios/slots/spell.json` (`size:1`, `render_toggle:false`, `icon:mna:slot/spell`) | sim | idêntico | OK |
| `assets/mna/models/item/obj/curios/*.obj/.mtl` | 44 arquivos | presentes | OK |

Distribuição por slot: back(6), belt(12), bracelet(4), charm(20), head(7), necklace(7), ring(12), spell(11) — **79 itens curios, idênticos**.

---

## 4. O que está QUEBRADO / FALTANDO

### R1 — `IsItemInCurioSlot` perdeu a checagem de slot (regressão funcional)

`com/mna/interop/CuriosInterop.java`:

```java
// 1.20.1
slotResult.slotContext().getIdentifier().equalsIgnoreCase(slot.getIdentifier())
// 1.21.1
exists.setValue(true)     // <-- setor de comparação removido
```

O parâmetro `SlotTypePreset slot` continua na assinatura mas **é ignorado**. Consequência: a função retorna `true` para *qualquer* slot onde o item esteja equipado.

Impacto concreto — 11 callers passam um slot específico que deixa de ser respeitado:

| Caller | Slot esperado |
|---|---|
| `ClientEventHandler` (EMBERGLOW_BRACELET) | BRACELET |
| `CommonEventHandler` (AIR_CAST_RING) | RING |
| `DamageEventHandler` ×3 (BATTLEMAGE/FALL/DROWN/BURN) | NECKLACE / CHARM |
| `BoundAxe`, `BoundBow`, `BoundSword` (BATTLEMAGE_AMULET) | NECKLACE |
| `BoundShield` (WARDING_AMULET) | NECKLACE |
| `ComponentCauterize` (EMBERGLOW_BRACELET) | BRACELET |

Na prática o efeito é atenuado (os itens citados só podem ir nesses slots pelas tags), mas é uma regressão semântica real: ex. um `BATTLEMAGE_AMULET` no slot errado ainda ativaria o bônus. **Correção:** restaurar `slotResult.slotContext().getIdentifier().equalsIgnoreCase(slot.getIdentifier())` (adaptando ao getter do Curios 9).

### R2 — `ArcaneCrown` perdeu a capability de curio (funcionalidade ausente)

`com/mna/items/artifice/ArcaneCrown.java`:

- 1.20.1: tinha `initCapabilities(ItemStack, CompoundTag)` retornando `CurioItemCapability.createProvider(new ICurio(){ curioTick(...) -> tickEffect(player, world); getStack() })`.
- 1.21.1: **o método `initCapabilities` foi removido por completo.** Restaram apenas os imports órfãos `CurioItemCapability`, `ICurio`, `CompoundTag`, `LivingEntity`, `net.neoforged.neoforge.capabilities.ICapabilityProvider` (não usados).

Efeito: a coroa arcana está na tag `head` e no inventário de curios do player, mas **não executa mais `curioTick`** — o efeito contínuo (MANA_BOOST + remoção de efeitos nocivos com cooldown DISPEL_EXHAUSTION) só roda via `onArmorTick` (slot de armadura vanilla), não quando equipada como curio. É exatamente o tipo de item que a integração curios existe para servir.

### R3 — `lucent` interop ausente (integração opcional, não migrada)

1.20.1 tem `com/mna/interop/lucent/MnALucentPlugin.java` — plugin `@LucentPlugin implements ILucentPlugin` que registra níveis de luz para 18 entity types (SPELL_PROJECTILE, SPELL_BEAM, SPELL_CONE, SPELL_EMANATION, SPELL_EMBER, SPELL_FISSURE, SPELL_FOCUS, SPELL_WALL, SPELL_WAVE, SMITE_PROJECTILE, SENTRY_PROJECTILE = 13; PORTAL_ENTITY, RIFT, DEMON_RETURN_PORTAL = 8; BIFROST, FEY_LIGHT = 15; FAERIE_QUEEN, LANTERN_WRAITH = 10) e itens (`ItemLightingRegistry`).

No port o diretório foi **removido de `src/main/java`** e está em `disabled_src/lucent-interop-backup/MnALucentPlugin.java` (backup intacto, junto de `disabled_src/jei-interop-backup/`).

**Avaliação:** não é regressão do GeckoLib nem do Curios — é integração opcional com mod de terceiro (`com.legacy.lucent`). Sem o Lucent no pack 1.21.1, esses entity types simplesmente não emitem luz. Se o Lucent não for portado, nada a fazer. Se for, o plugin precisa ser reescrito contra a API nova. **Marcar como decisão, não como bug.**

---

## Tabela final — O que precisa estar / como está no 1.21.1 / STATUS

| O que precisa estar | Como está no 1.21.1 | STATUS |
|---|---|---|
| `EntityInit` registra todas as entidades | 96 `register(`, 85 `EntityType.Builder` — idêntico à fonte | OK |
| Renderers registrados p/ todas as entidades | 84/84, lista byte-idêntica (`EntityClientInit`) | OK |
| 52 `.geo.json` (models GeckoLib) | 52/52, conteúdo idêntico | OK |
| 50 `.animation.json`/`.anim.json` | 50/50, conteúdo idêntico | OK |
| Paths de asset referenciados no código existem | 98/98 presentes, 0 faltando | OK |
| Imports GeckoLib no pacote 4.9.3 (`software.bernie.geckolib`) | migrados em todos os 134 arquivos; validados contra o jar 4.9.3 | OK |
| `GeoModel.getAnimationResource` com assinatura correta | 51 overrides `ResourceLocation` — bate com bytecode do 4.9.3 | OK |
| `GeoEntity`/`GeoBlockEntity`/`GeoItem` como animatables | 6+4 arquivos, idêntico | OK |
| `AnimationController` + `registerControllers` | 58 controllers / 29 arquivos, idêntico | OK |
| `MAGeckoRenderer` base funcional | presente, bound corrigido p/ `LivingEntity & GeoAnimatable` | OK |
| `CoreGeoBone` → `GeoBone` | 11/11 arquivos refatorados | OK |
| "Loaded 0 entity animations" investigado | assets+paths+controllers 100% OK; string não existe no 4.9.3 → log não é do GeckoLib, validar em jogo | OK (sem evidência de bug) |
| `CuriosInterop` registra 59 renderers curios | 59 registros, itens idênticos, via `FMLClientSetupEvent` | OK |
| 5 classes `ICurioRenderer` (Curio/Staff/Wand/Hat/Book) | presentes, imports idênticos | OK |
| Imports Curios batem com API 9.5.1 | 8/8 tipos existem no jar 9.5.1 | OK |
| `ICurioItem` handlers + `CuriosRendererRegistry` | migrados `ICurio`→`ICurioItem`, `LazyOptional`→`Optional` | PARCIAL (regressão R2 em ArcaneCrown) |
| 8 tags `data/curios/tags/item/*.json` | 8/8, conteúdo item-a-item idêntico (79 itens) | OK |
| `data/mna/curios/entities/entities.json` + slots | idênticos | OK |
| `IsItemInCurioSlot` respeita o slot pedido | comparação de identificador removida, parâmetro ignorado | **FALTA (R1)** |
| `ArcaneCrown.initCapabilities` + `curioTick` | método removido; imports órfãos; efeito não roda como curio | **FALTA (R2)** |
| `interop/lucent` (plugin de luz) | fora de `src/`, em `disabled_src/` | **FALTA (R3)** — integração opcional, decisão |

**Resumo:** GeckoLib e Curios estão migrados de forma **estruturalmente completa e fiel**. Os 3 apontamentos são pontuais e localizados — 1 bug lógico (R1), 1 funcionalidade perdida (R2), 1 integração opcional desabilitada (R3). Nenhum deles impede compilar ou rodar.
