# Better Beacons

A powerful, server-sided overhaul for the Minecraft beacon system. Better Beacons allows you to customize the effective range of beacons based on the blocks used in their pyramid, supporting "absurd" ranges across entire dimensions.

## Features

- **Configurable Ranges**: Define custom chunk-based radius for any block type.
- **Absurd Range Support**: Beacons can affect players across massive distances, persisting even when the beacon's chunk is unloaded.
- **Carpet Hiding**: Place a wool carpet directly on top of a beacon to hide its beam while maintaining its effects.
- **Server-Sided**: Works with vanilla clients; no mod installation required for players.
- **Dynamic Updates**: Changes to the configuration are applied immediately, including visual synchronization update messages.

## Commands

- `/betterbeacons list`: Lists all configured beacon blocks and their radii.
- `/betterbeacons set <block> [radius]`: Adds or updates a block's radius (radius is diameter in chunks).
- `/betterbeacons remove <block>`: Removes a block from the custom beacon list.
- `/betterbeacons reload`: Reloads the configuration from disk.

## Installation

### Fabric
1. Install [Fabric Loader](https://fabricmc.net/).
2. Drop the `betterbeacons-fabric-1.21.11.jar` into your `mods` folder.
3. Ensure [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) is present.

### NeoForge
1. Install [NeoForge](https://neoforged.net/).
2. Drop the `betterbeacons-neoforge-1.21.11.jar` into your `mods` folder.

## Configuration

The configuration is stored in `config/betterbeacons.json`. You can modify this file directly or use the in-game commands.

```json
{
  "enable_custom_beacons": true,
  "hide_beam_with_carpet": true,
  "beacon_blocks": {
    "minecraft:iron_block": 3,
    "minecraft:gold_block": 3,
    "minecraft:emerald_block": 5,
    "minecraft:diamond_block": 7,
    "minecraft:netherite_block": 9,
    "minecraft:dirt": 256
  }
}
```

## License

This project is licensed under the Apache-2.0 License.
