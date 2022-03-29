package tudbut.mod.client.ttcp.utils.ttcic.packet.client;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;

public class CPacketRequestValues
extends Packet {
    boolean group = false;
    boolean task = false;

    @Override
    public void read(TypedInputStream stream) throws IOException {
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
    }
}
