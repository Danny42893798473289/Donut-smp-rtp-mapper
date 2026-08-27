package dev.donutsmp.rtpmapper.engine;

import java.util.Locale;

/**
 * Classifies RTP landing coordinates into DonutSMP-style distance rings from world center (0, 0).
 * DonutSMP overworld RTP commonly lands tens of thousands of blocks from spawn, so rings extend past 30k.
 */
public final class MapRegion {
	/** DonutSMP RTP is configured around spawn — wiki/plugin defaults are ~5k–10k from 0,0. */
	public static final int DONUT_TYPICAL_RTP_MAX_DISTANCE = 15_000;

	public static final int[] RING_BOUNDS = {
			5_000, 10_000, 15_000, 20_000, 25_000, 30_000,
			40_000, 50_000, 60_000, 75_000, 100_000, 125_000, 150_000
	};

	private MapRegion() {
	}

	public static String ringLabel(double x, double z) {
		double distance = Math.sqrt(x * x + z * z);
		if (distance <= RING_BOUNDS[0]) {
			return "0-5k";
		}
		for (int index = 1; index < RING_BOUNDS.length; index++) {
			if (distance <= RING_BOUNDS[index]) {
				return formatRing(RING_BOUNDS[index - 1], RING_BOUNDS[index]);
			}
		}
		return "150k+";
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

	private static String formatRing(int lower, int upper) {
		return (lower / 1_000) + "k-" + (upper / 1_000) + "k";
	}
}
