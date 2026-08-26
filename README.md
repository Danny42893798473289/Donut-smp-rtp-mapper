# DonutSMP RTP Mapper

Client-side Fabric mod for **Minecraft 26.1** that automates DonutSMP random teleports, records landing coordinates, plots them on a map, and saves samples to CSV/TXT in your Minecraft config folder.

## Features

- **Automated RTP loop** — sends `/rtp <dimension>` on a configurable cooldown
- **Random dimensions** — cycles randomly through overworld, nether, and end (configurable)
- **Live HUD overlay** — in-game status panel without opening a GUI
- **Full mapper screen** — scatter map with 5k ring guides, stats, export, and settings
- **Auto-save** — writes to `.minecraft/config/donut-smp-rtp-mapper/`

## Requirements

- Minecraft **26.1** (Fabric)
- **Fabric Loader** 0.19.3+
- **Fabric API** for 26.1
- **Java 25** (for building from source)

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.1.
2. Download **Fabric API** for 26.1 and place it in your `mods` folder.
3. Build or download this mod jar:
   ```bash
   ./gradlew build
   ```
4. Copy `build/libs/donut-smp-rtp-mapper-1.0.0.jar` into `.minecraft/mods/`.

## Usage

### Keybinds (default)

| Key | Action |
|-----|--------|
| `M` | Open / close RTP Mapper screen |
| `N` | Toggle HUD overlay |
| `K` | Start / stop mapping |

### Workflow

1. Connect to **DonutSMP** (`donutsmp.net`).
2. Press `K` or open the mapper (`M`) and click **Start Mapping**.
3. Stay completely still during the RTP warmup countdown.
4. Each successful teleport is plotted on the map and saved to disk.

### Config & data files

All files are stored under:

```
.minecraft/config/donut-smp-rtp-mapper/
├── config.json          # Settings (cooldown, dimensions, HUD, etc.)
├── samples.csv          # Master log (opens in Excel)
├── samples.txt          # Human-readable log
├── export-session.csv   # Last session export
├── export-all.csv       # Last all-samples export
└── sessions/            # Per-session CSV files
```

### Settings

Open **Settings** from the mapper screen to configure:

- Cooldown seconds (default: 300)
- Warmup seconds (default: 5)
- Random dimension toggle and enabled dimension pool
- HUD corner and mini-map visibility
- Server address filter (default: `donutsmp`)

## How it works

DonutSMP RTP uses `/rtp overworld`, `/rtp nether`, or `/rtp end` to skip the GUI and start a warmup countdown. Movement during warmup cancels the teleport. The mod:

1. Waits for cooldown
2. Picks a random enabled dimension
3. Sends the RTP command
4. Waits through warmup and confirms a position jump
5. Records X/Y/Z and saves to CSV/TXT

## Disclaimer

This mod automates server commands for coordinate mapping. You are responsible for complying with DonutSMP server rules.

## License

MIT
