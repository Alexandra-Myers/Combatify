package net.atlas.combatify.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.*;
import net.atlas.combatify.networking.NetworkingHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("deprecated")
public abstract class AtlasConfig {
    public final ResourceLocation name;
	public boolean isDefault;
    public final Map<String, ConfigHolder<?>> valueNameToConfigHolderMap = Maps.newHashMap();
	List<ConfigHolder<?>> holders;
	public final List<Category> categories;
    public static final Map<ResourceLocation, AtlasConfig> configs = Maps.newHashMap();
	public static final Map<String, AtlasConfig> menus = Maps.newHashMap();
    final Path configFolderPath;
    File configFile;
    JsonObject configJsonObject;
    List<EnumHolder<?>> enumValues;
    List<StringHolder> stringValues;
    List<BooleanHolder> booleanValues;
    List<IntegerHolder> integerValues;
    List<DoubleHolder> doubleValues;
    public AtlasConfig(ResourceLocation name) {
		this.name = name;
        enumValues = new ArrayList<>();
        stringValues = new ArrayList<>();
        booleanValues = new ArrayList<>();
        integerValues = new ArrayList<>();
        doubleValues = new ArrayList<>();
		holders = new ArrayList<>();
		categories = createCategories();
        defineConfigHolders();
        configFolderPath = Path.of(FabricLoader.getInstance().getConfigDir().getFileName().getFileName() + "/" + name.getNamespace());
        if (!Files.exists(configFolderPath))
            try {
                Files.createDirectory(configFolderPath);
            } catch (IOException e) {
                throw new ReportedException(new CrashReport("Failed to create config directory for config " + name, e));
            }

        load();
        configs.put(name, this);
    }

	public AtlasConfig declareDefaultForMod(String modID) {
		menus.put(modID, this);
		return this;
	}

	public @NotNull List<Category> createCategories() {
		return new ArrayList<>();
	}

	public abstract void defineConfigHolders();

	public abstract void resetExtraHolders();

    public abstract <T> void alertChange(ConfigValue<T> tConfigValue, T newValue);

    public static String getString(JsonObject element, String name) {
        return element.get(name).getAsString();
    }

    public static Integer getInt(JsonObject element, String name) {
        return element.get(name).getAsInt();
    }

    public static Double getDouble(JsonObject element, String name) {
        return element.get(name).getAsDouble();
    }
    public static Boolean getBoolean(JsonObject element, String name) {
        return element.get(name).getAsBoolean();
    }
	public void reload() {
		resetExtraHolders();
		load();
	}
    public final void load() {
		isDefault = false;
        configFile = new File(configFolderPath.toAbsolutePath() + "/" + name.getPath() + ".json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                InputStream inputStream = getDefaultedConfig();
                Files.write(configFile.toPath(), inputStream.readAllBytes());
                inputStream.close();
            } catch (IOException e) {
                throw new ReportedException(new CrashReport("Failed to create config file for config " + name, e));
            }
        }

