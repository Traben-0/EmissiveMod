package traben.entity_texture_features.mixin.entity.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Optional;

@Mixin(ParrotOnShoulderLayer.class)
#if MC > MC_21
public abstract class MixinShoulderParrotFeatureRenderer extends RenderLayer<PlayerRenderState, PlayerModel> {
#else
public abstract class MixinShoulderParrotFeatureRenderer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
#endif

    @Unique
    private ETFEntity etf$heldEntity = null;

#if MC > MC_21
    public MixinShoulderParrotFeatureRenderer(final RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At(value = "HEAD"))
    private void etf$alterEntityLeft(final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, final PlayerRenderState playerRenderState, final float f, final float g, final CallbackInfo ci) {
        etf$heldEntity = ETFRenderContext.getCurrentEntity();
        if (etf$heldEntity instanceof Player playerEntity) {
            etf$setParrotAsCurrentEntity(playerEntity, playerEntity.getShoulderEntityLeft());
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/ParrotOnShoulderLayer;renderOnShoulder(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lnet/minecraft/world/entity/animal/Parrot$Variant;FFZ)V"
                , shift = At.Shift.AFTER, ordinal = 0
            )
    )
    private void etf$alterEntityRight(final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, final PlayerRenderState playerRenderState, final float f, final float g, final CallbackInfo ci) {
        etf$heldEntity = ETFRenderContext.getCurrentEntity();
        if (etf$heldEntity instanceof Player playerEntity) {
            etf$setParrotAsCurrentEntity(playerEntity, playerEntity.getShoulderEntityRight());
        }
    }

    @Unique
    private static void etf$setParrotAsCurrentEntity(final Player playerEntity, final CompoundTag nbtCompound) {
        if (nbtCompound != null) {
            Optional<Entity> optionalEntity = EntityType.create(nbtCompound, playerEntity.level(), EntitySpawnReason.COMMAND);
            if (optionalEntity.isPresent() && optionalEntity.get() instanceof Parrot parrot) {
                ETFRenderContext.setCurrentEntity((ETFEntity) parrot);
            }
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "RETURN"))
    private void etf$resetEntity(CallbackInfo ci) {
        if (etf$heldEntity != null) {
            ETFRenderContext.setCurrentEntity(etf$heldEntity);
        }
        etf$heldEntity = null;
    }

#else
    @SuppressWarnings("unused")
    public MixinShoulderParrotFeatureRenderer(RenderLayerParent<T, PlayerModel<T>> context) {
        super(context);
    }

    @Inject(method = "method_17958", at = @At(value = "HEAD"))
    private void etf$alterEntity(PoseStack matrixStack, boolean bl, Player playerEntity, CompoundTag nbtCompound, MultiBufferSource vertexConsumerProvider, int i, float f, float g, float h, float j, EntityType<?> type, CallbackInfo ci) {
        if (nbtCompound != null) {

            etf$heldEntity = ETFRenderContext.getCurrentEntity();

            Optional<Entity> optionalEntity = EntityType.create(nbtCompound, playerEntity.level());
            if (optionalEntity.isPresent() && optionalEntity.get() instanceof Parrot parrot) {
                ETFRenderContext.setCurrentEntity((ETFEntity) parrot);
            }
        }
    }

    @Inject(method = "method_17958", at = @At(value = "RETURN"))
    private void etf$resetEntity(PoseStack matrixStack, boolean bl, Player playerEntity, CompoundTag nbtCompound, MultiBufferSource vertexConsumerProvider, int i, float f, float g, float h, float j, EntityType<?> type, CallbackInfo ci) {
        if (etf$heldEntity != null) {
            ETFRenderContext.setCurrentEntity(etf$heldEntity);
        }
        etf$heldEntity = null;
    }

#endif


}


