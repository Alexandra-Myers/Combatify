package net.atlas.combatify.util.blocking.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ApplyEffect(HolderSet<MobEffect> toApply, LevelBasedValue minDuration, LevelBasedValue maxDuration, LevelBasedValue minAmplifier, LevelBasedValue maxAmplifier) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("apply_effect");
	public ApplyEffect(HolderSet<MobEffect> toApply, LevelBasedValue duration, LevelBasedValue amplifier) {
		this(toApply, duration, duration, amplifier, amplifier);
	}
	public static final MapCodec<ApplyEffect> PARTIAL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyEffect::toApply),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("duration").forGetter(ApplyEffect::maxDuration),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("amplifier").forGetter(ApplyEffect::minAmplifier))
			.apply(instance, ApplyEffect::new)
	);
	public static final MapCodec<ApplyEffect> FULL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyEffect::toApply),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("min_duration").forGetter(ApplyEffect::minDuration),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("max_duration").forGetter(ApplyEffect::maxDuration),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("min_amplifier").forGetter(ApplyEffect::minAmplifier),
				PostBlockEffects.LEVEL_BASED_VALUE_OR_CONSTANT_CODEC.fieldOf("max_amplifier").forGetter(ApplyEffect::maxAmplifier))
			.apply(instance, ApplyEffect::new)
	);
	public static final MapCodec<ApplyEffect> MAP_CODEC = Codec.mapEither(FULL_CODEC, PARTIAL_CODEC).xmap(
		Either::unwrap,
		Either::left
	);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
        assert enchantedItemInUse.owner() != null;
        @NotNull LivingEntity target = enchantedItemInUse.owner();
		RandomSource randomSource = target.getRandom();
		Optional<Holder<MobEffect>> optional = this.toApply.getRandomElement(randomSource);
		if (optional.isPresent()) {
			int duration = Math.round(Mth.randomBetween(randomSource, this.minDuration.calculate(enchantmentLevel), this.maxDuration.calculate(enchantmentLevel)) * 20.0F);
			int amp = Math.max(0, Math.round(Mth.randomBetween(randomSource, this.minAmplifier.calculate(enchantmentLevel), this.maxAmplifier.calculate(enchantmentLevel))));
			if (optional.get().value().isInstantenous()) {
				optional.get().value().applyInstantenousEffect(serverLevel, target, target, toApply, amp, 1);
				return;
			}
			toApply.addEffect(new MobEffectInstance(optional.get(), duration, amp));
		}
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
