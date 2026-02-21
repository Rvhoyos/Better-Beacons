package mc.betterbeacons.config;

import com.google.gson.JsonObject;

/**
 * In-memory storage for the current mod configuration.
 * Values are populated from JSON via {@link ConfigManager}.
 */
public final class BeaconConfig {
    public static boolean ENABLE_CUSTOM_BEACONS = true;
    public static boolean HIDE_BEAM_WITH_CARPET = true;
    public static java.util.Map<String, Integer> BEACON_BLOCK_SIZES = new java.util.HashMap<>();

    private BeaconConfig() {
    }

    /**
     * Loads settings from the configuration file into memory.
     */
    public static void load() {
        JsonObject root = ConfigManager.readOrCreate();

        if (root.has("enable_custom_beacons")) {
            ENABLE_CUSTOM_BEACONS = root.get("enable_custom_beacons").getAsBoolean();
        }

        if (root.has("hide_beam_with_carpet")) {
            HIDE_BEAM_WITH_CARPET = root.get("hide_beam_with_carpet").getAsBoolean();
        }

        if (root.has("beacon_blocks") && root.get("beacon_blocks").isJsonObject()) {
            BEACON_BLOCK_SIZES.clear();
            JsonObject bs = root.getAsJsonObject("beacon_blocks");
            for (String key : bs.keySet()) {
                BEACON_BLOCK_SIZES.put(key, bs.get(key).getAsInt());
            }
        }
    }
}
