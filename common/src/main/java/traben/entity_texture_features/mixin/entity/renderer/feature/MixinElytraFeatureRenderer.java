package traben.entity_texture_features.mixin.entity.renderer.feature;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.features.ETFRenderContext;
#if MC > MC_21
import net.minecraft.client.renderer.entity.layers.WingsLayer;
@Mixin(WingsLayer.class)
public abstract class MixinElytraFeatureRenderer<T extends LivingEntity> {
#else
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
@Mixin(ElytraLayer.class)
public abstract class MixinElytraFeatureRenderer<T extends LivingEntity> {
#endif

    @Inject(method =
            #if MC > MC_21
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"
            #else
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"
            #endif
            ,
            at = @At(value = "HEAD"))
    private void etf$markPatchable(CallbackInfo ci) {
        ETFRenderContext.allowTexturePatching();
    }

    @Inject(method =
            #if MC > MC_21
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"
            #else
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"
            #endif
            ,
            at = @At(value = "RETURN"))
    private void etf$markPatchableEnd(CallbackInfo ci) {
        ETFRenderContext.preventTexturePatching();
    }
}


