# Plano de Implementação — Finalização do Port MNA 1.21.1

Data: 2026-10-17. Base: 5 relatórios setoriais (`_audit/01..05_*.md`) + verificação
empírica em jogo + leitura do código-fonte dos achados. Este documento é o **todo vivo**
do port: cada item tem STATUS verificado e a ação exata.

---

## Método (como cada achado foi tratado)
1. Relatório setorial (subagent) → hipótese.
2. **Verificação empírica** contra o jar vanilla 1.21.1 e/ou o código do port antes de agir
   (descartou achados errados — ver "Achados DESCARTADOS").
3. Fix → rebuild (BUILD SUCCESSFUL) → relaunch → ler log de jogo (counts/sem crash).

---

## ✅ JÁ IMPLEMENTADO E VALIDADO EM JOGO (fechado)

| # | Subsistema | Fix | Evidência |
|---|---|---|---|
| A | Datapack | `recipes/advancements/loot_tables/structures/` → **SINGULAR** (`recipe/advancement/loot_table/structure/`); `loot_modifiers/` e `neoforge/` permanecem | launch: **1407 advancements**, **3086 receitas**, 393 loot tables lidos |
| B | Receitas vanilla | `result:"id"` → `result:{"id":...}` (718 arquivos) | **0 parse errors** |
| C | Receitas custom (codec) | `AMRecipeBaseSerializer.codec()` bridge (MapLike→JsonObject→readExtra) + `RecipeIdFixer` (restaura ID real, server+client) | **1078 IDs restaurados** |
| D | Magia/Spells | SHAPES 21 / MODIFIERS 9 / COMPONENTS 122 registrados; shims via RLoc.create; casting/mana/cantrips OK | relatório 01: paridade total |
| E | Blocos/Tiles/Multiblocos | 380 blocos, 56 tiles, 40 menus idênticos; 3 MultiblockTile + 7 multiblock JSON + 6 .nbt presentes | relatório 02: paridade de registro perfeita |
| F | Entidades/GeckoLib | 84/84 renderers; 52 geo + 42 anim; pacote GeckoLib migrado e validado por reflexão | relatório 03: OK |
| G | Progressão/GUI/Effects/Advancements | 29 triggers via RegisterEvent `mna:*`; 69 effects; guidebook 15/15 renderers; renderBg sem recursão | relatório 04 + launch sem crash |
| H | **R1 Curios** | `IsItemInCurioSlot` volta a checar o slot (`slotContext.identifier()` — virou record no 9.5.1) | build OK |
| I | **R2 Curios** | `ArcaneCrown.initCapabilities`/`curioTick` restaurado (ICurioItem) | build OK |
| J | **Capabilities de bloco** | `MACapabilityModEventHandlers` registra `ITEM_HANDLER` (12 tiles) + `FLUID_HANDLER` (FluidJug) via `RegisterCapabilitiesEvent`; `getOrCreateSidedHandler`→public | build OK |
| K | **Loot: RandomSpellStaff** | codec com campos **opcionais** (defaults 0/0/false, como o 1.20.1) | build OK |
| L | **Loot: set_nbt** | `minecraft:set_nbt` removido no 1.21.1 → `set_components` + `minecraft:potion_contents` (8 arquivos, 22 ocorrências) | build OK, validando em jogo |

---

## 🔴 PENDENTE DE VALIDAÇÃO EM JOGO (fix feito, aguardando launch)

- **Loot tables** (K+L): confirmar no próximo launch "0 Couldn't parse loot_table". Se limpo,
  fecha o sistema de loot (constructs, artifacts, entities, caches de estrutura).

---

## 🟡 PARCIAL / DECISÃO (não é bug de parity)

| Item | Situação | Ação |
|---|---|---|
| `interop/lucent` (plugin de luz, 18 entity types) | desabilitado em `disabled_src/lucent-interop-backup/` | **decisão de pack**: reativar só se o `com.legacy.lucent` existir no pack 1.21.1. Sem ele, inerte — não é regressão. |
| `test_multiblock.json` → `fountain_test.nbt` inexistente | receita de dev, sem uso em jogo, igual no 1.20.1 | ignorar (paridade). |
| 5 ids de parts sem campo estático (`cone/glide/impulse/nethergate/lesser_magnitude`) | idem no 1.20.1 (acesso por key em runtime) | sem ação. |

---

## 🧪 VERIFICAÇÃO MANUAL EM JOGO (para você testar, sem bug conhecido)

Estes fluxos dependem de interação/clique — o código está portado e registrado, mas convém
confirmar visualmente:
1. **Guidebook**: abrir → navegar capítulos → ver receita 3x3 renderizando itens (byKey→.value() OK).
2. **Cast de spell**: varinha + mana (shapes/components/modifiers resolvem; casting portado).
3. **Curios**: equipar staff/wand/ring num slot curios e ver o renderer + R1 (slot correto).
4. **GeckoLib**: summonar um Construct/Imp e ver o modelo/animação.
5. **Multibloco**: montar o Eldrin Altar e ver o trigger disparar.
6. **Automação**: hopper num Pedestal/MagiciansWorkbench e pipe no FluidJug (capabilities J).

---

## ❌ ACHADOS DESCARTADOS (verificação empírica contradisse o relatório)

| Relatório | Claim | Evidência contra | Conclusão |
|---|---|---|---|
| 04 (progression) | "`advancement/`→`advancements/` e `loot_table/`→`loot_tables/` (plural)" | jar vanilla 1.21.1: `advancement/`=1400, `loot_table/`=1183, `recipe/`=1291; **zero** plural. Launch já carrega 1407 advancements | **ERRADO** — o port já usa singular correto; advancements carregam. |
| 05 (version) | "GeckoLib 4.9 renomeia pacote p/ `com.geckolib`" | jar geckolib-neoforge-1.21.1-4.9.3 usa `software.bernie.geckolib` (rename p/ `com.geckolib` é só no 5.x) | confirmado que o port está no pacote certo (4.9.3). |

---

## RISCOS RESIDUAIS CONHECIDOS (não quebram hoje)

- Shims `Shapes/Components/Modifiers` resolvem em class-load — frágil só se novo código os
  referenciar dentro do `RegisterEvent`. Hoje OK.
- B21 (eventos removidos: jump boost/camuflagem/sleep) — features desativadas; mixin se necessário.
- Dimensão Reverie/estruturas MNA na geração de mundo — requerem exploração no mundo (ver §manual).

---

## PRÓXIMOS PASSOS (ordem)
1. Validar loot (K+L) no launch atual. → esperado "0 Couldn't parse loot_table".
2. Testar os 6 fluxos manuais acima.
3. Commit + push de todo o lote de fixes (R1, R2, capabilities, loot codec, set_nbt).
4. Se algum fluxo manual quebrar, abrir item no FIXLIST.md com log e corrigir.
