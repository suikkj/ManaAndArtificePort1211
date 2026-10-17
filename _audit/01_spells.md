# Auditoria Setorial 01 — Subsistema de Magia / Spells (Mana and Artifice 3.1.11)

Port **1.20.1 (Forge) → 1.21.1 (NeoForge)**. Fonte de referência:
`C:/Users/lucas/Documents/coisas/Entre Mods e Códigos/Mana And Artifice 1.21.1/decompiled/`
Jar original (só bytecode/datapack; datapack de receitas quase vazio nele): `.../mods/mna-forge-1.20.1-3.1.11-all.jar`
Workspace do port: `.../Mana And Artifice 1.21.1/mna-port-workspace/`

> Nota sobre o jar 1.20.1: `unzip -l` mostra apenas **361 arquivos em `data/mna/`** e **só 10 em `data/mna/recipes/`** (wickerwood_constructs, wizard_chalk, zombie_head_to_rotten_flesh). As 110/21/9/etc. receitas de magia **não vêm do jar** — são geradas pelo build (datagen) no mod original. Por isso a comparação de receitas é feita contra o **datapack já convertido do port** (`src/main/resources/data/mna/recipe/`, singular), que é a fonte de verdade no 1.21.1.

---

## 1. Parts de spell: shapes / components / modifiers

Três "partes" compõem um spell: **1 shape** (forma), até **5 components** (efeitos) e até **3 modifiers** (ajustadores de atributo). Registradas em 3 registries custom do mod.

### 1.20.1 — `com.mna.spells.SpellsInit` (`@Mod.EventBusSubscriber`, `RegisterEvent`)
| Registry (key `mna:…`) | Classe base | Qtd registrada | Onde |
|---|---|---:|---|
| `Registries.Shape` (`mna:shapes`) | `api/spells/parts/Shape.java` | **21** | `SpellsInit.register()` → `event.register(Shape key, …)` |
| `Registries.Modifier` (`mna:modifiers`) | `api/spells/parts/Modifier.java` | **9** | idem |
| `Registries.SpellEffect` (`mna:components`) | `api/spells/parts/SpellEffect.java` | **122** | idem |

- **21 shapes**: touch, bolt, self, projectile, wall, wave, emanation, beam, rune(→`ShapeSigil`), smite, bound_sword, bound_axe, bound_bow, bound_shield, summon, wrath, chain, fissure, focus, embers, cone.
- **122 components** (`components/*`): 118 classes `Component*.java` em `spells/components/` + 2 em `spells/components/boss/` (Chill, Heatwave) + base `PotionEffectComponent` (não registrada). Alguns ids reusam classes com flags: `animus`/`greater_animus` (`ComponentAnimus(..., false/true)`), `posession`/`greater_possession` (`ComponentPossession(..., false/true)`); ids com nome diferente da classe: `earthsky`→`ComponentWizardsIsland`, `fortification`→`ComponentFossilize`, `fill_hole`→`ComponentPack`, `insect_swarm`→`ComponentSummonInsectSwarm`, `glide`→`ComponentFlight`, `lmnop`→`ComponentWtfboom`.
- **9 modifiers**: range, damage, duration, speed, radius (RADIUS+WIDTH+HEIGHT+DEPTH), magnitude, lesser_magnitude, delay, precision. Cada um carrega custo de complexidade (250/500/300) e o(s) `Attribute` que altera.
- Registro usa `RLoc.create("shapes/…")` = `ResourceLocation.fromNamespaceAndPath("mna", …)`. Textura de ícone via `RLoc.create("textures/spell/{shape,component,modifier}/x.png")`.

---

## 2. Montagem / "crafting" de um spell part

**Não é crafting vanilla.** Cada spell part é desbloqueado por uma **receita custom do tipo item+padrão** (`ItemAndPatternRecipe`), mostrada no **Occulus** (`gui/block/GuiOcculus.java`). O jogador reúne os `items` e "tece" os `patterns` (manaweave) para dominar a parte.

