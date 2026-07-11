package com.tinblue.spacemod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A small zero-gravity shuttle. The riding player is the "controlling passenger",
 * so (exactly like a boat) the pilot's client simulates the movement and the
 * position is synced back to the server via vanilla vehicle-move packets.
 *
 * Controls (set each client tick from SpaceModClient):
 *   look around - aim, W - thrust toward where you look, S - retro thrust,
 *   Space - vertical thruster, Sprint key - boost, Sneak - dismount.
 */
public class ShipEntity extends Entity {
    private static final double THRUST = 0.06;
    private static final double BOOST_THRUST = 0.12;
    private static final double DRAG = 0.92;
    private static final double MAX_SPEED = 1.0;
    private static final double MAX_BOOST_SPEED = 2.0;

    // Only meaningful on the pilot's client, which is the side that simulates movement.
    private boolean thrustForward;
    private boolean thrustBack;
    private boolean thrustUp;
    private boolean boosting;

    public ShipEntity(EntityType<? extends ShipEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public void setInputs(boolean forward, boolean back, boolean up, boolean boost) {
        this.thrustForward = forward;
        this.thrustBack = back;
        this.thrustUp = up;
        this.boosting = boost;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isLogicalSideForUpdatingMovement()) {
            return; // another side is simulating us; position arrives over the network
        }

        Vec3d velocity = this.getVelocity();
        if (this.getControllingPassenger() instanceof PlayerEntity pilot) {
            this.setYaw(pilot.getYaw());
            this.setPitch(MathHelper.clamp(pilot.getPitch(), -60.0f, 60.0f));

            Vec3d look = pilot.getRotationVector();
            double accel = this.boosting ? BOOST_THRUST : THRUST;
            if (this.thrustForward) {
                velocity = velocity.add(look.multiply(accel));
            }
            if (this.thrustBack) {
                velocity = velocity.subtract(look.multiply(accel * 0.5));
            }
            if (this.thrustUp) {
                velocity = velocity.add(0.0, accel * 0.8, 0.0);
            }
        }

        velocity = velocity.multiply(DRAG);
        if (velocity.lengthSquared() < 1.0e-6) {
            velocity = Vec3d.ZERO;
        }
        double max = this.boosting ? MAX_BOOST_SPEED : MAX_SPEED;
        if (velocity.lengthSquared() > max * max) {
            velocity = velocity.normalize().multiply(max);
        }
        this.setVelocity(velocity);
        this.move(MovementType.SELF, velocity);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        }
        if (!this.getWorld().isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getWorld().isClient || this.isRemoved()) {
            return false;
        }
        boolean creative = source.getAttacker() instanceof PlayerEntity player && player.getAbilities().creativeMode;
        if (!creative) {
            this.dropItem(SpaceMod.SHIP_ITEM);
        }
        this.discard();
        return true;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof PlayerEntity player ? player : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().isEmpty();
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(SpaceMod.SHIP_ITEM);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}
