package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import net.atlas.combatify.Combatify;
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
	public boolean execFunc(String name, Reference... args) {
		try {
			Object ret = invokeFunc(name, args);
			cleanUpBindings(args);
			return !(ret instanceof Boolean bool) || bool;
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return false;
	}

	public double execGetterFunc(double fallback, String name, Reference... args) {
		try {
			Object ret = invokeFunc(name, args);
			cleanUpBindings(args);
			return !(ret instanceof Number number) ? fallback : number.doubleValue();
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return fallback;
	}

	public boolean execFoodFunc(FoodData foodData, Player player, String name, Reference... args) {
		try {
			Object ret = handleFoodBindings(foodData, player, name, args);
			return !(ret instanceof Boolean bool) || bool;
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return false;
	}

	public double execFoodGetterFunc(double fallback, FoodData foodData, Player player, String name, Reference... args) {
		try {
			Object ret = handleFoodBindings(foodData, player, name, args);
			return !(ret instanceof Number number) ? fallback : number.doubleValue();
		} catch (Exception e) {
			Combatify.LOGGER.info("Error executing " + name + " function: " + e.getMessage());
		}
		return fallback;
	}

	public Object handleFoodBindings(FoodData foodData, Player player, String name, Reference[] args) throws IOException {
		if (ScriptableObject.hasProperty(scope, "foodData", rhinoContext)) {
			ScriptableObject.deleteProperty(scope, "foodData", rhinoContext);
		}
		if (ScriptableObject.hasProperty(scope, "player", rhinoContext)) {
			ScriptableObject.deleteProperty(scope, "player", rhinoContext);
		}
		ScriptableObject.putProperty(scope, "foodData", foodData, rhinoContext);
		if (player != null) {
			ScriptableObject.putProperty(scope, "player", player, rhinoContext);
		}
		Object ret = invokeFunc(name, args);
		cleanUpBindings(args);
		return ret;
	}

	private Object invokeFunc(String name, Reference[] args) throws IOException {
		for (Reference ref : args) {
			ScriptableObject.putProperty(scope, ref.name, ref.value, rhinoContext);
		}

		if (load) {
			rhinoContext.evaluateReader(scope, new FileReader(FabricLoader.getInstance().getConfigDirectory().getAbsolutePath() + "/" + fileName + ".js"), fileName, 1, null);
			load = false;
		}

		return rhinoContext.evaluateString(scope, name + ";", fileName, 1, null);
	}

	private void cleanUpBindings(Reference... args) {
		for (Reference ref : args) {
			ScriptableObject.deleteProperty(scope, ref.name, rhinoContext);
		}
	}

	public record Reference(String name, Object value) {

	}
}