- Serializers: `recipes/spells/{ShapeRecipeSerializer, ComponentRecipeSerializer, ModifierRecipeSerializer}` → `instantiate(recipeId)` → `{Shape,Component,Modifier}Recipe` (estende `ItemAndPatternRecipe` → `AMRecipeBase`).
- `ItemAndPatternRecipeSerializer.readExtra(id, JsonObject)` chama `recipe.parseJSON(json)`; `ItemAndPatternRecipe.parseExtraJson` lê `output` (string `RL` ou `{item,…}`), `quantity`, `items[]`, `patterns[]`.
- `getComponent()` (ex. `ShapeRecipe`): se `output` tem namespace `mna` e path sem prefixo, remonta `mna:shapes/<path>` e busca em `Registries.Shape.getValue(...)`. Lança `InvalidParameterException` se não achar.
- Tipos de receita: `mna:shape` (`RecipeInit.SHAPE_TYPE`/`SHAPE_RECIPE_SERIALIZER`), `mna:component`, `mna:modifier`.
- **Como o player cria um spell**: itens `ItemSpell` / `MagicStaff` / `Grimoire` guardam um `SpellRecipe` serializado em NBT (`CompoundTag "spell"`). O jogador abre o radial de seleção (`gui/radial/`), escolhe shape → components → modifiers; `SpellRecipe` monta `ModifiedSpellPart<Shape/SpellEffect>` + `Modifier[3]` (MAX_COMPONENTS=5, MAX_MODIFIERS=3). `ModifiedSpellPart.fromNBT(nbt, MnaRegistry<T>)` desserializa cada parte pelo registry.

---

## 3. Casting: mana, CastingResource, fluxo

- **Recurso de conjuração**: `IPlayerMagic` (capability/attachment do player) guarda `ICastingResource castingResource = new Mana()`. Recursos registrados em `CastingResourceRegistry.registerDefaults()`: **mana, souls, council_mana, brimstone, summer_fire, winter_ice** (6).
- **`PlayerMagic.tick(player)`**: regen — `regen_pct_per_tick = 1/regenRate`; come `restore(max * pct)` (×3 se saciado/fome OK). `setMaxAmountByLevel(level)` no level-up. Sync a cada 10 ticks (`needsSync`).
- **Fluxo de lançamento** (`SpellCaster`):
  1. `ItemSpell.castSpellOnUse` → `SpellCaster.PlayerCast(stack, caster, hand, pos, look, world, consumeMana, isCurioCast)`.
  2. Checagens: magia destravada; `recipe.getTier(world)` ≤ tier do player; `getComplexity()` ≤ `tierMaxComplexity`; ingredientes/reagents presentes.
  3. **Custo**: `manaCost = recipe.getManaCost()`; se `!hasEnough(caster, manaCost)` e `consumeMana` → falha. Senão `castingResource.consume(caster, manaCost)` (ou `×0.25` em falha parcial).
  4. `ApplyComponents` aplica cada `SpellEffect.ApplyEffect(...)` sobre os `SpellTarget`s gerados pelo `Shape.Target(...)`; `mergeComponentResults`; `spawnClientFX`; `AddAffinityAndMagicXP`.
  5. Ajustadores (`SpellCaster.registerAdjuster`) aplicados em `FMLLoadCompleteEvent` (17 deles: afinidade, channeled, sigils, hellfire/arcane/rings/amulet/bracelet/circle, amplify_magic, animus, efficiency/step enchants, summer_fire, winter_ice).
- **Cantrips** (`cantrips/CantripRegistry.java`): **14 cantrips** hardcoded (ignite, transmute, halp, gust, ascend, dispel, drought, ward, reveal_ward, grimoire, faction_grimoire, freeform_spell_a/b/c). Disparados por gestos de **manaweaving** (`entities/manaweaving/Manaweave.java` → `CantripCastEvent`), lidos no `GuiCantrips`/`InventoryCantrips`. `ICantrip` guardado em `PlayerCantrips` (attachment). Requerem advancement de tier.

---

