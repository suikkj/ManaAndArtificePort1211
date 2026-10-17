# Plano de Implementação — Port Mana and Artifice 3.1.11 (Forge 1.20.1 → NeoForge 1.21.1)

> **v2 — reformulado em 2026-10-17 sob a metodologia do pacote "Minecraft Skills"**
> (RochStudio; SKILL.md + references/java-porting, behavior-recovery, mod-implementation,
> interactive-item-ports, inventory-ui-ports, rendering-assets-sfx, testing-release).
> A v1 está preservada no histórico do git. Este documento é o **todo vivo** do port:
> status por subsistema, camadas de evidência separadas e próximas ações reproduzíveis.

---

## 0. O pedido (reformulado)

### 0.1 Pedido original (verbatim, 11/10/2026)

> "Antes de prosseguir, destrinche no mod da 1.20.1 todas as funções do mod, suas receitas,
> seus blocos e interações, suas magias, suas estruturas multiblock. Após isso, crie uma
> explicação detalhada sobre o que é preciso estar e como deve estar no mod da 1.21.1. Após
> analisar todo o mod e destrinchar todas as suas ações, crie um Plano de Implementação
> explicando sobre cada ação necessária a ser feita, cada aplicação necessária. Pesquise
> profundamente sobre a versão do minecraft 1.20.1 e a 1.21.1, veja suas mudanças, suas
> maneiras de fazer mods e as mudanças de classes feitas. Após montar o Plano de
> Implementação, revise novamente o mod da 1.20.1 e verifique se está exatamente tudo ali.
> Após isso, comece a implementar em meu mod da 1.21.1 o que está restando. Verifique também
> o que está em conexão com os mods curios e geckolib da 1.20.1 para que eles estejam
> funcionando perfeitamente com a versão da 1.21.1."
> — Pastas de teste: `.../Mana and Artifice Source 1.20.1/mods` e `.../Mana and Artifice Port 1.21.1/mods`.

### 0.2 Pedido estruturado (parâmetros explícitos)

| # | Item pedido | Estado |
|---|---|---|
| 1 | Inventário do original: funções, receitas, blocos, interações, magias, multiblocks | ✅ `_audit/01..05_*.md` (5 relatórios setoriais) + §5 |
| 2 | Explicação do que precisa existir no alvo 1.21.1 | ✅ §3 (mapa de cobertura) + §4 (adaptações) |
| 3 | Plano de Implementação por ação | ✅ este documento |
| 4 | Pesquisa 1.20.1 → 1.21.1 (mudanças de classes/APIs) | ✅ §2 + skill `neoforge-121-api-migrations` (pitfalls de runtime) |
| 5 | Re-revisão do original ("está tudo ali?") | ✅ §5 (paridade jar-fonte × jar-port) |
| 6 | Implementar o que resta | 🟡 §7 (ações priorizadas) |
| 7 | Curios + GeckoLib "funcionando perfeitamente" | 🟡 §8 (build ok; verificação em jogo pendente) |
| 8 | Instalar nas instâncias + versionar no GitHub | ✅ instalado; push contínuo em `suikkj/ManaAndArtificePort1211` |

---

## 1. Tupla de compatibilidade (registrada)

| Campo | Fonte (1.20.1) | Alvo (1.21.1) |
|---|---|---|
| Edição/versão | Java 1.20.1 | Java 1.21.1 |
| Loader | Forge 47.x | **NeoForge 21.1.248** |
| Build plugin | (jar `-all` fechado; sem build fonte) | ModDevGradle 2.0.71 |
| Gradle | — | 8.10.2 |
| Compile JDK | 17 | **21** (toolchain) |
| Mappings | SRG (1.20.1) | Parchment `2024.11.17` (official 1.21.1) |
| GeckoLib | 4.7 (`geckolib-forge-1.20.1-4.7`) | **4.9.3** (`geckolib-neoforge-1.21.1-4.9.3`) |
| Curios | 5.14.1+1.20.1 | **9.5.1+1.21.1** |
| Mod | `mna-forge-1.20.1-3.1.11-all.jar` | `build/libs/mna-3.1.11.jar` — sha256 `8b98a23ddbd8eb658ff05f25122d5eb5911cf0be6a6a94b538ddd2b1a13edba6` |
| Instância | `.../Instances/Mana and Artifice Source 1.20.1` | `.../Instances/Mana and Artifice Port 1.21.1` |

