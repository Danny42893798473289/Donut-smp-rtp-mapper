package dev.donutsmp.rtpmapper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.donutsmp.rtpmapper.DonutRtpMapperMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
					if (loaded.enabledDimensions == null || loaded.enabledDimensions.isEmpty()) {
						loaded.enabledDimensions = new MapperConfig().enabledDimensions;
					}
					config = loaded;
				}
			} else {
				save();
			}
		} catch (IOException exception) {
			config = new MapperConfig();
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
		config = updated.copy();
		save();
	}
}