## 4. Receitas de magia no datapack (port 1.21.1) × serializer × parse

Datapack do port: `src/main/resources/data/mna/recipe/` (**SINGULAR**, 1796 json). Contagem por subconjunto, TYPE e situação:

| Subconjunto | Qtd | TYPE (serializer `mna:…`) | Serializer registrado em `RecipeInit`? | `readExtra` presente? | STATUS |
|---|---:|---|---|---|---|
| `components/` | 110 | `mna:component` | SIM (`COMPONENT_RECIPE_SERIALIZER`, `serializer("component", …)`) | SIM (×2: json+buf) | **OK** |
| `shapes/` | 21 | `mna:shape` | SIM (`SHAPE_RECIPE_SERIALIZER`) | SIM | **OK** |
| `modifiers/` | 9 | `mna:modifier` | SIM (`MODIFIER_RECIPE_SERIALIZER`) | SIM | **OK** |
| `staves/` | 21 | `minecraft:crafting_shaped` | vanilla | n/a | **OK** (result `{"id":…}`) |
| `wands/` | 20 | `minecraft:crafting_shaped` | vanilla | n/a | **OK** |
| `runeforging/` | 24 `mna:runeforging` + 2 shaping | `mna:runeforging` | SIM (`RUNEFORGING_SERIALIZER`) | SIM | **OK** |
| `runescribing/` | 24 `mna:runescribing` + 2 shaping | `mna:runescribing` | SIM (`RUNESCRIBING_SERIALIZER`) | SIM | **OK** |
| `manaweaving/` | 530 | `mna:manaweaving-recipe` | SIM (`MANAWEAVING_RECIPE_SERIALIZER`) | SIM | **OK** |
| `manaweave_patterns/` | 17 | `mna:manaweaving-pattern` | SIM (`MANAWEAVING_SERIALIZER`) | SIM | **OK** |
| `manaweave_cache_effects/` | 29 | `mna:manaweave-cache-effect` | SIM (`MANAWEAVE_CACHE_EFFECT_SERIALIZER`) | SIM | **OK** |
| `fume_filter/` | 18 | `mna:eldrin-fume` | SIM (`ELDRIN_FUME_RECIPE_SERIALIZER`) | SIM | **OK** |
| `stone_runes/` | 17 | `minecraft:crafting_shapeless` | vanilla | n/a | **OK** |
| `eldrin_altar/` | 122 | `mna:eldrin-altar` | SIM (`ELDRIN_ALTAR_RECIPE_SERIALIZER`) | SIM | **OK** |

Todos os **13 serializers custom** do mod estão registrados via `DeferredRegister` (`SERIALIZERS`/`TYPES`) e cada um tem `readExtra` (json + buffer). `ItemAndPatternRecipeSerializer` é a base de shape/component/modifier (e de manaweaving). Os tipos `minecraft:crafting_shaped/shapeless/smelting/blasting/stonecutting` já usam `result: {"id":…}` — **0 ocorrências** de `"result": "string"` ou `"result": {"item":…}` restantes (varredura confirma).

---

## 5. Estado no PORT 1.21.1

### 5.1 Registries (SHAPES / SPELL_EFFECT / MODIFIER)
- `Registries.java`: `DeferredRegister.create(SHAPES_KEY,"mna")` + `makeRegistry(b->b.sync(true))` embrulhado em `MnaRegistry<T>` (preserva `getValue/getValues/getKey/containsKey/containsValue/getRegistryKey`). Todos os 6 DeferredRegisters anexados ao mod bus em `ManaAndArtifice.java` (`Registries.register(modEventBus)`).
- **`SpellsInit` do port**: 21 `shapes/`, 9 `modifiers/`, 122 `components/` registrados — **idêntico ao 1.20.1** (paridade total). `onLoadComplete(FMLLoadCompleteEvent)` com os **17 `registerAdjuster`** intactos.
- `ManaAndArtifice.java` registra também `RecipeInit.SERIALIZERS/TYPES` e `CapabilitiesInit.ATTACHMENT_TYPES` no bus.

