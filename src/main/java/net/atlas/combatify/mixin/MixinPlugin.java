package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
		MixinExtrasBootstrap.init();
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith("ArmHeightMixin") && FabricLoader.getInstance().isModLoaded("cookeymod")) return false;
		if (!mixinClassName.contains(".compatibility.")) return true;
		String mixinPackage = mixinClassName.substring(0, mixinClassName.lastIndexOf("."));
		String immediatePackage = mixinPackage.substring(mixinPackage.lastIndexOf(".") + 1);
		if (immediatePackage.equals("rea") && !FabricLoader.getInstance().isModLoaded("reach-entity-attributes")) return false;
		if (immediatePackage.equals("cookeymod") && !FabricLoader.getInstance().isModLoaded("cookeymod")) return false;
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
}
