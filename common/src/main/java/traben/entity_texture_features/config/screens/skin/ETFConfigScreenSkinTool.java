package traben.entity_texture_features.config.screens.skin;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.ETFVersionDifferenceManager;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.player.ETFPlayerTexture;
import traben.entity_texture_features.utils.ETFUtils2;

import java.nio.file.Path;
import java.util.Objects;

import static traben.entity_texture_features.ETF.MOD_ID;

//inspired by puzzles custom gui code
@SuppressWarnings("EnhancedSwitchMigration")
public class ETFConfigScreenSkinTool extends ETFScreenOldCompat {
    private static final ResourceLocation APPLY_OVERLAY = ETFUtils2.res(MOD_ID + ":textures/skin_feature_printout.png");
    private static final ResourceLocation REMOVE_OVERLAY = ETFUtils2.res(MOD_ID + ":textures/skin_feature_remove.png");
    private static final ResourceLocation WHOLE_FACE_OVERLAY = ETFUtils2.res(MOD_ID + ":textures/skin_feature_whole_face.png");
    private static final ResourceLocation SMALL_EYE_OVERLAY = ETFUtils2.res(MOD_ID + ":textures/skin_feature_small_eyes.png");
    private static final ResourceLocation BOXES_OVERLAY = ETFUtils2.res(MOD_ID + ":textures/skin_feature_orange_areas.png");
    public Boolean originalEnableBlinking;
    public ETFPlayerTexture thisETFPlayerTexture = null;
    public NativeImage currentEditorSkin = null;
    public boolean flipView = false;
    Button printSkinFileButton = null;
    Button villagerNoseButton = null;
    Button coatButton = null;
    Button coatLengthButton = null;
    Button blinkButton = null;
    Button blinkHeightButton = null;
    Button emissiveButton = null;
    Button emissiveSelectButton = null;
    Button enchantButton = null;
    Button enchantSelectButton = null;
    //    ButtonWidget capeButton = null;
    Button transparencyButton = null;
    private Button overridesButton = null;
    private Boolean allowOverrides = null;

    public ETFConfigScreenSkinTool(Screen parent) {
        super("config." + MOD_ID + ".player_skin_features.title", parent, false);

    }

    public static int getPixelColour(int choice) {
        //no enhanced switch for back compat
        switch (choice) {
            case 1:
                return -65281;
            case 2:
                return -256;
            case 3:
                return -16776961;
            case 4:
                return -16711936;
            case 5:
                return -16760705;
            case 6:
                return -65536;
            case 7:
                return -16744449;
            case 8:
                return -14483457;
            default:
                return choice;
        }
    }

    private void onExit() {
        ETF.config().getConfig().enableBlinking = originalEnableBlinking;
        if (Minecraft.getInstance().player != null) {
            ETFManager.getInstance().PLAYER_TEXTURE_MAP.removeEntryOnly(Minecraft.getInstance().player.getUUID());
        }
        thisETFPlayerTexture = null;

    }

