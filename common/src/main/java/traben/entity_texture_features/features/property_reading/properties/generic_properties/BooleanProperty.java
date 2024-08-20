package traben.entity_texture_features.features.property_reading.properties.generic_properties;

import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.Properties;

public abstract class BooleanProperty extends RandomProperty {


    private final boolean BOOLEAN;

    protected BooleanProperty(Boolean bool) throws RandomPropertyException {
        if (bool == null) throw new RandomPropertyException(getPropertyId() + " property was broken");
        BOOLEAN = bool;
    }

    @Nullable
    public static Boolean getGenericBooleanThatCanNull(Properties props, int num, String... propertyNames) {
        if (propertyNames.length == 0)
            throw new IllegalArgumentException("BooleanProperty, empty property names given");
        for (String propertyName : propertyNames) {
            if (props.containsKey(propertyName + "." + num)) {
                String input = props.getProperty(propertyName + "." + num).trim();
                if ("true".equals(input) || "false".equals(input)) {
                    return "true".equals(input);
                } else {
                    ETFUtils2.logWarn("properties files number error in " + propertyName + " category");
                }
            }
        }
        return null;
    }

    @Override
    public boolean testEntityInternal(ETFEntity entity) {
        Boolean entityBoolean = getValueFromEntity(entity);
        if (entityBoolean != null) {
            return BOOLEAN == entityBoolean;
        }
        return false;
    }

    @Nullable
    protected abstract Boolean getValueFromEntity(ETFEntity entity);

    @Override
    protected String getPrintableRuleInfo() {
        return BOOLEAN ? "true" : "false";
    }
}
