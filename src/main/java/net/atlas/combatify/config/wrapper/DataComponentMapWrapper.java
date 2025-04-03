package net.atlas.combatify.config.wrapper;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.atlas.combatify.Combatify;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface DataComponentMapWrapper<M extends DataComponentMap> extends GenericAPIWrapper<M> {
	RegistryOps<Tag> ops();
	ResourceKey<Registry<DataComponentType<?>>> reg();
	default <T> T get(String type) {
		@SuppressWarnings("unchecked") DataComponentType<T> dataComponentType = (DataComponentType<T>) ops().getter(reg()).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE.asLookup()).getOrThrow(ResourceKey.create(reg(), ResourceLocation.parse(type))).value();
		return unwrap().get(dataComponentType);
	}
	default <T> T getOrDefault(String type, String defaultValue) {
		@SuppressWarnings("unchecked") DataComponentType<T> dataComponentType = (DataComponentType<T>) ops().getter(reg()).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE.asLookup()).getOrThrow(ResourceKey.create(reg(), ResourceLocation.parse(type))).value();
		StringReader reader = new StringReader(defaultValue);
		T defaultVal;
		try {
			Tag tag = new TagParser(reader).readValue();
			defaultVal = dataComponentType.codec().parse(ops(), tag).getOrThrow(s -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create());
		} catch (CommandSyntaxException e) {
			Combatify.LOGGER.error("Failed to decode default value obtained from a DataComponentMap in JavaScript! Exception: " + e);
			return unwrap().get(dataComponentType);
		}
		return unwrap().getOrDefault(dataComponentType, defaultVal);
	}
}
