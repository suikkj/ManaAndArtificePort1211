package com.mna.capabilities;

import com.mna.capabilities.chunkdata.ChunkMagic;
import com.mna.capabilities.chunkdata.ChunkMagicSerializer;
import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.particles.ParticleAura;
import com.mna.capabilities.playerdata.magic.PlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicSerializer;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionSerializer;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpells;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsSerializer;
import com.mna.capabilities.worlddata.WorldMagic;
import com.mna.capabilities.worlddata.WorldMagicSerializer;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Capabilities do M&A registradas como AttachmentType do NeoForge 1.21.1.
 * Substitui o antigo framework de IForgeCapability (removido no 1.21).
 *
 * ATEN: por ora este arquivo e ADITIVO (esqueleto do bloco C). A registracao
 * efetiva ({@code ATTACHMENT_TYPES.register(...)}) e a migracao dos call-sites
 * (getCapability -> getData) sao o passo atomico do bloco C.
 */
public class CapabilitiesInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "mna");

    // Player data
    public static final Supplier<AttachmentType<PlayerMagic>> PLAYER_MAGIC =
        ATTACHMENT_TYPES.register("player_magic", () -> AttachmentType.builder((Supplier<PlayerMagic>) PlayerMagic::new)
            .serialize(new PlayerMagicSerializer()).copyOnDeath().build());
    public static final Supplier<AttachmentType<PlayerProgression>> PLAYER_PROGRESSION =
        ATTACHMENT_TYPES.register("player_progression", () -> AttachmentType.builder((Supplier<PlayerProgression>) PlayerProgression::new)
            .serialize(new PlayerProgressionSerializer()).copyOnDeath().build());
    public static final Supplier<AttachmentType<PlayerRoteSpells>> PLAYER_ROTE_SPELLS =
        ATTACHMENT_TYPES.register("player_rote_spells", () -> AttachmentType.builder((Supplier<PlayerRoteSpells>) PlayerRoteSpells::new)
            .serialize(new PlayerRoteSpellsSerializer()).copyOnDeath().build());

    // World / Chunk data
    public static final Supplier<AttachmentType<WorldMagic>> WORLD_MAGIC =
        ATTACHMENT_TYPES.register("world_magic", () -> AttachmentType.builder((Supplier<WorldMagic>) WorldMagic::new)
            .serialize(new WorldMagicSerializer()).build());
    public static final Supplier<AttachmentType<ChunkMagic>> CHUNK_MAGIC =
        ATTACHMENT_TYPES.register("chunk_magic", () -> AttachmentType.builder((Supplier<ChunkMagic>) ChunkMagic::new)
            .serialize(new ChunkMagicSerializer()).build());

    // Entity / transient
    public static final Supplier<AttachmentType<MAPFX>> ENTITY_MAPFX =
        ATTACHMENT_TYPES.register("entity_mapfx", () -> AttachmentType.builder((Supplier<MAPFX>) MAPFX::new).build());
    public static final Supplier<AttachmentType<ParticleAura>> ENTITY_PARTICLE_AURA =
        ATTACHMENT_TYPES.register("entity_particle_aura", () -> AttachmentType.builder((Supplier<ParticleAura>) ParticleAura::new).build());
}