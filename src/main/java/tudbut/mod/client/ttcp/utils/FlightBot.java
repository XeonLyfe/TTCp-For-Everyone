package tudbut.mod.client.ttcp.utils;

import java.util.Date;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.obj.Atomic;

public class FlightBot {
    private static Atomic<Vec3d> destination;
    private static EntityPlayerSP player;
    private static volatile boolean lock;
    private static boolean flying;
    private static boolean active;
    private static long tookOff;
    private static double speed;

    public static boolean isActive() {
        return active;
    }

    public static boolean isFlying() {
        player = TTCp.player;
        return destination != null && destination.get() != null && flying && player.func_174791_d().distanceTo(destination.get()) > 1.0;
    }

    private FlightBot() {
    }

    public static void activate(Atomic<Vec3d> destination, double speed) {
        while (lock) {
        }
        flying = true;
        active = true;
        FlightBot.speed = speed;
        FlightBot.destination = destination;
    }

    public static void activate(Atomic<Vec3d> destination) {
        FlightBot.activate(destination, 1.0);
    }

    public static void deactivate() {
        active = false;
        speed = 1.0;
    }

    public static void updateDestination(Atomic<Vec3d> destination) {
        while (lock) {
        }
        FlightBot.destination = destination;
    }

    public static void setSpeed(double speed) {
        FlightBot.speed = speed;
    }

    private static void takeOff() {
        player = TTCp.player;
        if (FlightBot.player.field_70122_E) {
            if (!player.func_184613_cA()) {
                tookOff = 0L;
                player.func_70664_aZ();
            }
        } else if ((double)FlightBot.player.field_70143_R > 0.1) {
            FlightBot.player.field_70125_A = -20.0f;
            FlightBot.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(TTCp.player.field_70177_z, -20.0f, false));
            FlightBot.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)player, CPacketEntityAction.Action.START_FALL_FLYING));
            tookOff = new Date().getTime();
        }
    }

    public static synchronized boolean tickBot() {
        if (!active) {
            return false;
        }
        player = TTCp.player;
        if (!player.func_184613_cA()) {
            if (new Date().getTime() - tookOff > 100L) {
                FlightBot.takeOff();
            }
            return false;
        }
        if (new Date().getTime() - tookOff < 300L && tookOff != 0L) {
            return true;
        }
        if (destination.get() == null) {
            return false;
        }
        lock = true;
        Vec3d dest = destination.get();
        double dx = dest.x - FlightBot.player.field_70165_t;
        double dy = dest.y - FlightBot.player.field_70163_u;
        double dz = dest.z - FlightBot.player.field_70161_v;
        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d < 1.0) {
            d = 1.0;
            flying = false;
        } else {
            flying = true;
        }
        double x = dx / (d /= speed);
        double y = dy / d;
        double z = dz / d;
        FlightBot.player.field_70159_w = x;
        FlightBot.player.field_70181_x = y;
        FlightBot.player.field_70179_y = z;
        lock = false;
        return true;
    }

    static {
        player = TTCp.player;
        lock = false;
        flying = false;
        active = false;
        tookOff = 0L;
        speed = 1.0;
    }
}