        try {
            configJsonObject = JsonParser.parseReader(new JsonReader(new FileReader(configFile))).getAsJsonObject();
            for (EnumHolder<?> enumHolder : enumValues)
                if (configJsonObject.has(enumHolder.heldValue.name))
                    enumHolder.setValue(getString(configJsonObject, enumHolder.heldValue.name));
            for (StringHolder stringHolder : stringValues)
                if (configJsonObject.has(stringHolder.heldValue.name))
                    stringHolder.setValue(getString(configJsonObject, stringHolder.heldValue.name));
            for (BooleanHolder booleanHolder : booleanValues)
                if (configJsonObject.has(booleanHolder.heldValue.name))
                    booleanHolder.setValue(getBoolean(configJsonObject, booleanHolder.heldValue.name));
            for (IntegerHolder integerHolder : integerValues)
                if (configJsonObject.has(integerHolder.heldValue.name))
                    integerHolder.setValue(getInt(configJsonObject, integerHolder.heldValue.name));
            for (DoubleHolder doubleHolder : doubleValues)
                if (configJsonObject.has(doubleHolder.heldValue.name))
                    doubleHolder.setValue(getDouble(configJsonObject, doubleHolder.heldValue.name));
            loadExtra(configJsonObject);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    protected abstract void loadExtra(JsonObject jsonObject);
    protected abstract InputStream getDefaultedConfig();
    public AtlasConfig loadFromNetwork(FriendlyByteBuf buf) {
        enumValues.forEach(enumHolder -> enumHolder.readFromBuf(buf));
        stringValues.forEach(stringHolder -> stringHolder.readFromBuf(buf));
        booleanValues.forEach(booleanHolder -> booleanHolder.readFromBuf(buf));
        integerValues.forEach(integerHolder -> integerHolder.readFromBuf(buf));
        doubleValues.forEach(doubleHolder -> doubleHolder.readFromBuf(buf));
        return this;
    }
    public static AtlasConfig staticLoadFromNetwork(FriendlyByteBuf buf) {
        return configs.get(buf.readResourceLocation()).loadFromNetwork(buf);
    }

    public void saveToNetwork(FriendlyByteBuf buf) {
        enumValues.forEach(enumHolder -> enumHolder.writeToBuf(buf));
        stringValues.forEach(stringHolder -> stringHolder.writeToBuf(buf));
        booleanValues.forEach(booleanHolder -> booleanHolder.writeToBuf(buf));
        integerValues.forEach(integerHolder -> integerHolder.writeToBuf(buf));
        doubleValues.forEach(doubleHolder -> doubleHolder.writeToBuf(buf));
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    public ConfigHolder<?> fromValue(ConfigValue<?> value) {
        return valueNameToConfigHolderMap.get(value.name);
    }
    public final <E extends Enum<E>> EnumHolder<E> createEnum(String name, E defaultVal, Class<E> clazz, E[] values, Function<Enum, Component> names) {
        EnumHolder<E> enumHolder = new EnumHolder<>(new ConfigValue<>(defaultVal, values, false, name, this), clazz, names);
        enumValues.add(enumHolder);
		holders.add(enumHolder);
        return enumHolder;
    }
    public StringHolder createStringRange(String name, String defaultVal, String... values) {
        StringHolder stringHolder = new StringHolder(new ConfigValue<>(defaultVal, values, false, name, this));
        stringValues.add(stringHolder);
		holders.add(stringHolder);
        return stringHolder;
    }
	public StringHolder createString(String name, String defaultVal) {
		StringHolder stringHolder = new StringHolder(new ConfigValue<>(defaultVal, null, false, name, this));
		stringValues.add(stringHolder);
		holders.add(stringHolder);
		return stringHolder;
	}
    public BooleanHolder createBoolean(String name, boolean defaultVal) {
        BooleanHolder booleanHolder = new BooleanHolder(new ConfigValue<>(defaultVal, new Boolean[]{false, true}, false, name, this));
        booleanValues.add(booleanHolder);
		holders.add(booleanHolder);
        return booleanHolder;
    }
	public IntegerHolder createIntegerUnbound(String name, Integer defaultVal) {
		IntegerHolder integerHolder = new IntegerHolder(new ConfigValue<>(defaultVal, null, false, name, this));
		integerValues.add(integerHolder);
		holders.add(integerHolder);
		return integerHolder;
	}
    public IntegerHolder createInRestrictedValues(String name, Integer defaultVal, Integer... values) {
        IntegerHolder integerHolder = new IntegerHolder(new ConfigValue<>(defaultVal, values, false, name, this));
        integerValues.add(integerHolder);
		holders.add(integerHolder);
        return integerHolder;
    }
    public IntegerHolder createInRange(String name, int defaultVal, int min, int max) {
        Integer[] range = new Integer[]{min, max};
        IntegerHolder integerHolder = new IntegerHolder(new ConfigValue<>(defaultVal, range, true, name, this));
        integerValues.add(integerHolder);
		holders.add(integerHolder);
        return integerHolder;
    }
	public DoubleHolder createDoubleUnbound(String name, Double defaultVal) {
		DoubleHolder doubleHolder = new DoubleHolder(new ConfigValue<>(defaultVal, null, false, name, this));
		doubleValues.add(doubleHolder);
		holders.add(doubleHolder);
		return doubleHolder;
	}
    public DoubleHolder createInRestrictedValues(String name, Double defaultVal, Double... values) {
        DoubleHolder doubleHolder = new DoubleHolder(new ConfigValue<>(defaultVal, values, false, name, this));
        doubleValues.add(doubleHolder);
		holders.add(doubleHolder);
        return doubleHolder;
    }
    public DoubleHolder createInRange(String name, double defaultVal, double min, double max) {
        Double[] range = new Double[]{min, max};
        DoubleHolder doubleHolder = new DoubleHolder(new ConfigValue<>(defaultVal, range, true, name, this));
        doubleValues.add(doubleHolder);
		holders.add(doubleHolder);
        return doubleHolder;
    }

	public final void saveConfig() throws IOException {
		PrintWriter printWriter = new PrintWriter(new FileWriter(configFile), true);
		JsonWriter jsonWriter = new JsonWriter(printWriter);
		jsonWriter.beginObject();
		jsonWriter.setIndent("\t");
		saveExtra(jsonWriter, printWriter);
		for (Category category : categories) {
			for (ConfigHolder<?> holder : category.members) {
				holder.writeToJSONFile(jsonWriter);
				printWriter.flush();
			}
			printWriter.println();
		}
		jsonWriter.setIndent("");
		jsonWriter.endObject();
	}

	public abstract void saveExtra(JsonWriter jsonWriter, PrintWriter printWriter);

	public record ConfigValue<T>(T defaultValue, T[] possibleValues, boolean isRange, String name, AtlasConfig owner) {
        public void emitChanged(T newValue) {
            owner.alertChange(this, newValue);
        }
        public boolean isValid(T newValue) {
            return possibleValues == null || Arrays.stream(possibleValues).toList().contains(newValue);
        }
        public void addAssociation(ConfigHolder<T> configHolder) {
            if (owner.valueNameToConfigHolderMap.containsKey(name))
                throw new ReportedException(new CrashReport("Tried to associate a ConfigHolder to a ConfigValue which already has one!", new RuntimeException()));
            owner.valueNameToConfigHolderMap.put(name, configHolder);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConfigValue<?> that)) return false;
            return isRange() == that.isRange() && Objects.equals(defaultValue, that.defaultValue) && Arrays.equals(possibleValues, that.possibleValues) && Objects.equals(name, that.name) && Objects.equals(owner, that.owner);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(defaultValue, isRange(), name, owner);
            result = 31 * result + Arrays.hashCode(possibleValues);
            return result;
        }
    }
    public static abstract class ConfigHolder<T> {
        private T value;
        public final ConfigValue<T> heldValue;
        public final StreamCodec<ByteBuf, T> codec;
		public final BiConsumer<JsonWriter, T> update;
		public boolean restartRequired = false;
		public Supplier<Optional<Component[]>> tooltip = Optional::empty;

