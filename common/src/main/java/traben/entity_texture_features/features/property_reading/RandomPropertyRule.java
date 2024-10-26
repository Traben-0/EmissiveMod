package traben.entity_texture_features.features.property_reading;

import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.EntityBooleanLRU;

import java.util.*;

public class RandomPropertyRule {
    public final int RULE_NUMBER;
    public final String PROPERTY_FILE;
    private final Integer[] SUFFIX_NUMBERS;
    private final Integer[] WEIGHTS;
    private final int WEIGHT_TOTAL;
    private final RandomProperty[] PROPERTIES_TO_TEST;
    private final boolean RULE_ALWAYS_APPROVED;
    private final boolean UPDATES;

    static final RandomPropertyRule defaultReturn = new RandomPropertyRule(){
        @Override
        public int getVariantSuffixFromThisCase(final int seed) {
            return 1;
        }

        @Override
        public boolean doesEntityMeetConditionsOfThisCase(final ETFEntity etfEntity, final boolean isUpdate, final EntityBooleanLRU UUID_CaseHasUpdateablesCustom) {
            return true;
        }
    };

    private RandomPropertyRule(){
        RULE_NUMBER = 0;//used for rule matching settings
        PROPERTY_FILE = "default setter";
        SUFFIX_NUMBERS = new Integer[]{1};
        PROPERTIES_TO_TEST = new RandomProperty[]{};
        RULE_ALWAYS_APPROVED = true;
        UPDATES = false;
        WEIGHTS = null;
        WEIGHT_TOTAL = 0;
    }

    public boolean isAlwaysMet(){
        return RULE_ALWAYS_APPROVED;
    }


    public RandomPropertyRule(
            String propertiesFile,
            int ruleNumber,
            Integer[] suffixes,
            Integer[] weights,
            RandomProperty... properties

    ) {
        PROPERTY_FILE = propertiesFile;
        RULE_NUMBER = ruleNumber;
        PROPERTIES_TO_TEST = properties;
        RULE_ALWAYS_APPROVED = properties.length == 0;


        SUFFIX_NUMBERS = suffixes;

        if (weights == null || weights.length == 0) {
            WEIGHTS = null;
            WEIGHT_TOTAL = 0;
        } else /*if (weights.length == suffixes.length)*/ {

            if(weights.length != suffixes.length) {
                Integer[] weightsFinal = new Integer[suffixes.length];
                int smaller = Math.min(weights.length, suffixes.length);
                System.arraycopy(weights, 0, weightsFinal, 0, smaller);

                if (weights.length >= suffixes.length) {
                    ETFUtils2.logWarn("Random Property file [" + PROPERTY_FILE + "] rule # [" + RULE_NUMBER + "] has more weights than suffixes, trimming to match");
                } else {
                    ETFUtils2.logWarn("Random Property file [" + PROPERTY_FILE + "] rule # [" + RULE_NUMBER + "] has more suffixes than weights, expanding to match");
                    int avgWeight = Arrays.stream(weights).mapToInt(Integer::intValue).sum() / weights.length;
                    for (int i = weights.length; i < weightsFinal.length; i++) {
                        weightsFinal[i] = avgWeight;
                    }
                }
                weights = weightsFinal;
            }

            int total = 0;
            WEIGHTS = new Integer[weights.length];
            for (int i = 0; i < weights.length; i++) {
                Integer weight = weights[i];
                if (weight < 0) {
                    total = 0;
                    break;
                }
                total += weight;
                WEIGHTS[i] = total;
            }
            WEIGHT_TOTAL = total;
        }
        UPDATES = Arrays.stream(PROPERTIES_TO_TEST).anyMatch(RandomProperty::canPropertyUpdate);
    }

    public Set<Integer> getSuffixSet() {
        return new HashSet<>(List.of(SUFFIX_NUMBERS));
    }

    public boolean doesEntityMeetConditionsOfThisCase(ETFEntity etfEntity, boolean isUpdate, EntityBooleanLRU UUID_CaseHasUpdateablesCustom) {
        if (RULE_ALWAYS_APPROVED) return true;
        if (etfEntity == null) return false;
        if (UPDATES && UUID_CaseHasUpdateablesCustom != null) {
            UUID_CaseHasUpdateablesCustom.put(etfEntity.etf$getUuid(), true);
        }

        try {
            for (RandomProperty property : PROPERTIES_TO_TEST) {
                if (!property.testEntity(etfEntity, isUpdate)) return false;
            }
            return true;
        } catch (Exception e) {
            ETFUtils2.logWarn("Random Property file [" + PROPERTY_FILE + "] rule # [" + RULE_NUMBER + "] failed with Exception:\n" + e.getMessage());
            //fail this test
            return false;
        }
    }

    public int getVariantSuffixFromThisCase(int seed) {

        if (WEIGHT_TOTAL == 0){
            return SUFFIX_NUMBERS[Math.abs(seed) % SUFFIX_NUMBERS.length];
        }else{
            int seedValue = Math.abs(seed) % WEIGHT_TOTAL;
            for (int i = 0; i < WEIGHTS.length; i++) {
                if (seedValue < WEIGHTS[i]) {
                    return SUFFIX_NUMBERS[i];
                }
            }
            return 0;
        }

        //return SUFFIX_NUMBERS[Math.abs(seed) % SUFFIX_NUMBERS.length];
    }

    public void cacheEntityInitialResultsOfNonUpdatingProperties(ETFEntity entity) {
        for (RandomProperty property : PROPERTIES_TO_TEST) {
            if (!property.canPropertyUpdate()) {
                try {
                    property.cacheEntityInitialResult(entity);
                } catch (Exception ignored) {
                }
            }
        }
    }

}
