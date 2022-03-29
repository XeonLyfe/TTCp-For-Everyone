package tudbut.mod.client.ttcp.ttcic.packet.server;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.ttcic.packet.Packet;

public class SPacketTask
implements Packet {
    @Override
    public void serialize(TypedOutputStream stream) throws IOException {
    }

    @Override
    public void deserialize(TypedInputStream stream) throws IOException {
    }

    @Override
    public void onReceive() {
    }
}
