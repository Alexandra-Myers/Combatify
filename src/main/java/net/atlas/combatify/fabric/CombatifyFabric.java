package net.atlas.combatify.fabric;

//? fabric {
import net.atlas.combatify.Combatify;
import net.fabricmc.api.ModInitializer;
import org.mozilla.javascript.Context;

import java.lang.ref.Cleaner;

public class CombatifyFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Combatify.CLEANER.register(this, Context::exit);
		Combatify.init();
	}
}
//?}
