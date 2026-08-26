package dev.donutsmp.rtpmapper.engine;

import java.util.Locale;

/**
 * Classifies RTP landing coordinates into DonutSMP-style distance rings from world center (0, 0).
 * The live server picks random safe coords inside its configured RTP border; these rings help map coverage.
 */
public final class MapRegion {
	public static final int[] RING_BOUNDS = {5_000, 10_000, 15_000, 20_000, 25_000, 30_000};

	private MapRegion() {
	}

	public static String ringLabel(double x, double z) {
		double distance = Math.sqrt(x * x + z * z);
		if (distance <= RING_BOUNDS[0]) {
			return "0-5k";
		}
		for (int index = 1; index < RING_BOUNDS.length; index++) {
			if (distance <= RING_BOUNDS[index]) {
				return RING_BOUNDS[index - 1] / 1_000 + "k-" + RING_BOUNDS[index] / 1_000 + "k";
			}
		}
		return "30k+";
	}

	public static String quadrantLabel(double x, double z) {
		if (Math.abs(x) < 1 && Math.abs(z) < 1) {
			return "origin";
		}

		boolean east = x >= 0;
		boolean south = z >= 0;
		if (east && !south) {
			return "NE";
		}
		if (!east && !south) {
			return "NW";
		}
		if (east) {
			return "SE";
		}
		return "SW";
	}

	public static String regionLabel(double x, double z) {
		return String.format(Locale.ROOT, "%s %s", quadrantLabel(x, z), ringLabel(x, z));
	}
}
