package tudbut.mod.client.ttcp.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import tudbut.api.impl.RateLimit;
import tudbut.api.impl.TudbuTAPI;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.KillSwitch;
import tudbut.net.pbic2.PBIC2;
import tudbut.net.pbic2.PBIC2AEventHandler;
import tudbut.net.pbic2.PBIC2AListener;
import tudbut.obj.DoubleTypedObject;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.Lock;

public class WebServices {
    public static Lock handshakeLock;
    public static UUID uuid;
    public static UUID lastMessaged;
    public static PBIC2 client;
    public static PBIC2AEventHandler handler;
    public static Lock keepAliveLock;
    private static final PBIC2AListener listener;
    public static String[] usersOnline;
    static ArrayList<TCN> queuedMessages;

    public static void handshake() throws IOException, RateLimit {
        if (handshakeLock.isLocked()) {
            return;
        }
        TTCp.logger.info("Starting handshake");
        handshakeLock.lock();
        TudbuTAPIV2.handshake(uuid);
        if (client != null) {
            client.getSocket().close();
            handler.remove(client);
        }
        TTCp.logger.info("Handshake passed");
    }

    private static void login() throws IOException, RateLimit {
        DoubleTypedObject<Boolean, String> s = TudbuTAPIV2.request(uuid, "track/login", "TTCp TudbuT/ttcp:master@vB1.9.0");
        if (!((Boolean)s.o).booleanValue() || !((String)s.t).equals("OK")) {
            TTCp.logger.info("Error during login. Redoing handshake. Res: " + (String)s.t);
            WebServices.doLogin();
            return;
        }
        client = TudbuTAPIV2.connectGateway(uuid);
        handler.start(client, listener);
    }

    private static boolean play() throws IOException, RateLimit {
        DoubleTypedObject<Boolean, String> s = TudbuTAPIV2.request(uuid, "track/play", "");
        if (((String)s.t).equals("DISABLE")) {
            KillSwitch.deactivate();
        }
        if (!((Boolean)s.o).booleanValue()) {
            System.out.println((String)s.t);
        }
        return (Boolean)s.o;
    }

    public static void doLogin() {
        try {
            keepAliveLock.lock(15000);
            handshakeLock.unlock();
            Thread.sleep(1000L);
            WebServices.handshake();
            Thread.sleep(1000L);
            WebServices.login();
            Thread.sleep(1000L);
            WebServices.play();
        }
        catch (Exception e) {
            e.printStackTrace();
            TTCp.logger.info("Can't reach api.tudbut.de");
        }
    }

    public static void trackPlay() {
        try {
            if (TTCp.isIngame()) {
                WebServices.sendQueuedMessages();
            }
            if (!keepAliveLock.isLocked()) {
                handler.remove(client);
                TTCp.logger.info("Reconnecting gateway...");
                if (!((String)TudbuTAPIV2.request((UUID)Minecraft.getMinecraft().getSession().getProfile().getId(), (String)"check", (String)"").t).equals("OK")) {
                    TTCp.logger.info("Couldn't reconnect gateway. Redoing handshake...");
                    WebServices.doLogin();
                    return;
                }
                client = TudbuTAPIV2.connectGateway(Minecraft.getMinecraft().getSession().getProfile().getId());
                handler.start(client, listener);
                keepAliveLock.lock(20000);
            }
            if (!WebServices.play()) {
                TTCp.logger.info("Couldn't send track/play. Redoing handshake.");
                WebServices.doLogin();
            }
        }
        catch (Exception ignored) {
            WebServices.doLogin();
        }
    }

    public static void queueMessage(TCN event) {
        queuedMessages.add(event);
        if (TTCp.isIngame()) {
            WebServices.sendQueuedMessages();
        }
    }

    public static synchronized void sendQueuedMessages() {
        int queuedMessagesSize = queuedMessages.size();
        for (int i = 0; i < queuedMessagesSize; ++i) {
            TCN queuedMessage = queuedMessages.get(i);
            ChatUtils.print("§a[TTC] §lGOT MESSAGE");
            ChatUtils.print("§a[TTC] <" + queuedMessage.getSub("from").getSub("record").getString("name") + "> " + queuedMessage.getString("message"));
            queuedMessages.remove(queuedMessage);
        }
    }

    static {
        TudbuTAPI.port = 83;
        TudbuTAPI.portHTTP = 82;
        handshakeLock = new Lock();
        uuid = TTCp.mc.getSession().getProfile().getId();
        lastMessaged = null;
        handler = new PBIC2AEventHandler();
        keepAliveLock = new Lock(true);
        keepAliveLock.lock(20000);
        listener = new PBIC2AListener(){

            @Override
            public void onMessage(String s) throws IOException {
                try {
                    TCN tcn = JSON.read(s);
                    keepAliveLock.lock(20000);
                    if (tcn.getString("id").equalsIgnoreCase("message")) {
                        String fromUUID = tcn.getSub("from").getString("uuid");
                        lastMessaged = UUID.fromString(fromUUID);
                        WebServices.queueMessage(tcn);
                    }
                    if (tcn.getString("id").equalsIgnoreCase("save") || tcn.getString("id").equalsIgnoreCase("welcome")) {
                        usersOnline = tcn.getSub("data").getArray("onlineUsernames").toArray(new String[0]);
                        client.writeMessage("OK");
                    }
                }
                catch (JSON.JSONFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                keepAliveLock.unlock();
                throwable.printStackTrace();
            }
        };
        usersOnline = new String[0];
        queuedMessages = new ArrayList();
    }
}
