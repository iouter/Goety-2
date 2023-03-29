package com.Polarice3.Goety.common.items;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.SpellConfig;
import com.Polarice3.Goety.common.entities.vehicle.ModBoat;
import com.Polarice3.Goety.common.items.curios.*;
import com.Polarice3.Goety.common.items.equipment.DarkScytheItem;
import com.Polarice3.Goety.common.items.equipment.DeathScytheItem;
import com.Polarice3.Goety.common.items.magic.*;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ModMathHelper;
import net.minecraft.world.item.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Goety.MOD_ID);

    public static void init(){
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Item> TOTEM_OF_SOULS = ITEMS.register("totem_of_souls", TotemOfSouls::new);

    //Basic
    public static final RegistryObject<Item> SPENT_TOTEM = ITEMS.register("spent_totem", ItemBase::new);
    public static final RegistryObject<Item> CURSED_INGOT = ITEMS.register("cursed_ingot", ItemBase::new);
    public static final RegistryObject<Item> ECTOPLASM = ITEMS.register("ectoplasm", ItemBase::new);
    public static final RegistryObject<Item> DARK_FABRIC = ITEMS.register("dark_fabric", ItemBase::new);
    public static final RegistryObject<Item> MAGIC_FABRIC = ITEMS.register("magic_fabric", ItemBase::new);
    public static final RegistryObject<Item> OCCULT_FABRIC = ITEMS.register("occult_fabric", ItemBase::new);
    public static final RegistryObject<Item> SPIRIT_FABRIC = ITEMS.register("spirit_fabric", ItemBase::new);
    public static final RegistryObject<Item> SAVAGE_TOOTH = ITEMS.register("savage_tooth", ItemBase::new);
    public static final RegistryObject<Item> SOUL_RUBY = ITEMS.register("soul_ruby", ItemBase::new);
    public static final RegistryObject<Item> EMPTY_FOCUS = ITEMS.register("empty_focus", ItemBase::new);
    public static final RegistryObject<Item> ANIMATION_CORE = ITEMS.register("animation_core", ItemBase::new);
    public static final RegistryObject<Item> HUNGER_CORE = ITEMS.register("hunger_core", ItemBase::new);
    public static final RegistryObject<Item> WIND_CORE = ITEMS.register("wind_core", ItemBase::new);

    public static final RegistryObject<Item> DARK_SCROLL = ITEMS.register("dark_scroll", DarkScrollItem::new);
    public static final RegistryObject<Item> MAGIC_EMERALD = ITEMS.register("magic_emerald", () -> new SimpleFoiledItem(new Item.Properties().tab(Goety.TAB)));
    public static final RegistryObject<Item> SOUL_EMERALD = ITEMS.register("soul_emerald", () -> new SimpleFoiledItem(new Item.Properties().tab(Goety.TAB)));
    public static final RegistryObject<Item> UNHOLY_BLOOD = ITEMS.register("unholy_blood", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE).tab(Goety.TAB)));
    public static final RegistryObject<Item> SOUL_TRANSFER = ITEMS.register("soul_transfer", SoulTransferItem::new);
    public static final RegistryObject<Item> FLAME_CAPTURE = ITEMS.register("flame_capture", FlameCaptureItem::new);
    public static final RegistryObject<Item> UNDEATH_POTION = ITEMS.register("undeath_potion", UndeathPotionItem::new);

    public static final RegistryObject<Item> HAUNTED_BOAT = ITEMS.register("haunted_boat", () -> new ModBoatItem(false, ModBoat.Type.HAUNTED, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));
    public static final RegistryObject<Item> HAUNTED_CHEST_BOAT = ITEMS.register("haunted_chest_boat", () -> new ModBoatItem(true, ModBoat.Type.HAUNTED, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));

    //Curios
    public static final RegistryObject<Item> FOCUS_BAG = ITEMS.register("focus_bag", FocusBag::new);
    public static final RegistryObject<Item> RING_OF_WANT = ITEMS.register("ring_of_want", RingItem::new);
    public static final RegistryObject<Item> DARK_HAT = ITEMS.register("dark_hat", MagicHatItem::new);
    public static final RegistryObject<Item> NECRO_CROWN = ITEMS.register("necro_crown", MagicHatItem::new);
    public static final RegistryObject<Item> WITCH_HAT = ITEMS.register("witch_hat", SingleStackItem::new);
    public static final RegistryObject<Item> DARK_ROBE = ITEMS.register("dark_robe", MagicRobeItem::new);
    public static final RegistryObject<Item> NECRO_CAPE = ITEMS.register("necro_cape", SingleStackItem::new);
    public static final RegistryObject<Item> ILLUSION_ROBE = ITEMS.register("illusion_robe", SingleStackItem::new);
    public static final RegistryObject<Item> WITCH_ROBE = ITEMS.register("witch_robe", WitchRobeItem::new);
    public static final RegistryObject<Item> GRAVE_GLOVE = ITEMS.register("grave_glove", GloveItem::new);

    //Focus
    public static final RegistryObject<Item> VEXING_FOCUS = ITEMS.register("vexing_focus", () -> new MagicFocus(SpellConfig.VexCost.get()));
    public static final RegistryObject<Item> BITING_FOCUS = ITEMS.register("biting_focus", () -> new MagicFocus(SpellConfig.FangCost.get()));
    public static final RegistryObject<Item> FEAST_FOCUS = ITEMS.register("feast_focus", () -> new MagicFocus(SpellConfig.FeastCost.get()));
    public static final RegistryObject<Item> ICEOLOGY_FOCUS = ITEMS.register("iceology_focus", () -> new MagicFocus(SpellConfig.IceChunkCost.get()));
    public static final RegistryObject<Item> ILLUSION_FOCUS = ITEMS.register("illusion_focus", () -> new MagicFocus(SpellConfig.IllusionCost.get()));
    public static final RegistryObject<Item> SOUL_BOLT_FOCUS = ITEMS.register("soul_bolt_focus", () -> new MagicFocus(SpellConfig.SoulBoltCost.get()));
    public static final RegistryObject<Item> LIGHTNING_FOCUS = ITEMS.register("lightning_focus", () -> new MagicFocus(SpellConfig.LightningCost.get()));
    public static final RegistryObject<Item> SONIC_BOOM_FOCUS = ITEMS.register("sonic_boom_focus", () -> new MagicFocus(SpellConfig.SonicBoomCost.get()));
    public static final RegistryObject<Item> LAUNCH_FOCUS = ITEMS.register("launch_focus", () -> new MagicFocus(SpellConfig.LaunchCost.get()));
    public static final RegistryObject<Item> SOUL_LIGHT_FOCUS = ITEMS.register("soul_light_focus", () -> new MagicFocus(SpellConfig.SoulLightCost.get()));
    public static final RegistryObject<Item> GLOW_LIGHT_FOCUS = ITEMS.register("glow_light_focus", () -> new MagicFocus(SpellConfig.GlowLightCost.get()));
    public static final RegistryObject<Item> ROTTING_FOCUS = ITEMS.register("rotting_focus", () -> new MagicFocus(SpellConfig.ZombieCost.get()));
    public static final RegistryObject<Item> OSSEOUS_FOCUS = ITEMS.register("osseous_focus", () -> new MagicFocus(SpellConfig.SkeletonCost.get()));
    public static final RegistryObject<Item> SPOOKY_FOCUS = ITEMS.register("spooky_focus", () -> new MagicFocus(SpellConfig.WraithCost.get()));
    public static final RegistryObject<Item> SKULL_FOCUS = ITEMS.register("skull_focus", () -> new MagicFocus(SpellConfig.HauntedSkullCost.get()));

    //Tools & Weapons
    public static final RegistryObject<Item> DARK_WAND = ITEMS.register("dark_wand", DarkWand::new);
    public static final RegistryObject<Item> NECRO_STAFF = ITEMS.register("necro_staff", DarkStaff::new);
    public static final RegistryObject<Item> DARK_SCYTHE = ITEMS.register("dark_scythe", DarkScytheItem::new);
    public static final RegistryObject<Item> DEATH_SCYTHE = ITEMS.register("death_scythe", DeathScytheItem::new);

    //Discs
    public static final RegistryObject<Item> MUSIC_DISC_VIZIER = ITEMS.register("music_disc_vizier", () -> new RecordItem(14, ModSounds.VIZIER_THEME, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.RARE), (int) ModMathHelper.ticksToMinutes(1.33F)));
    public static final RegistryObject<Item> MUSIC_DISC_APOSTLE = ITEMS.register("music_disc_apostle", () -> new RecordItem(15, ModSounds.APOSTLE_THEME, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.RARE),(int) ModMathHelper.ticksToMinutes(2.41F)));

    //Dummies
    public static final RegistryObject<Item> PEDESTAL_DUMMY = ITEMS.register("pedestal_dummy",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<DummyItem> JEI_DUMMY_NONE = ITEMS.register(
            "jei_dummy/none", () -> new DummyItem(new Item.Properties()));
    public static final RegistryObject<DummyItem> JEI_DUMMY_REQUIRE_SACRIFICE = ITEMS.register(
            "jei_dummy/sacrifice", () -> new DummyItem(new Item.Properties()));
}
