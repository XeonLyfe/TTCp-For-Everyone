package tudbut.mod.client.ttcp.utils.ttcic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketConnect;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketDisconnect;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketKeepAlive;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketRequestTask;
import tudbut.mod.client.ttcp.utils.ttcic.packet.ds.PacketPlayer;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketID;

class ControlCenter$HandlerServer {
    int nextID = 0;

    private ControlCenter$HandlerServer() {
    }

    void handle(Packet packet) throws IOException {
        if (packet instanceof CPacketKeepAlive) {
            this.handleKeepAlive((CPacketKeepAlive)packet);
        }
        if (packet instanceof CPacketDisconnect) {
            this.handleDisconnect((CPacketDisconnect)packet);
        }
        if (packet instanceof CPacketConnect) {
            this.handleConnect((CPacketConnect)packet);
        }
        if (packet instanceof CPacketRequestTask && !hasTask) {
            ControlCenter.sSetTask(((CPacketRequestTask)packet).theTask);
        }
    }

    void handleKeepAlive(CPacketKeepAlive packet) {
    }

    void handleDisconnect(CPacketDisconnect packetDisconnect) {
        ChatUtils.print("§c[§aTTCIC§c] §c§lA client disconnected. Name was " + group[ControlCenter.getIndexByID((int)packetDisconnect.id)].name);
        ArrayList<PacketPlayer> groupArray = new ArrayList<PacketPlayer>(Arrays.asList(group));
        groupArray.removeIf(packetPlayer -> packetPlayer.id == packetDisconnect.id);
        ControlCenter.access$302(groupArray.toArray(new PacketPlayer[0]));
    }

    void handleConnect(CPacketConnect packetConnect) throws IOException {
        ChatUtils.print("§c[§aTTCIC§c] §a§lA client connected. Asking for name...");
        SPacketID id = new SPacketID();
        id.id = this.nextID++;
        id.serialize(bus);
        me.serialize(bus);
    }

    void handlePlayer(PacketPlayer packetPlayer) {
        ArrayList<PacketPlayer> groupArray = new ArrayList<PacketPlayer>(Arrays.asList(group));
        if (!groupArray.contains(packetPlayer)) {
            groupArray.add(packetPlayer);
            ControlCenter.access$302(groupArray.toArray(new PacketPlayer[0]));
            ControlCenter.sNotifyGroupChange();
            ChatUtils.print("§c[§aTTCIC§c] §a§lClient connected successfully.");
        } else {
            group[ControlCenter.getIndexByID((int)packetPlayer.id)] = packetPlayer;
        }
    }
}
