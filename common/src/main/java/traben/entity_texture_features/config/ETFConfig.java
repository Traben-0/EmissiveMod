package traben.entity_texture_features.config;

import com.demonwav.mcdev.annotations.Translatable;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.screens.skin.ETFConfigScreenSkinTool;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.player.ETFPlayerTexture;
import traben.entity_texture_features.features.property_reading.properties.RandomProperties;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.tconfig.TConfig;
import traben.entity_texture_features.utils.ETFEntity;
import traben.tconfig.gui.entries.*;

import java.util.ArrayList;
import java.util.List;

import static traben.entity_texture_features.ETF.MOD_ID;
import static traben.entity_texture_features.ETFApi.getBlockEntityTypeToTranslationKey;


@SuppressWarnings("CanBeFinal")
public final class ETFConfig extends TConfig {

    public boolean optifine_limitRandomVariantGapsBy10 = true;
    public boolean optifine_allowWeirdSkipsInTrueRandom = true;
    public boolean optifine_preventBaseTextureInOptifineDirectory = true;

    public IllegalPathMode illegalPathSupportMode = IllegalPathMode.None;
    public boolean enableCustomTextures = true;
    public boolean enableCustomBlockEntities = true;
    public UpdateFrequency textureUpdateFrequency_V2 = UpdateFrequency.Fast;

    public boolean enableEmissiveTextures = true;
    public boolean enableEnchantedTextures = true;
    public boolean enableEmissiveBlockEntities = true;
    public EmissiveRenderModes emissiveRenderMode = EmissiveRenderModes.DULL;
    public boolean alwaysCheckVanillaEmissiveSuffix = true;
    public boolean enableArmorAndTrims = true;
    public boolean skinFeaturesEnabled = true;

    public SkinTransparencyMode skinTransparencyMode = SkinTransparencyMode.ETF_SKINS_ONLY;
    public boolean skinTransparencyInExtraPixels = true;

    @Deprecated
    public boolean skinFeaturesEnableTransparency = true;

    @Deprecated
    public boolean skinFeaturesEnableFullTransparency = false;

    @Deprecated
    public boolean tryETFTransparencyForAllSkins = false;
    public boolean enableEnemyTeamPlayersSkinFeatures = true;
    public boolean enableBlinking = true;
    public int blinkFrequency = 150;
    public int blinkLength = 1;
    public double advanced_IncreaseCacheSizeModifier = 1.0;
    public DebugLogMode debugLoggingMode = DebugLogMode.None;
    public boolean logTextureDataInitialization = false;
    @Deprecated
    public boolean hideConfigButton = false;

    public SettingsButtonLocation configButtonLoc = SettingsButtonLocation.BOTTOM_RIGHT;
    public boolean disableVanillaDirectoryVariantTextures = false;
    public boolean use3DSkinLayerPatch = true;
    public boolean enableFullBodyWardenTextures = true;
    public String2BooleanNullMap entityEmissiveOverrides = new String2BooleanNullMap();
    public ObjectOpenHashSet<String> propertiesDisabled = new ObjectOpenHashSet<>();
    public ObjectOpenHashSet<String> propertyInvertUpdatingOverrides = new ObjectOpenHashSet<>();
    public String2BooleanNullMap entityRandomOverrides = new String2BooleanNullMap();
    public String2EnumNullMap<EmissiveRenderModes> entityEmissiveBrightOverrides = new String2EnumNullMap<>();
    public String2EnumNullMap<RenderLayerOverride> entityRenderLayerOverrides = new String2EnumNullMap<>();
    public Object2IntOpenHashMap<String> entityLightOverrides = new Object2IntOpenHashMap<>();

    public boolean isPropertyDisabled(@NotNull RandomProperties.RandomPropertyFactory property) {
        return propertiesDisabled.contains(property.getPropertyId());
    }

    public boolean canPropertyUpdate(@NotNull RandomProperties.RandomPropertyFactory property) {
        return propertyInvertUpdatingOverrides.contains(property.getPropertyId()) != property.updatesOverTime();
    }

