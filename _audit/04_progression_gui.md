# Auditoria 04 — Progressão / GUI / Guidebook / Effects / Advancements / Loot

Port 1.21.1 (NeoForge) vs fonte 1.20.1 (Forge). Fonte: `decompiled/com/mna/...` + jar `mna-forge-1.20.1-3.1.11-all.jar`. Port: `mna-port-workspace/src/main/...`. Somente análise, nada editado.

## Resumo executivo

O port está estruturalmente completo em CÓDIGO (Java): 29 triggers, 70 effects, 42 telas, 38 containers, 15 recipe renderers, 10 loot functions + 2 GLM + 1 loot condition — tudo portado e compilando. O jogo roda e advancements disparam.

Porém o datapack tem **DOIS BUGS CRÍTICOS DE NOME DE PASTA** que anulam dois sistemas inteiros em runtime:

1. **`data/mna/advancement/` (singular)** deveria ser **`data/mna/advancements/` (plural)** — o padrão vanilla é plural. 88 advancements estão em pasta que o vanilla NÃO lê → nenhum advancement do MNA é registrado de fato.
2. **`data/mna/loot_table/` (singular)** deveria ser **`data/mna/loot_tables/` (plural)** — 393 loot tables estão em pasta não lida → todo loot custom (constructs, artifacts, entities) não carrega.

Mais: **3 `this.renderBackground(...)`** sobreviveram ao fix de recursão (2 são recursão real em `Screen`/container). E o loot modifier usa o path Forge antigo (`data/mna/loot_modifiers/`) sem o índice NeoForge apontando para os JSONs `mna:`.

---

## 1. Advancements / Triggers

### Fonte 1.20.1
- `CustomAdvancementTriggers.java`: 29 triggers, registrados no `FMLCommonSetupEvent` via `CriteriaTriggers.register()` (namespace implícito do mod).
- `triggers/`: 29 classes estendendo `SimpleCriterionTrigger<Instance>`; `createInstance(JsonObject, ContextAwarePredicate, DeserializationContext)` + `m_7295_()` (getId). Ex: `TierTrigger` usa `RLoc.create("tier")`, criteria `tier` (int, `test = tier >= this.tier`).
- `predicates/`: 3 (CraftSpellPredicate, RequiredConstructPartPredicate, SummonConstructPredicate).
- Datapack: `data/mna/advancements/{tier_0..tier_4, boss, faction, misc}`.

### Port 1.21.1
- `CustomAdvancementTriggers.java`: mesmos 29 campos estáticos; registro migrado de `FMLCommonSetupEvent` para **`RegisterEvent` em `BuiltInRegistries.TRIGGER_TYPES`** via `helper.register(RLoc.create("..."), INSTANCE)` com namespace explícito `mna:*`. ✅ Correto (NeoForge congela registries antes do common setup).
- `TierTrigger` reescrito com `Codec`/`record Instance implements SimpleCriterionTrigger.SimpleInstance`, `codec()` via `RecordCodecBuilder`, campo `tier` obrigatório. ✅ Parity de critério mantida.
- Predicates: 3 arquivos presentes.
- Datapack: 88 JSONs, **MAS em `data/mna/advancement/` (SINGULAR)**.

### ⚠️ CRÍTICO — pasta singular
Vanilla carrega advancements de `data/<namespace>/advancements/` (plural). A pasta do port é `advancement/` (singular). Logo os 88 JSONs **não são carregados** pelo `ServerAdvancementManager`.

Contagens por tier: tier_0=5, tier_1=21, tier_2=20, tier_3=13, tier_4=12, boss=7, faction=4, misc=4, loot=1 (root.json solto).

**Reconciliação com "advancements disparam em jogo":** os TRIGGERS (codecs) estão registrados e o `TierTrigger.trigger()` roda via `ProgressionEventHandler`. O que "dispara" é a maquina de trigger de tier. Mas os ADVANCEMENTS (o que aparece na talla de advancements, com toast/title/description e os `required_advancement` do codex) dependem dos JSONs serem lidos — com pasta singular eles não são. Precisa rename `advancement` → `advancements`.

STATUS: Código OK / **Datapack FALTA (rename de pasta)**.

---

## 2. Guidebook (Codex)

