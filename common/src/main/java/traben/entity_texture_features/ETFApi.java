package traben.entity_texture_features;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.config.ETFConfigWarning;
import traben.entity_texture_features.config.ETFConfigWarnings;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.property_reading.PropertiesRandomProvider;
import traben.entity_texture_features.features.property_reading.TrueRandomProvider;
import traben.entity_texture_features.features.property_reading.properties.RandomProperties;
import traben.entity_texture_features.features.property_reading.properties.RandomProperty;
import traben.entity_texture_features.features.property_reading.properties.optifine_properties.BabyProperty;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.Properties;
import java.util.UUID;

/**
 * ETF's api for external mod access (primarily puzzle and EMF at this time)
 *
 * @constants {@link ETFApi#ETFApiVersion}
 * {@link ETFApi#ETF_GENERIC_UUID}<p>
 * @config_handling These methods allow for retrieving, editing, replacing, and saving ETF's config<p>
 * {@link ETFApi#getETFConfigObject()}<p>
 * {@link ETFApi#getCopyOfETFConfigObject()} <p>
 * {@link ETFApi#getDefaultETFConfigObject()} <p>
 * {@link ETFApi#setETFConfigObject(ETFConfig)} <p>
 * {@link ETFApi#saveETFConfigChangesAndResetETF()}
 * @resetting_ETF API method that will reset ETF to recalculate after any changes<p>
 * {@link ETFApi#resetETF()}
 * @get_variant_textures API methods for returning valid variant textures given an Entity, and it's default texture<p>
 * {@link ETFApi#getCurrentETFVariantTextureOfEntity(Entity, ResourceLocation)} <p>
 * {@link ETFApi#getCurrentETFVariantTextureOfBlockEntity(BlockEntity, ResourceLocation)}
 * @get_emissive_textures API methods for returning valid emissive textures given an Entity, and it's default texture<p>
 * These methods will first find the variant of the texture and then find that variants emissive texture<p>
 * {@link ETFApi#getCurrentETFEmissiveTextureOfEntityOrNull(Entity, ResourceLocation)} <p>
 * {@link ETFApi#getCurrentETFEmissiveTextureOfBlockEntityOrNull(BlockEntity, ResourceLocation)}
 * @automatic_emissive_rendering These methods will automatically find an entities emissive texture and render it on a given Model | ModelPart <p>
 * These methods should be placed directly after the initial models render, and before the MatrixStack's modifications are popped or changed<p>
 * {@link ETFApi#renderETFEmissiveModel(Entity, ResourceLocation, PoseStack, MultiBufferSource, Model)} <p>
 * {@link ETFApi#renderETFEmissiveModel(BlockEntity, ResourceLocation, PoseStack, MultiBufferSource, Model)}<p>
 * {@link ETFApi#renderETFEmissiveModelPart(Entity, ResourceLocation, PoseStack, MultiBufferSource, ModelPart)} <p>
 * {@link ETFApi#renderETFEmissiveModelPart(BlockEntity, ResourceLocation, PoseStack, MultiBufferSource, ModelPart)}
 * @adding_custom_optifine_properties This method allows registering custom {@link RandomProperty} objects to be included in OptiFine property file testing.<p>
 * {@link ETFApi#registerCustomRandomPropertyFactory(String, RandomProperties.RandomPropertyFactory...)}
 * @adding_custom_warnings This method allows registering custom {@link ETFConfigWarning} objects to be included in the ETF config screens and mod compatibility fixes.<p>
 * {@link ETFApi#registerCustomETFConfigWarning(String, ETFConfigWarning...)}
 * @recent_rule_matches These methods allow retrieving the latest matched OptiFine property rule index of a given entity.<p>
 * {@link ETFApi#getLastMatchingRuleOfEntity(Entity)}<p>
 * {@link ETFApi#getLastMatchingRuleOfBlockEntity(BlockEntity)}
 * @optifine_property_reading This method allows an external mod to send in the path of an OptiFine random properties file and return an object<p>
 * that can test specific entities to discover their assigned random suffix<p>
 * {@link ETFApi#getVariantSupplierOrNull(ResourceLocation, ResourceLocation, String...)} <p>
 * {@link ETFVariantSuffixProvider}
 */
@SuppressWarnings({"unused", "ConstantValue", "JavadocDeclaration"})
public final class ETFApi {

