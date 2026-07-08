package net.atlas.combatify.component.generators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.util.blocking.DamageReduction;
import net.atlas.combatify.util.blocking.ItemDamageFunction;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BlocksAttacksGenerator(Optional<Float> blockDelaySeconds,
									 Optional<Float> disableCooldownScale,
									 List<DamageReduction> damageReductions,
									 Optional<ItemDamageFunction> itemDamage,
									 Optional<HolderSet<DamageType>> bypassedBy,
									 Optional<Holder<SoundEvent>> blockSound,
									 Optional<Holder<SoundEvent>> disableSound,
									 boolean appendReductions) implements PatchGenerator {
	public static final MapCodec<BlocksAttacksGenerator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("block_delay_seconds").forGetter(BlocksAttacksGenerator::blockDelaySeconds),
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("disable_cooldown_scale").forGetter(BlocksAttacksGenerator::disableCooldownScale),
				DamageReduction.CODEC
					.listOf()
					.optionalFieldOf("damage_reductions", List.of(new DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)))
					.forGetter(BlocksAttacksGenerator::damageReductions),
				ItemDamageFunction.CODEC.optionalFieldOf("item_damage").forGetter(BlocksAttacksGenerator::itemDamage),
				RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacksGenerator::bypassedBy), // Unsure if this is a suitable replacement ?
				SoundEvent.CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacksGenerator::blockSound),
				SoundEvent.CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacksGenerator::disableSound),
				Codec.BOOL.optionalFieldOf("append_reductions", true).forGetter(BlocksAttacksGenerator::appendReductions)
			)
			.apply(instance, BlocksAttacksGenerator::new)
	);
	@Override
	public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
		ExtendedBlockingData original = patchedDataComponentMap.get(CustomDataComponents.EXTENDED_BLOCKING_DATA.get());
		if (original != null) {
			List<DamageReduction> combined = new ArrayList<>(damageReductions);
			if (appendReductions) combined.addAll(original.baseBlocksAttacks().damageReductions());
			patchedDataComponentMap.set(CustomDataComponents.EXTENDED_BLOCKING_DATA.get(), original.withBlocksAttacks(blockDelaySeconds.orElse(original.baseBlocksAttacks().useSeconds()),
				disableCooldownScale.orElse(original.baseBlocksAttacks().disableCooldownScale()),
				combined,
				itemDamage.orElse(original.baseBlocksAttacks().itemDamage())));
		} else {
			patchedDataComponentMap.set(CustomDataComponents.EXTENDED_BLOCKING_DATA.get(), ExtendedBlockingData.VANILLA_SHIELD.withBlocksAttacks(blockDelaySeconds.orElse(1F),
				disableCooldownScale.orElse(1F),
				damageReductions,
				itemDamage.orElse(ItemDamageFunction.DEFAULT)));
		}
	}

	@Override
	public MapCodec<? extends PatchGenerator> codec() {
		return CODEC;
	}
}
