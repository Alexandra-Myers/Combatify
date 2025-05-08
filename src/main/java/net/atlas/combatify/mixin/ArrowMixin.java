package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(Arrow.class)
public abstract class ArrowMixin extends AbstractArrow {
	protected ArrowMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(method = "doPostHurtEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;F)V"))
	public void applyInstantaneousEffect(PotionContents instance, Consumer<MobEffectInstance> consumer, float f, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) LivingEntity livingEntity) {
		Arrow arrow = Arrow.class.cast(this);
		original.call(instance, (Consumer<MobEffectInstance>) effectInstance -> {
			if (effectInstance.getEffect().value().isInstantenous() && livingEntity.level() instanceof ServerLevel serverLevel) {
				effectInstance.getEffect().value().applyInstantenousEffect(serverLevel, arrow, getEffectSource(), livingEntity, effectInstance.getAmplifier(), f);
				return;
			}
			consumer.accept(effectInstance);
		}, f);
	}
}
