package dev.donutsmp.rtpmapper.config;

import java.util.ArrayList;
import java.util.List;

public final class MapperConfig {
	public int cooldownSeconds = 300;
	public int warmupSeconds = 5;
	public boolean randomizeDimension = true;
	public List<String> enabledDimensions = new ArrayList<>(List.of("overworld", "nether", "end"));
	public String rtpDimension = "overworld";
	public boolean avoidRepeatDimension = false;
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
		copy.randomizeDimension = randomizeDimension;
		copy.enabledDimensions = new ArrayList<>(enabledDimensions);
		copy.rtpDimension = rtpDimension;
		copy.avoidRepeatDimension = avoidRepeatDimension;
		copy.teleportConfirmBlocks = teleportConfirmBlocks;
		copy.teleportConfirmTimeoutSeconds = teleportConfirmTimeoutSeconds;
		copy.serverAddressContains = serverAddressContains;
		copy.autoSaveAfterSample = autoSaveAfterSample;
		copy.hudEnabled = hudEnabled;
		copy.hudCorner = hudCorner;
		copy.hudShowMiniMap = hudShowMiniMap;
		return copy;
	}
}
