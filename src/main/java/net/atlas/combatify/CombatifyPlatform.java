package net.atlas.combatify;

//? fabric {
import net.atlas.combatify.fabric.CombatifyFabricPlatform;
//?} neoforge {
/*import net.atlas.combatify.neoforge.CombatifyNeoForgePlatform;
*///?}

import java.nio.file.Path;

public interface CombatifyPlatform {
	//? fabric {
	CombatifyPlatform INSTANCE = new CombatifyFabricPlatform();
	//?} neoforge {
	/*CombatifyPlatform INSTANCE = new CombatifyNeoForgePlatform();
	*///?}
}
