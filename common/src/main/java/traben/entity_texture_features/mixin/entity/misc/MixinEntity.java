package traben.entity_texture_features.mixin.entity.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class MixinEntity implements ETFEntity {
    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract UUID getUuid();

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    public abstract int getBlockY();

    @Shadow
    public abstract NbtCompound writeNbt(NbtCompound nbt);

    @Shadow
    public abstract boolean hasCustomName();

    @Shadow
    @Nullable
    public abstract Text getCustomName();

    @Shadow
    @Nullable
    public abstract AbstractTeam getScoreboardTeam();

    @Shadow
    public abstract Iterable<ItemStack> getItemsEquipped();



    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();

    @Shadow
    public abstract float distanceTo(Entity entity);

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract EntityPose getPose();

    @Shadow public abstract boolean isPlayer();

    @Shadow public abstract Iterable<ItemStack> getItemsHand();

    @Override
    public EntityType<?> etf$getType() {
        return getType();
    }

    @Override
    public UUID etf$getUuid() {
        return getUuid();
    }

    @Override
    public World etf$getWorld() {
        return getWorld();
    }

    @Override
    public BlockPos etf$getBlockPos() {
        return getBlockPos();
    }

    @Override
    public int etf$getBlockY() {
        return getBlockY();
    }

    @Override
    public NbtCompound etf$writeNbt(NbtCompound compound) {
        return writeNbt(compound);
    }

    @Override
    public boolean etf$hasCustomName() {
        return hasCustomName();
    }

    @Override
    public Text etf$getCustomName() {
        return getCustomName();
    }

    @Override
    public AbstractTeam etf$getScoreboardTeam() {
        return getScoreboardTeam();
    }

    @Override
    public Iterable<ItemStack> etf$getItemsEquipped() {
        return getItemsEquipped();
    }

    @Override
    public Iterable<ItemStack> etf$getHandItems() {
        return getItemsHand();
    }

    @Override
    public Iterable<ItemStack> etf$getArmorItems() {
        return getArmorItems();
    }

    @Override
    public float etf$distanceTo(Entity entity) {
        return distanceTo(entity);
    }

    @Override
    public Vec3d etf$getVelocity() {
        return getVelocity();
    }

    @Override
    public EntityPose etf$getPose() {
        return getPose();
    }

    @Override
    public boolean etf$canBeBright() {
        return !isPlayer();
    }

    @Override
    public boolean etf$isBlockEntity() {
        return false;
    }
}
