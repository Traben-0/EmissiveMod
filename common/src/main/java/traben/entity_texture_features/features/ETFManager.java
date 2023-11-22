package traben.entity_texture_features.features;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETFClientCommon;
import traben.entity_texture_features.ETFVersionDifferenceHandler;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.config.screens.skin.ETFConfigScreenSkinTool;
import traben.entity_texture_features.features.player.ETFPlayerEntity;
import traben.entity_texture_features.features.player.ETFPlayerTexture;
import traben.entity_texture_features.features.property_reading.RandomPropertiesFile;
import traben.entity_texture_features.features.property_reading.RandomPropertyRule;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFCacheKey;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFLruCache;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.*;

import static traben.entity_texture_features.ETFClientCommon.ETFConfigData;
import static traben.entity_texture_features.ETFClientCommon.MOD_ID;

// ETF re-write
//this class will ideally be where everything in vanilla interacts to get ETF stuff done
public class ETFManager {

    public static final UUID ETF_GENERIC_UUID = UUID.nameUUIDFromBytes(("GENERIC").getBytes());
    private static final ETFTexture ETF_ERROR_TEXTURE = getErrorETFTexture();
    private static ETFManager manager;
    public final ETFLruCache<UUID, Integer> LAST_MET_RULE_INDEX = new ETFLruCache<>();
    /*
     * Storage reasoning
     *
     * for every storage map using an entity that cannot be stored in a fast-util primitive type
     * will utilise a cache that can clear contents after reaching certain sizes to prevent exceeding memory
     *
     * for every storage map keyed by a vanilla or confirmed existing texture they will remain as non clearing maps as they have an intrinsic upper size limit
     *
     *the rewrite relies heavily on minimizing processing time during play by
     *  setting up the textures once and then passing already calculated objects when required to speed up render time.
     * a big part of this is minimizing texture lookups and storing that info in fastUtil maps
     *
     */
    public final ObjectOpenHashSet<String> EMISSIVE_SUFFIX_LIST = new ObjectOpenHashSet<>();
    public final ETFLruCache<ETFCacheKey, ETFTexture> ENTITY_TEXTURE_MAP = new ETFLruCache<>();
    public final ETFLruCache<UUID, ETFPlayerTexture> PLAYER_TEXTURE_MAP = new ETFLruCache<>();
    public final Object2LongOpenHashMap<UUID> ENTITY_BLINK_TIME = new Object2LongOpenHashMap<>();
    public final Object2ObjectOpenHashMap<UUID, ETFCacheKey> UUID_TO_MOB_CACHE_KEY_MAP_FOR_FEATURE_USAGE = new Object2ObjectOpenHashMap<>();
    public final ArrayList<String> KNOWN_RESOURCEPACK_ORDER = new ArrayList<>();
    public final Object2IntOpenHashMap<EntityType<?>> ENTITY_TYPE_VANILLA_BRIGHTNESS_OVERRIDE_VALUE = new Object2IntOpenHashMap<>();
    public final ObjectOpenHashSet<EntityType<?>> ENTITY_TYPE_IGNORE_PARTICLES = new ObjectOpenHashSet<>();
    public final Object2IntOpenHashMap<EntityType<?>> ENTITY_TYPE_RENDER_LAYER = new Object2IntOpenHashMap<>();
    public final ETFLruCache<UUID, Object2BooleanOpenHashMap<RandomProperty>> ENTITY_SPAWN_CONDITIONS_CACHE = new ETFLruCache<>();
    //null means it is true random as in no properties
    public final Object2ReferenceOpenHashMap<Identifier, RandomPropertiesFile> OPTIFINE_PROPERTY_CACHE = new Object2ReferenceOpenHashMap<>();
    //this is a cache of all known ETFTexture versions of any existing resource-pack texture, used to prevent remaking objects
    private final Object2ReferenceOpenHashMap<@NotNull Identifier, @Nullable ETFTexture> ETF_TEXTURE_CACHE = new Object2ReferenceOpenHashMap<>();
    private final Object2BooleanOpenHashMap<UUID> ENTITY_IS_UPDATABLE = new Object2BooleanOpenHashMap<>();
    private final ObjectOpenHashSet<UUID> ENTITY_UPDATE_QUEUE = new ObjectOpenHashSet<>();
    private final Object2ObjectOpenHashMap<UUID, ObjectOpenHashSet<ETFCacheKey>> ENTITY_KNOWN_FEATURES_LIST = new Object2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<UUID> ENTITY_DEBUG_QUEUE = new ObjectOpenHashSet<>();
    //contains the total number of variants for any given vanilla texture
    private final Object2IntOpenHashMap<Identifier> TRUE_RANDOM_COUNT_CACHE = new Object2IntOpenHashMap<>();