    @Override
    public void onClose() {
        onExit();
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();

        //make blinking enabled for skin tool
        if (originalEnableBlinking == null) {
            originalEnableBlinking = ETF.config().getConfig().enableBlinking;
            ETF.config().getConfig().enableBlinking = true;
        }


        if (Minecraft.getInstance().player != null && thisETFPlayerTexture == null) {
            thisETFPlayerTexture = ETFManager.getInstance().PLAYER_TEXTURE_MAP.get(Minecraft.getInstance().player.getUUID());
            if (thisETFPlayerTexture == null) {
                ETFPlayerTexture etfPlayerTexture = new ETFPlayerTexture();
                ETFManager.getInstance().PLAYER_TEXTURE_MAP.put(Minecraft.getInstance().player.getUUID(), etfPlayerTexture);
                thisETFPlayerTexture = etfPlayerTexture;
            } else if (thisETFPlayerTexture.etfTextureOfFinalBaseSkin != null) {
                thisETFPlayerTexture.etfTextureOfFinalBaseSkin.setGUIBlink();
            }
        }
        if (currentEditorSkin == null) {
            currentEditorSkin = ETFUtils2.emptyNativeImage(64, 64);
            NativeImage skin = ETFPlayerTexture.clientPlayerOriginalSkinImageForTool;
            if (skin != null) {
                currentEditorSkin.copyFrom(ETFPlayerTexture.clientPlayerOriginalSkinImageForTool);
            } else {

                onExit();
                ETFUtils2.logError("could not load tool as skin could not be loaded");
                Objects.requireNonNull(minecraft).setScreen(parent);

            }
        }

        this.addRenderableWidget(getETFButton(this.width / 2 - 210, (int) (this.height * 0.9), 200, 20, CommonComponents.GUI_CANCEL, (button) -> {
            onExit();
            Objects.requireNonNull(minecraft).setScreen(parent);
        }));

        this.addRenderableWidget(getETFButton((int) (this.width * 0.024), (int) (this.height * 0.2), 20, 20,
                Component.nullToEmpty("⟳"),
                (button) -> flipView = !flipView));

        printSkinFileButton = getETFButton(this.width / 2 + 10, (int) (this.height * 0.9), 200, 20,
                ETF.getTextFromTranslation("selectWorld.edit.save"),
                (button) -> {
                    boolean result = false;
                    if (Minecraft.getInstance().player != null) {
                        result = printPlayerSkinCopy();
                    }
                    onExit();
                    Objects.requireNonNull(minecraft).setScreen(new ETFConfigScreenSkinToolOutcome(parent, result, currentEditorSkin));
                });
        this.addRenderableWidget(printSkinFileButton);


        if (Minecraft.getInstance().player != null) {

            //skin feature buttons

            this.addRenderableWidget(getETFButton((int) (this.width * 0.25), (int) (this.height * 0.2), (int) (this.width * 0.42), 20,
                    thisETFPlayerTexture.hasFeatures ?
                            ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.remove_features") :
                            ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.add_features"),
                    (button) -> {
                        if (thisETFPlayerTexture.hasFeatures) {
                            applyExistingOverlayToSkin(REMOVE_OVERLAY);
                        } else {
                            applyExistingOverlayToSkin(APPLY_OVERLAY);
                        }
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        button.setMessage(thisETFPlayerTexture.hasFeatures ?
                                ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.remove_features") :
                                ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.add_features"));
                        updateButtons();
                    }));

            overridesButton = this.addRenderableWidget(getETFButton((int) (this.width * 0.695), (int) (this.height * 0.2), (int) (this.width * 0.275), 20,
                    ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.allow_examples"),
                    (button) -> {
                        allowOverrides = false;
                        button.active = false;
                        button.setMessage(ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.allow_examples.off"));
                    }, ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.allow_examples.tooltip")));

            villagerNoseButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.7), (int) (this.width * 0.2), 20,
                    Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.button").getString() +
                            thisETFPlayerTexture.noseType.getButtonText().getString()),
                    (button) -> {
                        int colour = thisETFPlayerTexture.noseType.next().getNosePixelColour();

                        ETFUtils2.setPixel(currentEditorSkin, 53, 17, colour);
                        if (thisETFPlayerTexture.noseType.next().appliesTextureOverlay()) {
                            applyExistingOverlayToSkin(BOXES_OVERLAY);
                        }
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);

                        button.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.button").getString() +
                                thisETFPlayerTexture.noseType.getButtonText().getString()));
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.tooltip"));

//            capeButton = getETFButton((int) (this.width * 0.47), (int) (this.height * 0.7), (int) (this.width * 0.2), 20,
//                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.button").getString() +
//                            thisETFPlayerTexture.capeType.getButtonText().getString()),
//                    (button) -> {
//                        CapeType cape = thisETFPlayerTexture.capeType.next();
//
//                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.button").getString() +
//                                cape.getButtonText().getString()));
//
//                        currentEditorSkin.setColor(53, 16, cape.getCapePixelColour());
//
//                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
//                        updateButtons();
//                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.tooltip")
//            );

            transparencyButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.7), (int) (this.width * 0.275), 20,
                    Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.button").getString() +
                            booleanAsOnOff(!thisETFPlayerTexture.wasForcedSolid)),
                    (button) -> {

                        button.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.button").getString() +
                                booleanAsOnOff(thisETFPlayerTexture.wasForcedSolid)));

                        ETFUtils2.setPixel(currentEditorSkin, 53, 18, getPixelColour(thisETFPlayerTexture.wasForcedSolid ? 0 : 1));

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        updateButtons();
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.tooltip")
            );

            coatButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.3), (int) (this.width * 0.42), 20,
                    CoatStyle.get(thisETFPlayerTexture.coatStyle).getTitle(),
                    (button) -> {
                        CoatStyle coat = CoatStyle.get(thisETFPlayerTexture.coatStyle).next();

                        button.setMessage(coat.getTitle());

                        ETFUtils2.setPixel(currentEditorSkin, 52, 17, coat.getCoatPixelColour());

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        updateButtons();
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.tooltip")
            );

            coatLengthButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.3), (int) (this.width * 0.275), 20,
                    Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.title").getString()
                            + thisETFPlayerTexture.coatLength),
                    (button) -> {
                        int lengthChoice;
                        if (thisETFPlayerTexture.coatLength == 8) {
                            lengthChoice = 1;
                        } else {
                            lengthChoice = thisETFPlayerTexture.coatLength + 1;
                        }
                        ETFUtils2.setPixel(currentEditorSkin, 52, 18, getPixelColour(lengthChoice));
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);

                        button.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.title").getString()
                                + thisETFPlayerTexture.coatLength));
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.tooltip")
            );

            blinkButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.4), (int) (this.width * 0.42), 20,
                    BlinkType.get(thisETFPlayerTexture.blinkType).getTitle(),
                    (button) -> {
                        BlinkType blink = BlinkType.get(thisETFPlayerTexture.blinkType).next();

                        button.setMessage(blink.getTitle());

                        if (blink != BlinkType.NONE
                                && blink != BlinkType.WHOLE_FACE_TWO
                                && blink != BlinkType.WHOLE_FACE) {
                            //set height choice to 0
                            ETFUtils2.setPixel(currentEditorSkin, 52, 19, getPixelColour(0));
                        } else if (ETFUtils2.getPixel(currentEditorSkin, 52, 19) > blink.getMaxEyePixelHeight()) {
                            //set height choice to the highest possible if too big for new type choice
                            ETFUtils2.setPixel(currentEditorSkin, 52, 19, getPixelColour(blink.getMaxEyePixelHeight()));
                        }

                        var overlay = blink.getExampleOverlay();
                        if (overlay != null) {
                            applyExistingOverlayToSkin(overlay);
                        }

                        ETFUtils2.setPixel(currentEditorSkin, 52, 16, blink.getBlinkPixelColour());

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        updateButtons();
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.tooltip")
            );

            blinkHeightButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.4), (int) (this.width * 0.275), 20,
                    Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.title").getString()
                            + thisETFPlayerTexture.blinkHeight),
                    (button) -> {
                        int heightChoice;
                        if (thisETFPlayerTexture.blinkHeight == BlinkType.get(thisETFPlayerTexture.blinkType).getMaxEyePixelHeight()) {
                            heightChoice = 1;
                        } else {
                            heightChoice = thisETFPlayerTexture.blinkHeight + 1;
                        }
                        ETFUtils2.setPixel(currentEditorSkin, 52, 19, getPixelColour(heightChoice));
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);

                        button.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.title").getString()
                                + thisETFPlayerTexture.blinkHeight));
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.tooltip")
            );

            emissiveButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.5), (int) (this.width * 0.42), 20,
                    Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.button").getString()
                            + (ETFUtils2.getPixel(currentEditorSkin, 1, 17) == getPixelColour(1) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).getString()),
                    (button) -> {


                        if (thisETFPlayerTexture.hasEmissives) {
                            ETFUtils2.setPixel(currentEditorSkin, 1, 17, 0);
                        } else {
                            ETFUtils2.setPixel(currentEditorSkin, 1, 17, getPixelColour(1));
                        }

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        button.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.button").getString()
                                + (ETFUtils2.getPixel(currentEditorSkin, 1, 17) == getPixelColour(1) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).getString()));
                        updateButtons();
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.tooltip")
            );

            emissiveSelectButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.5), (int) (this.width * 0.275), 20,
                    ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_select.button"),
                    (button) -> Objects.requireNonNull(minecraft).setScreen(new ETFConfigScreenSkinToolPixelSelection(this, ETFConfigScreenSkinToolPixelSelection.SelectionMode.EMISSIVE))
            );

            enchantButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.6), (int) (this.width * 0.42), 20,
                    Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.button").getString()
                            + (ETFUtils2.getPixel(currentEditorSkin, 1, 18) == getPixelColour(2) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).getString()),
                    (button) -> {


                        if (thisETFPlayerTexture.hasEnchant) {
                            ETFUtils2.setPixel(currentEditorSkin, 1, 18, 0);
                        } else {
                            ETFUtils2.setPixel(currentEditorSkin, 1, 18, getPixelColour(2));
                        }

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        button.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.button").getString()
                                + (ETFUtils2.getPixel(currentEditorSkin, 1, 18) == getPixelColour(2) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).getString()));
                        updateButtons();
                    }, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.tooltip")
            );

            enchantSelectButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.6), (int) (this.width * 0.275), 20,
                    ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_select.button"),
                    (button) -> Objects.requireNonNull(minecraft).setScreen(new ETFConfigScreenSkinToolPixelSelection(this, ETFConfigScreenSkinToolPixelSelection.SelectionMode.ENCHANTED))
            );

            updateButtons();
            this.addRenderableWidget(villagerNoseButton);
            this.addRenderableWidget(coatButton);
            this.addRenderableWidget(coatLengthButton);
            this.addRenderableWidget(blinkButton);
            this.addRenderableWidget(blinkHeightButton);
            this.addRenderableWidget(emissiveButton);
            this.addRenderableWidget(emissiveSelectButton);
            this.addRenderableWidget(enchantButton);
            this.addRenderableWidget(enchantSelectButton);
