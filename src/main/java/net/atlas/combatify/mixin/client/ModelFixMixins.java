package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.client.ClientMethodHandler;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ModelFixMixins {
	@Mixin(AbstractZombieModel.class)
	public static class AbstractZombieModelFixMixin<T extends Monster> extends HumanoidModel<T> {
		public AbstractZombieModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@WrapOperation(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, float f, float g, Operation<Void> original, @Local(argsOnly = true) T monster) {
			if (MethodHandler.isMobGuarding(monster)) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, monster, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, f, g);
		}
	}
	@Mixin(IllagerModel.class)
	public static class IllagerModelFixMixin<T extends AbstractIllager> {
		@Shadow
		@Final
		private ModelPart head;

		@WrapOperation(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, float f, float g, Operation<Void> original, @Local(argsOnly = true) T abstractIllager) {
			if (MethodHandler.isMobGuarding(abstractIllager)) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, abstractIllager, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, f, g);
		}
		@WrapOperation(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;swingWeaponDown(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/world/entity/Mob;FF)V"))
		public void injectGuardCheck1(ModelPart modelPart, ModelPart modelPart2, Mob mob, float f, float g, Operation<Void> original) {
			if (MethodHandler.isMobGuarding(mob)) {
				ClientMethodHandler.swingWeaponDown(modelPart, modelPart2, mob, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, mob, f, g);
		}
	}
	@Mixin(PiglinModel.class)
	public static class PiglinModelFixMixin<T extends Mob> extends PlayerModel<T> {

		public PiglinModelFixMixin(ModelPart modelPart, boolean bl) {
			super(modelPart, bl);
		}

		@WrapOperation(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, float f, float g, Operation<Void> original, @Local(argsOnly = true) T mob) {
			if (MethodHandler.isMobGuarding(mob)) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, mob, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, f, g);
		}
		@WrapOperation(method = "setupAttackAnimation(Lnet/minecraft/world/entity/Mob;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;swingWeaponDown(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/world/entity/Mob;FF)V"))
		public void injectGuardCheck1(ModelPart modelPart, ModelPart modelPart2, Mob mob, float f, float g, Operation<Void> original) {
			if (MethodHandler.isMobGuarding(mob)) {
				ClientMethodHandler.swingWeaponDown(modelPart, modelPart2, mob, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, mob, f, g);
		}
		@Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinArmPose;DANCING:Lnet/minecraft/world/entity/monster/piglin/PiglinArmPose;"))
		public void injectGuardCheck1(T mob, float f, float g, float h, float i, float j, CallbackInfo ci, @Local(ordinal = 0) PiglinArmPose piglinArmPose) {
			if ((piglinArmPose == PiglinArmPose.DEFAULT || (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && attackTime == 0.0F)) && MethodHandler.isMobGuarding(mob)) {
				boolean rightHanded = !mob.isLeftHanded();
				ModelPart arm = rightHanded ? leftArm : rightArm;
				ClientMethodHandler.animateBlockingBase(arm, rightHanded, this.attackTime, head.xRot);
			}
		}
	}
	@Mixin(ZombieVillagerModel.class)
	public static class ZombieVillagerModelFixMixin<T extends Zombie> extends HumanoidModel<T> {
		public ZombieVillagerModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@WrapOperation(method = "setupAnim(Lnet/minecraft/world/entity/monster/Zombie;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(ModelPart modelPart, ModelPart modelPart2, boolean bl, float f, float g, Operation<Void> original, @Local(argsOnly = true) T zombie) {
			if (MethodHandler.isMobGuarding(zombie)) {
				ClientMethodHandler.animateZombieArms(modelPart, modelPart2, zombie, f, g, head.xRot);
				return;
			}
			original.call(modelPart, modelPart2, bl, f, g);
		}
	}
	@Mixin(SkeletonModel.class)
	public static class SkeletonModelFixMixin<T extends Mob & RangedAttackMob> extends HumanoidModel<T> {
		public SkeletonModelFixMixin(ModelPart modelPart) {
			super(modelPart);
		}

		@Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;isAggressive()Z"))
		public void injectGuardCheck(T mob, float f, float g, float h, float i, float j, CallbackInfo ci) {
			ItemStack itemStack = mob.getMainHandItem();
			if (!(mob.isAggressive() && (itemStack.isEmpty() || !itemStack.is(Items.BOW))) && MethodHandler.isMobGuarding(mob)) {
				boolean rightHanded = mob.getMainArm() == HumanoidArm.RIGHT;
				ModelPart arm = rightHanded ? leftArm : rightArm;
				ClientMethodHandler.animateBlockingBase(arm, rightHanded, attackTime, head.xRot);
			}
		}
		@ModifyExpressionValue(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
		public ItemStack injectGuardCheck(ItemStack original, @Local(ordinal = 0, argsOnly = true) T mob, @Local(ordinal = 2, argsOnly = true) float ageInTicks) {
			if ((original.isEmpty() || !original.is(Items.BOW)) && MethodHandler.isMobGuarding(mob)) {
				ClientMethodHandler.animateZombieArms(leftArm, rightArm, mob, attackTime, ageInTicks, head.xRot);
				return Items.BOW.getDefaultInstance();
			}
			return original;
		}
	}
}