### Fonte 1.20.1
- `GuiGuideBook extends GuiJEIDisable<ContainerGuideBook>` (256×178). Abre via `MenuType ContainerGuideBook` (provider `NamedGuideBook`, aberto por `BookStandBlock` e por lectern no `CommonEventHandler`).
- Fluxo: `init()` → `GuideBookEntries.INSTANCE.reload()` + `updateUnlockedEntries(null)` → `setupIndexView()` → `setupCategorySelectionButtons()` + `setupCategoryItemButtons()` + `setupCategoryView()`. Navegação por `CodexBreadcrumb` (INDEX/ENTRY/RECIPE/SEARCH/CATEGORY). `displayRecipe()` constrói renderer via `RelatedRecipe.constructRenderer()`.
- Receitas: `RecipeRendererBase extends AbstractWidget`; renderiza background `SPELL_RECIPE`/`backgroundTexture()`, cycling a cada 40 ticks (`ICyclingRecipeRenderer`). `Recipe3x3` busca `level.getRecipeManager().byKey(rLoc)` → `RecipeHolder.value()` cast `CraftingRecipe` (Shaped/Shapeless).
- Categorias: `EntryCategory` (12: BASICS, MANAWEAVING, RITUALS, SORCERY, RUNESMITHING, ARTIFICE, CONSTRUCTS, ELDRIN_ALTAR, ENCHANTMENTS + custom via `Register()`).
- Gating por tier: `setupEntryRelatedButtons()` filtra `rr.getTier() > playerTier`; `setupCategoryItemButtons()` filtra `e.getTier() > progression.getTier()`. Tier vem de `RelatedRecipe.getTier()` = `renderer.getTier()`.
- Dados: `assets/mna/guide/en_us.json` (516 entries no port).

### Port 1.21.1
- `GuiGuideBook`: parity total. `init()`, `render()`, `renderBg()` (com fix de recursão aplicado — ver §3), `renderLabels()`, breadcrumbs (`breadcrumbType`), busca, tabs, categorias — tudo migrado (`addRenderableWidget`, `removeWidget`, `getData(PROGRESSION.get())`, `MNAImageButton`).
- `GuideBookEntries`: `byKey(advancementID)` agora devolve `AdvancementHolder`, progresso via `MNAAdvancementAccess.getProgress` (acesso ao `progress` map privado). `reload()` lê `assets/mna/guide/<lang>.json` + `pack_adds`. ✅ 516 entries.
- `GuidebookEntry`: parse sections (text/image/title/item/recipe), related_recipes, tier, `required_advancement`, index. `getTier()` cai para min tier dos related recipes. ✅ Parity.
- `RelatedRecipe`: `getOutputItems()` usa `recipes.byKey(rLoc)` → `RecipeHolder.value()`, trata `ISpellComponentRecipe` (dummy vellum + ITEM_NAME). ✅
- `RecipeRenderers`: **15/15 tipos** registrados (crafting, manaweaving_pattern, manaweaving_altar, ritual, runesmithing, arcane_furnace, crushing, runescribing, spell_part, eldrin_altar, multiblock, entity, transmutation, fume_filter). ✅ Mesma contagem da fonte.
- `RecipeRendererBase` / `Recipe3x3`: migrados para `byKey().value()`, `renderWidget`, `GuiGraphics.renderItem`, `getResultItem(registryAccess())`. ✅ Fluxo byKey→RecipeHolder→.value() confirmado.

STATUS: **OK** (fluxo de render confirmado; 3086 receitas carregam, 0 parse errors por auditoria prévia).

---

## 3. GUI (telas)

### Fonte vs Port
- Gui* screens: port **42** (fonte equivalente). Container* (menus): port **38**.
- `ContainerInit`: `DeferredRegister<MenuType<?>>` com **39 registros** (blocks via `of(block)` path + items por nome). Todos com `IMenuTypeExtension.create(...)`. Inclui `GUIDE_BOOK = "guide_book"`. ✅
- `GuiInit.onRegisterMenuScreens(RegisterMenuScreensEvent)`: mapeia cada MenuType→Screen (37+ `event.register`). ✅

### ⚠️ Recursão `renderBg` — 3 chamadas remanescentes
O fix `_fix_renderbg_loop.py` removeu `this.renderBackground(...)` de dentro de `renderBg()` na maioria das telas (comentário `[PORT 1.21.1] NAO chamar...`). Mas **3 chamadas `this.renderBackground(` sobreviveram**:

| Arquivo | Chamada | Risco |
|---|---|---|
| `GuiSpectralCraftingTable` (extends `AbstractContainerScreen`) | dentro do próprio `render()` override, antes de checar recipebook | **Recursão? Não direta** — mas é `render()` que chama `renderBackground` que chama `renderBg`; como o `renderBg` já foi limpo, **não faz loop**. É o padrão vanilla de recipebook. Baixo risco, mas redundante. |
| `GuiConstructDiagnostics` (extends `Screen`) | dentro de `render()` | `Screen.renderBackground` NÃO chama `renderBg` (só `AbstractContainerScreen` faz). **Sem loop.** OK. |
| `GuiOcculus` | `this.renderBackground(pGuiGraphics, left+i*58, top, ...)` — **assinatura diferente** (helper próprio de tier badges) | Não é o vanilla 4-arg; é método local. OK. |