//            this.addDrawableChild(capeButton);
            this.addRenderableWidget(transparencyButton);


        }

    }

    private void updateButtons() {
        boolean activeFeatures = thisETFPlayerTexture.hasFeatures;
        if (villagerNoseButton != null) {
            villagerNoseButton.active = activeFeatures;
            villagerNoseButton.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.button").getString() +
                    thisETFPlayerTexture.noseType.getButtonText().getString()));
        }
        if (coatButton != null) {
            coatButton.active = activeFeatures;
            coatButton.setMessage(CoatStyle.get(thisETFPlayerTexture.coatStyle).getTitle());
        }
        if (coatLengthButton != null) {
            coatLengthButton.active = activeFeatures && CoatStyle.get(thisETFPlayerTexture.coatStyle) != CoatStyle.NONE;
            coatLengthButton.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.title").getString()
                    + thisETFPlayerTexture.coatLength));
        }
        if (blinkButton != null) {
            blinkButton.active = activeFeatures;
            blinkButton.setMessage(BlinkType.get(thisETFPlayerTexture.blinkType).getTitle());
        }
        if (blinkHeightButton != null) {
            blinkHeightButton.active = activeFeatures
                    && BlinkType.get(thisETFPlayerTexture.blinkType) != BlinkType.NONE
                    && BlinkType.get(thisETFPlayerTexture.blinkType) != BlinkType.WHOLE_FACE_TWO
                    && BlinkType.get(thisETFPlayerTexture.blinkType) != BlinkType.WHOLE_FACE;
            blinkHeightButton.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.title").getString()
                    + thisETFPlayerTexture.blinkHeight));
        }
        if (emissiveButton != null) {
            emissiveButton.active = activeFeatures;
            emissiveButton.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.button").getString()
                    + (ETFUtils2.getPixel(currentEditorSkin, 1, 17) == getPixelColour(1) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).getString()));
        }
        if (emissiveSelectButton != null) {
            emissiveSelectButton.active = activeFeatures
                    && ETFUtils2.getPixel(currentEditorSkin, 1, 17) == getPixelColour(1);
        }
        if (enchantButton != null) {
            enchantButton.active = activeFeatures;
            enchantButton.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.button").getString()
                    + (ETFUtils2.getPixel(currentEditorSkin, 1, 18) == getPixelColour(2) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).getString()));
        }
        if (enchantSelectButton != null) {
            enchantSelectButton.active = activeFeatures
                    && ETFUtils2.getPixel(currentEditorSkin, 1, 18) == getPixelColour(2);
        }
