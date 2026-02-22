package mc.betterbeacons.beacons;

import mc.betterbeacons.config.BeaconConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active beacons across all dimensions.
 * This class handles registration, validation, and the logic for applying
 * custom effects based on configurable chunk-based ranges.
 */
public class BeaconManager {
    private static final BeaconManager INSTANCE = new BeaconManager();
    
    /** Map of active beacons, keyed by dimension ID then by block position. */
    private final Map<String, Map<BlockPos, BeaconInfo>> activeBeacons = new ConcurrentHashMap<>();

    private BeaconManager() {
    }

    /**
     * Gets the singleton instance of the BeaconManager.
     * 
     * @return The singleton instance.
     */
    public static BeaconManager get() {
        return INSTANCE;
    }

    /**
     * Updates or unregisters a beacon based on its current state.
     * This method is called during the beacon's tick to ensure the registration
     * stays in sync with the physical structure.
     * 
     * @param level     The level containing the beacon.
     * @param pos       The position of the beacon.
     * @param levels    The number of pyramid levels.
     * @param primary   The primary effect.
     * @param secondary The secondary effect.
     */
    public void updateBeaconRegistration(Level level, BlockPos pos, int levels, 
                                        @Nullable Holder<MobEffect> primary, 
                                        @Nullable Holder<MobEffect> secondary) {
        if (level.isClientSide()) return;

        if (levels > 0 && primary != null) {
            ScanResult result = scanForRadius(level, pos, levels);

            if (result.radius() > -1) {
                BeaconInfo info = new BeaconInfo(
                        pos,
                        level.dimension(),
                        result.radius(),
                        result.weakestBlockId(),
                        primary,
                        secondary);

                register(pos, level.dimension().identifier().toString(), info);
            } else {
                unregister(pos, level.dimension().identifier().toString());
            }
        } else {
            unregister(pos, level.dimension().identifier().toString());
        }
    }

    private void register(BlockPos pos, String dimensionId, BeaconInfo info) {
        activeBeacons.computeIfAbsent(dimensionId, k -> new ConcurrentHashMap<>()).put(pos, info);
    }

    /**
     * Unregisters a beacon from the management map.
     * 
     * @param pos         The position of the beacon.
     * @param dimensionId The string identifier of the dimension.
     */
    public void unregister(BlockPos pos, String dimensionId) {
        if (activeBeacons.containsKey(dimensionId)) {
            activeBeacons.get(dimensionId).remove(pos);
        }
    }

    /**
     * Clears all registered beacons from all dimensions.
     */
    public void clearAll() {
        activeBeacons.clear();
    }

    /**
     * Scans the beacon pyramid to determine the weakest block type and its associated radius.
     */
    private ScanResult scanForRadius(Level level, BlockPos pos, int levels) {
        int minRadius = 99;
        String weakestBlockId = null;
        boolean foundAny = false;

        for (int i = 1; i <= levels; i++) {
            int y = pos.getY() - i;
            int radius = i;

            for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
                for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

                    int blockRadius = BeaconConfig.BEACON_BLOCK_SIZES.getOrDefault(key, 3);
                    int chunkRadius = (blockRadius - 1) / 2;
                    if (chunkRadius < minRadius) {
                        minRadius = chunkRadius;
                        weakestBlockId = key;
                    }
                    foundAny = true;
                }
            }
        }
        return new ScanResult(foundAny ? Math.max(0, minRadius) : 0, weakestBlockId);
    }

    /**
     * Main tick loop executed on the server.
     * Validates that registered beacons still exist and applies effects to all players.
     * 
     * @param server The Minecraft server instance.
     */
    public void tick(MinecraftServer server) {
        if (!BeaconConfig.ENABLE_CUSTOM_BEACONS)
            return;
        if (server.getTickCount() % 80 != 0)
            return;

        String[] dimIds = activeBeacons.keySet().toArray(new String[0]);

        for (String dimId : dimIds) {
            Map<BlockPos, BeaconInfo> beacons = activeBeacons.get(dimId);
            if (beacons == null || beacons.isEmpty())
                continue;

            net.minecraft.server.level.ServerLevel level = null;
            for (net.minecraft.server.level.ServerLevel world : server.getAllLevels()) {
                if (world.dimension().identifier().toString().equals(dimId)) {
                    level = world;
                    break;
                }
            }

            if (level == null)
                continue;

            java.util.Iterator<Map.Entry<BlockPos, BeaconInfo>> iterator = beacons.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, BeaconInfo> entry = iterator.next();
                BlockPos pos = entry.getKey();

                if (level.isLoaded(pos)) {
                    net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof net.minecraft.world.level.block.entity.BeaconBlockEntity)) {
                        iterator.remove();
                    }
                }
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyEffectsToPlayer(player);
        }
    }

    /**
     * Applies configured beacon effects to a player if they are within the chunk-based radius.
     */
    private void applyEffectsToPlayer(ServerPlayer player) {
        String dimId = player.level().dimension().identifier().toString();
        if (!activeBeacons.containsKey(dimId))
            return;

        ChunkPos playerChunk = player.chunkPosition();

        for (BeaconInfo beacon : activeBeacons.get(dimId).values()) {
            ChunkPos beaconChunk = new ChunkPos(beacon.pos());
            
            int radius = beacon.radius();
            if (beacon.weakestBlockId() != null) {
                Integer configSize = BeaconConfig.BEACON_BLOCK_SIZES.get(beacon.weakestBlockId());
                if (configSize != null) {
                    radius = Math.max(0, (configSize - 1) / 2);
                } else {
                    radius = -1; 
                }
            }

            if (radius >= 0 && 
                    Math.abs(playerChunk.x - beaconChunk.x) <= radius &&
                    Math.abs(playerChunk.z - beaconChunk.z) <= radius) {

                int duration = 300;

                if (beacon.primary() != null) {
                    int primaryAmp = (radius >= 4 && beacon.primary().equals(beacon.secondary())) ? 1 : 0;
                    player.addEffect(new MobEffectInstance(beacon.primary(), duration, primaryAmp, true, true));
                }

                if (beacon.secondary() != null && !beacon.secondary().equals(beacon.primary())) {
                    player.addEffect(new MobEffectInstance(beacon.secondary(), duration, 0, true, true));
                }
            }
        }
    }

    /**
     * Internal result record for pyramid scanning.
     */
    private record ScanResult(int radius, String weakestBlockId) {}
}
