package dev.donutsmp.rtpmapper.engine;

import dev.donutsmp.rtpmapper.config.ConfigManager;
import dev.donutsmp.rtpmapper.config.MapperConfig;
import dev.donutsmp.rtpmapper.data.RtpSample;
import dev.donutsmp.rtpmapper.data.SampleStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.function.Consumer;

public final class RtpMapperEngine {
	private static final RtpMapperEngine INSTANCE = new RtpMapperEngine();

	private final RtpTargetPicker targetPicker = new RtpTargetPicker();
	private final SampleStore sampleStore = SampleStore.get();

	private RtpMapperState state = RtpMapperState.STOPPED;
	private boolean running = false;
	private boolean combatTagged = false;
	private Instant sessionStartedAt = Instant.now();
	private Instant cooldownEndsAt = Instant.now();
	private Instant stateStartedAt = Instant.now();
	private Instant toastExpiresAt = Instant.now();

	private String statusMessage = "Idle";
	private String toastMessage = "";
	private String currentDimension = "";
	private String currentTargetName = "";
	private String serverStatus = "Not connected";

	private double preRtpX;
	private double preRtpY;
	private double preRtpZ;

	private double lastRtpX;
	private double lastRtpZ;

	private int failedAttempts = 0;
	private int warmupTicksRemaining = 0;
	private int confirmTicksRemaining = 0;

	private Consumer<RtpSample> sampleListener = sample -> {};

	private RtpMapperEngine() {
	}

	public static RtpMapperEngine get() {
		return INSTANCE;
	}

	public void setSampleListener(Consumer<RtpSample> listener) {
		this.sampleListener = listener == null ? sample -> {} : listener;
	}

	public void start() {
		if (running) {
			return;
		}

		if (!isOnDonutSmp()) {
			Minecraft minecraft = Minecraft.getInstance();
			String address = DonutServerProbe.getServerAddress(minecraft);
			String brand = DonutServerProbe.getServerBrand(minecraft);
			showToast("Not on DonutSMP (addr=" + describe(address) + ", brand=" + describe(brand) + ")");
			return;
		}

		running = true;
		sampleStore.startSession();
		sessionStartedAt = Instant.now();
		failedAttempts = 0;
		state = RtpMapperState.WAITING_COOLDOWN;
		cooldownEndsAt = Instant.now();
		stateStartedAt = Instant.now();
		statusMessage = "Starting mapping session";
		showToast("Mapping started");
	}

	public void stop() {
		running = false;
		state = RtpMapperState.STOPPED;
		statusMessage = "Stopped";
		showToast("Mapping stopped");
	}

	public void toggleRunning() {
		if (running) {
			stop();
		} else {
			start();
		}
	}

	public void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		updateServerStatus(minecraft);

		if (!running || player == null || minecraft.level == null) {
			return;
		}

		if (!isOnDonutSmp()) {
			stop();
			showToast("Left DonutSMP server — mapping stopped.");
			return;
		}

		if (combatTagged) {
			statusMessage = "Combat tagged — waiting";
			return;
		}

