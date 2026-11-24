package com.dryf34r.trajectory.registry;

import com.dryf34r.trajectory.Trajectory;
import com.dryf34r.trajectory.spotting.RangefinderItem;
import com.dryf34r.trajectory.spotting.SpotterNotebookItem;
import com.dryf34r.trajectory.table.CalculationTableBlockItem;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Trajectory.MODID);

    public static final DeferredItem<RangefinderItem> RANGEFINDER = ITEMS.registerItem(
            "rangefinder", RangefinderItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpotterNotebookItem> SPOTTER_NOTEBOOK = ITEMS.registerItem(
            "spotter_notebook", SpotterNotebookItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<CalculationTableBlockItem> CALCULATION_TABLE = ITEMS.register(
            "calculation_table", () -> new CalculationTableBlockItem(ModBlocks.CALCULATION_TABLE.get(), new Item.Properties()));

    private ModItems() {
    }
}
