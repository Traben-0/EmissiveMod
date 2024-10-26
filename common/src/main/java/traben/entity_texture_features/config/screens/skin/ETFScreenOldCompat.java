package traben.entity_texture_features.config.screens.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.mixin.accessor.TooltipAccessor;
import traben.tconfig.gui.TConfigScreen;

import java.util.ArrayList;
import java.util.List;

public abstract class ETFScreenOldCompat extends TConfigScreen {

    @SuppressWarnings("SameParameterValue")
    protected ETFScreenOldCompat(final String title, final Screen parent, @SuppressWarnings("SameParameterValue") final boolean showBackButton) {
        super(title, parent, showBackButton);
    }

    public static void renderGUITexture(GuiGraphics context, ResourceLocation texture, double x1, double y1, double x2, double y2) {
        #if MC < MC_21
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(x1, y2, 0.0).uv(0, 1/*(float)x1, (float)y2*heightYValue*/ ).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(x2, y2, 0.0).uv(1, 1/*(float)x2*widthXValue, (float)y2*heightYValue*/ ).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(x2, y1, 0.0).uv(1, 0/*(float)x2*widthXValue, (float)y1*/ ).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(x1, y1, 0.0).uv(0, 0/*(float)x1, (float)y1*/ ).color(255, 255, 255, 255).endVertex();
        tessellator.end();
        #elif MC > MC_21
        context.blit(RenderType::guiTextured, texture, (int) x1, (int) y1, 0, 0, (int) (x2-x1), (int) (y2-y1), 1, 1, 1, 1,
                ARGB.color( 255, 255, 255, 255));
        #else //mc_21

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(  GameRenderer::   GameRenderer::getPositionTexColorShader );
        RenderSystem.enableBlend();

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        bufferBuilder.addVertex( (float)x1, (float)y2, (float)0).setUv(0, 1).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex( (float)x2, (float)y2, (float)0).setUv(1, 1).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex( (float)x2, (float)y1, (float)0).setUv(1, 0).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex( (float)x1, (float)y1, (float)0).setUv(0, 0).setColor(255, 255, 255, 255);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
        #endif
    }

    public static String booleanAsOnOff(boolean bool) {
        return CommonComponents.optionStatus(bool).getString();
    }

    public Button getETFButton(int x, int y, int width, @SuppressWarnings("SameParameterValue") int height, Component buttonText, Button.OnPress onPress) {
        return getETFButton(x, y, width, height, buttonText, onPress, Component.nullToEmpty(""));
    }

    public Button getETFButton(int x, int y, int width, int height, Component buttonText, Button.OnPress onPress, Component toolTipText) {
        int nudgeLeftEdge;
        if (width > 384) {
            nudgeLeftEdge = (width - 384) / 2;
            width = 384;
        } else {
            nudgeLeftEdge = 0;
        }
//        if (width > 800)
//            height=80;
//        if (width > 1600)
//            height=16;
        boolean tooltipIsEmpty = toolTipText.getString().isBlank();
//        String[] strings = toolTipText.getString().split("\n");
//        List<Text> lines = new ArrayList<>();
//        for (String str :
//                strings) {
//            lines.add(Text.of(str.strip()));
//        }

        if (tooltipIsEmpty) {
            //button with no tooltip
            return Button.builder(buttonText, onPress).bounds(x + nudgeLeftEdge, y, width, height).build();
        } else {
            //return ButtonWidget.builder(buttonText,onPress).dimensions(x+nudgeLeftEdge, y, width, height).tooltip(Tooltip.of(toolTipText)).build();
            //1.19.3 required only
            ///////////////////////////////////////

            Tooltip bob = Tooltip.create(toolTipText);
            if (!ETF.isThisModLoaded("adaptive-tooltips")) {
                //split tooltip by our rules
                String[] strings = toolTipText.getString().split("\n");
                List<FormattedCharSequence> texts = new ArrayList<>();
                for (String str :
                        strings) {
                    texts.add(Component.nullToEmpty(str).getVisualOrderText());
                }

                //apply to tooltip object

                ((TooltipAccessor) bob).setCachedTooltip(texts);
            }
            ////////////////////////////////////////
            //create button
            return Button.builder(buttonText, onPress).bounds(x + nudgeLeftEdge, y, width, height).tooltip(bob).build();


        }
    }
}
