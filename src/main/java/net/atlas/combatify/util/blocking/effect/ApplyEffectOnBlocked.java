package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public record ApplyEffectOnBlocked(HolderSet<MobEffect> toApply, float minDuration, float maxDuration, float minAmplifier, float maxAmplifier) implements PostBlockEffect {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("apply_effect");
	public static final MapCodec<ApplyEffectOnBlocked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyEffectOnBlocked::toApply),
				Codec.FLOAT.fieldOf("min_duration").forGetter(ApplyEffectOnBlocked::minDuration),
				Codec.FLOAT.fieldOf("max_duration").forGetter(ApplyEffectOnBlocked::maxDuration),
				Codec.FLOAT.fieldOf("min_amplifier").forGetter(ApplyEffectOnBlocked::minAmplifier),
				Codec.FLOAT.fieldOf("max_amplifier").forGetter(ApplyEffectOnBlocked::maxAmplifier))
			.apply(instance, ApplyEffectOnBlocked::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ApplyEffectOnBlocked> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.MOB_EFFECT), ApplyEffectOnBlocked::toApply,
		ByteBufCodecs.FLOAT, ApplyEffectOnBlocked::minDuration,
		ByteBufCodecs.FLOAT, ApplyEffectOnBlocked::maxDuration,
		ByteBufCodecs.FLOAT, ApplyEffectOnBlocked::minAmplifier,
		ByteBufCodecs.FLOAT, ApplyEffectOnBlocked::maxAmplifier, ApplyEffectOnBlocked::new);

	@Override
	public void doEffect(ItemStack blockingItem, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		RandomSource randomSource = target.getRandom();
		Optional<Holder<MobEffect>> optional = this.toApply.getRandomElement(randomSource);
		if (optional.isPresent()) {
			int j = Math.round(Mth.randomBetween(randomSource, this.minDuration, this.maxDuration) * 20.0F);
			int k = Math.max(0, Math.round(Mth.randomBetween(randomSource, this.minAmplifier, this.maxAmplifier)));
			attacker.addEffect(new MobEffectInstance(optional.get(), j, k));
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

	public static void mapStreamCodec(Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect>> map) {
		map.put(ID, STREAM_CODEC.map(applyEffectOnBlocked -> applyEffectOnBlocked, blockingCondition -> (ApplyEffectOnBlocked) blockingCondition));
	}
}
