 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

public class ConstructFish
extends ConstructAITask<ConstructFish> {
    protected Direction side;
    protected BlockPos blockPos;
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.FISH};
    private int fish_counter = -1;
    private int end_wait_time = -1;

    public ConstructFish(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        Level world = this.construct.asEntity().level();
        if (!world.isLoaded(this.blockPos)) {
            this.exitCode = 1;
            return;
        }
        if (this.end_wait_time > 0) {
            if (--this.end_wait_time <= 0) {
                this.construct.resetActions();
                this.exitCode = 0;
            }
            return;
        }
        if (!this.construct.isFishing()) {
            this.construct.getDiagnostics().pushTaskUpdate(this.getId(), this.guiIcon, IConstructDiagnostics.Status.RUNNING, Vec3.atCenterOf((Vec3i)this.blockPos));
            this.setMoveTarget(this.blockPos);
            if (this.doMove(8.0f)) {
                if (world.getBlockState(this.blockPos).getBlock() != Blocks.WATER) {
                    this.blockPos = this.blockPos.offset(this.side.getNormal());
                    if (world.getBlockState(this.blockPos).getBlock() != Blocks.WATER) {
                        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.fish_not_water", new Object[0]));
                        this.forceFail();
                        return;
                    }
                }
                this.construct.setFishing(this.blockPos);
            }
        } else {
            this.setMoveTarget(this.blockPos);
            this.faceBlockPos(this.blockPos);
            this.doMove(8.0f);
            if (--this.fish_counter <= 0) {
                AbstractGolem c = this.construct.asEntity();
                this.construct.stopFishing();
                List<ItemStack> fishingLoot = this.getLootRoll(BuiltInLootTables.FISHING.location(), new ItemStack((ItemLike)Items.FISHING_ROD), LootContextParamSets.FISHING);
                for (ItemStack itemstack : fishingLoot) {
                    Vec3 center = Vec3.atCenterOf((Vec3i)this.blockPos).add(0.0, 0.5, 0.0);
                    ItemEntity itementity = new ItemEntity(c.level(), center.x, center.y, center.z, itemstack);
                    double d0 = c.getX() - center.x;
                    double d1 = c.getY() - center.y;
                    double d2 = c.getZ() - center.z;
                    itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.1);
                    c.level().addFreshEntity((Entity)itementity);
                }
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.fish_success", new Object[0]));
                this.end_wait_time = 20;
            }
        }
    }

    @Override
    public void onTaskSet() {
        super.onTaskSet();
        this.end_wait_time = -1;
        this.fish_counter = this.getInteractTime(ConstructCapability.FISH, 400);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.FISH);
    }

    @Override
    public ConstructFish duplicate() {
        return new ConstructFish(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructFish copyFrom(ConstructAITask<?> other) {
        if (other instanceof ConstructFish) {
            this.blockPos = ((ConstructFish)other).blockPos;
            this.side = ((ConstructFish)other).side;
        }
        return this;
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        if (this.blockPos != null) {
            nbt.put("blockPos", (Tag)NbtUtils.writeBlockPos((BlockPos)this.blockPos));
        }
        if (this.side != null) {
            nbt.putInt("direction", this.side.get3DDataValue());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("blockPos")) {
            this.blockPos = NbtUtils.readBlockPos(nbt, "blockPos").orElse(null);
        }
        if (nbt.contains("direction")) {
            this.side = Direction.from3DDataValue((int)nbt.getInt("direction"));
        }
    }

    @Override
    public void inflateParameters() {
        this.getParameter("fish.point").ifPresent(param -> {
            if (param instanceof ConstructTaskPointParameter) {
                this.blockPos = ((ConstructTaskPointParameter)param).getPosition();
                this.side = ((ConstructTaskPointParameter)param).getDirection();
            }
        });
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("fish.point"));
        return parameters;
    }

    @Override
    public boolean isFullyConfigured() {
        return this.blockPos != null;
    }

    public static ConstructCapability[] getRequiredcaps() {
        return requiredCaps;
    }
}