		public ConfigHolder(ConfigValue<T> value, StreamCodec<ByteBuf, T> codec, BiConsumer<JsonWriter, T> update) {
            this.value = value.defaultValue;
            heldValue = value;
            this.codec = codec;
            value.addAssociation(this);
			this.update = update;
        }
        public T get() {
            return value;
        }
		public void writeToJSONFile(JsonWriter writer) throws IOException {
			writer.name(heldValue.name);
			update.accept(writer, value);
		}
        public void writeToBuf(FriendlyByteBuf buf) {
            codec.encode(buf, value);
        }
        public void readFromBuf(FriendlyByteBuf buf) {
            T newValue = codec.decode(buf);
            if (isNotValid(newValue))
                return;
            heldValue.emitChanged(newValue);
            value = newValue;
        }
        public boolean isNotValid(T newValue) {
            return !heldValue.isValid(newValue);
        }
        public void setValue(T newValue) {
            if (isNotValid(newValue))
                return;
            heldValue.emitChanged(newValue);
            value = newValue;
        }
		public void tieToCategory(Category category) {
			category.addMember(this);
		}
		public void setRestartRequired(boolean restartRequired) {
			this.restartRequired = restartRequired;
		}
		public void setupTooltip(int length) {
			Component[] components = new Component[length];
			for (int i = 0; i < length; i++) {
				components[i] = Component.translatable(getTranslationKey() + ".tooltip." + i);
			}
			this.tooltip = () -> Optional.of(components);
		}
		public String getTranslationKey() {
			return "text.config." + heldValue.owner.name.getPath() + ".option." + heldValue.name;
		}
		public String getTranslationResetKey() {
			return "text.config." + heldValue.owner.name.getPath() + ".reset";
		}

