package com.tinblue.spacemod.client;

import com.tinblue.spacemod.ShipEntity;
import com.tinblue.spacemod.SpaceMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

public class SpaceModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ShipEntityModel.LAYER, ShipEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(SpaceMod.SHIP, ShipEntityRenderer::new);

        // Space sky: no clouds, near-black fog. The dimension is locked to midnight,
        // so the vanilla star field is always out.
        DimensionRenderingRegistry.registerDimensionEffects(SpaceMod.id("space"),
                new DimensionEffects(Float.NaN, false, DimensionEffects.SkyType.NORMAL, false, false) {
                    @Override
                    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
                        return color.multiply(0.15);
                    }

                    @Override
                    public boolean useThickFog(int camX, int camY) {
                        return false;
                    }
                });

        // Feed the pilot's keys to the ship they are riding.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.getVehicle() instanceof ShipEntity ship) {
                ship.setInputs(
                        client.options.forwardKey.isPressed(),
                        client.options.backKey.isPressed(),
                        client.options.jumpKey.isPressed(),
                        client.options.sprintKey.isPressed());
            }
        });
    }
}
