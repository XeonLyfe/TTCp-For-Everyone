package tudbut.mod.client.ttcp.mods.chat;

import java.util.Arrays;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Chat;

@Chat
public class DMChat
extends Module {
    public static DMChat instance;
    public String[] users = new String[0];

    public DMChat() {
        instance = this;
    }

    public static DMChat getInstance() {
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
                ChatUtils.print("<" + name + "> " + s.substring(s.indexOf(": ") + 2));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return true;
    }

    @Override
    public int danger() {
        return 1;
    }
}
