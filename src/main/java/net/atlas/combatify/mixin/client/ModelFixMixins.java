package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.client.ClientMethodHandler;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.monster.piglin.AbstractPiglinModel;
import net.minecraft.client.model.monster.piglin.PiglinModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.monster.zombie.AbstractZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ModelFixMixins {
	@Mixin(AbstractZombieModel.class)
	public static class AbstractZombieModelFixMixin<T extends ZombieRenderState> extends HumanoidModel<T> {
		public AbstractZombieModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@WrapOperation(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZLnet/minecraft/client/renderer/entity/state/UndeadRenderState;)V"))
		public <S extends UndeadRenderState> void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, S undeadRenderState, Operation<Void> original, @Local(argsOnly = true) T renderState) {
			if (renderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, renderState.mainArm, undeadRenderState, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, undeadRenderState);
		}
	}
	@Mixin(IllagerModel.class)
	public static class IllagerModelFixMixin<T extends IllagerRenderState> {
		@Shadow
		@Final
		private ModelPart head;

		@WrapOperation(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZLnet/minecraft/client/renderer/entity/state/UndeadRenderState;)V"))
		public <S extends UndeadRenderState> void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, S undeadRenderState, Operation<Void> original, @Local(argsOnly = true) T renderState) {
			if (renderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, renderState.mainArm, undeadRenderState, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, undeadRenderState);
		}
		@WrapOperation(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;swingWeaponDown(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/world/entity/HumanoidArm;FF)V"))
		public void injectGuardCheck1(ModelPart modelPart, ModelPart modelPart2, HumanoidArm humanoidArm, float f, float g, Operation<Void> original, @Local(argsOnly = true) T renderState) {
			if (renderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.swingWeaponDown(modelPart, modelPart2, humanoidArm, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, humanoidArm, f, g);
		}
	}
	@Mixin(PiglinModel.class)
	public static class PiglinModelFixMixin<T extends PiglinRenderState> extends AbstractPiglinModel<T> {

		public PiglinModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}
		@WrapOperation(method = "setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/PiglinRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;swingWeaponDown(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/world/entity/HumanoidArm;FF)V"))
		public void injectGuardCheck1(ModelPart modelPart, ModelPart modelPart2, HumanoidArm humanoidArm, float f, float g, Operation<Void> original, @Local(argsOnly = true) T renderState) {
			if (renderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.swingWeaponDown(modelPart, modelPart2, humanoidArm, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, humanoidArm, f, g);
		}
		@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PiglinRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinArmPose;DANCING:Lnet/minecraft/world/entity/monster/piglin/PiglinArmPose;"))
		public void injectGuardCheck1(PiglinRenderState piglinRenderState, CallbackInfo ci, @Local(ordinal = 0) PiglinArmPose piglinArmPose) {
			if ((piglinArmPose == PiglinArmPose.DEFAULT || (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && piglinRenderState.attackTime == 0.0F)) && piglinRenderState.combatify$mobIsGuarding()) {
				boolean rightHanded = piglinRenderState.mainArm == HumanoidArm.RIGHT;
				ModelPart arm = rightHanded ? leftArm : rightArm;
				ClientMethodHandler.animateBlockingBase(arm, rightHanded, piglinRenderState.attackTime, head.xRot);
			}
		}
	}
	@Mixin(ZombifiedPiglinModel.class)
	public static class ZombifiedPiglinModelFixMixin<T extends ZombifiedPiglinRenderState> extends AbstractPiglinModel<T> {
		public ZombifiedPiglinModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@WrapOperation(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombifiedPiglinRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZLnet/minecraft/client/renderer/entity/state/UndeadRenderState;)V"))
		public <S extends UndeadRenderState> void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, S undeadRenderState, Operation<Void> original, @Local(argsOnly = true) T renderState) {
			if (renderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, renderState.mainArm, undeadRenderState, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, undeadRenderState);
		}
	}
	@Mixin(ZombieVillagerModel.class)
	public static class ZombieVillagerModelFixMixin<T extends ZombieVillagerRenderState> extends HumanoidModel<T> {
		public ZombieVillagerModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@WrapOperation(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombieVillagerRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZLnet/minecraft/client/renderer/entity/state/UndeadRenderState;)V"))
		public <S extends UndeadRenderState> void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, S undeadRenderState, Operation<Void> original, @Local(argsOnly = true) T renderState) {
			if (renderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, renderState.mainArm, undeadRenderState, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, undeadRenderState);
		}
	}
	@Mixin(SkeletonModel.class)
	public static class SkeletonModelFixMixin<T extends SkeletonRenderState> extends HumanoidModel<T> {
		public SkeletonModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;isAggressive:Z"))
		public void injectGuardCheck(T skeletonRenderState, CallbackInfo ci) {
			if (!(skeletonRenderState.isAggressive && !skeletonRenderState.isHoldingBow) && skeletonRenderState.combatify$mobIsGuarding()) {
				boolean rightHanded = skeletonRenderState.mainArm == HumanoidArm.RIGHT;
				ModelPart arm = rightHanded ? leftArm : rightArm;
				ClientMethodHandler.animateBlockingBase(arm, rightHanded, skeletonRenderState.attackTime, head.xRot);
			}
		}
		@ModifyExpressionValue(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;isHoldingBow:Z"))
		public boolean injectGuardCheck(boolean original, @Local(ordinal = 0, argsOnly = true) T skeletonRenderState) {
			if (!original && skeletonRenderState.combatify$mobIsGuarding()) {
				ClientMethodHandler.animateBlockingZombieArms(leftArm, rightArm, skeletonRenderState.mainArm, skeletonRenderState.attackTime, head.xRot);
				AnimationUtils.bobArms(rightArm, leftArm, skeletonRenderState.ageInTicks);
				return true;
			}
			return original;
		}
	}
}
