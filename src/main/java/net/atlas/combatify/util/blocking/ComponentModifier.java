package net.atlas.combatify.util.blocking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.util.blocking.condition.BlockingCondition;
import net.atlas.combatify.util.blocking.condition.BlockingConditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.LevelBasedValue.Constant;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ComponentModifier(Component tooltipComponent, EnchantmentValueEffect modifier, Optional<BlockingCondition> showInTooltip, float componentValueFactor) {
	public static final Codec<ComponentModifier> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(ComponentSerialization.CODEC.fieldOf("tooltip").forGetter(ComponentModifier::tooltipComponent),
				EnchantmentValueEffect.CODEC.fieldOf("modifier").forGetter(ComponentModifier::modifier),
				BlockingConditions.MAP_CODEC.codec().optionalFieldOf("show_in_tooltip").forGetter(ComponentModifier::showInTooltip),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("tooltip_value_factor", 1F).forGetter(ComponentModifier::componentValueFactor))
			.apply(instance, ComponentModifier::new));
	public static final Codec<ComponentModifier> NO_CONDITION_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(ComponentSerialization.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(ComponentModifier::tooltipComponent),
					EnchantmentValueEffect.CODEC.fieldOf("modifier").forGetter(ComponentModifier::modifier),
					ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("tooltip_value_factor", 1F).forGetter(ComponentModifier::componentValueFactor))
				.apply(instance, ComponentModifier::new));

	public ComponentModifier(Component tooltipComponent, EnchantmentValueEffect modifier, BlockingCondition showInTooltip, float componentValueFactor) {
		this(tooltipComponent, modifier, Optional.of(showInTooltip), componentValueFactor);
	}

	public ComponentModifier(Component tooltipComponent, EnchantmentValueEffect modifier, float componentValueFactor) {
		this(tooltipComponent, modifier, Optional.empty(), componentValueFactor);
	}

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
			if (tooltipComponent.equals(other.tooltipComponent) && componentValueFactor == other.componentValueFactor) {
				val = other.modifyValue(val, blockingLevel, randomSource);
				others.remove(i);
			}
		}
		val *= componentValueFactor;
		if (val > 0) res.add(buildComponent(tooltipComponent, val));
		if (!others.isEmpty()) others.getFirst().tryCombine(others, blockingLevel, randomSource);
		return res;
	}
	public float tryCombineVal(List<ComponentModifier> others, int blockingLevel, RandomSource randomSource) {
		float val = 0;
		for (ComponentModifier other : others) {
			val = other.modifyValue(val, blockingLevel, randomSource);
		}
		return val;
	}
	public record CombinedModifier(ComponentModifier base, ComponentModifier factor, Optional<BlockingCondition> showInTooltip) {
		public static Codec<CombinedModifier> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(ComponentModifier.NO_CONDITION_CODEC.fieldOf("base").forGetter(CombinedModifier::base),
				ComponentModifier.NO_CONDITION_CODEC.fieldOf("factor").forGetter(CombinedModifier::factor),
				BlockingConditions.MAP_CODEC.codec().optionalFieldOf("show_in_tooltip").forGetter(CombinedModifier::showInTooltip))
			.apply(instance, CombinedModifier::new));

		public DataSet modifyValues(DataSet val, int blockingLevel, RandomSource randomSource) {
			return new DataSet(base.modifyValue(val.addValue, blockingLevel, randomSource), factor.modifyValue(val.multiplyValue, blockingLevel, randomSource));
		}
		public List<Component> tryCombine(List<CombinedModifier> others, int blockingLevel, RandomSource randomSource) {
			List<ComponentModifier> res = new ArrayList<>();
			for (CombinedModifier other : others) {
				if (!other.base.tooltipComponent.equals(Component.empty())) res.add(other.base);
				if (!other.factor.tooltipComponent.equals(Component.empty())) res.add(other.factor);
			}
			if (!res.isEmpty()) return res.getFirst().tryCombine(res, blockingLevel, randomSource);
			return Collections.emptyList();
		}
		public DataSet tryCombineVal(List<CombinedModifier> others, int blockingLevel, RandomSource randomSource) {
			DataSet val = new DataSet(0, 0);
			for (CombinedModifier other : others) {
				val = other.modifyValues(val, blockingLevel, randomSource);
			}
			return val;
		}
		public boolean matches(ItemStack itemStack) {
			return showInTooltip.isEmpty() || showInTooltip.get().appliesComponentModifier(itemStack);
		}
		public static CombinedModifier createFactorOnly(ComponentModifier factor, Optional<BlockingCondition> showInTooltip) {
			return new CombinedModifier(new ComponentModifier(Component.empty(), new AddValue(new Constant(0)), Optional.empty()), factor, showInTooltip);
		}
		public static CombinedModifier createBaseOnly(ComponentModifier base, Optional<BlockingCondition> showInTooltip) {
			return new CombinedModifier(base, new ComponentModifier(Component.empty(), new AddValue(new Constant(0)), Optional.empty()), showInTooltip);
		}
	}
	public record DataSet(float addValue, float multiplyValue) {}
}
