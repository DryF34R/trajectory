package com.dryf34r.trajectory.registry;

import java.util.function.Supplier;

import com.dryf34r.trajectory.Trajectory;
import com.dryf34r.trajectory.table.CalculationTableBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Trajectory.MODID);

    @SuppressWarnings("DataFlowIssue") // the data fixer type is unused by mods and may be null
    public static final Supplier<BlockEntityType<CalculationTableBlockEntity>> CALCULATION_TABLE =
            BLOCK_ENTITIES.register("calculation_table", () -> BlockEntityType.Builder
                    .of(CalculationTableBlockEntity::new, ModBlocks.CALCULATION_TABLE.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
