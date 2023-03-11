package com.Polarice3.Goety.common.items;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.entities.ModEntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSpawnEggs {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Goety.MOD_ID);

    public static void init(){
        ModSpawnEggs.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ModSpawnEggItem> APOSTLE_SPAWN_EGG = ITEMS.register("apostle_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.APOSTLE, 0x080808, 0xf5da2a, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> VIZIER_SPAWN_EGG = ITEMS.register("vizier_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.VIZIER, 0x1e1c1a, 0x440a67, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> IRK_SPAWN_EGG = ITEMS.register("irk_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.IRK, 8032420, 8032420, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> ZOMBIE_MINION_SPAWN_EGG = ITEMS.register("zombie_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.ZOMBIE_SERVANT, 0x192927, 0x737885, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> HUSK_MINION_SPAWN_EGG = ITEMS.register("husk_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.HUSK_SERVANT, 0x322921, 0x64492a, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> DROWNED_MINION_SPAWN_EGG = ITEMS.register("drowned_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.DROWNED_SERVANT, 0x182d37, 0x2f8209, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> SKELETON_MINION_SPAWN_EGG = ITEMS.register("skeleton_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.SKELETON_SERVANT, 0x1f1f1f, 0x6e6473, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> STRAY_MINION_SPAWN_EGG = ITEMS.register("stray_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.STRAY_SERVANT, 0x495959, 0xb3d4e3, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> WRAITH_MINION_SPAWN_EGG = ITEMS.register("wraith_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.WRAITH_SERVANT, 0x0e0d36, 0x2586d9, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> ZPIGLIN_SERVANT_SPAWN_EGG = ITEMS.register("zpiglin_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.ZPIGLIN_SERVANT, 0x594036, 0xf5da2a, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> ZPIGLIN_BRUTE_SERVANT_SPAWN_EGG = ITEMS.register("zpiglin_brute_servant_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.ZPIGLIN_BRUTE_SERVANT, 0x1c1c1c, 0xf5da2a, new Item.Properties().tab(Goety.TAB)));

    public static final RegistryObject<ModSpawnEggItem> WRAITH_SPAWN_EGG = ITEMS.register("wraith_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.WRAITH, 0x16215c, 0x82d8f8, new Item.Properties().tab(Goety.TAB)));

}
