package traben.entity_texture_features.mixin.mods.skin_layers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.mixin.MixinModelPart;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.ETFVertexConsumer;

/**
 * this is a copy of {@link MixinModelPart}
 */
@Pseudo
@Mixin(value = CustomizableModelPart.class)// implements Mesh
public abstract class MixinCustomizableModelPart {

    #if MC > MC_20_4
    @Shadow
    public abstract void render(final ModelPart vanillaModel, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int light, final int overlay, final int color);
    #else
    @Shadow
    public abstract void render(ModelPart vanillaModel, PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha);
    #endif

    @Inject(method =
            #if MC > MC_20_4
            "render(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            #else
            "render(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            #endif
            at = @At(value = "HEAD"))
    private void etf$findOutIfInitialModelPart(CallbackInfo ci) {
        if (ETF.config().getConfig().use3DSkinLayerPatch) {
            ETFRenderContext.incrementCurrentModelPartDepth();
        }
    }

#if MC >= MC_21
    @ModifyVariable(method = "render(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private VertexConsumer etf$modify(final VertexConsumer value) {
        if (value instanceof BufferBuilder builder && !builder.building){
            if (value instanceof ETFVertexConsumer etf
                    && etf.etf$getRenderLayer() != null
                    && etf.etf$getProvider() != null){
                return etf.etf$getProvider().getBuffer(etf.etf$getRenderLayer());
            }
        }
        return value;
    }
#endif

    @Inject(method =
            #if MC > MC_20_4
            "render(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            #else
            "render(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            #endif
            at = @At(value = "RETURN"))
    private void etf$doEmissive(
            #if MC > MC_20_4
            final ModelPart vanillaModel, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int light, final int overlay, final int color, final CallbackInfo ci
            #else
            ModelPart vanillaModel, PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci
            #endif
            ) {
        if (ETF.config().getConfig().use3DSkinLayerPatch) {
            //run code if this is the initial topmost rendered part
            if (ETFRenderContext.getCurrentModelPartDepth() != 1) {
                ETFRenderContext.decrementCurrentModelPartDepth();
            } else {
                if (ETFRenderContext.isCurrentlyRenderingEntity()
                        && vertexConsumer instanceof ETFVertexConsumer etfVertexConsumer) {
                    ETFTexture texture = etfVertexConsumer.etf$getETFTexture();
                    if (texture != null && (texture.isEmissive() || texture.isEnchanted())) {
                        MultiBufferSource provider = etfVertexConsumer.etf$getProvider();
                        RenderType layer = etfVertexConsumer.etf$getRenderLayer();
                        if (provider != null && layer != null) {
                            //attempt special renders as eager OR checks
                            ETFUtils2.RenderMethodForOverlay renderer =
                                    (a, b) -> render(vanillaModel, poseStack, a, b, overlay,
                                            #if MC > MC_20_4 color #else red, green, blue, alpha #endif);

                            if (ETFUtils2.renderEmissive(texture, provider, renderer) |
                                    ETFUtils2.renderEnchanted(texture, provider, light, renderer)) {
                                //reset render layer stuff behind the scenes if special renders occurred
                                provider.getBuffer(layer);
                            }
                        }
                    }
                }
                //ensure model count is reset
                ETFRenderContext.resetCurrentModelPartDepth();
            }
        }
    }


}
