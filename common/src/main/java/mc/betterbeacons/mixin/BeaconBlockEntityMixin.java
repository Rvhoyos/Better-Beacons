package mc.betterbeacons.mixin;

import mc.betterbeacons.beacons.BeaconInfo;
import mc.betterbeacons.beacons.BeaconManager;
import mc.betterbeacons.config.BeaconConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

/**
 * Mixin to intercept vanilla beacon behavior and inject custom effect
 * management.
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    @Shadow
    int levels;
    @Shadow
    @Nullable
    Holder<MobEffect> primaryPower;
    @Shadow
    @Nullable
    Holder<MobEffect> secondaryPower;

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void cancelVanillaEffects(Level level, BlockPos pos, int levels, @Nullable Holder<MobEffect> primary,
            @Nullable Holder<MobEffect> secondary, CallbackInfo ci) {
        // Intercept and disable vanilla effect application if custom beacons are
        // active.
        if (BeaconConfig.ENABLE_CUSTOM_BEACONS) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private static void tick(Level level, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity,
            CallbackInfo ci) {
        if (!BeaconConfig.ENABLE_CUSTOM_BEACONS || level.isClientSide())
            return;

        // Perform registration check every 4 seconds (80 ticks).
        if (level.getGameTime() % 80L == 0L) {
            BeaconBlockEntityMixin self = (BeaconBlockEntityMixin) (Object) blockEntity;
            updateBeaconRegistration(level, pos, self);
        }
    }

    private static void updateBeaconRegistration(Level level, BlockPos pos, BeaconBlockEntityMixin beacon) {
        if (beacon.levels > 0 && beacon.primaryPower != null) {
            // It has levels! Now we must determine the Radius based on material.
            // Using vanilla levels to determine structure size, then checking config for
            // range.
            int radius = scanForRadius(level, pos, beacon.levels);

            if (radius > -1) { // -1 means invalid structure
                BeaconInfo info = new BeaconInfo(
                        pos,
                        level.dimension(),
                        radius,
                        beacon.primaryPower,
                        beacon.secondaryPower);

                BeaconManager.get().register(pos, level.dimension().identifier().toString(), info);
            } else {
                BeaconManager.get().unregister(pos, level.dimension().identifier().toString());
            }
        } else {
            // Invalid or no power
            BeaconManager.get().unregister(pos, level.dimension().identifier().toString());
        }
    }

    /**
     * Determines the effective chunk radius by scanning the pyramid structure.
     * Uses a "Weakest Link" approach: the final radius is the minimum radius
     * found among all blocks in the valid pyramid structure.
     */
    private static int scanForRadius(Level level, BlockPos pos, int levels) {
        int minRadius = 99;
        boolean foundAny = false;

        for (int i = 1; i <= levels; i++) {
            int y = pos.getY() - i;
            int radius = i;

            for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
                for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    String key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock())
                            .toString();

                    // Lookup radius from config; default to 3 (Iron size) if block is valid but
                    // unlisted.
                    int blockRadius = BeaconConfig.BEACON_BLOCK_SIZES.getOrDefault(key, 3);

                    int chunkRadius = (blockRadius - 1) / 2;
                    if (chunkRadius < minRadius) {
                        minRadius = chunkRadius;
                    }
                    foundAny = true;
                }
            }
        }
        return foundAny ? Math.max(0, minRadius) : 0;
    }
}
