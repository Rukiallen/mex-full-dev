package com.ruki.mexican_skeletons.registry;

import com.ruki.mexican_skeletons.RukisNecromancyMod;
import com.ruki.mexican_skeletons.menu.SmartZombieMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    RukisNecromancyMod.MODID
            );

    public static final Supplier<MenuType<SmartZombieMenu>> SMART_ZOMBIE =
            MENUS.register(
                    "smart_zombie",
                    () -> IMenuTypeExtension.create(SmartZombieMenu::new)
            );

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    private ModMenuTypes() {}
}
