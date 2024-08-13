package traben.entity_texture_features.features;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.utils.ETFEntity;
import traben.entity_texture_features.utils.ETFRenderLayerWithTexture;
import traben.entity_texture_features.utils.ETFVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ETFRenderContext {


    public static boolean renderingFeatures = false;
    private static boolean allowRenderLayerTextureModify = true;
    private static boolean limitModifyToProperties = false;
    private static ETFEntity currentEntity = null;
    private static int currentModelPartDepth = 0;
    private static boolean isInSpecialRenderOverlayPhase = false;
    private static boolean allowedToPatch = false;

    public static CompoundTag cacheEntityNBTForFrame(UUID entityUUID, Supplier<CompoundTag> computeNBT) {
        if(currentEntityNBT == null || !entityUUID.equals(entityNBT_UUID)){
            currentEntityNBT = computeNBT.get();
            entityNBT_UUID = entityUUID;
        }
        return currentEntityNBT;
    }

    private static CompoundTag currentEntityNBT = null;
    private static UUID entityNBT_UUID = null;

    public static boolean isRenderingFeatures() {
        return renderingFeatures;
    }
    public static void setRenderingFeatures(boolean renderingFeatures) {
        ETFRenderContext.renderingFeatures = renderingFeatures;
    }

    public static boolean isAllowedToRenderLayerTextureModify() {
        return allowRenderLayerTextureModify && ETF.config().getConfig().canDoCustomTextures();
    }

    public static void preventRenderLayerTextureModify() {
        ETFRenderContext.allowRenderLayerTextureModify = false;
    }

    public static void allowRenderLayerTextureModify() {
        ETFRenderContext.allowRenderLayerTextureModify = true;
    }

    @Nullable
    public static ETFEntity getCurrentEntity() {
        return currentEntity;
    }

    public static void setCurrentEntity(ETFEntity currentEntity) {
        //assert this
        allowRenderLayerTextureModify = true;
        currentEntityNBT = null;
        entityNBT_UUID = null;
        ETFRenderContext.currentEntity = currentEntity;
    }

    public static boolean canRenderInBrightMode() {
        boolean setForBrightMode = ETFManager.getEmissiveMode() == ETFConfig.EmissiveRenderModes.BRIGHT;
        if (setForBrightMode) {
            if (currentEntity != null) {
                return currentEntity.etf$canBeBright();// && !ETFRenderContext.getCurrentETFTexture().isPatched_CurrentlyOnlyArmor();
            } else {
                //establish default rule
                return true;
            }
        }
        return false;
    }

    public static boolean shouldEmissiveUseCullingLayer() {
        if (currentEntity != null) {
            return currentEntity.etf$isBlockEntity();
        } else {
            //establish default rule
            return true;
        }
    }

    public static int getCurrentModelPartDepth() {
        return currentModelPartDepth;
    }

    public static void incrementCurrentModelPartDepth() {
        currentModelPartDepth++;
    }

    public static void decrementCurrentModelPartDepth() {
        currentModelPartDepth--;
    }

    public static void resetCurrentModelPartDepth() {
        currentModelPartDepth = 0;
    }

    public static void reset() {
        currentModelPartDepth = 0;
        currentEntity = null;
        allowedToPatch = false;
        allowRenderLayerTextureModify = true;
        limitModifyToProperties = false;
        currentEntityNBT = null;
        entityNBT_UUID = null;
    }

    @SuppressWarnings("unused")//used in EMF
    public static boolean isIsInSpecialRenderOverlayPhase() {
        return isInSpecialRenderOverlayPhase;
    }

    public static void startSpecialRenderOverlayPhase() {
        ETFRenderContext.isInSpecialRenderOverlayPhase = true;
    }

    public static void endSpecialRenderOverlayPhase() {
        ETFRenderContext.isInSpecialRenderOverlayPhase = false;
    }

    public static boolean isAllowedToPatch() {
        return allowedToPatch;
    }

    public static void allowTexturePatching() {
        allowedToPatch = true;
    }

    public static void allowOnlyPropertiesRandom(){
        limitModifyToProperties = true;
    }

    public static void allowAllRandom(){
        limitModifyToProperties = false;
    }

    public static boolean isRandomLimitedToProperties(){
        return limitModifyToProperties;
    }

    public static void preventTexturePatching() {
        allowedToPatch = false;
    }

    public static RenderType modifyRenderLayerIfRequired(RenderType value) {

        if (isCurrentlyRenderingEntity()
                && isAllowedToRenderLayerTextureModify()) {
            var layer = ETF.config().getConfig().getRenderLayerOverride();
            if (layer != null
                    && !value.isOutline()
                    && value instanceof ETFRenderLayerWithTexture multiphase) {

                Optional<ResourceLocation> texture = multiphase.etf$getId();
                if (texture.isPresent()) {
                    preventRenderLayerTextureModify();

                    RenderType forReturn = switch (layer) {
                        case TRANSLUCENT -> RenderType.entityTranslucent(texture.get());
                        case TRANSLUCENT_CULL -> RenderType.entityTranslucentCull(texture.get());
                        case END -> RenderType.endGateway();
                        case OUTLINE -> RenderType.outline(texture.get());
                    };
                    allowRenderLayerTextureModify();
                    return forReturn;

                }
            }
        }
        return value;
    }

    public static void insertETFDataIntoVertexConsumer(MultiBufferSource provider, RenderType renderLayer, VertexConsumer vertexConsumer) {
        if (isCurrentlyRenderingEntity() && vertexConsumer instanceof ETFVertexConsumer etfVertexConsumer) {
            //need to store etf texture of consumer and original render layer
            //store provider as well for future actions
            etfVertexConsumer.etf$initETFVertexConsumer(provider, renderLayer);
        }
    }

    public static boolean isCurrentlyRenderingEntity() {
        return currentEntity != null;
    }
}
