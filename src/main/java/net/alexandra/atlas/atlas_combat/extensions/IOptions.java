package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.client.OptionInstance;

public interface IOptions {
	OptionInstance<Boolean> autoAttack();
	OptionInstance<Boolean> shieldCrouch();
	OptionInstance<Boolean> lowShield();
	OptionInstance<Boolean> getReducedDebugInfo();
}
