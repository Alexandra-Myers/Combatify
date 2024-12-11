package net.atlas.combatify.mixin;

import net.atlas.combatify.util.ClientMethodHandler;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


public class ModelFixMixins {
	@Mixin(AbstractZombieModel.class)
	public static class AbstractZombieModelFixMixin<T extends Monster> {
		@Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(T monster, float f, float g, float h, float i, float j, CallbackInfo ci) {
			if (MethodHandler.isMobGuarding(monster)) ClientMethodHandler.guarding = true;
		}
	}
	@Mixin(IllagerModel.class)
	public static class IllagerModelFixMixin<T extends AbstractIllager> {
		@Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(T abstractIllager, float f, float g, float h, float i, float j, CallbackInfo ci) {
			if (MethodHandler.isMobGuarding(abstractIllager)) ClientMethodHandler.guarding = true;
		}
	}
	@Mixin(PiglinModel.class)
	public static class PiglinModelFixMixin<T extends Mob> {
		@Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(T mob, float f, float g, float h, float i, float j, CallbackInfo ci) {
			if (MethodHandler.isMobGuarding(mob)) ClientMethodHandler.guarding = true;
		}
	}
	@Mixin(ZombieVillagerModel.class)
	public static class ZombieVillagerModelFixMixin<T extends Zombie> {
		@Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Zombie;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V"))
		public void injectGuardCheck(T zombie, float f, float g, float h, float i, float j, CallbackInfo ci) {
			if (MethodHandler.isMobGuarding(zombie)) ClientMethodHandler.guarding = true;
		}
	}
}
