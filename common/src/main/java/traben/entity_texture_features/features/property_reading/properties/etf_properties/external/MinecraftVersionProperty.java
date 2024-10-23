package traben.entity_texture_features.features.property_reading.properties.etf_properties.external;


import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.SemVerRangeFromStringArrayProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Properties;


public class MinecraftVersionProperty extends SemVerRangeFromStringArrayProperty {

    private final SemVerNumber version;

    protected MinecraftVersionProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "minecraftVersion"));
        version = new SemVerNumber(Minecraft.getInstance().getLaunchedVersion());
    }

    public static MinecraftVersionProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new MinecraftVersionProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Nullable
    @Override
    protected SemVerNumber getRangeValueFromEntity(ETFEntity entity) {
        return version;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"minecraftVersion"};
    }

}
