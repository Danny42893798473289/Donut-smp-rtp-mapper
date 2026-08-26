package dev.donutsmp.rtpmapper.client.ui;

import dev.donutsmp.rtpmapper.config.ConfigManager;
import dev.donutsmp.rtpmapper.config.MapperConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SettingsScreen extends Screen {
	private final Screen parent;
	private MapperConfig draft;
	private EditBox cooldownBox;
	private EditBox warmupBox;
	private EditBox serverFilterBox;
	private EditBox dimensionsBox;
	private EditBox fixedDimensionBox;

	public SettingsScreen(Screen parent) {
		super(Component.literal("RTP Mapper Settings"));
		this.parent = parent;
		this.draft = ConfigManager.get().getConfig().copy();
	}

	@Override
	protected void init() {
		int x = width / 2 - 150;
		int y = 40;
		int fieldWidth = 300;
		int fieldHeight = 20;

		cooldownBox = new EditBox(font, x, y, fieldWidth, fieldHeight, Component.literal("Cooldown seconds"));
		cooldownBox.setValue(String.valueOf(draft.cooldownSeconds));
		addRenderableWidget(cooldownBox);
		y += 28;

		warmupBox = new EditBox(font, x, y, fieldWidth, fieldHeight, Component.literal("Warmup seconds"));
		warmupBox.setValue(String.valueOf(draft.warmupSeconds));
		addRenderableWidget(warmupBox);
		y += 28;

		serverFilterBox = new EditBox(font, x, y, fieldWidth, fieldHeight, Component.literal("Server address contains"));
		serverFilterBox.setValue(draft.serverAddressContains);
		addRenderableWidget(serverFilterBox);
		y += 28;

		dimensionsBox = new EditBox(font, x, y, fieldWidth, fieldHeight, Component.literal("Enabled dimensions"));
		dimensionsBox.setValue(String.join(",", draft.enabledDimensions));
		addRenderableWidget(dimensionsBox);
		y += 28;

		fixedDimensionBox = new EditBox(font, x, y, fieldWidth, fieldHeight, Component.literal("Fixed dimension"));
		fixedDimensionBox.setValue(draft.rtpDimension);
		addRenderableWidget(fixedDimensionBox);
		y += 34;

		addRenderableWidget(CycleButton.onOffBuilder(draft.randomizeDimension)
				.create(x, y, fieldWidth, 20, Component.literal("Random dimension"), (button, value) -> draft.randomizeDimension = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.avoidRepeatDimension)
				.create(x, y, fieldWidth, 20, Component.literal("Avoid repeat dimension"), (button, value) -> draft.avoidRepeatDimension = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.hudEnabled)
				.create(x, y, fieldWidth, 20, Component.literal("HUD enabled"), (button, value) -> draft.hudEnabled = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.hudShowMiniMap)
				.create(x, y, fieldWidth, 20, Component.literal("HUD mini-map"), (button, value) -> draft.hudShowMiniMap = value));
		y += 24;

		addRenderableWidget(CycleButton.builder(HudCorner::label, HudCorner.TOP_LEFT)
				.withValues(HudCorner.values())
				.create(x, y, fieldWidth, 20, Component.literal("HUD corner"), (button, value) -> draft.hudCorner = value.id));
		y += 34;

		addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveAndClose())
				.bounds(x, y, 145, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
				.bounds(x + 155, y, 145, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title.getString(), width / 2, 16, 0xFFFFFF);
		graphics.text(font, "Cooldown (seconds)", width / 2 - 150, 28, 0xFFB0BEC5, false);
		graphics.text(font, "Warmup (seconds)", width / 2 - 150, 56, 0xFFB0BEC5, false);
		graphics.text(font, "Server filter", width / 2 - 150, 84, 0xFFB0BEC5, false);
		graphics.text(font, "Enabled dimensions (comma-separated)", width / 2 - 150, 112, 0xFFB0BEC5, false);
		graphics.text(font, "Fixed dimension when random is off", width / 2 - 150, 140, 0xFFB0BEC5, false);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private void saveAndClose() {
		try {
			draft.cooldownSeconds = Math.max(1, Integer.parseInt(cooldownBox.getValue().trim()));
			draft.warmupSeconds = Math.max(1, Integer.parseInt(warmupBox.getValue().trim()));
		} catch (NumberFormatException exception) {
			return;
		}

		draft.serverAddressContains = serverFilterBox.getValue().trim();
		draft.rtpDimension = fixedDimensionBox.getValue().trim().toLowerCase();
		draft.enabledDimensions = parseDimensions(dimensionsBox.getValue());
		ConfigManager.get().update(draft);
		onClose();
	}

	private List<String> parseDimensions(String raw) {
		List<String> dimensions = new ArrayList<>();
		for (String part : raw.split(",")) {
			String trimmed = part.trim().toLowerCase();
			if (!trimmed.isBlank()) {
				dimensions.add(trimmed);
			}
		}
		if (dimensions.isEmpty()) {
			dimensions.add("overworld");
		}
		return dimensions;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	private enum HudCorner {
		TOP_LEFT("top_left", "Top Left"),
		TOP_RIGHT("top_right", "Top Right"),
		BOTTOM_LEFT("bottom_left", "Bottom Left"),
		BOTTOM_RIGHT("bottom_right", "Bottom Right");

		private final String id;
		private final String label;

		HudCorner(String id, String label) {
			this.id = id;
			this.label = label;
		}

		static Component label(HudCorner corner) {
			return Component.literal(corner.label);
		}
	}
}
