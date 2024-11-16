package traben.entity_texture_features.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.features.player.ETFPlayerTexture;

#if MC > MC_21_2
import net.minecraft.client.renderer.texture.SkinTextureDownloader;

@Mixin(SkinTextureDownloader.class)
#else
import net.minecraft.client.renderer.texture.HttpTexture;

@Mixin(HttpTexture.class)
#endif

public abstract class MixinPlayerSkinTexture {


    @Shadow
    private static void setNoAlpha(final NativeImage image, final int x, final int y, final int width, final int height) {
    }

    @Inject(method = "setNoAlpha", cancellable = true, at = @At("HEAD"))
    private static void etf$cancelling(final NativeImage image, final int x1, final int y1, final int x2, final int y2, final CallbackInfo ci) {
        if (ETF.config().getConfig() != null) {
            var mode = ETF.config().getConfig().skinTransparencyMode;

            if (mode == ETFConfig.SkinTransparencyMode.ETF_SKINS_ONLY && ETFPlayerTexture.remappingETFSkin) {
                ci.cancel();
            } else if (mode == ETFConfig.SkinTransparencyMode.ALL) {
                ci.cancel();
            }
        }
    }


#if MC > MC_21_2
    @Inject(method = "processLegacySkin",
            cancellable = true,
            require = 0, //minecraft china will crash with this due to one of their inbuilt mods "netease_official-studio-1.20.jar"
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;setNoAlpha(Lcom/mojang/blaze3d/platform/NativeImage;IIII)V"
                    , shift = At.Shift.BEFORE, ordinal = 0))
    private static void etf$differentAlpha(final NativeImage nativeImage, final String string, final CallbackInfoReturnable<NativeImage> cir) {
        if (ETF.config().getConfig() != null && ETF.config().getConfig().skinTransparencyInExtraPixels) {
            //limit the alpha regions to the uv specifically mapped to the vanilla model only
            etf$alpha(nativeImage, cir);
        }
    }
#else
    @Inject(method = "processLegacySkin",
            cancellable = true,
            require = 0, //minecraft china will crash with this due to one of their inbuilt mods "netease_official-studio-1.20.jar"
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/HttpTexture;setNoAlpha(Lcom/mojang/blaze3d/platform/NativeImage;IIII)V"
                    , shift = At.Shift.BEFORE, ordinal = 0))
    private void etf$differentAlpha(final NativeImage image, final CallbackInfoReturnable<NativeImage> cir) {
        if (ETF.config().getConfig() != null && ETF.config().getConfig().skinTransparencyInExtraPixels) {
            //limit the alpha regions to the uv specifically mapped to the vanilla model only
            etf$alpha(image, cir);
        }
    }
#endif

    @Unique
    private static void etf$alpha(final NativeImage image, final CallbackInfoReturnable<NativeImage> cir) {
        //head
        setNoAlpha(image, 8, 0, 24, 8);
        setNoAlpha(image, 0, 8, 32, 16);

        //og body
        setNoAlpha(image, 4, 16, 12, 20);
        setNoAlpha(image, 20, 16, 36, 20);
        setNoAlpha(image, 44, 16, 52, 20);
        //main
        setNoAlpha(image, 0, 20, 64, 32);

        //alt limbs
        setNoAlpha(image, 20, 48, 28, 52);
        setNoAlpha(image, 36, 48, 44, 52);
        //main
        setNoAlpha(image, 16, 52, 48, 64);

        cir.setReturnValue(image);
    }
}


