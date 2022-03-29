package tudbut.mod.client.ttcp.utils;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import de.tudbut.tools.Hasher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.EventHandler;
import tudbut.mod.client.ttcp.mods.chat.Team;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.misc.AltControl;
import tudbut.net.http.HTTPHeader;
import tudbut.net.http.HTTPRequest;
import tudbut.net.http.HTTPRequestType;
import tudbut.net.http.HTTPResponse;
import tudbut.parsing.TCN;

public class Utils {
    private static float rotationX;
    private static float rotationY;
    private static boolean rotationUpdated;

    public static void markRotationSent() {
        rotationUpdated = false;
    }

    public static Vec2f getRotation() {
        if (!rotationUpdated) {
            return null;
        }
        return new Vec2f(rotationX, rotationY);
    }

    public static void setRotation(float x, float y) {
        rotationX = x;
        rotationY = y;
        rotationUpdated = true;
    }

    public static void setRotation(Vec2f vec) {
        Utils.setRotation(vec.x, vec.y);
    }

    public static float tpsMultiplier() {
        return EventHandler.tps / 20.0f;
    }

    public static long[] getPingToServer(ServerData server) {
        server = new ServerData(server.serverName, server.serverIP, server.isOnLAN());
        try {
            long sa = new Date().getTime();
            final AtomicLong pingSentAt = new AtomicLong();
            final AtomicBoolean done = new AtomicBoolean(false);
            ServerAddress serveraddress = ServerAddress.fromString((String)server.serverIP);
            final NetworkManager networkmanager = NetworkManager.createNetworkManagerAndConnect((InetAddress)InetAddress.getByName(serveraddress.getIP()), (int)serveraddress.getPort(), (boolean)false);
            server.pingToServer = -1L;
            final long[] players = new long[]{1L, 1L};
            final ServerData finalServer = server;
            networkmanager.setNetHandler((INetHandler)new INetHandlerStatusClient(){

                public void func_147231_a(@Nullable ITextComponent reason) {
                    done.set(true);
                }

                public void handleServerInfo(@Nullable SPacketServerInfo packetIn) {
                    pingSentAt.set(System.currentTimeMillis());
                    networkmanager.sendPacket((Packet)new CPacketPing(pingSentAt.get()));
                    try {
                        assert (packetIn != null);
                        players[0] = packetIn.getResponse().getPlayers().getOnlinePlayerCount();
                        players[1] = packetIn.getResponse().getPlayers().getMaxPlayers();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }

                public void handlePong(@Nullable SPacketPong packetIn) {
                    long j = System.currentTimeMillis();
                    finalServer.pingToServer = j - pingSentAt.get();
                    networkmanager.closeChannel((ITextComponent)new TextComponentString("Finished"));
                    done.set(true);
                }
            });
            networkmanager.sendPacket((Packet)new C00Handshake(serveraddress.getIP(), serveraddress.getPort(), EnumConnectionState.STATUS, false));
            networkmanager.sendPacket((Packet)new CPacketServerQuery());
            while (!done.get() && new Date().getTime() - sa < 7500L) {
            }
            return new long[]{server.pingToServer, players[0], players[1]};
        }
        catch (Throwable ignored) {
            return new long[]{-1L, 1L, 1L};
        }
    }

    public static String getPasswordFor(UUID uuid) throws IOException {
        HTTPRequest request = new HTTPRequest(HTTPRequestType.GET, "api.tudbut.de", 82, "/api/getHashedPassword?uuid=" + uuid.toString(), new HTTPHeader[0]);
        HTTPResponse req = request.send();
        return req.parse().getBody();
    }

    public static boolean setPassword(String currentPassword, String newPassword) {
        GameProfile profile = TTCp.mc.getSession().getProfile();
        try {
            return new HTTPRequest(HTTPRequestType.GET, "api.tudbut.de", 82, "/api/setPassword?uuid=" + profile.getId().toString() + "&key=" + URLEncoder.encode(currentPassword, "UTF8") + "&password=" + URLEncoder.encode(Hasher.sha512hex(Hasher.sha256hex(newPassword)), "UTF8"), new HTTPHeader[0]).send().parse().getBody().startsWith("Set!");
        }
        catch (IOException e) {
            return false;
        }
    }

    public static float roundTo(float f, int p) {
        p = (int)Math.pow(10.0, p);
        return (float)Math.round(f * (float)p) / (float)p;
    }

    public static boolean isCallingFrom(Class<?> clazz) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < trace.length; ++i) {
            if (!trace[i].getClassName().equals(clazz.getName())) continue;
            return true;
        }
        return false;
    }

    public static Object getPrivateField(Class<?> clazz, Object instance, String field) {
        try {
            Field f = clazz.getDeclaredField(field);
            boolean b = f.isAccessible();
            f.setAccessible(true);
            Object t = f.get(instance);
            f.setAccessible(b);
            return t;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static void setPrivateField(Class<?> clazz, Object instance, String field, Object content) {
        try {
            Field f = clazz.getDeclaredField(field);
            boolean b = f.isAccessible();
            f.setAccessible(true);
            f.set(instance, content);
            f.setAccessible(b);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String[] getFieldsForType(Class<?> clazz, Class<?> type) {
        try {
            Field[] all = clazz.getDeclaredFields();
            ArrayList<String> names = new ArrayList<String>();
            for (int i = 0; i < all.length; ++i) {
                if (all[i].getType() != type) continue;
                names.add(all[i].getName());
            }
            return names.toArray(new String[0]);
        }
        catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static <T> T[] getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        ArrayList<Entity> list = Lists.newArrayList();
        List loadedEntityList = TTCp.world.loadedEntityList;
        for (int i = 0; i < loadedEntityList.size(); ++i) {
            Entity entity4 = (Entity)loadedEntityList.get(i);
            if (!entityType.isAssignableFrom(entity4.getClass()) || !filter.test(entity4)) continue;
            list.add(entity4);
        }
        return list.toArray((Object[])Array.newInstance(entityType, 0));
    }

    public static String removeNewlines(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("\n", "").replaceAll("\r", "");
    }

    public static TCN getData() {
        try {
            String s;
            URL updateCheckURL = new URL("https://raw.githubusercontent.com/TudbuT/ttcp-data/master/data_main");
            InputStream stream = updateCheckURL.openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                builder.append(s).append("\n");
            }
            return TCN.read(builder.toString());
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getLatestVersion() {
        return "vB1.9.0";
    }

    public static int[] objectArrayToNativeArray(Integer[] oa) {
        int[] na = new int[oa.length];
        for (int i = 0; i < na.length; ++i) {
            na[i] = oa[i];
        }
        return na;
    }

    public static int[] range(int min, int max) {
        int[] r = new int[max - min];
        int i = min;
        int j = 0;
        while (i < max) {
            r[j] = i++;
            ++j;
        }
        return r;
    }

    public static int[] add(int[] array0, int[] array1) {
        int[] r = new int[array0.length + array1.length];
        System.arraycopy(array0, 0, r, 0, array0.length);
        System.arraycopy(array1, 0, r, 0 - array0.length, array1.length);
        return r;
    }

    public static Map<String, String> stringToMap(String mapStringParsable) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] splitTiles = mapStringParsable.split(";");
        for (int i = 0; i < splitTiles.length; ++i) {
            String tile = splitTiles[i];
            String[] splitTile = tile.split(":");
            if (!tile.contains(":")) continue;
            if (splitTile.length == 2) {
                map.put(splitTile[0].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"), splitTile[1].equals("%N") ? null : splitTile[1].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"));
                continue;
            }
            map.put(splitTile[0].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"), "");
        }
        return map;
    }

    public static String mapToString(Map<String, String> map) {
        StringBuilder r = new StringBuilder();
        for (String key : map.keySet().toArray(new String[0])) {
            r.append(key.replaceAll("%", "%P").replaceAll(";", "%B").replaceAll(":", "%I")).append(":").append(map.get(key) == null ? "%N" : map.get(key).replaceAll("%", "%P").replaceAll(";", "%B").replaceAll(":", "%I")).append(";");
        }
        return r.toString();
    }

    public static NetworkPlayerInfo[] getPlayerList() {
        return Minecraft.getMinecraft().getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
    }

    public static NetworkPlayerInfo getPlayerListPlayer(String name) {
        for (NetworkPlayerInfo p : Utils.getPlayerList()) {
            if (!p.getGameProfile().getName().equals(name)) continue;
            return p;
        }
        return null;
    }

    public static NetworkPlayerInfo getPlayerListPlayerIgnoreCase(String name) {
        for (NetworkPlayerInfo p : Utils.getPlayerList()) {
            if (!p.getGameProfile().getName().equalsIgnoreCase(name)) continue;
            return p;
        }
        return null;
    }

    public static NetworkPlayerInfo getPlayerListPlayer(UUID uuid) {
        for (NetworkPlayerInfo p : Utils.getPlayerList()) {
            if (!p.getGameProfile().getId().equals(uuid)) continue;
            return p;
        }
        return null;
    }

    public static Method[] getMethods(Class<GuiIngame> clazz, Class<?> ... args) {
        ArrayList<Method> methods = new ArrayList<Method>();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; ++i) {
            Method m = declaredMethods[i];
            if (!Arrays.equals(m.getParameterTypes(), args)) continue;
            methods.add(m);
        }
        return methods.toArray(new Method[0]);
    }

    public static EntityPlayer[] getAllies() {
        EntityPlayer[] players = TTCp.world.playerEntities.toArray(new EntityPlayer[0]);
        ArrayList<EntityPlayer> allies = new ArrayList<EntityPlayer>();
        for (int i = 0; i < players.length; ++i) {
            if (!players[i].func_110124_au().equals(TTCp.mc.getSession().getProfile().getId()) && !Team.getInstance().names.contains(players[i].getGameProfile().getName()) && !Friend.getInstance().names.contains(players[i].getGameProfile().getName()) && !AltControl.getInstance().isAlt(players[i])) continue;
            allies.add(players[i]);
        }
        return allies.toArray(new EntityPlayer[0]);
    }

    public static int trunc(double d) {
        return (int)(d < 0.0 ? Math.ceil(d) : Math.floor(d));
    }

    static {
        rotationUpdated = false;
    }
}
