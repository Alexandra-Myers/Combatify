package net.atlas.combatify.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ItemHasComponentPredicate(List<DataComponentType<?>> dataComponents, boolean anyOf) implements ItemSubPredicate {
	public static final Codec<ItemHasComponentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(DataComponentType.CODEC.listOf().fieldOf("components").forGetter(ItemHasComponentPredicate::dataComponents),
				Codec.BOOL.optionalFieldOf("any", true).forGetter(ItemHasComponentPredicate::anyOf))
				.apply(instance, ItemHasComponentPredicate::new)
	);

	@Override
	public boolean matches(ItemStack itemStack) {
		return anyOf ? dataComponents.stream().anyMatch(itemStack::has) : dataComponents.stream().allMatch(itemStack::has);
	}
}
