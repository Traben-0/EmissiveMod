package traben.entity_texture_features.mixin.entity.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

#if MC > MC_21
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.model.EntityModel;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.ETF;

import java.util.List;

@Mixin(WardenModel.class)
public abstract class MixinWardenExtraTextureParts extends EntityModel<WardenRenderState> {

    @Shadow @Final protected ModelPart bone;
    @Shadow @Final protected ModelPart body;
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart rightArm;
    @Shadow @Final protected ModelPart rightLeg;
    @Shadow @Final protected ModelPart rightRibcage;
    @Shadow @Final protected ModelPart rightTendril;
    @Shadow @Final protected ModelPart leftArm;
    @Shadow @Final protected ModelPart leftLeg;
    @Shadow @Final protected ModelPart leftRibcage;
    @Shadow @Final protected ModelPart leftTendril;

    @SuppressWarnings("unused")
    protected MixinWardenExtraTextureParts(final ModelPart modelPart) {
        super(modelPart);
    }

    @Inject(method = {
            "getPulsatingSpotsLayerModelParts",
            "getTendrilsLayerModelParts",
            "getBioluminescentLayerModelParts",
            "getHeartLayerModelParts"
    }, at = @At(value = "RETURN"), cancellable = true)
    private void etf$modifyParts1(final CallbackInfoReturnable<List<ModelPart>> cir) {
        if (ETF.config().getConfig().enableFullBodyWardenTextures) {
            cir.setReturnValue(List.of(bone, body, head, rightArm,rightLeg,rightRibcage,rightTendril,leftArm, leftLeg, leftRibcage, leftTendril));
        }
    }
}

#else
import net.minecraft.client.model.WardenModel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.client.renderer.entity.layers.WardenEmissiveLayer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.utils.ETFUtils2;

@Mixin(WardenEmissiveLayer.class)
public abstract class MixinWardenExtraTextureParts<T extends Warden, M extends WardenModel<T>> extends RenderLayer<T, M> {
    @SuppressWarnings("unused")
    public MixinWardenExtraTextureParts(RenderLayerParent<T, M> context) {
        super(context);
    }

        @Unique
    private static final ResourceLocation VANILLA_TEXTURE = ETFUtils2.res("textures/entity/warden/warden.png");
    @Shadow
    @Final
    private ResourceLocation texture;




    @Shadow
    protected abstract void resetDrawForAllParts();

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/monster/warden/Warden;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/WardenEmissiveLayer;onlyDrawSelectedParts()V",
                    shift = At.Shift.AFTER))
    private void etf$preventHiding(CallbackInfo ci) {
        if (ETF.config().getConfig().enableFullBodyWardenTextures && !VANILLA_TEXTURE.equals(texture)) {
            resetDrawForAllParts();
        }
    }
}
#endif