    public Boolean mooshroomBrownCustomShroomExists = null;
    //marks whether mooshroom mushroom overrides exist
    public Boolean mooshroomRedCustomShroomExists = null;
    public ETFTexture redMooshroomAlt = null;
    public ETFTexture brownMooshroomAlt = null;


    private ETFManager() {


        for (ResourcePack pack :
                MinecraftClient.getInstance().getResourceManager().streamResourcePacks().toList()) {
            KNOWN_RESOURCEPACK_ORDER.add(pack.getName());
        }

        try {
            List<Properties> props = new ArrayList<>();
            String[] paths = {"optifine/emissive.properties", "textures/emissive.properties", "etf/emissive.properties"};
            for (String path :
                    paths) {
                Properties prop = ETFUtils2.readAndReturnPropertiesElseNull(new Identifier(path));
                if (prop != null)
                    props.add(prop);
            }
            for (Properties prop :
                    props) {
                //not an optifine property that I know of but this has come up in a few packs, so I am supporting it
                if (prop.containsKey("entities.suffix.emissive")) {
                    if (prop.getProperty("entities.suffix.emissive") != null)
                        EMISSIVE_SUFFIX_LIST.add(prop.getProperty("entities.suffix.emissive"));
                }
                if (prop.containsKey("suffix.emissive")) {
                    if (prop.getProperty("suffix.emissive") != null)
                        EMISSIVE_SUFFIX_LIST.add(prop.getProperty("suffix.emissive"));
                }
            }
            if (ETFConfigData.alwaysCheckVanillaEmissiveSuffix) {
                EMISSIVE_SUFFIX_LIST.add("_e");
            }

            if (EMISSIVE_SUFFIX_LIST.isEmpty()) {
                ETFUtils2.logMessage("no emissive suffixes found: default emissive suffix '_e' used");
                EMISSIVE_SUFFIX_LIST.add("_e");
            } else {
                ETFUtils2.logMessage("emissive suffixes loaded: " + EMISSIVE_SUFFIX_LIST);
            }
        } catch (Exception e) {
            ETFUtils2.logError("emissive suffixes could not be read: default emissive suffix '_e' used");
            EMISSIVE_SUFFIX_LIST.add("_e");
        }
        ENTITY_TYPE_VANILLA_BRIGHTNESS_OVERRIDE_VALUE.defaultReturnValue(0);
        ENTITY_TYPE_RENDER_LAYER.defaultReturnValue(0);
    }

    public static ETFManager getInstance() {
        if (manager == null)
            manager = new ETFManager();
        return manager;
    }

    public static void resetInstance() {
        ETFUtils2.KNOWN_NATIVE_IMAGES = new ETFLruCache<>();
        ETFClientCommon.etf$loadConfig();
        ETFDirectory.resetCache();

        //instance based format solves the issue of hashmaps and arrays being clearing while also being accessed
        //as now those rare transitional (reading during clearing) occurrences will simply read from the previous instance of manager
        manager = new ETFManager();
    }


    public static ETFTexture getErrorETFTexture() {
        ETFUtils2.registerNativeImageToIdentifier(ETFUtils2.emptyNativeImage(), new Identifier(MOD_ID, "error.png"));
        return new ETFTexture(new Identifier(MOD_ID, "error.png")/*, false*/);//, ETFTexture.TextureSource.GENERIC_DEBUG);
    }

    public static EmissiveRenderModes getEmissiveMode() {
        if (ETFConfigData.emissiveRenderMode == EmissiveRenderModes.BRIGHT
                && ETFRenderContext.getCurrentEntity() != null
                && !ETFRenderContext.getCurrentEntity().etf$canBeBright()) {
            return EmissiveRenderModes.DULL;
        }
        return ETFConfigData.emissiveRenderMode;
    }

