package net.atlas.combatify.config;

public enum HealingMode implements BaseHealingMode {
	VANILLA(18),
	CTS(7),
	NEW(7);
	public final int minimumHealLevel;
	HealingMode(int minimumHealLevel) {
		this.minimumHealLevel = minimumHealLevel;
	}
	@Override
	public int getMinimumHealLevel() {
		return minimumHealLevel;
	}
}
