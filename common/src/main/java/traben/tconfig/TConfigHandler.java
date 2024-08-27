package traben.tconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import traben.entity_texture_features.ETFVersionDifferenceManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;




public class TConfigHandler<T extends TConfig> {


    private final Supplier<T> newConfigSupplier;
    private final String configFileName;
    private final Class<T> configClass;
    private final String logID;
    private T CONFIG;

    public TConfigHandler(Supplier<T> newConfigSupplier, String configFileName, String logID) {
        this.newConfigSupplier = newConfigSupplier;
        this.logID = logID;
        this.configFileName = configFileName.endsWith(".json") ? configFileName : configFileName + ".json";
        //noinspection unchecked
        this.configClass = Objects.requireNonNull((Class<T>) newConfigSupplier.get().getClass());
        //needs class set above
        this.loadFromFile();

    }

    public T getConfig() {
        if (CONFIG == null) {
            loadFromFile();
        }
        return CONFIG;
    }

    public void setConfig(final T CONFIG) {
        this.CONFIG = CONFIG;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TConfigHandler<?> that = (TConfigHandler<?>) o;
        return getConfig().getClass().equals(that.getConfig().getClass());
    }

    @SuppressWarnings("unused")
    public boolean configEquals(final Object that) {
        if (CONFIG == that) return true;
        if (that == null || CONFIG.getClass() != that.getClass()) return false;
        return toJson().equals(toJson(that));
    }

    @Override
    public int hashCode() {
        return Objects.hash(newConfigSupplier, CONFIG, configFileName, configClass);
    }

    public void saveToFile() {
        var configDir = ETFVersionDifferenceManager.getConfigDirectory();
        if (configDir == null) return;

        File config = new File(configDir.toFile(), configFileName);
        //noinspection ResultOfMethodCallIgnored
        config.getParentFile().mkdirs();

        try (FileWriter fileWriter = new FileWriter(config)) {
            fileWriter.write(toJson());
        } catch (IOException e) {
            TConfigLog.logError(logID, "Config file could not be saved: " + e.getMessage());
        }
    }

    public String toJson() {
        return toJson(CONFIG);
    }

    public String toJson(Object config) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(config);
    }

    public void loadFromFile() {
        var configDir = ETFVersionDifferenceManager.getConfigDirectory();
        if (configDir == null) {
            CONFIG = newConfigSupplier.get();
            return;
        }

        File config = new File(configDir.toFile(), configFileName);
        if (config.exists()) {
            try (FileReader fileReader = new FileReader(config)) {
                CONFIG = fromJson(fileReader);
            } catch (Exception e) {
                TConfigLog.logError(logID, "Config could not be loaded, using defaults");
                CONFIG = newConfigSupplier.get();
            }
        } else {
            CONFIG = newConfigSupplier.get();
        }

        if (CONFIG == null) {
            TConfigLog.logError(logID, "Config was null, using defaults");
            CONFIG = newConfigSupplier.get();
        }

        saveToFile();
    }

    public T fromJson(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, configClass);
    }

    public T fromJson(FileReader json) throws JsonIOException, JsonSyntaxException {
        Gson gson = new GsonBuilder().setLenient().create();
        return gson.fromJson(json, configClass);
    }

    public T copyOfConfig() {
        return fromJson(toJson());
    }

    public boolean doesGUI() {
        return getConfig().doesGUI();
    }

}
