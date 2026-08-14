package com.ruki.mexican_skeletons.registry;

import com.ruki.mexican_skeletons.RukisNecromancyMod;
import com.ruki.mexican_skeletons.entity.SmartZombieEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    BuiltInRegistries.ENTITY_TYPE,
                    RukisNecromancyMod.MODID
            );

    public static final Supplier<EntityType<SmartZombieEntity>> SMART_ZOMBIE =
            ENTITY_TYPES.register(
                    "smart_zombie",
                    () -> EntityType.Builder
                            .of(SmartZombieEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("smart_zombie")
            );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    private ModEntities() {}
}