package traben.entity_texture_features.features.property_reading.properties.optifine_properties;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.Map;
import java.util.Properties;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import static traben.entity_texture_features.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty.getStringMatcher_Regex_Pattern_List_Single;

public class NBTProperty extends RandomProperty {

    private final Map<String, NBTTester> NBT_MAP;
    private boolean printAll = false;

    private final String prefix;

    protected NBTProperty(Properties properties, int propertyNum, String nbtPrefix) throws RandomPropertyException {
        prefix = nbtPrefix;
        final String keyPrefix = prefix+"." + propertyNum + '.';
        NBT_MAP = new Object2ObjectLinkedOpenHashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();//are null keys allowed in properties?
            if (key != null && key.startsWith(keyPrefix)) {
                String nbtName = key.replaceFirst(keyPrefix, "");
                String instruction = entry.getValue().toString().trim()
                        .replace("print_raw:","print:raw:");//old format compat
                if (!nbtName.isBlank() && !instruction.isBlank()) {
                    printAll = printAll || instruction.startsWith("print_all:");

                    NBT_MAP.put(nbtName, NBTTester.of(nbtName, instruction));
                } else {
                    throw new RandomPropertyException("NBT failed, as instruction or nbt name was blank: " + keyPrefix + nbtName + "=" + instruction);
                }
            }
        }
        if (NBT_MAP.isEmpty()) throw new RandomPropertyException("NBT failed as the final testing map was empty");
    }

    public static NBTProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new NBTProperty(properties, propertyNum, "nbt");
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    private static boolean isStringValidInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    protected @Nullable CompoundTag getEntityNBT(ETFEntity entity) {
        //might return null, empty, INTENTIONAL_FAILURE, or throw an exception
        return entity.etf$getNbt();
    }

    protected static final CompoundTag INTENTIONAL_FAILURE = new CompoundTag();

    @Override
    protected boolean testEntityInternal(ETFEntity entity) {
        CompoundTag entityNBT;
        try {
            //return for child property instances
            entityNBT = getEntityNBT(entity);
        }catch (Exception e){
            ETFUtils2.logError(prefix+" test crashed reading entity NBT");
            throw e;
        }

        //dont log expected failure unless printing
        if (entityNBT == INTENTIONAL_FAILURE) {
            if (printAll) {
                ETFUtils2.logMessage(prefix+" property [full] print:\n<NBT is missing>");
            }
            return false;
        }

        if (entityNBT == null || entityNBT.isEmpty()) {
            if (printAll) {
                ETFUtils2.logMessage(prefix+" property [full] print:\n<NBT is empty or missing>");
            }
            //log unexpected failure
            ETFUtils2.logError(prefix+" test failed, as could not read entity NBT");
            return false;
        }

        if (printAll) {
            ETFUtils2.logMessage(prefix+" property [full] print:\n" + formatNbtPretty(entityNBT));
        }

        return testAllNBTCases(entityNBT);
    }

    protected boolean testAllNBTCases(final CompoundTag entityNBT) {
        for (Map.Entry<String, NBTTester> nbtPropertyEntry : NBT_MAP.entrySet()) {
            NBTTester data = nbtPropertyEntry.getValue();
            Tag finalNBTElement = findNBTElement(entityNBT, nbtPropertyEntry.getKey());

            boolean doesTestPass = finalNBTElement == null ? data.wantsBlank : data.tester.apply(finalNBTElement);

            if (data.print) {
                String printString = finalNBTElement == null ? "<NBT component not found>" : finalNBTElement.getAsString();
                ETFUtils2.logMessage(prefix+" property [single] print data: " + nbtPropertyEntry.getKey() + "=" + printString);
                ETFUtils2.logMessage(prefix+" property [single] print result: " + (data.inverts != doesTestPass));

            }
            //break if test fails
            if (data.inverts == doesTestPass){
                return false;
            }
            //otherwise check further nbt
        }
        return true;
    }

    public String formatNbtPretty(CompoundTag nbt) {
        String input = nbt.getAsString();
        StringBuilder output = new StringBuilder();
        int indent = 1;
        boolean inString = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inString && c != '"') {
                output.append(c);
                continue;
            }

            switch (c) {
                case '{', '[' -> {
                    output.append(c).append('\n');
                    indent += 4;
                    output.append(" ".repeat(indent));
                }
                case '}', ']' -> {
                    indent -= 4;
                    output.append('\n').append(" ".repeat(indent)).append(c);
                }
                case ',' -> output.append(c).append('\n').append(" ".repeat(indent));
                case '"' -> {
                    inString = !inString;
                    output.append(c);
                }
                case ':' -> output.append(c).append(" ");
                default -> output.append(c);
            }
        }
        return output.toString()
                .replaceAll("\\{\\s+}","{}")
                .replaceAll("\\[\\s+]","[]");
    }

    private Tag findNBTElement(CompoundTag entityNBT, String nbtIdentifier) {
        Tag lastElement = entityNBT;
        for (String instruction : nbtIdentifier.split("\\.")) {
            //noinspection IfCanBeSwitch
            if (lastElement == null) {
                //ETFUtils2.logWarn("null nbt in ETF");
                return null;
            }
            if (lastElement instanceof CompoundTag nbtCompound) {
                lastElement = nbtCompound.get(instruction);
            } else if (lastElement instanceof CollectionTag<?> nbtList) {
                lastElement = handleListInstruction(nbtList, instruction);
            } else {
                ETFUtils2.logError("Invalid NBT instruction: " + instruction);
                return null;
            }
        }
        return lastElement;
    }

    private Tag handleListInstruction(CollectionTag<?> nbtList, String instruction) {
        if ("*".equals(instruction)) {
            return nbtList;
        } else if (isStringValidInt(instruction)) {
            try {
                return nbtList.get(Integer.parseInt(instruction));
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } else {
            ETFUtils2.logWarn("Invalid list index: " + instruction);
            return null;
        }
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"nbt"};
    }

    @Override
    protected String getPrintableRuleInfo() {
        return null;
    }

    public record NBTTester(boolean inverts, Function<Tag, Boolean> tester, boolean wantsBlank,
                             boolean print) {

        public static NBTTester of(String nbtId, String instructionMaybePrint) throws RandomPropertyException {
            try {
                String step1 = instructionMaybePrint.replaceFirst("^print_all:", "");

                boolean printSingle = step1.startsWith("print:");
                String step2 = printSingle ? step1.substring(6) : step1;

                boolean invert = step2.startsWith("!");
                String instruction = invert ? step2.substring(1) : step2;

                if (instruction.startsWith("raw:")) {
                    String raw = instruction.replaceFirst("raw:", "");
                    boolean blank = raw.isBlank();
                    StringArrayOrRegexProperty.RegexAndPatternPropertyMatcher matcher = blank ? String::isBlank :
                            getStringMatcher_Regex_Pattern_List_Single(raw);
                    if (matcher == null)
                        throw new RandomPropertyException("NBT failed, as raw: instruction was invalid: " + instruction);

                    return new NBTTester(invert,
                            s -> matcher.testString(((Tag) s).getAsString()), blank, printSingle);
                }
                if (instruction.startsWith("exists:")) {
                    boolean exists = instruction.contains("exists:true");
                    boolean notExists = instruction.contains("exists:false");
                    return new NBTTester(invert, s -> exists, notExists, printSingle);
                }
                if (instruction.startsWith("range:")) {
                    SimpleIntegerArrayProperty.IntRange range = SimpleIntegerArrayProperty.getIntRange(instruction.replaceFirst("range:", ""));
                    return new NBTTester(invert, s -> {
                        if (s instanceof NumericTag nbtNumber) {
                            return range.isWithinRange(nbtNumber.getAsNumber().intValue());
                        }
                        ETFUtils2.logWarn("Invalid range for non-number NBT: " + nbtId + "=" + instruction);
                        return false;
                    }, false, printSingle);
                }

                StringArrayOrRegexProperty.RegexAndPatternPropertyMatcher matcher =
                        getStringMatcher_Regex_Pattern_List_Single(instruction);
                if (matcher == null)
                    throw new RandomPropertyException("NBT failed, as instruction was invalid: " + instruction);

                return new NBTTester(invert, s -> {
                    String test = (s instanceof NumericTag) ? ((Tag) s).getAsString().replaceAll("[^\\d.]", "") : ((Tag) s).getAsString();
                    return matcher.testString(test);
                }, false, false);
            } catch (RandomPropertyException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RandomPropertyException("NBT failed, unexpected exception: " + e.getMessage());
            }
        }
    }
}
