package dev.donutsmp.rtpmapper.client.ui;

import dev.donutsmp.rtpmapper.config.ConfigManager;
import dev.donutsmp.rtpmapper.data.RtpSample;
import dev.donutsmp.rtpmapper.data.SampleStore;
import dev.donutsmp.rtpmapper.engine.RegionStats;
import dev.donutsmp.rtpmapper.engine.RtpMapperEngine;
import dev.donutsmp.rtpmapper.render.MapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MapperScreen extends Screen {
	private final MapRenderer mapRenderer = new MapRenderer();
	private boolean showLifetimeSamples;
	private String footerMessage = "Ready";

	public MapperScreen() {
		super(Component.literal("DonutSMP RTP Mapper"));
		this.showLifetimeSamples = ConfigManager.get().getConfig().showLifetimeSamples;
	}

	@Override
	protected void init() {
		int top = 8;
		int buttonWidth = 92;
		int gap = 4;
		int x = 8;

		addRenderableWidget(Button.builder(Component.literal("Start Mapping"), button -> toggleMapping())
				.bounds(x, top, buttonWidth, 20).build());
		x += buttonWidth + gap;

		addRenderableWidget(Button.builder(Component.literal("Clear Data"), button -> clearData())
				.bounds(x, top, buttonWidth, 20).build());
		x += buttonWidth + gap;

		addRenderableWidget(Button.builder(Component.literal("Export CSV"), button -> exportCsv())
				.bounds(x, top, buttonWidth, 20).build());
		x += buttonWidth + gap;

		addRenderableWidget(Button.builder(Component.literal("Reset View"), button -> mapRenderer.resetView())
				.bounds(x, top, buttonWidth, 20).build());
		x += buttonWidth + gap;

		addRenderableWidget(Button.builder(Component.literal("Settings"), button -> openSettings())
				.bounds(x, top, buttonWidth, 20).build());
		x += buttonWidth + gap;

		addRenderableWidget(Button.builder(
				Component.literal(showLifetimeSamples ? "View: Lifetime" : "View: Session"),
				button -> {
					showLifetimeSamples = !showLifetimeSamples;
					var config = ConfigManager.get().getConfig();
					config.showLifetimeSamples = showLifetimeSamples;
					ConfigManager.get().update(config);
					clearWidgets();
					init();
				}
		).bounds(width - 118, top, 110, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		RtpMapperEngine engine = RtpMapperEngine.get();
		SampleStore store = SampleStore.get();
		Minecraft minecraft = Minecraft.getInstance();

		int panelWidth = 180;
		int topBarHeight = 34;
		int bottomBarHeight = 18;
		int mapX = panelWidth + 16;
		int mapY = topBarHeight + 8;
		int mapWidth = width - mapX - 8;
		int mapHeight = height - mapY - bottomBarHeight - 8;

		graphics.fill(0, 0, width, topBarHeight, 0xEE111827);
		graphics.text(font, "DonutSMP RTP Mapper", 8, 12, 0xFFECEFF1, false);

		String status = engine.isRunning() ? "RUNNING" : "STOPPED";
		int statusColor = engine.isRunning() ? 0xFF66BB6A : 0xFFE57373;
		graphics.text(font, status, 190, 12, statusColor, false);

		graphics.fill(8, topBarHeight + 8, panelWidth + 8, height - bottomBarHeight - 8, 0xCC101828);
		renderStatusPanel(graphics, engine, store, minecraft, 16, topBarHeight + 16);

		List<RtpSample> samples = store.getDisplaySamples(showLifetimeSamples);
		String title = showLifetimeSamples
				? "Random Teleports on DonutSMP — Lifetime (" + samples.size() + ")"
				: "Random Teleports on DonutSMP — Session (" + samples.size() + ")";
		graphics.text(font, title, mapX, mapY - 12, 0xFFCFD8DC, false);
		mapRenderer.render(graphics, mapX, mapY, mapWidth, mapHeight, samples);

		renderStatistics(graphics, samples, 16, height - bottomBarHeight - 72, panelWidth - 16);

		String toast = engine.getToastMessage();
		footerMessage = toast.isBlank() ? footerMessage : toast;
		graphics.text(font, footerMessage, 8, height - 14, 0xFF90A4AE, false);

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private void renderStatusPanel(GuiGraphicsExtractor graphics, RtpMapperEngine engine, SampleStore store, Minecraft minecraft, int x, int y) {
		int line = y;
		graphics.text(font, "MAPPER STATUS", x, line, 0xFF90CAF9, false);
		line += 14;
		graphics.text(font,
				"Samples: " + store.getSessionSamples().size() + " session / " + store.getAllSamples().size() + " total",
				x, line, 0xFFB0BEC5, false);
		line += 12;

		if (minecraft.player != null) {
			graphics.text(font, String.format(Locale.ROOT,
					"Current X/Y/Z: %.0f / %.0f / %.0f",
					minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ()
			), x, line, 0xFFB0BEC5, false);
			line += 12;
		}

		graphics.text(font, String.format(Locale.ROOT,
				"Last RTP X/Z: %.0f / %.0f", engine.getLastRtpX(), engine.getLastRtpZ()
		), x, line, 0xFFB0BEC5, false);
		line += 12;

		graphics.text(font, "Next RTP: " + engine.getNextRtpLabel(), x, line, 0xFFB0BEC5, false);
		line += 12;
		graphics.text(font, "Session: " + RtpMapperEngine.formatDuration(engine.getSessionDuration()), x, line, 0xFFB0BEC5, false);
		line += 12;
		graphics.text(font, "Failed attempts: " + engine.getFailedAttempts(), x, line, 0xFFB0BEC5, false);
		line += 12;
		graphics.text(font, engine.getServerStatus(), x, line, 0xFF81C784, false);
	}

	private void renderStatistics(GuiGraphicsExtractor graphics, List<RtpSample> samples, int x, int y, int widthBox) {
		graphics.text(font, "STATISTICS", x, y, 0xFF90CAF9, false);
		y += 14;

		if (samples.isEmpty()) {
			graphics.text(font, "No samples in this view.", x, y, 0xFF78909C, false);
			return;
		}

		double minX = samples.stream().mapToDouble(RtpSample::x).min().orElse(0);
		double maxX = samples.stream().mapToDouble(RtpSample::x).max().orElse(0);
		double minZ = samples.stream().mapToDouble(RtpSample::z).min().orElse(0);
		double maxZ = samples.stream().mapToDouble(RtpSample::z).max().orElse(0);
		double avgDistance = samples.stream().mapToDouble(RtpSample::distanceFromOrigin).average().orElse(0);

		graphics.text(font, String.format(Locale.ROOT, "Count: %d", samples.size()), x, y, 0xFFB0BEC5, false);
		y += 10;
		graphics.text(font, String.format(Locale.ROOT, "X range: %.0f to %.0f", minX, maxX), x, y, 0xFFB0BEC5, false);
		y += 10;
		graphics.text(font, String.format(Locale.ROOT, "Z range: %.0f to %.0f", minZ, maxZ), x, y, 0xFFB0BEC5, false);
		y += 10;
		graphics.text(font, String.format(Locale.ROOT, "Avg distance: %.0f", avgDistance), x, y, 0xFFB0BEC5, false);
		y += 10;

		String quadrants = RegionStats.formatQuadrantBreakdown(samples);
		if (!quadrants.isEmpty()) {
			graphics.text(font, "Quadrants: " + quadrants, x, y, 0xFFB0BEC5, false);
			y += 10;
		}

		for (Map.Entry<String, Long> entry : RegionStats.topRegions(samples, 3)) {
			graphics.text(font, String.format(Locale.ROOT, "%s: %d", entry.getKey(), entry.getValue()), x, y, 0xFFB0BEC5, false);
			y += 10;
		}

		long within5k = samples.stream().filter(sample -> sample.distanceFromOrigin() <= 5_000).count();
		long within10k = samples.stream().filter(sample -> sample.distanceFromOrigin() <= 10_000).count();
		graphics.text(font, String.format(Locale.ROOT,
				"Within 5k: %d  10k: %d", within5k, within10k
		), x, y, 0xFFB0BEC5, false);
		y += 10;

		if (!samples.isEmpty()) {
			RtpSample latest = samples.getLast();
			graphics.text(font, "Latest region: " + latest.mapRegion(), x, y, 0xFFB0BEC5, false);
			y += 10;
		}

		double avgDelta = samples.stream().mapToDouble(RtpSample::teleportDelta).filter(delta -> delta > 0).average().orElse(0);
		if (avgDelta > 0) {
			graphics.text(font, String.format(Locale.ROOT, "Avg RTP move: %.0f blocks", avgDelta), x, y, 0xFFB0BEC5, false);
			y += 10;
		}

		long outsideZone = samples.stream().filter(RtpSample::looksOutsideDonutRtpZone).count();
		if (outsideZone > 0) {
			graphics.text(font, "Outside ~15k RTP zone: " + outsideZone + "/" + samples.size(), x, y, 0xFFFFB74D, false);
			y += 10;
		}

		String diagnosis = RegionStats.diagnoseSamples(samples);
		if (!diagnosis.isEmpty()) {
			graphics.text(font, diagnosis, x, y, 0xFFFF8A65, false);
		}
	}

	private void toggleMapping() {
		RtpMapperEngine engine = RtpMapperEngine.get();
		engine.toggleRunning();
		footerMessage = engine.isRunning() ? "Mapping started" : "Mapping stopped";
	}

	private void clearData() {
		SampleStore.get().clearSession();
		footerMessage = "Session samples cleared";
	}

	private void exportCsv() {
		try {
			SampleStore store = SampleStore.get();
			List<RtpSample> samples = showLifetimeSamples ? store.getAllSamples() : store.getSessionSamples();
			Path target = ConfigManager.get().getConfigDir().resolve(
					showLifetimeSamples ? "export-all.csv" : "export-session.csv"
			);
			store.exportViewCsv(samples, target);
			footerMessage = "Exported CSV to " + target.getFileName();
		} catch (Exception exception) {
			footerMessage = "Export failed: " + exception.getMessage();
		}
	}

	private void openSettings() {
		Minecraft.getInstance().setScreen(new SettingsScreen(this));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
