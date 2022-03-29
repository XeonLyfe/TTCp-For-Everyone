package tudbut.mod.client.ttcp.mods.movement;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.obj.Save;

@Movement
public class BHop
extends Module {
    @Save
    Mode mode = Mode.JUMP;
    @Save
    boolean packetOnGround = false;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createEnum(Mode.class, "Mode", this, "mode"));
        this.subComponents.add(Setting.createBoolean("OnGround", this, "packetOnGround"));
    }

    @Override
    public void onTick() {
        if (this.player.field_70122_E && (this.player.movementInput.moveForward != 0.0f || this.player.movementInput.moveStrafe != 0.0f)) {
            if (this.mode == Mode.JUMP) {
                this.player.func_70664_aZ();
            }
            if (this.mode == Mode.MOTION) {
                this.player.field_70181_x = 0.425f;
            }
            if (this.mode == Mode.LOWHOP) {
                this.player.field_70181_x = 0.425f;
            }
            if (this.mode == Mode.PACKET) {
                this.player.connection.sendPacket((Packet)new CPacketPlayer.Position(this.player.field_70165_t, this.player.field_70163_u += 1.1, this.player.field_70161_v, this.packetOnGround));
                this.player.field_70122_E = false;
            }
            if (this.mode == Mode.PACKETJUMP) {
                this.player.connection.sendPacket((Packet)new CPacketPlayer.Position(this.player.field_70165_t, this.player.field_70163_u + 0.41999998688698, this.player.field_70161_v, this.packetOnGround));
                this.player.connection.sendPacket((Packet)new CPacketPlayer.Position(this.player.field_70165_t, this.player.field_70163_u + 0.7531999805211997, this.player.field_70161_v, this.packetOnGround));
                this.player.connection.sendPacket((Packet)new CPacketPlayer.Position(this.player.field_70165_t, this.player.field_70163_u + 1.00133597911214, this.player.field_70161_v, this.packetOnGround));
                this.player.connection.sendPacket((Packet)new CPacketPlayer.Position(this.player.field_70165_t, this.player.field_70163_u += 1.16610926093821, this.player.field_70161_v, this.packetOnGround));
                this.player.field_70122_E = false;
            }
        } else if (this.mode == Mode.LOWHOP) {
            this.player.field_70181_x = -0.1;
        }
    }

    public static enum Mode {
        PACKET,
        PACKETJUMP,
        MOTION,
        JUMP,
        LOWHOP;

    }
}
