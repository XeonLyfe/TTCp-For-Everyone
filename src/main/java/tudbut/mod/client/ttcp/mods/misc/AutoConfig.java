package tudbut.mod.client.ttcp.mods.misc;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.chat.TPAParty;
import tudbut.mod.client.ttcp.mods.chat.TPATools;
import tudbut.mod.client.ttcp.mods.chat.Team;
import tudbut.mod.client.ttcp.mods.combat.AutoTotem;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;

@Misc
public class AutoConfig
extends Module {
    @Save
    private boolean mode = false;
    @Save
    private boolean stackedTots = false;
    @Save
    private boolean pvp = false;
    @Save
    private boolean tpa = false;
    private Server server = Server._8b8t;

    @Override
    public void onEnable() {
        this.updateBinds();
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Mode: " + (this.mode ? "Server" : "Custom"), it -> {
            this.mode = !this.mode;
            it.text = "Mode: " + (this.mode ? "Server" : "Custom");
            this.updateBinds();
        }));
        if (this.mode) {
            this.subComponents.add(new Button("Server: " + this.server.name, it -> {
                int i = this.server.ordinal();
                i = Keyboard.isKeyDown((int)42) ? --i : ++i;
                if (i >= Server.values().length) {
                    i = 0;
                }
                if (i < 0) {
                    i = Server.values().length - 1;
                }
                this.server = Server.values()[i];
                it.text = "Server: " + this.server.name;
            }));
        } else {
            this.subComponents.add(new Button("Has stacked totems: " + this.stackedTots, it -> {
                this.stackedTots = !this.stackedTots;
                it.text = "Has stacked totems: " + this.stackedTots;
            }));
            this.subComponents.add(new Button("PvP meta: " + (this.pvp ? "32k" : "Crystal"), it -> {
                this.pvp = !this.pvp;
                it.text = "PvP meta: " + (this.pvp ? "32k" : "Crystal");
            }));
            this.subComponents.add(new Button("Has /tpa: " + this.tpa, it -> {
                this.tpa = !this.tpa;
                it.text = "Has /tpa: " + this.tpa;
            }));
        }
        this.subComponents.add(new Button("Set", it -> {
            if (this.mode) {
                this.stackedTots = this.server.stackedTots;
                this.pvp = this.server.pvp;
                this.tpa = this.server.tpa;
            }
            int i = 0;
            if (this.stackedTots) {
                i += this.pvp ? 4 : 2;
            }
            AutoTotem.getInstance().origMinCount = i;
            Team.getInstance().enabled = this.tpa;
            if (!this.tpa) {
                TPAParty.getInstance().enabled = false;
            }
            TPATools.getInstance().enabled = this.tpa;
            ThreadManager.run(() -> {
                it.text = "Done";
                try {
                    Thread.sleep(2000L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                it.text = "Set";
            });
        }));
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    private static enum Server {
        _8b8t("8b8t.xyz", true, true, true),
        _5b5t("5b5t.net", false, false, false),
        _0t0t("0b0t.org", false, false, true),
        _2b2t("2b2t.org", false, false, false),
        crystalpvp("crystalpvp.cc", false, false, false);

        String name;
        boolean stackedTots;
        boolean pvp;
        boolean tpa;

        private Server(String name, boolean stackedTots, boolean pvp, boolean tpa) {
            this.name = name;
            this.stackedTots = stackedTots;
            this.pvp = pvp;
            this.tpa = tpa;
        }
    }
}