Workspace: `C:/Users/lucas/Documents/coisas/Entre Mods e Códigos/Mana And Artifice 1.21.1/mna-port-workspace`.
Fonte descompilada de referência: `../decompiled/` (oráculo de comportamento).

---

## 2. Contrato de comportamento (o que não pode mudar)

- **Namespace `mna:` e todos os registry IDs preservados** (items, blocks, entities, spell parts, effects, enchantments).
- **Save fields**: attachment `mna:world_magic` (+ progression), NBT de tiles, capabilities de bloco (item/fluid handlers), dados de Curios.
- **Dados**: 1867 arquivos de receita, 98 advancements, 402 loot tables, 72 estruturas — schemas migrados para o formato 1.21.1 (singular, `result:{id}`, componentes).
- **Fidelidade observável**: ordenação/RNG/side-effects preservados onde observável; quirks do original documentados em §4 (não "consertados" silenciosamente).
- **Pesquisa de API 1.20.1→1.21.1** consolidada na skill `neoforge-121-api-migrations` (seções: runtime-only breakers, DataComponents, dynamic enchantments, RecipeHolder/streamCodec, ObjectHolder, EventBus, etc.).

---

## 3. Mapa de cobertura (subsistema → alvo → status → evidência)

Status: ✅ implementado **e verificado** · 🟢 implementado, verificação de gameplay pendente · 🔵 adaptado (diferença declarada) · ⏸ bloqueado

| # | Subsistema | Implementação no alvo | Status | Evidência |
|---|---|---|---|---|
| 1 | Build/toolchain | ModDevGradle 2.0.71, Java 21, Parchment, NeoForge 21.1.248 | ✅ | clean compile 0 erros (46.940→0); `BUILD SUCCESSFUL` |
| 2 | Registro geral | 380 blocos, 56 tiles, 40 menus, 84 renderers, 69 effects | ✅ | relatórios 02/03 + launch |
| 3 | **Spells** (SHAPES 21 / COMPONENTS 122 / MODIFIERS 9) | shims via `RLoc.create`; casting/mana/cantrips portados | 🟢 | relatório 01; cast em jogo não testado |
| 4 | **Receitas** (1867; manaweaving 530, eldrin_altar 122) | codec bridge MapLike→JsonObject + `RecipeIdFixer` + ID no streamCodec | ✅ dados / 🟢 GUI | launch: **3086 receitas**, **1078 IDs restaurados**, 0 parse errors |
| 5 | Advancements (98; 29 triggers custom) | `RegisterEvent` + Codec | ✅ | launch: **1407 advancements** |
| 6 | Loot (402 tabelas + modifiers) | `set_components` (era `set_nbt`); codec `RandomSpellStaff` opcional | ✅ | launch: **0 parse errors** (era 22) |
| 7 | Capabilities de bloco | `RegisterCapabilitiesEvent`: ITEM_HANDLER (12 tiles) + FLUID_HANDLER (FluidJug) | ✅ | log: `Registered block capabilities` |
| 8 | Attachment `mna:world_magic` | `ATTACHMENT_TYPES` registrado no bus | ✅ | player entra sem "unbound value" |
| 9 | **Curios** | adapters 9.5.1 (record `SlotContext`, `ICurioItem`) | 🟢 | build ok (§8); equip em jogo pendente |
| 10 | **GeckoLib** | 4.9.3, pacote `software.bernie.geckolib`, eventos `animation.keyframe.event.*` | 🟢 | relatório 03: 84/84 renderers, 52 geo + 42 anim; summon pendente |
| 11 | Mixins (16) | `[[mixins]]` no mods.toml; PlayerPoseMixin etc. | 🟢 | log: `Mixing LivingEntityFireWalkerMixin from mna.mixins.json` |
| 12 | Networking | `streamCodec` serializa o ID da receita (1.21.1 consome antes) | ✅ | 0 `Failed to decode packet` |
| 13 | **Rendering/assets** | 807 modelos `forge:item/default`→`neoforge:item/default`; `pylon_ring.obj` restaurado; shader `fog_distance` 2-arg; `renderBg` sem recursão (24 GUIs) | ✅ | launch: **0 "Unable to load model"**, 0 `Failed to load model` (eram 2) |
| 14 | Enchantments (registry dinâmico) | `MNAEnchantmentHelper` key-based (sem `.get()` em runtime) | ✅ | launch ok (era crash `mna:returning`) |
| 15 | GUIs (24+) | `renderBg` sem chamar `renderBackground` (recursão infinita 831 frames) | 🟢 | StackOverflow resolvido; guidebook test pendente |
| 16 | Multiblocks | 3 `MultiblockTile` + 7 JSON + 6 `.nbt` | 🟢 | relatório 02; montagem em jogo pendente |
| 17 | Worldgen (72 estruturas, biomas) | datapack singular | 🟢 | estruturas no jar; exploração pendente |
| 18 | Sons (257) | — | 🟢 | carregam; listening não revisado |
| 19 | Item attributes | `Holder<Attribute>` (AstroBlade, HellfireTrident) | ✅ build / 🟢 equip | CCE corrigido (era crash ao segurar) |
| 20 | Interop lucent | desabilitado (`disabled_src/`) | 🔵 | dep opcional ausente no pack; inerte (não é regressão) |
| 21 | OffsetBlockTile / FillerBlock | guards de offset null (`getUpdateTag`/`saveAdditional`/`getOffsetPos`) | ✅ | launch atual: 0 NPE (era crash às 04:46) |
| 22 | Config events (`ModConfigEvent.Unloading`) | guard nos 3 handlers (`GeneralConfig`/`ClientConfig`/`SpellConfig`) | ✅ | quit validado 14:41: "Unloading configs type SERVER" sem erro (era "Uncaught exception") |
| 23 | `ManaweaveCacheTile` save (loot key) | `getLootTable()`/`setLootTable()` retornam `null` sem loot table (não criam key com location null) | 🟢 | fix buildado+instalado; valida no próximo reload do mundo |

