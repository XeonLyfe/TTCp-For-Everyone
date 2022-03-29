package tudbut.mod.client.ttcp.ttcic.packet;

import java.io.IOException;
import tudbut.io.FileBus;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;

public interface Packet {
    public void serialize(TypedOutputStream var1) throws IOException;

    public void deserialize(TypedInputStream var1) throws IOException;

    public void onReceive();

    default public void write(FileBus stream) {
        try {
            stream.startWrite();
            TypedOutputStream os = stream.getTypedWriter();
            os.writeString(this.getClass().getName());
            this.serialize(os);
            stream.stopWrite();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
