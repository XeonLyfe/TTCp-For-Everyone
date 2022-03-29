package tudbut.mod.client.ttcp.ttcic.tracking;

import java.lang.reflect.Field;

public class TrackedEntity {
    public double posX;
    public double posY;
    public double posZ;
    public double lastPosX;
    public double lastPosY;
    public double lastPosZ;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TrackedEntity that = (TrackedEntity)o;
        Field[] fields = TrackedEntity.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            try {
                if (fields[i].get(this).equals(fields[i].get(that))) continue;
                return false;
            }
            catch (IllegalAccessException e) {
                return false;
            }
        }
        return true;
    }
}
