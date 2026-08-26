package dev.donutsmp.rtpmapper.render;

import dev.donutsmp.rtpmapper.data.RtpSample;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public final class MapRenderer {
	public static final int[] RING_DISTANCES = {5_000, 10_000, 15_000, 20_000, 25_000, 30_000};

	private double panX = 0.0;
	private double panZ = 0.0;
	private double zoom = 1.0;

	public void resetView() {
		panX = 0.0;
		panZ = 0.0;
		zoom = 1.0;
	}

	public void render(GuiGraphicsExtractor graphics, int x, int y, int width, int height, List<RtpSample> samples) {
		int background = 0xCC0B1220;
		int gridColor = 0x552A4A7A;
		int axisColor = 0xFF9DB4D9;
		int ringColor = 0x443C5F8A;

		graphics.fill(x, y, x + width, y + height, background);

		double maxDistance = 30_000.0;
		for (RtpSample sample : samples) {
			maxDistance = Math.max(maxDistance, Math.max(Math.abs(sample.x()), Math.abs(sample.z())));
		}

		double scale = (Math.min(width, height) / 2.0 - 12.0) / maxDistance * zoom;
		int centerX = x + width / 2 + (int) panX;
		int centerY = y + height / 2 + (int) panZ;

		drawGrid(graphics, x, y, width, height, gridColor);
		drawRings(graphics, centerX, centerY, scale, ringColor);
		drawAxis(graphics, x, y, width, height, centerX, centerY, axisColor);

		graphics.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, 0xFFFFD54F);

		for (RtpSample sample : samples) {
			int sampleX = centerX + (int) (sample.x() * scale);
			int sampleY = centerY + (int) (sample.z() * scale);
			int color = colorForDimension(sample.dimension());
			graphics.fill(sampleX - 2, sampleY - 2, sampleX + 3, sampleY + 3, color);
		}
	}

	private void drawGrid(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		for (int offset = 0; offset <= width; offset += 40) {
			graphics.fill(x + offset, y, x + offset + 1, y + height, color);
		}
		for (int offset = 0; offset <= height; offset += 40) {
			graphics.fill(x, y + offset, x + width, y + offset + 1, color);
		}
	}

	private void drawAxis(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int centerX, int centerY, int color) {
		if (centerY >= y && centerY <= y + height) {
			graphics.fill(x, centerY, x + width, centerY + 1, color);
		}
		if (centerX >= x && centerX <= x + width) {
			graphics.fill(centerX, y, centerX + 1, y + height, color);
		}
	}

	private void drawRings(GuiGraphicsExtractor graphics, int centerX, int centerY, double scale, int color) {
		for (int distance : RING_DISTANCES) {
			int radius = (int) (distance * scale);
			drawCircle(graphics, centerX, centerY, radius, color);
		}
	}

	private void drawCircle(GuiGraphicsExtractor graphics, int centerX, int centerY, int radius, int color) {
		if (radius <= 0) {
			return;
		}

		for (int angle = 0; angle < 360; angle++) {
			double radians = Math.toRadians(angle);
			int px = centerX + (int) (Math.cos(radians) * radius);
			int py = centerY + (int) (Math.sin(radians) * radius);
			graphics.fill(px, py, px + 1, py + 1, color);
		}
	}

	public static int colorForDimension(String dimension) {
		if (dimension == null) {
			return 0xFF64B5F6;
		}

		return switch (dimension.toLowerCase()) {
			case "nether" -> 0xFFE57373;
			case "end" -> 0xFFBA68C8;
			default -> 0xFF64B5F6;
		};
	}
}
