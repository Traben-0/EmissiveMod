package traben.entity_texture_features.config.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Guardian;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfigWarning;
import traben.entity_texture_features.config.ETFConfigWarnings;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.tconfig.gui.TConfigScreenMain;
import traben.tconfig.gui.entries.TConfigEntryCategory;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ETFConfigScreenMain extends TConfigScreenMain {

    final ObjectOpenHashSet<ETFConfigWarning> warningsFound = new ObjectOpenHashSet<>();

    private final Random rand = new Random();
    private final LogoCreeperRenderer LOGO_CREEPER = new LogoCreeperRenderer();
    private final ResourceLocation BLUE = ETFUtils2.res("entity_features", "textures/gui/entity/e.png");
    private final ResourceLocation RED = ETFUtils2.res("entity_features", "textures/gui/entity/t.png");
    private final ResourceLocation YELLOW = ETFUtils2.res("entity_features", "textures/gui/entity/f.png");
    boolean shownWarning = false;
    int warningCount = 0;
    private long timer = 0;
    private LivingEntity livingEntity = null;

    public ETFConfigScreenMain(Screen parent) {
        super("config.entity_features", parent, ETF.configHandlers, List.of(
                new TConfigEntryCategory("config.entity_features.textures_main"),
                new TConfigEntryCategory("config.entity_features.models_main").setEmptyTooltip("config.entity_features.empty_emf"),
                new TConfigEntryCategory("config.entity_features.sounds_main").setEmptyTooltip("config.entity_features.empty_esf"),
                new TConfigEntryCategory("config.entity_features.general_settings.title"),
                new TConfigEntryCategory("config.entity_features.optifine_settings"),
                new TConfigEntryCategory("config.entity_features.per_entity_settings")
        ));


        for (ETFConfigWarning warning :
                ETFConfigWarnings.getRegisteredWarnings()) {
            if (warning.isConditionMet()) {
                shownWarning = true;
                warningCount++;
                warningsFound.add(warning);
            }
        }
    }

    public static void drawEntity(GuiGraphics context, float x, float y, int size, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity entity) {
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
        #if MC > MC_21
        context.drawSpecial((multiBufferSource) -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, context.pose(), multiBufferSource, 15728880);
        });
        #else
        //noinspection deprecation
        RenderSystem.runAsFancy(() ->
                entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.pose(), context.bufferSource(), 15728880));
        #endif
        context.flush();
        entityRenderDispatcher.setRenderShadow(true);
        context.pose().popPose();
        Lighting.setupFor3DItems();
    }

    @Override
    protected void init() {
        super.init();
        if (shownWarning) {
            this.addRenderableWidget(Button.builder(Component.translatable("config.entity_features.warnings_main"),
                            (button) -> Objects.requireNonNull(minecraft).setScreen(new ETFConfigScreenWarnings(this, warningsFound)))
                    .bounds((int) (this.width * 0.1), (int) (this.height * 0.1) - 15, (int) (this.width * 0.2), 20
                    ).build());
        }
    }

    @Override
    public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
        super.render(context, mouseX, mouseY, delta);
        //get a random entity and display for 5 seconds
        //noinspection ConstantValue
        if (timer + 5000 < System.currentTimeMillis() && Minecraft.getInstance().player != null && Minecraft.getInstance().player.level() != null) {
            List<Entity> entityList = Minecraft.getInstance().player.level().getEntities(null, Minecraft.getInstance().player.getBoundingBox().inflate(128));
            Entity entity = null;
            for (int i = 0; i < Math.min(entityList.size(), 24); i++) {
                entity = entityList.get(rand.nextInt(entityList.size()));
                if (entity instanceof LivingEntity) break;
            }
            if (entity instanceof LivingEntity) {
                livingEntity = (LivingEntity) entity;
                timer = System.currentTimeMillis();
            }
        }

        //draw the entity else ETF logo creepers
        if (livingEntity != null && !livingEntity.isRemoved()) {
            renderEntitySample(context, mouseY);
        } else {
            renderETFLogoCreepers(context, mouseX, mouseY);
        }
        context.pose().popPose();
    }

    private void renderETFLogoCreepers(final GuiGraphics context, final int mouseX, final int mouseY) {
        int y = (int) (this.height * 0.75);
        int x = (int) (this.width * 0.33);
        float g = (float) -Math.atan(((-mouseY + this.height / 2f) / 40.0F));
        float g2 = (float) -Math.atan(((-mouseX + this.width / 3f) / 400.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F).rotateY(g2);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(-(g * 20.0F * 0.017453292F));
        quaternionf.mul(quaternionf2);

        context.pose().pushPose();
        context.pose().translate(x, y, 150.0);
        float scaling = (float) (this.height * 0.3);
        #if MC >= MC_20_6
        context.pose().mulPose((new Matrix4f()).scaling(scaling, scaling, -scaling));
        #else
        context.pose().mulPoseMatrix((new Matrix4f()).scaling(scaling, scaling, -scaling));
        #endif
        context.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();

        float sin1 = (float) (Math.sin(System.currentTimeMillis() / 500d) / 32);
        float sin2 = (float) (Math.sin(System.currentTimeMillis() / 500d + 1) / 32);
        float sin3 = (float) (Math.sin(System.currentTimeMillis() / 500d + 2) / 32);

        PoseStack matrixStack = context.pose();

        matrixStack.pushPose();
        matrixStack.translate(-0.6, -sin1, 0);
        matrixStack.scale(1 + sin1, 1 + sin1, 1 + sin1);
        LOGO_CREEPER.renderSimple(matrixStack, context.bufferSource #if MC < MC_21_2 () #endif, YELLOW);
        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.translate(0, -sin2, 0);
        matrixStack.scale(1 + sin2, 1 + sin2, 1 + sin2);
        LOGO_CREEPER.renderSimple(matrixStack, context.bufferSource #if MC < MC_21_2 () #endif, RED);
        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.translate(0.6, -sin3, 0);
        matrixStack.scale(1 + sin3, 1 + sin3, 1 + sin3);
        LOGO_CREEPER.renderSimple(matrixStack, context.bufferSource #if MC < MC_21_2 () #endif,  BLUE);
        matrixStack.popPose();

        Lighting.setupFor3DItems();
    }

    private void renderEntitySample(final GuiGraphics context, final int mouseY) {
        int y = (int) (this.height * 0.75);
        if (livingEntity.getBbHeight() < 0.7) y -= (int) (this.height * 0.15);
        int x = (int) (this.width * 0.33);
        //float f = (float)Math.atan((double)(-mouseX / 40.0F));
        float g = (float) Math.atan(((-mouseY + this.height / 2f) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F).rotateY((float) (System.currentTimeMillis() / 1000d % (2 * Math.PI)));
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(-(g * 20.0F * 0.017453292F));
        quaternionf.mul(quaternionf2);
        double scale;
        double autoScale = (this.height * 0.4) / ((Math.max(1, Math.max(livingEntity.getBbHeight(), livingEntity.getBbWidth()))));
        if (livingEntity instanceof Squid) {
            y -= (int) (this.height * 0.15);
            scale = autoScale * 0.5;
        } else if (livingEntity instanceof Guardian || livingEntity instanceof Sniffer) {
            y -= (int) (this.height * 0.1);
            scale = autoScale * 0.7;
        } else if (livingEntity instanceof EnderDragon) {
            y -= (int) (this.height * 0.15);
            scale = autoScale * 1.5;
        } else {
            scale = autoScale;
        }

        double scaleModify = Math.sin((System.currentTimeMillis() - timer) / 5000d * Math.PI) * 6;
        double scaleModify2 = Math.max(Math.min(Math.abs(scaleModify), 1d), 0);//clamp
        int modelHeight = (int) Math.min(scale * scaleModify2, (this.height * 0.4));

        context.pose().pushPose();
        drawEntity(context, x, y, modelHeight, quaternionf, quaternionf2, livingEntity);
    }

    public static class LogoCreeperRenderer {


        private final ModelPart root;

        public LogoCreeperRenderer() {
            root = CreeperModel.createBodyLayer(CubeDeformation.NONE).bakeRoot();
            root.getChild("right_hind_leg").xRot = (float) -Math.toRadians(25);
            root.getChild("left_hind_leg").xRot = (float) Math.toRadians(25);
            root.getChild("left_hind_leg").z -= 2;
            root.getChild("right_front_leg").xRot = (float) Math.toRadians(25);
            root.getChild("left_front_leg").xRot = (float) -Math.toRadians(25);
            root.getChild("left_front_leg").z += 2;
        }


        public void renderSimple(final PoseStack matrix, final MultiBufferSource vcp, ResourceLocation texture) {
            matrix.pushPose();
            matrix.scale(-1.0F, -1.0F, 1.0F);
            matrix.translate(0.0F, -1.501F, 0.0F);
            RenderType rendertype = RenderType.entitySolid(texture);
            //noinspection ConstantValue
            if (rendertype != null) {
                VertexConsumer vertexconsumer = vcp.getBuffer(rendertype);
                root.render(matrix, vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY #if MC < MC_21 , 1F, 1F, 1F, 1F #endif);
            }
            matrix.popPose();
        }
    }
}
