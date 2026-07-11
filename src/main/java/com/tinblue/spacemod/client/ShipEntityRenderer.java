package com.tinblue.spacemod.client;

import com.tinblue.spacemod.ShipEntity;
import com.tinblue.spacemod.SpaceMod;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ShipEntityRenderer extends EntityRenderer<ShipEntity> {
    private static final Identifier TEXTURE = Identifier.of(SpaceMod.MOD_ID, "textures/entity/ship.png");

    private final ShipEntityModel model;

    public ShipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new ShipEntityModel(context.getPart(ShipEntityModel.LAYER));
        this.shadowRadius = 1.2f;
    }

    @Override
    public void render(ShipEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        float pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));
        matrices.scale(-1.0f, -1.0f, 1.0f);
        this.model.render(matrices,
                vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE)),
                light, OverlayTexture.DEFAULT_UV, -1);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ShipEntity entity) {
        return TEXTURE;
    }
}
