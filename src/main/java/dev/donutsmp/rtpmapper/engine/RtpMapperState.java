package dev.donutsmp.rtpmapper.engine;

public enum RtpMapperState {
	STOPPED,
	WAITING_COOLDOWN,
	PICKING_DIMENSION,
	SENDING_RTP,
	WARMUP,
	CONFIRMING_TELEPORT,
	RECORDING_SAMPLE,
	FAILED
}
