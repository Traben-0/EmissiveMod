package traben.entity_texture_features.mixin.entity.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.player.ETFPlayerEntity;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends Entity implements ETFPlayerEntity {


    @SuppressWarnings("unused")
    public MixinPlayerEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract Inventory getInventory();


    @Shadow
    public abstract @NotNull Component getName();

    @Shadow
    public abstract boolean isModelPartShown(final PlayerModelPart playerModelPart);

    //will force update entity texture at any player interaction useful for debugging
    @Inject(method = "interactOn", at = @At("HEAD"))
    private void etf$injected(Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (level().isClientSide()) {
            if (ETF.config().getConfig().debugLoggingMode != ETFConfig.DebugLogMode.None)
                ETFManager.getInstance().markEntityForDebugPrint(entity.getUUID());
        }
    }

    @Override
    public Entity etf$getEntity() {
        return this;
    }

    @Override
    public boolean etf$isTeammate(Player player) {
        return isAlliedTo(player);
    }

    @Override
    public Inventory etf$getInventory() {
        return getInventory();
    }

    @Override
    public boolean etf$isPartVisible(PlayerModelPart part) {
        return isModelPartShown(part);
    }

    @Override
    public Component etf$getName() {
        return getName();
    }

    @Override
    public String etf$getUuidAsString() {
        return getStringUUID();
    }

}


