package com.dryf34r.trajectory.spotting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * One target position measured with the rangefinder and written into the spotter notebook.
 * Phase 3 stores the exact block; Phase 4 adds measurement error.
 *
 * @param target    the block the spotter was looking at
 * @param dimension where it was measured (a table only solves for its own dimension)
 */
public record Measurement(BlockPos target, ResourceKey<Level> dimension) {

    public static final Codec<Measurement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("target").forGetter(Measurement::target),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Measurement::dimension)
    ).apply(instance, Measurement::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Measurement> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Measurement::target,
            ResourceKey.streamCodec(Registries.DIMENSION), Measurement::dimension,
            Measurement::new);
}
