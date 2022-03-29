package tudbut.mod.client.ttcp.mods.chat;

import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Chat;
import tudbut.obj.Save;

@Chat
public class TPAParty
extends Module {
    static TPAParty instance;
    @Save
    public boolean disableOnDeath = true;

    public TPAParty() {
        instance = this;
    }

    public void updateButtons() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("DeathDisable", this, "disableOnDeath"));
    }

    public static TPAParty getInstance() {
        return instance;
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        if (s.contains("has requested to teleport to you.") && !s.startsWith("<")) {
            TTCp.player.sendChatMessage("/tpaccept");
        }
        return false;
    }

    @Override
    public int danger() {
        return 4;
    }
}
