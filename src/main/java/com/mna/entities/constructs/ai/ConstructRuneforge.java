 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructCommandTileEntityInteract;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ConstructRuneforge
extends ConstructCommandTileEntityInteract<BlockEntity, ConstructRuneforge> {
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.SMITH};
    private int interactTimer = this.getInteractTime(ConstructCapability.SMITH);

    public ConstructRuneforge(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon, BlockEntity.class);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.getTileEntity() instanceof RunicAnvilTile;
    }

    @Override
    public void tick() {
        super.tick();
        AbstractGolem c = this.getConstructAsEntity();
        if (this.getTileEntity() == null || !(this.getTileEntity() instanceof RunicAnvilTile)) {
            this.exitCode = 1;
            this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.te_missing", new Object[0]));
            c.goalSelector.removeGoal((Goal)this);
        }
        if (this.doMove()) {
            if (this.interactTimer > 0) {
                --this.interactTimer;
            } else {
                this.interactTimer = this.getInteractTime(ConstructCapability.SMITH);
                this.faceBlockPos(this.blockPos);
                this.preInteract();
                this.forgeNext();
            }
        }
    }

    private void forgeNext() {
        RunicAnvilTile te = (RunicAnvilTile)(this.getTileEntity());
        AbstractGolem c = this.getConstructAsEntity();
        if (te != null) {
            Player player = this.createFakePlayer();
            if (player == null) {
                this.exitCode = 1;
                return;
            }
            if (te.getItem(0).isEmpty() || te.getItem(1).isEmpty()) {
                if (!this.isSuccess()) {
                    this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.runeforge_missing_items", new Object[0]));
                }
                this.exitCode = 1;
                return;
            }
            this.construct.getHandWithCapability(ConstructCapability.SMITH).ifPresent(h -> c.swing(h));
            int advanceResponse = te.advanceCrafting(player, 5, true);
            if (advanceResponse == 0) {
                c.level().playSound(null, c.getX(), c.getY(), c.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return;
            }
            if (advanceResponse == 4) {
                c.level().playSound(null, c.getX(), c.getY(), c.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.runeforge_success", this.translate(te.getItem(0))));
                this.exitCode = 0;
            } else if (advanceResponse == 1) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.runeforge_no_recipe", new Object[0]));
                this.exitCode = 1;
            } else if (advanceResponse == 2) {
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.runeforge_low_tier", new Object[0]));
                this.exitCode = 1;
            }
        }
    }

    @Override
    public void start() {
        super.start();
        this.interactTimer = this.getInteractTime(ConstructCapability.SMITH);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.RUNEFORGE);
    }

    @Override
    public ConstructRuneforge duplicate() {
        return new ConstructRuneforge(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructRuneforge copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        return this;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    protected String getPointIdentifier() {
        return "runeforge.point";
    }
}

