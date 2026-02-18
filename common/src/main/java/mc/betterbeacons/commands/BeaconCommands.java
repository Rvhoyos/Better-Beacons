package mc.betterbeacons.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mc.betterbeacons.config.BeaconConfig;
import mc.betterbeacons.config.ConfigManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.concurrent.CompletableFuture;

public class BeaconCommands {

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                        CommandBuildContext buildContext) {
                dispatcher.register(Commands.literal("betterbeacons")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("reload")
                                                .executes(c -> {
                                                        BeaconConfig.load();
                                                        c.getSource().sendSuccess(() -> Component
                                                                        .literal("Better Beacons config reloaded!"),
                                                                        true);
                                                        return 1;
                                                }))
                                .then(Commands.literal("list")
                                                .executes(c -> {
                                                        if (BeaconConfig.BEACON_BLOCK_SIZES.isEmpty()) {
                                                                c.getSource().sendSuccess(() -> Component.literal(
                                                                                "No beacon blocks configured."), false);
                                                                return 1;
                                                        }
                                                        MutableComponent list = Component
                                                                        .literal("Configured Beacon Blocks:\n");
                                                        BeaconConfig.BEACON_BLOCK_SIZES.forEach((id, radius) -> {
                                                                list.append(Component.literal("- " + id + ": " + radius
                                                                                + " chunks\n"));
                                                        });
                                                        c.getSource().sendSuccess(() -> list, false);
                                                        return 1;
                                                }))
                                .then(Commands.literal("set")
                                                .then(Commands.argument("block", IdentifierArgument.id())
                                                                .suggests(BeaconCommands::suggestBlocks)
                                                                .executes(c -> setBlock(c.getSource(),
                                                                                IdentifierArgument
                                                                                                .getId(c, "block")
                                                                                                .toString(),
                                                                                3))
                                                                .then(Commands.argument("radius",
                                                                                IntegerArgumentType.integer(1, 100))
                                                                                .executes(c -> setBlock(c.getSource(),
                                                                                                IdentifierArgument
                                                                                                                .getId(c, "block")
                                                                                                                .toString(),
                                                                                                IntegerArgumentType
                                                                                                                .getInteger(c, "radius"))))))
                                .then(Commands.literal("remove")
                                                .then(Commands.argument("block", IdentifierArgument.id())
                                                                .suggests(BeaconCommands::suggestConfiguredBlocks)
                                                                .executes(c -> removeBlock(c.getSource(),
                                                                                IdentifierArgument
                                                                                                .getId(c, "block")
                                                                                                .toString())))));
        }

        private static CompletableFuture<Suggestions> suggestBlocks(CommandContext<CommandSourceStack> context,
                        SuggestionsBuilder builder) {
                return SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK.keySet(), builder);
        }

        private static CompletableFuture<Suggestions> suggestConfiguredBlocks(
                        CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
                return SharedSuggestionProvider.suggest(BeaconConfig.BEACON_BLOCK_SIZES.keySet(), builder);
        }

        private static int setBlock(CommandSourceStack source, String blockId, int radius) {
                BeaconConfig.BEACON_BLOCK_SIZES.put(blockId, radius);
                ConfigManager.save();
                source.sendSuccess(() -> Component.literal("Set " + blockId + " radius to " + radius), true);
                return 1;
        }

        private static int removeBlock(CommandSourceStack source, String blockId) {
                if (BeaconConfig.BEACON_BLOCK_SIZES.remove(blockId) != null) {
                        ConfigManager.save();
                        source.sendSuccess(
                                        () -> Component.literal("Removed " + blockId + " from Better Beacons config"),
                                        true);
                        return 1;
                } else {
                        source.sendFailure(
                                        Component.literal("Block " + blockId + " is not in the Better Beacons config"));
                        return 0;
                }
        }
}
