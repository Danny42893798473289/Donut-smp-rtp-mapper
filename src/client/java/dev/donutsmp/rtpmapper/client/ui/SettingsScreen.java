package dev.donutsmp.rtpmapper.client.ui;

import dev.donutsmp.rtpmapper.config.ConfigManager;
import dev.donutsmp.rtpmapper.config.MapperConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
	private final Screen parent;
	private MapperConfig draft;
	private EditBox cooldownBox;
	private EditBox warmupBox;
	private EditBox serverFilterBox;
	private EditBox serverBrandFilterBox;

	public SettingsScreen(Screen parent) {
		super(Component.literal("RTP Mapper Settings"));
		this.parent = parent;
		this.draft = ConfigManager.get().getConfig().copy();
	}

	@Override
	protected void init() {
		int x = width / 2 - 150;
		int y = 36;
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

		serverBrandFilterBox = new EditBox(font, x, y, fieldWidth, fieldHeight, Component.literal("Server brand contains"));
		serverBrandFilterBox.setValue(draft.serverBrandContains);
		addRenderableWidget(serverBrandFilterBox);
		y += 36;

		graphicsLabelY = y;
		y += 12;

		addRenderableWidget(CycleButton.onOffBuilder(draft.rtpOverworld)
				.create(x, y, fieldWidth, 20, Component.literal("RTP: Overworld"), (button, value) -> draft.rtpOverworld = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.rtpNether)
				.create(x, y, fieldWidth, 20, Component.literal("RTP: Nether"), (button, value) -> draft.rtpNether = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.rtpEnd)
				.create(x, y, fieldWidth, 20, Component.literal("RTP: The End"), (button, value) -> draft.rtpEnd = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.avoidRepeatTarget)
				.create(x, y, fieldWidth, 20, Component.literal("Avoid repeat target"), (button, value) -> draft.avoidRepeatTarget = value));
		y += 28;

		addRenderableWidget(CycleButton.onOffBuilder(draft.hudEnabled)
				.create(x, y, fieldWidth, 20, Component.literal("HUD enabled"), (button, value) -> draft.hudEnabled = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.hudShowMiniMap)
				.create(x, y, fieldWidth, 20, Component.literal("HUD mini-map"), (button, value) -> draft.hudShowMiniMap = value));
		y += 24;

		addRenderableWidget(CycleButton.onOffBuilder(draft.showLifetimeSamples)
				.create(x, y, fieldWidth, 20, Component.literal("Lifetime samples (map + HUD)"), (button, value) -> draft.showLifetimeSamples = value));
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

	private int graphicsLabelY = 120;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.centeredText(font, title.getString(), width / 2, 16, 0xFFFFFF);
		graphics.text(font, "Cooldown (seconds)", width / 2 - 150, 24, 0xFFB0BEC5, false);
		graphics.text(font, "Warmup (seconds)", width / 2 - 150, 52, 0xFFB0BEC5, false);
		graphics.text(font, "Server address filter", width / 2 - 150, 80, 0xFFB0BEC5, false);
		graphics.text(font, "Server brand filter (proxy-safe)", width / 2 - 150, 108, 0xFFB0BEC5, false);
		graphics.text(font, "RTP targets (random among enabled)", width / 2 - 150, graphicsLabelY, 0xFF90CAF9, false);
		graphics.text(font, "DonutSMP: /rtp overworld | nether | end", width / 2 - 150, graphicsLabelY + 10, 0xFF78909C, false);
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
		draft.serverBrandContains = serverBrandFilterBox.getValue().trim();
		if (!draft.hasAnyRtpTarget()) {
			draft.rtpOverworld = true;
		}
		ConfigManager.get().update(draft);
		onClose();
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
