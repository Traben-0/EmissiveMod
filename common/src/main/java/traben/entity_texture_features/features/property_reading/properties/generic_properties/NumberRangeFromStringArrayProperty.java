package traben.entity_texture_features.features.property_reading.properties.generic_properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.ArrayList;

public abstract class NumberRangeFromStringArrayProperty<N extends Number> extends RandomProperty {

    public final String originalInput;
    protected final ArrayList<RangeTester<N>> ARRAY = new ArrayList<>();
    protected final boolean doPrint;

    protected NumberRangeFromStringArrayProperty(String stringInput) throws RandomPropertyException {
        originalInput = stringInput;
        if (stringInput == null || stringInput.isBlank())
            throw new RandomPropertyException(getPropertyId() + " property was broken");

        doPrint = stringInput.startsWith("print:");

        String testString = doPrint ? stringInput.substring(6) : stringInput;

        //if you need to parse custom characters the originalInput is saved in that variable for you
        String onlyNumbersSpacesDashesAndPeriods = testString.replaceAll("[^0-9.\\s-]", "");

        String[] array = onlyNumbersSpacesDashesAndPeriods.trim().split("\\s+");

        if (array.length == 0)
            throw new RandomPropertyException(getPropertyId() + " property was broken");

        for (String str : array) {
            RangeTester<N> tester = getRangeTesterFromString(str);
            if (tester != null) ARRAY.add(tester);
        }
    }

    @Override
    public boolean testEntityInternal(ETFEntity entity) {
        N checkValue = getRangeValueFromEntity(entity);
        if (checkValue != null){
            for (RangeTester<N> range : ARRAY) {
                if (range != null && range.isValueWithinRangeOrEqual(checkValue)) {
                    if (doPrint)
                        ETFUtils2.logMessage(getPropertyId() + " property value print: [" + checkValue + "], returned: true.");
                    return true;
                }
            }
        }
        if (doPrint) ETFUtils2.logMessage(getPropertyId() + " property value print: [" + checkValue + "], returned: false.");
        return false;
    }


    @Nullable
    protected abstract N getRangeValueFromEntity(ETFEntity entity);

    @Nullable
    protected abstract RangeTester<N> getRangeTesterFromString(String possibleRange);


    @Override
    public abstract @NotNull String[] getPropertyIds();

    @Override
    protected String getPrintableRuleInfo() {
        return originalInput;
    }

    public interface RangeTester<N> {
        boolean isValueWithinRangeOrEqual(N value);
    }
}
