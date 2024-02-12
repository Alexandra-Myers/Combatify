package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

import static net.atlas.combatify.util.MethodHandler.getNewDamageAfterMagicAbsorb;

@Mixin(value = LivingEntity.class, priority = 1400)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {
	@Unique
	private double piercingNegation;

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	LivingEntity thisEntity = LivingEntity.class.cast(this);

	@Shadow
	public abstract double getAttributeValue(Holder<Attribute> attribute);

	@Shadow
	public abstract void hurtArmor(DamageSource damageSource, float v);

	@Shadow
	public abstract int getArmorValue();


	@Shadow
	public abstract boolean isDamageSourceBlocked(DamageSource damageSource);

	@Shadow
	protected int useItemRemaining;

	@Shadow
	public abstract ItemStack getUseItem();

	@SuppressWarnings("unused")
	@ModifyReturnValue(method = "isBlocking", at = @At(value="RETURN"))
	public boolean isBlocking(boolean original) {
		return !MethodHandler.getBlockingItem(thisEntity).isEmpty();
	}

	@Inject(method = "blockedByShield", at = @At(value="HEAD"), cancellable = true)
	public void blockedByShield(LivingEntity target, CallbackInfo ci) {
		double x = target.getX() - this.getX();
		double z = target.getZ() - this.getZ();
		double x2 = this.getX() - target.getX();
		double z2 = this.getZ() - target.getZ();
		Item blockingItem = MethodHandler.getBlockingItem(target).getItem();
		double piercingLevel = 0;
		Item item = thisEntity.getMainHandItem().getItem();
		piercingLevel += ((ItemExtensions)item).getPiercingLevel();
		if (Combatify.CONFIG.piercer())
			piercingLevel += CustomEnchantmentHelper.getPierce((LivingEntity) (Object) this) * 0.1;
		boolean bl = item instanceof AxeItem || piercingLevel > 0;
		ItemExtensions shieldItem = (ItemExtensions) blockingItem;
		if (bl && shieldItem.getBlockingType().canBeDisabled()) {
			if (piercingLevel > 0)
				((LivingEntityExtensions) target).setPiercingNegation(piercingLevel);
			float damage = (float) (Combatify.CONFIG.shieldDisableTime() + (float) CustomEnchantmentHelper.getChopping(thisEntity) * Combatify.CONFIG.cleavingDisableTime());
			if(Combatify.CONFIG.defender())
				damage -= (float) (CustomEnchantmentHelper.getDefense(target) * Combatify.CONFIG.defenderDisableReduction());
			if(target instanceof PlayerExtensions player)
				player.ctsShieldDisable(damage, blockingItem);
		}
		if(shieldItem.getBlockingType().isToolBlocker()) {
			ci.cancel();
			return;
		}
		MethodHandler.knockback(target, 0.5, x2, z2);
		MethodHandler.knockback(thisEntity, 0.5, x, z);
		ci.cancel();
	}
	@Override
	public void setPiercingNegation(double negation) {
		piercingNegation = negation;
	}
	@Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "HEAD"), cancellable = true)
	public void addPiercing(DamageSource source, float f, CallbackInfoReturnable<Float> cir) {
		if(source.getEntity() instanceof LivingEntity livingEntity && isSourceAnyOf(source, DamageTypes.PLAYER_ATTACK, DamageTypes.MOB_ATTACK_NO_AGGRO, DamageTypes.MOB_ATTACK)) {
			Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			double d = 0;
			d += ((ItemExtensions)item).getPiercingLevel();
			if (Combatify.CONFIG.piercer())
				d += CustomEnchantmentHelper.getPierce(livingEntity) * 0.1;
			d -= piercingNegation;
			d = Math.max(0, d);
			piercingNegation = 0;
			if(d > 0)
				cir.setReturnValue(combatify$getNewDamageAfterArmorAbsorb(source, f, d));
		}
	}
	@Inject(method = "getDamageAfterMagicAbsorb", at = @At(value = "HEAD"), cancellable = true)
	public void addPiercing1(DamageSource source, float f, CallbackInfoReturnable<Float> cir) {
		if(source.getEntity() instanceof LivingEntity livingEntity && isSourceAnyOf(source, DamageTypes.PLAYER_ATTACK, DamageTypes.MOB_ATTACK_NO_AGGRO, DamageTypes.MOB_ATTACK)) {
			Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			double d = 0;
			d += ((ItemExtensions)item).getPiercingLevel();
			if (Combatify.CONFIG.piercer())
				d += CustomEnchantmentHelper.getPierce(livingEntity) * 0.1;
			d -= piercingNegation;
			d = Math.max(0, d);
			piercingNegation = 0;
			if(d > 0)
				cir.setReturnValue(getNewDamageAfterMagicAbsorb(thisEntity, source, f, d));
		}
	}
	@Unique
	@SafeVarargs
	public final boolean isSourceAnyOf(DamageSource source, ResourceKey<DamageType>... damageTypes) {
		boolean bl = false;
		for(ResourceKey<DamageType> damageType : damageTypes) {
			bl |= source.is(damageType);
		}
		return bl;
	}
	@ModifyConstant(method = "handleDamageEvent", constant = @Constant(intValue = 20, ordinal = 0))
	private int syncInvulnerability(int x) {
		return 10;
	}

	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
	public boolean shield(LivingEntity instance, DamageSource source, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount, @Local(ordinal = 1) LocalFloatRef f, @Local(ordinal = 2) LocalFloatRef g, @Local(ordinal = 0) LocalBooleanRef bl) {
		if (amount.get() > 0.0F && isDamageSourceBlocked(source)) {
			if(MethodHandler.getBlockingItem(thisEntity).getItem() instanceof ItemExtensions shieldItem) {
				shieldItem.getBlockingType().block(instance, null, MethodHandler.getBlockingItem(thisEntity), source, amount, f, g, bl);
			}
		}
		return false;
	}
	@ModifyConstant(method = "hurt", constant = @Constant(intValue = 20, ordinal = 0))
	public int changeIFrames(int constant, @Local(ordinal = 0, argsOnly = true) final DamageSource source, @Local(ordinal = 0, argsOnly = true) final float amount) {
		Entity entity2 = source.getEntity();
		int invulnerableTime = 10;
		if (entity2 instanceof Player player) {
			int base = (int) Math.min(player.getCurrentItemAttackStrengthDelay(), invulnerableTime);
			invulnerableTime = base >= 4 ? base - 2 : base;
			if(player.getAttributeValue(Attributes.ATTACK_SPEED) - 1.5 >= 15 || Combatify.CONFIG.instaAttack())
				invulnerableTime = 5;
		}

		if (source.is(DamageTypeTags.IS_PROJECTILE) && !Combatify.CONFIG.projectilesHaveIFrames()) {
			invulnerableTime = 0;
		}

		if (source.is(DamageTypes.MAGIC) && !Combatify.CONFIG.magicHasIFrames()) {
			invulnerableTime = 0;
		}
		return invulnerableTime;

	}
	@Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", ordinal = 0))
	public void injectEatingInterruption(DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = source.getEntity();
		if (entity instanceof LivingEntity) {
			if (thisEntity.isUsingItem() && (getUseItem().getUseAnimation() == UseAnim.EAT || getUseItem().getUseAnimation() == UseAnim.DRINK) && Combatify.CONFIG.eatingInterruption()) {
				useItemRemaining = thisEntity.getUseItem().getUseDuration();
				if (level() instanceof ServerLevel serverLevel)
					for (UUID playerUUID : Combatify.moddedPlayers)
						if (serverLevel.getPlayerByUUID(playerUUID) instanceof ServerPlayer serverPlayer)
							ServerPlayNetworking.send(serverPlayer, new NetworkingHandler.RemainingUseSyncPacket(getId(), useItemRemaining));
			}
		}
	}
	@ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "floatValue=10.0F", ordinal = 0))
	public float changeIFrames(float constant) {
		return constant - 10;
	}
	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void modifyKB(LivingEntity instance, double d, double e, double f, @Local(ordinal = 0, argsOnly = true) final DamageSource source) {
		if ((Combatify.CONFIG.fishingHookKB() && source.getDirectEntity() instanceof FishingHook) || (!source.is(DamageTypeTags.IS_PROJECTILE) && Combatify.CONFIG.midairKB())) {
			MethodHandler.projectileKnockback(thisEntity, 0.4, e, f);
		} else {
			MethodHandler.knockback(thisEntity, 0.4, e, f);
		}
	}

	@Inject(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;"), cancellable = true)
	public void isDamageSourceBlocked(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		Vec3 currentVector = this.getViewVector(1.0F);
		if (currentVector.y > -0.99 && currentVector.y < 0.99) {
			currentVector = (new Vec3(currentVector.x, 0.0, currentVector.z)).normalize();
			Vec3 sourceVector = Objects.requireNonNull(source.getSourcePosition()).vectorTo(this.position());
			sourceVector = (new Vec3(sourceVector.x, 0.0, sourceVector.z)).normalize();
			cir.setReturnValue(sourceVector.dot(currentVector) * 3.1415927410125732 < -0.8726646304130554);
			return;
		}
		cir.setReturnValue(false);
	}
	@Unique
	public float combatify$getNewDamageAfterArmorAbsorb(DamageSource source, float amount, double piercingLevel) {
		if (!source.is(DamageTypeTags.BYPASSES_ARMOR) && piercingLevel < 1) {
			hurtArmor(source, (float) (amount * (1 + piercingLevel)));
			double armourStrength = getArmorValue();
			double toughness = getAttributeValue(Attributes.ARMOR_TOUGHNESS);
			amount = CombatRules.getDamageAfterAbsorb(amount, (float) (armourStrength - (armourStrength * piercingLevel)), (float) (toughness - (toughness * piercingLevel)));
		}

		return amount;
	}
	@Override
	public boolean hasEnabledShieldOnCrouch() {
		return true;
	}

	@Override
	public void setUseItemRemaining(int ticks) {
		this.useItemRemaining = ticks;
	}
}
