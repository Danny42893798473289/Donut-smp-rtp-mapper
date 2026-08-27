package dev.donutsmp.rtpmapper.data;

import dev.donutsmp.rtpmapper.engine.MapRegion;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record RtpSample(
		String id,
		String sessionId,
		Instant timestamp,
		double x,
		double y,
		double z,
		String dimension,
		String mapRegion,
		double teleportDelta
) {
	public static RtpSample create(String sessionId, double x, double y, double z, String dimension, double teleportDelta) {
		return new RtpSample(
				UUID.randomUUID().toString(),
				sessionId,
				Instant.now(),
				x,
				y,
				z,
				dimension,
				MapRegion.regionLabel(x, z),
				teleportDelta
		);
	}

	public double distanceFromOrigin() {
		return Math.sqrt(x * x + z * z);
	}

	public String toCsvRow() {
		return String.format(Locale.ROOT,
				"%s,%s,%.3f,%.3f,%.3f,%s,%s,%.3f,%.3f",
				timestamp.toString(),
				sessionId,
				x,
				y,
				z,
				dimension,
				mapRegion,
				distanceFromOrigin(),
				teleportDelta
		);
	}

	public String toTextLine() {
		return String.format(Locale.ROOT,
				"[%s] session=%s dim=%s region=%s x=%.1f y=%.1f z=%.1f dist=%.1f delta=%.1f",
				timestamp,
				sessionId,
				dimension,
				mapRegion,
				x,
				y,
				z,
				distanceFromOrigin(),
				teleportDelta
		);
	}

	public boolean looksOutsideDonutRtpZone() {
		return distanceFromOrigin() > MapRegion.DONUT_TYPICAL_RTP_MAX_DISTANCE;
	}
}
