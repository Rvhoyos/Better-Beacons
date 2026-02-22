package mc.betterbeacons.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Static container for mod configuration values.
 * This class serves as the central data store for the mod's settings during runtime.
 * Persistence is handled by the {@link ConfigManager}.
 */
public class BeaconConfig {
    /** Whether custom beacon effect and range logic is enabled. */
    public static boolean ENABLE_CUSTOM_BEACONS = true;
    
    /** Whether placing a carpet on top of a beacon should hide its beam. */
    public static boolean HIDE_BEAM_WITH_CARPET = true;

    /** 
     * Map of block identifiers to their effective beacon radius (expressed as chunk diameter).
     * For example, a value of 3 means a 3x3 chunk area centered on the beacon.
     */
    public static Map<String, Integer> BEACON_BLOCK_SIZES = new HashMap<>();

    static {
        // Initial defaults
        BEACON_BLOCK_SIZES.put("minecraft:iron_block", 3);
        BEACON_BLOCK_SIZES.put("minecraft:gold_block", 3);
        BEACON_BLOCK_SIZES.put("minecraft:emerald_block", 5);
        BEACON_BLOCK_SIZES.put("minecraft:diamond_block", 7);
        BEACON_BLOCK_SIZES.put("minecraft:netherite_block", 9);
    }

    /**
     * Triggers a reload of the configuration from the JSON file on disk.
     */
    public static void load() {
        ConfigManager.load();
    }

    /**
     * Saves the current in-memory configuration state to the JSON file on disk.
     */
    public static void save() {
        ConfigManager.save();
    }
}
