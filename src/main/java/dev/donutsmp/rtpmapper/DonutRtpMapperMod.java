package dev.donutsmp.rtpmapper;

import net.minecraft.resources.Identifier;

public final class DonutRtpMapperMod {
	public static final String MOD_ID = "donut-smp-rtp-mapper";
	public static final String CONFIG_FOLDER = "donut-smp-rtp-mapper";

	private DonutRtpMapperMod() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
