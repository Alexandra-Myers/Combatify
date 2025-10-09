package net.atlas.combatify.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
	public static final Multimap<String, MatchingMods> incompatibleMixins;
	public static final Multimap<String, MatchingMods> modSpecificMixins;
	static {
		ImmutableMultimap.Builder<String, MatchingMods> incompatible = ImmutableMultimap.builder();
		ImmutableMultimap.Builder<String, MatchingMods> modSpecific = ImmutableMultimap.builder();
		incompatible.put("net.atlas.combatify.mixin.InvulnerabilityMixin", new MatchingMods("neoforge"));
		incompatible.put("net.atlas.combatify.mixin.ModifyShieldMixin", new MatchingMods("neoforge"));
		incompatible.put("net.atlas.combatify.mixin.compatibility.appleskin.HUDOverlayHandlerMixin", new MatchingMods("neoforge"));
		incompatible.put("net.atlas.combatify.mixin.client.ArmHeightMixin", new MatchingMods("cookeymod"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.neoforge.InvulnerabilityMixin", new MatchingMods("neoforge"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.neoforge.ModifyShieldMixin", new MatchingMods("neoforge"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.appleskin.FoodHelperMixin", new MatchingMods("appleskin"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.appleskin.HUDOverlayHandlerMixin", new MatchingMods("appleskin"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.appleskin.neoforge.NeoForgeHUDOverlayHandlerMixin", new MatchingMods("appleskin", "neoforge"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.appleskin.neoforge.HungerOverlayMixin", new MatchingMods("appleskin", "neoforge"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.appleskin.neoforge.SaturationOverlayMixin", new MatchingMods("appleskin", "neoforge"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.cookeymod.AddForce100PercentRechargeMixin", new MatchingMods("cookeymod"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.cookeymod.ArmHeightFixMixin", new MatchingMods("cookeymod"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.cookeymod.DisableMissedAttackRecoveryMixin", new MatchingMods("cookeymod"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.polymer.PolymerComponentMixin", new MatchingMods("polymer-core"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.sodium.SodiumGameOptionPagesMixin", new MatchingMods("sodium"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.viafabricplus.Attributes1_20_5Mixin", new MatchingMods("viafabricplus"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.viafabricplus.EntityPacketRewriter1_20_5Mixin", new MatchingMods("viafabricplus"));
		modSpecific.put("net.atlas.combatify.mixin.compatibility.viafabricplus.ProtocolCombatTest8cTo1_16_2PacketHandlersMixin", new MatchingMods("viafabricplus"));
		incompatibleMixins = incompatible.build();
		modSpecificMixins = modSpecific.build();
	}
	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		Logger logger = LogManager.getLogger("Combatify");
		if (mentionsActiveMods(incompatibleMixins, mixinClassName)) {
			logger.warn("[{}] {} marked incompatible with mixin \"{}\", cancelling application.",
				getClass().getSimpleName(),
				getFirstMentionedActiveMod(incompatibleMixins, mixinClassName).forIncompatible(),
				mixinClassName);
			return false;
		}
		if (modSpecificMixins.containsKey(mixinClassName)) {
			if (mentionsActiveMods(modSpecificMixins, mixinClassName)) {
				logger.info("[{}] Loading mod-specific mixin \"{}\" since {}.",
					getClass().getSimpleName(),
					mixinClassName,
					getFirstMentionedActiveMod(modSpecificMixins, mixinClassName).forModSpecific());
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	public MatchingMods getFirstMentionedActiveMod(Multimap<String, MatchingMods> toRead, String mixinClassName) {
		FabricLoader fabricLoader = FabricLoader.getInstance();
		if (toRead.containsKey(mixinClassName)) {
			for (MatchingMods modId : toRead.get(mixinClassName)) {
				if (modId.areModsLoaded(fabricLoader)) {
					return modId;
				}
			}
		}
		return null;
	}

	public boolean mentionsActiveMods(Multimap<String, MatchingMods> toRead, String mixinClassName) {
		return getFirstMentionedActiveMod(toRead, mixinClassName) != null;
	}
	public record MatchingMods(String... mods) {
		public boolean areModsLoaded(FabricLoader fabricLoader) {
			for (String mod : mods) if (!fabricLoader.isModLoaded(mod)) return false;
			return true;
		}

		public String forModSpecific() {
			return mods.length == 1 ? "mod \"" + mods[0] + "\" is present" : "mods \"" + Arrays.toString(mods) + "\" are present";
		}

		public String forIncompatible() {
			return mods.length == 1 ? "Mod \"" + mods[0] + "\" is" : "Mods \"" + Arrays.toString(mods) + "\" are";
		}
	}
}
