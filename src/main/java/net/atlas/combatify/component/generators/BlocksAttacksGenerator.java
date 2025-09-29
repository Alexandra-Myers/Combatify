package net.atlas.combatify.component.generators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BlocksAttacksGenerator(Optional<Float> blockDelaySeconds,
									 Optional<Float> disableCooldownScale,
									 List<BlocksAttacks.DamageReduction> damageReductions,
									 Optional<BlocksAttacks.ItemDamageFunction> itemDamage,
									 Optional<TagKey<DamageType>> bypassedBy,
									 Optional<Holder<SoundEvent>> blockSound,
									 Optional<Holder<SoundEvent>> disableSound,
									 boolean appendReductions) implements PatchGenerator {
	public static final MapCodec<BlocksAttacksGenerator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("block_delay_seconds").forGetter(BlocksAttacksGenerator::blockDelaySeconds),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_cooldown_scale").forGetter(BlocksAttacksGenerator::disableCooldownScale),
				BlocksAttacks.DamageReduction.CODEC
					.listOf()
					.optionalFieldOf("damage_reductions", List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)))
					.forGetter(BlocksAttacksGenerator::damageReductions),
				BlocksAttacks.ItemDamageFunction.CODEC.optionalFieldOf("item_damage").forGetter(BlocksAttacksGenerator::itemDamage),
				TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacksGenerator::bypassedBy),
				SoundEvent.CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacksGenerator::blockSound),
				SoundEvent.CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacksGenerator::disableSound),
				Codec.BOOL.optionalFieldOf("append_reductions", true).forGetter(BlocksAttacksGenerator::appendReductions)
			)
			.apply(instance, BlocksAttacksGenerator::new)
	);
	@Override
	public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
		BlocksAttacks original = patchedDataComponentMap.get(DataComponents.BLOCKS_ATTACKS);
		if (original != null) {
			List<BlocksAttacks.DamageReduction> combined = new ArrayList<>(damageReductions);
			if (appendReductions) combined.addAll(original.damageReductions());
			patchedDataComponentMap.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(blockDelaySeconds.orElse(original.blockDelaySeconds()),
				disableCooldownScale.orElse(original.disableCooldownScale()),
				combined,
				itemDamage.orElse(original.itemDamage()),
				bypassedBy.or(original::bypassedBy),
				blockSound.or(original::blockSound),
				disableSound.or(original::disableSound)));
		} else {
			patchedDataComponentMap.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(blockDelaySeconds.orElse(1F),
				disableCooldownScale.orElse(1F),
				damageReductions,
				itemDamage.orElse(BlocksAttacks.ItemDamageFunction.DEFAULT),
				bypassedBy,
				blockSound,
				disableSound));
		}
	}

	@Override
	public MapCodec<? extends PatchGenerator> codec() {
		return CODEC;
	}
}
