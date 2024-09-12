package traben.entity_texture_features.features.property_reading.properties.etf_properties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import traben.entity_texture_features.features.property_reading.properties.optifine_properties.NBTProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Properties;

public class NBTVehicleProperty extends NBTProperty {
    protected NBTVehicleProperty(final Properties properties, final int propertyNum, final String nbtPrefix) throws RandomPropertyException {
        super(properties, propertyNum, nbtPrefix);
    }

    public static NBTProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new NBTVehicleProperty(properties, propertyNum, "nbtVehicle");
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected CompoundTag getEntityNBT(final ETFEntity entity) {
        if (entity instanceof Entity e) {
            ETFEntity vehicle = (ETFEntity)e.getVehicle();
            return vehicle != null ? vehicle.etf$getNbt() : INTENTIONAL_FAILURE;
        }
        return INTENTIONAL_FAILURE;
    }
}
