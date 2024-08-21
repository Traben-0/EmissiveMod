package traben.entity_texture_features.features.property_reading.properties.generic_properties;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringArrayOrRegexProperty extends RandomProperty {

    protected static final Pattern GROUP_BY_QUOTATION_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    protected final String ORIGINAL_INPUT;
    protected final ObjectOpenHashSet<String> ARRAY;
    protected final RegexAndPatternPropertyMatcher MATCHER;

    protected final boolean usesRegex;


    protected StringArrayOrRegexProperty(String string) throws RandomPropertyException {
        ORIGINAL_INPUT = string;
        if (string == null || string.isBlank())
            throw new RandomPropertyException(getPropertyId() + " property was broken");
        if (string.startsWith("regex:") || string.startsWith("pattern:")
                || string.startsWith("iregex:") || string.startsWith("ipattern:")) {
            MATCHER = getStringMatcher_Regex_Pattern_List_Single(string);
            ARRAY = ObjectOpenHashSet.of(string);
            usesRegex = true;
        } else {
            String[] array = string.trim().split("\\s+");

            if (array.length == 0)
                throw new RandomPropertyException(getPropertyId() + " property was broken");

            ARRAY = new ObjectOpenHashSet<>();
            for (String str :
                    array) {
                ARRAY.add(shouldForceLowerCaseCheck() ? str.toLowerCase() : str);
            }
            //add the entire text as well just incase spaced names were expected
            if (array.length != 1) {
                ARRAY.add(string.trim());
            }
            MATCHER = this::testArray;
            usesRegex = false;
        }
    }

    @Nullable
    public static StringArrayOrRegexProperty.RegexAndPatternPropertyMatcher getStringMatcher_Regex_Pattern_List_Single(@Nullable String propertyLine) {
        if (propertyLine == null || propertyLine.isBlank()) return null;

        String stringToMatch = propertyLine.trim();
        final boolean invert = stringToMatch.startsWith("!");
        if (invert) stringToMatch = stringToMatch.substring(1);

        if (stringToMatch.startsWith("regex:") || stringToMatch.startsWith("iregex:")) {
            final boolean ignoreCase = stringToMatch.startsWith("i");
            final String finalStringToMatch = stringToMatch.replaceFirst("iregex:|regex:", "");
            return (string) -> invert != string.matches(ignoreCase ? "(?i)" + finalStringToMatch : finalStringToMatch);
        }else if (stringToMatch.startsWith("pattern:") || stringToMatch.startsWith("ipattern:")) {
            final boolean ignoreCase = stringToMatch.startsWith("i");
            //todo faster way to do this
            stringToMatch = stringToMatch.replaceFirst("ipattern:|pattern:", "").replace("*", "\\E.*\\Q").replace("?", "\\E.\\Q");
            final String finalStringToMatch = "\\Q" + stringToMatch + "\\E";
            return (string) -> invert != string.matches(ignoreCase ? "(?i)" + finalStringToMatch : finalStringToMatch);
        } else {//direct comparison
            String[] splitMatches = stringToMatch.split("\\s+");
            boolean hasQuotes = stringToMatch.contains("\"");
            final String finalString = stringToMatch;
            return (string) -> {
                boolean matchFound = string.equals(finalString) || Arrays.asList(splitMatches).contains(string);
                if (!matchFound && hasQuotes) {
                    Matcher m = GROUP_BY_QUOTATION_PATTERN.matcher(finalString);
                    while (m.find()) {
                        if (string.equals(m.group(1).replace("\"", "").trim())) {
                            matchFound = true;
                            break;
                        }
                    }
                }
                return invert != matchFound;
            };
        }
    }

    private boolean testArray(String fromEntity) {
        boolean matches = false;
        for (String string : ARRAY) {
            if (string.startsWith("!")) {
                matches = true;
                if (string.substring(1).equals(fromEntity)) {
                    return false;
                }
            } else if (string.equals(fromEntity)) {
                return true;
            }
        }
        return matches;
    }

    @Override
    public boolean testEntityInternal(ETFEntity entity) {
        String entityString = getValueFromEntity(entity);
        if (entityString != null) {
            return MATCHER.testString(shouldForceLowerCaseCheck() ? entityString.toLowerCase() : entityString);
        }
        return false;
    }

    protected abstract boolean shouldForceLowerCaseCheck();

    @Nullable
    protected abstract String getValueFromEntity(ETFEntity entity);

    @Override
    protected String getPrintableRuleInfo() {
        return ORIGINAL_INPUT;
    }

    public interface RegexAndPatternPropertyMatcher {
        boolean testString(String currentEntityValue);
    }
}
