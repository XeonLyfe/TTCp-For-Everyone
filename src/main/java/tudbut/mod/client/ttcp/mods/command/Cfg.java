package tudbut.mod.client.ttcp.mods.command;

import java.io.File;
import java.io.IOException;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;

@Command
public class Cfg
extends Module {
    String cfg = "main";

    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onChat(String s, String[] args) {
        if (args.length == 2) {
            if (args[0].equals("use")) {
                if (new File("config/ttc/" + args[1] + ".cfg").exists()) {
                    ChatUtils.print("Loading config " + args[1]);
                    try {
                        TTCp.getInstance().setConfig(args[1]);
                        this.cfg = args[1];
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    ChatUtils.print("Done!");
                } else {
                    ChatUtils.print("That config doesn't exist, try `cfg save " + args[1] + "`!");
                }
            }
            if (args[0].equals("save")) {
                ChatUtils.print("Saving to " + args[1]);
                try {
                    TTCp.getInstance().saveConfig(args[1]);
                    this.cfg = args[1];
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                ChatUtils.print("Done!");
            }
        } else {
            ChatUtils.print("Current: " + this.cfg);
        }
    }
}
