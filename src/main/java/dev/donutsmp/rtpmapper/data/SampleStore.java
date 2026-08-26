package dev.donutsmp.rtpmapper.data;

import dev.donutsmp.rtpmapper.config.ConfigManager;
import dev.donutsmp.rtpmapper.DonutRtpMapperMod;
import dev.donutsmp.rtpmapper.engine.MapRegion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SampleStore {
	private static final String CSV_HEADER = "timestamp,session_id,x,y,z,dimension,map_region,distance_from_origin";
	private static final SampleStore INSTANCE = new SampleStore();

	private final List<RtpSample> allSamples = new ArrayList<>();
	private final List<RtpSample> sessionSamples = new ArrayList<>();
	private String currentSessionId = "";

	private SampleStore() {
	}

	public static SampleStore get() {
		return INSTANCE;
	}

	public void startSession() {
		currentSessionId = DonutRtpMapperMod.MOD_ID + "-" + System.currentTimeMillis();
		sessionSamples.clear();
	}

	public String getCurrentSessionId() {
		return currentSessionId;
	}

	public void addSample(RtpSample sample) {
		allSamples.add(sample);
		sessionSamples.add(sample);
	}

	public List<RtpSample> getAllSamples() {
		return Collections.unmodifiableList(allSamples);
	}

	public List<RtpSample> getSessionSamples() {
		return Collections.unmodifiableList(sessionSamples);
	}

	public void clearSession() {
		sessionSamples.clear();
	}

	public void clearAll() {
		allSamples.clear();
		sessionSamples.clear();
	}

	public void loadExisting() {
		Path csvPath = getSamplesCsvPath();
		if (!Files.exists(csvPath)) {
			return;
		}

		try {
			List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
			for (int index = 1; index < lines.size(); index++) {
				RtpSample sample = parseCsvLine(lines.get(index));
				if (sample != null) {
					allSamples.add(sample);
				}
			}
		} catch (IOException ignored) {
		}
	}

	private RtpSample parseCsvLine(String line) {
		String[] parts = line.split(",", -1);
		if (parts.length < 7) {
			return null;
		}

		try {
			boolean hasRegion = parts.length >= 8;
			return new RtpSample(
					"loaded-" + parts[1],
					parts[1],
					java.time.Instant.parse(parts[0]),
					Double.parseDouble(parts[2]),
					Double.parseDouble(parts[3]),
					Double.parseDouble(parts[4]),
					parts[5],
					hasRegion ? parts[6] : MapRegion.regionLabel(Double.parseDouble(parts[2]), Double.parseDouble(parts[4]))
			);
		} catch (RuntimeException exception) {
			return null;
		}
	}

	public void appendSample(RtpSample sample) throws IOException {
		Path configDir = ConfigManager.get().getConfigDir();
		Files.createDirectories(configDir);
		Files.createDirectories(configDir.resolve("sessions"));

		Path csvPath = getSamplesCsvPath();
		Path txtPath = configDir.resolve("samples.txt");
		Path sessionCsv = configDir.resolve("sessions").resolve(currentSessionId + ".csv");

		writeHeaderIfNeeded(csvPath);
		writeHeaderIfNeeded(sessionCsv);

		String row = sample.toCsvRow();
		Files.writeString(csvPath, row + System.lineSeparator(), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		Files.writeString(sessionCsv, row + System.lineSeparator(), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		Files.writeString(txtPath, sample.toTextLine() + System.lineSeparator(), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	public void exportViewCsv(List<RtpSample> samples, Path target) throws IOException {
		Files.createDirectories(target.getParent());
		String content = CSV_HEADER + System.lineSeparator()
				+ samples.stream().map(RtpSample::toCsvRow).collect(Collectors.joining(System.lineSeparator()));
		if (!samples.isEmpty()) {
			content += System.lineSeparator();
		}
		Files.writeString(target, content, StandardCharsets.UTF_8);
	}

	public Path getSamplesCsvPath() {
		return ConfigManager.get().getConfigDir().resolve("samples.csv");
	}

	private void writeHeaderIfNeeded(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.writeString(path, CSV_HEADER + System.lineSeparator(), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
	}
}
