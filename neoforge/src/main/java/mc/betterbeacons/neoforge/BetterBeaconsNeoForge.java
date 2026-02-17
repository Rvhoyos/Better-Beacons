package mc.betterbeacons.neoforge;

import net.neoforged.fml.common.Mod;
import mc.betterbeacons.config.BeaconConfig;

/**
 * NeoForge mod entrypoint. Triggers shared setup via {@link BeaconConfig}.
 */
@Mod("betterbeacons")
public final class BetterBeaconsNeoForge {
    public BetterBeaconsNeoForge() {
        // Run our common setup.
        BeaconConfig.load();

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS
                .addListener((net.neoforged.neoforge.event.RegisterCommandsEvent event) -> {
                    mc.betterbeacons.commands.BeaconCommands.register(event.getDispatcher(), event.getBuildContext());
                });
    }
}
