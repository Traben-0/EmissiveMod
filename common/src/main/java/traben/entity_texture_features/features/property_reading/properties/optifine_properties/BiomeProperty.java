package traben.entity_texture_features.features.property_reading.properties.optifine_properties;

import com.google.common.base.CaseFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Properties;


public class BiomeProperty extends StringArrayOrRegexProperty {


    protected BiomeProperty(String data) throws RandomProperty.RandomPropertyException {
        super(data);
        System.out.println("biomecreate: " + data);
    }

    public static BiomeProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            String dataFromProperty = RandomProperty.readPropertiesOrThrow(properties, propertyNum, "biomes", "biome");
            if (dataFromProperty.startsWith("regex:") || dataFromProperty.startsWith("pattern:")) {
                //add entire line as a test
                return new BiomeProperty(dataFromProperty);
            } else {
                boolean prints = dataFromProperty.startsWith("print:");
                String[] biomeList = (prints ? dataFromProperty.substring(6) : dataFromProperty).split("\\s+");

                //strip out old format optifine biome names
                //I could be way more in-depth and make these line up to all variants but this is legacy code
                //only here for compat, pack makers need to fix these
                if (biomeList.length > 0) {
                    for (int currentIndex = 0; currentIndex < biomeList.length; currentIndex++) {
                        biomeList[currentIndex] = biomeList[currentIndex].strip().replaceAll("^minecraft:", "");

                        switch (biomeList[currentIndex]) {
                            //case "Ocean" -> biomeList[i] = "ocean";
                            //case "Plains" -> biomeList[i] = "plains";
                            case "ExtremeHills" -> biomeList[currentIndex] = "stony_peaks";
                            case "Forest", "ForestHills" -> biomeList[currentIndex] = "forest";
                            case "Taiga", "TaigaHills" -> biomeList[currentIndex] = "taiga";
                            case "Swampland" -> biomeList[currentIndex] = "swamp";
//                        case "River" -> biomeList[currentIndex] = "river";
                            case "Hell" -> biomeList[currentIndex] = "nether_wastes";
                            case "Sky" -> biomeList[currentIndex] = "the_end";
                            //case "FrozenOcean" -> biomeList[i] = "frozen_ocean";
                            //case "FrozenRiver" -> biomeList[i] = "frozen_river";
                            case "IcePlains" -> biomeList[currentIndex] = "snowy_plains";
                            case "IceMountains" -> biomeList[currentIndex] = "snowy_slopes";
                            case "MushroomIsland", "MushroomIslandShore" -> biomeList[currentIndex] = "mushroom_fields";
                            //case "Beach" -> biomeList[i] = "beach";
                            case "DesertHills", "Desert" -> biomeList[currentIndex] = "desert";
                            case "ExtremeHillsEdge" -> biomeList[currentIndex] = "meadow";
                            case "Jungle", "JungleHills" -> biomeList[currentIndex] = "jungle";
                            default -> {
                                String currentBiome = biomeList[currentIndex];
                                if (!currentBiome.contains("_") && !currentBiome.equals(currentBiome.toLowerCase())) {
                                    //has capitals and no "_" it is probably the camel case format
                                    biomeList[currentIndex] = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, currentBiome);
                                    //biomeList[currentIndex] = currentBiome.replaceAll("(\\B)([A-Z])", "_$2");
                                }
                            }
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    if (prints) builder.append("print:");
                    for (String str :
                            biomeList) {
                        builder.append(str).append(" ");
                    }
                    //lower case required
                    return new BiomeProperty(builder.toString().trim().toLowerCase());
                }
            }
            return null;
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return true;
    }


    @Override
    public @Nullable String getValueFromEntity(ETFEntity etfEntity) {
        if (etfEntity.etf$getWorld() != null && etfEntity.etf$getBlockPos() != null) {
            String biome = ETF.getBiomeString(etfEntity.etf$getWorld(), etfEntity.etf$getBlockPos());
            return biome == null ? null : biome.replace("minecraft:", "");
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"biomes", "biome"};
    }

}
