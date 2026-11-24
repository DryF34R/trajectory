package com.dryf34r.trajectory.table;

import java.util.List;
import java.util.Optional;

import com.dryf34r.trajectory.registry.ModItems;
import com.dryf34r.trajectory.spotting.Measurement;
import com.dryf34r.trajectory.spotting.SpotterNotebookItem;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Calculation table. Right-click with a spotter notebook to get a fire solution for the latest
 * measurement (Phase 3: printed to chat; Phase 5 replaces this with a screen).
 */
public class CalculationTableBlock extends BaseEntityBlock {

    public static final MapCodec<CalculationTableBlock> CODEC = simpleCodec(CalculationTableBlock::new);

    public CalculationTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // BaseEntityBlock defaults to INVISIBLE
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CalculationTableBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.SPOTTER_NOTEBOOK.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof CalculationTableBlockEntity table)) {
            return ItemInteractionResult.FAIL;
        }

        Optional<BlockPos> mount = table.getLinkedMount();
        if (mount.isEmpty()) {
            player.sendSystemMessage(Component.translatable("block.trajectory.calculation_table.not_linked")
                    .withStyle(ChatFormatting.RED));
            return ItemInteractionResult.SUCCESS;
        }

        List<Measurement> measurements = SpotterNotebookItem.getMeasurements(stack);
        if (measurements.isEmpty()) {
            player.sendSystemMessage(Component.translatable("block.trajectory.calculation_table.no_measurement")
                    .withStyle(ChatFormatting.RED));
            return ItemInteractionResult.SUCCESS;
        }

        Measurement target = measurements.getLast();
        // TODO(Phase 3, Claude): read the cannon on the mount and print FireSolver's solution.
        player.sendSystemMessage(Component.translatable("block.trajectory.calculation_table.target",
                target.target().getX(), target.target().getY(), target.target().getZ(),
                mount.get().getX(), mount.get().getY(), mount.get().getZ()));
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CalculationTableBlockEntity table) {
            Component message = table.getLinkedMount()
                    .map(m -> Component.translatable("block.trajectory.calculation_table.linked_to",
                            m.getX(), m.getY(), m.getZ()))
                    .orElse(Component.translatable("block.trajectory.calculation_table.not_linked"));
            player.displayClientMessage(message, true);
        }
        return InteractionResult.SUCCESS;
    }
}
