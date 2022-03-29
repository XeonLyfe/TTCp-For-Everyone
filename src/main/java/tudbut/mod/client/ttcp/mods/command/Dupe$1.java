package tudbut.mod.client.ttcp.mods.command;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

class Dupe$1
implements Packet<INetHandler> {
    Dupe$1() {
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBytes(new InputStream(){

            @Override
            public int read() throws IOException {
                return (int)(Math.random() * 255.0);
            }
        }, Integer.MAX_VALUE);
    }

    public void processPacket(INetHandler handler) {
    }
}
