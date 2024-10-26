package traben.entity_texture_features.mixin.entity.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.utils.ETFUtils2;






@Mixin(EnderDragonRenderer.class)
public abstract class MixinEnderDragonEntityRenderer extends #if MC > MC_21 EntityRenderer<EnderDragon, EnderDragonRenderState> #else EntityRenderer<EnderDragon> #endif {

    @Final
    @Shadow
    private static ResourceLocation DRAGON_LOCATION;          // = new Identifier("textures/entity/enderdragon/dragon.png");
    @Final
    @Shadow
    private static ResourceLocation DRAGON_EYES_LOCATION;      // = new Identifier("textures/entity/enderdragon/dragon_eyes.png");
    @Final
    @Shadow
    private static RenderType RENDER_TYPE;   //= RenderLayer.getEntityCutoutNoCull(TEXTURE);
    @Final
    @Shadow
    private static RenderType DECAL;    //= RenderLayer.getEntityDecal(TEXTURE);
    @Final
    @Shadow
    private static RenderType EYES;     //= RenderLayer.getEyes(EYE_TEXTURE);

    @SuppressWarnings("unused")
    protected MixinEnderDragonEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @ModifyArg(
            method =
        #if MC > MC_21
                    "render(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        #else
                    "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        #endif
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType etf$returnAlteredTexture(RenderType texturedRenderLayer) {
        if (ETF.config().getConfig().canDoCustomTextures()) {
            try {
                if (DECAL.equals(texturedRenderLayer)) {
                    return RenderType.entityDecal(DRAGON_LOCATION);
                } else if (RENDER_TYPE.equals(texturedRenderLayer)) {
                    return RenderType.entityCutoutNoCull(DRAGON_LOCATION);
                } else if (EYES.equals(texturedRenderLayer)) {
                    return RenderType.eyes(DRAGON_EYES_LOCATION);
                }
            } catch (Exception e) {
                ETFUtils2.logError(e.toString(), false);
            }
        }
        return texturedRenderLayer;
    }

}


