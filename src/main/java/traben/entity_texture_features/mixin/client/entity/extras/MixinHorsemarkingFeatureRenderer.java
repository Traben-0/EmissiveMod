package traben.entity_texture_features.mixin.client.entity.extras;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HorseMarkingFeatureRenderer;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import traben.entity_texture_features.client.ETFUtils;

import static traben.entity_texture_features.client.ETFClient.PATH_IS_EXISTING_FEATURE;
import static traben.entity_texture_features.client.ETF_CLIENT.ETFConfigData;

@Mixin(HorseMarkingFeatureRenderer.class)
public abstract class MixinHorsemarkingFeatureRenderer extends FeatureRenderer<HorseEntity, HorseEntityModel<HorseEntity>> {


    public MixinHorsemarkingFeatureRenderer(FeatureRendererContext<HorseEntity, HorseEntityModel<HorseEntity>> context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/passive/HorseEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/HorseEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
                    shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)

    private void etf$applyEmissive(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, HorseEntity horseEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci, Identifier identifier, VertexConsumer vertexConsumer) {
        //UUID id = livingEntity.getUuid();
        ETFUtils.renderEmissiveModel(matrixStack, vertexConsumerProvider, etf$returnAlteredTexture(identifier), (this.getContextModel()), false);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/passive/HorseEntity;FFFFFF)V",
            at = @At(value = "HEAD"))
    private void etf$getEntity(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, HorseEntity horseEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        etf$entity = horseEntity;
    }

    HorseEntity etf$entity = null;

    @ModifyArg(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/passive/HorseEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private Identifier etf$returnAlteredTexture(Identifier texture) {

        if (ETFConfigData.enableCustomTextures) {
            String check = ETFUtils.generalReturnAlreadySetAlteredTexture(texture, etf$entity).toString();
            if (!PATH_IS_EXISTING_FEATURE.containsKey(check)) {
                PATH_IS_EXISTING_FEATURE.put(check, ETFUtils.isExistingNativeImageFile(new Identifier(check)));
            }
            if (PATH_IS_EXISTING_FEATURE.get(check)) {
                return new Identifier(check);
            }
        }
        return texture;
    }
}


