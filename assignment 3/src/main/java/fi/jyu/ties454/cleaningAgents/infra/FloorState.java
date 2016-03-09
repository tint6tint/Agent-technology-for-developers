package fi.jyu.ties454.cleaningAgents.infra;

public enum FloorState {
	CLEAN, DIRTY;

	public FloorState invert() {
		return this == CLEAN ? DIRTY : CLEAN;
	}
}
