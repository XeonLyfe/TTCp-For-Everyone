package tudbut.mod.client.ttcp.mods.chat;

import java.util.Objects;
import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.chat.TPATools;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.category.Chat;

@Chat
public class DMAll
extends Module {
    public DMAll() {
        this.enabled = true;
    }

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onEverySubTick() {
        this.enabled = true;
    }

    @Override
    public void onChat(String s, String[] args) {
        ChatUtils.print("Sending...");
        ThreadManager.run(() -> {
            for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                try {
                    TTCp.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " " + s);
                    ChatUtils.print("Sent to " + info.getGameProfile().getName());
                    Thread.sleep(TPATools.getInstance().delay);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            ChatUtils.print("Done!");
        });
    }
}
