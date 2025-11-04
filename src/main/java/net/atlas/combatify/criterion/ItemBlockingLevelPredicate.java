package net.atlas.combatify.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;

public record ItemBlockingLevelPredicate(MinMaxBounds.Ints value) implements SingleComponentItemPredicate<Integer> {
	public static final Codec<ItemBlockingLevelPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("value", MinMaxBounds.Ints.ANY).forGetter(ItemBlockingLevelPredicate::value))
			.apply(instance, ItemBlockingLevelPredicate::new)
	);

	@Override
	public DataComponentType<Integer> componentType() {
		return CustomDataComponents.BLOCKING_LEVEL;
	}

	@Override
	public boolean matches(Integer object) {
		return value.matches(object);
	}
}
