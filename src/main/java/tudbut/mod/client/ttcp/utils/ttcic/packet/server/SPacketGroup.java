package tudbut.mod.client.ttcp.utils.ttcic.packet.server;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.utils.ttcic.packet.ds.PacketPlayer;

public class SPacketGroup
extends Packet {
    public PacketPlayer[] players;

    public SPacketGroup() {
    }

    public SPacketGroup(PacketPlayer[] players) {
        this.players = players;
    }

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.players = new PacketPlayer[stream.readInt()];
        for (int i = 0; i < this.players.length; ++i) {
            this.players[i] = (PacketPlayer)Packet.deserialize(stream);
        }
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        stream.writeInt(this.players.length);
        for (int i = 0; i < this.players.length; ++i) {
            this.players[i].serialize(stream);
        }
    }
}
