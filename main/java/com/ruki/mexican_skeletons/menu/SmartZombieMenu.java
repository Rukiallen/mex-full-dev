package com.ruki.mexican_skeletons.menu;

import com.ruki.mexican_skeletons.entity.SmartZombieEntity;
import com.ruki.mexican_skeletons.registry.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SmartZombieMenu extends AbstractContainerMenu {

    public static final int UNIT_SLOT_COUNT = SmartZombieEntity.INVENTORY_SIZE; // 42
    public static final int PLAYER_MAIN_START = UNIT_SLOT_COUNT;               // 42
    public static final int PLAYER_MAIN_END = PLAYER_MAIN_START + 27;          // 69 exclusive
    public static final int PLAYER_HOTBAR_START = PLAYER_MAIN_END;             // 69
    public static final int TOTAL_SLOT_COUNT = PLAYER_HOTBAR_START + 9;        // 78

    // Temporary GUI coordinates. We'll replace these when the final texture is ready.
    public static final int EQUIPMENT_X = 10;
    public static final int EQUIPMENT_Y = 18;

    public static final int UNIT_GRID_X = 58;
    public static final int UNIT_HOTBAR_Y = 18;
    public static final int UNIT_STORAGE_Y = 42;

    public static final int PLAYER_X = 34;
    public static final int PLAYER_MAIN_Y = 140;
    public static final int PLAYER_HOTBAR_Y = 198;

    private final SmartZombieEntity zombie;

    // ===== SERVER CONSTRUCTOR =====

    public SmartZombieMenu(
            int containerId,
            Inventory playerInventory,
            SmartZombieEntity zombie
    ) {
        super(ModMenuTypes.SMART_ZOMBIE.get(), containerId);

        this.zombie = zombie;

        ItemStackHandler unitInventory = zombie.getInventory();

        // ---- 6 equipment slots: head/chest/legs/feet/left/right ----
        for (int i = 0; i < 6; i++) {
            this.addSlot(new SlotItemHandler(
                    unitInventory,
                    i,
                    EQUIPMENT_X,
                    EQUIPMENT_Y + (i * 18)
            ));
        }

        // ---- Active unit hotbar: inventory slots 6..14 ----
        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(
                    unitInventory,
                    SmartZombieEntity.SLOT_HOTBAR_START + col,
                    UNIT_GRID_X + (col * 18),
                    UNIT_HOTBAR_Y
            ));
        }

        // ---- Unit storage: inventory slots 15..41 (3x9) ----
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int unitSlot =
                        SmartZombieEntity.SLOT_STORAGE_START
                                + (row * 9)
                                + col;

                this.addSlot(new SlotItemHandler(
                        unitInventory,
                        unitSlot,
                        UNIT_GRID_X + (col * 18),
                        UNIT_STORAGE_Y + (row * 18)
                ));
            }
        }

        // ---- Player main inventory: 3x9 ----
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int playerSlot = 9 + (row * 9) + col;

                this.addSlot(new Slot(
                        playerInventory,
                        playerSlot,
                        PLAYER_X + (col * 18),
                        PLAYER_MAIN_Y + (row * 18)
                ));
            }
        }

        // ---- Player hotbar ----
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    PLAYER_X + (col * 18),
                    PLAYER_HOTBAR_Y
            ));
        }
    }

    // ===== CLIENT CONSTRUCTOR =====
    // Receives the zombie entity id written by ServerPlayer#openMenu.

    public SmartZombieMenu(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf extraData
    ) {
        this(
                containerId,
                playerInventory,
                findZombie(playerInventory, extraData.readVarInt())
        );
    }

    private static SmartZombieEntity findZombie(
            Inventory playerInventory,
            int entityId
    ) {
        Entity entity = playerInventory.player.level().getEntity(entityId);

        if (!(entity instanceof SmartZombieEntity zombie)) {
            throw new IllegalStateException(
                    "Could not find SmartZombieEntity with entity id " + entityId
            );
        }

        return zombie;
    }

    public SmartZombieEntity getZombie() {
        return zombie;
    }

    @Override
    public boolean stillValid(Player player) {
        return zombie.isAlive() && player.distanceToSqr(zombie) <= 64.0D;
    }

    // ===== SHIFT-CLICK TRANSFER =====

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = this.slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();

        if (index < UNIT_SLOT_COUNT) {
            // Unit -> player inventory + player hotbar
            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_MAIN_START,
                    TOTAL_SLOT_COUNT,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player -> unit active hotbar + storage.
            // Deliberately skip equipment slots 0..5 for automatic transfer.
            if (!this.moveItemStackTo(
                    sourceStack,
                    SmartZombieEntity.SLOT_HOTBAR_START,
                    UNIT_SLOT_COUNT,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return originalStack;
    }
}
