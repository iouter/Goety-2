package com.Polarice3.Goety.data;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends IntrinsicHolderTagsProvider<EntityType<?>> {

    public ModEntityTypeTagsProvider(PackOutput p_256095_, CompletableFuture<HolderLookup.Provider> p_256572_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_256095_, Registries.ENTITY_TYPE, p_256572_, (p_256665_) -> {
            return p_256665_.builtInRegistryHolder().key();
        }, Goety.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_255894_) {
        this.tag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(ModEntityType.WRAITH.get(),
                ModEntityType.WRAITH_SERVANT.get(),
                ModEntityType.BORDER_WRAITH.get(),
                ModEntityType.BORDER_WRAITH_SERVANT.get(),
                ModEntityType.FROZEN_ZOMBIE_SERVANT.get(),
                ModEntityType.STRAY_SERVANT.get(),
                ModEntityType.ICY_SPIDER_SERVANT.get(),
                ModEntityType.POLAR_BEAR_SERVANT.get(),
                ModEntityType.BOUND_ICEOLOGER.get(),
                ModEntityType.CAIRN_NECROMANCER.get(),
                ModEntityType.WIGHT.get(),
                ModEntityType.BONE_LORD.get(),
                ModEntityType.SKULL_LORD.get(),
                ModEntityType.HAUNTED_ARMOR.get(),
                ModEntityType.HAUNTED_ARMOR_SERVANT.get(),
                ModEntityType.ICE_GOLEM.get(),
                ModEntityType.CRYOLOGER.get(),
                ModEntityType.GLACIAL_WALL.get());
        this.tag(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add(ModEntityType.BLAZE_SERVANT.get(),
                ModEntityType.WILDFIRE.get(),
                ModEntityType.INFERNO.get(),
                ModEntityType.MAGMA_CUBE_SERVANT.get());
        this.tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(ModEntityType.WRAITH.get(),
                ModEntityType.WRAITH_SERVANT.get(),
                ModEntityType.BORDER_WRAITH.get(),
                ModEntityType.BORDER_WRAITH_SERVANT.get(),
                ModEntityType.ICY_SPIDER_SERVANT.get(),
                ModEntityType.BOUND_ICEOLOGER.get(),
                ModEntityType.ICE_GOLEM.get(),
                ModEntityType.CRYOLOGER.get(),
                ModEntityType.GLACIAL_WALL.get());
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(
                ModEntityType.INFERNO.get(),
                ModEntityType.MAGMA_CUBE_SERVANT.get(),
                ModEntityType.CARRION_MAGGOT.get(),
                ModEntityType.STORM_CASTER.get());
        this.tag(EntityTypeTags.RAIDERS).add(ModEntityType.ARMORED_RAVAGER.get(),
                ModEntityType.WARLOCK.get(),
                ModEntityType.MAVERICK.get(),
                ModEntityType.HERETIC.get(),
                ModEntityType.CRONE.get(),
                ModEntityType.SORCERER.get(),
                ModEntityType.ENVIOKER.get(),
                ModEntityType.INQUILLAGER.get(),
                ModEntityType.CONQUILLAGER.get(),
                ModEntityType.PIKER.get(),
                ModEntityType.RIPPER.get(),
                ModEntityType.TRAMPLER.get(),
                ModEntityType.CRUSHER.get(),
                ModEntityType.STORM_CASTER.get(),
                ModEntityType.CRYOLOGER.get(),
                ModEntityType.PREACHER.get(),
                ModEntityType.MINISTER.get(),
                ModEntityType.HOSTILE_REDSTONE_GOLEM.get(),
                ModEntityType.HOSTILE_REDSTONE_MONSTROSITY.get(),
                ModEntityType.APOSTLE.get(),
                ModEntityType.VIZIER.get());
        this.tag(EntityTypeTags.ARROWS).add(ModEntityType.GHOST_ARROW.get(),
                ModEntityType.RAIN_ARROW.get(),
                ModEntityType.DEATH_ARROW.get());
        this.tag(EntityTypeTags.IMPACT_PROJECTILES).add(ModEntityType.SOUL_BOLT.get(),
                ModEntityType.STEAM_MISSILE.get(),
                ModEntityType.WITHER_BOLT.get(),
                ModEntityType.HELL_BOLT.get(),
                ModEntityType.HELL_BLAST.get(),
                ModEntityType.NECRO_BOLT.get(),
                ModEntityType.MAGIC_BOLT.get(),
                ModEntityType.SHIELD_DEBRIS.get(),
                ModEntityType.ILL_BOMB.get(),
                ModEntityType.SCATTER_BOMB.get(),
                ModEntityType.SCYTHE.get(),
                ModEntityType.SWORD.get(),
                ModEntityType.ICE_SPIKE.get(),
                ModEntityType.MOD_FIREBALL.get(),
                ModEntityType.LAVABALL.get(),
                ModEntityType.HAUNTED_SKULL_SHOT.get(),
                ModEntityType.MOD_WITHER_SKULL.get());
        this.tag(EntityTypeTags.FROG_FOOD).add(ModEntityType.SLIME_SERVANT.get(),
                ModEntityType.MAGMA_CUBE_SERVANT.get(),
                ModEntityType.CRYPT_SLIME_SERVANT.get(),
                ModEntityType.CRYPT_SLIME.get());

        //Based on https://github.com/ochotonida/artifacts/blob/1.20.x/data/src/main/java/artifacts/data/providers/EntityTypeTags.java for max compat
        this.tag(ModTags.EntityTypes.CREEPERS).add(EntityType.CREEPER);

        List<String> creepers = Arrays.asList(
                "jungle_creeper",
                "bamboo_creeper",
                "desert_creeper",
                "badlands_creeper",
                "hills_creeper",
                "savannah_creeper",
                "mushroom_creeper",
                "swamp_creeper",
                "dripstone_creeper",
                "cave_creeper",
                "dark_oak_creeper",
                "spruce_creeper",
                "beach_creeper",
                "snowy_creeper"
        );
        for (String creeper : creepers) {
            this.tag(ModTags.EntityTypes.CREEPERS).addOptional(new ResourceLocation("creeperoverhaul", creeper));
        }

        this.tag(ModTags.EntityTypes.ENDERMEN).add(EntityType.ENDERMAN);

        List<String> endermen = Arrays.asList(
                "badlands_enderman",
                "cave_enderman",
                "crimson_forest_enderman",
                "dark_oak_enderman",
                "desert_enderman",
                "end_enderman",
                "end_islands_enderman",
                "flower_fields_enderman",
                "ice_spikes_enderman",
                "mushroom_fields_enderman",
                "nether_wastes_enderman",
                "coral_enderman",
                "savanna_enderman",
                "snowy_enderman",
                "soulsand_valley_enderman",
                "swamp_enderman",
                "warped_forest_enderman",
                "windswept_hills_enderman",
                "pet_enderman",
                "hammerhead_pet_enderman",
                "axolotl_pet_enderman"
        );
        for (String enderman : endermen) {
            this.tag(ModTags.EntityTypes.ENDERMEN).addOptional(new ResourceLocation("endermanoverhaul", enderman));
        }
    }
}
