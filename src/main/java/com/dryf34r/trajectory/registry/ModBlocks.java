package com.dryf34r.trajectory.registry;

import com.dryf34r.trajectory.Trajectory;
import com.dryf34r.trajectory.table.CalculationTableBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Trajectory.MODID);

    public static final DeferredBlock<CalculationTableBlock> CALCULATION_TABLE = BLOCKS.registerBlock(
            "calculation_table", CalculationTableBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD));

    private ModBlocks() {
    }
}
