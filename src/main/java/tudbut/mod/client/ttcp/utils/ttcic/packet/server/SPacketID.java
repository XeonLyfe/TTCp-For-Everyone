package tudbut.mod.client.ttcp.utils.ttcic.packet.server;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;

public class SPacketID
extends Packet {
    public int id;

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.id = stream.readInt();
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        stream.writeInt(this.id);
    }
}
