package dev.donutsmp.rtpmapper.engine;

import dev.donutsmp.rtpmapper.config.MapperConfig;

import java.util.Locale;

/**
 * Detects DonutSMP even when connecting through a proxy (address may not contain "donutsmp").
 */
public final class DonutServerDetector {
	private DonutServerDetector() {
	}

	public static boolean matches(String serverAddress, String serverBrand, MapperConfig config) {
		if (config == null) {
			return false;
		}

		if (matchesAnyNeedle(serverAddress, config.serverAddressContains)) {
			return true;
		}

		return matchesAnyNeedle(serverBrand, config.serverBrandContains);
	}

	private static boolean matchesAnyNeedle(String value, String needles) {
		if (value == null || needles == null) {
			return false;
		}

		for (String needle : needles.split(",")) {
			if (matchesNeedle(value, needle)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesNeedle(String value, String needle) {
		if (value == null || needle == null) {
			return false;
		}

		String trimmed = needle.trim();
		if (trimmed.isEmpty()) {
			return false;
		}

		return value.toLowerCase(Locale.ROOT).contains(trimmed.toLowerCase(Locale.ROOT));
	}
}
