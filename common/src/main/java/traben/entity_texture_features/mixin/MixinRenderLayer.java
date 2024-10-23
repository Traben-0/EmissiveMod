package traben.entity_texture_features.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import traben.entity_texture_features.utils.ETFUtils2;


@Mixin(RenderType.class)
public abstract class MixinRenderLayer {


    @ModifyVariable(
            method = {
                    "entitySolid",
                    "eyes",
                    "energySwirl",
                    "entitySmoothCutout",
                    "itemEntityTranslucentCull",
                    "entityCutout",
                    "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    "entityCutoutNoCullZOffset(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    "entityDecal",
                    "entityNoOutline",
                    "entitySmoothCutout",
                    "entityTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    #if MC < MC_21_2 "entityTranslucentCull",#endif
                    "entityTranslucentEmissive(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
                    "armorCutoutNoCull",
                    "entityShadow"
            },
            at = @At(value = "HEAD"),
            index = 0, argsOnly = true)
    private static ResourceLocation etf$mixinAllEntityLayers(ResourceLocation value) {
        return ETFUtils2.getETFVariantNotNullForInjector(value);
    }

}
