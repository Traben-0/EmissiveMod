package traben.entity_texture_features.mixin.client.entity.blockEntity;

import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BellBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.client.ETFUtils;

@Mixin(BellBlockEntityRenderer.class)
public abstract class MixinBellBlockEntityRenderer implements BlockEntityRenderer<BellBlockEntity> {

    @Shadow
    @Final
    public static SpriteIdentifier BELL_BODY_TEXTURE;

    @Shadow
    @Final
    private ModelPart bellBody;

    @Inject(method = "render(Lnet/minecraft/block/entity/BellBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At(value = "TAIL"))
    private void etf$applyEmissiveBell(BellBlockEntity bellBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci) {
        ETFUtils.renderEmissivePart(matrixStack, vertexConsumerProvider, BELL_BODY_TEXTURE.getTextureId(), this.bellBody, true);
    }
}


