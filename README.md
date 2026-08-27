# DonutSMP RTP Mapper

Client-side Fabric mod for **Minecraft 26.1.2** that automates DonutSMP random teleports, records landing coordinates, plots them on a map, and saves samples to CSV/TXT in your Minecraft config folder.

## Features

- **Automated RTP loop** — sends `/rtp <dimension>` on a configurable cooldown
- **Settings-driven targets** — only dimensions you enable in Settings are used; the mod randomly picks among those each cycle (e.g. only Overworld enabled → always `/rtp overworld`)
- **Map region tracking** — each sample records its quadrant + distance ring (e.g. `NW 75k-100k`) based on DonutSMP's RTP coordinate range (often 30k–150k+ from spawn)
- **Live HUD overlay** — in-game status panel without opening a GUI
- **Full mapper screen** — scatter map with 5k ring guides, stats, export, and settings
- **Auto-save** — writes to `.minecraft/config/donut-smp-rtp-mapper/`

## Requirements

- Minecraft **26.1.2** (Fabric)
- **Fabric Loader** 0.19.3+
- **Fabric API** `0.155.0+26.1.2`
- **Java 25** (for building from source)

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.1.2.
2. Download **Fabric API 0.155.0+26.1.2** and place it in your `mods` folder.
3. Build or download this mod jar:
   ```bash
   ./gradlew build
   ```
4. Copy `build/libs/donut-smp-rtp-mapper-1.0.0+26.1.2.jar` into `.minecraft/mods/`.

## Usage

### Keybinds (default)

| Key | Action |
|-----|--------|
| `M` | Open / close RTP Mapper screen |
| `N` | Toggle HUD overlay |
| `K` | Start / stop mapping |

### RTP targets (Settings)

DonutSMP RTP is dimension-level only ([wiki](https://donutsmp.wiki/rtp)):

| Command | Dimension |
|---------|-----------|
| `/rtp overworld` | Overworld |
| `/rtp nether` | Nether |
| `/rtp end` | The End |

In **Settings**, toggle which dimensions to include. The mod **randomly chooses among enabled targets** each cycle:

- Only **Overworld** on → always `/rtp overworld`
- **Overworld + Nether** on → randomly alternates between them
- All three on → random among all three

The server then picks a random safe coordinate inside its configured RTP border for that dimension. The mod records which **map region** (quadrant + distance ring from spawn) each landing falls in.

### Workflow

1. Connect to **DonutSMP** (`donutsmp.net`) or via a proxy — the mod detects Donut by **server brand** (e.g. `DonutFolia`) as well as address.
2. Open Settings and enable the dimension(s) you want to map.
3. Press `K` or click **Start Mapping**.
4. Stay completely still during the RTP warmup countdown.
5. Each successful teleport is plotted and saved automatically.

### Config & data files

```
.minecraft/config/donut-smp-rtp-mapper/
├── config.json          # Settings (cooldown, RTP targets, HUD, etc.)
├── samples.csv          # Master log (opens in Excel)
├── samples.txt          # Human-readable log
├── export-session.csv   # Last session export
└── sessions/            # Per-session CSV files
```

CSV columns: `timestamp, session_id, x, y, z, dimension, map_region, distance_from_origin`

## Troubleshooting

### Why does every sample say "NW 30k+"?

Two common causes:

1. **Old ring labels** — before v1.0.6, anything past 30k blocks from spawn was lumped into a single `30k+` bucket. DonutSMP overworld RTP often lands around **50k–100k+** blocks out, so almost every landing looked identical. v1.0.6 adds finer rings up to `150k+`. Re-open the mapper to reload `samples.csv` with updated labels.

2. **False samples from movement** — if you moved 50+ blocks while the mod was "Confirming teleport" (before v1.0.6), it could save your **current** position instead of the RTP landing. v1.0.6 freezes movement during confirm and raises the default threshold to **500 blocks** (Settings → Teleport confirm blocks). Stay still through the full warmup + confirm phase.

The mod only sends `/rtp overworld` — **DonutSMP picks the coordinates**. Check the Statistics panel for quadrant counts (`NE`, `NW`, etc.) and X/Z ranges to see whether you truly have an NW bias or just coarse labels / bad samples.

### Sodium crash: "Overflowed the mesh time buffer"

This crash comes from **Sodium + heavy ESP mods** (Meteor/Glazed Covered Hole, Storage ESP, etc.), not from RTP Mapper. While mapping:

- Disable Glazed hole/spawner ESP and Meteor storage ESP / break indicators
- Lower render distance (8 or less)
- Avoid freecam while mapping
- Consider turning off Iris shader pipeline features if crashes persist at 4K

## Disclaimer

This mod automates server commands for coordinate mapping. You are responsible for complying with DonutSMP server rules.

## License

MIT
