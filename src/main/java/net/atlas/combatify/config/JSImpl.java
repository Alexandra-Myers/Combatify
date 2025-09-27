package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.atlas.combatify.Combatify;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.util.TimeUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class JSImpl {
	public static final ThreadLocal<Context> CONTEXT;
	public final Scriptable scope;
	static {
		CONTEXT = ThreadLocal.withInitial(Context::enter);
	}
	public final String fileName;
	public final String fileData;
	public final Map<String, org.mozilla.javascript.Function> functions;
	public static final Codec<JSImpl> CODEC = Codec.STRING.validate(str -> str.contains(".") ? DataResult.error(() -> "File loc not expected to contain a file extension!") : DataResult.success(str)).xmap(JSImpl::new, JSImpl::fileName);
	public JSImpl(String fileName) {
		String readFileData;
		this.fileName = fileName;
		this.scope = CONTEXT.get().initStandardObjects();
		Reader reader;
		try {
			reader = new BufferedReader(new FileReader(FabricLoader.getInstance().getConfigDirectory().getAbsolutePath() + "/" + fileName + ".js"));
			readFileData = Kit.readReader(reader);
		} catch (IOException e) {
			Combatify.JS_LOGGER.error("Error parsing JS File " + fileName + ". Exception: {}", e);
			readFileData = "";
		}
		this.fileData = readFileData;
		this.functions = new Object2ObjectOpenHashMap<>();
		String[] funcs = Arrays.stream(fileData.split("function")).filter(s -> !s.isBlank()).map(str -> "function" + str).toArray(String[]::new);
		for (String func : funcs) {
			functions.put(func.substring("function ".length(), func.indexOf(')') + 1), CONTEXT.get().compileFunction(scope, func, fileName, 0, null));
		}
		List<Map.Entry<String, Function>> func = functions.entrySet().stream().toList();
		for (Map.Entry<String, Function> stringFunctionEntry : func) {
			String key = stringFunctionEntry.getKey();
			scope.put(key.substring(0, key.indexOf('(')), scope, stringFunctionEntry.getValue());
		}
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
	public boolean execFunc(String name, Object... args) {
		long nanos = Util.getNanos();
		try {
			Object ret = invokeFunc(name, args);
			if (Combatify.CONFIG.enableDebugLogging())
				Combatify.JS_LOGGER.info("Time spent running " + name + ": " + ((double) (Util.getNanos() - nanos) / TimeUtil.NANOSECONDS_PER_MILLISECOND) + " ms");
			return !(ret instanceof Boolean bool) || bool;
		} catch (Exception e) {
			Combatify.JS_LOGGER.error("Error executing " + name + " function: {}", e);
		}
		return false;
	}

	public double execGetterFunc(double fallback, String name, Object... args) {
		long nanos = Util.getNanos();
		try {
			Object ret = invokeFunc(name, args);
			if (Combatify.CONFIG.enableDebugLogging())
				Combatify.JS_LOGGER.info("Time spent running " + name + ": " + ((double) (Util.getNanos() - nanos) / TimeUtil.NANOSECONDS_PER_MILLISECOND) + " ms");
			return !(ret instanceof Number number) ? fallback : number.doubleValue();
		} catch (Exception e) {
			Combatify.JS_LOGGER.error("Error executing " + name + " function: {}", e);
		}
		return fallback;
	}

	private Object invokeFunc(String name, Object... params) {
		String func = functions.keySet().stream().filter(function -> function.contains(name)).findFirst().orElseThrow();
		return functions.get(func).call(CONTEXT.get(), scope, null, params);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JSImpl js)) return false;
		return Objects.equals(fileName(), js.fileName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileName());
	}
}
