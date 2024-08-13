package traben.tconfig.gui;

import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
#if MC < MC_20_6
import net.minecraft.client.renderer.RenderType;
#endif
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import traben.entity_texture_features.ETF;

public class TConfigScreen extends Screen {
    private final boolean showBackButton;
    protected Screen parent;
    protected Runnable resetDefaultValuesRunnable = null;
    protected Runnable undoChangesRunnable = null;

    protected TConfigScreen(@Translatable final String title, Screen parent, boolean showBackButton) {
        super(Component.translatable(title));
        this.parent = parent;
        this.showBackButton = showBackButton;
    }

    protected Component getBackButtonText() {
        return CommonComponents.GUI_BACK;
    }


    @Override
    protected void init() {
        if (showBackButton) this.addRenderableWidget(Button.builder(
                        getBackButtonText(),
                        (button) -> onClose())
                .bounds((int) (this.width * 0.7), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());

        if (resetDefaultValuesRunnable != null) {
            this.addRenderableWidget(Button.builder(
                    ETF.getTextFromTranslation("dataPack.validation.reset"),
                    (button) -> {
                        resetDefaultValuesRunnable.run();
                        rebuildWidgets();
                    }).bounds((int) (this.width * 0.4), (int) (this.height * 0.9), (int) (this.width * 0.22), 20).build());
        }
        if (undoChangesRunnable != null) {
            this.addRenderableWidget(Button.builder(
                    Component.nullToEmpty("Undo changes"),
                    (button) -> {
                        undoChangesRunnable.run();
                        rebuildWidgets();
                    }).bounds((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20).build());
        }

    }

    @Override
    public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

#if MC < MC_20_2
    @Override
    public void renderBackground(final GuiGraphics guiGraphics) {
#elif MC < MC_20_6
    @Override
    public void renderTransparentBackground(final GuiGraphics guiGraphics) {
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int i, final int j, final float f) {


#endif


#if MC < MC_20_6
            guiGraphics.pose().pushPose();

        int topy = (int) (height * 0.15);
        int bottomy = (int) (height * 0.85);

        guiGraphics.pose().translate(0, 0, -100);
//        if (MinecraftClient.getInstance().world == null || !allowTransparentBackground()) {
        guiGraphics.setColor(0.15F, 0.15F, 0.15F, 1.0F);
        guiGraphics.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, height, 32, 32);
//        } else {
//            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
//        }

        guiGraphics.fillGradient(RenderType.gui(), 0, topy, this.width, topy + 4, -16777216, 0, 0);
        guiGraphics.fillGradient(RenderType.gui(), 0, bottomy - 4, this.width, bottomy, 0, -16777216, 0);

        guiGraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
        guiGraphics.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, topy, 32, 32);
        guiGraphics.blit(BACKGROUND_LOCATION, 0, bottomy, 0, 0.0F, 0.0F, this.width, topy, 32, 32);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
    }

    @Override
    public void renderDirtBackground(final GuiGraphics guiGraphics) {
    }
    #endif
}
