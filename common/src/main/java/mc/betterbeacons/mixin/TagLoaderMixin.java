package mc.betterbeacons.mixin;

import mc.betterbeacons.config.BeaconConfig;
import net.minecraft.resources.Identifier;
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

/**
 * Mixin to dynamically inject custom beacon base blocks into the 
 * {@code minecraft:beacon_base_blocks} tag during tag loading.
 */
@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Shadow
    @Final
    private String directory;

    /**
     * Intercepts the tag loading process to append configured custom blocks
     * to the beacon base blocks tag.
     * Overrides the whole tag to ensure "remove" works even for vanilla blocks.
     */
    @Inject(method = "load", at = @At("RETURN"))
    private void injectCustomBeaconTags(
            CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir) {
        
        // In 1.21.x, the directory for block tags is "tags/block"
        if ("tags/block".equals(directory)) {
            Map<Identifier, List<TagLoader.EntryWithSource>> map = cir.getReturnValue();
            Identifier beaconTag = Identifier.withDefaultNamespace("beacon_base_blocks");

            List<TagLoader.EntryWithSource> newEntries = new ArrayList<>();

            // Always ensure config is loaded
            BeaconConfig.load();

            for (String blockId : BeaconConfig.BEACON_BLOCK_SIZES.keySet()) {
                Identifier loc = Identifier.tryParse(blockId);
                if (loc != null) {
                    // Inject as an OPTIONAL element entry. 
                    // This prevents "missing reference" errors during initial bootstrap 
                    // when some registries might not be fully ready.
                    newEntries.add(new TagLoader.EntryWithSource(TagEntry.optionalElement(loc), "Better Beacons"));
                }
            }
            
            // Override the tag with our config-controlled list
            map.put(beaconTag, newEntries);
        }
    }
}
