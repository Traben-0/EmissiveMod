package traben.entity_texture_features.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.mixin.mods.sodium.MixinModelPartSodium;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.ETFVertexConsumer;

/**
 * this method figures out if a {@link ModelPart} is the top level of the children tree being rendered,
 * then applies overlay rendering like emissives and enchanted pixels.
 * <p>
 * this is copied in {@link MixinModelPartSodium} for sodium's alternative model part render method.
 * <p>
 * the priority is set so this method will never run before sodium cancels the vanilla rendering code.
 */
@Mixin(value = ModelPart.class, priority = 2000)
public abstract class MixinModelPart {

#if MC < MC_21
    @Shadow
    public abstract void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha);
#else
    @Shadow public abstract void render(final PoseStack poseStack, final VertexConsumer vertexConsumer, final int i, final int j, final int k);

#endif




    #if MC < MC_21
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",at = @At(value = "HEAD"))
    private void etf$findOutIfInitialModelPart(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {

    #else
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",at = @At(value = "HEAD"))
    private void etf$findOutIfInitialModelPart(final PoseStack poseStack, final VertexConsumer vertexConsumer, final int i, final int j, final int k, final CallbackInfo ci) {
    #endif
        ETFRenderContext.incrementCurrentModelPartDepth();
    }

    #if MC < MC_21
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",at = @At(value = "RETURN"))
    private void etf$doEmissiveIfInitialPart(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {

    #else
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",at = @At(value = "RETURN"))
    private void etf$doEmissiveIfInitialPart(final PoseStack matrices, final VertexConsumer vertices, final int light, final int overlay, final int k, final CallbackInfo ci) {
    #endif
        //run code if this is the initial topmost rendered part
        if (ETFRenderContext.getCurrentModelPartDepth() > 1) {
            ETFRenderContext.decrementCurrentModelPartDepth();
        } else {
            //top level model so try special rendering
            if (ETFRenderContext.isCurrentlyRenderingEntity()
                    && vertices instanceof ETFVertexConsumer etfVertexConsumer) {
                ETFTexture texture = etfVertexConsumer.etf$getETFTexture();
                //is etf texture not null and does it special render?
                if (texture != null && (texture.isEmissive() || texture.isEnchanted())) {
                    MultiBufferSource provider = etfVertexConsumer.etf$getProvider();
                    //very important this is captured before doing the special renders as they can potentially modify
                    //the same ETFVertexConsumer down stream
                    RenderType layer = etfVertexConsumer.etf$getRenderLayer();
                    //are these render required objects valid?
                    if (provider != null && layer != null) {
                        //attempt special renders as eager OR checks
                        ETFUtils2.RenderMethodForOverlay renderer = (a, b) -> render(matrices, a, b, overlay, #if MC < MC_21 red, green, blue, alpha #else k #endif);
                        if (ETFUtils2.renderEmissive(texture, provider, renderer, matrices)
                                | ETFUtils2.renderEnchanted(texture, provider, light, renderer)
                        ) {
                            //reset render layer stuff behind the scenes if special renders occurred
                            //this will also return ETFVertexConsumer held data to normal if the same ETFVertexConsumer
                            //was previously affected by a special render
                            #if MC < MC_21 provider.getBuffer(layer); #endif
                        }
                    }
                }
            }
            //ensure model count is reset
            ETFRenderContext.resetCurrentModelPartDepth();
        }
    }
    #if MC >= MC_21


    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
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
}






