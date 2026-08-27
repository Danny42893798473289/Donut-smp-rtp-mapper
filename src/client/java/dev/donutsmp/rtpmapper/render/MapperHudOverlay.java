package dev.donutsmp.rtpmapper.render;

import dev.donutsmp.rtpmapper.config.ConfigManager;
import dev.donutsmp.rtpmapper.data.SampleStore;
import dev.donutsmp.rtpmapper.engine.RtpMapperEngine;
import dev.donutsmp.rtpmapper.engine.RtpMapperState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;

public final class MapperHudOverlay {
	private static final MapRenderer MINI_MAP = new MapRenderer();

	private MapperHudOverlay() {
	}

	public static void render(GuiGraphicsExtractor graphics) {
		var config = ConfigManager.get().getConfig();
		if (!config.hudEnabled) {
			return;
		}

		RtpMapperEngine engine = RtpMapperEngine.get();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return;
		}

		int panelWidth = config.hudShowMiniMap ? 220 : 210;
		int panelHeight = config.hudShowMiniMap ? 170 : 90;
		int margin = 8;
		int x = margin;
		int y = margin;

		switch (config.hudCorner) {
			case "top_right" -> x = minecraft.getWindow().getGuiScaledWidth() - panelWidth - margin;
			case "bottom_left" -> y = minecraft.getWindow().getGuiScaledHeight() - panelHeight - margin;
			case "bottom_right" -> {
				x = minecraft.getWindow().getGuiScaledWidth() - panelWidth - margin;
				y = minecraft.getWindow().getGuiScaledHeight() - panelHeight - margin;
			}
			default -> {
			}
		}

		graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xCC101828);
		drawOutline(graphics, x, y, panelWidth, panelHeight, 0xFF2E4367);

		int textY = y + 6;
		int textX = x + 8;
		int statusColor = engine.isRunning() ? 0xFF66BB6A : 0xFFE57373;
		String status = engine.isRunning() ? "RUNNING" : "STOPPED";

		graphics.text(minecraft.font, "DonutSMP RTP Mapper", textX, textY, 0xFFECEFF1, false);
		graphics.text(minecraft.font, status, x + panelWidth - 58, textY, statusColor, false);
		textY += 12;

		SampleStore store = SampleStore.get();
		var displaySamples = store.getDisplaySamples(config.showLifetimeSamples);
		String sampleLabel = config.showLifetimeSamples
				? "Samples: " + displaySamples.size() + " (lifetime)"
				: "Samples: " + displaySamples.size() + " (session)";
		graphics.text(minecraft.font, sampleLabel, textX, textY, 0xFFB0BEC5, false);
		textY += 10;

		var player = minecraft.player;
		graphics.text(minecraft.font, String.format(Locale.ROOT,
				"Current: %.0f / %.0f / %.0f", player.getX(), player.getY(), player.getZ()
		), textX, textY, 0xFFB0BEC5, false);
		textY += 10;

		if (displaySamples.isEmpty()) {
			graphics.text(minecraft.font, "Last RTP: —", textX, textY, 0xFFB0BEC5, false);
		} else {
			graphics.text(minecraft.font, String.format(Locale.ROOT,
					"Last RTP: %.0f / %.0f [%s]", engine.getLastRtpX(), engine.getLastRtpZ(), engine.getCurrentTargetName()
			), textX, textY, 0xFFB0BEC5, false);
		}
		textY += 10;

		graphics.text(minecraft.font, "Next RTP: " + engine.getNextRtpLabel(), textX, textY, 0xFFB0BEC5, false);
		textY += 10;

		graphics.text(minecraft.font, String.format(Locale.ROOT,
				"Session: %s  Failed: %d",
				RtpMapperEngine.formatDuration(engine.getSessionDuration()),
				engine.getFailedAttempts()
		), textX, textY, 0xFFB0BEC5, false);
		textY += 10;

		if (engine.getState() == RtpMapperState.WARMUP) {
			graphics.text(minecraft.font, "STAND STILL — warmup", textX, textY, 0xFFFF8A65, false);
			textY += 10;
		}

		String toast = engine.getToastMessage();
		if (!toast.isBlank()) {
			graphics.text(minecraft.font, toast, textX, textY, 0xFF81C784, false);
		}

		if (config.hudShowMiniMap) {
			int mapX = x + 8;
			int mapY = y + panelHeight - 88;
			MINI_MAP.render(graphics, mapX, mapY, 204, 80, displaySamples);
		}
	}

	private static void drawOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}
}
