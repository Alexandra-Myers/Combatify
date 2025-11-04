package net.atlas.combatify.config.wrapper;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.atlas.combatify.Combatify;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public record PatchedDataComponentMapWrapper(RegistryOps<Tag> ops, ResourceKey<Registry<DataComponentType<?>>> reg, PatchedDataComponentMap value) implements DataComponentMapWrapper<PatchedDataComponentMap> {
	public <T> void set(String type, String value) {
		@SuppressWarnings("unchecked") DataComponentType<T> dataComponentType = (DataComponentType<T>) ops().getter(reg()).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE).getOrThrow(ResourceKey.create(reg(), Identifier.parse(type))).value();
		StringReader reader = new StringReader(value);
		T val;
		try {
			Tag tag = TagParser.create(ops()).parseAsArgument(reader);
			val = dataComponentType.codec().parse(ops(), tag).getOrThrow(s -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create());
		} catch (CommandSyntaxException e) {
			Combatify.LOGGER.error("Failed to decode input to set the value of " + type + "! Exception: " + e);
			return;
		}
		unwrap().set(dataComponentType, val);
	}
	public <T> void remove(String type) {
		@SuppressWarnings("unchecked") DataComponentType<T> dataComponentType = (DataComponentType<T>) ops().getter(reg()).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE).getOrThrow(ResourceKey.create(reg(), Identifier.parse(type))).value();
		unwrap().remove(dataComponentType);
	}
	public void applyPatch(String patch) {
		StringReader reader = new StringReader(patch);
		DataComponentPatch val;
		try {
			Tag tag = TagParser.create(ops()).parseAsArgument(reader);
			val = DataComponentPatch.CODEC.parse(ops(), tag).getOrThrow(s -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create());
		} catch (CommandSyntaxException e) {
			Combatify.LOGGER.error("Failed to decode data component patch! Exception: " + e);
			return;
		}
		unwrap().applyPatch(val);
	}
	@Override
	public PatchedDataComponentMap unwrap() {
		return value;
	}
}
