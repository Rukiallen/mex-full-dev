package com.ruki.mexican_skeletons;

import com.ruki.mexican_skeletons.registry.ModEntities;
import com.ruki.mexican_skeletons.registry.ModEntityAttributes;
import com.ruki.mexican_skeletons.registry.ModItems;
import com.ruki.mexican_skeletons.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(RukisNecromancyMod.MODID)
public final class RukisNecromancyMod {

    public static final String MODID = "mexican_skeletons";

    public RukisNecromancyMod(
            IEventBus modEventBus,
            ModContainer modContainer
    ) {
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        modEventBus.addListener(
                ModEntityAttributes::registerAttributes
        );
    }
}
