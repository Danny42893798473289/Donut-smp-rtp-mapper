package dev.donutsmp.rtpmapper.engine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class DonutServerProbe {
	private DonutServerProbe() {
	}

	public static String getServerAddress(Minecraft minecraft) {
		if (minecraft.getCurrentServer() == null) {
			return null;
		}
		return minecraft.getCurrentServer().ip;
	}

	public static String getServerBrand(Minecraft minecraft) {
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null) {
			return null;
		}
		return connection.serverBrand();
	}
}
