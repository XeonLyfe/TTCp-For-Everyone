package tudbut.mod.client.ttcp.mods.command;

import java.util.UUID;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.WebServices;
import tudbut.mod.client.ttcp.utils.category.Command;

@Command
public class R
extends Module {
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            UUID uuid = WebServices.lastMessaged;
            if (uuid == null) {
                ChatUtils.print("No one messaged you recently!");
                return;
            }
            TudbuTAPIV2.request(this.player.func_110124_au(), "message", "other=" + uuid, s);
            ChatUtils.print("Done.");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}