		switch (state) {
			case WAITING_COOLDOWN -> tickWaitingCooldown();
			case PICKING_DIMENSION -> tickPickingDimension();
			case SENDING_RTP -> tickSendingRtp(player);
			case WARMUP -> tickWarmup(player);
			case CONFIRMING_TELEPORT -> tickConfirmingTeleport(player);
			case RECORDING_SAMPLE -> tickRecordingSample(player);
			default -> {
			}
		}
	}

	private void tickWaitingCooldown() {
		if (Instant.now().isBefore(cooldownEndsAt)) {
			statusMessage = "Cooldown";
			return;
		}

		transition(RtpMapperState.PICKING_DIMENSION);
	}

	private void tickPickingDimension() {
		MapperConfig config = ConfigManager.get().getConfig();
		if (!config.hasAnyRtpTarget()) {
			handleFailure("No RTP targets enabled in Settings");
			return;
		}

		RtpTargetPicker.RtpTarget target = targetPicker.pick(config);
		currentDimension = target.commandArg();
		currentTargetName = target.displayName();
		transition(RtpMapperState.SENDING_RTP);
	}

	private void tickSendingRtp(LocalPlayer player) {
		preRtpX = player.getX();
		preRtpY = player.getY();
		preRtpZ = player.getZ();
		player.connection.sendCommand("rtp " + currentDimension);
		warmupTicksRemaining = ConfigManager.get().getConfig().warmupSeconds * 20;
		transition(RtpMapperState.WARMUP);
		statusMessage = "Warmup — stand still";
	}

	private void tickWarmup(LocalPlayer player) {
		player.input.keyPresses = Input.EMPTY;

		if (warmupTicksRemaining > 0) {
			warmupTicksRemaining--;
			statusMessage = "Warmup — stand still (" + (warmupTicksRemaining / 20 + 1) + "s)";
			return;
		}

		confirmTicksRemaining = ConfigManager.get().getConfig().teleportConfirmTimeoutSeconds * 20;
		transition(RtpMapperState.CONFIRMING_TELEPORT);
		statusMessage = "Confirming teleport";
	}

	private void tickConfirmingTeleport(LocalPlayer player) {
		player.input.keyPresses = Input.EMPTY;

		MapperConfig config = ConfigManager.get().getConfig();
		double dx = player.getX() - preRtpX;
		double dz = player.getZ() - preRtpZ;
		double distanceSquared = dx * dx + dz * dz;
		int threshold = config.teleportConfirmBlocks;

		if (distanceSquared >= (long) threshold * threshold) {
			transition(RtpMapperState.RECORDING_SAMPLE);
			return;
		}

		if (confirmTicksRemaining <= 0) {
			handleFailure("Teleport not detected in time");
			return;
		}

		confirmTicksRemaining--;
		statusMessage = "Confirming teleport (" + (confirmTicksRemaining / 20 + 1) + "s)";
	}

	private void tickRecordingSample(LocalPlayer player) {
		RtpSample sample = RtpSample.create(
				sampleStore.getCurrentSessionId(),
				player.getX(),
				player.getY(),
				player.getZ(),
				currentDimension
		);

		lastRtpX = sample.x();
		lastRtpZ = sample.z();
		sampleStore.addSample(sample);

		if (ConfigManager.get().getConfig().autoSaveAfterSample) {
			try {
				sampleStore.appendSample(sample);
			} catch (Exception exception) {
				showToast("Failed to save sample: " + exception.getMessage());
			}
		}

		sampleListener.accept(sample);
		showToast("Sample #" + sampleStore.getSessionSamples().size() + " saved ["
				+ currentTargetName + " / " + MapRegion.regionLabel(sample.x(), sample.z())
				+ " dist " + RegionStats.formatDistance(sample.distanceFromOrigin()) + "]");

		startCooldown();
		transition(RtpMapperState.WAITING_COOLDOWN);
		statusMessage = "Sample recorded";
	}

	public void handleFailure(String reason) {
		failedAttempts++;
		statusMessage = reason;
		showToast(reason);
		startCooldown();
		transition(RtpMapperState.WAITING_COOLDOWN);
	}

	public void handleWarmupCancelled() {
		handleFailure("Warmup cancelled");
	}

	public void handleCombatTagged() {
		combatTagged = true;
		statusMessage = "Combat tagged";
		showToast("Combat tagged — RTP blocked");
	}

	public void clearCombatTagged() {
		combatTagged = false;
	}

	public void onChatMessage(String message) {
		String lower = message.toLowerCase(Locale.ROOT);
		if (lower.contains("combat")) {
			if (lower.contains("tagged") || lower.contains("you are now")) {
				handleCombatTagged();
			}
			if (lower.contains("no longer") || lower.contains("expired")) {
				clearCombatTagged();
			}
		}
		if (lower.contains("warmup") && (lower.contains("cancel") || lower.contains("cancelled"))) {
			if (state == RtpMapperState.WARMUP) {
				handleWarmupCancelled();
			}
		}
		if (lower.contains("teleport") && lower.contains("fail")) {
			if (state == RtpMapperState.WARMUP || state == RtpMapperState.CONFIRMING_TELEPORT) {
				handleFailure("Teleport failed");
			}
		}
	}

	public void onActionBarMessage(String message) {
		String lower = message.toLowerCase(Locale.ROOT);
		if (lower.contains("combat")) {
			handleCombatTagged();
		}
		if (state == RtpMapperState.WARMUP && lower.matches(".*\\b[0-5]\\b.*")) {
			statusMessage = "Warmup — stand still";
		}
	}

	private void startCooldown() {
		int seconds = ConfigManager.get().getConfig().cooldownSeconds;
		cooldownEndsAt = Instant.now().plusSeconds(seconds);
	}

	private void transition(RtpMapperState next) {
		state = next;
		stateStartedAt = Instant.now();
	}

	private void updateServerStatus(Minecraft minecraft) {
		if (minecraft.getCurrentServer() == null) {
			serverStatus = "Not connected";
			return;
		}

		String address = DonutServerProbe.getServerAddress(minecraft);
		if (isOnDonutSmp()) {
			String brand = DonutServerProbe.getServerBrand(minecraft);
			if (brand != null && !brand.isBlank()) {
				serverStatus = "DonutSMP accepted (" + brand + ")";
			} else {
				serverStatus = "DonutSMP server accepted";
			}
		} else {
			serverStatus = "Connected to " + address;
		}
	}

	public boolean isOnDonutSmp() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.getCurrentServer() == null) {
			return false;
		}

		return DonutServerDetector.matches(
				DonutServerProbe.getServerAddress(minecraft),
				DonutServerProbe.getServerBrand(minecraft),
				ConfigManager.get().getConfig()
		);
	}

	public boolean isRunning() {
		return running;
	}

	public RtpMapperState getState() {
		return state;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public String getToastMessage() {
		if (Instant.now().isAfter(toastExpiresAt)) {
			return "";
		}
		return toastMessage;
	}

	public String getServerStatus() {
		return serverStatus;
	}

	public String getCurrentDimension() {
		return currentDimension;
	}

	public String getCurrentTargetName() {
		return currentTargetName;
	}

	public int getFailedAttempts() {
		return failedAttempts;
	}

	public double getLastRtpX() {
		return lastRtpX;
	}

	public double getLastRtpZ() {
		return lastRtpZ;
	}

	public Duration getSessionDuration() {
		return Duration.between(sessionStartedAt, Instant.now());
	}

	public Duration getCooldownRemaining() {
		if (state != RtpMapperState.WAITING_COOLDOWN) {
			return Duration.ZERO;
		}
		Duration remaining = Duration.between(Instant.now(), cooldownEndsAt);
		return remaining.isNegative() ? Duration.ZERO : remaining;
	}

	public String getNextRtpLabel() {
		return switch (state) {
			case STOPPED -> "Idle";
			case WAITING_COOLDOWN -> formatCountdown(getCooldownRemaining());
			case WARMUP -> "Warmup…";
			case CONFIRMING_TELEPORT -> "Confirming…";
			case PICKING_DIMENSION, SENDING_RTP -> "Sending…";
			case RECORDING_SAMPLE -> "Saving…";
			case FAILED -> "Failed";
		};
	}

	public static String formatCountdown(Duration duration) {
		long totalSeconds = Math.max(0, duration.getSeconds());
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;
		return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
	}

	public static String formatDuration(Duration duration) {
		return formatCountdown(duration);
	}

	private void showToast(String message) {
		toastMessage = message;
		toastExpiresAt = Instant.now().plusSeconds(4);
	}

	private static String describe(String value) {
		if (value == null || value.isBlank()) {
			return "unknown";
		}
		return value.length() > 32 ? value.substring(0, 29) + "..." : value;
	}
}
