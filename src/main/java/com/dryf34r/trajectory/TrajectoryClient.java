package com.dryf34r.trajectory;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Trajectory.MODID, dist = Dist.CLIENT)
public class TrajectoryClient {
    public TrajectoryClient(ModContainer container) {
        // Lets NeoForge build a config screen from our ModConfigSpec
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
