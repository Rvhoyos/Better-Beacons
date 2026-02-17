package mc.betterbeacons.mixin;

import mc.betterbeacons.config.BeaconConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Shadow
    @Final
    private String directory;

    @Inject(method = "load", at = @At("RETURN"))
    private void injectCustomBeaconTags(
            CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        // In 1.21.x, the directory for block tags is "tags/block"
        if ("tags/block".equals(directory)) {
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = cir.getReturnValue();
            ResourceLocation beaconTag = ResourceLocation.withDefaultNamespace("beacon_base_blocks");

            List<TagLoader.EntryWithSource> entries = map.computeIfAbsent(beaconTag, k -> new ArrayList<>());

            for (String blockId : BeaconConfig.BEACON_BLOCK_SIZES.keySet()) {
                ResourceLocation loc = ResourceLocation.tryParse(blockId);
                if (loc != null) {
                    // Inject as a standard element entry
                    entries.add(new TagLoader.EntryWithSource(TagEntry.element(loc), "Better Beacons"));
                }
            }
        }
    }
}
