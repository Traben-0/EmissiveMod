package traben.entity_texture_features.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.utils.ETFUtils2;


@Mixin(ResourceLocation.class)
public abstract class MixinIdentifier {


    @Inject(method = "isValidPath", cancellable = true, at = @At("RETURN"), require = 0)
    private static void etf$illegalPathOverride(String path, CallbackInfoReturnable<Boolean> cir) {
        if (ETF.config().getConfig() != null) {
            if (ETF.config().getConfig().illegalPathSupportMode != ETFConfig.IllegalPathMode.None) {
                if (!cir.getReturnValue() && path != null) {


                    switch (ETF.config().getConfig().illegalPathSupportMode) {
                        case Entity -> {
                            if ((path.contains("/entity/") || path.contains("optifine/") || path.contains("etf/") || path.contains("emf/"))
                                    && (path.endsWith(".png") || path.endsWith(".properties") || path.endsWith(".mcmeta") || path.endsWith(".jem") || path.endsWith(".jpm"))) {
                                ETFUtils2.logWarn(ETF.getTextFromTranslation("config.entity_texture_features.illegal_path_warn").getString()
                                        + " [" + path + "]");

                                // String filename =path.replace(".png", "");
                                // String[] split = filename.split("/");
                                //ETFManager.getInstance().EXCUSED_ILLEGAL_PATHS.add(split[split.length-1]);

                                cir.setReturnValue(true);
                            }
                        }
                        case All -> {
                            ETFUtils2.logWarn(ETF.getTextFromTranslation("config.entity_texture_features.illegal_path_warn").getString()
                                    + " [" + path + "]");
                            if (!path.isBlank())
                                cir.setReturnValue(true);
                        }
                        default -> ETFUtils2.logWarn("this message should not appear #65164");
                    }
                }
            }
        }
    }
}


