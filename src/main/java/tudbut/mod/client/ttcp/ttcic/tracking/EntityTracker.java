package tudbut.mod.client.ttcp.ttcic.tracking;

import net.minecraft.entity.Entity;
import tudbut.mod.client.ttcp.ttcic.TTCIC;
import tudbut.mod.client.ttcp.ttcic.packet.dual.DPacketEntity;
import tudbut.mod.client.ttcp.ttcic.tracking.TrackedEntity;

public class EntityTracker {
    public Entity entity;
    public TrackedEntity trackedEntity;

    public EntityTracker(Entity entity) {
        this.entity = entity;
    }

    public void update() {
        TrackedEntity lastTrackedEntity = this.trackedEntity;
        this.trackedEntity = new TrackedEntity();
        this.trackedEntity.posX = this.entity.posX;
        this.trackedEntity.posY = this.entity.posY;
        this.trackedEntity.posZ = this.entity.posZ;
        this.trackedEntity.lastPosX = this.entity.lastTickPosX;
        this.trackedEntity.lastPosY = this.entity.lastTickPosY;
        this.trackedEntity.lastPosZ = this.entity.lastTickPosZ;
        if (!this.trackedEntity.equals(lastTrackedEntity)) {
            DPacketEntity packet = new DPacketEntity();
            packet.entity = this.trackedEntity;
            packet.write(TTCIC.bus);
        }
    }
}