---

## 4. Adaptações declaradas (diferenças vs original)

| # | Diferença | Razão (mudança de versão) |
|---|---|---|
| A | `forge:item/default` → `neoforge:item/default` (807 modelos) | Forge shipava `assets/forge/models/`; NeoForge shipa `assets/neoforge/models/` — **conteúdo byte-idêntico** (verificado) |
| B | `minecraft:set_nbt` → `set_components`+`potion_contents` (8 loot tables) | função removida no 1.21.1 |
| C | `fluid_jug`: function `set_contents` removida; `chalk_rune`: `"metal": true`→`"true"` | dados obsoletos que o Forge tolerava; idênticos no jar 1.20.1 (não era regressão) |
| D | Datapack **singular** (`recipe/`, `advancement/`, `loot_table/`, `structure/`) | exigência do NeoForge 1.21.1 |
| E | Receitas `result:"id"` → `result:{"id":…}` (718 arquivos) | schema 1.21.1 |
| F | Recipe codec bridge + `RecipeIdFixer` + ID dentro do `streamCodec` | 1.21.1 carrega via `codec()` e o `RecipeHolder.STREAM_CODEC` consome o ID antes do serializer |
| G | Enchantments key-based (`getKey()`) | registry **dinâmico** no 1.21.1 (`.get()`/`.value()` lançam "Registry not present") |
| H | Reflexão p/ campos privatizados: `MNAFallingBlocks`, `MNAGoalSelectorAccess`, `MNAPoseStackAccess`, `MNAMenuAccess`, `MNAReflection`, `MNATileAccess`, `BubbleBoat` | campos viraram `private` no 1.21.1 |
| I | Guards vazios do decompiler restaurados (68 em 24 arquivos); casts içados movidos p/ dentro do short-circuit | artefatos de decompilação que quebravam em runtime |
| J | `renderBg` não chama mais `renderBackground` | recursão infinita no 1.21.1 (StackOverflow 831 frames) |
| K | `pylon_ring.obj` restaurado (cópia de `pylon_ring_old.obj`) | o ORIGINAL referencia `pylon_ring.obj` que não existe (quebrado no 1.20.1 também — corrigido no port) |
| L | `@ObjectHolder` removido no NeoForge → instanciação direta (Music.DANCE_MIX etc.) | Forge injetava por anotação |
| M | `interop/lucent` desabilitado | dep `com.legacy.lucent` ausente no pack 1.21.1 |
| N | Guard em `ModConfigEvent.Unloading` (3 handlers de config) | evento **novo** do NeoForge no stop do servidor; ler config nele lança "Cannot get config value before config is loaded" |
| O | Tip `helptip.mna.pretty_models` (`forge-client.toml` → `neoforge-client.toml`) | NeoForge mantém `experimentalForgeLightPipelineEnabled`, mas no arquivo `neoforge-client.toml` (6 langs) |
| P | `ManaweaveCacheTile.getLootTable()`: `null` quando sem loot table | `ResourceKey.create(..., null)` gera key não-nula com location null; `trySaveLootTable` do vanilla NPE-a no `.location().toString()` |

