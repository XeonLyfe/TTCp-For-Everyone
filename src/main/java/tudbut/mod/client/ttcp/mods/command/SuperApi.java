package tudbut.mod.client.ttcp.mods.command;

import de.tudbut.timer.AsyncTask;
import de.tudbut.type.Nothing;
import java.text.DateFormat;
import java.util.Date;
import tudbut.api.impl.TudbuTAPI;
import tudbut.api.impl.UserRecord;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Command;
import tudbut.tools.Time;

@Command
public class SuperApi
extends Module {
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public boolean doStoreEnabled() {
        return false;
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public void onChat(String s, String[] args) {
        new AsyncTask<Nothing>(() -> {
            try {
                this.printData(TudbuTAPI.adminGetUserRecord(Utils.getPlayerListPlayerIgnoreCase(args[0]).getGameProfile().getId(), args[1]));
            }
            catch (Exception e) {
                try {
                    this.printData(TudbuTAPI.adminGetUserRecordByName(args[0], args[1]));
                }
                catch (Exception ex) {
                    ChatUtils.print("Couldn't find that player on api.tudbut.de");
                }
            }
            return null;
        });
    }

    private void printData(UserRecord record) {
        DateFormat f = DateFormat.getDateTimeInstance();
        String s = "";
        s = s + "Last login: " + f.format(new Date(record.lastLogin)) + " (" + Time.ydhms((new Date().getTime() - record.lastLogin) / 1000L).split("y ")[1] + " ago)\n";
        s = s + "Playtime: " + Time.ydhms(record.playTime) + "\n";
        s = s + "Premium: " + (record.registered ? "Yes" : "No") + "\n";
        s = s + "Last playing: " + f.format(new Date(record.lastPlayRequest)) + " (" + Time.ydhms((new Date().getTime() - record.lastPlayRequest) / 1000L).split("y ")[1] + " ago for " + Time.ydhms(Math.abs(record.lastPlayRequest - record.lastLogin) / 1000L).split("y ")[1] + ")\n";
        s = s + "Online: " + (new Date().getTime() - record.lastPlayRequest < 2000L ? "Yes" : "No");
        ChatUtils.print(s);
    }
}
