/*
 * Registros custom do M&A convertidos para registries vanilla do NeoForge 1.21.1.
 * IForgeRegistry/NewRegistryEvent/RegistryBuilder foram removidos; aqui usamos
 * DeferredRegister.create(ResourceKey) + makeRegistry() que devolve o Registry<T>.
 * MnaRegistry preserva getValues()/getRegistryKey()/containsValue() (antigos metodos
 * de IForgeRegistry) para os call-sites continuarem funcionando.
 */
package com.mna;

import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.faction.IFaction;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.RLoc;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class Registries {
    public static final ResourceKey<Registry<Shape>> SHAPES_KEY =
        ResourceKey.createRegistryKey(RLoc.create("shapes"));
    public static final DeferredRegister<Shape> SHAPES =
        DeferredRegister.create(SHAPES_KEY, "mna");
    public static final MnaRegistry<Shape> Shape =
        new MnaRegistry<>(SHAPES.makeRegistry(b -> b.sync(true)));

    public static final ResourceKey<Registry<SpellEffect>> SPELL_EFFECT_KEY =
        ResourceKey.createRegistryKey(RLoc.create("components"));
    public static final DeferredRegister<SpellEffect> SPELL_EFFECT =
        DeferredRegister.create(SPELL_EFFECT_KEY, "mna");
    public static final MnaRegistry<SpellEffect> SpellEffect =
        new MnaRegistry<>(SPELL_EFFECT.makeRegistry(b -> b.sync(true)));

    public static final ResourceKey<Registry<Modifier>> MODIFIER_KEY =
        ResourceKey.createRegistryKey(RLoc.create("modifiers"));
    public static final DeferredRegister<Modifier> MODIFIER =
        DeferredRegister.create(MODIFIER_KEY, "mna");
    public static final MnaRegistry<Modifier> Modifier =
        new MnaRegistry<>(MODIFIER.makeRegistry(b -> b.sync(true)));

    public static final ResourceKey<Registry<RitualEffect>> RITUAL_EFFECT_KEY =
        ResourceKey.createRegistryKey(RLoc.create("ritual-effects"));
    public static final DeferredRegister<RitualEffect> RITUAL_EFFECT =
        DeferredRegister.create(RITUAL_EFFECT_KEY, "mna");
    public static final MnaRegistry<RitualEffect> RitualEffect =
        new MnaRegistry<>(RITUAL_EFFECT.makeRegistry(b -> b.sync(true)));

    public static final ResourceKey<Registry<ConstructTask>> CONSTRUCT_TASK_KEY =
        ResourceKey.createRegistryKey(RLoc.create("construct_task"));
    public static final DeferredRegister<ConstructTask> CONSTRUCT_TASK =
        DeferredRegister.create(CONSTRUCT_TASK_KEY, "mna");
    public static final MnaRegistry<ConstructTask> ConstructTasks =
        new MnaRegistry<>(CONSTRUCT_TASK.makeRegistry(b -> b.sync(true)));

    public static final ResourceKey<Registry<IFaction>> FACTIONS_KEY =
        ResourceKey.createRegistryKey(RLoc.create("factions"));
    public static final DeferredRegister<IFaction> FACTIONS =
        DeferredRegister.create(FACTIONS_KEY, "mna");
    public static final MnaRegistry<IFaction> Factions =
        new MnaRegistry<>(FACTIONS.makeRegistry(b -> b.sync(true)));

    public static void register(IEventBus bus) {
        SHAPES.register(bus);
        SPELL_EFFECT.register(bus);
        MODIFIER.register(bus);
        RITUAL_EFFECT.register(bus);
        CONSTRUCT_TASK.register(bus);
        FACTIONS.register(bus);
        ManaAndArtifice.LOGGER.info("Registered entity registries!");
    }
}
