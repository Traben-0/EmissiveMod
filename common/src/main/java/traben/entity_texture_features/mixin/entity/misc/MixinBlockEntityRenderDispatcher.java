package traben.entity_texture_features.mixin.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFEntity;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
    @Inject(method =
            #if MC > MC_21_2
            "setupAndRender",
            #else
            "tryRender",
            #endif
            at = @At(value = "HEAD"))
    private static <T extends BlockEntity> void etf$grabContext(
            #if MC > MC_21_2
            final BlockEntityRenderer<T> blockEntityRenderer, final T blockEntity, final float f, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final CallbackInfo ci
            #else
            BlockEntity blockEntity, Runnable runnable, CallbackInfo ci
            #endif

    ) {
        ETFRenderContext.setCurrentEntity((ETFEntity) blockEntity);

    }

    @Inject(method =
            #if MC > MC_21_2
            "setupAndRender",
            #else
            "tryRender",
            #endif
            at = @At(value = "RETURN"))
    private static void etf$clearContext(CallbackInfo ci) {
        ETFRenderContext.reset();
    }


    @ModifyArg(method = "setupAndRender",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"),
            index = 4)
    private static int etf$vanillaLightOverride(final int light) {
        //if need to override vanilla brightness behaviour
        //change return with overridden light value still respecting higher block and sky lights
        return ETF.config().getConfig().getLightOverrideBE(light);
    }


}
