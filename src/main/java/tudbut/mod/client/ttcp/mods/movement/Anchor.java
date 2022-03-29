package tudbut.mod.client.ttcp.mods.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.obj.Save;

@Movement
public class Anchor
extends Module {
    @Save
    boolean x;
    @Save
    boolean y;
    @Save
    boolean z;
    double px;
    double py;
    double pz;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("X", this, "x"));
        this.subComponents.add(Setting.createBoolean("Y", this, "y"));
        this.subComponents.add(Setting.createBoolean("Z", this, "z"));
    }

    @Override
    public void onEnable() {
        EntityPlayerSP player = TTCp.player;
        if (player != null) {
            this.px = player.field_70165_t;
            this.py = player.field_70163_u;
            this.pz = player.field_70161_v;
        }
    }

    @Override
    public void onSubTick() {
        EntityPlayerSP player = TTCp.player;
        if (this.x) {
            player.field_70159_w = 0.0;
            player.field_70165_t = this.px;
        }
        if (this.y) {
            player.field_70181_x = 0.0;
            player.field_70163_u = this.py;
        }
        if (this.z) {
            player.field_70179_y = 0.0;
            player.field_70161_v = this.pz;
        }
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        return packet instanceof CPacketConfirmTeleport;
    }
}
