package dev.donutsmp.rtpmapper.engine;

import dev.donutsmp.rtpmapper.config.MapperConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DimensionPicker {
	private final Random random = new Random();
	private String lastDimension = "";

	public String pick(MapperConfig config) {
		List<String> pool = new ArrayList<>();
		for (String dimension : config.enabledDimensions) {
			if (dimension != null && !dimension.isBlank()) {
				pool.add(dimension.trim().toLowerCase());
			}
		}

		if (pool.isEmpty()) {
			return "overworld";
		}

		if (!config.randomizeDimension) {
			lastDimension = config.rtpDimension;
			return config.rtpDimension;
		}

		if (pool.size() == 1) {
			lastDimension = pool.getFirst();
			return lastDimension;
		}

		String picked;
		do {
			picked = pool.get(random.nextInt(pool.size()));
		} while (config.avoidRepeatDimension && pool.size() > 1 && picked.equals(lastDimension));

		lastDimension = picked;
		return picked;
	}

	public String getLastDimension() {
		return lastDimension;
	}
}
