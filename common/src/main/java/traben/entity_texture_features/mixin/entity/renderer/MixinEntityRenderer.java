package traben.entity_texture_features.mixin.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.features.ETFRenderContext;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

    @Inject(method = "getPackedLightCoords", at = @At(value = "RETURN"), cancellable = true)
    private void etf$vanillaLightOverrideCancel(T entity, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        //if need to override vanilla brightness behaviour
        //change return with overridden light value still respecting higher block and sky lights
        cir.setReturnValue(ETF.config().getConfig().getLightOverride(
                entity,
                tickDelta,
                cir.getReturnValue()));
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void etf$protectPostRenderersLikeNametag(final CallbackInfo ci) {
        ETFRenderContext.preventRenderLayerTextureModify();
    }

}