---

## 5. Revisão de completude do original ("está tudo ali?")

Contagens **jar-fonte × jar-port** (2026-10-17, comando reproduzível em §9):

| Categoria | Fonte | Port | Δ |
|---|---|---|---|
| `assets/mna/models/` | 3911 | 3912 | +1 (`pylon_ring.obj` restaurado) |
| `assets/mna/textures/` | 1819 | 1819 | 0 |
| `assets/mna/sounds/` | 257 | 257 | 0 |
| `assets/mna/lang/` | 7 | 7 | 0 |
| `data/mna/recipe/` | 1867 | 1867 | 0 |
| `data/mna/advancement/` | 98 | 98 | 0 |
| `data/mna/loot_table/` | 402 | 402 | 0 |
| `data/mna/structure/` | 72 | 72 | 0 |

Cobertura funcional: os 5 relatórios setoriais (`_audit/01_spells`, `02_blocks_multiblocks`, `03_entities_geckolib`, `04_progression_gui`, `05_version_api`) destrincham funções, receitas, blocos, interações, magias e multiblocks do original; achados contraditos por verificação empírica ficam registrados (v1 §"descartados": ex. claim de plural no datapack era errado; `com.geckolib` é rename do 5.x, não do 4.9.3).

---

## 6. Camadas de evidência (o que está provado × o que não está)

| Camada | Prova | Deixa aberto |
|---|---|---|
| Compile limpo (0 erros) | APIs corretas p/ o alvo | Nada de gameplay |
| Launch + registries | 3086 receitas, 1407 advancements, 1078 IDs, 0 loot errors, capabilities, 0 crash | Interações, GUI, combate |
| Cliente conectado | Player entra, HUD do MNA (mana/afinidades), menus abrem/fecham | Cast, guidebook completo, curios |
| Visual (capturas) | Mundo renderiza, HUD, menus | Modelos/animações de itens e entidades específicas |
| **Não verificado** | — | Guidebook (receitas/passo a passo), cast de spell, equipar Curios, Construct GeckoLib, multiblock Eldrin Altar, automação (hopper/pipe), save/reload, death/respawn, sons (listening) |

> Regra (testing-release): "A successful compile, test count or screenshot count does not establish complete parity."
> Pass/fail/skip/unverified ficam separados — nenhum item acima é promovido por inferência.

---

## 7. Ações restantes (priorizadas)

**P1 — fechar o bug reportado (guidebook: craftings/passo a passo não apareciam)**
1. Launch de validação do lote atual (offset guards + 807 modelos + pylon obj): ✅ **0 warnings de modelo, 0 crashes**, 3086 receitas, 1078 IDs, player no mundo.
2. ✅ **Evidência visual (14:26–14:29)**: guidebook aberto exibindo **receita com itens renderizados na grade 3×3** + tabs de receitas relacionadas + navegação — capturas `computer_use_ac1e1fbb…png`, `computer_use_36ac6f37…png` (usuário navegando ao vivo; magic level 20 via comando).
3. Pendente: navegação completa pelo usuário ("passo a passo") + inventário criativo (ícones dos itens MNA com os modelos corrigidos).

