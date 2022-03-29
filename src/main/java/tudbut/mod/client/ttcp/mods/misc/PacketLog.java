package tudbut.mod.client.ttcp.mods.misc;

import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import java.io.IOException;
import java.util.Date;
import net.minecraft.network.Packet;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.parsing.TCN;
import tudbut.tools.ObjectSerializerTCN;
import tudbut.tools.ThreadPool;

@Misc
public class PacketLog
extends Module {
    TCN map = TCN.getEmpty();
    ThreadPool pool = new ThreadPool(5, "PacketLogger thread", false);

    @Override
    public boolean onPacket(Packet<?> packet) {
        this.pool.run(() -> this.savePacket(packet));
        return false;
    }

    @Override
    public void onDisable() {
        this.pool.run(() -> {
            try {
                new FileRW("packetlog.tcnmap").setContent(Tools.mapToString(this.map.toMap()));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void savePacket(Packet<?> packet) {
        try {
            this.map.set(new Date().getTime() + " " + packet.getClass().getName(), new ObjectSerializerTCN(packet).convertAll().done(new Object[0]));
        }
        catch (Exception e) {
            ChatUtils.print("PacketLog couldn't serialize a packet! Packet was: " + packet.getClass().getName());
        }
    }
}
