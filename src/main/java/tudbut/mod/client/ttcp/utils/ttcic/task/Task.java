package tudbut.mod.client.ttcp.utils.ttcic.task;

import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;

public abstract class Task
extends Packet {
    public abstract void run();

    public abstract void onTick();
}
