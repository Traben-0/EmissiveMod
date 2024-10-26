package traben.entity_texture_features.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.api.Mesh;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.PlayerModelPart;

// compatibility class for the wonderful 3D skin layers mod by @tr9zw to utilise ETF skin features
public abstract class ETF3DSkinLayersUtil {


    // todo ensure rendering method is kept up to date with how 3D skin layers mod does it
    // copy of 3D skin layers hand rendering code as ETF needs to be able to render it at will with a custom VertexConsumer
    public static void renderHand(PlayerRenderer instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, AbstractClientPlayer abstractClientPlayer, #if MC > MC_21 ModelPart armAndSleeve #else ModelPart arm, ModelPart sleeve #endif)
            throws NoClassDefFoundError {


        #if MC > MC_21
        return;//todo 3d skin layers for 1.21.2
        //safe calls to get arm sleeve part
//        var sleeve = armAndSleeve.hasChild("right_sleeve")
//                ? armAndSleeve.getChild("right_sleeve")
//                : armAndSleeve.hasChild("left_sleeve")
//                    ? armAndSleeve.getChild("left_sleeve")
//                    : null;
//        if (sleeve == null) return;

        #else

        boolean rightSleeve;
        label59:
        {
            rightSleeve = (instance.getModel()).leftSleeve != sleeve;
            if (rightSleeve) {
                if (SkinLayersModBase.config.enableRightSleeve) {
                    break label59;
                }
            } else if (SkinLayersModBase.config.enableLeftSleeve) {
                break label59;
            }

            return;
        }

        sleeve.visible = false;
        if (abstractClientPlayer.isModelPartShown(rightSleeve ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE)) {
            PlayerSettings settings = (PlayerSettings) abstractClientPlayer;
            float armHeightScaling = 1.1F;
            boolean thinArms = ((PlayerEntityModelAccessor) instance.getModel()).hasThinArms();
            if (SkinUtil.setup3dLayers(abstractClientPlayer, settings, thinArms, instance.getModel())) {
                Mesh part = sleeve == (instance.getModel()).leftSleeve ? settings.getLeftArmMesh() : settings.getRightArmMesh();

                #if MC <= MC_21
                part.copyFrom(arm);
                #endif

                poseStack.pushPose();
                poseStack.scale(SkinLayersModBase.config.firstPersonPixelScaling, armHeightScaling, SkinLayersModBase.config.firstPersonPixelScaling);
                boolean left = sleeve == (instance.getModel()).leftSleeve;
                float x = left ? 5.0F : -5.0F;
                float y = 1.4F;
                double scaleOffset = ((double) SkinLayersModBase.config.firstPersonPixelScaling - 1.1) * 5.0;
                if (left) {
                    x = (float) ((double) x - scaleOffset);
                } else {
                    x = (float) ((double) x + scaleOffset);
                }

                if (!thinArms) {
                    if (left) {
                        x = (float) ((double) x + 0.45);
                    } else {
                        x = (float) ((double) x - 0.45);
                    }
                }

                part.setPosition(x, y, 0.0F);
                part.render(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
                part.setPosition(0.0F, 0.0F, 0.0F);
                part.setRotation(0.0F, 0.0F, 0.0F);
                poseStack.popPose();
            }
        }
        #endif
    }
}
