package tudbut.mod.client.ttcp.utils.ttcic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import tudbut.io.FileBus;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.ttcic.packet.Packet;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketConnect;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketDisconnect;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketKeepAlive;
import tudbut.mod.client.ttcp.utils.ttcic.packet.client.CPacketRequestTask;
import tudbut.mod.client.ttcp.utils.ttcic.packet.ds.PacketPlayer;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketEnd;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketGroup;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketID;
import tudbut.mod.client.ttcp.utils.ttcic.packet.server.SPacketServer;
import tudbut.mod.client.ttcp.utils.ttcic.task.Task;
import tudbut.mod.client.ttcp.utils.ttcic.task.TaskIdle;
import tudbut.mod.client.ttcp.utils.ttcic.task.TaskKillPlayer;
import tudbut.tools.Lock;

public class ControlCenter {
    private static PacketPlayer[] group = new PacketPlayer[0];
    public static final FileBus bus;
    private static PacketPlayer me;
    private static Task task;
    private static int id;
    private static PacketPlayer main;
    private static boolean hasTask;
    private static boolean connected;
    private static boolean running;
    public static boolean stop;
    private static boolean isServer;
    static Lock tickWaitLock;
    private static final HandlerClient clientHandler;
    private static final HandlerServer serverHandler;

    public static boolean isRunning() {
        return running;
    }

    public static boolean isServer() {
        return isServer;
    }

    public static void setGroup(PacketPlayer[] players) {
        if (players != null) {
            group = players;
        }
    }

    public static PacketPlayer[] getGroup() {
        return group;
    }

    public static FileBus getBus() {
        return bus;
    }

    public static void setTask(Task task) {
        ControlCenter.task = task;
        task.run();
        hasTask = !(task instanceof TaskIdle);
    }

    public static int getIndexByID(int id) {
        for (int i = 0; i < group.length; ++i) {
            if (ControlCenter.group[i].id != id) continue;
            return i;
        }
        return 0;
    }

    public static void syncTick() {
        if (isServer) {
            // empty if block
        }
    }

    public static void onTick() {
        tickWaitLock.waitHere();
        PacketPlayer me = new PacketPlayer(Minecraft.getMinecraft().getSession().getProfile(), (EntityPlayer)Minecraft.getMinecraft().player, isServer);
        me.id = id;
        ControlCenter.me = me;
        task.onTick();
        if (main == null || isServer) {
            main = me;
        }
    }

