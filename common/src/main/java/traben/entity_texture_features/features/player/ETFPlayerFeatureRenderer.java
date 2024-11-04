package traben.entity_texture_features.features.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.config.screens.skin.ETFConfigScreenSkinTool;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFUtils2;

#if MC > MC_21
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public class ETFPlayerFeatureRenderer<T extends PlayerRenderState, M extends PlayerModel> extends RenderLayer<T, M> {
#else
import net.minecraft.world.entity.player.Player;

public class ETFPlayerFeatureRenderer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {
#endif

    protected static final ModelPart villagerNose = getModelData(new CubeDeformation(0)).getRoot().getChild("nose").bake(64, 64);
    protected static final ModelPart textureNose = getModelData(new CubeDeformation(0)).getRoot().getChild("textureNose").bake(8, 8);
    protected static final ModelPart jacket = getModelData(new CubeDeformation(0)).getRoot().getChild("jacket").bake(64, 64);
    protected static final ModelPart fatJacket = getModelData(new CubeDeformation(0)).getRoot().getChild("fatJacket").bake(64, 64);
    static private final ResourceLocation VILLAGER_TEXTURE = ETFUtils2.res("textures/entity/villager/villager.png");
    protected final ETFPlayerSkinHolder skinHolder;


    //public boolean sneaking;
    public ETFPlayerFeatureRenderer(RenderLayerParent<T, M> context) {
        super(context);
        this.skinHolder = context instanceof ETFPlayerSkinHolder holder ? holder : null;
    }

    public static MeshDefinition getModelData(CubeDeformation dilation) {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 12.5F, -2.0F, 8.0F, 12.0F, 4.0F, dilation.extend(0.25F)), PartPose.ZERO);
        modelPartData.addOrReplaceChild("fatJacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 12.5F, -2.0F, 8.0F, 12.0F, 4.0F, dilation.extend(0.25F).extend(0.5F)), PartPose.ZERO);
        modelPartData.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
        modelPartData.addOrReplaceChild("textureNose", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -8.0F, -8.0F, 0.0F, 8.0F, 4.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
        return modelData;
    }

    public static void renderSkullFeatures(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, SkullModelBase skullModel, ETFPlayerTexture playerTexture, float yaw) {
        ETFRenderContext.preventRenderLayerTextureModify();
        ETFRenderContext.startSpecialRenderOverlayPhase();

        if (playerTexture.hasVillagerNose || playerTexture.texturedNoseIdentifier != null) {
            villagerNose.yRot = yaw * 0.017453292F;
            villagerNose.xRot = 0;
            villagerNose.y = 0;
            textureNose.yRot = yaw * 0.017453292F;
            textureNose.xRot = 0;
            textureNose.y = 0;
            renderNose(matrixStack, vertexConsumerProvider, light, playerTexture);
        }
//        ETFPlayerFeatureRenderer.renderEmmisive(matrixStack, vertexConsumerProvider, playerTexture, skullModel);
        ETFPlayerFeatureRenderer.renderEnchanted(matrixStack, vertexConsumerProvider, light, playerTexture, skullModel);

        ETFRenderContext.endSpecialRenderOverlayPhase();
        ETFRenderContext.allowRenderLayerTextureModify();
    }

//    private static void renderEmmisive(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, ETFPlayerTexture playerTexture, Model model) {
//        if (playerTexture.hasEmissives && playerTexture.etfTextureOfFinalBaseSkin != null) {
//            playerTexture.etfTextureOfFinalBaseSkin.renderEmissive(matrixStack, vertexConsumerProvider, model);
//        }
//    }

    private static void renderEnchanted(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, ETFPlayerTexture playerTexture, Model model) {
        if (playerTexture.hasEnchant && playerTexture.baseEnchantIdentifier != null && playerTexture.etfTextureOfFinalBaseSkin != null) {
            VertexConsumer enchantVert = ItemRenderer.getArmorFoilBuffer(vertexConsumerProvider, RenderType.armorCutoutNoCull(
                    switch (playerTexture.etfTextureOfFinalBaseSkin.currentTextureState) {
                        case BLINK, BLINK_PATCHED, APPLY_BLINK -> playerTexture.baseEnchantBlinkIdentifier;
                        case BLINK2, BLINK2_PATCHED, APPLY_BLINK2 -> playerTexture.baseEnchantBlink2Identifier;
                        default -> playerTexture.baseEnchantIdentifier;
                    }), #if MC < MC_21 false, #endif true);
            model.renderToBuffer(matrixStack, enchantVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
        }
    }

    private static void renderNose(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, ETFPlayerTexture playerTexture) {
        if (playerTexture.hasVillagerNose) {
//            villagerNose.copyTransform(model.head);
            if (playerTexture.noseType == ETFConfigScreenSkinTool.NoseType.VILLAGER_TEXTURED || playerTexture.noseType == ETFConfigScreenSkinTool.NoseType.VILLAGER_TEXTURED_REMOVE) {
                VertexConsumer villagerVert = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(playerTexture.etfTextureOfFinalBaseSkin.getTextureIdentifier(null)));
                villagerNose.render(matrixStack, villagerVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
                playerTexture.etfTextureOfFinalBaseSkin.renderEmissive(matrixStack, vertexConsumerProvider, villagerNose);
            } else {
                VertexConsumer villagerVert = vertexConsumerProvider.getBuffer(RenderType.entitySolid(VILLAGER_TEXTURE));
                villagerNose.render(matrixStack, villagerVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            }
        } else if (playerTexture.texturedNoseIdentifier != null) {
//            textureNose.copyTransform(model.head);
            VertexConsumer noseVertex = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(playerTexture.texturedNoseIdentifier));
            textureNose.render(matrixStack, noseVertex, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            if (playerTexture.texturedNoseIdentifierEmissive != null) {
//                textureNose.copyTransform(model.head);
                VertexConsumer noseVertex_e;
                if (ETFManager.getEmissiveMode() == ETFConfig.EmissiveRenderModes.BRIGHT) {
                    noseVertex_e = vertexConsumerProvider.getBuffer(RenderType.beaconBeam(playerTexture.texturedNoseIdentifierEmissive, true));
                } else {
                    noseVertex_e = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(playerTexture.texturedNoseIdentifierEmissive));
                }
                textureNose.render(matrixStack, noseVertex_e, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            }
            if (playerTexture.texturedNoseIdentifierEnchanted != null) {
//                textureNose.copyTransform(model.head);
                VertexConsumer noseVertex_ench = ItemRenderer.getArmorFoilBuffer(vertexConsumerProvider, RenderType.armorCutoutNoCull(playerTexture.texturedNoseIdentifierEnchanted), #if MC < MC_21 false, #endif true);
                textureNose.render(matrixStack, noseVertex_ench, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            }
        }
    }

#if MC > MC_21
    @Override
    public void render(final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final T entityRenderState, final float f, final float g) {
#else
    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
#endif
        if (ETF.config().getConfig().skinFeaturesEnabled && skinHolder != null) {
            ETFRenderContext.preventRenderLayerTextureModify();

            ETFPlayerTexture playerTexture = skinHolder.etf$getETFPlayerTexture();
            if (playerTexture != null && playerTexture.hasFeatures) {

                renderFeatures(matrices, vertexConsumers, light, getParentModel(), playerTexture);
            }

            ETFRenderContext.allowRenderLayerTextureModify();
        }
    }

    public void renderFeatures(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, M model, ETFPlayerTexture playerTexture) {
        if (playerTexture.canUseFeaturesForThisPlayer()) {
            ETFRenderContext.startSpecialRenderOverlayPhase();

            if (playerTexture.hasVillagerNose || playerTexture.texturedNoseIdentifier != null) {
                villagerNose.copyFrom(model.head);
                textureNose.copyFrom(model.head);
                renderNose(matrixStack, vertexConsumerProvider, light, playerTexture);
            }
            renderCoat(matrixStack, vertexConsumerProvider, light, playerTexture, model);

//            ETFPlayerFeatureRenderer.renderEmmisive(matrixStack, vertexConsumerProvider, playerTexture, model);
            //ETFPlayerFeatureRenderer.renderEnchanted(matrixStack, vertexConsumerProvider, light, playerTexture, model);

            ETFRenderContext.endSpecialRenderOverlayPhase();
        }
    }

    private void renderCoat(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, ETFPlayerTexture playerTexture, M model) {
        ItemStack armour = playerTexture.player.etf$getInventory().getArmor(1);
        if (playerTexture.coatIdentifier != null &&
                playerTexture.player.etf$isPartVisible(PlayerModelPart.JACKET) &&
                !(armour.is(Items.CHAINMAIL_LEGGINGS) ||
                        armour.is(Items.LEATHER_LEGGINGS) ||
                        armour.is(Items.DIAMOND_LEGGINGS) ||
                        armour.is(Items.GOLDEN_LEGGINGS) ||
                        armour.is(Items.IRON_LEGGINGS) ||
                        armour.is(Items.NETHERITE_LEGGINGS))
        ) {
            //String coat = ETFPlayerSkinUtils.SKIN_NAMESPACE + id + "_coat.png";

            if (playerTexture.hasFatCoat) {
                fatJacket.copyFrom(model.jacket);
            } else {
                jacket.copyFrom(model.jacket);
            }
            //perform texture features
            VertexConsumer coatVert = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(playerTexture.coatIdentifier));
            matrixStack.pushPose();
            if (playerTexture.hasFatCoat) {
                fatJacket.render(matrixStack, coatVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            } else {
                jacket.render(matrixStack, coatVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            }
            if (playerTexture.coatEnchantedIdentifier != null) {
                VertexConsumer enchantVert = ItemRenderer.getArmorFoilBuffer(vertexConsumerProvider, RenderType.armorCutoutNoCull(playerTexture.coatEnchantedIdentifier), #if MC < MC_21 false, #endif true);
                if (playerTexture.hasFatCoat) {
                    fatJacket.render(matrixStack, enchantVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
                } else {
                    jacket.render(matrixStack, enchantVert, light, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
                }
            }


            if (playerTexture.coatEmissiveIdentifier != null) {
                VertexConsumer emissiveVert;// = vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(emissive, true));
                if (ETFManager.getEmissiveMode() == ETFConfig.EmissiveRenderModes.BRIGHT) {
                    emissiveVert = vertexConsumerProvider.getBuffer(RenderType.beaconBeam(playerTexture.coatEmissiveIdentifier, true));
                } else {
                    emissiveVert = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(playerTexture.coatEmissiveIdentifier));
                }

                if (playerTexture.hasFatCoat) {
                    fatJacket.render(matrixStack, emissiveVert, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
                } else {
                    jacket.render(matrixStack, emissiveVert, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
                }
            }

            matrixStack.popPose();
        }
    }

}
