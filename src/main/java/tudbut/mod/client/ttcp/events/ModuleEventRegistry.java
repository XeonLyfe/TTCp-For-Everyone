package tudbut.mod.client.ttcp.events;

import java.util.ArrayList;
import tudbut.mod.client.ttcp.utils.Module;

public class ModuleEventRegistry {
    public static ArrayList<Module> disableOnNewPlayer = new ArrayList();

    static void onNewPlayer() {
        for (Module module : disableOnNewPlayer) {
            if (!module.enabled) continue;
            module.toggle();
        }
    }
}
