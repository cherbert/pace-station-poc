package com.tinblue.spacemod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Places a ship entity on the clicked block, boat-style. */
public class ShipItem extends Item {
    public ShipItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }

        BlockPos pos = context.getBlockPos().offset(context.getSide());
        ShipEntity ship = new ShipEntity(SpaceMod.SHIP, world);
        float yaw = context.getPlayer() != null ? context.getPlayer().getYaw() : 0.0f;
        ship.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, yaw, 0.0f);
        serverWorld.spawnEntity(ship);

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().creativeMode) {
            context.getStack().decrement(1);
        }
        return ActionResult.CONSUME;
    }
}
