package com.aurumkinetics.registry;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.entity.GoldCoatingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AurumKinetics.MOD_ID);

    public static final RegistryObject<EntityType<GoldCoatingEntity>> GOLD_COATING =
            ENTITIES.register("gold_coating", () -> EntityType.Builder
                    .<GoldCoatingEntity>of(GoldCoatingEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("gold_coating"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
