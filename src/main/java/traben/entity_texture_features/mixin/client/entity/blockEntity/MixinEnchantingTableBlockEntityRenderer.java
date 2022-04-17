package traben.entity_texture_features.mixin.client.entity.blockEntity;

import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.client.ETFUtils;

@Mixin(EnchantingTableBlockEntityRenderer.class)
public abstract class MixinEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<EnchantingTableBlockEntity> {

    @Shadow
    @Final
    public static SpriteIdentifier BOOK_TEXTURE;
    @Shadow
    @Final
    private BookModel book;

    @Inject(method = "render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BookModel;renderBook(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
                    shift = At.Shift.AFTER))
    private void etf$applyEmissiveBook(EnchantingTableBlockEntity enchantingTableBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci) {
        //BOOK_TEXTURE.getTextureId().toString()
        VertexConsumer etf$vertex = ETFUtils.generalEmissiveGetVertexConsumer(new Identifier("minecraft:textures/entity/enchanting_table_book.png"), vertexConsumerProvider, false);
        if (etf$vertex != null) {
            this.book.renderBook(matrixStack, etf$vertex, 15728640, j, 1, 1, 1, 1);
        }
    }
}


