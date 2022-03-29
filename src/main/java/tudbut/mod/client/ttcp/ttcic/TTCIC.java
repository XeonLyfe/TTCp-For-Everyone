package tudbut.mod.client.ttcp.ttcic;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import tudbut.io.FileBus;
import tudbut.mod.client.ttcp.ttcic.tracking.EntityTracker;
import tudbut.mod.client.ttcp.ttcic.tracking.TrackedEntity;
import tudbut.mod.client.ttcp.utils.Module;

public class TTCIC
extends Module {
    public static ArrayList<TrackedEntity> trackedEntities = new ArrayList();
    public static FileBus bus;
    static ArrayList<EntityTracker> trackers;
    int tracker = 0;

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEveryTick() {
        if (this.tracker++ % 20 == 0) {
            TTCIC.updateEntities();
        }
        TTCIC.sendEntities();
    }

    private static void updateEntities() {
        int j;
        boolean b;
        int i;
        for (i = 0; i < TTCIC.mc.world.field_72996_f.size(); ++i) {
            Entity entity = (Entity)TTCIC.mc.world.field_72996_f.get(i);
            b = false;
            for (j = 0; j < trackers.size(); ++j) {
                if (TTCIC.trackers.get((int)j).entity.getEntityId() != entity.getEntityId()) continue;
                b = true;
                break;
            }
            if (b) continue;
            trackers.add(new EntityTracker(entity));
        }
        for (i = 0; i < trackers.size(); ++i) {
            EntityTracker entityTracker = trackers.get(i);
            b = false;
            for (j = 0; j < TTCIC.mc.world.field_72996_f.size(); ++j) {
                if (((Entity)TTCIC.mc.world.field_72996_f.get(j)).getEntityId() != entityTracker.entity.getEntityId()) continue;
                b = true;
                break;
            }
            if (b) continue;
            trackers.remove(entityTracker);
        }
    }

    private static void sendEntities() {
        for (int i = 0; i < trackers.size(); ++i) {
            trackers.get(i).update();
        }
    }

    static {
        trackers = new ArrayList();
    }
}