    /**
     * The current ETF API version.
     */
    public static final int ETFApiVersion = 9;
    /**
     * This UUID if passed into ETF will tell it to skip looking for variants
     */
    public static final UUID ETF_GENERIC_UUID = UUID.nameUUIDFromBytes(("GENERIC").getBytes());
    /**
     * This is the value that is used for mob spawner UUID's least significant bits.
     * <p>
     * (uuid.getLeastSignificantBits() == ETF_SPAWNER_MARKER) is true for all mob spawner mini entities
     * <p>
     * This is how it appears in NBT as an int list with 4 values "[I;?,?,12345,12345]" making it identifiable there too
     */
    public static final long ETF_SPAWNER_MARKER = (12345L << 32) + 12345L;
    @Deprecated
    public static ETFConfig getETFConfigObject = null;

    /**
     * provides access to the live ETF config object to read AND modify its values
     * please be sure to run the save config method below after any changes
     *
     * @return the etf config object
     */
    public static ETFConfig getETFConfigObject() {
        return ETF.config().getConfig();
    }

    /**
     * sets the current config object used by etf, this will also save the config and reset etf
     *
     * @param newETFConfig the new ETF config to be saved and used by ETF
     */
    public static void setETFConfigObject(ETFConfig newETFConfig) {
        if (newETFConfig != null) {
            ETF.config().setConfig(newETFConfig);
            saveETFConfigChangesAndResetETF();
        } else {
            ETFUtils2.logError("new config was null: ignoring.");
        }
    }

    /**
     * returns a copy of the ETF config object that can be freely modified without affecting ETF in runtime
     *
     * @return the copy of ETF's current config object
     */
    public static ETFConfig getCopyOfETFConfigObject() {
        return ETF.config().copyOfConfig();
    }

    /**
     * returns a new ETF config object with default settings
     *
     * @return a default ETF config object
     */
    public static ETFConfig getDefaultETFConfigObject() {
        return new ETFConfig();
    }

    /**
     * gets the translation key for a block entity type
     *
     * @param type the block entity type
     * @return the translation key
     */
    public static String getBlockEntityTypeToTranslationKey(BlockEntityType<?> type) {
        var id = BlockEntityType.getKey(type);
        if (id == null) return null;
        return "block." + id.getNamespace() + '.' + id.getPath();
    }

    /**
     * saves any config changes to the live ETF config to file and resets ETF to function with the new settings
     */
    public static void saveETFConfigChangesAndResetETF() {
        ETF.config().saveToFile();
        ETFManager.resetInstance();
    }

    /**
     * resets ETF in its entirety, ETF will re asses all textures and properties files and recalculate all variants
     */
    public static void resetETF() {
        ETFManager.resetInstance();
    }


    /**
     * pass in a block entity and receive the UUID ETF will recognise it as
     *
     * @param blockEntity the block entity
     * @return the UUID ETF recognises for that block Entity
     */
    public static UUID getUUIDForBlockEntity(BlockEntity blockEntity) {
        final var blockEntityState = blockEntity.getBlockState();
        long most = blockEntityState == null ? Long.MAX_VALUE : blockEntityState.hashCode();
        final var pos = blockEntity.getBlockPos();
        long least = pos == null ? 0 : pos.asLong();
        return new UUID(most, least);
    }

