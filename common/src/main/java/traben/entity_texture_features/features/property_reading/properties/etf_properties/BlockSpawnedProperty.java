package traben.entity_texture_features.features.property_reading.properties.etf_properties;

import org.jetbrains.annotations.NotNull;
import traben.entity_texture_features.features.property_reading.properties.optifine_properties.BlocksProperty;

import java.util.Properties;

public class BlockSpawnedProperty extends BlocksProperty {


    protected BlockSpawnedProperty(final Properties properties, final int propertyNum, final String[] ids) throws RandomPropertyException {
        super(properties, propertyNum, ids);
    }

    public static BlocksProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new BlockSpawnedProperty(properties, propertyNum, new String[]{"blockSpawned"});
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"blockSpawned"};
    }
}
