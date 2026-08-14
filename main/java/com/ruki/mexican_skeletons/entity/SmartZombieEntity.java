package com.ruki.mexican_skeletons.entity;

import com.ruki.mexican_skeletons.menu.SmartZombieMenu;
import com.ruki.mexican_skeletons.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SmartZombieEntity extends Zombie implements MenuProvider {

    // ===== UNIT INVENTORY LAYOUT =====

    public static final int SLOT_HEAD = 0;
    public static final int SLOT_CHEST = 1;
    public static final int SLOT_LEGS = 2;
    public static final int SLOT_FEET = 3;
    public static final int SLOT_LEFT_HAND = 4;
    public static final int SLOT_RIGHT_HAND = 5;

    public static final int SLOT_HOTBAR_START = 6;
    public static final int SLOT_HOTBAR_END = 14;

    public static final int SLOT_STORAGE_START = 15;
    public static final int SLOT_STORAGE_END = 41;

    public static final int INVENTORY_SIZE = 42;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!SmartZombieEntity.this.level().isClientSide) {
                SmartZombieEntity.this.syncEquipmentSlot(slot);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= SLOT_HEAD && slot <= SLOT_RIGHT_HAND) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }
    };

    // Temporary ownership rule for testing:
    // the first player to sneak-right-click/open the unit claims it.
    @Nullable
    private UUID ownerUUID;

    // Guards against accidental double creation during death handling.
    private boolean soulCreated = false;

    public SmartZombieEntity(
            EntityType<? extends Zombie> entityType,
            Level level
    ) {
        super(entityType, level);

        // The unit's equipment is preserved inside the soul item instead,
        // so vanilla should not separately drop synced hand/armor equipment.
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
        this.setDropChance(EquipmentSlot.LEGS, 0.0F);
        this.setDropChance(EquipmentSlot.FEET, 0.0F);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getHotbarItem(int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex > 8) {
            throw new IllegalArgumentException("Unit hotbar index must be 0-8");
        }

        return inventory.getStackInSlot(SLOT_HOTBAR_START + hotbarIndex);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    // ===== EQUIPMENT SYNC =====

    private void syncEquipmentSlot(int inventorySlot) {
        EquipmentSlot equipmentSlot = switch (inventorySlot) {
            case SLOT_HEAD -> EquipmentSlot.HEAD;
            case SLOT_CHEST -> EquipmentSlot.CHEST;
            case SLOT_LEGS -> EquipmentSlot.LEGS;
            case SLOT_FEET -> EquipmentSlot.FEET;
            case SLOT_LEFT_HAND -> EquipmentSlot.OFFHAND;
            case SLOT_RIGHT_HAND -> EquipmentSlot.MAINHAND;
            default -> null;
        };

        if (equipmentSlot != null) {
            this.setItemSlot(
                    equipmentSlot,
                    inventory.getStackInSlot(inventorySlot)
            );
        }
    }

    private void syncAllEquipment() {
        for (int slot = SLOT_HEAD; slot <= SLOT_RIGHT_HAND; slot++) {
            syncEquipmentSlot(slot);
        }
    }

    // ===== INTERACTION / MENU =====

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide
                    && player instanceof ServerPlayer serverPlayer) {

                // TEMPORARY ownership acquisition for development testing.
                // Replace this later with the real acquisition/summoning system.
                if (this.ownerUUID == null) {
                    this.ownerUUID = serverPlayer.getUUID();
                }

                serverPlayer.openMenu(
                        this,
                        buf -> buf.writeVarInt(this.getId())
                );
            }

            return InteractionResult.sidedSuccess(
                    this.level().isClientSide
            );
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Unit Inventory");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new SmartZombieMenu(
                containerId,
                playerInventory,
                this
        );
    }

    // ===== SAVE / LOAD =====

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.put(
                "UnitInventory",
                inventory.serializeNBT(this.level().registryAccess())
        );

        if (ownerUUID != null) {
            tag.putUUID("UnitOwner", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("UnitInventory", Tag.TAG_COMPOUND)) {
            inventory.deserializeNBT(
                    this.level().registryAccess(),
                    tag.getCompound("UnitInventory")
            );
        }

        if (tag.hasUUID("UnitOwner")) {
            ownerUUID = tag.getUUID("UnitOwner");
        } else {
            ownerUUID = null;
        }

        syncAllEquipment();
    }

    // ===== SOUL / DEATH SNAPSHOT =====

    private ItemStack createUnitSoul() {
        // Save the entity's full persistent state.
        // This automatically includes UnitInventory and UnitOwner because
        // they are written by addAdditionalSaveData above.
        CompoundTag entityData = new CompoundTag();
        this.saveWithoutId(entityData);

        CompoundTag soulData = new CompoundTag();

        var entityKey =
                BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());

        if (entityKey != null) {
            soulData.putString(
                    "UnitEntityType",
                    entityKey.toString()
            );
        }

        soulData.put(
                "UnitEntityData",
                entityData
        );

        soulData.putInt(
                "SoulFormatVersion",
                1
        );

        ItemStack soul =
                new ItemStack(ModItems.UNIT_SOUL.get());

        soul.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(soulData)
        );

        String unitName =
                this.hasCustomName()
                        ? this.getCustomName().getString()
                        : "Smart Zombie";

        soul.set(
                DataComponents.CUSTOM_NAME,
                Component.literal("Soul of " + unitName)
        );

        return soul;
    }

    private void deliverSoul(
            ServerLevel level,
            ItemStack soul
    ) {
        ServerPlayer owner = null;

        if (ownerUUID != null) {
            owner = level
                    .getServer()
                    .getPlayerList()
                    .getPlayer(ownerUUID);
        }

        if (owner != null) {
            // Preferred behavior:
            // place the dead unit directly into its owner's inventory.
            boolean inserted =
                    owner.getInventory().add(soul);

            if (!inserted) {
                // Inventory full -> drop beside the owner instead.
                owner.drop(soul, false);
            }
        } else {
            // No owner / owner offline -> leave the soul at the corpse.
            this.spawnAtLocation(soul);
        }
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level,
            DamageSource damageSource,
            boolean recentlyHit
    ) {
        if (!soulCreated) {
            soulCreated = true;

            ItemStack soul = createUnitSoul();
            deliverSoul(level, soul);
        }

        super.dropCustomDeathLoot(
                level,
                damageSource,
                recentlyHit
        );
    }

    public void restoreFromSoul(
            CompoundTag savedData,
            double x,
            double y,
            double z
    ) {
        // Never reuse the dead Minecraft entity UUID.
        //
        // The newly-created entity already has a fresh UUID.
        // Later we'll add our own permanent Unit ID which survives resurrection.
        CompoundTag restoredData = savedData.copy();

        restoredData.remove("UUID");

        // Don't restore corpse position / velocity.
        restoredData.remove("Pos");
        restoredData.remove("Motion");
        restoredData.remove("Rotation");

        // Load everything else:
        // - inventory
        // - equipment
        // - custom name
        // - owner
        // - attributes
        // - future saved unit state
        this.load(restoredData);

        // ===== RECONSTRUCT LIVING STATE =====

        this.setPos(x, y, z);

        this.setDeltaMovement(Vec3.ZERO);

        this.setHealth(this.getMaxHealth());

        this.deathTime = 0;
        this.hurtTime = 0;

        this.fallDistance = 0.0F;

        this.clearFire();

        this.setPose(Pose.STANDING);

        // Make absolutely sure our six equipment slots are
        // reflected on the actual entity after loading.
        this.syncAllEquipment();
    }

    // ===== BASIC UNIT BEHAVIOR =====

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return true;
    }
}
