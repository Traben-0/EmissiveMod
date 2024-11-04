package traben.entity_texture_features.config.screens.skin;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static traben.entity_texture_features.ETF.MOD_ID;

//inspired by puzzles custom gui code
public class ETFConfigScreenSkinToolPixelSelection extends ETFScreenOldCompat {

    private final SelectionMode MODE;

    private final ETFConfigScreenSkinTool etfParent;
    Set<Integer> selectedPixels;
    ResourceLocation currentSkinToRender = ETFUtils2.res(MOD_ID + ":textures/gui/icon.png");

    protected ETFConfigScreenSkinToolPixelSelection(ETFConfigScreenSkinTool parent, SelectionMode mode) {
        super("config." + ETF.MOD_ID + (mode == SelectionMode.EMISSIVE ? ".emissive_select" : ".enchanted_select") + ".title", parent, false);
        this.MODE = mode;
        etfParent = parent;


    }

    @Override
    protected void init() {
        super.init();

        ResourceLocation randomID = ETFUtils2.res(MOD_ID + "_ignore", "gui_skin_" + System.currentTimeMillis() + ".png");
        if (ETFUtils2.registerNativeImageToIdentifier(etfParent.currentEditorSkin, randomID)) {
            currentSkinToRender = randomID;
        }

        selectedPixels = new HashSet<>();
        for (int x = MODE.startX; x < MODE.startX + 8; x++) {
            for (int y = MODE.startY; y < MODE.startY + 8; y++) {
                int color = ETFUtils2.getPixel(etfParent.currentEditorSkin, x, y);
                if (color != 0) {
                    selectedPixels.add(color);
                }
            }
        }

        this.addRenderableWidget(getETFButton((int) (this.width * 0.024), (int) (this.height * 0.2), 20, 20,
                Component.nullToEmpty("⟳"),
                (button) -> etfParent.flipView = !etfParent.flipView));


        this.addRenderableWidget(getETFButton((int) (this.width * 0.55), (int) (this.height * 0.9), (int) (this.width * 0.2), 20,
                CommonComponents.GUI_BACK,
                (button) -> Objects.requireNonNull(minecraft).setScreen(parent)));

        int pixelSize = (int) (this.height * 0.7 / 64);

        //simple method to create 4096 buttons instead of extrapolating mouse position
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                Button butt = getButtonPixels(x, y, pixelSize);
//todo can do so much better than this
                this.addRenderableWidget(butt);
            }
        }

    }

    @NotNull
    private Button getButtonPixels(final int x, final int y, final int pixelSize) {

        return new Button((int) ((ETFConfigScreenSkinToolPixelSelection.this.width * 0.35) + (x * pixelSize)), (int) ((ETFConfigScreenSkinToolPixelSelection.this.height * 0.2) + (y * pixelSize)), pixelSize, pixelSize,
                Component.nullToEmpty(""),
                (button) -> {
                    int colorAtPixel = ETFUtils2.getPixel(etfParent.currentEditorSkin, x, y);
                    if (selectedPixels.contains(colorAtPixel)) {
                        selectedPixels.remove(colorAtPixel);
                    } else {
                        selectedPixels.add(colorAtPixel);
                    }

                    applyCurrentSelectedPixels();
                    etfParent.thisETFPlayerTexture.changeSkinToThisForTool(etfParent.currentEditorSkin);
                    ResourceLocation randomID2 = ETFUtils2.res(MOD_ID + "_ignore", "gui_skin_" + System.currentTimeMillis() + ".png");
                    if (ETFUtils2.registerNativeImageToIdentifier(etfParent.currentEditorSkin, randomID2)) {
                        currentSkinToRender = randomID2;
                    }
                }, Supplier::get) {

            @Override
            protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                //invisible lol
//                        super.renderWidget(context, mouseX, mouseY, delta);
            }

        };
    }

    private void applyCurrentSelectedPixels() {
        ArrayList<Integer> integerSet = new ArrayList<>(selectedPixels);

        for (int x = MODE.startX; x < MODE.startX + 8; x++) {
            for (int y = MODE.startY; y < MODE.startY + 8; y++) {
                if (integerSet.isEmpty()) {
                    ETFUtils2.setPixel(etfParent.currentEditorSkin, x, y, 0);
                } else {
                    ETFUtils2.setPixel(etfParent.currentEditorSkin, x, y, integerSet.get(0));
                    integerSet.remove(0);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int pixelSize = (int) (this.height * 0.7 / 64);

        renderGUITexture(context,currentSkinToRender, (int) ((this.width * 0.35)), (int) ((this.height * 0.2)), (int) ((this.width * 0.35) + (64 * pixelSize)), (int) ((this.height * 0.2) + (64 * pixelSize)));
        context.drawString(font, ETF.getTextFromTranslation("config." + MOD_ID + ".skin_select" + (selectedPixels.size() > 64 ? ".warn" : ".hint")), width / 7, (int) (this.height * 0.8), selectedPixels.size() > 64 ? 0xff1515 : 0xFFFFFF);


        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {

            int height = (int) (this.height * 0.75);
            int playerX = (int) (this.width * 0.14);
            drawEntity(context, playerX, height, (int) (this.height * 0.3), (float) (-mouseX + playerX), (float) (-mouseY + (this.height * 0.3)), player);
        } else {
            context.drawString(font, Component.nullToEmpty("Player model only visible while in game!"), width / 7, (int) (this.height * 0.4), 0xFFFFFF);
            context.drawString(font, Component.nullToEmpty("load a single-player world and then open this menu."), width / 7, (int) (this.height * 0.45), 0xFFFFFF);
        }


//        if(MODE == SelectionMode.EMISSIVE && ETFVersionDifferenceHandler.isThisModLoaded("iris"))
//            drawTextWithShadow(matrices, textRenderer, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.iris_message"), width / 8, (int) (this.height * 0.15), 0xFF5555);

    }

    public void drawEntity(GuiGraphics context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float) Math.atan((mouseX / 40.0f));

        float g = (float) Math.atan((mouseY / 40.0F));
//1.20.5        MatrixStack matrixStack = RenderSystem.getModelViewStack();

//        float j2 = (float) Math.atan(((-mouseY + this.height / 2f) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(0);//j2 * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        context.pose().pushPose();
        context.pose().translate(x, y, 150.0);
        #if MC >= MC_20_6
        context.pose().mulPose((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
        #else
        context.pose().mulPoseMatrix((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
        #endif
        context.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadow(false);
        float h = entity.yBodyRot;
        float i = entity.getYRot();
        float j = entity.getXRot();
        float k = entity.yHeadRotO;
        float l = entity.yHeadRot;
        entity.yBodyRot = (etfParent.flipView ? 0 : 180.0F) + f * 20.0F;
        entity.setYRot((etfParent.flipView ? 0 : 180.0F) + f * 40.0F);
        entity.setXRot(-g * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        #if MC > MC_21
        context.drawSpecial((bufferSource) -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, context.pose(), bufferSource, LightTexture.pack(7, 7)));
        #else
        //noinspection deprecation
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.pose(), context.bufferSource(), LightTexture.pack(7, 7)));
        #endif

//        immediate.draw();
        context.flush();
        entityRenderDispatcher.setRenderShadow(true);
        context.pose().popPose();
        Lighting.setupFor3DItems();
//        entityRenderDispatcher.setRenderShadows(true);
        entity.yBodyRot = h;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
//        matrixStack.pop();
//        RenderSystem.applyModelViewMatrix();
//        DiffuseLighting.enableGuiDepthLighting();
    }

    public enum SelectionMode {
        EMISSIVE(56, 16),
        ENCHANTED(56, 24);

        final int startX;
        final int startY;

        SelectionMode(@SuppressWarnings("SameParameterValue") int start_x, int start_y) {
            startX = start_x;
            startY = start_y;
        }
    }

}
