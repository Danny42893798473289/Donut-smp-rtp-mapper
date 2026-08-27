package dev.donutsmp.rtpmapper.engine;

import dev.donutsmp.rtpmapper.data.RtpSample;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class RegionStats {
	private RegionStats() {
	}

	public static Map<String, Long> countByRegion(List<RtpSample> samples) {
		return samples.stream()
				.collect(Collectors.groupingBy(
						sample -> MapRegion.regionLabel(sample.x(), sample.z()),
						Collectors.counting()
				));
	}

	public static Map<String, Long> countByQuadrant(List<RtpSample> samples) {
		return samples.stream()
				.collect(Collectors.groupingBy(
						sample -> MapRegion.quadrantLabel(sample.x(), sample.z()),
						Collectors.counting()
				));
	}

	public static List<Map.Entry<String, Long>> topRegions(List<RtpSample> samples, int limit) {
		return countByRegion(samples).entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
						.thenComparing(Map.Entry::getKey))
				.limit(limit)
				.toList();
	}

	public static String formatQuadrantBreakdown(List<RtpSample> samples) {
		Map<String, Long> counts = new LinkedHashMap<>();
		for (String quadrant : List.of("NE", "NW", "SE", "SW", "origin")) {
			counts.put(quadrant, 0L);
		}
		countByQuadrant(samples).forEach(counts::put);

		return counts.entrySet().stream()
				.filter(entry -> entry.getValue() > 0)
				.map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(Collectors.joining("  "));
	}

	public static String formatDistance(double distance) {
		if (distance >= 1_000) {
			return String.format(Locale.ROOT, "%.1fk", distance / 1_000.0);
		}
		return String.format(Locale.ROOT, "%.0f", distance);
	}

	public static String diagnoseSamples(List<RtpSample> samples) {
		if (samples.isEmpty()) {
			return "";
		}

		long outsideRtpZone = samples.stream().filter(RtpSample::looksOutsideDonutRtpZone).count();
		Map<String, Long> quadrants = countByQuadrant(samples);
		long distinctQuadrants = quadrants.size();
		double avgDelta = samples.stream().mapToDouble(RtpSample::teleportDelta).average().orElse(0);

		if (outsideRtpZone == samples.size() && distinctQuadrants <= 1) {
			return "All samples outside Donut RTP zone (~15k) in one quadrant — likely not real RTP landings";
		}
		if (outsideRtpZone > samples.size() / 2) {
			return outsideRtpZone + "/" + samples.size() + " samples outside Donut RTP zone (~15k from spawn)";
		}
		if (avgDelta > 0 && avgDelta < 2_000 && outsideRtpZone > 0) {
			return "Low avg move (" + formatDistance(avgDelta) + ") — check teleport confirm setting";
		}
		return "";
	}
}
