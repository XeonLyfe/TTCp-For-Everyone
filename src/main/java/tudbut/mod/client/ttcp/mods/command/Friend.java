package tudbut.mod.client.ttcp.mods.command;

import java.util.ArrayList;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;
import tudbut.obj.Save;

@Command
public class Friend
extends Module {
    static Friend instance;
    @Save
    public ArrayList<String> names = new ArrayList();

    public Friend() {
        instance = this;
    }

    public static Friend getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEverySubTick() {
        this.enabled = true;
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
        switch (args[0].toLowerCase()) {
            case "add": {
                this.names.remove(args[1]);
                this.names.add(args[1]);
                ChatUtils.print("Done!");
                break;
            }
            case "remove": {
                this.names.remove(args[1]);
                ChatUtils.print("Done!");
                break;
            }
            case "list": {
                StringBuilder toPrint = new StringBuilder("Friend: ");
                for (String name : this.names) {
                    toPrint.append(name).append(", ");
                }
                if (this.names.size() >= 1) {
                    toPrint.delete(toPrint.length() - 2, toPrint.length() - 1);
                }
                ChatUtils.print(toPrint.toString());
            }
        }
        this.updateBinds();
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }
}
