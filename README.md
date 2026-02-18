# Better Beacons
**A powerful, flexible overhaul for Minecraft beacons.**

Better Beacons makes beacons more useful by giving them massive, chunk based ranges and letting you use almost any block as a base. Effects stick with you even if the beacon unloads, making it the perfect tool for large bases and survival projects.

## Key Features

- **Custom Bases**: Use any block type you want. From Iron to Netherite, or even custom modded blocks.
- **Massive Range**: Beacons now cover entire square areas of chunks. No more losing your haste effect because you walked 50 blocks away.
- **Smart Material Logic**: When you mix blocks in a pyramid, the beacon's range is decided by the weakest block used. Build consistent layers for the best results.
- **Persistent Effects**: Your beacon powers stay active as long as you're in range, even if the server unloads the beacon's area to save memory.
- **Server Side Only**: No client mod needed. Your friends can join with a vanilla game and everything will work perfectly.

---

## Player Guide

### How Ranges Work
Instead of blocks, this mod counts Chunks (16x16 areas).
- **Iron/Gold**: Covers a 3x3 chunk area.
- **Emerald**: Covers a 5x5 chunk area.
- **Diamond**: Covers a 7x7 chunk area.
- **Netherite**: Covers a massive 9x9 chunk area.

### Building Your Beacon
You build the pyramid just like in vanilla Minecraft. Thanks to our dynamic tag engine, any block you configure as a beacon base will automatically show a beam and work perfectly for all players, even those on vanilla clients.

---

## Admin Commands
Level 2 OP permission required.

Manage your beacon settings directly in-game:
- `/betterbeacons set <block> [size]` - Add or change a block's range (example: `/betterbeacons set minecraft:dirt 3` for a 3x3 chunk area).
- `/betterbeacons remove <block>` - Stop a specific block from working as a custom beacon base.
- `/betterbeacons list` - See every block that currently has a custom range.
- `/betterbeacons reload` - Refresh settings if you manually edited the config file.

---

## Advanced Customization
Server owners can find the configuration file at `config/betterbeacons.json`. You can add any block ID here and set its chunk diameter. Size 3 means a 3x3 chunk area centered on the beacon.

```json
{
  "enable_custom_beacons": true,
  "beacon_blocks": {
    "minecraft:iron_block": 3,
    "minecraft:diamond_block": 7,
    "minecraft:netherite_block": 9
  }
}
```
---
## License
Copyright (c) 2026 Monte_Carlo_Simulations. Licensed under the Apache License, Version 2.0.
