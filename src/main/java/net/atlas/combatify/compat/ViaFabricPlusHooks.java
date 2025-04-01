package net.atlas.combatify.compat;

import de.florianmichael.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.viaaprilfools.api.AprilFoolsProtocolVersion;

public class ViaFabricPlusHooks {
	public static boolean is8cTarget() {
		return ProtocolTranslator.getTargetVersion().equalTo(AprilFoolsProtocolVersion.sCombatTest8c);
	}
}