**P2 — interações principais**
4. Cast de spell: varinha + mana + shapes/components resolvendo.
5. Curios: equipar belt/ring/staff num slot e ver renderer (§8).
6. GeckoLib: summonar Construct/Imp → modelo + animação (§8).

**P3 — sistemas**
7. Multiblock: montar Eldrin Altar e ver trigger.
8. Automação: hopper no MagiciansWorkbench/Pedestal; pipe no FluidJug.
9. Save/reload + death/respawn (lifecycle).
10. Sons: listening review (257 arquivos decodificam; qualidade não revisada).

**P4 — higiene**
11. `updateJSONURL` do mods.toml aponta p/ `manaandartifice.com/update.json` (retorna não-JSON → erro benigno do VersionChecker do NeoForge). Opções: apontar p/ releases do repo ou remover. (Não afeta gameplay.)

---

## 8. Curios + GeckoLib (pedido explícito do usuário)

**Curios (9.5.1):**
- R1 `IsItemInCurioSlot` → `slotResult.slotContext().identifier()` (`SlotContext` virou **record** no Curios 9.x) — ✅ build.
- R2 `ArcaneCrown.initCapabilities`/`curioTick` restaurado (provider `ICurioItem`) — ✅ build.
- Pendente em jogo: equipar item num slot curios e verificar (a) o efeito de slot (R1) e (b) o renderer do item equipado.

**GeckoLib (4.9.3):**
- Pacote correto: `software.bernie.geckolib` (o rename p/ `com.geckolib` é do 5.x — NÃO se aplica).
- Eventos de keyframe: `animation.keyframe.event.*` (`CustomInstructionKeyframeEvent`, `KeyFrameEvent`, `ParticleKeyframeEvent`, `SoundKeyframeEvent`); `RenderUtils`→`RenderUtil`.
- Relatório 03: 84/84 renderers, 52 geo + 42 anim migrados; pendente: summon em jogo p/ ver modelo+animação.

---

## 9. Checkpoint (formato testing-release)

- **Revisão**: port `mna` 3.1.11; commits `61a7c00` (runtime batch), `6bca8b1` (FIXLIST), + lote atual (offset guards, 807 modelos, pylon obj, plano v2).
- **Artefato**: `build/libs/mna-3.1.11.jar` — sha256 `8b98a23ddbd8eb658ff05f25122d5eb5911cf0be6a6a94b538ddd2b1a13edba6` (completo em `_build_check.log`); instalado como `mods/mna-neoforge-1.21.1-3.1.11.jar` na instância Port.
- **Tupla**: §1. **Evidência**: `mna_run.log` (launch 14:23 — 0 warnings de modelo, 0 crashes), `_build_check.log`.
- **Implementado/adaptado/pendente**: §3 (21 subsistemas) e §4 (13 adaptações).
- **Próxima tarefa concreta**: entrar no mundo → abrir guidebook → verificar receita renderizando.
  Reproduzível: `cd '/c/Users/lucas/curseforge/minecraft/Instances/Mana and Artifice Port 1.21.1' && bash launch_mna.sh`.
- **Limitações**: §6 (nada promovido por inferência; multiplayer/WAN não testado; sons não ouvidos).

---

## 10. Regras de processo (metodologia Minecraft-Skills)

1. Não alegar paridade por compilação — camadas de evidência separadas (§6).
2. Medir por **clean compile** (`_cv.sh` → `_build_last.log`); reverter ao snapshot se a contagem subir.
3. IDs estáveis + save fields preservados; quirks do original documentados, nunca "consertados" em silêncio (§4).
4. Instalação e publicação são ações **separadas e verificadas** (hash do jar; push no repo com verificação).
5. `FIXLIST.md` continua como fila de erros viva; **este plano** como todo de subsistemas.
6. Cada novo bug de runtime: entra na fila → verificação empírica (contra original/jar vanilla) → fix → build → launch → evidência.
7. **Instalar o jar só com o jogo fechado** — `cp` sobre `mods/*.jar` com o jogo aberto (Windows) faz o classloader ler o jar inconsistente → `ClassNotFoundException` aleatório em classes ainda não carregadas (crash 14:50 do `ImageWidget`; artefato de hot-swap, NÃO bug do mod). Checar `Get-Process java` antes do cp.
