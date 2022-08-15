package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasClient;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin implements IOptions {
	@Shadow
	@Final
	private OptionInstance<Boolean> reducedDebugInfo;

	@Shadow
	public abstract OptionInstance<Boolean> reducedDebugInfo();

	@Inject(method = "processOptions", at = @At(value = "HEAD"))
	public void injectOptions(Options.FieldAccess visitor, CallbackInfo ci) {
		visitor.process("autoAttack", AtlasClient.autoAttack);
		visitor.process("shieldCrouch", AtlasClient.shieldCrouch);
		visitor.process("lowShield", AtlasClient.lowShield);
	}

	@Override
	public OptionInstance<Boolean> autoAttack() {
		return AtlasClient.autoAttack;
	}
	@Override
	public OptionInstance<Boolean> shieldCrouch() {
		return AtlasClient.shieldCrouch;
	}

	@Override
	public OptionInstance<Boolean> lowShield() {
		return AtlasClient.lowShield;
	}
}
