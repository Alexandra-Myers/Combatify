package net.atlas.combatify.client;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;
import static net.minecraft.client.model.AnimationUtils.bobArms;

@Environment(EnvType.CLIENT)
public class ClientMethodHandler {
	public static HitResult redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return null;
		Minecraft minecraft = Minecraft.getInstance();
		if (Combatify.CONFIG.swingThroughGrass() && instance.getType() == HitResult.Type.BLOCK && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			Player minecraftPlayer = Objects.requireNonNull(minecraft.player);
			Entity player = Objects.requireNonNull(minecraft.getCameraEntity());
			double reach = MethodHandler.getCurrentAttackReachWithoutChargedReach(minecraftPlayer) + ((Combatify.CONFIG.chargedReach() && !player.isCrouching()) ? 1.25 : 0.25);
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, reach);
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null) {
				double dist = player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				reach *= reach;
				if (dist > reach)
					return null;
				double enemyDistance = player.distanceTo(entity);
				HitResult newResult = pickCollisions(player, enemyDistance);
				if (newResult.getType() != HitResult.Type.MISS)
					return null;
				return rayTraceResult;
			}
		}
		return null;
	}
	public static void animateBlockingBase(ModelPart arm, boolean rightHanded, float attackTime, float headXRot) {
		float h = Mth.sin(attackTime * 3.1415927F);
		float i = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * 3.1415927F);
		arm.yRot = (!rightHanded ? -30.0F : 30.0F) * 0.017453292F - (0.1F - h * 0.6F);
		arm.xRot = arm.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
		arm.xRot += h * 1.2F - i * 0.4F;
	}
	public static <T extends Mob> void swingWeaponDown(ModelPart rightArm, ModelPart leftArm, T mob, float attackTime, float ageInTicks, float headXRot) {
		float h = Mth.sin(attackTime * 3.1415927F);
		float i = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * 3.1415927F);
		rightArm.zRot = 0.0F;
		leftArm.zRot = 0.0F;
		if (mob.getMainArm() == HumanoidArm.RIGHT) {
			rightArm.yRot = 0.15707964F;
			leftArm.yRot = 30.0F * 0.017453292F - 0.15707964F;
			rightArm.xRot = -1.8849558F + Mth.cos(ageInTicks * 0.09F) * 0.15F;
			leftArm.xRot = leftArm.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
			rightArm.xRot += h * 2.2F - i * 0.4F;
			leftArm.xRot += h * 1.2F - i * 0.4F;
		} else {
			rightArm.yRot = -30.0F * 0.017453292F + 0.15707964F;
			leftArm.yRot = -0.15707964F;
			rightArm.xRot = rightArm.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
			leftArm.xRot = -1.8849558F + Mth.cos(ageInTicks * 0.09F) * 0.15F;
			rightArm.xRot += h * 1.2F - i * 0.4F;
			leftArm.xRot += h * 2.2F - i * 0.4F;
		}

		bobArms(rightArm, leftArm, ageInTicks);
	}
	public static <T extends Mob> void animateZombieArms(ModelPart leftArm, ModelPart rightArm, T mob, float attackTime, float ageInTicks, float headXRot) {
		float h = Mth.sin(attackTime * 3.1415927F);
		float i = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * 3.1415927F);
		rightArm.zRot = 0.0F;
		leftArm.zRot = 0.0F;
		boolean isRightHanded = mob.getMainArm() == HumanoidArm.RIGHT;
		ModelPart modelPart3 = isRightHanded ? rightArm : leftArm;
		ModelPart modelPart4 = isRightHanded ? leftArm : rightArm;
		modelPart3.yRot = -(0.1F - h * 0.6F);
		modelPart4.yRot = (!isRightHanded ? -30.0F : 30.0F) * 0.017453292F + (0.1F - h * 0.6F);
		modelPart3.xRot = (float) (-Math.PI / 2.25F);
		modelPart4.xRot = modelPart4.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
		rightArm.xRot += h * 1.2F - i * 0.4F;
		leftArm.xRot += h * 1.2F - i * 0.4F;
		bobArms(rightArm, leftArm, ageInTicks);
	}
}
