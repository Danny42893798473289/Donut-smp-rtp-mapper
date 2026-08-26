package dev.donutsmp.rtpmapper.engine;

import dev.donutsmp.rtpmapper.config.MapperConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Picks the next DonutSMP RTP command target.
 * DonutSMP only exposes dimension-level RTP: /rtp overworld | nether | end.
 * The server then picks a random safe coordinate within its configured RTP range for that dimension.
 */
public final class RtpTargetPicker {
	public record RtpTarget(String commandArg, String displayName) {
	}

	private static final RtpTarget OVERWORLD = new RtpTarget("overworld", "Overworld");
	private static final RtpTarget NETHER = new RtpTarget("nether", "Nether");
	private static final RtpTarget END = new RtpTarget("end", "The End");

	private final Random random = new Random();
	private RtpTarget lastTarget = null;

	public RtpTarget pick(MapperConfig config) {
		List<RtpTarget> enabled = enabledTargets(config);
		if (enabled.isEmpty()) {
			lastTarget = OVERWORLD;
			return OVERWORLD;
		}

		if (enabled.size() == 1) {
			lastTarget = enabled.getFirst();
			return lastTarget;
		}

		RtpTarget picked;
		do {
			picked = enabled.get(random.nextInt(enabled.size()));
		} while (config.avoidRepeatTarget && lastTarget != null && picked.equals(lastTarget));

		lastTarget = picked;
		return picked;
	}

	public RtpTarget getLastTarget() {
		return lastTarget;
	}

	public static List<RtpTarget> enabledTargets(MapperConfig config) {
		List<RtpTarget> targets = new ArrayList<>(3);
		if (config.rtpOverworld) {
			targets.add(OVERWORLD);
		}
		if (config.rtpNether) {
			targets.add(NETHER);
		}
		if (config.rtpEnd) {
			targets.add(END);
		}
		return targets;
	}
}
