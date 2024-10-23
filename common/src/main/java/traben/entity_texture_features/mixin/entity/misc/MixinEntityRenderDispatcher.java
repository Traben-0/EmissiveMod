package traben.entity_texture_features.mixin.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFEntity;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Inject(method =
            #if MC > MC_21
            "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At(value = "HEAD"))
    private <E extends Entity, S extends EntityRenderState> void etf$grabContext(final E entity, final double d, final double e, final double f, final float g, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, final EntityRenderer<? super E, S> entityRenderer, final CallbackInfo ci) {
            #else
            "render",
            at = @At(value = "HEAD"))
    private <E extends Entity> void etf$grabContext(E entity, double x, double y, double z, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
            #endif
        ETFRenderContext.setCurrentEntity((ETFEntity) entity);

//        ETFRenderContext.setCurrentProvider(vertexConsumers);

    }

    @Inject(method =
            #if MC > MC_21
            "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            #else
                "render",
                #endif
            at = @At(value = "RETURN"))
    private <E extends Entity> void etf$clearContext(CallbackInfo ci) {
        ETFRenderContext.reset();

    }


//    @ModifyArg(
//            method = "render",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
//            index = 4
//    )
//    private VertexConsumerProvider etf$injectIntoGetBuffer(VertexConsumerProvider vertexConsumers) {
//        return layer -> ETFRenderContext.processVertexConsumer(vertexConsumers, layer);
//    }
}
