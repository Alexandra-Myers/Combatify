package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Arrow.class)
public abstract class ArrowMixin extends AbstractArrow {
	protected ArrowMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(method = "doPostHurtEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
	public boolean applyInstantaneousEffect(LivingEntity instance, MobEffectInstance mobEffectInstance, Entity entity, Operation<Boolean> original, @Local(ordinal = 0) MobEffectInstance effectInstance) {
        if (effectInstance.getEffect().value().isInstantenous()) {
			effectInstance.getEffect().value().applyInstantenousEffect(this, getEffectSource(), instance, effectInstance.getAmplifier(), Combatify.CONFIG.instantTippedArrowEffectMultiplier());
			return true;
		}
		return original.call(instance, mobEffectInstance, entity);
	}
}
