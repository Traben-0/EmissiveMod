package traben.entity_texture_features.features.property_reading.properties.optifine_properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import traben.entity_texture_features.utils.ETFUtils2;


public class BlocksProperty extends StringArrayOrRegexProperty {

    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAP_PRINTER = new Function<>() {
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            } else {
                Property<?> property = entry.getKey();
                String var10000 = property.getName();
                return var10000 + "=" + this.nameValue(property, entry.getValue());
            }
        }

        private <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
            //noinspection unchecked
            return property.getName((T) value);
        }
    };
    protected final Function<BlockState, Boolean> blockStateMatcher;
    protected final boolean botherWithDeepStateCheck;

    protected BlocksProperty(Properties properties, int propertyNum, String[] ids) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, ids).replaceAll("(?<=(^| ))minecraft:", ""));
        if (usesRegex) {
            blockStateMatcher = (blockState) -> {
                if (MATCHER.testString(getFromStateBlockNameOnly(blockState))) {
                    return true;
                } else {
                    return MATCHER.testString(getFromStateBlockNameWithStateData(blockState));
                }
            };
            botherWithDeepStateCheck = false;
        } else {
            blockStateMatcher = this::testBlocks;
            boolean hasStateNeeds = false;
            for (String s : ARRAY) {
                if (s.contains(":")) {
                    hasStateNeeds = true;
                    break;
                }
            }
            botherWithDeepStateCheck = hasStateNeeds;
        }
    }

    public static BlocksProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new BlocksProperty(properties, propertyNum, new String[]{"blocks", "block"});
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    protected String getFromStateBlockNameOnly(BlockState state) {
        String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().replaceFirst("minecraft:", "");
        if (doPrint)ETFUtils2.logMessage("Blocks property print (no blockstate data): [" + block+"]");
        return block;
    }

    private String getFromStateBlockNameWithStateData(BlockState state) {
        String block = getFromStateBlockNameOnly(state);
        if (!state.getValues().isEmpty())
            block = block + ':' + state.getValues().entrySet().stream().map(PROPERTY_MAP_PRINTER).collect(Collectors.joining(":"));

        if (doPrint)ETFUtils2.logMessage("Blocks property print (with blockstate data): [" + block+"]");
        return block;
    }

    protected boolean testBlocks(BlockState blockState) {
        //is array only non regex
        if (MATCHER.testString(getFromStateBlockNameOnly(blockState))) {
            return true;
        } else if (botherWithDeepStateCheck) {
            String fullBlockState = getFromStateBlockNameWithStateData(blockState);
            for (String string : ARRAY) {
                if (string.contains(":")) {
                    //block has state requirements
                    boolean matchesAllStateDataNeeded = true;
                    for (String split : string.split(":")) {
                        //check only the declared state data is present so foreach by the declaration
                        if (!fullBlockState.contains(split)) {
                            matchesAllStateDataNeeded = false;
                            break;
                        }
                    }
                    //if so loop can continue
                    if (matchesAllStateDataNeeded) return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return true;
    }

    @Nullable
    protected BlockState[] getTestingBlocks(ETFEntity entity){
        if (entity.etf$getUuid().getLeastSignificantBits() == ETFApi.ETF_SPAWNER_MARKER) {
            // entity is a mini mob spawner entity
            // return a blank mob spawner block state
            return new BlockState[]{Blocks.SPAWNER.defaultBlockState()};
        } else if (entity instanceof BlockEntity blockEntity) {
            if (blockEntity.getLevel() == null) {
                return new BlockState[]{blockEntity.getBlockState()};
            } else {
                return new BlockState[]{blockEntity.getBlockState(), blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().below())};
            }
        } else {
            if (entity.etf$getWorld() == null || entity.etf$getBlockPos() == null){
                return null;
            }
            Level world = entity.etf$getWorld();
            BlockPos pos = entity.etf$getBlockPos();
            return new BlockState[]{world.getBlockState(pos), world.getBlockState(pos.below())};
        }
    }

    @Override
    public boolean testEntityInternal(ETFEntity entity) {
        BlockState[] entityBlocks = getTestingBlocks(entity);
        if (entityBlocks == null){
            if (doPrint) ETFUtils2.logMessage("Blocks property print result: [false], because null");
            return false;
        }

        if (doPrint) ETFUtils2.logMessage("Blocks property print, found blocks: [" + Arrays.toString(entityBlocks) + "]");

        for (BlockState entityBlock : entityBlocks) {
            //check each block before returning false
            if (blockStateMatcher.apply(entityBlock)){
                if (doPrint) ETFUtils2.logMessage("Blocks property print result: [true]");
                return true;
            }
        }
        if (doPrint) ETFUtils2.logMessage("Blocks property print result: [false]");
        return false;
    }


    @Override
    public @Nullable String getValueFromEntity(ETFEntity etfEntity) {
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"blocks", "block"};
    }

}
