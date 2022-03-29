package tudbut.mod.client.ttcp.ttcic.task;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.Module;

public abstract class Task
extends Module {
    public abstract void serialize(TypedOutputStream var1) throws IOException;

    public abstract void deserialize(TypedInputStream var1) throws IOException;

    public abstract void onReceive();

    public void write(TypedOutputStream stream) throws IOException {
        stream.writeString(this.getClass().getName());
        this.serialize(stream);
    }

    public static Task read(TypedInputStream stream) throws IOException {
        try {
            return (Task)Class.forName(stream.readString()).newInstance();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
