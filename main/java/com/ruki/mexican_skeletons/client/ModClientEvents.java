package com.ruki.mexican_skeletons.client;

import com.ruki.mexican_skeletons.RukisNecromancyMod;
import com.ruki.mexican_skeletons.registry.ModEntities;
import com.ruki.mexican_skeletons.registry.ModMenuTypes;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = RukisNecromancyMod.MODID,
        value = Dist.CLIENT
)
public final class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                ModEntities.SMART_ZOMBIE.get(),
                ZombieRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerScreens(
            RegisterMenuScreensEvent event
    ) {
        event.register(
                ModMenuTypes.SMART_ZOMBIE.get(),
                SmartZombieScreen::new
        );
    }

    private ModClientEvents() {}
}
