package tudbut.mod.client.ttcp.mods.command;

import java.io.IOException;
import tudbut.api.impl.RateLimit;
import tudbut.api.impl.TudbuTAPI;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;

@Command
public class Password
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
        try {
            if (TudbuTAPI.setPassword(mc.getSession().getProfile().getId(), args[0], args[1]).equals("Set!")) {
                ChatUtils.print("Password set!");
            } else {
                ChatUtils.print("Couldn't set password!");
            }
        }
        catch (IOException | RateLimit e) {
            e.printStackTrace();
        }
    }
}
