package traben.entity_texture_features.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
#if MC < MC_20_6
//todo maybe more versions.....
import net.minecraft.world.item.ArmorItem;
#endif
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.config.screens.skin.ETFScreenOldCompat;
import traben.entity_texture_features.features.texture_handlers.ETFArmorHandler;


@Mixin(HumanoidArmorLayer.class)
public abstract class MixinArmorFeatureRenderer<T extends LivingEntity, A extends HumanoidModel<T>> extends RenderLayer<T, A> {
    @Unique
    private final ETFArmorHandler etf$armorHandler = new ETFArmorHandler();

    @SuppressWarnings("unused")
    public MixinArmorFeatureRenderer(RenderLayerParent<T, A> context) {
        super(context);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "HEAD"))
    private void etf$markNotToChange(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        etf$armorHandler.start();
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "RETURN"))
    private void etf$markAllowedToChange(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        etf$armorHandler.end();
    }



    #if MC >= MC_21
    @ModifyArg(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))

    #elif MC >= MC_20_6

    @ModifyArg(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    #else
    @ModifyArg(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    #endif
    private ResourceLocation etf$changeTexture(ResourceLocation texture) {
        return etf$armorHandler.getBaseTexture(texture);
    }
    #if MC >= MC_21
    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "TAIL"),remap = false)
    private void etf$applyEmissive(final PoseStack arg, final MultiBufferSource arg2, final int i, final Model model, final int k, final ResourceLocation arg4, final CallbackInfo ci) {

#elif MC >= MC_20_6
    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "TAIL"))
    private void etf$applyEmissive(final PoseStack arg, final MultiBufferSource arg2, final int i, final Model model, final float f, final float g, final float h, final ResourceLocation arg4, final CallbackInfo ci) {

#else
@Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V",
        at = @At(value = "TAIL"))
    private void etf$applyEmissive(final PoseStack arg, final MultiBufferSource arg2, final int i, final ArmorItem arg3, final A model, final boolean bl, final float f, final float g, final float h, final String string, final CallbackInfo ci) {

#endif
        etf$armorHandler.renderBaseEmissive(arg,arg2,model, #if MC < MC_21 f, g, h #else FastColor.ARGB32.red(k),FastColor.ARGB32.green(k), FastColor.ARGB32.blue(k) #endif);

    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "HEAD"), cancellable = true)
    private void etf$cancelIfUi(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof ETFScreenOldCompat) {
            //cancel armour rendering
            ci.cancel();
        }
    }


#if MC >= MC_20_6
    @Inject(method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(value = "HEAD"))
    private void etf$trimGet(final Holder<ArmorMaterial> arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final Model arg5, final boolean bl, final CallbackInfo ci) {

#else
    @Inject(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
        at = @At(value = "HEAD"))
    private void etf$trimGet(final ArmorMaterial arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final A arg5, final boolean bl, final CallbackInfo ci) {
#endif
        etf$armorHandler.setTrim(arg,arg4,bl);
    }

//#if MC >= MC_21
//   @ModifyArg(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
//           at = @At(value = "INVOKE",
//                   target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"),
//           index = 1)
//#elif MC >= MC_20_6
//    @ModifyArg(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
//            index = 1)
//#else
//    @ModifyArg(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
//        at = @At(value = "INVOKE",
//                target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
//        index = 1)
//#endif
//    private VertexConsumer etf$changeTrim(VertexConsumer par2) {
//        return etf$armorHandler.modifyTrim(par2);
//    }

#if MC >= MC_20_6
    @Inject(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(value = "TAIL"))
    private void etf$trimEmissive(final Holder<ArmorMaterial> arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final Model arg5, final boolean bl, final CallbackInfo ci) {
#else
@Inject(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
        at = @At(value = "TAIL"))

    private void etf$trimEmissive(final ArmorMaterial arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final A arg5, final boolean bl, final CallbackInfo ci) {
#endif
        etf$armorHandler.renderTrimEmissive(arg2,arg3,arg5);
    }
}


