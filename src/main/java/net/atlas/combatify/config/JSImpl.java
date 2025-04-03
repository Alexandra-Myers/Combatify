package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.rhino.*;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.wrapper.FoodDataWrapper;
import net.atlas.combatify.config.wrapper.GenericAPIWrapper;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.atlas.combatify.config.wrapper.SimpleAPIWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Function;

public class JSImpl {
	public final String fileName;
	public boolean load = true;

	private final Scriptable scope;
	private final Context rhinoContext;
	public static final Codec<JSImpl> CODEC = Codec.STRING.validate(str -> str.contains(".") ? DataResult.error(() -> "File loc not expected to contain a file extension!") : DataResult.success(str)).xmap(JSImpl::new, JSImpl::fileName);
	public JSImpl(String fileName) {
		this.fileName = fileName;
		this.rhinoContext = new ContextFactory().enter();
		this.scope = this.rhinoContext.initStandardObjects();
	}
	public String fileName() {
		return fileName;
	}
	static {
		initIncluded("vanilla_food_impl");
		initIncluded("cts_food_impl");
		initIncluded("combatify_food_impl");
		initIncluded("vanilla_crit_impl");
		initIncluded("cts_crit_impl");
		initIncluded("combatify_crit_impl");
		initIncluded("armor_calculations");
	}
	public static void initIncluded(String fileName) {
		File included = new File(FabricLoader.getInstance().getConfigDirectory().getAbsolutePath() + "/" + fileName + ".js");
		if (!included.exists()) {
			try {
				included.createNewFile();

				try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName + ".js")) {
					Files.write(included.toPath(), inputStream.readAllBytes());
				}
			} catch (IOException e) {
				throw new ReportedException(new CrashReport("Failed to clone base JS files!", e));
			}
		}
	}
	public boolean execFunc(String name, Reference<?, ?>... args) {
		try {
			Object ret = invokeFunc(name, args);
			return !(ret instanceof Boolean bool) || bool;
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return false;
	}

	public double execGetterFunc(double fallback, String name, Reference<?, ?>... args) {
		try {
			Object ret = invokeFunc(name, args);
			return !(ret instanceof Number number) ? fallback : number.doubleValue();
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return fallback;
	}

	public boolean execPlayerFunc(Player player, String name, Reference<?, ?>... args) {
		try {
			Object ret = handlePlayerBindings(player, name, args);
			return !(ret instanceof Boolean bool) || bool;
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return false;
	}

	public double execPlayerGetterFunc(double fallback, Player player, String name, Reference<?, ?>... args) {
		try {
			Object ret = handlePlayerBindings(player, name, args);
			return !(ret instanceof Number number) ? fallback : number.doubleValue();
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return fallback;
	}

	public boolean execFoodFunc(FoodData foodData, Player player, String name, Reference<?, ?>... args) {
		try {
			Object ret = handleFoodBindings(foodData, player, name, args);
			return !(ret instanceof Boolean bool) || bool;
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return false;
	}

	public double execFoodGetterFunc(double fallback, FoodData foodData, Player player, String name, Reference<?, ?>... args) {
		try {
			Object ret = handleFoodBindings(foodData, player, name, args);
			return !(ret instanceof Number number) ? fallback : number.doubleValue();
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return fallback;
	}

	public Object handleFoodBindings(FoodData foodData, Player player, String name, Reference<?, ?>[] args) throws IOException {
		if (ScriptableObject.hasProperty(scope, "foodData", rhinoContext)) {
			ScriptableObject.deleteProperty(scope, "foodData", rhinoContext);
		}
		if (ScriptableObject.hasProperty(scope, "player", rhinoContext)) {
			ScriptableObject.deleteProperty(scope, "player", rhinoContext);
		}
		ScriptableObject.putProperty(scope, "foodData", new FoodDataWrapper(foodData), rhinoContext);
		if (player != null) {
			ScriptableObject.putProperty(scope, "player", new PlayerWrapper<>(player), rhinoContext);
		}
		return invokeFunc(name, args);
	}

	public Object handlePlayerBindings(Player player, String name, Reference<?, ?>[] args) throws IOException {
		if (ScriptableObject.hasProperty(scope, "player", rhinoContext)) {
			ScriptableObject.deleteProperty(scope, "player", rhinoContext);
		}
		ScriptableObject.putProperty(scope, "player", new PlayerWrapper<>(player), rhinoContext);
		return invokeFunc(name, args);
	}

	private Object invokeFunc(String name, Reference<?, ?>[] args) throws IOException {
		cleanUpBindings(args);
		for (Reference<?, ?> ref : args) {
			Object value = ref.value;
			if (value instanceof SimpleAPIWrapper<?> simple) value = simple.unwrap();
			ScriptableObject.putProperty(scope, ref.name, value, rhinoContext);
		}

		if (load) {
			rhinoContext.evaluateReader(scope, new FileReader(FabricLoader.getInstance().getConfigDirectory().getAbsolutePath() + "/" + fileName + ".js"), fileName, 1, null);
			load = false;
		}

		return rhinoContext.evaluateString(scope, name + ";", fileName, 1, null);
	}

	private void cleanUpBindings(Reference<?, ?>... args) {
		for (Reference<?, ?> ref : args) {
			if (ScriptableObject.hasProperty(scope, ref.name, rhinoContext)) ScriptableObject.deleteProperty(scope, ref.name, rhinoContext);
		}
	}

	public record Reference<T extends GenericAPIWrapper<O>, O>(String name, T value) {
		public Reference(String name, O value, Function<O, T> mapper) {
			this(name, mapper.apply(value));
		}
	}
}
