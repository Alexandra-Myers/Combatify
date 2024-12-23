package net.atlas.combatify.client;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;
import static net.minecraft.client.model.AnimationUtils.bobArms;

@Environment(EnvType.CLIENT)
public class ClientMethodHandler {
	public static HitResult redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return null;
		Minecraft minecraft = Minecraft.getInstance();
		if (Combatify.CONFIG.swingThroughGrass() && instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			Level level = Objects.requireNonNull(minecraft.level);
			Player minecraftPlayer = Objects.requireNonNull(minecraft.player);
			Entity player = Objects.requireNonNull(minecraft.getCameraEntity());
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, getCurrentAttackReach(minecraftPlayer, 0.0F));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				double dist = player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				double reach = MethodHandler.getCurrentAttackReach(minecraftPlayer, 1.0F);
				reach *= reach;
				if (!minecraftPlayer.hasLineOfSight(entity))
					reach = 6.25;
				if (dist > reach)
					return null;
				double enemyDistance = player.distanceTo(entity);
				List<BlockPos> blockPosList = pickFromPos(player, enemyDistance);
				if (!blockPosList.isEmpty())
					return null;
				return rayTraceResult;
			}
		}
		return null;
	}
	public static void animateBlockingBase(ModelPart arm, boolean rightHanded, float f, float headXRot) {
		float h = Mth.sin(f * 3.1415927F);
		float i = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * 3.1415927F);
		arm.yRot = (!rightHanded ? -30.0F : 30.0F) * 0.017453292F - (0.1F - h * 0.6F);
		arm.xRot = arm.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
		arm.xRot += h * 1.2F - i * 0.4F;
	}
	public static void swingWeaponDown(ModelPart modelPart, ModelPart modelPart2, HumanoidArm humanoidArm, float f, float g, float headXRot) {
		float h = Mth.sin(f * 3.1415927F);
		float i = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * 3.1415927F);
		modelPart.zRot = 0.0F;
		modelPart2.zRot = 0.0F;
		if (humanoidArm == HumanoidArm.RIGHT) {
			modelPart.yRot = 0.15707964F;
			modelPart2.yRot = 30.0F * 0.017453292F - 0.15707964F;
			modelPart.xRot = -1.8849558F + Mth.cos(g * 0.09F) * 0.15F;
			modelPart2.xRot = modelPart2.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
			modelPart.xRot += h * 2.2F - i * 0.4F;
			modelPart2.xRot += h * 1.2F - i * 0.4F;
		} else {
			modelPart.yRot = -30.0F * 0.017453292F + 0.15707964F;
			modelPart2.yRot = -0.15707964F;
			modelPart.xRot = modelPart.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
			modelPart2.xRot = -1.8849558F + Mth.cos(g * 0.09F) * 0.15F;
			modelPart.xRot += h * 1.2F - i * 0.4F;
			modelPart2.xRot += h * 2.2F - i * 0.4F;
		}

		bobArms(modelPart, modelPart2, g);
	}
	public static void animateZombieArms(ModelPart modelPart, ModelPart modelPart2, HumanoidArm humanoidArm, float f, float g, float headXRot) {
		float h = Mth.sin(f * 3.1415927F);
		float i = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * 3.1415927F);
		modelPart2.zRot = 0.0F;
		modelPart.zRot = 0.0F;
		boolean isRightHanded = humanoidArm == HumanoidArm.RIGHT;
		ModelPart modelPart3 = isRightHanded ? modelPart2 : modelPart;
		ModelPart modelPart4 = isRightHanded ? modelPart : modelPart2;
		modelPart3.yRot = -(0.1F - h * 0.6F);
		modelPart4.yRot = (!isRightHanded ? -30.0F : 30.0F) * 0.017453292F + (0.1F - h * 0.6F);
		modelPart3.xRot = (float) (-Math.PI / 2.25F);
		modelPart4.xRot = modelPart4.xRot * 0.5F - 0.9424779F + Mth.clamp(headXRot, -1.3962634F, 0.43633232F);
		modelPart2.xRot += h * 1.2F - i * 0.4F;
		modelPart.xRot += h * 1.2F - i * 0.4F;
		bobArms(modelPart2, modelPart, g);
	}
}
