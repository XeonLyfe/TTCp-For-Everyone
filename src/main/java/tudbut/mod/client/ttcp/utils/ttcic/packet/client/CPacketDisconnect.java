package tudbut.mod.client.ttcp.utils.ttcic.packet.client;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;

public class CPacketDisconnect
extends Packet {
    public int id = ControlCenter.myID();

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.id = stream.readInt();
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        stream.writeInt(this.id);
    }
}
