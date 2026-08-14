package com.ruki.mexican_skeletons.registry;

import com.ruki.mexican_skeletons.RukisNecromancyMod;
import com.ruki.mexican_skeletons.item.UnitSoulItem;

import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(
                    RukisNecromancyMod.MODID
            );

    public static final DeferredItem<UnitSoulItem> UNIT_SOUL =
            ITEMS.register(
                    "unit_soul",
                    () -> new UnitSoulItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private ModItems() {}
}