### 5.2 Shims Shapes / Components / Modifiers (substituem `@ObjectHolder` removido)
- Em 1.20.1 eram `@ObjectHolder` (campos `= null` → NPE). No port viraram lookups em tempo de init: `Registries.{Shape,SpellEffect,Modifier}.getValue(RLoc.create("…"))`.
- Paridade de campos: Shapes 20/20, Components 121/121, Modifiers 8/8 (mesma contagem do 1.20.1).
- **Lacunas de shim (não são bug, são por design)**: shapes sem campo estático = `cone`; components = `glide`, `impulse`, `nethergate`; modifier = `lesser_magnitude`. **O 1.20.1 também não tinha** esses campos estáticos (confirmado: grep em `decompiled/api/spells/collections/` = nenhum). São acessados por **key em runtime** (Occulus itera o registry: `Registries.Shape/SpellEffect/Modifier.containsKey/getValue`), então aparecem no jogo normalmente. **STATUS: OK** (equivalente ao original).

### 5.3 Casting / mana
- `PlayerMagic`, `Mana`/`Souls`/`CouncilMana`/`Brimstone`/`SummerFire`/`WinterIce`, `CastingResourceRegistry` (6 recursos) — **todos portados**, `tick()` de regen intacto.
- Capability `IPlayerMagic` → **Attachment** (`CapabilitiesInit.PLAYER_MAGIC`, `serialize(PlayerMagicSerializer).copyOnDeath()`), acessada via shim `MNACap.get(player, …).ifPresent(...)` (substitui `LazyOptional`, removido no 1.21.1).
- `SpellCaster.PlayerCast` / `Affect` / `ApplyComponents` portados; `consume(caster, manaCost)` no caminho de cast presente. `MagicStaff extends ItemSpell`; `SpellRecipe.fromNBT` usa `MnaRegistry` (`ModifiedSpellPart.fromNBT(nbt, MnaRegistry<T>)`).
- `CantripRegistry`: **14 cantrips** (igual ao 1.20.1). Padrões de manaweave referenciados (10) todos existem em `manaweave_patterns/` (17) — nenhum faltando.

### 5.4 Recipe codec bridge (1.20.1 `fromJson` → 1.21.1 `codec()`)
- `AMRecipeBaseSerializer.codec()` reconstrói um `JsonObject` do `MapLike` e reusa `readExtra(PENDING_ID, json)`. O ID real é restaurado pós-load por `RecipeIdFixer` (`ServerStartedEvent` + `RecipesUpdatedEvent` no cliente), varrendo `RecipeManager.getRecipes()` e setando `base.setId(holder.id())` nos `AMRecipeBase`. Build log: **BUILD SUCCESSFUL**, launch prévio **3086 receitas, 0 parse errors, 1078 IDs restaurados**.

### 5.5 Pontos de atenção / risco (não quebram hoje, mas frágeis)
1. **Inicialização estática dos shims**: `Shapes/Components/Modifiers` resolvem via `getValue` em **class-load**. Se alguma dessas classes for carregada **durante** o `RegisterEvent` (ex.: `ComponentAnimus.adjustSpell` referencia `Components.ANIMUS`), o registry ainda pode estar vazio → campo `null`. Hoje os call-sites rodam em `FMLLoadCompleteEvent`/runtime (após registro), então está OK — mas é um ordering frágil se novo código referenciar os shims mais cedo.
2. **`streamCodec()` passa `null` como recipeId** (`fromNetwork(null, buf)`) → ID nulo no cliente até `RecipeIdFixer.onRecipesUpdated`. Se alguma tela abrir antes do `RecipesUpdatedEvent` receber ID ainda nulo, pode haver divergência cosmética (não crash).
3. `MultiblockRecipeSerializer` (bem como os demais) depende do `RecipeIdFixer`; multiblocos não são do escopo spells mas compartilham a mesma bridge.

---

## Tabela final — O que precisa estar × como está × STATUS

