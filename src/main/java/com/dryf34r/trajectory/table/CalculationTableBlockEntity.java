package com.dryf34r.trajectory.table;

import java.util.Optional;

import com.dryf34r.trajectory.registry.ModBlockEntities;
import com.dryf34r.trajectory.registry.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Remembers which cannon mount this table computes solutions for. */
public class CalculationTableBlockEntity extends BlockEntity {

    private static final String TAG_LINKED_MOUNT = "LinkedMount";

    private BlockPos linkedMount;

    public CalculationTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CALCULATION_TABLE.get(), pos, state);
    }

    public Optional<BlockPos> getLinkedMount() {
        return Optional.ofNullable(linkedMount);
    }

    public void setLinkedMount(BlockPos pos) {
        linkedMount = pos;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (linkedMount != null) tag.put(TAG_LINKED_MOUNT, NbtUtils.writeBlockPos(linkedMount));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        linkedMount = NbtUtils.readBlockPos(tag, TAG_LINKED_MOUNT).orElse(null);
    }

    // Item <-> block entity: the link travels with the item when the table is placed or broken.

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        linkedMount = input.get(ModDataComponents.LINKED_MOUNT.get());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (linkedMount != null) components.set(ModDataComponents.LINKED_MOUNT.get(), linkedMount);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(TAG_LINKED_MOUNT);
    }
}
