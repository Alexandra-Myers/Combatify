package net.atlas.combat_enhanced;

import com.mojang.serialization.Codec;
import net.atlas.combat_enhanced.config.ShieldIndicatorStatus;
import net.atlas.combat_enhanced.networking.ClientNetworkingHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.OptionInstance;

import java.util.Arrays;

public class CombatEnhancedClient implements ClientModInitializer {
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
	public static final OptionInstance<Boolean> protectionIndicator = OptionInstance.createBoolean("options.protIndicator",false);
	public static final OptionInstance<Boolean> fishingRodLegacy = OptionInstance.createBoolean("options.fishingRodLegacy",false);
	public static final OptionInstance<ShieldIndicatorStatus> shieldIndicator = new OptionInstance<>(
			"options.shieldIndicator",
			OptionInstance.noTooltip(),
			OptionInstance.forOptionEnum(),
			new OptionInstance.Enum<>(Arrays.asList(ShieldIndicatorStatus.values()), Codec.INT.xmap(ShieldIndicatorStatus::byId, ShieldIndicatorStatus::getId)),
			ShieldIndicatorStatus.CROSSHAIR,
			value -> {
			}
	);
	public ClientNetworkingHandler clientNetworkingHandler;

	@Override
	public void onInitializeClient() {
		clientNetworkingHandler = new ClientNetworkingHandler();
	}
}
