package tudbut.mod.client.ttcp.mods.chat;

import java.util.UUID;
import tudbut.api.impl.TudbuTAPI;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Chat;

@Chat
public class Msg
extends Module {
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if (args.length == 0) {
            ChatUtils.print("§aPlayers online: " + String.join((CharSequence)" ", new String[0]));
            return;
        }
        try {
            String name = args[0];
            UUID uuid = TudbuTAPI.getUUID(name);
            TudbuTAPIV2.request(this.player.func_110124_au(), "message", "other=" + uuid, s.substring(name.length() + 1));
            ChatUtils.print("Done.");
        }
        catch (Exception e) {
            ChatUtils.print("Couldn't find that player! Usage: ,msg <name> <message...>");
        }
    }
}
