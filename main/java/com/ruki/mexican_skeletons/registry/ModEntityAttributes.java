package com.ruki.mexican_skeletons.registry;

import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class ModEntityAttributes {

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.SMART_ZOMBIE.get(),
                Zombie.createAttributes().build()
        );
    }

    private ModEntityAttributes() {}
}