package traben.entity_texture_features.features.property_reading.properties.generic_properties;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class SemVerRangeFromStringArrayProperty extends NumberRangeFromStringArrayProperty<SemVerRangeFromStringArrayProperty.SemVerNumber> {


    protected SemVerRangeFromStringArrayProperty(String string) throws RandomPropertyException {
        super(string);
    }


    @Override
    protected @Nullable RangeTester<SemVerRangeFromStringArrayProperty.SemVerNumber> getRangeTesterFromString(String possibleRange) {
        try {
            String[] str = possibleRange.split("(?<!^|-)-");
            SemVerNumber left = new SemVerNumber(str[0]);
            SemVerNumber right = str.length > 1 ? new SemVerNumber(str[1]) : null;

            if (str.length < 2 || left.sameAs(right)) {
                return (value) -> value.sameAs(left);
            } else if (right.largerThan(left)) {
                return (value) -> value.betweenInclusive(left, right);
            } else {
                return (value) -> value.betweenInclusive(right, left);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static class SemVerNumber extends Number {

        private final int[] versions;

        public SemVerNumber(String value) {
            this.versions = Arrays.stream(value.split("\\."))
                    .map(SemVerNumber::parse)
                    .mapToInt(i -> i)
                    .toArray();
        }

        private static int parse(String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return 0;
            }

        }

        public boolean sameAs(SemVerNumber other) {
            if (versions.length != other.versions.length) {
                return false;
            }
            for (int i = 0; i < versions.length; i++) {
                if (versions[i] != other.versions[i]) {
                    return false;
                }
            }
            return true;
        }



        public boolean betweenInclusive(SemVerNumber smaller, SemVerNumber larger) {
            return largerThanOrEqual(smaller) && smallerThanOrEqual(larger);
        }

        public boolean largerThanOrEqual(SemVerNumber other) {
            return largerThan(other) || sameAs(other);
        }

        public boolean smallerThanOrEqual(SemVerNumber other) {
            return smallerThan(other) || sameAs(other);
        }

        public boolean largerThan(SemVerNumber other) {
            for (int i = 0; i < Math.min(versions.length, other.versions.length); i++) {
                if (versions[i] > other.versions[i]) {
                    return true;
                } else if (versions[i] < other.versions[i]) {
                    return false;
                }
            }
            return versions.length > other.versions.length;
        }

        public boolean smallerThan(SemVerNumber other) {
            for (int i = 0; i < Math.min(versions.length, other.versions.length); i++) {
                if (versions[i] < other.versions[i]) {
                    return true;
                } else if (versions[i] > other.versions[i]) {
                    return false;
                }
            }
            return versions.length < other.versions.length;
        }

        @Override
        public int intValue() {
            return versions[0];
        }

        @Override
        public long longValue() {
            return versions[0];
        }

        @Override
        public float floatValue() {
            return versions[0];
        }

        @Override
        public double doubleValue() {
            return versions[0];
        }
    }

}
