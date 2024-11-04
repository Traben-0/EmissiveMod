package traben.entity_texture_features.features.property_reading.properties.etf_properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Properties;

public class TemperatureProperty extends FloatRangeFromStringArrayProperty {


    protected TemperatureProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "temperature"));
    }

    public static TemperatureProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new TemperatureProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Nullable
    @Override
    protected Float getRangeValueFromEntity(ETFEntity entity) {
        if (entity == null) return null;

        var level = entity.etf$getWorld();
        if (level == null) return null;

        var biome = level.getBiome(entity.etf$getBlockPos());
        return biome.value().getHeightAdjustedTemperature(entity.etf$getBlockPos()
            #if MC > MC_21    , entity.etf$getWorld().getSeaLevel() #endif
        );
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"temperature"};
    }

}