//        if (capeButton != null) {
//            capeButton.active = activeFeatures;
//            capeButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.button").getString() +
//                    thisETFPlayerTexture.capeType.getButtonText().getString()));
//        }
        if (transparencyButton != null) {
            transparencyButton.active = activeFeatures;
            transparencyButton.setMessage(Component.nullToEmpty(ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.button").getString() +
                    booleanAsOnOff(!thisETFPlayerTexture.wasForcedSolid)));
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);


        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {


            int height = (int) (this.height * 0.75);
            int playerX = (int) (this.width * 0.14);
            drawEntity(context, playerX, height, (int) (this.height * 0.3), (float) (-mouseX + playerX), (float) (-mouseY + (this.height * 0.3)), player);
        } else {
            context.drawString(font, Component.nullToEmpty("Player is null for some reason!"), width / 7, (int) (this.height * 0.4), 0xFFFFFF);
            context.drawString(font, Component.nullToEmpty("Cannot load player to render!"), width / 7, (int) (this.height * 0.45), 0xFFFFFF);
        }


        context.drawString(font, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.crouch_message"), width / 40, (int) (this.height * 0.8), 0x555555);
        context.drawString(font, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_message"), width / 40, (int) (this.height * 0.1), 0x555555);
//        if(ETFVersionDifferenceHandler.isThisModLoaded("iris"))
//            drawTextWithShadow(matrices, textRenderer, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.iris_message"), width / 8, (int) (this.height * 0.15), 0xFF5555);
    }

    public void applyExistingOverlayToSkin(ResourceLocation overlayTexture) {
        if ((ETF.isFabric() == ETF.isThisModLoaded("fabric"))) {//todo still needed? might be implicit now


            NativeImage overlayImage = ETFUtils2.getNativeImageElseNull(overlayTexture);
            assert overlayImage != null;
            //first check if the overlay will overwrite any pixels and prevent doing so if this is the case
            //ignore this if doing the apply/remove overlay
            if (!(overlayTexture.equals(REMOVE_OVERLAY) || overlayTexture.equals(APPLY_OVERLAY))) {
//                for (int x = 0; x < currentEditorSkin.getWidth(); x++) {
//                    for (int y = 0; y < currentEditorSkin.getHeight(); y++) {
//
//                        int overlay = overlayImage.getColor(x, y);
//                        boolean skinTransparent = currentEditorSkin.getOpacity(x, y) == 0;
//                        boolean conflictDetected = overlay != 0 && !skinTransparent;
//
//                        if (conflictDetected) {
//                            int skin = currentEditorSkin.getColor(x, y);
//                            //ignore an already applied overlay texture with identical pixels
//                            if (skin != overlay) {
                if (allowOverrides == null) {
                    Minecraft.getInstance().setScreen(new ConfirmScreen(Component.nullToEmpty(""), this));
                    if (allowOverrides == null) allowOverrides = false;
                }
                if (!allowOverrides) {
                    ETFUtils2.logMessage("Skin example overlay [" + overlayTexture + "] not applied.", false);
                    return;
                }
//                            }
//                        }
//                    }
//                }
            }

            try {
                for (int x = 0; x < currentEditorSkin.getWidth(); x++) {
                    for (int y = 0; y < currentEditorSkin.getHeight(); y++) {
                        if (ETFUtils2.getPixel(overlayImage, x, y) != 0) {
                            ETFUtils2.setPixel(currentEditorSkin, x, y, ETFUtils2.getPixel(overlayImage, x, y));
                        }
                    }
                }

                //ETFUtils2.logMessage("Skin feature layout successfully applied to a copy of your skin.", true);
            } catch (Exception e) {
                ETFUtils2.logMessage("Skin feature layout could not be applied to a copy of your skin. Error written to log.", false);
                ETFUtils2.logError(e.toString(), false);
            }

        } else {
            //requires fab api to read from mod resources
            ETFUtils2.logError("Fabric API required for skin processing, cancelling.", false);
        }
        //return skinImage;
    }

    public boolean printPlayerSkinCopy() {
        if ((ETF.isFabric() == ETF.isThisModLoaded("fabric")) && ETFVersionDifferenceManager.getConfigDirectory() != null) {
            Path outputDirectory = Path.of(ETFVersionDifferenceManager.getConfigDirectory().toFile().getParent(), "\\ETF_player_skin_printout.png");
            try {
                currentEditorSkin.writeToFile(outputDirectory);
                ETFUtils2.logMessage(ETF.getTextFromTranslation("config." + ETF.MOD_ID + ".player_skin_editor.print_skin.result.success").getString(), false);

                return true;
            } catch (Exception e) {
                //ETFUtils2.logMessage("Skin feature layout could not be applied to a copy of your skin and has not been saved. Error written to log.", true);
                ETFUtils2.logError(e.toString(), false);
            }

        }
        return false;
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
        entity.yBodyRot = (flipView ? 0 : 180.0F) + f * 20.0F;
        entity.setYRot((flipView ? 0 : 180.0F) + f * 40.0F);
        entity.setXRot(-g * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();


//        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        //noinspection deprecation
        #if MC > MC_21
        context.drawSpecial((bufferSource) -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, context.pose(), bufferSource, 15728880));
        #else

        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.pose(), context.bufferSource(), 15728880));
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


    @SuppressWarnings("EnhancedSwitchMigration")
    public enum NoseType {
        VILLAGER(1, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager")),
        VILLAGER_TEXTURED(7, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager2")),
        VILLAGER_REMOVE(8, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager3")),
        VILLAGER_TEXTURED_REMOVE(9, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager4")),
        TEXTURED_1(2, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.1")),
        TEXTURED_2(3, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.2")),
        TEXTURED_3(4, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.3")),
        TEXTURED_4(5, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.4")),
        TEXTURED_5(6, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.5")),
        NONE(0, ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.none"));

        public final int id;
        private final Component buttonText;


        NoseType(int i, Component buttonText) {
            this.id = i;
            this.buttonText = buttonText;
        }

        public NoseType getByColorId(int id) {
            for (NoseType nose : NoseType.values()) {
                if (nose.id == id) return nose;
            }
            return NONE;
        }

        public boolean appliesTextureOverlay() {
            return this == TEXTURED_1 || this == TEXTURED_2 || this == TEXTURED_3 || this == TEXTURED_4 || this == TEXTURED_5;
        }

        public Component getButtonText() {
            return buttonText;
        }

        public NoseType next() {
            switch (this) {
                case NONE:
                    return VILLAGER;
                case VILLAGER:
                    return VILLAGER_TEXTURED;
                case VILLAGER_TEXTURED:
                    return VILLAGER_REMOVE;
                case VILLAGER_REMOVE:
                    return VILLAGER_TEXTURED_REMOVE;
                case VILLAGER_TEXTURED_REMOVE:
                    return TEXTURED_1;
                case TEXTURED_1:
                    return TEXTURED_2;
                case TEXTURED_2:
                    return TEXTURED_3;
                case TEXTURED_3:
                    return TEXTURED_4;
                case TEXTURED_4:
                    return TEXTURED_5;
                default:
                    return NONE;

            }
        }

        public int getNosePixelColour() {
            return ETFPlayerTexture.getSkinNumberToPixelColour(id);
        }
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    public enum CoatStyle {
        COPIED_THIN_TOP,
        MOVED_THIN_TOP,
        COPIED_FAT_TOP,
        MOVED_FAT_TOP,
        COPIED_THIN,
        MOVED_THIN,
        COPIED_FAT,
        MOVED_FAT,
        NONE;


        public static CoatStyle get(int id) {
            //no enhanced switch for back compat
            switch (id) {
                case 1:
                    return COPIED_THIN_TOP;
                case 2:
                    return MOVED_THIN_TOP;
                case 3:
                    return COPIED_FAT_TOP;
                case 4:
                    return MOVED_FAT_TOP;
                case 5:
                    return COPIED_THIN;
                case 6:
                    return MOVED_THIN;
                case 7:
                    return COPIED_FAT;
                case 8:
                    return MOVED_FAT;
                default:
                    return NONE;
            }
        }

        public Component getTitle() {
            //no enhanced switch for back compat
            switch (this) {
                case COPIED_THIN_TOP:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.1");
                case MOVED_THIN_TOP:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.2");
                case COPIED_FAT_TOP:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.3");
                case MOVED_FAT_TOP:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.4");
                case COPIED_THIN:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.5");
                case MOVED_THIN:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.6");
                case COPIED_FAT:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.7");
                case MOVED_FAT:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.8");
                default:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.none");
            }
        }

        public int getCoatPixelColour() {
            //no enhanced switch for back compat
            switch (this) {
                case COPIED_THIN_TOP:
                    return -65281;
                case MOVED_THIN_TOP:
                    return -256;
                case COPIED_FAT_TOP:
                    return -16776961;
                case MOVED_FAT_TOP:
                    return -16711936;
                case COPIED_THIN:
                    return -16760705;
                case MOVED_THIN:
                    return -65536;
                case COPIED_FAT:
                    return -16744449;
                case MOVED_FAT:
                    return -14483457;
                default:
                    return 0;
            }
        }

        public CoatStyle next() {
            //no enhanced switch for back compat
            switch (this) {
                case NONE:
                    return COPIED_THIN_TOP;
                case COPIED_THIN_TOP:
                    return MOVED_THIN_TOP;
                case MOVED_THIN_TOP:
                    return COPIED_FAT_TOP;
                case COPIED_FAT_TOP:
                    return MOVED_FAT_TOP;
                case MOVED_FAT_TOP:
                    return COPIED_THIN;
                case COPIED_THIN:
                    return MOVED_THIN;
                case MOVED_THIN:
                    return COPIED_FAT;
                case COPIED_FAT:
                    return MOVED_FAT;
                default:
                    return NONE;
            }
        }
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    public enum BlinkType {
        ONE_PIXEL,
        TWO_PIXEL,
        FOUR_PIXEL,
        WHOLE_FACE,
        WHOLE_FACE_TWO,
        NONE;


        public static BlinkType get(int id) {
            //no enhanced switch for back compat
            switch (id) {
                case 3:
                    return ONE_PIXEL;
                case 4:
                    return TWO_PIXEL;
                case 5:
                    return FOUR_PIXEL;
                case 1:
                    return WHOLE_FACE;
                case 2:
                    return WHOLE_FACE_TWO;
                default:
                    return NONE;
            }
        }

        public ResourceLocation getExampleOverlay() {
            return switch (this) {
                case WHOLE_FACE, WHOLE_FACE_TWO -> WHOLE_FACE_OVERLAY;
                case NONE -> null;
                default -> SMALL_EYE_OVERLAY;
            };
        }

        public Component getTitle() {
            //no enhanced switch for back compat
            switch (this) {
                case ONE_PIXEL:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.1");
                case TWO_PIXEL:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.2");
                case FOUR_PIXEL:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.4");
                case WHOLE_FACE:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.whole.1");
                case WHOLE_FACE_TWO:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.whole.2");
                default:
                    return ETF.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.none");
            }
        }

        public int getBlinkPixelColour() {
            //no enhanced switch for back compat
            switch (this) {
                case ONE_PIXEL:
                    return getPixelColour(3);
                case TWO_PIXEL:
                    return getPixelColour(4);
                case FOUR_PIXEL:
                    return getPixelColour(5);
                case WHOLE_FACE:
                    return getPixelColour(1);
                case WHOLE_FACE_TWO:
                    return getPixelColour(2);
                default:
                    return 0;
            }
        }

        public int getMaxEyePixelHeight() {
            //no enhanced switch for back compat
            switch (this) {
                case ONE_PIXEL:
                    return 8;
                case TWO_PIXEL:
                    return 7;
                case FOUR_PIXEL:
                    return 5;
                default:
                    return 1;
            }
        }

        public BlinkType next() {
            //no enhanced switch for back compat
            switch (this) {
                case NONE:
                    return ONE_PIXEL;
                case ONE_PIXEL:
                    return TWO_PIXEL;
                case TWO_PIXEL:
                    return FOUR_PIXEL;
                case FOUR_PIXEL:
                    return WHOLE_FACE;
                case WHOLE_FACE:
                    return WHOLE_FACE_TWO;
                default:
                    return NONE;
            }
        }
    }

    private class ConfirmScreen extends Screen {

        final Screen parent;

        protected ConfirmScreen(final Component title, Screen parent) {
            super(title);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            if (overridesButton == null) {
                allowOverrides = false;
            } else {
                overridesButton.onPress();
            }
            Minecraft.getInstance().setScreen(this.parent);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }

        @Override
        protected void init() {
            super.init();

            addRenderableWidget(Button.builder(CommonComponents.GUI_YES, (button) -> {
                allowOverrides = true;
                Minecraft.getInstance().setScreen(this.parent);
            }).bounds(width / 2 - 210, height / 2 + 50, 200, 20).build());

            addRenderableWidget(Button.builder(CommonComponents.GUI_NO, (button) -> onClose()).bounds(width / 2 + 10, height / 2 + 50, 200, 20).build());
        }

        @Override
        public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
            super.render(context, mouseX, mouseY, delta);

            context.drawCenteredString(font, ETF.getTextFromTranslation("config.entity_texture_features.skin_editor.overlays.1"), width / 2, height / 2, 0xFFFFFF);
            context.drawCenteredString(font, ETF.getTextFromTranslation("config.entity_texture_features.skin_editor.overlays.2"), width / 2, height / 2 + 11, 0xFFFFFF);
            context.drawCenteredString(font, ETF.getTextFromTranslation("config.entity_texture_features.skin_editor.overlays.3"), width / 2, height / 2 + 22, 0xFFFFFF);
        }
    }

}
