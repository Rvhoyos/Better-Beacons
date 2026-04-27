package mc.betterbeacons.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import mc.betterbeacons.config.BeaconConfig;
import mc.betterbeacons.config.ConfigManager;
import mc.betterbeacons.commands.BeaconCommands;

/**
 * Forge mod entrypoint.
 * Handles platform-specific setup and triggers shared configuration loading.
 */
@Mod("betterbeacons")
public final class BetterBeaconsForge {
    public BetterBeaconsForge() {
        // Initialize common config path for Forge
        ConfigManager.configDir = FMLPaths.CONFIGDIR.get();
        // Run our common setup.
        BeaconConfig.load();

        // Register commands using Forge event bus
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            BeaconCommands.register(event.getDispatcher(), event.getBuildContext());
        });
    }
}
