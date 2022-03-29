package tudbut.mod.client.ttcp.mods.command;

import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;

@Command
public class Prefix
extends Module {
    public Prefix() {
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
        TTCp.prefix = s;
    }
}
