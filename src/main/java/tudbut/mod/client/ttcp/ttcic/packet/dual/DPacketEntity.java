package tudbut.mod.client.ttcp.ttcic.packet.dual;

import java.io.IOException;
import java.lang.reflect.Field;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.ttcic.tracking.TrackedEntity;

public class DPacketEntity
implements Packet {
    public TrackedEntity entity = new TrackedEntity();

    @Override
    public void serialize(TypedOutputStream stream) throws IOException {
        Field[] fields = TrackedEntity.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            try {
                if (!fields[i].isAccessible()) continue;
                Object o = fields[i].get(this.entity);
                if (o instanceof Double) {
                    stream.writeDouble((Double)o);
                }
                if (o instanceof Integer) {
                    stream.writeInt((Integer)o);
                }
                if (o instanceof String) {
                    stream.writeString((String)o);
                }
                if (o instanceof Float) {
                    stream.writeFloat(((Float)o).floatValue());
                }
                if (!(o instanceof Short)) continue;
                stream.writeFloat(((Short)o).shortValue());
                continue;
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deserialize(TypedInputStream stream) throws IOException {
        this.entity.posX = stream.readDouble();
        this.entity.posY = stream.readDouble();
        this.entity.posZ = stream.readDouble();
        this.entity.lastPosX = stream.readDouble();
        this.entity.lastPosY = stream.readDouble();
        this.entity.lastPosZ = stream.readDouble();
    }

    @Override
    public void onReceive() {
    }
}
