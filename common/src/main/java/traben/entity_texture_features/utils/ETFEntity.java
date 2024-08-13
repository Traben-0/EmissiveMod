package traben.entity_texture_features.utils;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public interface ETFEntity {


    boolean etf$canBeBright();

    boolean etf$isBlockEntity();

    @Nullable
    EntityType<?> etf$getType();

    UUID etf$getUuid();


    Level etf$getWorld();

    BlockPos etf$getBlockPos();

    int etf$getBlockY();

    CompoundTag etf$getNbt();

    boolean etf$hasCustomName();

    Component etf$getCustomName();

    Team etf$getScoreboardTeam();

    //boolean isOnFire();

    Iterable<ItemStack> etf$getItemsEquipped();

    Iterable<ItemStack> etf$getHandItems();

    Iterable<ItemStack> etf$getArmorItems();

    float etf$distanceTo(Entity entity);

    Vec3 etf$getVelocity();

    @Deprecated
    Pose etf$getPose();

    @Nullable
    String etf$getEntityKey();

}