| O que precisa estar (1.20.1) | Como está no 1.21.1 (port) | STATUS |
|---|---|---|
| Registry `mna:shapes` com 21 shapes | `Registries.Shape` (DeferredRegister+makeRegistry, sync) + `SpellsInit` registra 21 | **OK** |
| Registry `mna:modifiers` com 9 modifiers | `Registries.Modifier` + 9 registrados | **OK** |
| Registry `mna:components` com 122 spell effects | `Registries.SpellEffect` + 122 registrados | **OK** |
| Shims `Shapes/Components/Modifiers` (ex-`@ObjectHolder`) resolvendo | lookups `getValue(RLoc.create)` — 20/121/8 campos, paridade com 1.20.1 | **OK** |
| `RecipeInit`: 13 serializers + 13 tipos custom registrados | todos presentes via DeferredRegister no mod bus | **OK** |
| `readExtra` (json+buf) em cada serializer custom | presentes (×2) nos 13 | **OK** |
| Receitas `mna:shape` (21) / `mna:component` (110) / `mna:modifier` (9) | mesmas contagens, type correto, parse via bridge codec | **OK** |
| Receitas `mna:runeforging` (24+2) / `runescribing` (24+2) / `manaweaving` (530) / `manaweaving-pattern` (17) / `manaweave-cache-effect` (29) / `eldrin-fume` (18) / `eldrin-altar` (122) | todas com serializer + readExtra registrados | **OK** |
| Receitas vanilla (`staves` 21, `wands` 20, `stone_runes` 17) | `result: {"id":…}`; 0 ocorrências de formato antigo | **OK** |
| Casting: `SpellCaster.PlayerCast/Affect/ApplyComponents` + consumo de mana | portado; `castingResource.consume` no caminho | **OK** |
| `IPlayerMagic` capability + 6 `ICastingResource` (Mana…) | → Attachment `PLAYER_MAGIC` (+progressão/rote/world/chunk); 6 recursos registrados | **OK** |
| `PlayerMagic.tick` regen de mana + `setMaxAmountByLevel` | intacto | **OK** |
| 14 cantrips + gestos de manaweaving + dados de cantrips do player | 14 cantrips; padrões existem; `PlayerCantrips` é campo interno de `PlayerMagic` (`getCantripData()`), persistido via `PlayerMagicSerializer` (attachment `PLAYER_MAGIC`) | **OK** |
| 17 `registerAdjuster` em `FMLLoadCompleteEvent` | 17 presentes | **OK** |
| `ModifiedSpellPart.fromNBT` / `SpellRecipe.fromNBT` por registry | usam `MnaRegistry<T>` (containsKey/getValue) | **OK** |
| Restauração do ID de receita (codec não recebe RL) | `RecipeIdFixer` (server+client) | **OK** |

\* **Nota (cantrips — agora OK)**: `PlayerCantrips` **não** é uma capability separada; é um campo interno de `PlayerMagic` (`getCantripData()` retorna `this.cantripData`), copiado em `copyFrom` e persistido dentro de `PlayerMagicSerializer`. Por isso não precisa de `AttachmentType` próprio — fecha com o `PLAYER_MAGIC`. Verificado: sem pendência.

---

## Resumo executivo
- **Spells (shapes 21 / components 122 / modifiers 9), cantrips (14), serializers de receita (13 custom) e o fluxo de casting/mana estão PORTADOS e registrados com paridade total ao 1.20.1.** Build SUCCESSFUL; launch prévio 3086 receitas / 0 erros de parse.
- Bridge de recipe codec (`AMRecipeBaseSerializer.codec` + `RecipeIdFixer`) cobre todos os tipos custom; receitas vanilla já em `{"id":…}`.
- **Nenhum item QUEBRADO ou FALTANDO confirmado no subsistema de magia.** Shims sem campo estático para `cone/glide/impulse/nethergate/lesser_magnitude` são equivalentes ao original (acesso por key em runtime), não são falha. Único risco residual é de *ordering* (shims resolvem via `getValue` em class-load; frágil só se novo código referenciá-los dentro do `RegisterEvent`).
