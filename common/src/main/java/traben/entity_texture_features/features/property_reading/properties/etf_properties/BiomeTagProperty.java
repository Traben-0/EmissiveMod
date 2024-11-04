package traben.entity_texture_features.features.property_reading.properties.etf_properties;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class BiomeTagProperty extends RandomProperty {

    private final String input;
    private final List<ResourceLocation> tagsList;
    private final boolean print;

    protected BiomeTagProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        input = RandomProperty.readPropertiesOrThrow(properties, propertyNum, "biomeTag", "biomeTags");
        print = input.startsWith("print:");
        tagsList = Arrays.stream(input.replaceFirst("^print:","").split("\\s+")).map(ETFUtils2::res).collect(Collectors.toCollection(ArrayList::new));
    }

    public static BiomeTagProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new BiomeTagProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    protected boolean testEntityInternal(final ETFEntity entity) {
        if (entity == null) return fail();

        var level = entity.etf$getWorld();
        if (level == null) return fail();

        var biome = level.getBiome(entity.etf$getBlockPos());
        if (biome == null) return fail();

        var tagStream = biome.tags();

        return tagStream.map((tag)->{
            var loc = tag.location();
            if(print){
                ETFUtils2.logMessage("BiomeTagProperty: " + input + " found tag: " + loc);
            }
            return loc;
        }).anyMatch(tagsList::contains);
    }

    private boolean fail(){
        if(print) ETFUtils2.logMessage("BiomeTagProperty: " + input + " failed to read entity");
        return false;
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"biomeTag", "biomeTags"};
    }

    @Override
    protected String getPrintableRuleInfo() {
        return "biomeTag=" + input;
    }
}
