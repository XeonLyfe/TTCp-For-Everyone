package tudbut.mod.client.ttcp.utils;

import java.io.IOException;
import java.util.UUID;
import tudbut.mod.client.ttcp.utils.WebServices;
import tudbut.net.pbic2.PBIC2AListener;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;

final class WebServices$1
implements PBIC2AListener {
    WebServices$1() {
    }

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
}