    public static void processNewOptiFinePropertiesFile(ETFEntity entity, Identifier vanillaIdentifier, Identifier properties) {
        ETFManager manager = getInstance();
        try {
            Properties props = ETFUtils2.readAndReturnPropertiesElseNull(properties);

            if (props != null) {
                //check for etf entity properties
                if (props.containsKey("vanillaBrightnessOverride")) {
                    String value = props.getProperty("vanillaBrightnessOverride").trim();
                    int tryNumber;
                    try {
                        tryNumber = Integer.parseInt(value.replaceAll("\\D", ""));
                    } catch (NumberFormatException e) {
                        tryNumber = 0;
                    }
                    if (tryNumber >= 16) tryNumber = 15;
                    if (tryNumber < 0) tryNumber = 0;
                    manager.ENTITY_TYPE_VANILLA_BRIGHTNESS_OVERRIDE_VALUE.put(entity.etf$getType(), tryNumber);
                }

                if (props.containsKey("suppressParticles")
                        && "true".equals(props.getProperty("suppressParticles"))) {
                    manager.ENTITY_TYPE_IGNORE_PARTICLES.add(entity.etf$getType());
                }

                if (props.containsKey("entityRenderLayerOverride")) {
                    String layer = props.getProperty("entityRenderLayerOverride");
                    //noinspection EnhancedSwitchMigration
                    switch (layer) {
                        case "translucent":
                            manager.ENTITY_TYPE_RENDER_LAYER.put(entity.etf$getType(), 1);
                            break;
                        case "translucent_cull":
                            manager.ENTITY_TYPE_RENDER_LAYER.put(entity.etf$getType(), 2);
                            break;
                        case "end_portal":
                            manager.ENTITY_TYPE_RENDER_LAYER.put(entity.etf$getType(), 3);
                            break;
                        case "outline":
                            manager.ENTITY_TYPE_RENDER_LAYER.put(entity.etf$getType(), 4);
                            break;
                    }
                }
                List<RandomPropertyRule> allCasesForTexture = RandomPropertiesFile.getAllValidPropertyObjects(props, vanillaIdentifier, "skins", "textures");

                if (!allCasesForTexture.isEmpty()) {
                    //it all worked now just get the first texture called and everything is set for the next time the texture is called for fast processing
                    manager.OPTIFINE_PROPERTY_CACHE.put(vanillaIdentifier, RandomPropertiesFile.of(allCasesForTexture));
                } else {
                    ETFUtils2.logMessage("Ignoring properties file that failed to load any cases @ " + vanillaIdentifier, false);
                    manager.OPTIFINE_PROPERTY_CACHE.put(vanillaIdentifier, null);
                }
            } else {//properties file is null
                ETFUtils2.logMessage("Ignoring properties file that was null @ " + vanillaIdentifier, false);
                manager.OPTIFINE_PROPERTY_CACHE.put(vanillaIdentifier, null);
            }
        } catch (Exception e) {
            ETFUtils2.logWarn("Ignoring properties file that caused Exception @ " + vanillaIdentifier + "\n" + e, false);
            e.printStackTrace();
            manager.OPTIFINE_PROPERTY_CACHE.put(vanillaIdentifier, null);
        }
    }

    public void removeThisEntityDataFromAllStorage(ETFCacheKey ETFId) {
        ENTITY_TEXTURE_MAP.removeEntryOnly(ETFId);
        //ENTITY_FEATURE_MAP.clear();


        UUID uuid = ETFId.getMobUUID();
        ENTITY_SPAWN_CONDITIONS_CACHE.removeEntryOnly(uuid);
        ENTITY_IS_UPDATABLE.removeBoolean(uuid);
        ENTITY_UPDATE_QUEUE.remove(uuid);
        ENTITY_DEBUG_QUEUE.remove(uuid);
        ENTITY_BLINK_TIME.removeLong(uuid);
        UUID_TO_MOB_CACHE_KEY_MAP_FOR_FEATURE_USAGE.remove(uuid);
    }

