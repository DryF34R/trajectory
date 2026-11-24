package com.dryf34r.trajectory.spotting;

import com.dryf34r.trajectory.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Spotter's rangefinder. Works like a spyglass (zoom and overlay come from vanilla because it extends
 * {@link SpyglassItem}); while scoping, the distance to the looked-at block is shown on the action bar.
 * Releasing it while sneaking writes that block into a spotter notebook in the player's inventory.
 */
public class RangefinderItem extends SpyglassItem {

    public static final double MAX_RANGE = 512.0;
    private static final int DISPLAY_INTERVAL_TICKS = 4;

    public RangefinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide || !(entity instanceof Player player)) return;
        if (remainingUseDuration % DISPLAY_INTERVAL_TICKS != 0) return;

        BlockHitResult hit = lookedAtBlock(player);
        Component message;
        if (hit == null) {
            message = Component.translatable("item.trajectory.rangefinder.no_target").withStyle(ChatFormatting.GRAY);
        } else {
            double distance = player.getEyePosition().distanceTo(hit.getLocation());
            message = Component.translatable("item.trajectory.rangefinder.distance",
                    String.format("%.1f", distance),
                    hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
        }
        player.displayClientMessage(message, true);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        super.releaseUsing(stack, level, entity, timeCharged);
        if (level.isClientSide || !(entity instanceof Player player) || !player.isShiftKeyDown()) return;

        BlockHitResult hit = lookedAtBlock(player);
        if (hit == null) {
            player.displayClientMessage(Component.translatable("item.trajectory.rangefinder.no_target")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        ItemStack notebook = findNotebook(player);
        if (notebook.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.trajectory.rangefinder.no_notebook")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        SpotterNotebookItem.addMeasurement(notebook, new Measurement(hit.getBlockPos(), level.dimension()));
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(Component.translatable("item.trajectory.rangefinder.recorded",
                hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ())
                .withStyle(ChatFormatting.GREEN), true);
    }

    /** The block the player looks at within {@link #MAX_RANGE}, or null for sky / unloaded terrain. */
    private static BlockHitResult lookedAtBlock(Player player) {
        HitResult hit = player.pick(MAX_RANGE, 1.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) return null;
        return (BlockHitResult) hit;
    }

    /** Offhand first, then the inventory. */
    private static ItemStack findNotebook(Player player) {
        if (player.getOffhandItem().is(ModItems.SPOTTER_NOTEBOOK.get())) return player.getOffhandItem();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SPOTTER_NOTEBOOK.get())) return stack;
        }
        return ItemStack.EMPTY;
    }
}
