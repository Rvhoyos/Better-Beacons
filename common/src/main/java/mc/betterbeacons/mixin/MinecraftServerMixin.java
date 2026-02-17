package mc.betterbeacons.mixin;

import mc.betterbeacons.beacons.BeaconManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * Injects the {@link BeaconManager} tick into the server tick loop.
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        BeaconManager.get().tick((MinecraftServer) (Object) this);
    }
}
