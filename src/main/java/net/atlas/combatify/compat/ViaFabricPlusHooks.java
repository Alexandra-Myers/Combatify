package net.atlas.combatify.compat;

//? fabric {
import com.viaversion.viaaprilfools.api.AprilFoolsProtocolVersion;
import com.viaversion.viafabricplus.ViaFabricPlus;

public class ViaFabricPlusHooks {
	public static boolean is8cTarget() {
		return ViaFabricPlus.getImpl().getTargetVersion().equalTo(AprilFoolsProtocolVersion.sCombatTest8c);
	}
}
//?}
