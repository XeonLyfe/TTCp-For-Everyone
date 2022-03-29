package tudbut.mod.client.ttcp.mods.command;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;

@Command
public class Bind
extends Module {
    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if (s.equals("help")) {
            ChatUtils.print("§a§lBinds");
            for (int i = 0; i < TTCp.modules.length; ++i) {
                ChatUtils.print("§aModule: " + TTCp.modules[i].toString());
                if (TTCp.modules[i].key.key != null) {
                    ChatUtils.print("State: " + Keyboard.getKeyName((int)TTCp.modules[i].key.key));
                }
                for (String kb : TTCp.modules[i].customKeyBinds.keys()) {
                    if (TTCp.modules[i].customKeyBinds.get((String)kb).key != null) {
                        ChatUtils.print("Function " + kb + ": " + Keyboard.getKeyName((int)TTCp.modules[i].customKeyBinds.get((String)kb).key));
                        continue;
                    }
                    ChatUtils.print("Function " + kb);
                }
            }
            return;
        }
        for (int i = 0; i < TTCp.modules.length; ++i) {
            if (!args[0].equalsIgnoreCase(TTCp.modules[i].toString().toLowerCase())) continue;
            if (args.length == 2) {
                int key = Keyboard.getKeyIndex((String)args[1].toUpperCase());
                if (key == 0) {
                    TTCp.modules[i].customKeyBinds.get((String)args[1]).key = null;
                    continue;
                }
                TTCp.modules[i].key.key = key;
                continue;
            }
            if (args.length == 3) {
                if (TTCp.modules[i].customKeyBinds.keys().contains(args[1])) {
                    TTCp.modules[i].customKeyBinds.get((String)args[1]).key = Keyboard.getKeyIndex((String)args[2].toUpperCase());
                    continue;
                }
                ChatUtils.print("Function not found");
                continue;
            }
            TTCp.modules[i].key.key = null;
        }
    }
}
