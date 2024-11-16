package traben.entity_texture_features.config.screens.skin;


import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
#if MC > MC_21_2
import net.minecraft.client.renderer.texture.DynamicTexture;
#else
import net.minecraft.client.renderer.texture.HttpTexture;
#endif

#if MC > MC_20_1
import net.minecraft.client.resources.PlayerSkin;
#else
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
#endif
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.ETFVersionDifferenceManager;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.utils.ETFUtils2;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



//inspired by puzzles custom gui code
public class ETFConfigScreenSkinToolOutcome extends ETFScreenOldCompat {
    private final boolean didSucceed;
    private final NativeImage skin;

    protected ETFConfigScreenSkinToolOutcome(Screen parent, boolean success, NativeImage skin) {
        super("config." + ETF.MOD_ID + ".player_skin_editor.print_skin.result", parent, false);
        didSucceed = success;
        this.skin = skin;
        //this.skin = new PlayerSkinTexture(skin);
    }

    //upload code sourced from by https://github.com/cobrasrock/Skin-Swapper/blob/1.18-fabric/src/main/java/net/cobrasrock/skinswapper/changeskin/SkinChange.java
    //I do not intend to allow uploading of just any skin file, only ETF skin feature changes to an already existing skin, so I will not encroach on the scope of the excellent skin swapper mod
    public static boolean uploadSkin(boolean skinType) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            if ("127.0.0.1".equals(InetAddress.getLocalHost().getHostAddress())) {
                return false;
            }

            String auth = Minecraft.getInstance().getUser().getAccessToken();

            //uploads skin
            HttpPost http = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");


            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("variant", skinType ? "classic" : "slim", ContentType.TEXT_PLAIN);
            assert ETFVersionDifferenceManager.getConfigDirectory() != null;
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(Path.of(ETFVersionDifferenceManager.getConfigDirectory().toFile().getParent(), "\\ETF_player_skin_printout.png").toFile()),
                    ContentType.IMAGE_PNG,
                    "skin.png"
            );

            http.setEntity(builder.build());
            http.addHeader("Authorization", "Bearer " + auth);
            HttpResponse response = httpClient.execute(http);

            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void init() {
        super.init();


        this.addRenderableWidget(getETFButton((int) (this.width * 0.55), (int) (this.height * 0.9), (int) (this.width * 0.2), 20,
                CommonComponents.GUI_DONE,
                (button) -> Objects.requireNonNull(minecraft).setScreen(parent)));
        if (didSucceed) {
            this.addRenderableWidget(getETFButton((int) (this.width * 0.15), (int) (this.height * 0.6), (int) (this.width * 0.7), 20,
                    ETF.getTextFromTranslation("config." + ETF.MOD_ID + ".player_skin_editor.print_skin.open"),
                    (button) -> {
                        try {
                            assert ETFVersionDifferenceManager.getConfigDirectory() != null;
                            Path outputDirectory = Path.of(ETFVersionDifferenceManager.getConfigDirectory().toFile().getParent());
                            Util.getPlatform().openFile(outputDirectory.toFile());
                        } catch (Exception ignored) {
                        }
                    }));
            this.addRenderableWidget(getETFButton((int) (this.width * 0.15), (int) (this.height * 0.4), (int) (this.width * 0.7), 20,
                    ETF.getTextFromTranslation("config." + ETF.MOD_ID + ".player_skin_editor.upload_skin"),
                    (button) -> {
                        boolean skinType = true;//true for steve false for alex
                        if (Minecraft.getInstance().player != null && Minecraft.getInstance().getConnection() != null) {
                            PlayerInfo playerListEntry = Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getUUID());
                            if (playerListEntry != null) {
                                #if MC > MC_20_1
                                skinType = Minecraft.getInstance().getSkinManager().getInsecureSkin(playerListEntry.getProfile()).model() == PlayerSkin.Model.WIDE;
                                #else
                                String skinTypeData = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(playerListEntry.getProfile()).get(MinecraftProfileTexture.Type.SKIN).getMetadata("model");
                                if (skinTypeData != null) {
                                    skinType = !"slim".equals(skinTypeData);
                                }
                                #endif
                            }
                        }
                        boolean changeSuccess = uploadSkin(skinType);
                        button.setMessage(ETF.getTextFromTranslation("config." + ETF.MOD_ID + ".player_skin_editor.upload_skin_v2." +
                                (changeSuccess ? "success" : "fail")));
                        if (changeSuccess) {
                            //ETFUtils2.logMessage(ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".player_skin_editor.upload_skin.success" ).getString(),true);
                            //change internally cached skin
                            #if MC > MC_21_2

                            var texture = Minecraft.getInstance().getTextureManager().getTexture(Minecraft.getInstance().player.getSkin().texture());
                            if(texture instanceof DynamicTexture dynamicTexture){
                                dynamicTexture.setPixels(skin);
                            }

                            #else
                            HttpTexture skinfile =
                                #if MC > MC_20_1
                                    (HttpTexture) Minecraft.getInstance().getSkinManager().skinTextures.textureManager.getTexture((Minecraft.getInstance().player).getSkin().texture(), null);
                                #else
                                    (HttpTexture) Minecraft.getInstance().getSkinManager().textureManager.getTexture(Minecraft.getInstance().player.getSkinTextureLocation(), null);
                                #endif
                                try {
                                assert skinfile.file != null;
                                skin.writeToFile(skinfile.file);
                                } catch (IOException e) {
                                ETFUtils2.logError(ETF.getTextFromTranslation("config." + ETF.MOD_ID + ".player_skin_editor.upload_skin.success_local_fail").getString(), true);
                                //System.out.println("failed to change internal skin");
                            }
                            #endif

                            //clear etf data of skin
                            if (Minecraft.getInstance().player != null) {
                                ETFManager.getInstance().PLAYER_TEXTURE_MAP.removeEntryOnly(Minecraft.getInstance().player.getUUID());
                            }
                        }
                        button.active = false;
                    }));
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        String[] strings =
                ETF.getTextFromTranslation(
                        "config." + ETF.MOD_ID + ".player_skin_editor.print_skin.result." + (didSucceed ? "success" : "fail")
                ).getString().split("\n");
        List<Component> lines = new ArrayList<>();

        for (String str :
                strings) {
            lines.add(Component.nullToEmpty(str.strip()));
        }
        int i = 0;
        for (Component txt :
                lines) {
            context.drawCenteredString(font, txt.getVisualOrderText(), (int) (width * 0.5), (int) (height * 0.3) + i, 0xFFFFFF);
            i += txt.getString().isBlank() ? 5 : 10;
        }


    }

}
