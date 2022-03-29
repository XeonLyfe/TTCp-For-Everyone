package tudbut.mod.client.ttcp.utils.ttcic.packet.ds;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;

public class PacketPlayer
extends Packet {
    public String name;
    public UUID uuid;
    public int dim;
    public int id;
    public boolean server;

    public PacketPlayer() {
    }

    public PacketPlayer(GameProfile profile, EntityPlayer player, boolean server) {
        this.name = profile.getName();
        this.uuid = profile.getId();
        this.dim = player.field_71093_bK;
        this.id = ControlCenter.myID();
        this.server = server;
    }

    @Override
    public void read(TypedInputStream stream) throws IOException {
        this.name = stream.readString();
        this.uuid = UUID.fromString(stream.readString());
        this.dim = stream.readInt();
        this.id = stream.readInt();
        this.server = stream.readBoolean();
    }

    @Override
    public void write(TypedOutputStream stream) throws IOException {
        stream.writeString(this.name);
        stream.writeString(this.uuid.toString());
        stream.writeInt(this.dim);
        stream.writeInt(this.id);
        stream.writeBoolean(this.server);
    }
}
