package tudbut.mod.client.ttcp.utils.ttcic.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import tudbut.io.FileBus;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;

public abstract class Packet {
    public abstract void read(TypedInputStream var1) throws IOException;

    public abstract void write(TypedOutputStream var1) throws IOException;

    public void serialize(FileBus bus) throws IOException {
        bus.startWrite();
        TypedOutputStream stream = bus.getTypedWriter();
        this.serialize(stream);
        bus.stopWrite();
    }

    public void serialize(TypedOutputStream stream) throws IOException {
        stream.writeString(this.getClass().getName());
        this.write(stream);
    }

    public static <T extends Packet> T deserialize(TypedInputStream stream) {
        try {
            Class<?> clazz = Class.forName(stream.readString());
            Packet packet = (Packet)clazz.newInstance();
            packet.read(stream);
            return (T)packet;
        }
        catch (Exception e) {
            System.out.println("Couldn't deserialize a packet!");
            return null;
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Packet) {
            Packet packet = (Packet)o;
            ByteArrayOutputStream me = new ByteArrayOutputStream();
            ByteArrayOutputStream other = new ByteArrayOutputStream();
            try {
                this.serialize(new TypedOutputStream(me));
            }
            catch (IOException iOException) {
                // empty catch block
            }
            try {
                packet.serialize(new TypedOutputStream(other));
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return Arrays.equals(me.toByteArray(), other.toByteArray());
        }
        return false;
    }
}
