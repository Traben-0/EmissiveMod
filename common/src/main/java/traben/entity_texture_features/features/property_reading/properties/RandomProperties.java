package traben.entity_texture_features.features.property_reading.properties;

import com.demonwav.mcdev.annotations.Translatable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.features.property_reading.properties.etf_properties.*;
import traben.entity_texture_features.features.property_reading.properties.etf_properties.external.*;
import traben.entity_texture_features.features.property_reading.properties.optifine_properties.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class RandomProperties {
    @SuppressWarnings("StaticCollection")
    private static final ObjectOpenHashSet<RandomPropertyFactory> REGISTERED_PROPERTIES = new ObjectOpenHashSet<>();

    static {
        register(
                //ETF properties
                RandomPropertyFactory.of("angry", "config.entity_texture_features.property_explanation.angry", AngryProperty::getPropertyOrNull),
                RandomPropertyFactory.of("creeperCharged", "config.entity_texture_features.property_explanation.creeper", ChargedCreeperProperty::getPropertyOrNull),
                RandomPropertyFactory.of("distance", "config.entity_texture_features.property_explanation.distance", DistanceToPlayerProperty::getPropertyOrNull),
                RandomPropertyFactory.of("items", "config.entity_texture_features.property_explanation.items", ItemProperty::getPropertyOrNull),
                RandomPropertyFactory.of("jumpStrength", "config.entity_texture_features.property_explanation.jump", JumpProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("llamaInventory", "config.entity_texture_features.property_explanation.llama", LlamaInventoryProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("maxHealth", "config.entity_texture_features.property_explanation.max_health", MaxHealthProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("moving", "config.entity_texture_features.property_explanation.moving", MovingProperty::getPropertyOrNull),
                RandomPropertyFactory.of("hiddenGene", "config.entity_texture_features.property_explanation.gene", PandaGeneProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("playerCreated", "config.entity_texture_features.property_explanation.created", PlayerCreatedProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("screamingGoat", "config.entity_texture_features.property_explanation.goat", ScreamingGoatProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("maxSpeed", "config.entity_texture_features.property_explanation.speed", SpeedProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("isSpawner", "config.entity_texture_features.property_explanation.spawner", SpawnerProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("dimension", "config.entity_texture_features.property_explanation.dimension", DimensionProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("light", "config.entity_texture_features.property_explanation.light", LightProperty::getPropertyOrNull),
                RandomPropertyFactory.of("variant", "config.entity_texture_features.property_explanation.variant", VariantProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("isCreative", "config.entity_texture_features.property_explanation.creative", CreativeProperty::getPropertyOrNull),
                RandomPropertyFactory.of("isTeammate", "config.entity_texture_features.property_explanation.teammate", TeammateProperty::getPropertyOrNull),
                RandomPropertyFactory.of("isClientPlayer", "config.entity_texture_features.property_explanation.client", ClientPlayerProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("teams", "config.entity_texture_features.property_explanation.team", TeamProperty::getPropertyOrNull),
                RandomPropertyFactory.of("blockSpawned", "config.entity_texture_features.property_explanation.block_spawned", BlockSpawnedProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("nbtVehicle", "config.entity_texture_features.property_explanation.nbt_vehicle", NBTVehicleProperty::getPropertyOrNull),
                RandomPropertyFactory.of("blockAbove", "config.entity_texture_features.property_explanation.block_above", BlockAboveProperty::getPropertyOrNull),
                RandomPropertyFactory.of("blockAboveSolid", "config.entity_texture_features.property_explanation.block_above_solid", BlockAboveSolidProperty::getPropertyOrNull),
                RandomPropertyFactory.of("blockBelow", "config.entity_texture_features.property_explanation.block_below", BlockBelowProperty::getPropertyOrNull),
                RandomPropertyFactory.of("blockBelowSolid", "config.entity_texture_features.property_explanation.block_below_solid", BlockBelowSolidProperty::getPropertyOrNull),
                RandomPropertyFactory.of("biomeTag", "config.entity_texture_features.property_explanation.biome_tag", BiomeTagProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("temperature", "config.entity_texture_features.property_explanation.temperature", TemperatureProperty::getPropertyOrNull,true),

                //etf externals
                RandomPropertyFactory.of("hour", "config.entity_texture_features.property_explanation.hour", HourProperty::getPropertyOrNull),
                RandomPropertyFactory.of("minute", "config.entity_texture_features.property_explanation.min", MinuteProperty::getPropertyOrNull),
                RandomPropertyFactory.of("monthDay", "config.entity_texture_features.property_explanation.month_day", MonthDayProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("month", "config.entity_texture_features.property_explanation.month", MonthProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("second", "config.entity_texture_features.property_explanation.sec", SecondProperty::getPropertyOrNull),
                RandomPropertyFactory.of("weekDay", "config.entity_texture_features.property_explanation.week_day", WeekDayProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("yearDay", "config.entity_texture_features.property_explanation.year_day", YearDayProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("year", "config.entity_texture_features.property_explanation.year", YearProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("language", "config.entity_texture_features.property_explanation.lang", LanguageProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("textureSuffix", "config.entity_texture_features.property_explanation.texture_suffix", TextureSuffixProperty::getPropertyOrNull),
                RandomPropertyFactory.of("textureRule", "config.entity_texture_features.property_explanation.texture_rule", TextureRuleIndexProperty::getPropertyOrNull),
                RandomPropertyFactory.of("modLoaded", "config.entity_texture_features.property_explanation.mod_rule", ModLoadedProperty::getPropertyOrNull),
                RandomPropertyFactory.of("nbtClient", "config.entity_texture_features.property_explanation.nbt_client", NBTClientProperty::getPropertyOrNull),
                RandomPropertyFactory.of("minecraftVersion", "config.entity_texture_features.property_explanation.mc_version", MinecraftVersionProperty::getPropertyOrNull),


                //OptiFine properties
                RandomPropertyFactory.of("baby", "config.entity_texture_features.property_explanation.baby", BabyProperty::getPropertyOrNull),
                RandomPropertyFactory.of("biomes", "config.entity_texture_features.property_explanation.biome", BiomeProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("blocks", "config.entity_texture_features.property_explanation.block", BlocksProperty::getPropertyOrNull),
                RandomPropertyFactory.of("colors", "config.entity_texture_features.property_explanation.color", ColorProperty::getPropertyOrNull),
                RandomPropertyFactory.of("health", "config.entity_texture_features.property_explanation.health", HealthProperty::getPropertyOrNull),
                RandomPropertyFactory.of("heights", "config.entity_texture_features.property_explanation.height", HeightProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("moonPhase", "config.entity_texture_features.property_explanation.moon", MoonPhaseProperty::getPropertyOrNull, true),
                RandomPropertyFactory.of("name", "config.entity_texture_features.property_explanation.name", NameProperty::getPropertyOrNull),
                RandomPropertyFactory.of("nbt", "config.entity_texture_features.property_explanation.nbt", NBTProperty::getPropertyOrNull),
                RandomPropertyFactory.of("professions", "config.entity_texture_features.property_explanation.profession", ProfessionProperty::getPropertyOrNull),
                RandomPropertyFactory.of("sizes", "config.entity_texture_features.property_explanation.size", SizeProperty::getPropertyOrNull),
                RandomPropertyFactory.of("dayTime", "config.entity_texture_features.property_explanation.day_time", TimeOfDayProperty::getPropertyOrNull),
                RandomPropertyFactory.of("weather", "config.entity_texture_features.property_explanation.weather", WeatherProperty::getPropertyOrNull, true)
        );
    }

    public static void forEachProperty(@NotNull Consumer<RandomPropertyFactory> consumer) {
        REGISTERED_PROPERTIES.stream()
                .sorted(Comparator.comparing(RandomPropertyFactory::getPropertyId))
                .forEach(consumer);
    }

    /**
     * Register new {@link RandomProperty} objects to be tested against entities.
     *
     * @param properties the property to be registered for testing
     */
    public static void register(RandomPropertyFactory... properties) {
        for (RandomPropertyFactory factory :
                properties) {
            if (factory != null) {
                REGISTERED_PROPERTIES.add(factory);
            }
        }
    }


    /**
     * Get an array of all {@link RandomProperty} that are present for this property index.
     *
     * @param properties  the properties file
     * @param propertyNum the property index
     * @return the array of all {@link RandomProperty} that are present for this property index.
     */
    public static RandomProperty[] getAllRegisteredRandomPropertiesOfIndex(Properties properties, int propertyNum) {
        ArrayList<RandomProperty> randomProperties = new ArrayList<>();
        for (RandomPropertyFactory factory :
                REGISTERED_PROPERTIES) {
            if (factory != null) {
                RandomProperty property = factory.getPropertyOrNull(properties, propertyNum);
                if (property != null) randomProperties.add(property);
            }
        }
        return randomProperties.toArray(new RandomProperty[0]);
    }


    public interface RandomPropertyFactory {

        static @NotNull RandomPropertyFactory of(@NotNull String id,
                                                 @NotNull @Translatable String explanationKey,
                                                 @NotNull BiFunction<Properties, Integer, RandomProperty> factory) {
            return of(id, explanationKey, factory, false);
        }

        static @NotNull RandomPropertyFactory of(@NotNull String id,
                                                 @NotNull @Translatable String explanationKey,
                                                 @NotNull BiFunction<Properties, Integer, @Nullable RandomProperty> factory,
                                                 boolean isSpawnLocked) {
            return new RandomPropertyFactory() {
                @Override
                public RandomProperty getPropertyOrNull(Properties properties, int propertyNum) {
                    if (ETF.config().getConfig().isPropertyDisabled(this)) return null;
                    if (properties == null) return null;

                    RandomProperty property = factory.apply(properties, propertyNum);
                    if (property == null) return null;

                    property.setCanUpdate(ETF.config().getConfig().canPropertyUpdate(this));
                    return property;
                }

                @Override
                public @NotNull String getPropertyId() {
                    return id;
                }

                @Override
                public boolean equals(final Object obj) {
                    return obj instanceof RandomPropertyFactory && ((RandomPropertyFactory) obj).getPropertyId().equals(getPropertyId());
                }

                @Override
                public int hashCode() {
                    return getPropertyId().hashCode();
                }

                @Override
                public boolean updatesOverTime() {
                    return !isSpawnLocked;
                }

                @Override
                public @NotNull String getExplanationTranslationKey() {
                    return explanationKey;
                }
            };
        }

        @Nullable RandomProperty getPropertyOrNull(@SuppressWarnings("unused") Properties properties, @SuppressWarnings("unused") int propertyNum);

        @NotNull String getPropertyId();

        boolean updatesOverTime();

        @NotNull String getExplanationTranslationKey();

    }

}