    public boolean canDoCustomTextures() {
        if (entityRandomOverrides.isEmpty() || ETFRenderContext.getCurrentEntity() == null)
            return enableCustomTextures;
        var key = ETFRenderContext.getCurrentEntity().etf$getEntityKey();
        if (key != null && entityRandomOverrides.containsKey(key)) {
            return entityRandomOverrides.getBoolean(key);
        }
        return enableCustomTextures;
    }


    public boolean canDoEmissiveTextures() {
        if (entityEmissiveOverrides.isEmpty() || ETFRenderContext.getCurrentEntity() == null)
            return enableEmissiveTextures;
        var key = ETFRenderContext.getCurrentEntity().etf$getEntityKey();
        if (key != null && entityEmissiveOverrides.containsKey(key)) {
            return entityEmissiveOverrides.getBoolean(key);
        }
        return enableEmissiveTextures;
    }

    public EmissiveRenderModes getEmissiveRenderMode() {
        if (entityEmissiveBrightOverrides.isEmpty() || ETFRenderContext.getCurrentEntity() == null)
            return emissiveRenderMode;
        var key = ETFRenderContext.getCurrentEntity().etf$getEntityKey();
        if (key != null && entityEmissiveBrightOverrides.containsKey(key)) {
            return entityEmissiveBrightOverrides.get(key);
        }
        return emissiveRenderMode;
    }

    public RenderLayerOverride getRenderLayerOverride() {
        if (entityRenderLayerOverrides.isEmpty() || ETFRenderContext.getCurrentEntity() == null)
            return null;
        var key = ETFRenderContext.getCurrentEntity().etf$getEntityKey();
        if (key != null && entityRenderLayerOverrides.containsKey(key)) {
            return entityRenderLayerOverrides.get(key);
        }
        return null;
    }

    public int getLightOverride(Entity entity, float tickDelta, int light) {
        if (entityLightOverrides.isEmpty() || entity == null)
            return light;
        var key = ((ETFEntity) entity).etf$getEntityKey();
        if (key != null && entityLightOverrides.containsKey(key)) {
            //noinspection deprecation
            int lightETF = Mth.clamp(entityLightOverrides.get(key), 0, 15);
            //recalculate to avoid child overrides
            var pos = BlockPos.containing(entity.getLightProbePosition(tickDelta));
            int block = entity.level().getBrightness(LightLayer.SKY, pos);//LightmapTextureManager.getBlockLightCoordinates(light);
            int sky = entity.isOnFire() ? 15 : entity.level().getBrightness(LightLayer.BLOCK, pos);//LightmapTextureManager.getSkyLightCoordinates(light);
            return LightTexture.pack(Math.max(block, sky), lightETF);
        }
        return light;
    }

    public int getLightOverrideBE(int light) {
        if (entityLightOverrides.isEmpty() || ETFRenderContext.getCurrentEntity() == null)
            return light;
        var key = ETFRenderContext.getCurrentEntity().etf$getEntityKey();
        if (key != null && entityLightOverrides.containsKey(key)) {
            //noinspection deprecation
            int lightETF = Mth.clamp(entityLightOverrides.get(key), 0, 15);

            var world = ETFRenderContext.getCurrentEntity().etf$getWorld();
            var pos = ETFRenderContext.getCurrentEntity().etf$getBlockPos();
            if (world == null || pos == null) return light;

            int block = world.getBrightness(LightLayer.BLOCK, pos);
            int sky = world.getBrightness(LightLayer.SKY, pos);
            return LightTexture.pack(Math.max(block, sky), lightETF);
        }
        return light;
    }


