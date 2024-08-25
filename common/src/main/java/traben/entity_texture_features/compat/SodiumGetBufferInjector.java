package traben.entity_texture_features.compat;

#if MC < MC_21 && MC != MC_20_2
#define ALLOW
#endif


#if ALLOW
import me.jellysquid.mods.sodium.client.render.vertex.buffer.ExtendedBufferBuilder;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.ETFVertexConsumer;

import java.lang.reflect.Method;
import java.util.Objects;
#endif
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import traben.entity_texture_features.utils.ETFUtils2;


/**
 * A separate class to handle a specific sodium injection case for sodium 0.5.6+
 * This is called in one place and handles usage of sodium classes in a quarantined way.
 */
public abstract class SodiumGetBufferInjector {

    private static final TriConsumer<MultiBufferSource, RenderType, VertexConsumer> INSTANCE = get();


    public static void inject(MultiBufferSource provider, RenderType renderLayer, VertexConsumer vertexConsumer) {
        if (INSTANCE != null) INSTANCE.accept(provider, renderLayer, vertexConsumer);
    }

    private static TriConsumer<MultiBufferSource, RenderType, VertexConsumer> get() {
        #if ALLOW
        try {
            return new Impl();
            //sodium 0.5.9+ will cause the NoSuchMethodError as the interface ExtendedBufferBuilder has changed to no longer need that
            // ETF for now just still uses 0.5.8 to retain back compatibility until enough time has passed to drop this entirely
        } catch (NoClassDefFoundError | NoSuchMethodError | NullPointerException ignored) {
            ETFUtils2.logWarn("ETF compatibility method for sodium versions prior to 0.5.9 failed, this is fine if you are using a newer sodium or if it isn't installed at all");
        }
        //any of these errors means we are using an different version of sodium
        #endif
        return null;
    }
#if ALLOW
    private static class Impl implements TriConsumer<MultiBufferSource, RenderType, VertexConsumer> {
        Impl() {
            Objects.requireNonNull(ExtendedBufferBuilder.class);
            var methods = ExtendedBufferBuilder.class.getMethods();
            boolean found = false;
            for (Method method : methods) {
                if (method.getName().equals("sodium$getDelegate")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NoSuchMethodError("Method sodium$getDelegate not found");
            }
        }

        @Override
        public void accept(final MultiBufferSource vertexConsumerProvider, final RenderType renderLayer, final VertexConsumer vertexConsumer) {
            if (vertexConsumer instanceof ExtendedBufferBuilder buff) {
                var delegate = buff.sodium$getDelegate();
                if (delegate instanceof ETFVertexConsumer) {
                    ETFRenderContext.insertETFDataIntoVertexConsumer(vertexConsumerProvider, renderLayer, delegate);
                }
            }
        }
    }
    #endif
}
