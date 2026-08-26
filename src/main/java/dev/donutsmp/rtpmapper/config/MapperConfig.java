package dev.donutsmp.rtpmapper.config;

public final class MapperConfig {
	public int cooldownSeconds = 300;
	public int warmupSeconds = 5;

	/** DonutSMP RTP targets — only enabled targets are used; one is chosen at random each cycle. */
	public boolean rtpOverworld = true;
	public boolean rtpNether = false;
	public boolean rtpEnd = false;

	/** When multiple targets are enabled, avoid picking the same target twice in a row. */
	public boolean avoidRepeatTarget = true;

	public int teleportConfirmBlocks = 50;
	public int teleportConfirmTimeoutSeconds = 30;
	public String serverAddressContains = "donutsmp";
	public boolean autoSaveAfterSample = true;
	public boolean hudEnabled = true;
	public String hudCorner = "top_left";
	public boolean hudShowMiniMap = true;

	public MapperConfig copy() {
		MapperConfig copy = new MapperConfig();
		copy.cooldownSeconds = cooldownSeconds;
		copy.warmupSeconds = warmupSeconds;
		copy.rtpOverworld = rtpOverworld;
		copy.rtpNether = rtpNether;
		copy.rtpEnd = rtpEnd;
		copy.avoidRepeatTarget = avoidRepeatTarget;
		copy.teleportConfirmBlocks = teleportConfirmBlocks;
		copy.teleportConfirmTimeoutSeconds = teleportConfirmTimeoutSeconds;
		copy.serverAddressContains = serverAddressContains;
		copy.autoSaveAfterSample = autoSaveAfterSample;
		copy.hudEnabled = hudEnabled;
		copy.hudCorner = hudCorner;
		copy.hudShowMiniMap = hudShowMiniMap;
		return copy;
	}

	public boolean hasAnyRtpTarget() {
		return rtpOverworld || rtpNether || rtpEnd;
	}
}
