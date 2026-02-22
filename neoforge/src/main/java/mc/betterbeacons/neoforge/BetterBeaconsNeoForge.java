package mc.betterbeacons.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import mc.betterbeacons.config.BeaconConfig;
import mc.betterbeacons.config.ConfigManager;

/**
 * NeoForge mod entrypoint.
 * Handles platform-specific setup and triggers shared configuration loading.
 */
@Mod("betterbeacons")
public final class BetterBeaconsNeoForge {
    public BetterBeaconsNeoForge() {
        // Initialize common config path for NeoForge
        ConfigManager.configDir = FMLPaths.CONFIGDIR.get();
        // Run our common setup.
        BeaconConfig.load();

        // Register commands using NeoForge event bus
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS
                .addListener((net.neoforged.neoforge.event.RegisterCommandsEvent event) -> {
                    mc.betterbeacons.commands.BeaconCommands.register(event.getDispatcher(), event.getBuildContext());
                });
    }
}
