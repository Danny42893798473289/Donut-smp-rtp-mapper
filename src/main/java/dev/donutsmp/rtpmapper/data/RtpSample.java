package dev.donutsmp.rtpmapper.data;

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
		String dimension
) {
	public static RtpSample create(String sessionId, double x, double y, double z, String dimension) {
		return new RtpSample(
				UUID.randomUUID().toString(),
				sessionId,
				Instant.now(),
				x,
				y,
				z,
				dimension
		);
	}

	public double distanceFromOrigin() {
		return Math.sqrt(x * x + z * z);
	}

	public String toCsvRow() {
		return String.format(Locale.ROOT,
				"%s,%s,%.3f,%.3f,%.3f,%s,%.3f",
				timestamp.toString(),
				sessionId,
				x,
				y,
				z,
				dimension,
				distanceFromOrigin()
		);
	}

	public String toTextLine() {
		return String.format(Locale.ROOT,
				"[%s] session=%s dim=%s x=%.1f y=%.1f z=%.1f dist=%.1f",
				timestamp,
				sessionId,
				dimension,
				x,
				y,
				z,
				distanceFromOrigin()
		);
	}
}