    @Override
    public TConfigEntryCategory getGUIOptions() {
        return new TConfigEntryCategory.Empty().add(
                new TConfigEntryCategory("config.entity_features.textures_main").add(
                        new TConfigEntryCategory("config.entity_texture_features.random_settings.title").add(
                                new TConfigEntryBoolean("config.entity_texture_features.enable_custom_textures.title", "config.entity_texture_features.enable_custom_textures.tooltip",
                                        () -> enableCustomTextures, aBoolean -> enableCustomTextures = aBoolean, true),
                                new TConfigEntryEnumSlider<>("config.entity_texture_features.texture_update_frequency.title", "config.entity_texture_features.texture_update_frequency.tooltip",
                                        () -> textureUpdateFrequency_V2, updateFrequency -> textureUpdateFrequency_V2 = updateFrequency,
                                        UpdateFrequency.Fast),
                                new TConfigEntryBoolean("config.entity_texture_features.custom_block_entity.title", "config.entity_texture_features.custom_block_entity.tooltip",
                                        () -> enableCustomBlockEntities, aBoolean -> enableCustomBlockEntities = aBoolean, true),
                                new TConfigEntryBoolean("config.entity_texture_features.disable_default_directory.title", "config.entity_texture_features.disable_default_directory.tooltip",
                                        () -> disableVanillaDirectoryVariantTextures, aBoolean -> disableVanillaDirectoryVariantTextures = aBoolean, false)
                        ), new TConfigEntryCategory("config.entity_texture_features.emissive_settings.title").add(
                                new TConfigEntryBoolean("config.entity_texture_features.enable_emissive_textures.title", "config.entity_texture_features.enable_emissive_textures.tooltip",
                                        () -> enableEmissiveTextures, aBoolean -> enableEmissiveTextures = aBoolean, true),
                                new TConfigEntryBoolean("config.entity_texture_features.emissive_block_entity.title", "config.entity_texture_features.emissive_block_entity.tooltip",
                                        () -> enableEmissiveBlockEntities, aBoolean -> enableEmissiveBlockEntities = aBoolean, true),
                                new TConfigEntryEnumButton<>("config.entity_texture_features.emissive_mode.title", "config.entity_texture_features.emissive_mode.tooltip",
                                        () -> emissiveRenderMode, renderMode -> emissiveRenderMode = renderMode, EmissiveRenderModes.DULL),
                                new TConfigEntryBoolean("config.entity_texture_features.always_check_vanilla_emissive_suffix.title", "config.entity_texture_features.always_check_vanilla_emissive_suffix.tooltip",
                                        () -> alwaysCheckVanillaEmissiveSuffix, aBoolean -> alwaysCheckVanillaEmissiveSuffix = aBoolean, true),
                                new TConfigEntryBoolean("config.entity_texture_features.armor_enable", "config.entity_texture_features.armor_enable.tooltip",
                                        () -> enableArmorAndTrims, aBoolean -> enableArmorAndTrims = aBoolean, true),
                                new TConfigEntryBoolean("config.entity_texture_features.enchanted_enable", "config.entity_texture_features.enchanted_enable.tooltip",
                                        () -> enableEnchantedTextures, aBoolean -> enableEnchantedTextures = aBoolean, true)
                        ), new TConfigEntryCategory("config.entity_texture_features.player_skin_settings.title").add(
                                new TConfigEntryBoolean("config.entity_texture_features.player_skin_features.title", "config.entity_texture_features.player_skin_features.tooltip",
                                        () -> skinFeaturesEnabled, aBoolean -> skinFeaturesEnabled = aBoolean, true),
//                                new TConfigEntryBoolean("config.entity_texture_features.skin_features_enable_transparency.title", "config.entity_texture_features.skin_features_enable_transparency.tooltip",
//                                        () -> skinFeaturesEnableTransparency, aBoolean -> skinFeaturesEnableTransparency = aBoolean, true),
//                                new TConfigEntryBoolean("config.entity_texture_features.skin_features_enable_full_transparency.title", "config.entity_texture_features.skin_features_enable_full_transparency.tooltip",
//                                        () -> skinFeaturesEnableFullTransparency, aBoolean -> skinFeaturesEnableFullTransparency = aBoolean, false),
//                                new TConfigEntryBoolean("config.entity_texture_features.skin_features_try_transparency_for_all.title", "config.entity_texture_features.skin_features_try_transparency_for_all.tooltip",
//                                        () -> tryETFTransparencyForAllSkins, aBoolean -> tryETFTransparencyForAllSkins = aBoolean, false),
                                new TConfigEntryEnumButton<>("config.entity_texture_features.transparent_skins.title", "config.entity_texture_features.transparent_skins.tooltip",
                                        () -> skinTransparencyMode, mode -> skinTransparencyMode = mode, SkinTransparencyMode.ETF_SKINS_ONLY),
                                new TConfigEntryBoolean("config.entity_texture_features.transparent_skins_extra.title", "config.entity_texture_features.transparent_skins_extra.tooltip",
                                        () -> skinTransparencyInExtraPixels, aBoolean -> skinTransparencyInExtraPixels = aBoolean, true),
                                new TConfigEntryBoolean("config.entity_texture_features.enable_enemy_team_players_skin_features.title", "config.entity_texture_features.enable_enemy_team_players_skin_features.tooltip",
                                        () -> enableEnemyTeamPlayersSkinFeatures, aBoolean -> enableEnemyTeamPlayersSkinFeatures = aBoolean, true),
                                ETF.SKIN_LAYERS_DETECTED ?
                                        new TConfigEntryBoolean("config.entity_texture_features.skin_layers_patch.title", "config.entity_texture_features.skin_layers_patch.tooltip",
                                                () -> use3DSkinLayerPatch, aBoolean -> use3DSkinLayerPatch = aBoolean, true) : null,
                                getPlayerSkinEditorButton()
                        ), new TConfigEntryCategory("config.entity_texture_features.blinking_mob_settings_sub.title").add(
                                new TConfigEntryBoolean("config.entity_texture_features.blinking_mob_settings.title", "config.entity_texture_features.blinking_mob_settings.tooltip",
                                        () -> enableBlinking, aBoolean -> enableBlinking = aBoolean, true),
                                new TConfigEntryInt("config.entity_texture_features.blink_frequency.title", "config.entity_texture_features.blink_frequency.tooltip",
                                        () -> blinkFrequency, aInt -> blinkFrequency = aInt, 150, 1, 1024),
                                new TConfigEntryInt("config.entity_texture_features.blink_length.title", "config.entity_texture_features.blink_length.tooltip",
                                        () -> blinkLength, aInt -> blinkLength = aInt, 1, 1, 20)

                        ), new TConfigEntryCategory("config.entity_texture_features.debug_screen.title").add(
                                new TConfigEntryEnumButton<>("config.entity_texture_features.debug_logging_mode.title", "config.entity_texture_features.debug_logging_mode.tooltip",
                                        () -> debugLoggingMode, debugLogMode -> debugLoggingMode = debugLogMode, DebugLogMode.None),
                                new TConfigEntryBoolean("config.entity_texture_features.log_creation", "config.entity_texture_features.log_creation.tooltip",
                                        () -> logTextureDataInitialization, aBoolean -> logTextureDataInitialization = aBoolean, false),
                                new TConfigEntryCustomButton("config.entity_texture_features.debug_screen.mass_log", "config.entity_texture_features.debug_screen.mass_log.tooltip",
                                        (button) -> {
                                            ETFManager.getInstance().doTheBigBoyPrintoutKronk();
                                            button.setMessage(ETF.getTextFromTranslation("config.entity_texture_features.debug_screen.mass_log.done"));
                                            button.active = false;
                                        })
                        )
                ), new TConfigEntryCategory("config.entity_features.general_settings.title").add(
                        new TConfigEntryEnumButton<>("config.entity_texture_features.allow_illegal_texture_paths.title", "config.entity_texture_features.allow_illegal_texture_paths.tooltip",
                                () -> illegalPathSupportMode, illegalPathMode -> illegalPathSupportMode = illegalPathMode, IllegalPathMode.None),
                        new TConfigEntryBoolean("config.entity_texture_features.warden.title", "config.entity_texture_features.warden.tooltip",
                                () -> enableFullBodyWardenTextures, aBoolean -> enableFullBodyWardenTextures = aBoolean, true),
                        new TConfigEntryEnumButton<>("config.entity_texture_features.settings_button_loc.title", "config.entity_texture_features.settings_button_loc.tooltip",
                                () -> configButtonLoc, settingsButtonLocation -> configButtonLoc = settingsButtonLocation, SettingsButtonLocation.BOTTOM_RIGHT)
                ),
                new TConfigEntryCategory("config.entity_texture_features.restrict_update_properties2").addAll(
                        getPropertySettings()
                ),
                getEntitySettings(),
                new TConfigEntryCategory("config.entity_features.optifine_settings","config.entity_texture_features.optifine.desc").addAll(
                        TConfigEntryText.fromLongOrMultilineTranslation("config.entity_texture_features.optifine.desc",200, TConfigEntryText.TextAlignment.LEFT)
                ).add(
                        new TConfigEntryBoolean("config.entity_texture_features.optifine.variant_gap.title", "config.entity_texture_features.optifine.variant_gap.tooltip",
                                () -> optifine_limitRandomVariantGapsBy10, aBoolean -> optifine_limitRandomVariantGapsBy10 = aBoolean, true),
                        new TConfigEntryBoolean("config.entity_texture_features.optifine.random_skips.title", "config.entity_texture_features.optifine.random_skips.tooltip",
                                () -> optifine_allowWeirdSkipsInTrueRandom, aBoolean -> optifine_allowWeirdSkipsInTrueRandom = aBoolean, true),
                        new TConfigEntryBoolean("config.entity_texture_features.optifine.prevent_base.title", "config.entity_texture_features.optifine.prevent_base.tooltip",
                                () -> optifine_preventBaseTextureInOptifineDirectory, aBoolean -> optifine_preventBaseTextureInOptifineDirectory = aBoolean, true)
                )
        );
    }

