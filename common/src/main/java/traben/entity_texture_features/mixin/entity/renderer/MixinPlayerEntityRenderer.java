package traben.entity_texture_features.mixin.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.compat.ETF3DSkinLayersUtil;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.player.ETFPlayerFeatureRenderer;
import traben.entity_texture_features.features.player.ETFPlayerSkinHolder;
import traben.entity_texture_features.features.player.ETFPlayerTexture;
import traben.entity_texture_features.utils.ETFUtils2;


#if MC > MC_21
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> implements ETFPlayerSkinHolder {
#else
@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> implements ETFPlayerSkinHolder {
#endif


    @Shadow
    protected abstract void renderNameTag(final PlayerRenderState playerRenderState, final Component component, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i);

    @Unique
    private ETFPlayerTexture etf$ETFPlayerTexture = null;

    @SuppressWarnings("unused")
    public MixinPlayerEntityRenderer(EntityRendererProvider.Context ctx, #if MC > MC_21 PlayerModel #else PlayerModel<AbstractClientPlayer> #endif model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>",
            at = @At(value = "TAIL"))
    private void etf$addFeatures(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        PlayerRenderer self = (PlayerRenderer) ((Object) this);
        this.addLayer(new ETFPlayerFeatureRenderer<>(self));
    }


    /*
     * For some reason cancelling in this way is the only way to get this working
     * */
    #if MC > MC_21
    @Inject(method = "renderHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
                    shift = At.Shift.BEFORE), cancellable = true)
    private void etf$redirectNicely(final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final ResourceLocation resourceLocation, final ModelPart armAndSleeve, final boolean bl, final CallbackInfo ci) {
        //todo redo all this, 1.21.2 really changed things
        if (Minecraft.getInstance().player == null) return;
        var player = Minecraft.getInstance().player;

    #else
        @Inject(method = "renderHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/PlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
                    shift = At.Shift.AFTER), cancellable = true)
    private void etf$redirectNicely(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {

    #endif
        if (ETF.config().getConfig().skinFeaturesEnabled) {
            ETFPlayerTexture thisETFPlayerTexture = ETFManager.getInstance().getPlayerTexture(player,
                    #if MC > MC_20_1
                        player.getSkin().texture()
                    #else
                        player.getSkinTextureLocation()
                    #endif
            );
            if (thisETFPlayerTexture != null && thisETFPlayerTexture.hasFeatures) {
                ResourceLocation etfTexture = thisETFPlayerTexture.getBaseTextureIdentifierOrNullForVanilla(player);
                if (etfTexture != null) {
                    ETFRenderContext.preventRenderLayerTextureModify();
                    #if MC <= MC_21
                    arm.xRot = 0.0F;
                    sleeve.xRot = 0.0F;
                    #endif

                    VertexConsumer vc1 = vertexConsumers.getBuffer(RenderType.entityTranslucent(etfTexture));
                    etf$renderOnce(matrices, vc1, light, player, #if MC > MC_21 armAndSleeve #else arm, sleeve #endif);

                    ETFRenderContext.startSpecialRenderOverlayPhase();
                    ResourceLocation emissive = thisETFPlayerTexture.getBaseTextureEmissiveIdentifierOrNullForNone();
                    if (emissive != null) {
                        VertexConsumer vc2 = vertexConsumers.getBuffer(RenderType.entityTranslucent(emissive));
                        etf$renderOnce(matrices, vc2, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, player, #if MC > MC_21 armAndSleeve #else arm, sleeve #endif);
                    }
                    if (thisETFPlayerTexture.baseEnchantIdentifier != null) {
                        VertexConsumer vc3 = ItemRenderer.getArmorFoilBuffer(vertexConsumers,
                                RenderType.armorCutoutNoCull(thisETFPlayerTexture.baseEnchantIdentifier), #if MC < MC_21 false, #endif true);
                        etf$renderOnce(matrices, vc3, light, player, #if MC > MC_21 armAndSleeve #else arm, sleeve #endif);
                    }
                    ETFRenderContext.endSpecialRenderOverlayPhase();

                    ETFRenderContext.allowRenderLayerTextureModify();
                    //don't further render vanilla arms
                    ci.cancel();
                }
            }
        }

    }


    @Unique
    private void etf$renderOnce(PoseStack matrixStack, VertexConsumer consumer, int light, AbstractClientPlayer player,#if MC > MC_21 ModelPart armAndSleeve #else ModelPart arm, ModelPart sleeve #endif) {
        #if MC > MC_21
        armAndSleeve.render(matrixStack, consumer, light, OverlayTexture.NO_OVERLAY);
        #else
        arm.render(matrixStack, consumer, light, OverlayTexture.NO_OVERLAY);
        sleeve.render(matrixStack, consumer, light, OverlayTexture.NO_OVERLAY);
        #endif

        if (ETF.SKIN_LAYERS_DETECTED && ETF.config().getConfig().use3DSkinLayerPatch) {
            try {
                // handler class is only ever accessed if the mod is present
                // prevents NoClassDefFoundError
                //noinspection DataFlowIssue
                ETF3DSkinLayersUtil.renderHand((PlayerRenderer) ((Object) this), matrixStack, consumer, light, player, #if MC > MC_21 armAndSleeve #else arm, sleeve #endif);
            } catch (Exception e) {
                //ETFUtils2.logWarn("Exception with ETF's 3D skin layers mod compatibility: " + e);
            } catch (NoClassDefFoundError error) {
                // Should never be thrown
                // unless a significant change if skin layers mod
                ETFUtils2.logError("Error with ETF's 3D skin layers mod hand compatibility: " + error);
                error.printStackTrace();
                //prevent further attempts
                ETF.SKIN_LAYERS_DETECTED = false;
            }
        }
    }

    #if MC > MC_21
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At(value = "RETURN"), cancellable = true)
    private void etf$getTexture(final PlayerRenderState playerRenderState, final CallbackInfoReturnable<ResourceLocation> cir) {
        if(!(ETFRenderContext.getCurrentEntity() instanceof AbstractClientPlayer abstractClientPlayerEntity)) return;

    #else
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At(value = "RETURN"), cancellable = true)
    private void etf$getTexture(AbstractClientPlayer abstractClientPlayerEntity, CallbackInfoReturnable<ResourceLocation> cir) {
    #endif
        if (ETF.config().getConfig().skinFeaturesEnabled) {
            etf$ETFPlayerTexture = ETFManager.getInstance().getPlayerTexture(abstractClientPlayerEntity, cir.getReturnValue());
            if (etf$ETFPlayerTexture != null && etf$ETFPlayerTexture.hasFeatures) {
                ResourceLocation texture = etf$ETFPlayerTexture.getBaseTextureIdentifierOrNullForVanilla(abstractClientPlayerEntity);
                if (texture != null) {
//                    System.out.println(etf$ETFPlayerTexture.etfTextureOfFinalBaseSkin);
                    cir.setReturnValue(texture);
                }
            }
        } else {
            etf$ETFPlayerTexture = null;
        }
    }

    @Override
    public @Nullable ETFPlayerTexture etf$getETFPlayerTexture() {
        return etf$ETFPlayerTexture;
    }
}