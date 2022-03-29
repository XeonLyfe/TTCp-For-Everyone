package tudbut.mod.client.ttcp.utils.ttcic.task;

import java.io.IOException;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.FlightBot;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.task.Task;

public class TaskIdle
extends Task {
    @Override
    public void run() {
    }

    @Override
    public void onTick() {
        if (!ControlCenter.isServer()) {
            FlightBot.deactivate();
        }
    }

    @Override
    public void read(TypedInputStream stream) throws IOException {
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
    }
}
