package mc.betterbeacons.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import mc.betterbeacons.config.BeaconConfig;
import mc.betterbeacons.config.ConfigManager;

/**
 * Fabric mod initializer. 
 * Handles platform-specific setup and triggers shared configuration loading.
 */
public final class BetterBeaconsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Initialize common config path for Fabric
        ConfigManager.configDir = FabricLoader.getInstance().getConfigDir();
        // Run our common setup.
        BeaconConfig.load();

        // Register commands using Fabric API
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT
                .register((dispatcher, registrySelection, selection) -> {
                    mc.betterbeacons.commands.BeaconCommands.register(dispatcher, registrySelection);
                });
    }
}
