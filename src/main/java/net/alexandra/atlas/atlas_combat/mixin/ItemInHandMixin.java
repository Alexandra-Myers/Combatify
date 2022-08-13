package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandMixin {
	@Redirect(method = "tick", at = @At(value = "INVOKE",target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	public float modifyArmPos4(LocalPlayer instance, float v) {
		return (instance.getAttackStrengthScale(v) / 2.0F);
	}
	@Inject(method = "applyItemArmAttackTransform", at = @At(value = "HEAD"))
	public void changeArmAttack(PoseStack matrices, HumanoidArm arm, float swingProgress, CallbackInfo ci) {
		int var4 = arm == HumanoidArm.RIGHT ? 1 : -1;
		float var5 = Mth.sin(swingProgress * swingProgress * 3.1415927F);
		matrices.mulPose(Vector3f.YP.rotationDegrees((float)var4 * (45.0F + var5 * -20.0F)));
		float var6 = Mth.sin(Mth.sqrt(swingProgress) * 3.1415927F);
		matrices.mulPose(Vector3f.ZP.rotationDegrees((float)var4 * var6 * -20.0F));
		matrices.mulPose(Vector3f.XP.rotationDegrees(var6 * -80.0F));
		matrices.mulPose(Vector3f.YP.rotationDegrees((float)var4 * -45.0F));
		ci.cancel();
	}
}
