package net.atlas.combatify.fabric.client;

import net.atlas.combatify.CombatifyClient;
import net.fabricmc.api.ClientModInitializer;

public class CombatifyFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CombatifyClient.init();
	}
}
