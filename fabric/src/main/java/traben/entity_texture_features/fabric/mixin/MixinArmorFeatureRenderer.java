package traben.entity_texture_features.fabric.mixin;




#if MC < MC_21_2
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.ArmorItem;

import org.spongepowered.asm.mixin.injection.ModifyArg;
import traben.entity_texture_features.features.texture_handlers.ETFArmorHandler;
import org.spongepowered.asm.mixin.Unique;

#if MC >= MC_20_6
import net.minecraft.core.Holder;
#else
import net.minecraft.world.item.ArmorItem;
#endif

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
public abstract class MixinArmorFeatureRenderer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
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

    @ModifyArg(method = "renderModel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"),index = 0)
    private ResourceLocation etf$changeTexture2(ResourceLocation texture) {
        return etf$armorHandler.getBaseTexture(texture);
    }


    @Inject(method = "renderModel",
            at = @At(value = "TAIL"))

    #if MC >= MC_21
    private void etf$applyEmissive(final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final A model, final int k, final ResourceLocation resourceLocation, final CallbackInfo ci) {
    #elif MC >= MC_20_6
    private void etf$applyEmissive(final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final A model, final float red, final float green, final float blue, final ResourceLocation overlay, final CallbackInfo ci) {
    #else
    private void etf$applyEmissive(final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final ArmorItem armorItem, final A model, final boolean bl, final float red, final float green, final float blue, final String string, final CallbackInfo ci) {
    #endif

        etf$armorHandler.renderBaseEmissive(matrices,vertexConsumers,model,#if MC >= MC_21 FastColor.ARGB32.red(k),FastColor.ARGB32.green(k), FastColor.ARGB32.blue(k) #else red,green,blue #endif);
    }






    #if MC >= MC_20_6
    @Inject(method = "renderTrim",
            at = @At(value = "HEAD"))
    private void etf$trimGet(final Holder<ArmorMaterial> armorMaterial, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final ArmorTrim trim, final A model, final boolean leggings, final CallbackInfo ci) {
        etf$armorHandler.setTrim(armorMaterial,trim,leggings);
    }
    #else
    @Inject(method = "renderTrim",
            at = @At(value = "HEAD"))
    private void etf$trimGet(final ArmorMaterial armorMaterial, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int i, final ArmorTrim trim, final A model, final boolean leggings, final CallbackInfo ci) {
        etf$armorHandler.setTrim(armorMaterial,trim,leggings);
    }
    #endif





    #if MC >= MC_20_6
    @Inject(method = "renderTrim",
            at = @At(value = "TAIL"))
    private void etf$trimEmissive(final Holder<ArmorMaterial> armorMaterial, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final ArmorTrim trim, final A model, final boolean leggings, final CallbackInfo ci) {
        etf$armorHandler.renderTrimEmissive(matrices,vertexConsumers,model);
    }
    #else
        @Inject(method = "renderTrim",
            at = @At(value = "TAIL"))
    private void etf$trimEmissive(final ArmorMaterial armorMaterial, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int i, final ArmorTrim trim, final A model, final boolean bl, final CallbackInfo ci) {
        etf$armorHandler.renderTrimEmissive(matrices,vertexConsumers,model);
    }
    #endif

    #endif
}

