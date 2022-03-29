package tudbut.mod.client.ttcp.mods.chat;

import java.util.Arrays;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Chat;

@Chat
public class DM
extends Module {
    public static DM instance;
    public String[] users = new String[0];

    public DM() {
        instance = this;
    }

    public static DM getInstance() {
        return instance;
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        this.users = args;
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        try {
            String name = (String)Arrays.stream(this.users).filter(theName -> s.startsWith(theName + " whispers:") || s.startsWith("~" + theName + " whispers:") || s.startsWith(theName + " whispers to you:") || s.startsWith("~" + theName + " whispers to you:") || s.startsWith("From " + theName + ":") || s.startsWith("From ~" + theName + ":")).iterator().next();
            if (name != null) {
                ChatUtils.print("§b§lDM from conversation partner: §r<" + name + "> " + s.substring(s.indexOf(": ") + 2));
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    @Override
    public int danger() {
        return 1;
    }
}