		public abstract AbstractConfigListEntry<?> transformIntoConfigEntry();
	}
    public static class EnumHolder<E extends Enum<E>> extends ConfigHolder<E> {
        public final Class<E> clazz;
		public final Function<Enum, Component> names;
        private EnumHolder(ConfigValue<E> value, Class<E> clazz, Function<Enum, Component> names) {
            super(value, new StreamCodec<>() {
                @Override
                public void encode(ByteBuf object, E object2) {
                    new FriendlyByteBuf(object).writeEnum(object2);
                }

                @Override
                public @NotNull E decode(ByteBuf object) {
                    return new FriendlyByteBuf(object).readEnum(clazz);
                }
            }, (writer, e) -> {
                try {
                    writer.value(e.name());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            this.clazz = clazz;
			this.names = names;
        }
        public void setValue(String name) {
            setValue(Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT)));
        }

		@Override
		public AbstractConfigListEntry<?> transformIntoConfigEntry() {
			return new EnumListEntry<>(Component.translatable(getTranslationKey()), clazz, get(), Component.translatable(getTranslationResetKey()), () -> heldValue.defaultValue, this::setValue, names, tooltip, restartRequired);
		}
	}
    public static class StringHolder extends ConfigHolder<String> {
        private StringHolder(ConfigValue<String> value) {
            super(value, ByteBufCodecs.STRING_UTF8, (writer, s) -> {
                try {
                    writer.value(s);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

		@Override
		public AbstractConfigListEntry<?> transformIntoConfigEntry() {
			return new StringListEntry(Component.translatable(getTranslationKey()), get(), Component.translatable(getTranslationResetKey()), () -> heldValue.defaultValue, this::setValue, tooltip, restartRequired);
		}
	}
    public static class BooleanHolder extends ConfigHolder<Boolean> {
        private BooleanHolder(ConfigValue<Boolean> value) {
            super(value, ByteBufCodecs.BOOL, (writer, b) -> {
				try {
					writer.value(b);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
        }

		@Override
		public AbstractConfigListEntry<?> transformIntoConfigEntry() {
			return new BooleanListEntry(Component.translatable(getTranslationKey()), get(), Component.translatable(getTranslationResetKey()), () -> heldValue.defaultValue, this::setValue, tooltip, restartRequired);
		}
	}
    public static class IntegerHolder extends ConfigHolder<Integer> {
        private IntegerHolder(ConfigValue<Integer> value) {
            super(value, ByteBufCodecs.VAR_INT, (writer, i) -> {
				try {
					writer.value(i);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
        }

        @Override
        public boolean isNotValid(Integer newValue) {
			if (heldValue.possibleValues == null)
				return super.isNotValid(newValue);
            boolean inRange = heldValue.isRange && newValue >= heldValue.possibleValues[0] && newValue <= heldValue.possibleValues[1];
            return super.isNotValid(newValue) && !inRange;
        }

		@Override
		public AbstractConfigListEntry<?> transformIntoConfigEntry() {
			if (!heldValue.isRange)
				return new IntegerListEntry(Component.translatable(getTranslationKey()), get(), Component.translatable(getTranslationResetKey()), () -> heldValue.defaultValue, this::setValue, tooltip, restartRequired);
			return new IntegerSliderEntry(Component.translatable(getTranslationKey()), heldValue.possibleValues[0], heldValue.possibleValues[1], get(), Component.translatable(getTranslationResetKey()), () -> heldValue.defaultValue, this::setValue, tooltip, restartRequired);
		}
	}
    public static class DoubleHolder extends ConfigHolder<Double> {
        private DoubleHolder(ConfigValue<Double> value) {
            super(value, ByteBufCodecs.DOUBLE, (writer, d) -> {
				try {
					writer.value(d);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
        }

        @Override
        public boolean isNotValid(Double newValue) {
			if (heldValue.possibleValues == null)
				return super.isNotValid(newValue);
            boolean inRange = heldValue.isRange && newValue >= heldValue.possibleValues[0] && newValue <= heldValue.possibleValues[1];
            return super.isNotValid(newValue) && !inRange;
        }

		@Override
		public AbstractConfigListEntry<?> transformIntoConfigEntry() {
			return new DoubleListEntry(Component.translatable(getTranslationKey()), get(), Component.translatable(getTranslationResetKey()), () -> heldValue.defaultValue, this::setValue, tooltip, restartRequired);
		}
	}

	public void reloadFromDefault() {
		resetExtraHolders();
		isDefault = true;
		JsonObject configJsonObject = JsonParser.parseReader(new JsonReader(new InputStreamReader(getDefaultedConfig()))).getAsJsonObject();

		for (EnumHolder<?> enumHolder : enumValues)
			if (configJsonObject.has(enumHolder.heldValue.name))
				enumHolder.setValue(getString(configJsonObject, enumHolder.heldValue.name));
		for (StringHolder stringHolder : stringValues)
			if (configJsonObject.has(stringHolder.heldValue.name))
				stringHolder.setValue(getString(configJsonObject, stringHolder.heldValue.name));
		for (BooleanHolder booleanHolder : booleanValues)
			if (configJsonObject.has(booleanHolder.heldValue.name))
				booleanHolder.setValue(getBoolean(configJsonObject, booleanHolder.heldValue.name));
		for (IntegerHolder integerHolder : integerValues)
			if (configJsonObject.has(integerHolder.heldValue.name))
				integerHolder.setValue(getInt(configJsonObject, integerHolder.heldValue.name));
		for (DoubleHolder doubleHolder : doubleValues)
			if (configJsonObject.has(doubleHolder.heldValue.name))
				doubleHolder.setValue(getDouble(configJsonObject, doubleHolder.heldValue.name));
		loadExtra(configJsonObject);
	}

	public abstract void handleExtraSync(NetworkingHandler.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender);
	public abstract Screen createScreen(Screen prevScreen);
	public boolean hasScreen() {
		return true;
	}

	public record Category(AtlasConfig config, String name, List<ConfigHolder<?>> members) {
		public String translationKey() {
			return "text.config." + config.name.getPath() + ".category." + name;
		}
		public void addMember(ConfigHolder<?> member) {
			members.add(member);
		}

		public List<AbstractConfigListEntry<?>> membersAsCloth() {
			List<AbstractConfigListEntry<?>> transformed = new ArrayList<>();
			members.forEach(configHolder -> transformed.add(configHolder.transformIntoConfigEntry()));
			return transformed;
		}
	}
}
