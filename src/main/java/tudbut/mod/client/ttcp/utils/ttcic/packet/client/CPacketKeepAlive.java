package tudbut.mod.client.ttcp.utils.ttcic.packet.client;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.utils.ttcic.packet.ds.PacketPlayer;

public class CPacketKeepAlive
extends Packet {
    public int playerID;

    public CPacketKeepAlive() {
    }

    public CPacketKeepAlive(PacketPlayer[] group) {
        this.playerID = ControlCenter.myID();
    }

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.playerID = stream.readInt();
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        stream.writeInt(this.playerID);
    }
}
