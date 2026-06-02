package net.atlas.combatify.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ItemHasComponentPredicate(List<DataComponentType<?>> dataComponents, boolean anyOf) implements DataComponentPredicate {
	public static final Codec<ItemHasComponentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(DataComponentType.CODEC.listOf().fieldOf("components").forGetter(ItemHasComponentPredicate::dataComponents),
				Codec.BOOL.optionalFieldOf("any", true).forGetter(ItemHasComponentPredicate::anyOf))
				.apply(instance, ItemHasComponentPredicate::new)
	);

	@Override
	public boolean matches(@NotNull DataComponentGetter dataComponentGetter) {
		return anyOf ? dataComponents.stream().anyMatch(dataComponentType -> dataComponentGetter.get(dataComponentType) != null) : dataComponents.stream().allMatch(dataComponentType -> dataComponentGetter.get(dataComponentType) != null);
	}
}
