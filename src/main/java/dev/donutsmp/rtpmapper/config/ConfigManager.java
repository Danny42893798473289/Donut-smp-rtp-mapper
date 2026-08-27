package dev.donutsmp.rtpmapper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.donutsmp.rtpmapper.DonutRtpMapperMod;
import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final ConfigManager INSTANCE = new ConfigManager();

	private final Path configDir;
	private final Path configFile;
	private MapperConfig config = new MapperConfig();

	private ConfigManager() {
		this.configDir = FabricLoader.getInstance().getConfigDir().resolve(DonutRtpMapperMod.CONFIG_FOLDER);
		this.configFile = configDir.resolve("config.json");
	}

	public static ConfigManager get() {
		return INSTANCE;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public MapperConfig getConfig() {
		return config;
	}

	public void load() {
		try {
			Files.createDirectories(configDir);
			if (Files.exists(configFile)) {
				String json = Files.readString(configFile, StandardCharsets.UTF_8);
				MapperConfig loaded = GSON.fromJson(json, MapperConfig.class);
				if (loaded != null) {
					migrateLegacy(json, loaded);
					config = loaded;
				}
			} else {
				save();
			}
		} catch (IOException exception) {
			config = new MapperConfig();
		}
	}

	private void migrateLegacy(String json, MapperConfig loaded) {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			if (root.has("enabledDimensions") && !root.has("rtpOverworld")) {
				loaded.rtpOverworld = false;
				loaded.rtpNether = false;
				loaded.rtpEnd = false;
				JsonArray dimensions = root.getAsJsonArray("enabledDimensions");
				for (JsonElement element : dimensions) {
					if (!element.isJsonPrimitive()) {
						continue;
					}
					switch (element.getAsString().trim().toLowerCase(java.util.Locale.ROOT)) {
						case "nether" -> loaded.rtpNether = true;
						case "end" -> loaded.rtpEnd = true;
						default -> loaded.rtpOverworld = true;
					}
				}
			}

			if (root.has("randomizeDimension") && !root.get("randomizeDimension").getAsBoolean()
					&& root.has("rtpDimension")) {
				String fixed = root.get("rtpDimension").getAsString().toLowerCase(Locale.ROOT);
				loaded.rtpOverworld = fixed.equals("overworld");
				loaded.rtpNether = fixed.equals("nether");
				loaded.rtpEnd = fixed.equals("end");
			}

			if (root.has("avoidRepeatDimension")) {
				loaded.avoidRepeatTarget = root.get("avoidRepeatDimension").getAsBoolean();
			}

			if (!root.has("serverBrandContains") || root.get("serverBrandContains").isJsonNull()
					|| root.get("serverBrandContains").getAsString().isBlank()) {
				loaded.serverBrandContains = "donut,donutfolia";
			}

			if (!root.has("showLifetimeSamples")) {
				loaded.showLifetimeSamples = true;
			}

			if (!loaded.hasAnyRtpTarget()) {
				loaded.rtpOverworld = true;
			}
		} catch (RuntimeException ignored) {
			if (!loaded.hasAnyRtpTarget()) {
				loaded.rtpOverworld = true;
			}
			if (loaded.serverBrandContains == null || loaded.serverBrandContains.isBlank()) {
				loaded.serverBrandContains = "donut,donutfolia";
			}
		}
	}

	public void save() {
		try {
			Files.createDirectories(configDir);
			Files.writeString(configFile, GSON.toJson(config), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
	}

	public void update(MapperConfig updated) {
		if (!updated.hasAnyRtpTarget()) {
			updated.rtpOverworld = true;
		}
		if (updated.serverBrandContains == null || updated.serverBrandContains.isBlank()) {
			updated.serverBrandContains = "donut,donutfolia";
		}
		config = updated.copy();
		save();
	}
}
