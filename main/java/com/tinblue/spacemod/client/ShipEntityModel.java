package com.tinblue.spacemod.client;

import com.tinblue.spacemod.ShipEntity;
import com.tinblue.spacemod.SpaceMod;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * A chunky little shuttle: hull, raised cockpit, nose cone, two wings,
 * twin engines and landing skids. Nose points to -Z in model space.
 */
public class ShipEntityModel extends EntityModel<ShipEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(SpaceMod.id("ship"), "main");

    private final ModelPart root;

    public ShipEntityModel(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        root.addChild("hull", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-8.0f, -16.0f, -14.0f, 16.0f, 8.0f, 28.0f), ModelTransform.NONE);
        root.addChild("cockpit", ModelPartBuilder.create().uv(88, 0)
                .cuboid(-5.0f, -21.0f, -12.0f, 10.0f, 5.0f, 8.0f), ModelTransform.NONE);
        root.addChild("nose", ModelPartBuilder.create().uv(0, 36)
                .cuboid(-4.0f, -14.0f, -22.0f, 8.0f, 5.0f, 8.0f), ModelTransform.NONE);
        root.addChild("wing_right", ModelPartBuilder.create().uv(32, 36)
                .cuboid(8.0f, -12.0f, -2.0f, 12.0f, 2.0f, 12.0f), ModelTransform.NONE);
        root.addChild("wing_left", ModelPartBuilder.create().uv(32, 36)
                .cuboid(-20.0f, -12.0f, -2.0f, 12.0f, 2.0f, 12.0f), ModelTransform.NONE);
        root.addChild("engine_right", ModelPartBuilder.create().uv(80, 36)
                .cuboid(2.0f, -15.0f, 14.0f, 5.0f, 5.0f, 6.0f), ModelTransform.NONE);
        root.addChild("engine_left", ModelPartBuilder.create().uv(80, 36)
                .cuboid(-7.0f, -15.0f, 14.0f, 5.0f, 5.0f, 6.0f), ModelTransform.NONE);
        root.addChild("skid_right", ModelPartBuilder.create().uv(0, 50)
                .cuboid(5.0f, -6.0f, -8.0f, 3.0f, 6.0f, 20.0f), ModelTransform.NONE);
        root.addChild("skid_left", ModelPartBuilder.create().uv(0, 50)
                .cuboid(-8.0f, -6.0f, -8.0f, 3.0f, 6.0f, 20.0f), ModelTransform.NONE);

        return TexturedModelData.of(data, 128, 128);
    }

    @Override
    public void setAngles(ShipEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.root.render(matrices, vertices, light, overlay, color);
    }
}