    public void checkIfShouldTriggerUpdate(UUID id) {
        //type safe check as returns false if missing

        if (ENTITY_IS_UPDATABLE.getBoolean(id)
                && ETFConfigData.enableCustomTextures
                && ETFConfigData.textureUpdateFrequency_V2 != ETFConfig.UpdateFrequency.Never) {
            if (ENTITY_UPDATE_QUEUE.size() > 2000)
                ENTITY_UPDATE_QUEUE.clear();
            int delay = ETFConfigData.textureUpdateFrequency_V2.getDelay();
            long randomizer = delay * 20L;
            if (System.currentTimeMillis() % randomizer == Math.abs(id.hashCode()) % randomizer
            ) {
                //marks texture to update next render if a certain delay time is reached
                ENTITY_UPDATE_QUEUE.add(id);
            }
        }
    }

    public void markEntityForDebugPrint(UUID id) {
        if (ETFConfigData.debugLoggingMode != ETFConfig.DebugLogMode.None) {
            ENTITY_DEBUG_QUEUE.add(id);
        }
    }

    @NotNull
    public ETFTexture getETFTextureNoVariation(Identifier vanillaIdentifier) {
        return getOrCreateETFTexture(vanillaIdentifier);
    }

    @NotNull
    public ETFTexture getETFTexture(@NotNull Identifier vanillaIdentifier, @Nullable ETFEntity entity, @NotNull TextureSource source) {
        try {
            if (entity == null) {
                //this should only purposefully call for features like armor or elytra that append to players and have no ETF customizing
                return getETFTextureNoVariation(vanillaIdentifier);
            }
            UUID id = entity.etf$getUuid();
            //use custom cache id this differentiates feature renderer calls here and makes the base feature still identifiable by uuid only when features are called
            ETFCacheKey cacheKey = new ETFCacheKey(id, vanillaIdentifier); //source == TextureSource.ENTITY_FEATURE ? vanillaIdentifier : null);
            if (source == TextureSource.ENTITY) {
                //this is so feature renderers can find the 'base texture' of the mob to test it's variant if required
                UUID_TO_MOB_CACHE_KEY_MAP_FOR_FEATURE_USAGE.put(id, cacheKey);
            }

            //fastest in subsequent runs
            if (id == ETF_GENERIC_UUID || entity.etf$getBlockPos().equals(Vec3i.ZERO)) {
                return getETFTextureNoVariation(vanillaIdentifier);
            }
            if (ENTITY_TEXTURE_MAP.containsKey(cacheKey)) {
                ETFTexture quickReturn = ENTITY_TEXTURE_MAP.get(cacheKey);
                if (quickReturn == null) {
                    ETFTexture vanillaETF = getETFTextureNoVariation(vanillaIdentifier);
                    ENTITY_TEXTURE_MAP.put(cacheKey, vanillaETF);
                    quickReturn = vanillaETF;

                }
                if (source == TextureSource.ENTITY) {
                    if (ENTITY_DEBUG_QUEUE.contains(id)) {
                        boolean inChat = ETFConfigData.debugLoggingMode == ETFConfig.DebugLogMode.Chat;
                        ETFUtils2.logMessage(
                                "\nGeneral ETF:" +
                                        "\n - Texture cache size: " + ETF_TEXTURE_CACHE.size() +
                                        "\nThis " + entity.etf$getType().toString() + ":" +
                                        "\n - Texture: " + quickReturn + "\nEntity cache size: " + ENTITY_TEXTURE_MAP.size() +
                                        "\n - Original spawn state: " + ENTITY_SPAWN_CONDITIONS_CACHE.get(id) +
                                        "\n - OptiFine property count: " + (OPTIFINE_PROPERTY_CACHE.containsKey(vanillaIdentifier) && OPTIFINE_PROPERTY_CACHE.get(vanillaIdentifier) != null ? OPTIFINE_PROPERTY_CACHE.get(vanillaIdentifier).size() : 0) +
                                        "\n - Non property random total: " + TRUE_RANDOM_COUNT_CACHE.getInt(vanillaIdentifier), inChat);

                        ENTITY_DEBUG_QUEUE.remove(id);
                    }
                    if (ENTITY_UPDATE_QUEUE.contains(id)) {//&& source != TextureSource.ENTITY_FEATURE) {
                        Identifier newVariantIdentifier = returnNewAlreadyConfirmedOptifineTexture(entity, vanillaIdentifier, false);
                        ENTITY_TEXTURE_MAP.put(cacheKey, Objects.requireNonNullElse(getOrCreateETFTexture(Objects.requireNonNullElse(newVariantIdentifier, vanillaIdentifier)), getETFTextureNoVariation(vanillaIdentifier)));

                        //only if changed
                        if (!quickReturn.thisIdentifier.equals(newVariantIdentifier)) {
                            //iterate over list of all known features and update them
                            ObjectOpenHashSet<ETFCacheKey> featureSet = ENTITY_KNOWN_FEATURES_LIST.getOrDefault(id, new ObjectOpenHashSet<>());
                            //possible concurrent editing of hashmap issues but simplest way to perform this
                            featureSet.forEach((forKey) -> {
                                Identifier forVariantIdentifier = getPossibleVariantIdentifierRedirectForFeatures(entity, forKey.identifier(), TextureSource.ENTITY_FEATURE); //  returnNewAlreadyConfirmedOptifineTexture(entity, forKey.identifier(), true);
                                ENTITY_TEXTURE_MAP.put(forKey, Objects.requireNonNullElse(getOrCreateETFTexture(Objects.requireNonNullElse(forVariantIdentifier, forKey.identifier())), getETFTextureNoVariation(forKey.identifier())));

                            });
                        }

                        ENTITY_UPDATE_QUEUE.remove(id);
                    } else {
                        checkIfShouldTriggerUpdate(id);
                    }
                }
                //this is where 99.99% of calls here will end only the very first call to this method by an entity goes further
                //the first call by any entity of a type will go the furthest and be the slowest as it triggers the initial setup, this makes all future calls by the same entity type faster
                //this is as close as possible to method start I can move this without losing update and debug functionality
                //this is the focal point of the rewrite where all the optimization is expected
                return quickReturn;
            }
            //need to create or find an ETFTexture object for entity and find or add to cache and entity map
            //firstly just going to check if this mob is some sort of gui element or not a real mob


            Identifier possibleIdentifier;
            if (source == TextureSource.ENTITY_FEATURE) {
                possibleIdentifier = getPossibleVariantIdentifierRedirectForFeatures(entity, vanillaIdentifier, source);
            } else {
                possibleIdentifier = getPossibleVariantIdentifier(entity, vanillaIdentifier, source);
            }

            ETFTexture foundTexture;
            foundTexture = Objects.requireNonNullElse(getOrCreateETFTexture(possibleIdentifier == null ? vanillaIdentifier : possibleIdentifier), getETFTextureNoVariation(vanillaIdentifier));
            //if(!(source == TextureSource.ENTITY_FEATURE && possibleIdentifier == null))

            // replace with vanilla non-variant texture if it is a variant and the path is vanilla and this has been disabled in config
            if (ETFConfigData.disableVanillaDirectoryVariantTextures
                    && !foundTexture.thisIdentifier.equals(vanillaIdentifier)
                    && ETFDirectory.getDirectoryOf(foundTexture.thisIdentifier) == ETFDirectory.VANILLA) {
                foundTexture = getETFTextureNoVariation(vanillaIdentifier);
            }
            ENTITY_TEXTURE_MAP.put(cacheKey, foundTexture);
            if (source == TextureSource.ENTITY_FEATURE) {
                ObjectOpenHashSet<ETFCacheKey> knownFeatures = ENTITY_KNOWN_FEATURES_LIST.getOrDefault(entity.etf$getUuid(), new ObjectOpenHashSet<>());
                knownFeatures.add(cacheKey);
                ENTITY_KNOWN_FEATURES_LIST.put(entity.etf$getUuid(), knownFeatures);
            }
            return foundTexture;

        } catch (Exception e) {
            ETFUtils2.logWarn("ETF Texture error! if this happens more than a couple times, then something is wrong");
            return getETFTextureNoVariation(vanillaIdentifier);
        }
    }

