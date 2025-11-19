package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.atlas.combatify.util.MethodHandler.voidReturnLogic;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {

	@Shadow
	@Final
	private static EntityDataAccessor<Byte> ID_LOYALTY;

	@Shadow
	private boolean dealtDamage;

	protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void injectVoidReturnLogic(CallbackInfo ci) {
		voidReturnLogic(ThrownTrident.class.cast(this), ID_LOYALTY);
	}
	@WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
	public float dealtDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f, Operation<Float> original) {
		return CustomEnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, f, original);
	}
	@ModifyExpressionValue(method = "onHitEntity", at = @At(value = "CONSTANT", args = "floatValue=8.0"))
	public float editTridentDamage(float original) {
		float diff = (float) (original - Combatify.CONFIG.thrownTridentDamage());
		return original - diff;
	}
	@Inject(method = "onHitEntity", at = @At("TAIL"))
	public void removeDealtDamage(EntityHitResult entityHitResult, CallbackInfo ci) {
		if (Combatify.CONFIG.disableLoyaltyOnHitEntity()) dealtDamage = false;
	}
}
