package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {

	@Shadow
	@Final
	private static EntityDataAccessor<Byte> ID_LOYALTY;

	@Shadow
	private ItemStack tridentItem;

	@Shadow
	private boolean dealtDamage;

	protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void injectVoidReturnLogic(CallbackInfo ci) {
		ThrownTrident trident = ((ThrownTrident) (Object)this);
		int j = trident.entityData.get(ID_LOYALTY);
		if(trident.getY() <= -65) {
			if (!trident.isAcceptibleReturnOwner()) {
				trident.discard();
			}else {
				trident.setNoPhysics(true);
				Vec3 vec3 = trident.getEyePosition().subtract(trident.position());
				trident.setPosRaw(trident.getX(), trident.getY() + vec3.y * 0.015 * j, trident.getZ());
				if (trident.level.isClientSide) {
					trident.yOld = trident.getY();
				}

				double d = 0.05 * j;
				trident.setDeltaMovement(trident.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d)));
				if (trident.clientSideReturnTridentTickCount == 0) {
					trident.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
				}

				++trident.clientSideReturnTridentTickCount;
			}
		}
	}
	@Inject(method = "onHitEntity", at = @At(value = "HEAD"))
	public void injectVoidReturnLogic(EntityHitResult entityHitResult, CallbackInfo ci) {
		ThrownTrident trident = ((ThrownTrident) (Object)this);
		Entity entity = entityHitResult.getEntity();
		float f = 7.0F;
		EnchantmentHelper helper = new EnchantmentHelper();
		if (entity instanceof LivingEntity livingEntity) {
			f += ((IEnchantmentHelper)helper).getDamageBonus(this.tridentItem, livingEntity);
		}

		Entity entity2 = trident.getOwner();
		DamageSource damageSource = DamageSource.trident(trident, (Entity)(entity2 == null ? trident : entity2));
		dealtDamage = true;
		SoundEvent soundEvent = SoundEvents.TRIDENT_HIT;
		if (entity.hurt(damageSource, f)) {
			if (entity.getType() == EntityType.ENDERMAN) {
				return;
			}

			if (entity instanceof LivingEntity livingEntity2) {
				if (entity2 instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects(livingEntity2, entity2);
					EnchantmentHelper.doPostDamageEffects((LivingEntity)entity2, livingEntity2);
				}

				this.doPostHurtEffects(livingEntity2);
			}
		}

		trident.setDeltaMovement(trident.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
		float g = 1.0F;
		if (trident.level instanceof ServerLevel && trident.level.isThundering() && trident.isChanneling()) {
			BlockPos blockPos = entity.blockPosition();
			if (trident.level.canSeeSky(blockPos)) {
				LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(trident.level);
				lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
				lightningBolt.setCause(entity2 instanceof ServerPlayer ? (ServerPlayer)entity2 : null);
				trident.level.addFreshEntity(lightningBolt);
				soundEvent = SoundEvents.TRIDENT_THUNDER;
				g = 5.0F;
			}
		}

		trident.playSound(soundEvent, g, 1.0F);
	}

}
