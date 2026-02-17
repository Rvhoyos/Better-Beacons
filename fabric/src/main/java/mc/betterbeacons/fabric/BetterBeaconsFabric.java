package mc.betterbeacons.fabric;

import net.fabricmc.api.ModInitializer;
import mc.betterbeacons.config.BeaconConfig;

/**
 * Fabric mod initializer. Triggers shared setup via {@link BeaconConfig}.
 */
public final class BetterBeaconsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Run our common setup.
        BeaconConfig.load();

        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT
                .register((dispatcher, registrySelection, selection) -> {
                    mc.betterbeacons.commands.BeaconCommands.register(dispatcher, registrySelection);
                });
    }
}
