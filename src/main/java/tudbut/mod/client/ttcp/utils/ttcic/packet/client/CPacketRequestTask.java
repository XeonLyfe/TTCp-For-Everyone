package tudbut.mod.client.ttcp.utils.ttcic.packet.client;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.utils.ttcic.task.Task;

public class CPacketRequestTask
extends Packet {
    public Task theTask;

    public CPacketRequestTask() {
    }

    public CPacketRequestTask(Task task) {
        this.theTask = task;
    }

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.theTask = (Task)Packet.deserialize(stream);
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        this.theTask.serialize(stream);
    }
}
