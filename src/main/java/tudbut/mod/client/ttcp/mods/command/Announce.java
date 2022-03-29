package tudbut.mod.client.ttcp.mods.command;

import tudbut.api.impl.TudbuTAPI;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;
import tudbut.net.http.HTTPUtils;

@Command
public class Announce
extends Module {
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            String pwd = args[0];
            TudbuTAPI.get("admin/announce", "key=" + HTTPUtils.encodeUTF8(pwd) + "&message=" + HTTPUtils.encodeUTF8(s.substring(pwd.length() + 1)));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}
