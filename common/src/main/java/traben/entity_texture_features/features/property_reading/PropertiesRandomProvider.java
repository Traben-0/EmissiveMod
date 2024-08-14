package traben.entity_texture_features.features.property_reading;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.property_reading.properties.RandomProperties;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.EntityBooleanLRU;

import java.util.*;
import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class PropertiesRandomProvider implements ETFApi.ETFVariantSuffixProvider {


    protected final List<RandomPropertyRule> propertyRules;

    protected final EntityBooleanLRU entityCanUpdate = new EntityBooleanLRU(1000);

    protected final @NotNull String packname;
    protected EntityRandomSeedFunction entityRandomSeedFunction = (entity) -> entity.etf$getUuid().hashCode();
    protected BiConsumer<ETFEntity, @Nullable RandomPropertyRule> onMeetsRule = (entity, rule) -> {
    };

    private PropertiesRandomProvider(ResourceLocation propertiesFileIdentifier, List<RandomPropertyRule> propertyRules) {
        this.propertyRules = propertyRules;
        this.packname = Minecraft.getInstance().getResourceManager().getResource(propertiesFileIdentifier)
                .map(Resource::sourcePackId)
                .orElse("vanilla");
    }

    @Nullable
    public static PropertiesRandomProvider of(ResourceLocation initialPropertiesFileIdentifier, ResourceLocation vanillaIdentifier, String... suffixKeyName) {
        ResourceLocation propertiesFileIdentifier = ETFDirectory.getDirectoryVersionOf(initialPropertiesFileIdentifier);
        if (propertiesFileIdentifier == null) return null;

        try {
            Properties props = ETFUtils2.readAndReturnPropertiesElseNull(propertiesFileIdentifier);
            if (props == null) {
                ETFUtils2.logMessage("Ignoring properties file that was null @ " + propertiesFileIdentifier, false);
                return null;
            }
            if (vanillaIdentifier.getPath().endsWith(".png")) {
                ETFManager.getInstance().grabSpecialProperties(props, ETFRenderContext.getCurrentEntity());
            }

            List<RandomPropertyRule> propertyRules = PropertiesRandomProvider.getAllValidPropertyObjects(props, propertiesFileIdentifier, suffixKeyName);
            if (propertyRules.isEmpty()) {
                ETFUtils2.logMessage("Ignoring properties file that failed to load any cases @ " + propertiesFileIdentifier, false);
                return null;
            }

            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            String properties = resourceManager.getResource(propertiesFileIdentifier).map(Resource::sourcePackId).orElse(null);
            String vanillaPack = resourceManager.getResource(vanillaIdentifier).map(Resource::sourcePackId).orElse(null);

            if (properties != null
                    && properties.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(properties, vanillaPack))) {
                return new PropertiesRandomProvider(propertiesFileIdentifier, propertyRules);
            }
        } catch (Exception e) {
            ETFUtils2.logWarn("Ignoring properties file that caused Exception @ " + propertiesFileIdentifier + "\n" + e, false);
            e.printStackTrace();
        }
        return null;
    }

    public static List<RandomPropertyRule> getAllValidPropertyObjects(Properties properties, ResourceLocation propertiesFilePath, String... suffixToTest) throws Exception {
        Set<String> propIds = properties.stringPropertyNames();
        //set so only 1 of each
        List<Integer> numbersList = getCaseNumbers(propIds);
        Collections.sort(numbersList);
        List<RandomPropertyRule> allRulesOfProperty = new ArrayList<>();
        for (Integer ruleNumber :
                numbersList) {
            //System.out.println("constructed as "+ruleNumber);
            //loops through each known number in properties
            //all rule.1 ect should be processed here
            Integer[] suffixesOfRule = getSuffixes(properties, ruleNumber, suffixToTest);

            //list easier to build
            if (suffixesOfRule != null && suffixesOfRule.length != 0) {
                allRulesOfProperty.add(new RandomPropertyRule(
                        propertiesFilePath.toString(),
                        ruleNumber,
                        suffixesOfRule,
                        getWeights(properties, ruleNumber),
                        RandomProperties.getAllRegisteredRandomPropertiesOfIndex(properties, ruleNumber)
                ));
            } else {
                ETFUtils2.logWarn("property number \"" + ruleNumber + ". in file \"" + propertiesFilePath + ". failed to read.");
            }
        }
        return allRulesOfProperty;
    }

    @NotNull
    private static List<Integer> getCaseNumbers(final Set<String> propIds) {
        Set<Integer> foundRuleNumbers = new HashSet<>();

        //get the foundRuleNumbers we are working with
        for (String str : propIds) {
            String[] split = str.split("\\.");
            if (split.length >= 2 && !split[1].isBlank()) {
                String possibleRuleNumber = split[1].replaceAll("\\D", "");
                if (!possibleRuleNumber.isBlank()) {
                    try {
                        foundRuleNumbers.add(Integer.parseInt(possibleRuleNumber));
                    } catch (NumberFormatException e) {
                        //ETFUtils2.logWarn("properties file number error in start count");
                    }
                }
            }
        }
        //sort from lowest to largest
        return new ArrayList<>(foundRuleNumbers);
    }

    @Nullable
    private static Integer[] getSuffixes(Properties props, int num, String... suffixToTest) throws Exception {
        var suffixes = SimpleIntegerArrayProperty.getGenericIntegerSplitWithRanges(props, num, suffixToTest);
        //throw if it contains 0 or negatives
        if (suffixes != null) {
            for (Integer suffix : suffixes) {
                if (suffix < 1) {
                    throw new Exception("Invalid suffix: [" + suffix + "] in " + Arrays.toString(suffixes));
                }
            }
        }
        return suffixes;
    }

    @Nullable
    private static Integer[] getWeights(Properties props, int num) {
        return SimpleIntegerArrayProperty.getGenericIntegerSplitWithRanges(props, num, "weights");
    }

    @SuppressWarnings("unused")
    public void setOnMeetsRuleHook(BiConsumer<ETFEntity, RandomPropertyRule> onMeetsRule) {
        if (onMeetsRule != null)
            this.onMeetsRule = onMeetsRule;
    }

    public @NotNull String getPackName() {
        return packname;
    }

    public boolean isHigherPackThan(@Nullable String packNameOther){
        return packname.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(packname, packNameOther));
    }

    @Override
    public boolean entityCanUpdate(UUID uuid) {
        return entityCanUpdate.getBoolean(uuid);
    }

    @SuppressWarnings("unused")
    @Override
    public IntOpenHashSet getAllSuffixes() {
        IntOpenHashSet allSuffixes = new IntOpenHashSet();
        for (RandomPropertyRule rule :
                propertyRules) {
            allSuffixes.addAll(rule.getSuffixSet());
        }
        return allSuffixes;
    }

    @Override
    public int size() {
        return propertyRules.size();
    }

    @Override
    public int getSuffixForETFEntity(ETFEntity entityToBeTested) {
        if (entityToBeTested == null) return 0;
        UUID id = entityToBeTested.etf$getUuid();
        boolean entityHasBeenTestedBefore = entityCanUpdate.containsKey(id);
        if (entityHasBeenTestedBefore) {
            //return andNothingElse
            int result = testEntityAgainstRules(entityToBeTested);
            if (result > 0) return result;
        } else {
            //return but capture spawn conditions of first time entity
            int result = testEntityAgainstRules(entityToBeTested);
            //must be done separate to, and after, above method as it sets the entityCanUpdate return
            if (entityCanUpdate.getBoolean(id)) {
                for (RandomPropertyRule rule : propertyRules) {
                    //cache entity spawns
                    rule.cacheEntityInitialResultsOfNonUpdatingProperties(entityToBeTested);
                }
            }
            if (result > 0) return result;
        }
        onMeetsRule.accept(entityToBeTested, null);
        return 0;
    }

    private int testEntityAgainstRules(final ETFEntity entityToBeTested) {
        for (RandomPropertyRule rule : propertyRules) {
            if (rule.doesEntityMeetConditionsOfThisCase(entityToBeTested, true, entityCanUpdate)) {
                onMeetsRule.accept(entityToBeTested, rule);
                return rule.getVariantSuffixFromThisCase(entityRandomSeedFunction.toInt(entityToBeTested));
            }
        }
        return 0;
    }

    @Override
    public void setRandomSupplier(final EntityRandomSeedFunction entityRandomSeedFunction) {
        if (entityRandomSeedFunction != null) {
            this.entityRandomSeedFunction = entityRandomSeedFunction;
        }
    }


}
