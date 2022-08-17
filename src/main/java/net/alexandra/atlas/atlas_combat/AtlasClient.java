package net.alexandra.atlas.atlas_combat;

import net.alexandra.atlas.atlas_combat.networking.ClientNetworkingHandler;
import net.alexandra.atlas.atlas_combat.networking.NetworkingHandler;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class AtlasClient implements ClientModInitializer {
	private static final Component ACCESSIBILITY_TOOLTIP_LOW_SHIELD = Component.translatable("options.lowShield.tooltip");
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> lowShield = OptionInstance.createBoolean("options.lowShield", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_LOW_SHIELD),false);
	@Override
	public void onInitializeClient(ModContainer mod) {

		ClientNetworkingHandler networkingHandler = new ClientNetworkingHandler();

	}
}