    private TConfigEntryCategory getEntitySettings() {
        var category = new TConfigEntryCategory("config.entity_features.per_entity_settings");
        try {
            BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                if (entityType == EntityType.PLAYER) return;
                String translationKey = entityType.getDescriptionId();
                var entityCategory = new TConfigEntryCategory(translationKey);
                addEntityConfigs(entityCategory, translationKey);
                category.add(entityCategory);
            });
            var warn = new TConfigEntryText("config.entity_features.per_entity_settings.blocks");
            var warn2 = new TConfigEntryText("config.entity_features.per_entity_settings.blocks2");
            category.add(warn, warn2);
            BlockEntityRenderers.PROVIDERS.keySet().forEach(entityType -> {
                String translationKey = getBlockEntityTypeToTranslationKey(entityType);
                var entityCategory = new TConfigEntryCategory(translationKey).add(warn, warn2);
                addEntityConfigs(entityCategory, translationKey);
                category.add(entityCategory);
            });
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return category;
    }

    private void addEntityConfigs(final TConfigEntryCategory entityCategory, final String translationKey) {
        entityCategory.add(
                new TConfigEntryCategory("config.entity_features.textures_main").add(
                        new TConfigEntryEnumButton<>("config.entity_texture_features.enable_emissive_textures.title", "config.entity_texture_features.enable_emissive_textures.tooltip",
                                () -> entityEmissiveOverrides.getNullable(translationKey), overrideBooleanType -> entityEmissiveOverrides.putNullable(translationKey, overrideBooleanType), null, OverrideBooleanType.class),
                        new TConfigEntryEnumButton<>("config.entity_texture_features.enable_custom_textures.title", "config.entity_texture_features.enable_custom_textures.tooltip",
                                () -> entityRandomOverrides.getNullable(translationKey), overrideBooleanType -> entityRandomOverrides.putNullable(translationKey, overrideBooleanType), null, OverrideBooleanType.class),
                        new TConfigEntryEnumButton<>("config.entity_texture_features.emissive_mode.title", "config.entity_texture_features.emissive_mode.tooltip",
                                () -> entityEmissiveBrightOverrides.getNullable(translationKey),
                                mode -> entityEmissiveBrightOverrides.putNullable(translationKey, mode),
                                null, EmissiveRenderModes.class),
                        new TConfigEntryEnumButton<>("config.entity_features.per_entity_settings.layer", "config.entity_features.per_entity_settings.layer.tooltip",
                                () -> entityRenderLayerOverrides.getNullable(translationKey),
                                layer -> entityRenderLayerOverrides.putNullable(translationKey, layer),
                                null, RenderLayerOverride.class)
                ),
                new TConfigEntryInt("config.entity_features.per_entity_settings.light", "config.entity_features.per_entity_settings.light.tooltip",
                        () -> entityLightOverrides.getOrDefault(translationKey, -1),
                        light -> {
                            if (light == -1) {
                                entityLightOverrides.removeInt(translationKey);
                                return;
                            }
                            //noinspection deprecation
                            entityLightOverrides.put(translationKey, light);
                        }, -1, -1, 15, true, false)
        );
    }

    private List<TConfigEntry> getPropertySettings() {
        var list = new ArrayList<TConfigEntry>();
        RandomProperties.forEachProperty(propertySettings -> {
            boolean defaultNoUpdate = !propertySettings.updatesOverTime();
            String id = propertySettings.getPropertyId();
            var category = new TConfigEntryCategory(id);
            list.add(category);
            category.add(
                    new TConfigEntryBoolean("config.entity_texture_features.restrict_update_properties.allow", "config.entity_texture_features.restrict_update_properties.allow.tooltip",
                            () -> !propertiesDisabled.contains(id),
                            aBoolean -> {
                                if (aBoolean) propertiesDisabled.remove(id);
                                else propertiesDisabled.add(id);
                            },
                            true),
                    new TConfigEntryBoolean("config.entity_texture_features.restrict_update_properties.lock", "config.entity_texture_features.restrict_update_properties.lock.tooltip",
                            () -> propertyInvertUpdatingOverrides.contains(id) != defaultNoUpdate,
                            aBoolean -> {
                                if (aBoolean != defaultNoUpdate) propertyInvertUpdatingOverrides.add(id);
                                else propertyInvertUpdatingOverrides.remove(id);
                            },
                            defaultNoUpdate)

            ).addAll(TConfigEntryText.fromLongOrMultilineTranslation(propertySettings.getExplanationTranslationKey(), 200, TConfigEntryText.TextAlignment.LEFT));
        });
        return list;
    }

    private TConfigEntry getPlayerSkinEditorButton() {
        boolean condition1 = ETF.config().getConfig().skinFeaturesEnabled;
        boolean condition2 = !ETF.isFabric() || ETF.isThisModLoaded("fabric");
        boolean condition3 = Minecraft.getInstance().player != null;
        boolean condition4 = ETFPlayerTexture.clientPlayerOriginalSkinImageForTool != null;
        boolean canLaunchSkinTool = condition1 && condition2 && condition3 && condition4;

        StringBuilder reasonText = new StringBuilder();
        if (!canLaunchSkinTool) {
            //log reason
            reasonText.append(ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.reason_0").getString());
            if (!condition1) {
                reasonText.append(ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.reason_1").getString());
            }
            if (!condition2) {
                reasonText.append(ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.reason_2").getString());
            }
            if (!condition3) {
                reasonText.append(ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.reason_3").getString());
            }
            if (!condition4) {
                reasonText.append(ETF.getTextFromTranslation("config.entity_texture_features.player_skin_editor.reason_4").getString());
            }
            //ETFUtils2.logWarn(reasonText.toString());
        }

        return canLaunchSkinTool ?
                new TConfigEntryCustomScreenOpener("config.entity_texture_features.player_skin_editor.button.enabled", reasonText.toString(),
                        () -> new ETFConfigScreenSkinTool(Minecraft.getInstance().screen), false) :
                new TConfigEntryCustomScreenOpener("config.entity_texture_features.player_skin_editor.button.disabled", reasonText.toString(),
                        () -> new ETFConfigScreenSkinTool(Minecraft.getInstance().screen), false).setEnabled(false);
    }

    @Override
    public ResourceLocation getModIcon() {
        return ETFUtils2.res(MOD_ID, "textures/gui/etf.png");
    }

    public enum OverrideBooleanType {
        TRUE, FALSE;

        @Override
        public String toString() {
            return switch (this) {
                case TRUE -> CommonComponents.OPTION_ON.getString();
                case FALSE -> CommonComponents.OPTION_OFF.getString();
            };
        }
    }


    @SuppressWarnings({"unused"})
    public enum UpdateFrequency {
        Never(-1, "config.entity_texture_features.update_frequency.never"),
        Slow(80, "config.entity_texture_features.update_frequency.slow"),
        Average(20, "config.entity_texture_features.update_frequency.average"),
        Fast(5, "config.entity_texture_features.update_frequency.fast"),
        Instant(1, "config.entity_texture_features.update_frequency.instant");

        final private int delay;
        final private String key;

        UpdateFrequency(int delay, @Translatable String key) {
            this.delay = delay;
            this.key = key;
        }

        public int getDelay() {
            return delay;
        }

        @Override
        public String toString() {
            return ETF.getTextFromTranslation(key).getString();
        }


    }

    @SuppressWarnings({"unused"})
    public enum DebugLogMode {
        None("config.entity_texture_features.Debug_log_mode.none"),
        Log("config.entity_texture_features.Debug_log_mode.log"),
        Chat("config.entity_texture_features.Debug_log_mode.chat");

        private final String key;

        DebugLogMode(@Translatable String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return ETF.getTextFromTranslation(key).getString();
        }

    }

    @SuppressWarnings({"unused"})
    public enum IllegalPathMode {
        None("options.off"),
        Entity("config.entity_texture_features.illegal_path_mode.entity"),
        All("config.entity_texture_features.illegal_path_mode.all");

        private final String key;

        IllegalPathMode(@Translatable String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return ETF.getTextFromTranslation(key).getString();
        }

    }

    public enum EmissiveRenderModes {
        DULL,
        BRIGHT;


        @Override
        public String toString() {
            return switch (this) {
                case DULL -> ETF.getTextFromTranslation(
                        "config.entity_texture_features.emissive_mode.dull").getString();
                case BRIGHT -> ETF.getTextFromTranslation(
                        "config.entity_texture_features.emissive_mode.bright").getString();
            };
        }

    }

    public enum RenderLayerOverride {
        TRANSLUCENT("config.entity_texture_features.render_layer.translucent"),
        TRANSLUCENT_CULL("config.entity_texture_features.render_layer.translucent_cull"),
        END("config.entity_texture_features.render_layer.end"),
        OUTLINE("config.entity_texture_features.render_layer.outline"),
        ;

        private final String key;

        RenderLayerOverride(@Translatable String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return ETF.getTextFromTranslation(key).getString();
        }

    }

    public enum SettingsButtonLocation{
        OFF,
        BOTTOM_RIGHT,
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT
    }

    public enum SkinTransparencyMode {
        @SuppressWarnings("unused") VANILLA("config.entity_texture_features.transparent_skins.vanilla"),
        ETF_SKINS_ONLY("config.entity_texture_features.transparent_skins.etf"),
        ALL("config.entity_texture_features.transparent_skins.all");

        private final String key;

        SkinTransparencyMode(@Translatable String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return ETF.getTextFromTranslation(key).getString();
        }

    }

    public static class String2BooleanNullMap extends Object2BooleanOpenHashMap<String> {
        public String2BooleanNullMap() {
            super();
            defaultReturnValue(false);
        }


        public void putNullable(final String s, final OverrideBooleanType v) {
            if (v == null) {
                removeBoolean(s);
                return;
            }
            super.put(s, v == OverrideBooleanType.TRUE);
        }

        public OverrideBooleanType getNullable(final String s) {
            if (getBoolean(s)) {
                return OverrideBooleanType.TRUE;
            } else {
                if (containsKey(s)) {
                    return OverrideBooleanType.FALSE;
                } else {
                    return null;
                }
            }

        }
    }

    public static class String2EnumNullMap<E extends Enum<E>> extends Object2ObjectOpenHashMap<String, E> {


        public void putNullable(final String s, final E v) {
            if (v == null) {
                remove(s);
                return;
            }
            super.put(s, v);
        }

        public E getNullable(final String s) {
            if (containsKey(s)) {
                return get(s);
            } else {
                return null;
            }
        }
    }


}
