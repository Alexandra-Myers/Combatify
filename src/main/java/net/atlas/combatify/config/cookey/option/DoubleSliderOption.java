package net.atlas.combatify.config.cookey.option;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.LongSliderEntry;
import me.shedaniel.clothconfig2.impl.builders.LongSliderBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.config.cookey.category.Category;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DoubleSliderOption extends Option<Double, LongSliderEntry> {
    public DoubleSliderOption(String id, Category category, Double defaultValue, double from, double to) {
        super(id, category, defaultValue);
        this.setConfigEntry(() -> {
            LongSliderBuilder builder = ConfigEntryBuilder.create()
                    .startLongSlider(Component.translatable(this.getTranslationKey()),
                            (long) (this.get() * 100.0),
                            (long) (from * 100.0),
                            (long) (to * 100.0))
                    .setTextGetter(value -> {
                        if (value == 0) {
                            return MutableComponent.create(
                                    new TranslatableContents(ModConfig.GENERIC_KEYS + ".off", null, new Object[0]));
                        }
                        return MutableComponent.create(new PlainTextContents.LiteralContents(new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US))
                                .format(value / 100.0)));
                    }).setSaveConsumer(value -> this.set(value / 100.0))
                    .setDefaultValue((long) (defaultValue * 100.0));
            builder.setTooltip(this.getTooltip(this.getTranslationKey()));
            return builder.build();
        });
    }
}
