package traben.entity_texture_features.features.property_reading.properties;

import org.jetbrains.annotations.NotNull;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.EntityBooleanLRU;

import java.util.Properties;

/**
 * Abstract class to hold each random property to test entities against
 * Must be registered with {@link RandomProperties#register(RandomProperties.RandomPropertyFactory...)}
 */
public abstract class RandomProperty {

    protected final EntityBooleanLRU entityCachedInitialResult = new EntityBooleanLRU();
    private boolean canUpdate = true;

    /**
     * Reads the given property data from the properties file, allowing for multiple property names and throws an
     * exception to ensure that any actual returned String is always non-blank.
     *
     * @param properties  the properties file being read
     * @param propertyNum the property index that is being read
     * @param propertyId  the property id's to be read
     * @return the String value of the desired property
     * @throws RandomPropertyException for any invalid, blank, or missing property.
     */
    @NotNull
    public static String readPropertiesOrThrow(Properties properties, int propertyNum, String... propertyId) throws RandomPropertyException {
        if (propertyId.length == 0)
            throw new IllegalArgumentException("[ETF] readPropertiesOrThrow() was given empty property id's");
        for (String id :
                propertyId) {
            if (properties.containsKey(id + "." + propertyNum)) {
                String dataFromProps = properties.getProperty(id + "." + propertyNum).strip();
                if (!dataFromProps.isBlank()) return dataFromProps;
            }
        }
        throw new RandomPropertyException("failed to read property [" + propertyId[0] + "]");
    }

    /**
     * Test the entity against this property.
     * <p>
     * This method wraps {@link RandomProperty#testEntityInternal(ETFEntity)} testEntityInternal()}
     * to provide spawn condition & repeated check efficiency
     *
     * @param entity   the ETFEntity being tested by this property
     * @param isUpdate flags if this test is part of an update
     * @return true if the entity meets the requirements of this property
     */
    public boolean testEntity(ETFEntity entity, boolean isUpdate) {
        var key = entity.etf$getUuid();
        if (isUpdate && !canPropertyUpdate() && entityCachedInitialResult.containsKey(key)) {
            return entityCachedInitialResult.getBoolean(key);//false default value
        }
        try {
            return testEntityInternal(entity);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Internal abstract method functionality for {@link RandomProperty#testEntity(ETFEntity, boolean)}
     */
    protected abstract boolean testEntityInternal(ETFEntity entity);

    /**
     * @return true if this property can be considered to update over time. factoring in the unknown restriction setting
     * which can get overridden by separate property settings.
     */
    public final boolean canPropertyUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(final boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    /**
     * Returns a String[] of all valid property id's these will be the text before the first full stop in the properties file.
     *
     * @return a string[]
     */
    @NotNull
    public abstract String[] getPropertyIds();

    /**
     * Returns the first index of {@link RandomProperty#getPropertyIds()}
     *
     * @return the string
     */
    @NotNull
    public String getPropertyId() {
        return getPropertyIds()[0];
    }

    /**
     * Gets a printable representation of the properties rules, e.g. "true"|"false" for a boolean property.
     * <p>
     * Best practice would be to simply copy the initial input string of the property.
     *
     * @return a string representation of what this property is checking for.
     */
    protected abstract String getPrintableRuleInfo();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Property: " + getPropertyId() + ", Rule: " + getPrintableRuleInfo() + "]";
    }

    public void cacheEntityInitialResult(ETFEntity entity) {
        try {
            entityCachedInitialResult.put(entity.etf$getUuid(), testEntityInternal(entity));
        } catch (Exception ignored) {
            entityCachedInitialResult.put(entity.etf$getUuid(), false);
        }
    }

    /**
     * This exception indicates that something has failed while creating an instance of {@link RandomProperty} from a
     * properties file.
     * <p>
     * This is thrown when the 'failure' is a safe or expected 'failure' with examples such as:<p>
     * - The property does not exist in this file<p>
     * - The property was written incorrectly<p>
     * - the property is empty or blank<p>
     * <p>
     * This should always be caught and ignored, it acts as a method of filtering out other unexpected Exceptions and debugging.
     */
    public static class RandomPropertyException extends Exception {
        /**
         * Instantiates a new Random property exception.
         *
         * @param reason the reason
         */
        public RandomPropertyException(String reason) {
            super("[ETF] " + reason);
        }
    }
}