**Conclusão:** os 3 são falsos positivos do grep — nenhum é o loop `renderBg→renderBackground→renderBg`. O fix de recursão está completo. (Confirmar em runtime que SpectralCraftingTable abre sem stack overflow, mas a lógica indica seguro.)

STATUS: **OK**.

---

## 4. Effects

### Fonte vs Port
- `EffectInit`: `DeferredRegister<MobEffect>` → **69 efeitos** (idêntico fonte e port). Categorias: beneficial, harmful, neutral + `SimpleBeneficialEffect/SimpleHarmfulEffect/SimpleNeutralEffect`.
- Nomes com rename de path preservados (ex: `flame_cloak`, `fossilize`, `death`, `reduced`, `enlarged`).
- `EffectHelper.java` + `interfaces/`, `particles/` presentes (70 arquivos fonte = 70 port). ✅

STATUS: **OK** (69/69 registrados via DeferredRegister, mecanismo nativo NeoForge).

---

## 5. Loot

### Fonte 1.20.1
- `GlobalLootModifiers`: DeferredRegister `GLOBAL_LOOT_MODIFIER_SERIALIZERS` com 2 GLM (`drops_chimerite`=AddChimeriteModifier, `beheading`=BeheadingModifier). 10 loot functions registradas em common setup.
- `Random*` (10): RandomSpellStaff, RandomJournalPage, RandomManaweaveRecipe, RandomRunescribeRecipe, RandomConstructPart, RandomThaumaturgicLink, RandomJokeSpell, RandomArtifactSpell, RandomSilverSpell, RandomHat — cada uma com `LootItemFunctionType TYPE`.
- `conditions/`: `PlayerFactionCheck` (loot condition `player-faction`), `MALootConditions`.
- 393 loot tables em `data/mna/loot_tables/`; 11 loot modifiers em `data/mna/loot_modifiers/`.

### Port 1.21.1
- `GlobalLootModifiers`: DeferredHolder GLM OK; 10 loot functions migradas para `RegisterEvent` em `BuiltInRegistries.LOOT_FUNCTION_TYPE` via `helper.register(RLoc.create("random_..."), TYPE)`. ✅
- `MALootConditions`: `RegisterEvent` em `Registries.LOOT_CONDITION_TYPE`, registra `player-faction`. ✅
- `PlayerFactionCheck`: `MapCodec CODEC` + `LootItemConditionType PLAYER_FACTION`. ✅ (migração Codec 1.21.1 correta)
- Classes `Random*`: 10 presentes. `BeheadingModifier`/`AddChimeriteModifier` com CODEC. ✅

### ⚠️ CRÍTICO — pasta `loot_table` singular
393 JSONs estão em **`data/mna/loot_table/`** (SINGULAR). Vanilla lê **`data/mna/loot_tables/`** (PLURAL). → **Todo loot custom não carrega** (construct adventure tables, artifacts, entities, blocks, transmutation). Ex: `loot_table/artifacts/totally_legendary_artifacts.json` referencia `mna:random_joke_spell` mas nunca é lido.

### ⚠️ Global Loot Modifiers — índice e path
- O índice NeoForge `data/neoforge/loot_modifiers/global_loot_modifiers.json` lista 10 entradas `mna:*` (coal_chimerite, skeleton_head, ...). ✅ Path NeoForge correto.
- Mas os JSONs dos modifiers estão em **`data/mna/loot_modifiers/`** (11 arquivos) — esse é o path FORGE 1.20.1. O NeoForge 1.21.1 **também** usa `data/<ns>/loot_modifiers/` para os JSONs individuais (o índice fica em `data/neoforge/`). Então `data/mna/loot_modifiers/*.json` + índice `data/neoforge/...` está **correto** — o índice referencia por ID `mna:coal_chimerite` e o resolver acha em `data/mna/loot_modifiers/coal_chimerite.json`. ✅ (confirmar em runtime, mas o layout segue a spec GLM do NeoForge).

STATUS: Functions/Conditions/GLM code **OK** / **Loot tables FALTA (rename `loot_table`→`loot_tables`)**.

---

## 6. Progressão (tiers)

