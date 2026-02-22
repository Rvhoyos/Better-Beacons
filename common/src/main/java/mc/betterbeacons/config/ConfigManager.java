package mc.betterbeacons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Handles JSON configuration I/O for the Better Beacons mod.
 * This class is responsible for synchronizing the {@link BeaconConfig} with the 
 * physical configuration file on disk.
 */
public final class ConfigManager {
    private static final String FILE_NAME = "betterbeacons.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** The platform-specific configuration directory. This must be initialized by the loader during startup. */
    public static Path configDir;

    private ConfigManager() {
    }

    /**
     * Gets the full path to the configuration file.
     * 
     * @return The path to betterbeacons.json.
     */
    public static Path path() {
        if (configDir == null) {
            return Path.of("config").resolve(FILE_NAME);
        }
        return configDir.resolve(FILE_NAME);
    }

    /**
     * Loads the configuration from disk and populates the static fields in {@link BeaconConfig}.
     * If the file does not exist, a default one will be created.
     */
    public static void load() {
        Path p = path();
        try {
            if (!Files.exists(p)) {
                save();
                return;
            }
            String s = Files.readString(p, StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(s, JsonObject.class);
            if (obj == null) {
                save();
                return;
            }

            if (obj.has("enable_custom_beacons")) {
                BeaconConfig.ENABLE_CUSTOM_BEACONS = obj.get("enable_custom_beacons").getAsBoolean();
            }
            
            if (obj.has("hide_beam_with_carpet")) {
                BeaconConfig.HIDE_BEAM_WITH_CARPET = obj.get("hide_beam_with_carpet").getAsBoolean();
            }

            if (obj.has("beacon_blocks") && obj.get("beacon_blocks").isJsonObject()) {
                JsonObject blocks = obj.getAsJsonObject("beacon_blocks");
                BeaconConfig.BEACON_BLOCK_SIZES.clear();
                for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                    BeaconConfig.BEACON_BLOCK_SIZES.put(entry.getKey(), entry.getValue().getAsInt());
                }
            } else if (BeaconConfig.BEACON_BLOCK_SIZES.isEmpty()) {
                applyDefaults();
            }

        } catch (Exception e) {
            System.err.println("Failed to load Better Beacons config: " + e.getMessage());
            applyDefaults();
        }
    }

    private static void applyDefaults() {
        BeaconConfig.BEACON_BLOCK_SIZES.put("minecraft:iron_block", 3);
        BeaconConfig.BEACON_BLOCK_SIZES.put("minecraft:gold_block", 3);
        BeaconConfig.BEACON_BLOCK_SIZES.put("minecraft:emerald_block", 5);
        BeaconConfig.BEACON_BLOCK_SIZES.put("minecraft:diamond_block", 7);
        BeaconConfig.BEACON_BLOCK_SIZES.put("minecraft:netherite_block", 9);
    }

    /**
     * Saves the current static state of {@link BeaconConfig} to the disk.
     */
    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("enable_custom_beacons", BeaconConfig.ENABLE_CUSTOM_BEACONS);
        root.addProperty("hide_beam_with_carpet", BeaconConfig.HIDE_BEAM_WITH_CARPET);

        JsonObject bs = new JsonObject();
        for (var entry : BeaconConfig.BEACON_BLOCK_SIZES.entrySet()) {
            bs.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("beacon_blocks", bs);

        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
