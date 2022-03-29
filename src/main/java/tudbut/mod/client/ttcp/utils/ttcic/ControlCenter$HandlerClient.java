package tudbut.mod.client.ttcp.utils.ttcic;

import java.io.IOException;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.utils.ttcic.packet.ds.PacketPlayer;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketGroup;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketID;
import tudbut.mod.client.ttcp.utils.ttcic.task.Task;

class ControlCenter$HandlerClient {
    boolean hasID = false;

    private ControlCenter$HandlerClient() {
    }

    void handle(Packet packet) throws IOException {
        if (packet instanceof SPacketID) {
            this.handleID((SPacketID)packet);
        }
        if (packet instanceof PacketPlayer && ((PacketPlayer)packet).server) {
            this.handlePlayer((PacketPlayer)packet);
        }
        if (packet instanceof SPacketGroup) {
            this.handleGroup((SPacketGroup)packet);
        }
        if (packet instanceof Task) {
            ControlCenter.setTask(task);
        }
    }

    void handleID(SPacketID packetID) throws IOException {
        connected = true;
        if (!this.hasID) {
            ChatUtils.print("§c[§aTTCIC§c] §a§lConnected to server. Exchanging name and group...");
            id = packetID.id;
            this.hasID = true;
            me.serialize(bus);
        }
    }

    void handlePlayer(PacketPlayer packetPlayer) {
        main = packetPlayer;
    }

    void handleGroup(SPacketGroup group) {
        ControlCenter.access$302(group.players);
        ChatUtils.print("§c[§aTTCIC§c] §a§lConnected and initialized successfully.");
    }
}
