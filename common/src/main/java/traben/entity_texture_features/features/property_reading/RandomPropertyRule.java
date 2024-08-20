package traben.entity_texture_features.features.property_reading;

import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.EntityBooleanLRU;

import java.util.*;

public class RandomPropertyRule {
    public final int RULE_NUMBER;
    public final String PROPERTY_FILE;
    private final Integer[] SUFFIX_NUMBERS_WEIGHTED;
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
        SUFFIX_NUMBERS_WEIGHTED = new Integer[]{1};
        PROPERTIES_TO_TEST = new RandomProperty[]{};
        RULE_ALWAYS_APPROVED = true;
        UPDATES = false;
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

        if (weights == null || weights.length == 0) {
            SUFFIX_NUMBERS_WEIGHTED = suffixes;
        } else if (weights.length == suffixes.length) {
            List<Integer> weightedSuffixArray = new ArrayList<>();
            for (int i = 0; i < suffixes.length; i++) {
                int suffixValue = suffixes[i];
                int weightValue = weights[i];
                for (int j = 0; j < weightValue; j++) {
                    weightedSuffixArray.add(suffixValue);
                }
            }
            SUFFIX_NUMBERS_WEIGHTED = weightedSuffixArray.toArray(new Integer[0]);
        } else {
            ETFUtils2.logWarn("random texture weights don't match for [" +
                    PROPERTY_FILE + "] rule # [" + RULE_NUMBER + "] :\n suffixes: " + Arrays.toString(suffixes) + "\n weights: " + Arrays.toString(weights), false);
            SUFFIX_NUMBERS_WEIGHTED = suffixes;
        }
        UPDATES = Arrays.stream(properties).anyMatch(RandomProperty::canPropertyUpdate);
    }

    public Set<Integer> getSuffixSet() {
        return new HashSet<>(List.of(SUFFIX_NUMBERS_WEIGHTED));
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
            ETFUtils2.logWarn("Random Property file [" +
                    PROPERTY_FILE + "] rule # [" + RULE_NUMBER + "] failed with Exception:\n" + e.getMessage());
            //fail this test
            return false;
        }
    }

    public int getVariantSuffixFromThisCase(int seed) {
        return SUFFIX_NUMBERS_WEIGHTED[Math.abs(seed) % SUFFIX_NUMBERS_WEIGHTED.length];
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
