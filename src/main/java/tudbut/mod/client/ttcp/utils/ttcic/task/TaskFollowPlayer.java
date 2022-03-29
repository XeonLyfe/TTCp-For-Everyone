package tudbut.mod.client.ttcp.utils.ttcic.task;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.movement.ElytraFlight;
import tudbut.mod.client.ttcp.utils.FlightBot;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.ttcic.task.Task;
import tudbut.obj.Atomic;

public class TaskFollowPlayer
extends Task {
    public int entityID;

    public TaskFollowPlayer() {
    }

    public TaskFollowPlayer(Entity entity) {
        this.entityID = entity.getEntityId();
    }

    @Override
    public void run() {
        ElytraFlight flight = TTCp.getModule(ElytraFlight.class);
        if (!flight.enabled) {
            flight.toggle();
        }
    }

    public Entity getEntity() {
        Entity[] entities = Utils.getEntities(Entity.class, e -> e.getEntityId() == this.entityID);
        if (entities.length > 0) {
            return entities[0];
        }
        return null;
    }

    @Override
    public void onTick() {
        Entity e = this.getEntity();
        if (e != null) {
            FlightBot.activate(new Atomic<Vec3d>(e.getPositionVector().addVector(0.0, 3.0, 0.0)));
        }
    }

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.entityID = stream.readInt();
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        stream.writeInt(this.entityID);
    }
}
