package traben.entity_texture_features.mixin.mods.sodium;
#if MC >= MC_21 || MC == MC_20_2
import traben.entity_texture_features.ETFException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(ETFException.class)
public class MixinSodiumBufferBuilder {}
#else
import me.jellysquid.mods.sodium.client.render.vertex.buffer.SodiumBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFRenderLayerWithTexture;
import traben.entity_texture_features.utils.ETFVertexConsumer;

import java.util.Optional;

//class no longer exists post sodium 0.5.9 which is always true past 1.21
@Pseudo
@Mixin(SodiumBufferBuilder.class)
public class MixinSodiumBufferBuilder implements ETFVertexConsumer {

    @Unique
    MultiBufferSource etf$provider = null;
    @Unique
    RenderType etf$renderLayer = null;
    @Unique
    ETFTexture etf$ETFTexture = null;

    @Override
    public ETFTexture etf$getETFTexture() {
        return etf$ETFTexture;
    }

    @Override
    public MultiBufferSource etf$getProvider() {
        return etf$provider;
    }

    @Override
    public RenderType etf$getRenderLayer() {
        return etf$renderLayer;
    }

    @Override
    public void etf$initETFVertexConsumer(MultiBufferSource provider, RenderType renderLayer) {
        etf$provider = provider;

        etf$renderLayer = renderLayer;

        //todo sprites give atlas texture here
        if (renderLayer instanceof ETFRenderLayerWithTexture etfRenderLayerWithTexture) {
            Optional<ResourceLocation> possibleId = etfRenderLayerWithTexture.etf$getId();
            possibleId.ifPresent(identifier -> etf$ETFTexture = ETFManager.getInstance().getETFTextureNoVariation(identifier));
        }
//        else {
//            etf$ETFTexture = null;
//        }
    }
}
#endif
