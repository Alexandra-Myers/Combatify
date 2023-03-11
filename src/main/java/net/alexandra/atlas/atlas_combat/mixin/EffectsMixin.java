package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MobEffects.class)
public class EffectsMixin {

	@ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 3.0, ordinal = 0))
	private static double modifyDamageBoost(double original) {
		return 0.2;
	}

	@ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 0.0, ordinal = 0))
	private static double modifyDamageBoostModifier(double original) {
		return 0.2;
	}

	@ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = -4.0, ordinal = 0))
	private static double modifyWeakness(double original) {
		return -0.2;
	}

	@ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 0.0, ordinal = 1))
	private static double modifyWeaknessModifier(double original) {
		return -0.2;
	}
}
