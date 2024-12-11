package traben.entity_texture_features.mixin.entity.renderer;

import net.minecraft.client.renderer.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFEntity;


@Mixin(IllusionerRenderer.class)
public abstract class MixinIllusionerRenderer {

    @Unique
    private ETFEntity etf$heldEntity = null;

    @Inject(method =
           #if MC > MC_21
            "render(Lnet/minecraft/client/renderer/entity/state/IllusionerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            #else
            "render(Lnet/minecraft/world/entity/monster/Illusioner;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            #endif
            at = @At(value = "HEAD"))
    private void etf$start(CallbackInfo ci) {
        etf$heldEntity = ETFRenderContext.getCurrentEntity();
    }

    @Inject(method =
           #if MC > MC_21
            "render(Lnet/minecraft/client/renderer/entity/state/IllusionerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            #else
            "render(Lnet/minecraft/world/entity/monster/Illusioner;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            #endif
            at = @At(value = "INVOKE", target =
                    #if MC > MC_21
                    "Lnet/minecraft/client/renderer/entity/IllagerRenderer;render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    #elif MC > MC_20_6
                    "Lnet/minecraft/client/renderer/entity/IllagerRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    #else
                    "Lnet/minecraft/client/renderer/entity/IllagerRenderer;render(Lnet/minecraft/world/entity/Mob;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    #endif
                    shift = At.Shift.BEFORE))
    private void etf$loop(CallbackInfo ci) {
        //assert main entity each loop
        ETFRenderContext.setCurrentEntity(etf$heldEntity);
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.endSpecialRenderOverlayPhase();
    }
}


