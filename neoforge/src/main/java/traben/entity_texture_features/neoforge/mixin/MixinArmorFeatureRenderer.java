package traben.entity_texture_features.neoforge.mixin;
#if MC < MC_21_2
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
#if MC >= MC_20_6
import net.minecraft.core.Holder;
#else
import net.minecraft.world.item.ArmorItem;
#endif
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.checkerframework.checker.units.qual.A;

#else
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.config.screens.skin.ETFScreenOldCompat;
import traben.entity_texture_features.features.texture_handlers.ETFArmorHandler;

#if MC > MC_21
@Mixin(EquipmentLayerRenderer.class)
public abstract class MixinArmorFeatureRenderer {
#else
@Mixin(HumanoidArmorLayer.class)
public abstract class MixinArmorFeatureRenderer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
#endif
    @Unique
    private static final ETFArmorHandler etf$armorHandler = new ETFArmorHandler();

    @Inject(method =
            #if MC > MC_21
            "renderLayers(Lnet/minecraft/world/item/equipment/EquipmentModel$LayerType;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            #else
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            #endif
            at = @At(value = "HEAD"), cancellable = true)
    private void etf$cancelIfUi(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof ETFScreenOldCompat) {
            //cancel armour rendering
            ci.cancel();
        }
    }


    #if MC >= MC_21_2

    @Inject(method = "renderLayers(Lnet/minecraft/world/item/equipment/EquipmentModel$LayerType;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "HEAD"))
    private void etf$markNotToChange(CallbackInfo ci) {
        etf$armorHandler.start();
    }

    @Inject(method = "renderLayers(Lnet/minecraft/world/item/equipment/EquipmentModel$LayerType;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "RETURN"))
    private void etf$markAllowedToChange(CallbackInfo ci) {
        etf$armorHandler.end();
    }

    @ModifyArg(method = "renderLayers(Lnet/minecraft/world/item/equipment/EquipmentModel$LayerType;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object etf$setTrim(final Object t) {
        if (t instanceof EquipmentLayerRenderer.TrimSpriteKey trimSpriteKey){
            etf$armorHandler.setTrim(trimSpriteKey.trim.getTexture(trimSpriteKey.layerType, trimSpriteKey.equipmentModelId));
        }
        return t;
    }

    @Inject(method = "renderLayers(Lnet/minecraft/world/item/equipment/EquipmentModel$LayerType;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
                    shift = At.Shift.AFTER))
    private void etf$renderEmissiveTrim(final EquipmentModel.LayerType layerType, final ResourceLocation resourceLocation, final Model model, final ItemStack itemStack, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, final ResourceLocation resourceLocation2, final CallbackInfo ci) {
        etf$armorHandler.renderTrimEmissive(poseStack, multiBufferSource, model);
    }

    #else


    @SuppressWarnings("unused")
    public MixinArmorFeatureRenderer(RenderLayerParent<T, M> context) {
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
    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    #if MC >= MC_21
    @ModifyArg(method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    #elif MC >= MC_20_6

    @ModifyArg(method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    #else
    @ModifyArg(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    #endif
    private ResourceLocation etf$changeTexture(ResourceLocation texture) {
        return etf$armorHandler.getBaseTexture(texture);
    }
#if MC >= MC_21
    @Inject(method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "TAIL"))
    private void etf$applyEmissive(final PoseStack arg, final MultiBufferSource arg2, final int i, final Model model, final int k, final ResourceLocation arg4, final CallbackInfo ci) {

    #elif MC >= MC_20_6
    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "TAIL"))
    private void etf$applyEmissive(final PoseStack arg, final MultiBufferSource arg2, final int i, final Model model, final float f, final float g, final float h, final ResourceLocation arg4, final CallbackInfo ci) {

#else
@Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V",
        at = @At(value = "TAIL"))
    private void etf$applyEmissive(final PoseStack arg, final MultiBufferSource arg2, final int i, final ArmorItem arg3, final M model, final boolean bl, final float f, final float g, final float h, final String string, final CallbackInfo ci) {

#endif
        etf$armorHandler.renderBaseEmissive(arg,arg2,model,#if MC < MC_21 f, g, h #else FastColor.ARGB32.red(k),FastColor.ARGB32.green(k), FastColor.ARGB32.blue(k) #endif);

    }




    #if MC >= MC_20_6
    @Inject(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(value = "HEAD"))
    private void etf$trimGet(final Holder<ArmorMaterial> arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final Model arg5, final boolean bl, final CallbackInfo ci) {

#else
    @Inject(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
        at = @At(value = "HEAD"))
    private void etf$trimGet(final ArmorMaterial arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final M arg5, final boolean bl, final CallbackInfo ci) {
#endif
        etf$armorHandler.setTrim(arg,arg4,bl);
    }

//    #if MC >= MC_21
//    @ModifyArg(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"),
//            index = 1)
//    #elif MC >= MC_20_6
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

    private void etf$trimEmissive(final ArmorMaterial arg, final PoseStack arg2, final MultiBufferSource arg3, final int i, final ArmorTrim arg4, final M arg5, final boolean bl, final CallbackInfo ci) {
#endif
        etf$armorHandler.renderTrimEmissive(arg2,arg3,arg5);
    }
    #endif
}


