package net.atlas.combatify.util.blocking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.util.blocking.condition.BlockingCondition;
import net.atlas.combatify.util.blocking.condition.BlockingConditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record ComponentModifier(Component tooltipComponent, EnchantmentValueEffect modifier, Optional<BlockingCondition> showInTooltip) {
	public static final Codec<ComponentModifier> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(ComponentSerialization.CODEC.fieldOf("tooltip").forGetter(ComponentModifier::tooltipComponent),
				EnchantmentValueEffect.CODEC.fieldOf("modifier").forGetter(ComponentModifier::modifier),
				BlockingConditions.MAP_CODEC.codec().optionalFieldOf("show_in_tooltip").forGetter(ComponentModifier::showInTooltip))
			.apply(instance, ComponentModifier::new));

	public float modifyValue(float value, int blockingLevel, RandomSource randomSource) {
		return modifier.process(blockingLevel, randomSource, value);
	}
	public boolean matches(ItemStack itemStack) {
		return showInTooltip.isEmpty() || showInTooltip.get().appliesComponentModifier(itemStack);
	}
	public static Component buildComponent(Component base, float val) {
		if (base.copy().getContents() instanceof TranslatableContents translatableContents) {
			Object[] args = {ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(val)};
			args = Arrays.copyOf(args, translatableContents.getArgs().length + 1);
			int i = 1;
			for (Object o : translatableContents.getArgs()) {
				if (i >= args.length) break;
				args[i] = o;
				i++;
			}
			TranslatableContents copiedContents = new TranslatableContents(translatableContents.getKey(), translatableContents.getFallback(), args);
			return MutableComponent.create(copiedContents);
		} else return base;
	}
	public List<Component> tryCombine(List<ComponentModifier> others, int blockingLevel, RandomSource randomSource) {
		List<Component> res = new ArrayList<>();
		float val = 0;
		for (int i = others.size() - 1; i >= 0; i--) {
			ComponentModifier other = others.get(i);
			if (tooltipComponent.equals(other.tooltipComponent)) {
				val = other.modifyValue(val, blockingLevel, randomSource);
				others.remove(i);
			}
		}
		res.add(buildComponent(tooltipComponent, val));
		if (!others.isEmpty()) others.getFirst().tryCombine(others, blockingLevel, randomSource);
		return res;
	}

	public static ConditionalEffect<ComponentModifier> matchingConditions(Component tooltipComponent, EnchantmentValueEffect modifier, BlockingCondition showInTooltip, LootItemCondition applyCondition) {
		return new ConditionalEffect<>(new ComponentModifier(tooltipComponent, modifier, Optional.ofNullable(showInTooltip)), Optional.ofNullable(applyCondition));
	}

	public static ConditionalEffect<ComponentModifier> noConditions(Component tooltipComponent, EnchantmentValueEffect modifier) {
		return new ConditionalEffect<>(new ComponentModifier(tooltipComponent, modifier, Optional.empty()), Optional.empty());
	}
}
