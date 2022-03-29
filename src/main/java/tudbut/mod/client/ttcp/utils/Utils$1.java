package tudbut.mod.client.ttcp.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

final class Utils$1
implements INetHandlerStatusClient {
    final AtomicBoolean val$done;
    final AtomicLong val$pingSentAt;
    final NetworkManager val$networkmanager;
    final long[] val$players;
    final ServerData val$finalServer;

    Utils$1(AtomicBoolean atomicBoolean, AtomicLong atomicLong, NetworkManager networkManager, long[] lArray, ServerData serverData) {
        this.val$done = atomicBoolean;
        this.val$pingSentAt = atomicLong;
        this.val$networkmanager = networkManager;
        this.val$players = lArray;
        this.val$finalServer = serverData;
    }

    public void func_147231_a(@Nullable ITextComponent reason) {
        this.val$done.set(true);
    }

    public void handleServerInfo(@Nullable SPacketServerInfo packetIn) {
        this.val$pingSentAt.set(System.currentTimeMillis());
        this.val$networkmanager.sendPacket((Packet)new CPacketPing(this.val$pingSentAt.get()));
        try {
            assert (packetIn != null);
            this.val$players[0] = packetIn.getResponse().getPlayers().getOnlinePlayerCount();
            this.val$players[1] = packetIn.getResponse().getPlayers().getMaxPlayers();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void handlePong(@Nullable SPacketPong packetIn) {
        long j = System.currentTimeMillis();
        this.val$finalServer.pingToServer = j - this.val$pingSentAt.get();
        this.val$networkmanager.closeChannel((ITextComponent)new TextComponentString("Finished"));
        this.val$done.set(true);
    }
}
