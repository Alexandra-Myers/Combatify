package net.alexandra.atlas.atlas_combat.mixin;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongSliderBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.rizecookey.cookeymod.config.category.Category;
import net.rizecookey.cookeymod.config.option.DoubleSliderOption;
import net.rizecookey.cookeymod.config.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(DoubleSliderOption.class)
public class DoubleSliderOptionMixin extends OptionMixin<Double> {
	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void injectIntoConstructor(String id, Category category, Double defaultValue, double from, double _to, CallbackInfo ci) {
		if(Objects.equals(id, "attackCooldownHandOffset")) {
			setEntry(() -> {
				LongSliderBuilder builder = ConfigEntryBuilder.create().startLongSlider(MutableComponent.create(new TranslatableContents(this.getTranslationKey())), (long)((Double)get() * 100.0), (long)((from - 0.65) * 100.0), (long)((_to - 0.65) * 100.0)).setTextGetter((value) -> {
					return value == 0L ? MutableComponent.create(new TranslatableContents("options.cookeymod.generic.options.off")) : MutableComponent.create(new LiteralContents((new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US))).format((double)value / 100.0)));
				}).setSaveConsumer((value) -> {
					set(value / 100.0);
				}).setDefaultValue((long)((defaultValue - 0.65) * 100.0));
				builder.setTooltip(getTooltip(getTranslationKey()));
				return builder.build();
			});
		}
	}
}
