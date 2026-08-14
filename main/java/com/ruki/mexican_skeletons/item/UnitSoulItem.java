package com.ruki.mexican_skeletons.item;

import com.ruki.mexican_skeletons.entity.SmartZombieEntity;
import com.ruki.mexican_skeletons.registry.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

public class UnitSoulItem extends Item {

    public UnitSoulItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        // Let the server do the actual resurrection.
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        ItemStack soulStack = context.getItemInHand();
        Player player = context.getPlayer();

        // ===== READ SOUL DATA =====

        CustomData customData =
                soulStack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("This soul contains no unit data."),
                        true
                );
            }

            return InteractionResult.FAIL;
        }

        CompoundTag soulData = customData.copyTag();

        if (!soulData.contains(
                "UnitEntityData",
                Tag.TAG_COMPOUND
        )) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("This soul is corrupted."),
                        true
                );
            }

            return InteractionResult.FAIL;
        }

        CompoundTag entityData =
                soulData.getCompound("UnitEntityData");

        // ===== CREATE FRESH ENTITY =====

        SmartZombieEntity revived =
                ModEntities.SMART_ZOMBIE.get().create(serverLevel);

        if (revived == null) {
            return InteractionResult.FAIL;
        }

        // Spawn on the face of the block that was clicked.
        BlockPos spawnPos =
                context.getClickedPos()
                        .relative(context.getClickedFace());

        revived.restoreFromSoul(
                entityData,
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D
        );

        // ===== ACTUALLY SPAWN =====

        boolean spawned =
                serverLevel.addFreshEntity(revived);

        if (!spawned) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("The unit could not be revived here."),
                        true
                );
            }

            return InteractionResult.FAIL;
        }

        // Only consume the soul AFTER successful spawn.
        if (player == null || !player.getAbilities().instabuild) {
            soulStack.shrink(1);
        }

        if (player != null) {
            player.displayClientMessage(
                    Component.literal("Unit revived."),
                    true
            );
        }

        return InteractionResult.CONSUME;
    }
}