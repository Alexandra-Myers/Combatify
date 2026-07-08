package net.atlas.combatify.util.blocking;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public record DamageReduction(float horizontalBlockingAngle, Optional<HolderSet<DamageType>> type, float base, float factor) {
	public static final DamageReduction DEFAULT = new DamageReduction(90, Optional.empty(), 0, 1);
	public static final Codec<DamageReduction> CODEC = RecordCodecBuilder.create(
		i -> i.group(
				ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("horizontal_blocking_angle", 90.0F).forGetter(DamageReduction::horizontalBlockingAngle),
				RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageReduction::type),
				Codec.FLOAT.fieldOf("base").forGetter(DamageReduction::base),
				Codec.FLOAT.fieldOf("factor").forGetter(DamageReduction::factor)
			)
			.apply(i, DamageReduction::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, DamageReduction> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT,
		DamageReduction::horizontalBlockingAngle,
		ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional),
		DamageReduction::type,
		ByteBufCodecs.FLOAT,
		DamageReduction::base,
		ByteBufCodecs.FLOAT,
		DamageReduction::factor,
		DamageReduction::new
	);

	public float resolve(final DamageSource source, final float dealtDamage, final double angle) {
		if (angle > (float) (Math.PI / 180.0) * this.horizontalBlockingAngle) {
			return 0.0F;
		} else {
			return this.type.isPresent() && !this.type.get().contains(source.typeHolder())
				? 0.0F
				: Mth.clamp(this.base + this.factor * dealtDamage, 0.0F, dealtDamage);
		}
	}
}
