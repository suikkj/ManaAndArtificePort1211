 
package com.mna.blocks.tileentities.wizard_lab;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.items.sorcery.ItemTornJournalPage;
import com.mna.spells.SpellCaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.mna.api.tools.MNACap;

public class StudyDeskTile
extends WizardLabTile
implements IEldrinConsumerTile {
    public static final int SLOT_THESIS = 0;
    public static final int INVENTORY_SIZE = 1;

    public StudyDeskTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public StudyDeskTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.STUDY_DESK.get(), pos, state, 1);
    }

    @Override
    public boolean canActivate(Player player) {
        boolean base = this.canContinue();
        if (!base) {
            return false;
        }
        return this.getCantStartReason(player) == CantStartReason.NONE;
    }

    @Override
    protected boolean canContinue() {
        return this.hasStack(0);
    }

    @Override
    public float getPctComplete() {
        return this.hasStack(0) ? 1.0f : 0.0f;
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(new Integer[0]);
    }

    @Override
    protected boolean canActiveTick() {
        return true;
    }

    @Override
    protected void onComplete() {
        ItemStack thesis = this.getItem(0);
        Player crafter = this.getCrafter();
        if (thesis.isEmpty() || crafter == null) {
            return;
        }
        HashMap<ISpellComponent, Float> partsToRote = new HashMap<ISpellComponent, Float>();
        if (thesis.getItem() instanceof ItemTornJournalPage && !this.getLevel().isClientSide()) {
            ISpellComponent component = ((ItemTornJournalPage)thesis.getItem()).getComponent(thesis);
            if (component == null) {
                return;
            }
            partsToRote.put(component, Float.valueOf(((ItemTornJournalPage)thesis.getItem()).isThesis(thesis) ? (float)component.requiredXPForRote() : (float)component.requiredXPForRote() * 0.25f));
        } else if (thesis.getItem() instanceof ICanContainSpell) {
            ISpellDefinition spell = ((ICanContainSpell)thesis.getItem()).getSpell(thesis);
            if (!spell.isChargedSpell(thesis) || spell.isMysterious()) {
                return;
            }
            partsToRote.put(spell.getShape().getPart(), Float.valueOf((float)spell.getShape().getPart().requiredXPForRote() * 0.05f));
            spell.iterateComponents(c -> partsToRote.put((ISpellComponent)c.getPart(), Float.valueOf((float)((SpellEffect)c.getPart()).requiredXPForRote() * 0.1f)));
            spell.getModifiers().forEach(m -> partsToRote.put((ISpellComponent)m, Float.valueOf((float)m.requiredXPForRote() * 0.1f)));
        } else {
            return;
        }
        thesis.shrink(1);
        if (!this.getLevel().isClientSide()) {
            this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Gui.CHARCOAL_SCRIBBLE, SoundSource.BLOCKS, 1.0f, (float)(0.95 + Math.random() * (double)0.1f));
        }
        MNACap.get(crafter, PlayerRoteSpellsProvider.ROTE).ifPresent(p -> {
            ArrayList<ISpellComponent> alreadyRote = new ArrayList<ISpellComponent>();
            for (Map.Entry entry : partsToRote.entrySet()) {
                if (p.isRote((ISpellComponent)entry.getKey())) {
                    alreadyRote.add((ISpellComponent)entry.getKey());
                    continue;
                }
                p.addRoteXP(crafter, (ISpellComponent)entry.getKey(), ((Float)entry.getValue()).floatValue());
                float progression = p.getRoteProgression((ISpellComponent)entry.getKey());
                partsToRote.put((ISpellComponent)entry.getKey(), Float.valueOf(progression));
                if (!(crafter instanceof ServerPlayer)) continue;
                CustomAdvancementTriggers.STUDY_DESK_ROTE.trigger((ServerPlayer)crafter, ((ISpellComponent)entry.getKey()).getRegistryName(), progression);
            }
            for (ISpellComponent iSpellComponent : alreadyRote) {
                partsToRote.remove(iSpellComponent);
            }
        });
        partsToRote.forEach((c, p) -> {
            if (p.floatValue() < 1.0f) {
                ChatFormatting color = ChatFormatting.WHITE;
                IFaction faction = c.getFactionRequirement();
                if (faction != null) {
                    color = faction.getTornJournalPageFactionColor();
                }
                crafter.sendSystemMessage((Component)Component.translatable((String)"item.mna.torn_journal_page.learned.prefix").withStyle(ChatFormatting.ITALIC).append((Component)Component.translatable((String)c.getRegistryName().toString()).withStyle(color)).append((Component)Component.translatable((String)"item.mna.torn_journal_page.learned.suffix").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.WHITE)));
            } else {
                SpellCaster.sendRoteMessage(crafter, c);
            }
        });
    }

    @Override
    protected boolean needsPower() {
        return false;
    }

    @Override
    public int getXPCost(Player crafter) {
        ItemStack thesis = this.getItem(0);
        if (!thesis.isEmpty()) {
            if (thesis.getItem() instanceof ItemTornJournalPage) {
                return ((ItemTornJournalPage)thesis.getItem()).isThesis(thesis) ? 100 : 20;
            }
            if (thesis.getItem() instanceof ICanContainSpell) {
                ISpellDefinition spell = ((ICanContainSpell)thesis.getItem()).getSpell(thesis);
                if (!spell.isChargedSpell(thesis) || spell.isMysterious()) {
                    return super.getXPCost(crafter);
                }
                return 20;
            }
        }
        return super.getXPCost(crafter);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !this.isActive();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.isActive()) {
            return false;
        }
        ItemStack existing = this.getItem(index);
        switch (index) {
            case 0: {
                if (existing.getItem() instanceof ItemTornJournalPage) {
                    return existing.isEmpty() || existing.getCount() < existing.getMaxStackSize();
                }
                if (!(existing.getItem() instanceof ICanContainSpell)) break;
                ISpellDefinition spell = ((ICanContainSpell)existing.getItem()).getSpell(existing);
                return spell.isChargedSpell(existing) && !spell.isMysterious();
            }
        }
        return false;
    }

    public CantStartReason getCantStartReason(Player player) {
        if (!player.isCreative() && player.totalExperience < this.getXPCost(player)) {
            return CantStartReason.NOT_ENOUGH_XP;
        }
        java.util.Optional<PlayerRoteSpellsProvider> rote = MNACap.get(player, PlayerRoteSpellsProvider.ROTE);
        java.util.Optional<PlayerProgressionProvider> progression = MNACap.get(player, PlayerProgressionProvider.PROGRESSION);
        if (!rote.isPresent() || !progression.isPresent()) {
            return CantStartReason.MISSING_CAPABILITY;
        }
        ItemStack thesis = this.getItem(0);
        ArrayList<ISpellComponent> components = new ArrayList<ISpellComponent>();
        if (thesis.getItem() instanceof ItemTornJournalPage) {
            ISpellComponent component = ((ItemTornJournalPage)thesis.getItem()).getComponent(thesis);
            if (component == null) {
                return CantStartReason.EMPTY_SPELL;
            }
            components.add(component);
        } else if (thesis.getItem() instanceof ICanContainSpell) {
            ISpellDefinition spell = ((ICanContainSpell)thesis.getItem()).getSpell(thesis);
            if (!spell.isChargedSpell(thesis)) {
                return CantStartReason.NON_CHARGED_SPELL;
            }
            if (spell.isMysterious()) {
                return CantStartReason.MYSTERIOUS_SPELL;
            }
            components.add(spell.getShape().getPart());
            spell.iterateComponents(c -> components.add((ISpellComponent)c.getPart()));
            spell.getModifiers().forEach(m -> components.add((ISpellComponent)m));
        }
        if (components.size() == 0) {
            return CantStartReason.EMPTY_SPELL;
        }
        if (components.stream().anyMatch(c -> ((IPlayerProgression)progression.get()).getTier() < c.getTier(player.level()))) {
            return CantStartReason.TIER_TOO_LOW;
        }
        if (components.stream().allMatch(c -> ((IPlayerRoteSpells)rote.get()).isRote((ISpellComponent)c))) {
            return CantStartReason.ALREADY_FULLY_ROTE;
        }
        return CantStartReason.NONE;
    }

    public static enum CantStartReason {
        NONE(""),
        MISSING_CAPABILITY("gui.mna.study_desk.capability_missing"),
        MYSTERIOUS_SPELL("gui.mna.study_desk.mysterious_spell"),
        NON_CHARGED_SPELL("gui.mna.study_desk.non_charged"),
        ALREADY_FULLY_ROTE("gui.mna.study_desk.already_rote"),
        TIER_TOO_LOW("gui.mna.study_desk.low_tier"),
        NOT_ENOUGH_XP("gui.mna.study_desk.not_enough_xp"),
        EMPTY_SPELL("gui.mna.study_desk.empty_spell");

        private Component tooltip;

        private CantStartReason(String componentTranslationString) {
            this.tooltip = Component.translatable((String)componentTranslationString);
        }

        public Component tooltip() {
            return this.tooltip;
        }
    }
}