    public static void sNotifyGroupChange() {
        SPacketGroup group = new SPacketGroup(ControlCenter.getGroup());
        try {
            group.serialize(bus);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sSetTask(Task task) throws IOException {
        task.serialize(bus);
    }

    public static void onAttacked(EntityPlayer by) throws IOException {
        if (!hasTask && connected) {
            TaskKillPlayer task = new TaskKillPlayer();
            task.entityID = by.func_145782_y();
            if (isServer) {
                task.serialize(bus);
            } else {
                CPacketRequestTask taskPacket = new CPacketRequestTask(task);
                taskPacket.serialize(bus);
            }
        }
    }

    public static void client() {
        running = true;
        new Thread(() -> {
            try {
                ControlCenter.clientRunner();
            }
            catch (IOException e) {
                ChatUtils.print("§c[§aTTCIC§c] §4§lClient crashed! Did someone delete the bus?");
            }
        }).start();
    }

    public static void server() {
        running = true;
        new Thread(() -> {
            try {
                ControlCenter.serverRunner();
            }
            catch (IOException e) {
                ChatUtils.print("§c[§aTTCIC§c] §4§lServer crashed! Did someone delete the bus?");
            }
        }).start();
    }

    private static void clientRunner() throws IOException {
        new CPacketConnect().serialize(bus);
        ControlCenter.clientHandler.hasID = false;
        isServer = false;
        group = new PacketPlayer[0];
        while (!stop) {
            try {
                Object packet = Packet.deserialize(bus.getTypedReader());
                if (packet instanceof SPacketServer) {
                    ChatUtils.print("§c[§aTTCIC§c] §b§lServer changed! Reconnecting...");
                    group = new PacketPlayer[0];
                    connected = false;
                    ControlCenter.client();
                    break;
                }
                clientHandler.handle((Packet)packet);
            }
            catch (Exception e) {
                ChatUtils.print("§c[§aTTCIC§c] §c§lError processing a packet. Please report this.");
            }
        }
        stop = false;
        running = false;
    }

    private static void serverRunner() throws IOException {
        new SPacketServer().serialize(bus);
        ControlCenter.serverHandler.nextID = 0;
        id = ControlCenter.serverHandler.nextID++;
        group = new PacketPlayer[]{ControlCenter.me()};
        connected = true;
        isServer = true;
        while (!stop) {
            try {
                Object packet = Packet.deserialize(bus.getTypedReader());
                if (packet instanceof SPacketServer) {
                    ChatUtils.print("§c[§aTTCIC§c] §b§lA different instance is now server!");
                    isServer = false;
                    group = new PacketPlayer[0];
                    connected = false;
                    ControlCenter.client();
                    break;
                }
                serverHandler.handle((Packet)packet);
            }
            catch (Exception e) {
                ChatUtils.print("§c[§aTTCIC§c] §c§lError processing a packet. Please report this.");
            }
        }
        stop = false;
        running = false;
    }

    public static PacketPlayer me() {
        return me;
    }

    public static int myID() {
        return id;
    }

    public static void shutdown() {
        if (ControlCenter.isServer()) {
            try {
                new SPacketEnd().serialize(bus);
                isServer = false;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                new CPacketDisconnect().serialize(bus);
                group = new PacketPlayer[0];
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        group = new PacketPlayer[0];
        ControlCenter.me.id = 0;
        connected = false;
        running = false;
    }

    static PacketPlayer[] access$302(PacketPlayer[] x0) {
        group = x0;
        return x0;
    }

    static {
        id = 0;
        hasTask = false;
        connected = false;
        running = false;
        stop = false;
        FileBus theBus = null;
        try {
            theBus = new FileBus(System.getProperty("user.home") + "/ttc.bus");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bus = theBus;
        tickWaitLock = new Lock();
        clientHandler = new HandlerClient();
        serverHandler = new HandlerServer();
    }

    private static class HandlerClient {
        boolean hasID = false;

        private HandlerClient() {
        }

        void handle(Packet packet) throws IOException {
            if (packet instanceof SPacketID) {
                this.handleID((SPacketID)packet);
            }
            if (packet instanceof PacketPlayer && ((PacketPlayer)packet).server) {
                this.handlePlayer((PacketPlayer)packet);
            }
            if (packet instanceof SPacketGroup) {
                this.handleGroup((SPacketGroup)packet);
            }
            if (packet instanceof Task) {
                ControlCenter.setTask(task);
            }
        }

        void handleID(SPacketID packetID) throws IOException {
            connected = true;
            if (!this.hasID) {
                ChatUtils.print("§c[§aTTCIC§c] §a§lConnected to server. Exchanging name and group...");
                id = packetID.id;
                this.hasID = true;
                me.serialize(bus);
            }
        }

        void handlePlayer(PacketPlayer packetPlayer) {
            main = packetPlayer;
        }

        void handleGroup(SPacketGroup group) {
            ControlCenter.access$302(group.players);
            ChatUtils.print("§c[§aTTCIC§c] §a§lConnected and initialized successfully.");
        }
    }

    private static class HandlerServer {
        int nextID = 0;

        private HandlerServer() {
        }

        void handle(Packet packet) throws IOException {
            if (packet instanceof CPacketKeepAlive) {
                this.handleKeepAlive((CPacketKeepAlive)packet);
            }
            if (packet instanceof CPacketDisconnect) {
                this.handleDisconnect((CPacketDisconnect)packet);
            }
            if (packet instanceof CPacketConnect) {
                this.handleConnect((CPacketConnect)packet);
            }
            if (packet instanceof CPacketRequestTask && !hasTask) {
                ControlCenter.sSetTask(((CPacketRequestTask)packet).theTask);
            }
        }

        void handleKeepAlive(CPacketKeepAlive packet) {
        }

        void handleDisconnect(CPacketDisconnect packetDisconnect) {
            ChatUtils.print("§c[§aTTCIC§c] §c§lA client disconnected. Name was " + group[ControlCenter.getIndexByID((int)packetDisconnect.id)].name);
            ArrayList<PacketPlayer> groupArray = new ArrayList<PacketPlayer>(Arrays.asList(group));
            groupArray.removeIf(packetPlayer -> packetPlayer.id == packetDisconnect.id);
            ControlCenter.access$302(groupArray.toArray(new PacketPlayer[0]));
        }

        void handleConnect(CPacketConnect packetConnect) throws IOException {
            ChatUtils.print("§c[§aTTCIC§c] §a§lA client connected. Asking for name...");
            SPacketID id = new SPacketID();
            id.id = this.nextID++;
            id.serialize(bus);
            me.serialize(bus);
        }

        void handlePlayer(PacketPlayer packetPlayer) {
            ArrayList<PacketPlayer> groupArray = new ArrayList<PacketPlayer>(Arrays.asList(group));
            if (!groupArray.contains(packetPlayer)) {
                groupArray.add(packetPlayer);
                ControlCenter.access$302(groupArray.toArray(new PacketPlayer[0]));
                ControlCenter.sNotifyGroupChange();
                ChatUtils.print("§c[§aTTCIC§c] §a§lClient connected successfully.");
            } else {
                group[ControlCenter.getIndexByID((int)packetPlayer.id)] = packetPlayer;
            }
        }
    }
}
