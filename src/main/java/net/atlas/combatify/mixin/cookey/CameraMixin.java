package net.atlas.combatify.mixin.cookey;

import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.atlas.combatify.config.cookey.option.DoubleSliderOption;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private float eyeHeight;

    @Shadow
    public abstract Entity getEntity();

    @Shadow
    private float eyeHeightOld;

	@Unique
	private DoubleSliderOption sneakAnimationSpeed;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectOptions(CallbackInfo ci) {
		sneakAnimationSpeed = CombatifyClient.getInstance().getConfig().animations().sneakAnimationSpeed();
	}

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void disableSneakAnimation(CallbackInfo ci) {
        if (this.sneakAnimationSpeed.get() == 0.0 && this.entity != null) {
            this.eyeHeight = this.getEntity().getEyeHeight();
            this.eyeHeightOld = this.eyeHeight;
            ci.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Camera;eyeHeight:F", opcode = Opcodes.PUTFIELD))
    public void setSneakAnimationSpeed(Camera camera, float value) {
        this.eyeHeight += (float) ((this.entity.getEyeHeight() - this.eyeHeight) * 0.5F * this.sneakAnimationSpeed.get());
    }
}
