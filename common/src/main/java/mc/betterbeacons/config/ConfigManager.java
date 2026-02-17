package mc.betterbeacons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles JSON configuration I/O for the Better Beacons mod.
 * Manages the betterbeacons.json file, including default generation and
 * structural validation.
 */
public final class ConfigManager {
    private static final String FILE_NAME = "betterbeacons.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ConfigManager() {
    }

    public static Path path() {
        return Platform.getConfigFolder().resolve(FILE_NAME);
    }

    /**
     * Loads the configuration from disk, creating it with default values if it
     * doesn't exist.
     * Also performs a "lazy repair" by adding missing fields and removing invalid
     * data.
     */
    public static JsonObject readOrCreate() {
        Path p = path();
        try {
            Files.createDirectories(p.getParent());
            if (!Files.exists(p) || Files.size(p) == 0) {
                JsonObject root = defaultJson();
                Files.writeString(p, GSON.toJson(root), StandardCharsets.UTF_8);
                return root;
            }
            String s = Files.readString(p, StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(s, JsonObject.class);
            if (obj == null)
                obj = defaultJson();
            boolean dirty = false;

            if (!obj.has("enable_custom_beacons")) {
                obj.addProperty("enable_custom_beacons", true);
                dirty = true;
            }
            if (!obj.has("beacon_blocks") || !obj.get("beacon_blocks").isJsonObject()) {
                obj.add("beacon_blocks", defaultBeaconBlocksJson());
                dirty = true;
            }

            if (dirty) {
                Files.writeString(p, GSON.toJson(obj), StandardCharsets.UTF_8);
            }
            return obj;
        } catch (JsonSyntaxException e) {
            // Malformed JSON — throw a descriptive error for callers to display
            throw new ConfigParseException(
                    "Malformed JSON in " + FILE_NAME + ": " + e.getMessage(), e);
        } catch (IOException e) {
            // Fall back to default in-memory config when IO fails
            return defaultJson();
        }
    }

    /**
     * Exception thrown when the betterbeacons.json file contains syntax errors
     * that prevent standard GSON parsing.
     */
    public static class ConfigParseException extends RuntimeException {
        public ConfigParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static JsonObject defaultJson() {
        JsonObject root = new JsonObject();
        root.addProperty("enable_custom_beacons", true);
        root.add("beacon_blocks", defaultBeaconBlocksJson());
        return root;
    }

    /**
     * Returns a JSON object containing the default block-to-radius mappings.
     * Radii are expressed in "diameter of chunks" (e.g. 3 = 3x3 chunks).
     */
    static JsonObject defaultBeaconBlocksJson() {
        JsonObject bs = new JsonObject();
        bs.addProperty("minecraft:iron_block", 3);
        bs.addProperty("minecraft:gold_block", 3);
        bs.addProperty("minecraft:emerald_block", 5);
        bs.addProperty("minecraft:diamond_block", 7);
        bs.addProperty("minecraft:netherite_block", 9);
        return bs;
    }

    public static void save() {
        JsonObject root = defaultJson();

        // Update root with current values
        root.addProperty("enable_custom_beacons", BeaconConfig.ENABLE_CUSTOM_BEACONS);

        JsonObject bs = new JsonObject();
        for (var entry : BeaconConfig.BEACON_BLOCK_SIZES.entrySet()) {
            bs.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("beacon_blocks", bs);

        try {
            Path p = path();
            Files.writeString(p, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the config file and reloads defaults.
     */
    public static void resetToFactory() {
        try {
            Files.deleteIfExists(path());
            BeaconConfig.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
