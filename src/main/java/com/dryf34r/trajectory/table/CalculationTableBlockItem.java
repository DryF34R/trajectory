package com.dryf34r.trajectory.table;

import java.util.List;

import com.dryf34r.trajectory.registry.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

/**
 * Sneak + right-click a CBC cannon mount with the table item to link it; the link is stored on the
 * item and copied into the block entity when the table is placed.
 */
public class CalculationTableBlockItem extends BlockItem {

    public CalculationTableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos clicked = context.getClickedPos();
        if (player != null && player.isShiftKeyDown()
                && context.getLevel().getBlockEntity(clicked) instanceof CannonMountBlockEntity) {
            if (!context.getLevel().isClientSide) {
                context.getItemInHand().set(ModDataComponents.LINKED_MOUNT.get(), clicked.immutable());
                player.displayClientMessage(Component.translatable("block.trajectory.calculation_table.linked",
                        clicked.getX(), clicked.getY(), clicked.getZ()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        BlockPos mount = stack.get(ModDataComponents.LINKED_MOUNT.get());
        if (mount == null) {
            tooltip.add(Component.translatable("block.trajectory.calculation_table.not_linked")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("block.trajectory.calculation_table.linked_to",
                    mount.getX(), mount.getY(), mount.getZ()).withStyle(ChatFormatting.GRAY));
        }
    }
}
