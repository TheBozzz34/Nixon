package xyz.necrozma.event.impl.motion;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xyz.necrozma.event.Event;
import xyz.necrozma.util.InstanceAccess;
import xyz.necrozma.util.MoveUtil;

@Getter
@Setter
@AllArgsConstructor
public final class StrafeEvent extends Event implements InstanceAccess {
    private float forward, strafe, friction;

    public void setSpeedPartialStrafe(float friction, final float strafe) {
        final float remainder = 1 - strafe;

        if (forward != 0 && this.strafe != 0) {
            friction *= 0.91;
        }

        if (mc.thePlayer.onGround) {
            setSpeed(friction);
        } else {
            mc.thePlayer.motionX *= strafe;
            mc.thePlayer.motionZ *= strafe;
            setFriction(friction * remainder);
        }
    }

    public void setSpeed(final float speed, final double motionMultiplier) {
        setFriction(getForward() != 0 && getStrafe() != 0 ? speed * 0.99F : speed);
        mc.thePlayer.motionX *= motionMultiplier;
        mc.thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(final float speed) {
        setFriction(getForward() != 0 && getStrafe() != 0 ? speed * 0.99F : speed);
        MoveUtil.stop();
    }
}