    /**
     * pass in an entity, and its default texture and receive the current variant of that texture or the default if no variant exists
     *
     * @param entity         the entity
     * @param defaultTexture the default texture
     * @return the variant texture or the defaultTexture if no variant exists
     */
    @NotNull
    public static ResourceLocation getCurrentETFVariantTextureOfEntity(@NotNull Entity entity, @NotNull ResourceLocation defaultTexture) {
        if (entity != null) {
            ETFEntity etfEntity = (ETFEntity) entity;
            ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTexture, etfEntity);
            if (etfTexture != null) {// just in case
                ResourceLocation etfIdentifier = etfTexture.getTextureIdentifier(etfEntity);
                if (etfIdentifier != null) {// just in case
                    return etfIdentifier;
                }
            }
        }
        return defaultTexture;
    }

    /**
     * pass in a block entity, and its default texture and receive the current variant of that texture or the default if no variant exists
     *
     * @param entity         the entity
     * @param defaultTexture the default texture
     *                       (send the hash of any extra identifying type parameter your block entity might have, e.g. facing direction)
     * @return the variant texture or the defaultTexture if no variant exists
     */
    public static ResourceLocation getCurrentETFVariantTextureOfBlockEntity(@NotNull BlockEntity entity, @NotNull ResourceLocation defaultTexture) {
        if (entity != null) {
            ETFEntity etfEntity = (ETFEntity) entity;
            return getCurrentETFVariantTextureOfBlockEntityInternal(etfEntity, defaultTexture);
        }
        return defaultTexture;
    }

    public static ResourceLocation getCurrentETFVariantTextureOfBlockEntity(@NotNull BlockEntity entity, @NotNull ResourceLocation defaultTexture, @NotNull UUID specifiedUUID) {
        if (entity != null) {
            ETFEntity etfEntity = (ETFEntity) entity;
            return getCurrentETFVariantTextureOfBlockEntityInternal(etfEntity, defaultTexture);
        }
        return defaultTexture;
    }

    private static ResourceLocation getCurrentETFVariantTextureOfBlockEntityInternal(@NotNull ETFEntity etfEntity, @NotNull ResourceLocation defaultTexture) {
        ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTexture, etfEntity);
        if (etfTexture != null) {// just in case
            ResourceLocation etfIdentifier = etfTexture.getTextureIdentifier(etfEntity);
            if (etfIdentifier != null) {// just in case
                return etfIdentifier;
            }
        }
        return defaultTexture;
    }

    @NotNull
    @Deprecated
    public static ResourceLocation getCurrentETFVariantTextureOfEntity(@NotNull BlockEntity entity, @NotNull ResourceLocation defaultTexture, UUID ignore) {
        return getCurrentETFVariantTextureOfBlockEntity(entity, defaultTexture);
    }

    /**
     * pass in an entity, and it's default texture and receive it's current emissive texture if it exists else returns null
     *
     * @param entity         the entity
     * @param defaultTexture the default texture
     * @return the emissive texture or null if it doesn't exist
     */
    @Nullable
    public static ResourceLocation getCurrentETFEmissiveTextureOfEntityOrNull(@NotNull Entity entity, @NotNull ResourceLocation defaultTexture) {
        if (entity != null) {
            ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTexture, (ETFEntity) entity);
            if (etfTexture != null) {// just in case
                return etfTexture.getEmissiveIdentifierOfCurrentState();
            }
        }
        return null;
    }

    /**
     * pass in an entity, and it's default texture and receive it's current emissive texture if it exists else returns null
     *
     * @param entity         the entity
     * @param defaultTexture the default texture
     * @return the emissive texture or null if it doesn't exist
     */
    @Nullable
    public static ResourceLocation getCurrentETFEmissiveTextureOfBlockEntityOrNull(@NotNull BlockEntity entity, @NotNull ResourceLocation defaultTexture) {
        if (entity != null) {
            ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTexture, (ETFEntity) entity);
            if (etfTexture != null) {// just in case
                return etfTexture.getEmissiveIdentifierOfCurrentState();
            }
        }
        return null;
    }

    /**
     * To render your entity using its emissive textures you can simply call this method sometime after
     * your entity model is rendered, but before you pop or modify the matrix stack
     *
     * @param entity                 the entity
     * @param defaultTextureOfEntity the default (non-variant) texture of entity
     * @param matrixStack            the matrix stack
     * @param vertexConsumerProvider the vertex consumer provider
     * @param model                  the model to render
     */
    public static void renderETFEmissiveModel(
            @NotNull Entity entity,
            @NotNull ResourceLocation defaultTextureOfEntity,
            @NotNull PoseStack matrixStack,
            @NotNull MultiBufferSource vertexConsumerProvider,
            @NotNull Model model
    ) {
        ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTextureOfEntity, (ETFEntity) entity);
        if (etfTexture != null) {// just in case
            etfTexture.renderEmissive(matrixStack, vertexConsumerProvider, model);
        }
    }

    /**
     * same method as {@link ETFApi#renderETFEmissiveModel(Entity, ResourceLocation, PoseStack, MultiBufferSource, Model)} but for an individual ModelPart
     *
     * @param entity                 the entity
     * @param defaultTextureOfEntity the default (non-variant) texture of entity
     * @param matrixStack            the matrix stack
     * @param vertexConsumerProvider the vertex consumer provider
     * @param modelPart              the model part to render
     */
    public static void renderETFEmissiveModelPart(
            @NotNull Entity entity,
            @NotNull ResourceLocation defaultTextureOfEntity,
            @NotNull PoseStack matrixStack,
            @NotNull MultiBufferSource vertexConsumerProvider,
            @NotNull ModelPart modelPart
    ) {
        ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTextureOfEntity, (ETFEntity) entity);
        if (etfTexture != null) {// just in case
            etfTexture.renderEmissive(matrixStack, vertexConsumerProvider, modelPart);
        }
    }

    /**
     * To render your entity using its emissive textures you can simply call this method sometime after
     * your entity model is rendered, but before you pop or modify the matrix stack
     *
     * @param entity                 the entity
     * @param defaultTextureOfEntity the default (non-variant) texture of entity
     * @param matrixStack            the matrix stack
     * @param vertexConsumerProvider the vertex consumer provider
     * @param model                  the model to render
     */
    public static void renderETFEmissiveModel(
            @NotNull BlockEntity entity,
            @NotNull ResourceLocation defaultTextureOfEntity,
            @NotNull PoseStack matrixStack,
            @NotNull MultiBufferSource vertexConsumerProvider,
            @NotNull Model model
    ) {
        ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTextureOfEntity, (ETFEntity) entity);
        if (etfTexture != null) {// just in case
            etfTexture.renderEmissive(matrixStack, vertexConsumerProvider, model);
        }
    }

    /**
     * same method as {@link ETFApi#renderETFEmissiveModel(Entity, ResourceLocation, PoseStack, MultiBufferSource, Model)} but for an individual ModelPart
     *
     * @param entity                 the entity
     * @param defaultTextureOfEntity the default (non-variant) texture of entity
     * @param matrixStack            the matrix stack
     * @param vertexConsumerProvider the vertex consumer provider
     * @param modelPart              the model part to render
     */
    public static void renderETFEmissiveModelPart(
            @NotNull BlockEntity entity,
            @NotNull ResourceLocation defaultTextureOfEntity,
            @NotNull PoseStack matrixStack,
            @NotNull MultiBufferSource vertexConsumerProvider,
            @NotNull ModelPart modelPart
    ) {
        ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(defaultTextureOfEntity, (ETFEntity) entity);
        if (etfTexture != null) {// just in case
            etfTexture.renderEmissive(matrixStack, vertexConsumerProvider, modelPart);
        }
    }

    /**
     * This should only be used if you want to handle your own variation code.
     * This is used by EMF for random model variations.
     * <p>.<p>
     * creates an instance of {@link ETFVariantSuffixProvider}.
     * this method will return null for any failure and should print some relevant information on the failure reason.
     * the return from this method only requires
     * {@link ETFVariantSuffixProvider#getSuffixForEntity(Entity)}
     * to be called to retrieve a suffix integer,
     * the suffix may or may not exist, it is up to you to test if the file with that suffix actually exists.
     * see the JavaDocs within the object itself for further info.
     *
     * @param propertiesFileIdentifier the properties file identifier
     * @param vanillaIdentifier        the vanilla file identifier
     * @param suffixKeys               the suffix keys to use. These would be {"skins","textures"} for regular OptiFine random textures and "models" for OptiFine random entity models.
     * @return a valid {@link ETFVariantSuffixProvider} or null.
     */
    public static @Nullable ETFVariantSuffixProvider getVariantSupplierOrNull(ResourceLocation propertiesFileIdentifier, ResourceLocation vanillaIdentifier, String... suffixKeys) {
        return ETFVariantSuffixProvider.getVariantProviderOrNull(propertiesFileIdentifier, vanillaIdentifier, suffixKeys);
    }


    @Deprecated
    public static int getLastMatchingRuleOfEntity(Entity entity) {
        return 0;
    }

    @Deprecated
    public static int getLastMatchingRuleOfBlockEntity(BlockEntity entity) {
        return 0;
    }

    /**
     * Register new {@link RandomProperty} objects to be included in OptiFine property file testing.
     * <p>For example {@link BabyProperty#getPropertyOrNull(Properties, int)}
     * is the factory sent here for the object {@link BabyProperty}
     * representing the "baby" OptiFine property.
     *
     * @param yourModId your Mod's ID
     * @param factories the {@link RandomProperties.RandomPropertyFactory} of your custom {@link RandomProperty} object to be registered.
     * @usage_examples {@link RandomProperties}
     */
    public static void registerCustomRandomPropertyFactory(String yourModId, RandomProperties.RandomPropertyFactory... factories) {
        if (factories != null && factories.length != 0) {
            RandomProperties.register(factories);
            ETFUtils2.logMessage(factories.length + " new ETF Random Properties registered by " + yourModId);
        }
    }

    /**
     * Pass in custom warnings to be displayed to ETF users, given certain conditions.
     *
     * @param yourModId your Mod's ID
     * @param warnings  one or more instance of {@link ETFConfigWarning} to be listed on the ETF config warning screen
     */
    public static void registerCustomETFConfigWarning(String yourModId, ETFConfigWarning... warnings) {
        if (warnings != null && warnings.length != 0) {
            ETFConfigWarnings.registerConfigWarning(warnings);
            ETFUtils2.logMessage(warnings.length + " new ETF Config Warnings registered by " + yourModId);
        }
    }

    /**
     * This should only be used if you want to handle your own variation code.
     * This is used by EMF for random model variations.
     * <p>.<p>
     * provides functionality to input an entity and output a suffix integer as defined in either:<p>
     * - a valid OptiFine random entity properties file.<p>
     * - non property random variation (E.G. having a *2.png texture and so forth).
     * <p>
     * Should be built via {@link ETFApi#getVariantSupplierOrNull(ResourceLocation, ResourceLocation, String...)}
     */
    public interface ETFVariantSuffixProvider {

        @Nullable
        static ETFApi.ETFVariantSuffixProvider getVariantProviderOrNull(ResourceLocation propertiesFileIdentifier, ResourceLocation vanillaIdentifier, String... suffixKeyName) {
            //get optifine property provider or null
            PropertiesRandomProvider optifine = PropertiesRandomProvider.of(propertiesFileIdentifier, vanillaIdentifier, suffixKeyName);
            //get true random provider or null
            TrueRandomProvider random = ETFRenderContext.isRandomLimitedToProperties() ? null : TrueRandomProvider.of(vanillaIdentifier);

            //try fallback property if null
            if (optifine == null
                    && vanillaIdentifier.getPath().endsWith(".png")
                    && "minecraft".equals(vanillaIdentifier.getNamespace())
                    && vanillaIdentifier.getPath().contains("_")) {
                String vanId = vanillaIdentifier.getPath().replaceAll("_(tame|angry|nectar|shooting|cold)", "");
                optifine = PropertiesRandomProvider.of(ETFUtils2.res(vanId.replace(".png", ".properties")), ETFUtils2.res(vanId), suffixKeyName);
            }

            if (random == null && optifine == null) {
                //no variation at all
                return null;
            } else if (/*only*/ optifine == null) {
                return random;
            } else if (/*only*/ random == null) {
                //optifine random confirmed
                return optifine;
            } else {
                //if 2.png is higher it MUST be treated as true random
                if (optifine.getPackName() != null
                        && optifine.getPackName().equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(
                        random.getPackName(), optifine.getPackName()))) {
                    return optifine;
                } else {
                    return random;
                }
            }
        }

        /**
         * @return a boolean confirming if the entity's variant can be updated
         */
        boolean entityCanUpdate(UUID uuid);

        /**
         * @return all the suffixes mentioned in this OptiFine property file
         */
        IntOpenHashSet getAllSuffixes();

        /**
         * @return the amount of rules in this OptiFine property file
         */
        int size();

        /**
         * This method will accept an entity and some additional args and will output a variant suffix integer that matches
         * the OptiFine cases outlined in the properties file, it ONLY outputs an integer, testing whether that
         * variant number exists or not is up to you.
         *
         * @param entityToBeTested the entity to be tested
         * @return the suffix number for this entity. An output of 0 ALWAYS means you need to use the vanilla variant,
         * usually due to finding no match.
         * <p> An output of 1, can be handled in 2 ways, usually it is used to refer to the vanilla suffix, but you might
         * also choose to check for a #1 suffix, I would recommend using 1 to mean the vanilla/default variant.
         */
        default int getSuffixForEntity(Entity entityToBeTested) {
            return getSuffixForETFEntity((ETFEntity) entityToBeTested);

        }

        /**
         * Same as {@link ETFVariantSuffixProvider#getSuffixForEntity(Entity)} but for block entities
         *
         * @param entityToBeTested the block entity to be tested
         * @return the suffix number for the block entity
         */

        default int getSuffixForBlockEntity(BlockEntity entityToBeTested) {
            return getSuffixForETFEntity((ETFEntity) entityToBeTested);
        }

        /**
         * Same as {@link ETFVariantSuffixProvider#getSuffixForEntity(Entity)} but for the internal use ETFEntity interface
         *
         * @param entityToBeTested the block entity to be tested
         * @return the suffix number for the block entity
         */
        int getSuffixForETFEntity(ETFEntity entityToBeTested);

        /**
         * sets the method by which the random number to select a suffix is generated. The default returns
         * ETFEntity.getUuid().hashCode() which means the same entity returns the same seed each time.
         * This affects the suffix chosen when multiple are supplied.
         *
         * @param entityRandomSeedFunction the function to generate the seed
         */
        void setRandomSupplier(EntityRandomSeedFunction entityRandomSeedFunction);


        interface EntityRandomSeedFunction {
            int toInt(ETFEntity entity);
        }

    }


}