### Mecanismo (fonte e port idênticos)
- `ProgressionCondition` (recipe type `mna:progression-condition`): JSON `{type, advancement, tier}`. Liga um advancement a um passo de tier.
- `ProgressionEventHandler` (Bus.GAME):
  - `AdvancementEarnEvent`: ao ganhar advancement, marca `ProgressionCondition` correspondente → `progression.addTierProgressionComplete(id)`.
  - `getTierProgress(level) >= 1.0` → tier pronto.
  - `CanPlayerSleepEvent`: se tier 1 completo ao dormir → `setTier(tier+1)` (avança dormindo).
  - `confirmExistingAdvancements` no login (catch-up).
- Tier desbloqueia: completar todos os `ProgressionCondition` do tier atual → avançar. 5 tiers (0-4), teto `getTier() >= 5`.

### Datapack
- `data/mna/recipe/progression/{tier_1..tier_4}`: tier_1=20, tier_2=19, tier_3=16, tier_4=15 = **70 condições**. ✅ Contagem bate com a fonte.
- Cada uma referencia um advancement `mna:tier_N/...` — que depende do fix da pasta advancements (§1).

STATUS: **Lógica OK / depende do rename de advancements** para os `required_advancement` e `advancement` resolverem.

---

## Tabela final — O que precisa estar / como está no 1.21.1 / STATUS

| Sistema | O que precisa estar | Como está no 1.21.1 | STATUS |
|---|---|---|---|
| Advancement triggers | 29 codecs registrados como `mna:*` | 29 via `RegisterEvent`/`TRIGGER_TYPES`, `RLoc.create("...")`; TierTrigger com `Codec`+record | **OK** |
| Advancement predicates | 3 | 3 presentes (CraftSpell/RequiredConstructPart/SummonConstruct) | **OK** |
| Advancement datapack | `data/mna/advancements/` (plural), 88 JSONs | **`data/mna/advancement/` (SINGULAR)**, 88 JSONs | **FALTA (rename)** |
| Guidebook screen | GuiGuideBook 256×178, breadcrumbs, busca, tabs | Portado, `init/render/renderBg/renderLabels` OK | **OK** |
| Guidebook data | `assets/mna/guide/en_us.json` | 516 entries, reload() lê lang + pack_adds | **OK** |
| Guidebook entries/related | GuidebookEntry + RelatedRecipe + 15 renderers | 15/15 renderers; byKey→RecipeHolder→.value(); ISpellComponentRecipe tratado | **OK** |
| Recipe render flow | RecipeRendererBase/Recipe3x3 desenham | renderWidget + GuiGraphics + registryAccess; cycling 40t | **OK** |
| Categories | 12 EntryCategory | 12 (9 built-in + Register custom) | **OK** |
| Menus (containers) | 39 MenuType | 39 via `DeferredRegister<MenuType>` + `IMenuTypeExtension` | **OK** |
| Screens | 42 Gui*, mapeadas em RegisterMenuScreensEvent | 42; GuiInit.onRegisterMenuScreens 37+ register | **OK** |
| renderBg recursão | sem `renderBackground` dentro de `renderBg` | fix aplicado; 3 grep hits são falsos positivos (Screen/helper local/recipebook) | **OK** |
| MobEffects | 69 via DeferredRegister | 69/69 (EffectInit) | **OK** |
| Loot functions | 10 `mna:random_*` | 10 via `RegisterEvent`/`LOOT_FUNCTION_TYPE` | **OK** |
| Loot condition | `mna:player-faction` | Registrado em `LOOT_CONDITION_TYPE`, MapCodec | **OK** |
| Global Loot Modifiers | 2 (drops_chimerite, beheading) + índice | DeferredHolder OK; índice `data/neoforge/loot_modifiers/` lista 10 `mna:*`; JSONs em `data/mna/loot_modifiers/` | **OK** |
| Loot tables | `data/mna/loot_tables/` (plural), 393 | **`data/mna/loot_table/` (SINGULAR)**, 393 | **FALTA (rename)** |
| ProgressionCondition | `mna:progression-condition`, 70 | 70 (tier_1=20,2=19,3=16,4=15); serializer + handler portados | **OK** |
| Tier advance logic | dormir/advancement → setTier | ProgressionEventHandler Bus.GAME portado (MNACap, AdvancementHolder.id) | **OK** |

## Ações necessárias (não executadas — só análise)

1. **Rename `data/mna/advancement/` → `data/mna/advancements/`** (88 arquivos). Sem isso nenhum advancement do MNA é registrado; `required_advancement` do codex e os critérios de tier ficam órfãos.
2. **Rename `data/mna/loot_table/` → `data/mna/loot_tables/`** (393 arquivos). Sem isso todo loot custom (constructs, artifacts, entities, transmutation) não carrega.
3. Confirmar em runtime que a tela de advancements mostra as 88 e que mobs/constructs dropam o loot custom após os renames.

Nada foi editado. Relatório apenas.
