package com.tinblue.spacemod;

import com.tinblue.spacemod.world.SpaceChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SpaceMod implements ModInitializer {
    public static final String MOD_ID = "spacemod";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    /** The custom dimension that holds the station, the asteroids and the planet. */
    public static final RegistryKey<World> SPACE_WORLD = RegistryKey.of(RegistryKeys.WORLD, id("space"));

    /** Where new players appear, inside the station's main room. */
    public static final BlockPos STATION_SPAWN = new BlockPos(0, 101, -4);

    /** Where the ship sits on the docking pad. */
    public static final double DOCK_X = 14.5, DOCK_Y = 100.2, DOCK_Z = 0.5;

    public static final Block GRAVITY_GENERATOR = new GravityGeneratorBlock(
            AbstractBlock.Settings.create()
                    .strength(2.0f, 6.0f)
                    .luminance(state -> 15)
                    .nonOpaque());

    public static final Item GRAVITY_GENERATOR_ITEM = new BlockItem(GRAVITY_GENERATOR, new Item.Settings());

    public static final Item SHIP_ITEM = new ShipItem(new Item.Settings().maxCount(1));

    public static final EntityType<ShipEntity> SHIP = EntityType.Builder
            .create(ShipEntity::new, SpawnGroup.MISC)
            .dimensions(2.5f, 1.1f)
            .maxTrackingRange(10)
            .build();

    private static final String INIT_TAG = "spacemod_init";

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, id("gravity_generator"), GRAVITY_GENERATOR);
        Registry.register(Registries.ITEM, id("gravity_generator"), GRAVITY_GENERATOR_ITEM);
        Registry.register(Registries.ITEM, id("ship"), SHIP_ITEM);
        Registry.register(Registries.ENTITY_TYPE, id("ship"), SHIP);
        Registry.register(Registries.CHUNK_GENERATOR, id("space"), SpaceChunkGenerator.CODEC);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(GRAVITY_GENERATOR_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(SHIP_ITEM));

        // First time a player ever joins, move them to the space station and make sure a ship is docked.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (player.getCommandTags().contains(INIT_TAG)) {
                return;
            }
            player.addCommandTag(INIT_TAG);

            ServerWorld space = server.getWorld(SPACE_WORLD);
            if (space == null) {
                return;
            }

            player.teleport(space,
                    STATION_SPAWN.getX() + 0.5, STATION_SPAWN.getY(), STATION_SPAWN.getZ() + 0.5,
                    0.0f, 10.0f);
            player.setSpawnPoint(SPACE_WORLD, STATION_SPAWN, 0.0f, true, false);
            player.giveItemStack(new ItemStack(SHIP_ITEM)); // spare ship, just in case

            Box dockArea = new Box(DOCK_X - 8, DOCK_Y - 4, DOCK_Z - 8, DOCK_X + 8, DOCK_Y + 6, DOCK_Z + 8);
            if (space.getEntitiesByType(SHIP, dockArea, entity -> true).isEmpty()) {
                ShipEntity ship = new ShipEntity(SHIP, space);
                ship.refreshPositionAndAngles(DOCK_X, DOCK_Y, DOCK_Z, -90.0f, 0.0f); // nose pointing at the planet
                space.spawnEntity(ship);
            }
        });
    }
}
