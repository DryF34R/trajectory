package com.dryf34r.trajectory.registry;

import java.util.List;
import java.util.function.Supplier;

import com.dryf34r.trajectory.Trajectory;
import com.dryf34r.trajectory.spotting.Measurement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {

    /** How many measurements a notebook keeps; the oldest is dropped first. */
    public static final int MAX_MEASUREMENTS = 32;

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Trajectory.MODID);

    /** Spotter notebook contents, oldest first. */
    public static final Supplier<DataComponentType<List<Measurement>>> MEASUREMENTS =
            COMPONENTS.registerComponentType("measurements", builder -> builder
                    .persistent(Measurement.CODEC.listOf())
                    .networkSynchronized(Measurement.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_MEASUREMENTS))));

    /** Cannon mount a calculation table (item or block) is linked to. */
    public static final Supplier<DataComponentType<BlockPos>> LINKED_MOUNT =
            COMPONENTS.registerComponentType("linked_mount", builder -> builder
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));

    private ModDataComponents() {
    }
}
