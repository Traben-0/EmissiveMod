package traben.entity_texture_features.features.property_reading;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.ETFException;
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
    protected EntityRandomSeedFunction entityRandomSeedFunction = ETFEntity::etf$getOptifineId;
        //entity.etf$getUuid().hashCode();


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

            //assure default return always present
            if (!propertyRules.get(propertyRules.size() - 1).isAlwaysMet()) {
                propertyRules.add(RandomPropertyRule.defaultReturn);
            }

            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            String properties = resourceManager.getResource(propertiesFileIdentifier).map(Resource::sourcePackId).orElse(null);
            String vanillaPack = resourceManager.getResource(vanillaIdentifier).map(Resource::sourcePackId).orElse(null);

            if (properties != null
                    && properties.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(properties, vanillaPack))) {
                return new PropertiesRandomProvider(propertiesFileIdentifier, propertyRules);
            }
        }catch (ETFException etf){
            if (!propertiesFileIdentifier.toString().contains("optifine/cit/")) {
                ETFUtils2.logWarn("Ignoring properties file with problem: " + propertiesFileIdentifier + "\n" + etf, false);
            }
        }catch (Exception e) {
            ETFUtils2.logWarn("Ignoring properties file that caused unexpected Exception: " + propertiesFileIdentifier + "\n" + e, false);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    public static List<RandomPropertyRule> getAllValidPropertyObjects(Properties properties, ResourceLocation propertiesFilePath, String... suffixToTest) throws ETFException {
        Set<String> propIds = properties.stringPropertyNames();
        //set so only 1 of each
        List<Integer> numbersList = getCaseNumbers(propIds);
        Collections.sort(numbersList);

        if (numbersList.isEmpty()) {
            ETFUtils2.logWarn("Properties file [" + propertiesFilePath + "] contains no rules, this is invalid.", false);
            throw new ETFException("Properties file [" + propertiesFilePath + "] contains no rules, this is invalid.");
        }

        if(numbersList.get(0) < 1){
            ETFUtils2.logWarn("Properties file [" + propertiesFilePath + "] contains rule numbers less than 1, this is invalid.", false);
            throw new ETFException("Properties file [" + propertiesFilePath + "] contains rule numbers less than 1, this is invalid.");
        }

        //send log message if skipping rule numbers
        int last = 0;
        for (Integer i : numbersList) {
            if (i >= last + 10) {
                last = -1;
                break;
            }
            last = i;
        }
        if (last == -1) {
           if (ETF.config().getConfig().optifine_limitRandomVariantGapsBy10) {
                ETFUtils2.logError("Properties file [" + propertiesFilePath + "] has skipped rule numbers by values greater than 10, this is invalid in OptiFine. This limitation can be disabled in ETF's settings, but will make your pack incompatible with OptiFine.", false);
                throw new ETFException("Properties file [" + propertiesFilePath + "] has skipped rule numbers by values greater than 10, this is invalid in OptiFine. This limitation can be disabled in ETF's settings, but will make your pack incompatible with OptiFine.");
            }else{
                ETFUtils2.logWarn("Properties file [" + propertiesFilePath + "] has skipped rule numbers by values greater than 10, this is invalid in OptiFine. This limitation has been disabled in ETF's settings, your pack is incompatible with OptiFine.", false);
            }
        }

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
    private static Integer[] getSuffixes(Properties props, int num, String... suffixToTest) throws ETFException {
        var suffixes = SimpleIntegerArrayProperty.getGenericIntegerSplitWithRanges(props, num, suffixToTest);
        //throw if it contains 0 or negatives
        if (suffixes != null) {
            for (Integer suffix : suffixes) {
                if (suffix < 1) {
                    throw new ETFException("Invalid suffix: [" + suffix + "] in " + Arrays.toString(suffixes));
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

    public @NotNull String getPackName() {//todo check not used by EMF or ESF
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

        int result= testEntityAgainstRules(entityToBeTested, entityHasBeenTestedBefore);

        if (!entityHasBeenTestedBefore) {
            //return but capture spawn conditions of first time entity
            //must be done separate to, and after, above method as it sets the entityCanUpdate return
            if (entityCanUpdate.getBoolean(id)) {
                for (RandomPropertyRule rule : propertyRules) {
                    //cache entity spawns
                    rule.cacheEntityInitialResultsOfNonUpdatingProperties(entityToBeTested);
                }
            }
        }
        if (result > 0) return result;

        onMeetsRule.accept(entityToBeTested, null);
        return 0;
    }

    private int testEntityAgainstRules(final ETFEntity entityToBeTested, boolean isUpdate) {
        for (RandomPropertyRule rule : propertyRules) {
            if (rule.doesEntityMeetConditionsOfThisCase(entityToBeTested, isUpdate, entityCanUpdate)) {
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
