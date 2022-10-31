package net.alexandra.atlas.atlas_combat;

import com.mojang.serialization.Codec;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import eu.midnightdust.lib.config.MidnightConfig;
import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.alexandra.atlas.atlas_combat.networking.ClientNetworkingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import java.util.Arrays;

public class AtlasClient implements ClientModInitializer {
	private static final Component ACCESSIBILITY_TOOLTIP_LOW_SHIELD = Component.translatable("options.lowShield.tooltip");
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> lowShield = OptionInstance.createBoolean("options.lowShield", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_LOW_SHIELD),false);
	public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
	public static final OptionInstance<Boolean> protectionIndicator = OptionInstance.createBoolean("options.protIndicator",false);
	public static final OptionInstance<ShieldIndicatorStatus> shieldIndicator = new OptionInstance<>(
			"options.shieldIndicator",
			OptionInstance.noTooltip(),
			OptionInstance.forOptionEnum(),
			new OptionInstance.Enum<>(Arrays.asList(ShieldIndicatorStatus.values()), Codec.INT.xmap(ShieldIndicatorStatus::byId, ShieldIndicatorStatus::getId)),
			ShieldIndicatorStatus.CROSSHAIR,
			value -> {
			}
	);

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientNetworkingHandler networkingHandler = new ClientNetworkingHandler();

	}
}
