package tudbut.mod.client.ttcp.mods.misc;

import de.tudbut.timer.AsyncTask;
import de.tudbut.type.Vector3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiPlayerSelect;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.FlightBot;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.TTCIC;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.net.ic.PBIC;
import tudbut.obj.Atomic;
import tudbut.obj.Save;
import tudbut.tools.Queue;

@Misc
public class AltControl
extends Module {
    private static AltControl instance;
    private int confirmationInstance;
    public int mode;
    @Save
    private boolean botMain;
    @Save
    private boolean useElytra;
    private boolean stopped;
    private final Atomic<Vec3d> commonTarget;
    private EntityPlayer commonTargetPlayer;
    private long lostTimer;
    public final Queue<PBIC.Packet> toSend;
    PBIC.Server server;
    PBIC.Client client;
    Alt main;
    ArrayList<Alt> alts;
    Map<PBIC.Connection, Alt> altsMap;

    public AltControl() {
        instance = this;
        this.confirmationInstance = 0;
        this.mode = -1;
        this.botMain = true;
        this.useElytra = true;
        this.stopped = true;
        this.commonTarget = new Atomic();
        this.commonTargetPlayer = null;
        this.lostTimer = 0L;
        this.toSend = new Queue();
        this.main = new Alt();
        this.alts = new ArrayList();
        this.altsMap = new HashMap<PBIC.Connection, Alt>();
    }

    public static AltControl getInstance() {
        return instance;
    }

    @Override
    public void init() {
        PlayerSelector.types.add(new PlayerSelector.Type(player -> this.onChat("kill " + player.getGameProfile().getName(), ("kill " + player.getGameProfile().getName()).split(" ")), "Set AltControl.Kill target"));
        PlayerSelector.types.add(new PlayerSelector.Type(player -> this.onChat("follow " + player.getGameProfile().getName(), ("follow " + player.getGameProfile().getName()).split(" ")), "Set AltControl.Follow target"));
    }

    public void triggerSelectKill() {
        TTCp.mc.displayGuiScreen((GuiScreen)new GuiPlayerSelect((EntityPlayer[])TTCp.world.playerEntities.stream().filter(player -> !player.getName().equals(TTCp.player.func_70005_c_())).toArray(EntityPlayer[]::new), player -> {
            if (this.server != null) {
                this.onChat("kill " + player.getName(), ("kill " + player.getName()).split(" "));
            }
            return true;
        }));
    }

    public void triggerSelectFollow() {
        TTCp.mc.displayGuiScreen((GuiScreen)new GuiPlayerSelect(TTCp.world.playerEntities.toArray(new EntityPlayer[0]), player -> {
            if (this.server != null) {
                this.onChat("follow " + player.getName(), ("follow " + player.getName()).split(" "));
            }
            return true;
        }));
    }

    public void triggerStop() {
        this.onChat("stop", "stop".split(" "));
    }

    @Override
    public void onConfigLoad() {
    }

    @Override
    public void updateBinds() {
        this.customKeyBinds.set("kill", new Module.KeyBind(null, this.toString() + "::triggerSelectKill", false));
        this.customKeyBinds.set("follow", new Module.KeyBind(null, this.toString() + "::triggerSelectFollow", false));
        this.customKeyBinds.set("stop", new Module.KeyBind(null, this.toString() + "::triggerStop", false));
        this.subComponents.clear();
        if (this.mode == -1) {
            this.subComponents.add(new Button("Main mode", it -> {
                if (this.mode != -1) {
                    return;
                }
                this.displayConfirmation = true;
                this.confirmationInstance = 0;
            }));
            this.subComponents.add(new Button("Alt mode", it -> {
                if (this.mode != -1) {
                    return;
                }
                this.displayConfirmation = true;
                this.confirmationInstance = 1;
            }));
        } else {
            this.subComponents.add(new Button("End connection", it -> this.onChat("end", "end".split(" "))));
            this.subComponents.add(new Button("List", it -> this.onChat("list", "list".split(" "))));
        }
        if (this.mode == 0) {
            this.subComponents.add(new Button("TPA alts here", it -> this.onChat("tpa", "tpa".split(" "))));
            this.subComponents.add(new Button("Stop alts", it -> this.onChat("stop", "stop".split(" "))));
            this.subComponents.add(new Button("Follow me", it -> this.onChat("follow", "follow".split(" "))));
            this.subComponents.add(new Button("Send client config", it -> this.onChat("send", "send".split(" "))));
            this.subComponents.add(new Button("Use elytra: " + this.useElytra, it -> {
                this.onChat("telytra", "telytra".split(" "));
                it.text = "Use elytra: " + this.useElytra;
            }));
            this.subComponents.add(new Button("Bot main: " + this.botMain, it -> {
                this.botMain = !this.botMain;
                it.text = "Bot main: " + this.botMain;
            }));
        }
        this.subComponents.add(new Button("Show GUIs", it -> {}){
            {
                this.subComponents.add(Setting.createKey("Kill", (Module.KeyBind)AltControl.this.customKeyBinds.get("kill")));
                this.subComponents.add(Setting.createKey("Follow", (Module.KeyBind)AltControl.this.customKeyBinds.get("follow")));
            }
        });
        this.subComponents.add(Setting.createKey("Stop", (Module.KeyBind)this.customKeyBinds.get("stop")));
    }

    public boolean isAlt(EntityPlayer player) {
        try {
            for (int i = 0; i < this.alts.size(); ++i) {
                if (!this.alts.get((int)i).uuid.equals(player.getGameProfile().getId())) continue;
                return true;
            }
            return player.getGameProfile().getId().equals(this.main.uuid);
        }
        catch (NullPointerException e) {
            for (int i = 0; i < this.alts.size(); ++i) {
                if (!this.alts.get((int)i).name.equals(player.getGameProfile().getName())) continue;
                return true;
            }
            return player.getGameProfile().getName().equals(this.main.name);
        }
    }

    @Override
    public void onConfirm(boolean result) {
        if (result) {
            switch (this.confirmationInstance) {
                case 0: {
                    this.onChat("server", "server".split(" "));
                    break;
                }
                case 1: {
                    this.onChat("client", "client".split(" "));
                }
            }
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onTick() {
        if (this.useElytra && !this.stopped && TTCp.isIngame()) {
            NetworkPlayerInfo[] players = Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
            if (this.main.uuid.equals(TTCp.player.func_110124_au())) {
                if (new Date().getTime() - this.lostTimer > 10000L) {
                    FlightBot.setSpeed(1.0);
                } else if (new Date().getTime() - this.lostTimer > 5000L) {
                    FlightBot.setSpeed(0.75);
                }
            }
            if (this.commonTargetPlayer != null && TTCp.world.getPlayerEntityByName(this.commonTargetPlayer.getName()) != null) {
                this.follow();
            } else if (new Date().getTime() - this.lostTimer > 5000L) {
                FlightBot.deactivate();
                this.commonTargetPlayer = null;
                this.commonTarget.set(null);
                if (!this.main.uuid.equals(TTCp.player.func_110124_au())) {
                    if (TTCp.world.getPlayerEntityByName(this.main.name) == null && new Date().getTime() - this.lostTimer > 5000L && Arrays.stream(players).anyMatch(player -> player.getGameProfile().getId().equals(this.main.uuid))) {
                        try {
                            this.sendPacket(TTCIC.PacketsCS.LOST, "");
                        }
                        catch (PBIC.PBICException.PBICWriteException e) {
                            e.printStackTrace();
                        }
                        this.lostTimer = new Date().getTime();
                    } else {
                        this.follow(this.main.name);
                    }
                }
            }
        }
    }

    public void onPacketSC(TTCIC.PacketSC packet) {
        if (this.client == null) {
            throw new RuntimeException();
        }
        try {
            ChatUtils.chatPrinterDebug().println("Received packet[" + (Object)((Object)packet.type()) + "]{" + packet.content() + "}");
            switch (packet.type()) {
                case INIT: {
                    this.main = new Alt();
                    this.sendPacket(TTCIC.PacketsCS.NAME, TTCp.mc.getSession().getProfile().getName());
                    break;
                }
                case NAME: {
                    this.main.name = packet.content();
                    ChatUtils.print("Connection to main " + this.main.name + " established!");
                    this.sendPacket(TTCIC.PacketsCS.UUID, TTCp.mc.getSession().getProfile().getId().toString());
                    break;
                }
                case UUID: {
                    this.main.uuid = UUID.fromString(packet.content());
                    ChatUtils.print("Got UUID from main " + this.main.name + ": " + packet.content());
                    this.sendPacket(TTCIC.PacketsCS.KEEPALIVE, "");
                    break;
                }
                case TPA: {
                    ChatUtils.print("TPA'ing main account...");
                    TTCp.player.sendChatMessage("/tpa " + this.main.name);
                    break;
                }
                case EXECUTE: {
                    ChatUtils.print("Sending message received from main account...");
                    ChatUtils.simulateSend(packet.content(), false);
                    break;
                }
                case LIST: {
                    TTCp.logger.info("Received alt list from main.");
                    Map<String, String> map0 = Utils.stringToMap(packet.content());
                    this.alts.clear();
                    int len = map0.keySet().size();
                    for (int i = 0; i < len; ++i) {
                        Alt alt = new Alt();
                        this.alts.add(alt);
                        Map<String, String> map1 = Utils.stringToMap(map0.get(String.valueOf(i)));
                        alt.name = map1.get("name");
                        alt.uuid = UUID.fromString(map1.get("uuid"));
                    }
                    break;
                }
                case KILL: {
                    ChatUtils.print("Killing player " + packet.content());
                    this.kill(packet.content());
                    break;
                }
                case FOLLOW: {
                    ChatUtils.print("Following " + packet.content());
                    this.follow(packet.content());
                    break;
                }
                case STOP: {
                    this.stop(packet.content());
                    break;
                }
                case CONFIG: {
                    break;
                }
                case WALK: {
                    this.useElytra = false;
                    FlightBot.deactivate();
                    break;
                }
                case ELYTRA: {
                    if (!this.useElytra && !this.stopped) {
                        ChatUtils.simulateSend("#stop", false);
                    }
                    this.useElytra = true;
                    break;
                }
                case KEEPALIVE: {
                    this.sendPacket(TTCIC.PacketsCS.KEEPALIVE, "");
                    break;
                }
                case POSITION: {
                    if (this.commonTargetPlayer != null || this.stopped) break;
                    Vector3d vec = Vector3d.fromMap(Utils.stringToMap(packet.content()));
                    FlightBot.deactivate();
                    this.commonTarget.set(new Vec3d(vec.getX(), vec.getY() + 2.0, vec.getZ()));
                    FlightBot.activate(this.commonTarget);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPacketCS(TTCIC.PacketCS packet, PBIC.Connection connection) throws PBIC.PBICException.PBICWriteException {
        ChatUtils.chatPrinterDebug().println("Received packet[" + (Object)((Object)packet.type()) + "]{" + packet.content() + "}");
        switch (packet.type()) {
            case NAME: {
                this.altsMap.get((Object)connection).name = packet.content();
                ChatUtils.print("Connection to alt " + packet.content() + " established!");
                connection.writePacket(TTCIC.getPacketSC(TTCIC.PacketsSC.NAME, TTCp.mc.getSession().getProfile().getName()));
                break;
            }
            case UUID: {
                this.altsMap.get((Object)connection).uuid = UUID.fromString(packet.content());
                ChatUtils.print("Got UUID from alt " + this.altsMap.get((Object)connection).name + ": " + packet.content());
                connection.writePacket(TTCIC.getPacketSC(TTCIC.PacketsSC.UUID, TTCp.mc.getSession().getProfile().getId().toString()));
                this.sendList();
                break;
            }
            case KEEPALIVE: {
                ThreadManager.run(() -> {
                    try {
                        Thread.sleep(10000L);
                        connection.writePacket(TTCIC.getPacketSC(TTCIC.PacketsSC.KEEPALIVE, ""));
                    }
                    catch (InterruptedException | PBIC.PBICException e) {
                        e.printStackTrace();
                    }
                });
                break;
            }
            case LOST: {
                EntityPlayerSP player = TTCp.player;
                if (player != null && TTCp.world != null) {
                    connection.writePacket(TTCIC.getPacketSC(TTCIC.PacketsSC.POSITION, new Vector3d(player.field_70165_t, player.field_70163_u, player.field_70161_v).toString()));
                }
                FlightBot.setSpeed(0.5);
                this.lostTimer = new Date().getTime();
            }
        }
    }

    public void sendPacketSC(TTCIC.PacketsSC type, String content) {
        if (this.server.connections.size() == 0) {
            return;
        }
        AsyncTask<Object> task = new AsyncTask<Object>(() -> {
            ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
            try {
                PBIC.Connection[] connections = this.server.connections.toArray(new PBIC.Connection[0]);
                for (int i = 0; i < connections.length; ++i) {
                    try {
                        connections[i].writePacket(TTCIC.getPacketSC(type, content));
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
            catch (Throwable e) {
                return e;
            }
            return new Object();
        });
        task.setTimeout((long)this.server.connections.size() * 1500L);
        this.pce(task.waitForFinish(0));
    }

    public void sendPacketDelayedSC(TTCIC.PacketsSC type, String content) {
        if (this.server.connections.size() == 0) {
            return;
        }
        AsyncTask<Object> task = new AsyncTask<Object>(() -> {
            ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
            try {
                PBIC.Connection[] connections = this.server.connections.toArray(new PBIC.Connection[0]);
                for (int i = 0; i < connections.length; ++i) {
                    try {
                        connections[i].writePacket(TTCIC.getPacketSC(type, content));
                        Thread.sleep(500L);
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
            catch (Throwable e) {
                return e;
            }
            return new Object();
        });
        task.setTimeout((long)this.server.connections.size() * 2000L);
        task.then(this::pce);
    }

    private void pce(Object r) {
        if (r instanceof Throwable || r == null) {
            String etype;
            ChatUtils.chatPrinterDebug().println("§c§lError during communication!");
            if (r == null) {
                etype = "ETimeout";
            } else if (r instanceof Exception) {
                etype = "EExceptionSend {" + ((Exception)r).getMessage() + "}";
                ((Throwable)r).printStackTrace(ChatUtils.chatPrinterDebug());
            } else {
                etype = "EErrorSend {" + ((Throwable)r).getMessage() + "}";
                ((Throwable)r).printStackTrace(ChatUtils.chatPrinterDebug());
            }
            ChatUtils.chatPrinterDebug().println(etype);
        }
    }

    public void sendPacket(TTCIC.PacketsCS type, String content) throws PBIC.PBICException.PBICWriteException {
        ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
        if (this.client == null) {
            throw new RuntimeException();
        }
        this.client.connection.writePacket(TTCIC.getPacketCS(type, content));
    }

    @Override
    public void onChat(String s, String[] args) {
        try {
            if (s.equals("server") && this.server == null) {
                this.main = new Alt();
                this.main.name = TTCp.mc.getSession().getProfile().getName();
                this.main.uuid = TTCp.mc.getSession().getProfile().getId();
                this.altsMap = new HashMap<PBIC.Connection, Alt>();
                this.server = new PBIC.Server(50278);
                this.server.onJoin.add(() -> {
                    PBIC.Connection theConnection = this.server.lastConnection;
                    AsyncTask<Object> task = new AsyncTask<Object>(() -> {
                        ChatUtils.chatPrinterDebug().println("Sending packet[INIT]{}");
                        try {
                            theConnection.writePacket(TTCIC.getPacketSC(TTCIC.PacketsSC.INIT, ""));
                        }
                        catch (Throwable e) {
                            return e;
                        }
                        ChatUtils.chatPrinterDebug().println("Done");
                        return new Object();
                    });
                    task.setTimeout(1500L);
                    this.pce(task.waitForFinish(0));
                    this.altsMap.put(theConnection, new Alt());
                    while (true) {
                        String string = "UNKNOWN";
                        try {
                            PBIC.Packet packet = theConnection.readPacket();
                            string = packet.getContent();
                            this.onPacketCS(TTCIC.getPacketCS(packet), theConnection);
                            continue;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("Packet: " + string);
                            continue;
                        }
                        break;
                    }
                });
                this.server.start();
                this.mode = 0;
                ChatUtils.print("§aServer started");
            }
            if (args[0].equals("client") && this.client == null) {
                this.client = args.length == 2 ? new PBIC.Client(args[1], 50278) : (args.length == 3 ? new PBIC.Client(args[1], Integer.parseInt(args[2])) : new PBIC.Client("127.0.0.1", 50278));
                ChatUtils.print("Client started");
                ThreadManager.run("TTCIC client receive thread", () -> {
                    while (true) {
                        String string = "UNKNOWN";
                        try {
                            PBIC.Packet packet = this.client.connection.readPacket();
                            string = packet.getContent();
                            this.onPacketSC(TTCIC.getPacketSC(packet));
                            continue;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("Packet: " + string);
                            this.onChat("end", "end".split(" "));
                            continue;
                        }
                        break;
                    }
                });
                this.mode = 1;
            }
            if (args.length >= 2) {
                String st;
                if (args[0].equals("send") && s.contains(" ")) {
                    st = s.substring(s.indexOf(" ") + 1);
                    this.sendPacketSC(TTCIC.PacketsSC.EXECUTE, st);
                    ChatUtils.simulateSend(st, false);
                }
                if (args[0].equals("kill") && s.contains(" ")) {
                    this.sendList();
                    st = s.substring(s.indexOf(" ") + 1);
                    if (this.useElytra) {
                        this.sendPacketSC(TTCIC.PacketsSC.ELYTRA, "");
                    } else {
                        this.sendPacketSC(TTCIC.PacketsSC.WALK, "");
                    }
                    this.sendPacketSC(TTCIC.PacketsSC.KILL, st);
                    if (this.botMain) {
                        this.kill(st);
                    }
                }
                if (args[0].equals("stop") && s.contains(" ")) {
                    st = s.substring(s.indexOf(" ") + 1);
                    this.sendPacketSC(TTCIC.PacketsSC.STOP, st);
                    ChatUtils.print("Stopping killing player " + st);
                    if (this.botMain) {
                        this.stop(st);
                    }
                }
                if (args[0].equals("follow")) {
                    if (this.useElytra) {
                        this.sendPacketSC(TTCIC.PacketsSC.ELYTRA, "");
                    } else {
                        this.sendPacketSC(TTCIC.PacketsSC.WALK, "");
                    }
                    this.sendPacketSC(TTCIC.PacketsSC.FOLLOW, args[1]);
                    this.follow(args[1]);
                }
            }
            if (s.equals("stop")) {
                if (this.useElytra) {
                    this.sendPacketSC(TTCIC.PacketsSC.ELYTRA, "");
                } else {
                    this.sendPacketSC(TTCIC.PacketsSC.WALK, "");
                }
                this.sendPacketSC(TTCIC.PacketsSC.STOP, "");
                ChatUtils.print("Stopping killing/following all players");
                if (this.botMain) {
                    this.stop(null);
                }
            }
            if (s.equals("send")) {
                TTCp.getInstance().setConfig();
                ChatUtils.print("Sending config to all alts");
            }
            if (s.equals("tpa")) {
                this.sendList();
                this.sendPacketDelayedSC(TTCIC.PacketsSC.TPA, "");
            }
            if (s.equals("follow")) {
                if (this.useElytra) {
                    this.sendPacketSC(TTCIC.PacketsSC.ELYTRA, "");
                } else {
                    this.sendPacketSC(TTCIC.PacketsSC.WALK, "");
                }
                this.sendPacketSC(TTCIC.PacketsSC.FOLLOW, this.main.name);
            }
            if (s.equals("telytra")) {
                boolean bl = this.useElytra = !this.useElytra;
            }
            if (s.equals("end")) {
                this.alts.clear();
                while (this.toSend.hasNext()) {
                    this.toSend.next();
                }
                this.altsMap.clear();
                this.stopped = false;
                this.useElytra = false;
                this.commonTargetPlayer = null;
                this.commonTarget.set(null);
                this.stopped = false;
                this.main = new Alt();
                if (this.client != null) {
                    this.client.close();
                }
                this.client = null;
                if (this.server != null) {
                    this.server.close();
                }
                this.server = null;
                this.mode = -1;
                this.alts = new ArrayList();
                this.altsMap = new HashMap<PBIC.Connection, Alt>();
            }
            if (s.equals("list")) {
                int i;
                StringBuilder string = new StringBuilder("List:");
                if (this.server != null) {
                    for (i = 0; i < this.server.connections.size(); ++i) {
                        PBIC.Connection connection = this.server.connections.get(i);
                        Alt alt = this.altsMap.get(connection);
                        if (alt == null || alt.name == null) {
                            this.onChat("end", "end".split(" "));
                            continue;
                        }
                        string.append(" ").append(alt.name).append(",");
                    }
                }
                if (this.client != null) {
                    for (i = 0; i < this.alts.size(); ++i) {
                        Alt alt = this.alts.get(i);
                        if (alt == null || alt.name == null) {
                            this.onChat("end", "end".split(" "));
                            continue;
                        }
                        string.append(" ").append(alt.name).append(",");
                    }
                }
                if (string.toString().contains(",")) {
                    string = new StringBuilder(string.substring(0, string.length() - 2));
                }
                ChatUtils.print(string.toString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.updateBinds();
    }

    private void sendList() {
        if (this.server.connections.size() == 0) {
            return;
        }
        HashMap<String, String> map0 = new HashMap<String, String>();
        PBIC.Connection[] keys = this.altsMap.keySet().toArray(new PBIC.Connection[0]);
        this.alts.clear();
        for (int i = 0; i < keys.length; ++i) {
            Alt alt = this.altsMap.get(keys[i]);
            this.alts.add(alt);
            HashMap<String, String> map1 = new HashMap<String, String>();
            map1.put("name", alt.name);
            map1.put("uuid", alt.uuid.toString());
            map0.put(String.valueOf(i), Utils.mapToString(map1));
        }
        this.sendPacketSC(TTCIC.PacketsSC.LIST, Utils.mapToString(map0));
    }

    public void follow(String name) {
        if (TTCp.player.func_70005_c_().equals(name)) {
            return;
        }
        this.commonTargetPlayer = TTCp.world.getPlayerEntityByName(name);
        this.follow();
    }

    public void kill(String name) {
        this.follow(name);
        KillAura aura = KillAura.getInstance();
        aura.enabled = true;
        aura.onEnable();
        aura.targets.add(name);
    }

    public void stop(String name) {
        KillAura aura = KillAura.getInstance();
        this.commonTargetPlayer = null;
        this.commonTarget.set(null);
        this.stopped = true;
        FlightBot.deactivate();
        if (!this.useElytra) {
            ChatUtils.simulateSend("#stop", false);
        }
        if (name == null || name.equals("")) {
            aura.targets.clear();
            aura.enabled = false;
            aura.onDisable();
        } else {
            aura.targets.remove(name);
            aura.targets.trimToSize();
            if (aura.targets.size() != 0) {
                ChatUtils.print("Killing player " + name);
                this.follow(aura.targets.get(0));
            }
        }
    }

    public void follow() {
        if (this.commonTargetPlayer == null) {
            FlightBot.deactivate();
            return;
        }
        this.stopped = false;
        try {
            if (this.useElytra) {
                FlightBot.deactivate();
                FlightBot.activate(this.commonTarget);
                this.commonTarget.set(this.commonTargetPlayer.func_174791_d().addVector(0.0, 2.0, 0.0));
            } else {
                ChatUtils.simulateSend("#follow player " + this.commonTargetPlayer.getName(), false);
            }
        }
        catch (Exception e) {
            e.printStackTrace(ChatUtils.chatPrinter());
        }
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        if (s.contains("has requested to teleport to you.") && this.alts.stream().anyMatch(alt -> s.startsWith(alt.name + " ") || s.startsWith("~" + alt.name + " "))) {
            TTCp.player.sendChatMessage("/tpaccept");
        }
        return false;
    }

    @Override
    public void onDisable() {
        this.onChat("end", null);
    }

    public static class Alt {
        public String name;
        public UUID uuid;
    }
}
