package traben.entity_texture_features.mixin.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import traben.entity_texture_features.ETFApi;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.UUID;

import static traben.entity_texture_features.ETFApi.getBlockEntityTypeToTranslationKey;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ETFEntity {

    @Unique
    private UUID etf$id = null;


    @Shadow
    public abstract BlockEntityType<?> getType();


    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    @Nullable
    public abstract Level getLevel();

    @Shadow
    protected abstract void saveMetadata(final CompoundTag compoundTag);

    @Override
    public EntityType<?> etf$getType() {
        return null;
    }

    @Override
    public UUID etf$getUuid() {
        if (etf$id == null) etf$id = ETFApi.getUUIDForBlockEntity((BlockEntity) ((Object) this));
        return etf$id;
    }

    @Override
    public Level etf$getWorld() {
        return getLevel();
    }

    @Override
    public BlockPos etf$getBlockPos() {
        return getBlockPos();
    }

    @Override
    public int etf$getBlockY() {
        return getBlockPos().getY();
    }

    @Override
    public CompoundTag etf$getNbt() {
        return ETFRenderContext.cacheEntityNBTForFrame(etf$getUuid(),
                ()->{
                    CompoundTag compound = new CompoundTag();
                    saveMetadata(compound);
                    return compound;
                });
    }

    @Override
    public boolean etf$hasCustomName() {
        return this instanceof Nameable name && name.hasCustomName();
    }

    @Override
    public Component etf$getCustomName() {
        return this instanceof Nameable name && name.hasCustomName() ? name.getCustomName() : Component.nullToEmpty("null");
    }

    @Override
    public Team etf$getScoreboardTeam() {
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getItemsEquipped() {
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getHandItems() {
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getArmorItems() {
        return null;
    }

    @Override
    public float etf$distanceTo(Entity entity) {
        BlockPos pos = getBlockPos();
        float f = (float) (pos.getX() - entity.getX());
        float g = (float) (pos.getY() - entity.getY());
        float h = (float) (pos.getZ() - entity.getZ());
        return Mth.sqrt(f * f + g * g + h * h);
    }

    @Override
    public Vec3 etf$getVelocity() {
        return Vec3.ZERO;
    }

    @Override
    public Pose etf$getPose() {
        return Pose.STANDING;
    }

    @Override
    public boolean etf$canBeBright() {
        return false;
    }

    @Override
    public boolean etf$isBlockEntity() {
        return true;
    }

    @Override
    public String etf$getEntityKey() {
        return getBlockEntityTypeToTranslationKey(getType());
    }
}
