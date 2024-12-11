package traben.entity_texture_features.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
#if MC > MC_20_1
import net.minecraft.client.gui.components.WidgetSprites;
#endif
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.config.screens.ETFConfigScreenMain;
import traben.entity_texture_features.utils.ETFUtils2;

import java.nio.file.Path;
import java.util.Objects;

@Mixin(PackSelectionScreen.class)
public abstract class MixinPackScreen extends Screen {


    @Unique
    private static final ResourceLocation etf$FOCUSED = ETFUtils2.res("entity_features", "textures/gui/settings_focused.png");
    @Unique
    private static final ResourceLocation etf$UNFOCUSED = ETFUtils2.res("entity_features", "textures/gui/settings_unfocused.png");
    @Shadow
    @Final
    private Path packDir;
    @Shadow
    private Button doneButton;

    @Unique
    private ImageButton etf$button = null;

    @SuppressWarnings("unused")
    protected MixinPackScreen(Component title) {
        super(title);
    }

    #if MC > MC_20_6
    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void etf$etfButtonResize(CallbackInfo ci) {
        if (etf$button == null) return;
        int[] vals = etf$etfButtonReSize();
        if (vals == null) return;
        int x = vals[0];
        int y = vals[1];
        etf$button.setX(x);
        etf$button.setY(y);
    }
    #endif


    @Inject(method = "init", at = @At("TAIL"))
    private void etf$etfButton(CallbackInfo ci) {

        if (ETF.config().getConfig().configButtonLoc == ETFConfig.SettingsButtonLocation.OFF) return;

        if (this.minecraft == null
                || !this.packDir.equals(this.minecraft.getResourcePackDirectory())
                || (ETF.isFabric() != ETF.isThisModLoaded("fabric")))
            return;

//        int x = doneButton.getX() + doneButton.getWidth() + 8;
//        int y = doneButton.getY();

        int[] vals = etf$etfButtonReSize();
        if (vals == null) return;
        int x = vals[0];
        int y = vals[1];

        etf$button = this.addRenderableWidget(new ImageButton(
                x, y, 24, 20,
                    #if MC > MC_20_1 new WidgetSprites(etf$UNFOCUSED, etf$FOCUSED), #else 0,0,20, etf$UNFOCUSED, #endif
                (button) -> Objects.requireNonNull(minecraft).setScreen(new ETFConfigScreenMain(this))
                    #if MC > MC_20_1 , Component.nullToEmpty("") #endif ) {
            {
                setTooltip(Tooltip.create(ETF.getTextFromTranslation(
                        "config.entity_features.button_tooltip")));
            }

            //override required because textured button widget just doesnt work
            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                ResourceLocation identifier = this.isHoveredOrFocused() ? etf$FOCUSED : etf$UNFOCUSED;
                context.blit(#if MC > MC_21 RenderType::guiTextured, #endif identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            }

        });


    }

    @Unique
    private @Nullable int[] etf$etfButtonReSize() {
        int x, y;

        switch (ETF.config().getConfig().configButtonLoc) {
            case BOTTOM_RIGHT -> {
                x = doneButton.getX() + doneButton.getWidth() + 8;
                y = doneButton.getY();
            }
            case BOTTOM_LEFT -> {
                int middle = width / 2;
                int bottomRight = doneButton.getX() + doneButton.getWidth() + 8;
                int offset = bottomRight - middle;
                x = middle - offset - 24;
                y = doneButton.getY();
            }
            case TOP_RIGHT -> {
                x = doneButton.getX() + doneButton.getWidth() + 8;
                y = height - doneButton.getY() - doneButton.getHeight();
            }
            case TOP_LEFT -> {
                var middle = width / 2;
                var bottomRight = doneButton.getX() + doneButton.getWidth() + 8;
                var offset = bottomRight - middle;
                x = middle - offset - 24;
                y = height - doneButton.getY() - doneButton.getHeight();
            }
            default -> {
                return null;
            }
        }
        return new int[]{x, y};
    }
}