    @Nullable //when vanilla
    private Identifier getPossibleVariantIdentifierRedirectForFeatures(ETFEntity entity, Identifier vanillaIdentifier, TextureSource source) {


        Identifier regularReturnIdentifier = getPossibleVariantIdentifier(entity, vanillaIdentifier, source);
        //if the feature does not have a .properties file and returns the vanilla file or null check if we can copy the base texture's variant
        if (OPTIFINE_PROPERTY_CACHE.get(vanillaIdentifier) == null &&
                (regularReturnIdentifier == null || vanillaIdentifier.equals(regularReturnIdentifier))
        ) {
            //random assignment either failed or returned texture1
            //as this is a feature we will also try one last time to match it to a possible variant of the base texture

            ETFCacheKey baseCacheId = UUID_TO_MOB_CACHE_KEY_MAP_FOR_FEATURE_USAGE.get(entity.etf$getUuid()); //new ETFCacheKey(entity.getUuid(), null);

            if (baseCacheId != null && ENTITY_TEXTURE_MAP.containsKey(baseCacheId)) {
                ETFTexture baseETFTexture = ENTITY_TEXTURE_MAP.get(baseCacheId);
                if (baseETFTexture != null) {
                    return baseETFTexture.getFeatureTexture(vanillaIdentifier);
                }
            }

        } else {
            return regularReturnIdentifier;
        }
        return null;
    }

