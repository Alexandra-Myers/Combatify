package net.atlas.combatify.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record ItemBlockingLevelPredicate(MinMaxBounds.Ints value) implements SingleComponentItemPredicate<Integer> {
	public static final Codec<ItemBlockingLevelPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("value", MinMaxBounds.Ints.ANY).forGetter(ItemBlockingLevelPredicate::value))
			.apply(instance, ItemBlockingLevelPredicate::new)
	);

	@Override
	public DataComponentType<Integer> componentType() {
		return CustomDataComponents.BLOCKING_LEVEL.get();
	}

	@Override
	public boolean matches(ItemStack itemStack, Integer object) {
		return value.matches(object);
	}
}
