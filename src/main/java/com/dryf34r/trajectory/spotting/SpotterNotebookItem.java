package com.dryf34r.trajectory.spotting;

import java.util.ArrayList;
import java.util.List;

import com.dryf34r.trajectory.registry.ModDataComponents;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Holds the spotter's measurements ({@link ModDataComponents#MEASUREMENTS}).
 * The tooltip ({@code appendHoverText}) is left for Arda to write.
 */
public class SpotterNotebookItem extends Item {

    public SpotterNotebookItem(Properties properties) {
        super(properties);
    }

    public static List<Measurement> getMeasurements(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MEASUREMENTS.get(), List.of());
    }

    /** Appends a measurement, dropping the oldest one when the notebook is full. */
    public static void addMeasurement(ItemStack stack, Measurement measurement) {
        List<Measurement> list = new ArrayList<>(getMeasurements(stack));
        list.add(measurement);
        while (list.size() > ModDataComponents.MAX_MEASUREMENTS) {
            list.removeFirst();
        }
        stack.set(ModDataComponents.MEASUREMENTS.get(), List.copyOf(list));
    }
}
