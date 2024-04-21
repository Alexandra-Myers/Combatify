package net.atlas.combatify.config.cookey;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.cookey.category.AnimationsCategory;
import net.atlas.combatify.config.cookey.category.Category;
import net.atlas.combatify.config.cookey.category.HudRenderingCategory;
import net.atlas.combatify.config.cookey.category.MiscCategory;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class ModConfig {
    public static final String TRANSLATION_KEY = "options.cookeymod";
    public static final String GENERIC_KEYS = TRANSLATION_KEY + "." + "generic.options";

    private static final String CONFIG_VERSION_KEY = "config-version";

    private final Logger logger;

    private final Path file;
    private Toml defaults;
    private Toml toml;
    private final Map<String, Category> categories = new HashMap<>();
    private long version;

    private final AnimationsCategory animations;
    private final HudRenderingCategory hudRendering;
    private final MiscCategory misc;

    public ModConfig(CombatifyClient mod, Path file) {
        this.logger = mod.getCookeyModLogger();

        this.file = file;

        animations = this.registerCategory(new AnimationsCategory(this));
        hudRendering = this.registerCategory(new HudRenderingCategory(this));
        misc = this.registerCategory(new MiscCategory(this));

        try {
            this.loadConfig();
        } catch (IOException e) {
            logger.error("Failed to load CookeyMod config file!");
            e.printStackTrace();
        }
    }

    public <T extends Category> T registerCategory(T category) {
        categories.put(category.getId(), category);
        return category;
    }

    public void loadCategories() {
        Map<String, Object> map = toml.toMap();
        boolean updated = ConfigUpdater.update(map, this.version);

        if (updated) logger.info("Updated config.");

        for (String id : categories.keySet()) {
            Map<String, Object> category = map.containsKey(id) && map.get(id) instanceof Map ? (Map<String, Object>) map.get(id) : new HashMap<>();
            categories.get(id).loadOptions(category != null ? category : new HashMap<>());
        }

        this.version = this.defaults.contains(CONFIG_VERSION_KEY) ? this.defaults.getLong(CONFIG_VERSION_KEY) : 1;

        if (updated) {
            try {
                this.saveConfig();
            } catch (IOException e) {
                CombatifyClient.getInstance().getCookeyModLogger().error("Failed to save CookeyMod config file!");
                e.printStackTrace();
            }
        }
    }

    public Map<String, Category> getCategories() {
        return new HashMap<>(this.categories);
    }

    public void loadConfig() throws IOException {
        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }

        InputStream resourceStream = getConfigResource();
        this.defaults = new Toml().read(resourceStream);
        if (!Files.exists(file)) {
            logger.info("Config not found, creating default one...");
            new TomlWriter().write(this.defaults, file.toFile());
            logger.info("Copied default config.");
        } else {
            Map<String, Object> configMap = new Toml().read(file.toFile()).toMap();
            Map<String, Object> fallbackMap = this.defaults.toMap();
            configMap.putIfAbsent(CONFIG_VERSION_KEY, 1);
            this.copyMissingNested(fallbackMap, configMap);
            new TomlWriter().write(configMap, file.toFile());
        }
        resourceStream.close();

        this.toml = new Toml().read(file.toFile());
        this.version = this.toml.contains(CONFIG_VERSION_KEY) ? this.toml.getLong(CONFIG_VERSION_KEY) : 1;
        this.loadCategories();
    }

    public Category getCategory(String id) {
        return this.categories.get(id);
    }

    public void saveConfig() throws IOException {
        Map<String, Object> optionsMap = new HashMap<>();
        for (String id : categories.keySet()) {
            optionsMap.put(id, categories.get(id).toMap());
        }

        optionsMap.put(CONFIG_VERSION_KEY, this.version);

        new TomlWriter().write(optionsMap, file.toFile());
    }

    @SuppressWarnings("rawtypes")
    public <T, U> void copyMissingNested(Map<T, U> from, Map<T, U> to) {
        for (Map.Entry<T, U> fromEntry : from.entrySet()) {
            T fromKey = fromEntry.getKey();
            U fromValue = fromEntry.getValue();
            Optional<U> toValue = to.containsKey(fromKey) ? Optional.of(to.get(fromKey)) : Optional.empty();
            if (toValue.isEmpty()) {
                to.put(fromKey, fromValue);
            } else if (fromValue instanceof Map fromMap && toValue.get() instanceof Map toMap) {
                this.copyMissingNested(fromMap, toMap);
            }
        }
    }

    public InputStream getConfigResource() {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("assets/cookeymod/config.toml");
        if (resourceStream == null) {
            logger.error("Failed to find config resource!");
            return null;
        }
        return resourceStream;
    }

    public long getVersion() {
        return version;
    }

    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    public AnimationsCategory animations() {
        return animations;
    }

    public HudRenderingCategory hudRendering() {
        return hudRendering;
    }

    public MiscCategory misc() {
        return misc;
    }
}
