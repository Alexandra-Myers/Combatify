package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ServerPlayerExtensions;
import net.atlas.combatify.util.CombatUtil;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ServerPlayerExtensions {
	@Unique
	private boolean retainAttack;

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	@Shadow
	public ServerGamePacketListenerImpl connection;

	@Shadow
	public abstract Entity getCamera();

	@Unique
	public final ServerPlayer player = ServerPlayer.class.cast(this);

	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(method = "getEnchantedDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
	public float modTrident(ServerLevel serverLevel, ItemStack itemStack, Entity target, DamageSource damageSource, float f, Operation<Float> original) {
		return CustomEnchantmentHelper.modifyDamage(serverLevel, itemStack, target, damageSource, f, original);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void hitreg(CallbackInfo ci) {
		CombatUtil.setPosition((ServerPlayer)(Object)this);
		if (this.player.combatify$isAttackAvailable(-1.0F) && retainAttack && Combatify.unmoddedPlayers.contains(getUUID())) {
			retainAttack = false;
			combatify$customSwing(InteractionHand.MAIN_HAND);
			Entity entity = getCamera();
			if (entity == null)
				entity = this.player;
			Vec3 eyePos = entity.getEyePosition(1.0f);
			Vec3 viewVector = entity.getViewVector(1.0f);
			double reach = entityInteractionRange();
			double sqrReach = reach * reach;
			Vec3 adjPos = eyePos.add(viewVector.x * reach, viewVector.y * reach, viewVector.z * reach);
			AABB rayBB = entity.getBoundingBox().expandTowards(viewVector.scale(reach)).inflate(1.0, 1.0, 1.0);
			HitResult hitResult = entity.pick(reach, 1.0f, false);
			double i = hitResult.getLocation().distanceToSqr(eyePos);
			EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, eyePos, adjPos, rayBB, (entityx) -> !entityx.isSpectator() && entityx.isPickable(), sqrReach);
			if (entityHitResult != null && entityHitResult.getLocation().distanceToSqr(eyePos) < i)
				hitResult = entityHitResult;
			else
				hitResult = MethodHandler.redirectResult(player, hitResult);
			if (hitResult.getType() == HitResult.Type.ENTITY)
				connection.handleInteract(ServerboundInteractPacket.createAttackPacket(((EntityHitResult)hitResult).getEntity(), isShiftKeyDown()));
		}
	}
	@Inject(method = "swing", at = @At(value = "HEAD"), cancellable = true)
	public void removeReset(InteractionHand hand, CallbackInfo ci) {
		super.swing(hand);
		if (Combatify.unmoddedPlayers.contains(getUUID())) {
			if (Combatify.isPlayerAttacking.get(getUUID()))
				handleInteract();
			Combatify.isPlayerAttacking.put(getUUID(), true);
		}
		ci.cancel();
	}
	@Unique
	public void handleInteract() {
		if (retainAttack)
			return;
		if (!combatify$isAttackAvailable(0.0F)) {
			float var1 = this.player.getAttackStrengthScale(0.0F);
			if (var1 < 0.8F) {
				combatify$resetAttackStrengthTicker(!combatify$getMissedAttackRecovery());
				return;
			}

			if (var1 < 1.0F) {
				retainAttack = true;
				return;
			}
		}
		combatify$attackAir();
	}
	@Inject(method = "updatePlayerAttributes", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getAttribute(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;", ordinal = 1), cancellable = true)
	public void removeCreativeReach(CallbackInfo ci) {
		@Nullable final var attackRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		double chargedBonus;
		float strengthScale = player.getAttackStrengthScale(1.0F);
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
		if (attackRange != null) {
			Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			chargedBonus = item.getChargedAttackBonus();
			AttributeModifier modifier = new AttributeModifier(Combatify.CHARGED_REACH_ID, chargedBonus, AttributeModifier.Operation.ADD_VALUE);
			if (strengthScale > charge && !player.isCrouching() && Combatify.CONFIG.chargedReach())
				attackRange.addOrUpdateTransientModifier(modifier);
			else
				attackRange.removeModifier(modifier);
		}
		if (!Combatify.CONFIG.creativeAttackReach())
			ci.cancel();
	}

	public boolean combatify$isRetainingAttack() {
		return retainAttack;
	}

	public void combatify$setRetainAttack(boolean retain) {
		retainAttack = retain;
	}
}