    @Nullable //when vanilla
    private Identifier getPossibleVariantIdentifier(ETFEntity entity, Identifier vanillaIdentifier, TextureSource source) {

        if (ETFConfigData.enableCustomTextures) {
            //has this been checked before?
            if (TRUE_RANDOM_COUNT_CACHE.containsKey(vanillaIdentifier) || OPTIFINE_PROPERTY_CACHE.containsKey(vanillaIdentifier)) {
                //has optifine checked before?
                if (OPTIFINE_PROPERTY_CACHE.containsKey(vanillaIdentifier)) {
                    RandomPropertiesFile optifineProperties = OPTIFINE_PROPERTY_CACHE.get(vanillaIdentifier);
                    if (optifineProperties != null) {
                        return returnNewAlreadyConfirmedOptifineTexture(entity, vanillaIdentifier, true, optifineProperties);
                    }
                }
                //has true random checked before?
                if (TRUE_RANDOM_COUNT_CACHE.containsKey(vanillaIdentifier) && source != TextureSource.ENTITY_FEATURE) {
                    int randomCount = TRUE_RANDOM_COUNT_CACHE.getInt(vanillaIdentifier);
                    if (randomCount != TRUE_RANDOM_COUNT_CACHE.defaultReturnValue()) {
                        return returnNewAlreadyConfirmedTrueRandomTexture(entity, vanillaIdentifier, randomCount);
                    }
                }
                //if we got here the texture is NOT random after having already checked before so return null
                return null;
            }


            //this is a new texture, we need to find what kind of random it is

            //if not null the below two represent the highest version of said files
            Identifier possibleProperty = ETFDirectory.getDirectoryVersionOf(ETFUtils2.replaceIdentifier(vanillaIdentifier, ".png", ".properties"));
            Identifier possible2PNG = ETFDirectory.getDirectoryVersionOf(ETFUtils2.addVariantNumberSuffix(vanillaIdentifier, 2));
            //try fallback properties
            if (possibleProperty == null && "minecraft".equals(vanillaIdentifier.getNamespace()) && vanillaIdentifier.getPath().contains("_")) {
                String vanId = vanillaIdentifier.getPath().replaceAll("(_tame|_angry|_nectar|_shooting|_cold)", "");
                possibleProperty = ETFDirectory.getDirectoryVersionOf(new Identifier(vanId.replace(".png", ".properties")));
            }

            //if both null vanilla fallback as no randoms
            if (possible2PNG == null && possibleProperty == null) {
                //this will tell next call with this texture that these have been checked already
                OPTIFINE_PROPERTY_CACHE.put(vanillaIdentifier, null);
                return null;
            } else if (/*only*/possibleProperty == null) {
                if (source != TextureSource.ENTITY_FEATURE) {
                    newTrueRandomTextureFound(vanillaIdentifier, possible2PNG);
                    return returnNewAlreadyConfirmedTrueRandomTexture(entity, vanillaIdentifier);
                }
            } else if (/*only*/possible2PNG == null) {
                //optifine random confirmed
                processNewOptiFinePropertiesFile(entity, vanillaIdentifier, possibleProperty);
                return returnNewAlreadyConfirmedOptifineTexture(entity, vanillaIdentifier, true);
            } else //noinspection CommentedOutCode
            {//neither null this will be annoying
                //if 2.png is higher it MUST be treated as true random confirmed
                ResourceManager resources = MinecraftClient.getInstance().getResourceManager();
                String p2pngPackName = resources.getResource(possible2PNG).isPresent() ? resources.getResource(possible2PNG).get().getResourcePackName() : null;
                String propertiesPackName = resources.getResource(possibleProperty).isPresent() ? resources.getResource(possibleProperty).get().getResourcePackName() : null;
                //ObjectOpenHashSet<String> packs = new ObjectOpenHashSet<>();
                //if (p2pngPackName != null)
                //packs.add(p2pngPackName);
                //if (propertiesPackName != null)
                //packs.add(propertiesPackName);

                // System.out.println("debug6534="+p2pngPackName+","+propertiesPackName+","+ETFUtils2.returnNameOfHighestPackFrom(packs));
                if (propertiesPackName != null && propertiesPackName.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(new String[]{p2pngPackName, propertiesPackName}))) {
                    processNewOptiFinePropertiesFile(entity, vanillaIdentifier, possibleProperty);
                    return returnNewAlreadyConfirmedOptifineTexture(entity, vanillaIdentifier, true);
                } else {
                    if (source != TextureSource.ENTITY_FEATURE) {
                        newTrueRandomTextureFound(vanillaIdentifier, possible2PNG);
                        return returnNewAlreadyConfirmedTrueRandomTexture(entity, vanillaIdentifier);
                    }
                }
            }
        }
        //marker to signify code has run before and is not random or true random
        OPTIFINE_PROPERTY_CACHE.put(vanillaIdentifier, null);
        //use vanilla as fallback
        return null;
    }

    private void newTrueRandomTextureFound(Identifier vanillaIdentifier, Identifier variant2PNG) {
        //here 2.png is confirmed to exist and has its directory already applied
        //I'm going to ignore 1.png that will be hardcoded as vanilla or optifine replaced
        ResourceManager resources = MinecraftClient.getInstance().getResourceManager();
        int totalTextureCount = 2;
        while (resources.getResource(ETFUtils2.replaceIdentifier(variant2PNG, "[0-9]+(?=\\.png)", String.valueOf((totalTextureCount + 1)))).isPresent()) {
            totalTextureCount++;
        }
        //here totalTextureCount == the confirmed last value of the random order
        //System.out.println("total true random was="+totalTextureCount);
        TRUE_RANDOM_COUNT_CACHE.put(vanillaIdentifier, totalTextureCount);

        //make sure to return first check
        //return returnAlreadyConfirmedTrueRandomTexture(entity,vanillaIdentifier,totalTextureCount);
        //can't return null as 2.png confirmed exists
    }

    @Nullable
    private Identifier returnNewAlreadyConfirmedOptifineTexture(ETFEntity entity, Identifier vanillaIdentifier, boolean firstTimeForEntity) {
        return returnNewAlreadyConfirmedOptifineTexture(entity, vanillaIdentifier, firstTimeForEntity, OPTIFINE_PROPERTY_CACHE.get(vanillaIdentifier));
    }

    @Nullable
    private Identifier returnNewAlreadyConfirmedOptifineTexture(ETFEntity entity, Identifier vanillaIdentifier, boolean firstTimeForEntity, RandomPropertiesFile optifineProperties) {

        int variantNumber = optifineProperties.getSuffixForETFEntity(entity, firstTimeForEntity, ENTITY_IS_UPDATABLE);

        Identifier variantIdentifier = returnNewAlreadyNumberedRandomTexture(vanillaIdentifier, variantNumber);
        if (variantIdentifier == null) {
            return null;
        }
        //must test these exist
        if (ETF_TEXTURE_CACHE.containsKey(variantIdentifier)) {
            if (ETF_TEXTURE_CACHE.get(variantIdentifier) == null) {
                return null;
            }
            //then we know it exists
            return variantIdentifier;
        }
        Optional<Resource> variantResource = MinecraftClient.getInstance().getResourceManager().getResource(variantIdentifier);
        if (variantResource.isPresent()) {
            return variantIdentifier;
            //it will be added to cache for future checks later
        } else {
            ETF_TEXTURE_CACHE.put(variantIdentifier, null);
        }
        //ETFUtils.logError("texture assign has failed, vanilla texture has been used as fallback");

        return null;
    }


    @NotNull
    private Identifier returnNewAlreadyConfirmedTrueRandomTexture(ETFEntity entity, Identifier vanillaIdentifier) {
        return returnNewAlreadyConfirmedTrueRandomTexture(entity, vanillaIdentifier, TRUE_RANDOM_COUNT_CACHE.getInt(vanillaIdentifier));
    }

    @NotNull
    private Identifier returnNewAlreadyConfirmedTrueRandomTexture(ETFEntity entity, Identifier vanillaIdentifier, int totalCount) {
        int randomReliable = Math.abs(entity.etf$getUuid().hashCode());
        randomReliable %= totalCount;
        randomReliable++;
        //no need to test as they have already all been confirmed existing by code
        Identifier toReturn = returnNewAlreadyNumberedRandomTexture(vanillaIdentifier, randomReliable);
        return toReturn == null ? vanillaIdentifier : toReturn;
    }

    @Nullable
    private Identifier returnNewAlreadyNumberedRandomTexture(Identifier vanillaIdentifier, int variantNumber) {
        return ETFDirectory.getDirectoryVersionOf(ETFUtils2.addVariantNumberSuffix(vanillaIdentifier, variantNumber));
    }


    @NotNull
    private ETFTexture getOrCreateETFTexture(Identifier ofIdentifier) {
        if (ETF_TEXTURE_CACHE.containsKey(ofIdentifier)) {
            //use cached ETFTexture
            ETFTexture cached = ETF_TEXTURE_CACHE.get(ofIdentifier);
            if (cached != null) {
                return cached;
            }
        } else {
            //create new ETFTexture and cache it
            ETFTexture newTexture = new ETFTexture(ofIdentifier);
            ETF_TEXTURE_CACHE.put(ofIdentifier, newTexture);
            return newTexture;
        }
        ETFUtils2.logError("getOrCreateETFTexture reached the end and should not have");
        return ETF_ERROR_TEXTURE;
    }

    @Nullable
    public ETFPlayerTexture getPlayerTexture(PlayerEntity player, Identifier rendererGivenSkin) {
        return getPlayerTexture((ETFPlayerEntity) player, rendererGivenSkin);
    }

    @Nullable
    public ETFPlayerTexture getPlayerTexture(ETFPlayerEntity player, Identifier rendererGivenSkin) {
        try {
            UUID id = player.etf$getUuid();
            if (PLAYER_TEXTURE_MAP.containsKey(id)) {
                ETFPlayerTexture possibleSkin = PLAYER_TEXTURE_MAP.get(id);
                if (possibleSkin == null ||
                        (possibleSkin.player == null && possibleSkin.isCorrectObjectForThisSkin(rendererGivenSkin))) {
                    return null;
                } else if (possibleSkin.isCorrectObjectForThisSkin(rendererGivenSkin)
                        || MinecraftClient.getInstance().currentScreen instanceof ETFConfigScreenSkinTool) {
                    return possibleSkin;
                }

            }
            PLAYER_TEXTURE_MAP.put(id, null);
            ETFPlayerTexture etfPlayerTexture = new ETFPlayerTexture(player, rendererGivenSkin);
            PLAYER_TEXTURE_MAP.put(id, etfPlayerTexture);
            return etfPlayerTexture;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public enum TextureSource {
        ENTITY,
        BLOCK_ENTITY,
        ENTITY_FEATURE
    }

    public enum EmissiveRenderModes {
        DULL,
        BRIGHT//,
        //COMPATIBLE
        ;


        @Override
        public String toString() {
            return switch (this) {
                case DULL -> ETFVersionDifferenceHandler.getTextFromTranslation(
                        "config.entity_texture_features.emissive_mode.dull").getString();
                case BRIGHT -> ETFVersionDifferenceHandler.getTextFromTranslation(
                        "config.entity_texture_features.emissive_mode.bright").getString();
//                default -> ETFVersionDifferenceHandler.getTextFromTranslation(
//                        "config.entity_texture_features.emissive_mode.compatible").getString();
            };
        }

        public EmissiveRenderModes next() {
            if (this == DULL)
                return BRIGHT;
            return DULL;
        }
    }

}
